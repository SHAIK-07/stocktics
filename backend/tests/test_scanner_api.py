import sys
from pathlib import Path
root_dir = Path(__file__).resolve().parent.parent.parent
if str(root_dir) not in sys.path:
    sys.path.insert(0, str(root_dir))

from fastapi.testclient import TestClient
from backend.api import app

client = TestClient(app)


def test_scanner_categories():
    response = client.get("/scanner/categories")
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"
    data = response.json()
    assert "categories" in data
    assert data["total_patterns"] >= 33
    print(f"PASS: /scanner/categories returned {data['total_patterns']} pattern rows.")

    # Check structure of first category
    first = data["categories"][0]
    assert "pattern" in first
    assert "display_name" in first
    assert "confidence_tier" in first
    assert "top5" in first
    print(f"Sample category: {first['display_name']} (Tier: {first['confidence_tier']}), Matches: {len(first['top5'])}")


def test_scanner_top_week():
    response = client.get("/scanner/top-week?limit=10")
    assert response.status_code == 200
    data = response.json()
    assert "top_stocks" in data
    print(f"PASS: /scanner/top-week returned {len(data['top_stocks'])} ranked stocks.")
    if data["top_stocks"]:
        top1 = data["top_stocks"][0]
        print(f"Top 1 Stock: {top1['ticker']} (Composite: {top1['avg_composite_score']})")


def test_scanner_pattern_matches():
    response = client.get("/scanner/pattern/SUPPORT_RESISTANCE_TOUCH?page=1&page_size=10")
    assert response.status_code == 200
    data = response.json()
    assert "results" in data
    assert "total" in data
    print(f"PASS: /scanner/pattern/SUPPORT_RESISTANCE_TOUCH returned {len(data['results'])} matches (Total: {data['total']}).")


def test_scanner_stock_patterns():
    response = client.get("/scanner/stock/AAPL/patterns")
    assert response.status_code == 200
    data = response.json()
    assert "patterns" in data
    print(f"PASS: /scanner/stock/AAPL/patterns returned {data['total_matches']} active setups.")


def test_scanner_backtest():
    response = client.get("/scanner/backtest/VCP")
    assert response.status_code == 200
    data = response.json()
    assert "pattern" in data
    print(f"PASS: /scanner/backtest/VCP returned backtest with sample size {data.get('sample_size')} and 5D win rate {data.get('win_rate_5d')}%.")


def test_scanner_universe():
    response = client.get("/scanner/universe")
    assert response.status_code == 200
    data = response.json()
    assert "securities" in data
    print(f"PASS: /scanner/universe returned {data['total']} tracked universe securities.")


def test_core_endpoints():
    r_health = client.get("/health")
    assert r_health.status_code == 200
    assert r_health.json()["status"] == "healthy"

    r_api_health = client.get("/api/health")
    assert r_api_health.status_code == 200

    r_search = client.get("/api/search?q=RELIANCE")
    assert r_search.status_code == 200
    assert len(r_search.json().get("results", [])) > 0
    print(f"PASS: /api/search returned {len(r_search.json()['results'])} results for RELIANCE.")

    r_quotes = client.get("/api/quotes?symbols=AAPL")
    assert r_quotes.status_code == 200
    assert "AAPL" in r_quotes.json().get("quotes", {})
    print("PASS: /api/quotes successfully retrieved live quote.")


if __name__ == "__main__":
    print("\n--- RUNNING STOCKTICS API & SCANNER AUTOMATED TESTS ---")
    test_core_endpoints()
    test_scanner_categories()
    test_scanner_top_week()
    test_scanner_pattern_matches()
    test_scanner_stock_patterns()
    test_scanner_backtest()
    test_scanner_universe()
    print("\n--- ALL TESTS PASSED SUCCESSFULLY! ---")

