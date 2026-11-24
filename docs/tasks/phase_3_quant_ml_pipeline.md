# 🤖 Phase 3: Quantitative ML Models & Backtesting Pipeline

> **Goal**: Build an institutional-grade quantitative forecasting pipeline featuring XGBoost tabular classification, PyTorch LSTM sequence modeling, Amazon Chronos zero-shot forecasting, and a strict walk-forward backtester that simulates trade exits with zero lookahead bias.

---

## 📋 Task Checklist

- [ ] **Task 3.1: Feature Engineering & 3-Class Label Generator**
  - [ ] Implement [`backend/app/ml/features.py`](file:///g:/Projects/stocktics/backend/app/ml/features.py).
  - [ ] Generate stationary normalized feature matrix from OHLCV and indicators (RSI, MACD, EMAs, ATR, ADX, Bollinger Band width, Volume ratios).
  - [ ] Build 10-day forward return 3-class target labels:
    - `BUY (Class 2)`: Forward Return $> +3.0\%$
    - `HOLD (Class 1)`: Forward Return between $-3.0\%$ and $+3.0\%$
    - `SELL (Class 0)`: Forward Return $< -3.0\%$
  - [ ] Implement strict time-series sanitization: no shuffling, no future window leakage.

- [ ] **Task 3.2: Tabular Classifier (XGBoost)**
  - [ ] Implement [`backend/app/ml/train_classical.py`](file:///g:/Projects/stocktics/backend/app/ml/train_classical.py).
  - [ ] Train multi-class XGBoost classifier on engineered technical & fundamental features.
  - [ ] Compute feature importances (SHAP values & Gain metric).
  - [ ] Output class probabilities: $P(\text{BUY}), P(\text{HOLD}), P(\text{SELL})$.

- [ ] **Task 3.3: Sequence Deep Learning Model (PyTorch LSTM)**
  - [ ] Implement [`backend/app/ml/train_dl.py`](file:///g:/Projects/stocktics/backend/app/ml/train_dl.py).
  - [ ] Construct PyTorch Dataset creating 20-day rolling tensor windows $(N, 20, F)$.
  - [ ] Build Multi-layer LSTM / GRU network with Dropout and Dense classification head.
  - [ ] Implement learning rate scheduling, EarlyStopping on validation loss, and gradient clipping.
  - [ ] Output sequence model prediction probabilities and confidence scores.

- [ ] **Task 3.4: Pretrained Zero-Shot Forecasting (Amazon Chronos)**
  - [ ] Implement [`backend/app/ml/pretrained/chronos_wrapper.py`](file:///g:/Projects/stocktics/backend/app/ml/pretrained/chronos_wrapper.py).
  - [ ] Integrate Amazon Chronos (T5-based time-series foundation model) for zero-shot probabilistic forecasting.
  - [ ] Generate 10-day projected price distributions ($10^{\text{th}}, 50^{\text{th}}, 90^{\text{th}}$ percentiles).
  - [ ] Provide lightweight statistical fallback (AutoARIMA / Exponential Smoothing) if GPU/Chronos dependencies are unavailable.

- [ ] **Task 3.5: Walk-Forward Backtester & Trade Simulation Engine**
  - [ ] Implement [`backend/app/ml/backtest.py`](file:///g:/Projects/stocktics/backend/app/ml/backtest.py).
  - [ ] Execute expanding/rolling window walk-forward validation across 3–5 years of historical bars.
  - [ ] Simulate real trade mechanics:
    - Entry upon `BUY` signal with slippage and transaction costs.
    - Stop-Loss exit trigger ($\text{Price} \le \text{Entry} \times (1 - \text{SL}_\%)$).
    - Profit Target exit trigger ($\text{Price} \ge \text{Entry} \times (1 + \text{TP}_\%)$).
    - Trailing Stop exit trigger ($\text{Price} \le \text{Peak} \times (1 - \text{TS}_\%)$).
    - Maximum holding period timeout ($\Delta t > \text{MaxDays}$).
    - Reversal signal exit (Model flips to `SELL`).
  - [ ] Compute performance metrics: Win Rate %, Profit Factor, Max Drawdown (MDD), Sharpe Ratio, Total Trades, Cumulative P&L curve.

- [ ] **Task 3.6: Stacking Ensemble & Model Registry**
  - [ ] Implement [`backend/app/ml/ensemble.py`](file:///g:/Projects/stocktics/backend/app/ml/ensemble.py) and [`backend/app/ml/registry.py`](file:///g:/Projects/stocktics/backend/app/ml/registry.py).
  - [ ] Combine XGBoost, LSTM, and Chronos predictions using backtest-win-rate-weighted voting.
  - [ ] Serialize trained model weights and metadata into `public.model_registry` and Supabase Storage bucket.
  - [ ] Implement automated inference endpoint: `GET /api/v1/stocks/{symbol}/forecast`.

---

## 🎯 Acceptance Criteria
- [ ] No lookahead bias: features at bar $t$ only utilize data up to bar $t$.
- [ ] Walk-forward backtester outputs comprehensive trade logs with realistic exits.
- [ ] Ensemble model generates Buy/Hold/Sell signals with consensus confidence percentage.
- [ ] Inference execution for all 3 models completes in $<1.5$ seconds for a single symbol.
