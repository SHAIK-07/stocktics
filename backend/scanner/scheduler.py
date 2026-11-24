"""
Stocktics Daily Pattern Scanner Scheduler
Runs pattern detection daily once across all 1,000 US stocks strictly in non-market hours.

US Market Schedule:
- Regular Trading Hours: 09:30 - 16:00 Eastern Time (ET), Monday - Friday.
- Non-Market Hours:
    * Weekdays: 16:30 - 09:00 ET (After market close to pre-market open)
    * Target Scan Window: 17:00 ET (5:00 PM ET / 21:00 UTC / ~02:30 AM IST)
    * Weekends: Saturday and Sunday (All day non-market hours)

Behavior:
1. Runs once per trading day after US market closes (17:00 ET).
2. If server was offline at 17:00 ET, catches up during remaining non-market hours.
3. Will NEVER execute during active trading hours (09:30 - 16:00 ET).
4. Persists execution state to local JSON file to ensure idempotency across server restarts.
"""

import os
import json
import time
import logging
import threading
from datetime import datetime, timedelta, time as dtime
from zoneinfo import ZoneInfo
from pathlib import Path
from typing import Dict, Any, Optional

from backend.scanner.pipeline import run_pipeline

logger = logging.getLogger("DailyPatternScannerScheduler")

STATE_FILE = Path(__file__).resolve().parent / "scheduler_state.json"
NY_TZ = ZoneInfo("America/New_York")


class DailyPatternScannerScheduler:
    _instance: Optional["DailyPatternScannerScheduler"] = None
    _lock = threading.Lock()

    def __new__(cls):
        with cls._lock:
            if cls._instance is None:
                cls._instance = super().__new__(cls)
                cls._instance._initialized = False
            return cls._instance

    def __init__(self):
        if getattr(self, "_initialized", False):
            return

        self._initialized = True
        self.is_running = False
        self.market_filter = "US"
        self.target_hour_et = 17  # 5:00 PM ET (after 4:00 PM close and settlement)
        self.target_minute_et = 0
        self.worker_thread: Optional[threading.Thread] = None

        # Load persisted state
        self.state = self._load_state()

    def _load_state(self) -> Dict[str, Any]:
        default_state = {
            "last_scanned_trading_date": None,
            "last_run_timestamp": None,
            "last_run_duration_seconds": None,
            "last_run_status": "IDLE",
            "last_summary": {},
            "total_runs_completed": 0
        }
        if STATE_FILE.exists():
            try:
                with open(STATE_FILE, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    default_state.update(data)
            except Exception as e:
                logger.warning(f"Error reading scanner scheduler state file: {e}")
        return default_state

    def _save_state(self):
        try:
            with open(STATE_FILE, "w", encoding="utf-8") as f:
                json.dump(self.state, f, indent=2)
        except Exception as e:
            logger.warning(f"Error persisting scanner scheduler state file: {e}")

    def get_current_et_time(self) -> datetime:
        return datetime.now(NY_TZ)

    def is_non_market_hours(self, dt: Optional[datetime] = None) -> bool:
        """
        Returns True if dt is during non-market hours for US equities.
        Non-market:
        - Saturday (weekday == 5) or Sunday (weekday == 6)
        - Weekdays before 09:00 ET
        - Weekdays after 16:30 ET
        """
        if dt is None:
            dt = self.get_current_et_time()

        weekday = dt.weekday()  # 0=Monday, 6=Sunday
        if weekday in (5, 6):
            return True

        current_time = dt.time()
        market_open_threshold = dtime(9, 0)
        market_close_threshold = dtime(16, 30)

        if current_time >= market_close_threshold or current_time < market_open_threshold:
            return True

        return False

    def get_target_trading_date(self, dt: Optional[datetime] = None) -> Optional[str]:
        """
        Determines the relevant trading date that should be scanned.
        - If weekday after 16:30 ET: today's date.
        - If weekday before 09:00 ET: previous trading date (e.g. yesterday, or Friday if Monday morning).
        - If weekend: Friday's date.
        """
        if dt is None:
            dt = self.get_current_et_time()

        weekday = dt.weekday()
        current_time = dt.time()

        if weekday == 5:  # Saturday -> Friday
            target_dt = dt - timedelta(days=1)
            return target_dt.strftime("%Y-%m-%d")
        elif weekday == 6:  # Sunday -> Friday
            target_dt = dt - timedelta(days=2)
            return target_dt.strftime("%Y-%m-%d")
        else:
            if current_time >= dtime(16, 30):
                return dt.strftime("%Y-%m-%d")
            elif current_time < dtime(9, 0):
                # Before open: target is previous trading day
                offset = 3 if weekday == 0 else 1
                target_dt = dt - timedelta(days=offset)
                return target_dt.strftime("%Y-%m-%d")
            else:
                # During regular trading hours
                return None

    def should_trigger_scan(self) -> bool:
        """Checks if a daily scan should execute right now."""
        if self.is_running:
            return False

        now_et = self.get_current_et_time()
        if not self.is_non_market_hours(now_et):
            return False

        target_date = self.get_target_trading_date(now_et)
        if not target_date:
            return False

        last_scanned = self.state.get("last_scanned_trading_date")
        if last_scanned == target_date:
            return False

        return True

    def execute_daily_scan(self, force: bool = False, limit_securities: Optional[int] = None) -> Dict[str, Any]:
        """Executes the daily pattern scanner across all US securities."""
        with self._lock:
            if self.is_running:
                return {"status": "already_running", "message": "Pattern scan is currently in progress."}
            self.is_running = True

        now_et = self.get_current_et_time()
        target_date = self.get_target_trading_date(now_et) or now_et.strftime("%Y-%m-%d")

        logger.info(f"[ScannerScheduler] Starting daily pattern scan for target trading date: {target_date} (Force={force})")
        start_ts = datetime.utcnow()
        self.state["last_run_timestamp"] = start_ts.isoformat() + "Z"
        self.state["last_run_status"] = "RUNNING"
        self._save_state()

        try:
            # Run parallel pipeline for all US securities without heavy forward-return backtests
            result = run_pipeline(market_filter="US", limit_securities=limit_securities, run_backtest=False, max_workers=16)

            duration = (datetime.utcnow() - start_ts).total_seconds()
            self.state["last_scanned_trading_date"] = target_date
            self.state["last_run_duration_seconds"] = round(duration, 1)
            self.state["last_run_status"] = "SUCCESS"
            self.state["last_summary"] = result
            self.state["total_runs_completed"] = self.state.get("total_runs_completed", 0) + 1
            self._save_state()

            logger.info(f"[ScannerScheduler] Daily pattern scan completed successfully in {duration:.1f}s for date {target_date}.")
            return {"status": "success", "target_date": target_date, "result": result}

        except Exception as e:
            duration = (datetime.utcnow() - start_ts).total_seconds()
            logger.error(f"[ScannerScheduler] Error during daily pattern scan: {e}")
            self.state["last_run_status"] = "FAILED"
            self.state["last_run_duration_seconds"] = round(duration, 1)
            self.state["last_summary"] = {"error": str(e)}
            self._save_state()
            return {"status": "error", "error": str(e)}

        finally:
            with self._lock:
                self.is_running = False

    def trigger_in_background(self, force: bool = False, limit_securities: Optional[int] = None):
        """Launches scan asynchronously in a worker thread."""
        t = threading.Thread(target=self.execute_daily_scan, kwargs={"force": force, "limit_securities": limit_securities}, daemon=True)
        t.start()
        return {"status": "initiated", "message": "Daily pattern scan initiated in background thread."}

    def _daemon_loop(self):
        """Periodic background check every 5 minutes."""
        logger.info("[ScannerScheduler] Daily Pattern Scanner Scheduler daemon thread started.")
        time.sleep(30)  # Initial grace period on boot

        while True:
            try:
                if self.should_trigger_scan():
                    logger.info("[ScannerScheduler] Non-market condition met. Triggering daily scan for US universe.")
                    self.execute_daily_scan()
            except Exception as e:
                logger.error(f"[ScannerScheduler] Daemon loop exception: {e}")

            time.sleep(300)  # Check every 5 minutes

    def start_scheduler(self):
        """Starts the background monitoring loop if not already running."""
        if self.worker_thread is None or not self.worker_thread.is_alive():
            self.worker_thread = threading.Thread(target=self._daemon_loop, daemon=True, name="StockticsScannerScheduler")
            self.worker_thread.start()
            logger.info("[ScannerScheduler] Background thread registered.")

    def get_status(self) -> Dict[str, Any]:
        """Returns comprehensive real-time scheduler metadata."""
        now_et = self.get_current_et_time()
        is_non_mkt = self.is_non_market_hours(now_et)
        target_date = self.get_target_trading_date(now_et)

        # Calculate next target run time
        if now_et.time() < dtime(17, 0) and now_et.weekday() < 5:
            next_run_et = now_et.replace(hour=17, minute=0, second=0, microsecond=0)
        else:
            # Next day or next Monday if Friday after 17:00 / weekend
            days_ahead = 1
            if now_et.weekday() == 4 and now_et.time() >= dtime(17, 0):  # Friday night -> Monday
                days_ahead = 3
            elif now_et.weekday() == 5:  # Saturday -> Monday
                days_ahead = 2
            elif now_et.weekday() == 6:  # Sunday -> Monday
                days_ahead = 1

            next_run_dt = now_et + timedelta(days=days_ahead)
            next_run_et = next_run_dt.replace(hour=17, minute=0, second=0, microsecond=0)

        eta_seconds = max(0, int((next_run_et - now_et).total_seconds()))
        hours, rem = divmod(eta_seconds, 3600)
        minutes, _ = divmod(rem, 60)

        return {
            "scheduler_enabled": True,
            "market": self.market_filter,
            "is_running": self.is_running,
            "current_et_time": now_et.strftime("%Y-%m-%d %H:%M:%S %Z"),
            "is_non_market_hours": is_non_mkt,
            "target_trading_date": target_date,
            "last_scanned_trading_date": self.state.get("last_scanned_trading_date"),
            "last_run_timestamp": self.state.get("last_run_timestamp"),
            "last_run_status": self.state.get("last_run_status"),
            "last_run_duration_seconds": self.state.get("last_run_duration_seconds"),
            "last_summary": self.state.get("last_summary"),
            "total_runs_completed": self.state.get("total_runs_completed", 0),
            "next_scheduled_run_et": next_run_et.strftime("%Y-%m-%d %H:%M:%S %Z"),
            "next_run_in": f"{hours}h {minutes}m"
        }


# Singleton instance
scanner_scheduler = DailyPatternScannerScheduler()
