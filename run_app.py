"""
Stocktics Quick Launcher
Launches both the FastAPI backend (port 8000) and the Vite React frontend (port 5173).
Automatically cleans up ports if already occupied.
"""

import subprocess
import sys
import time
import os
import socket

def free_port(port):
    """Frees up a port if a previous lingering process is holding it."""
    try:
        cmd = f'powershell -Command "Get-NetTCPConnection -LocalPort {port} -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess"'
        out = subprocess.check_output(cmd, shell=True).decode().strip()
        for line in out.splitlines():
            pid = line.strip()
            if pid and pid != "0":
                subprocess.run(f"taskkill /F /PID {pid}", shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except Exception:
        pass

def main():
    root_dir = os.path.dirname(os.path.abspath(__file__))
    frontend_dir = os.path.join(root_dir, "frontend")
    
    print("=================================================================")
    print("🚀 Starting Stocktics Real-Data Market Analysis Application...")
    print("=================================================================")
    
    # Pre-flight: ensure ports 8000 and 5173 are clear
    free_port(8000)
    free_port(5173)
    time.sleep(1)
    
    # 1. Start FastAPI backend
    print("• Launching FastAPI Backend on http://127.0.0.1:8000 ...")
    backend_proc = subprocess.Popen(
        [sys.executable, "-m", "uvicorn", "backend.api:app", "--host", "127.0.0.1", "--port", "8000", "--reload"],
        cwd=root_dir
    )
    
    time.sleep(2)
    
    # 2. Start Vite frontend
    print("• Launching React Vite Frontend on http://127.0.0.1:5173 ...")
    frontend_proc = subprocess.Popen(
        ["npx", "vite", "--host", "127.0.0.1", "--port", "5173"],
        cwd=frontend_dir,
        shell=True
    )
    
    print("\n✅ Stocktics is live!")
    print("👉 Open your browser at: http://127.0.0.1:5173")
    print("Press Ctrl+C to stop both servers.")
    
    try:
        backend_proc.wait()
        frontend_proc.wait()
    except KeyboardInterrupt:
        print("\nStopping Stocktics servers...")
        backend_proc.terminate()
        frontend_proc.terminate()

if __name__ == "__main__":
    main()
