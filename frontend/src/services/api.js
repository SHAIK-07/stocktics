/**
 * Stocktics Real-Data API Client
 * Connects to the local FastAPI backend to retrieve 100% real financial metrics & search results.
 */

const API_BASE = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api`
  : '/api';

export async function searchStocks(query) {
  if (!query || query.trim().length === 0) return [];
  try {
    const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query.trim())}`);
    if (!res.ok) throw new Error('Search failed');
    const data = await res.json();
    return data.results || [];
  } catch (err) {
    console.error('Error searching stocks:', err);
    return [];
  }
}

export async function fetchStockAnalysis(ticker) {
  if (!ticker) throw new Error('Ticker is required');
  const cleanTicker = ticker.trim().toUpperCase();
  const res = await fetch(`${API_BASE}/stock/${encodeURIComponent(cleanTicker)}`);
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.detail || `Failed to fetch data for ${cleanTicker}`);
  }
  return await res.json();
}

export async function fetchStockHistory(ticker, period = '1y') {
  if (!ticker) return null;
  const cleanTicker = ticker.trim().toUpperCase();
  const res = await fetch(`${API_BASE}/stock/${encodeURIComponent(cleanTicker)}/history?period=${period}`);
  if (!res.ok) throw new Error('Failed to fetch stock history');
  return await res.json();
}

/**
 * Dual-Market Pattern Scanner Endpoints
 */
export async function fetchScannerCategories(market = null) {
  try {
    const url = market && market !== 'ALL'
      ? `${API_BASE}/scanner/categories?market=${encodeURIComponent(market)}`
      : `${API_BASE}/scanner/categories`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch scanner categories');
    return await res.json();
  } catch (err) {
    console.error('Error in fetchScannerCategories:', err);
    return { categories: [], total_patterns: 0 };
  }
}

export async function fetchPatternMatches(pattern, market = null, page = 1, pageSize = 20) {
  try {
    let url = `${API_BASE}/scanner/pattern/${encodeURIComponent(pattern)}?page=${page}&page_size=${pageSize}`;
    if (market && market !== 'ALL') {
      url += `&market=${encodeURIComponent(market)}`;
    }
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Failed to fetch matches for ${pattern}`);
    return await res.json();
  } catch (err) {
    console.error('Error in fetchPatternMatches:', err);
    return { results: [], total: 0, page: 1, total_pages: 1 };
  }
}

export async function fetchTopWeek(market = null, limit = 10) {
  try {
    const url = market && market !== 'ALL'
      ? `${API_BASE}/scanner/top-week?market=${encodeURIComponent(market)}&limit=${limit}`
      : `${API_BASE}/scanner/top-week?limit=${limit}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch top week rankings');
    return await res.json();
  } catch (err) {
    console.error('Error in fetchTopWeek:', err);
    return { top_stocks: [] };
  }
}

export async function fetchStockPatterns(ticker) {
  if (!ticker) return { patterns: [] };
  try {
    const res = await fetch(`${API_BASE}/scanner/stock/${encodeURIComponent(ticker.trim().toUpperCase())}/patterns`);
    if (!res.ok) throw new Error(`Failed to fetch patterns for ${ticker}`);
    return await res.json();
  } catch (err) {
    console.error('Error in fetchStockPatterns:', err);
    return { patterns: [] };
  }
}

export async function fetchPatternBacktest(pattern, market = null) {
  try {
    const url = market && market !== 'ALL'
      ? `${API_BASE}/scanner/backtest/${encodeURIComponent(pattern)}?market=${encodeURIComponent(market)}`
      : `${API_BASE}/scanner/backtest/${encodeURIComponent(pattern)}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Failed to fetch backtest for ${pattern}`);
    return await res.json();
  } catch (err) {
    console.error('Error in fetchPatternBacktest:', err);
    return null;
  }
}

export async function fetchScannerUniverse(market = null) {
  try {
    const url = market && market !== 'ALL'
      ? `${API_BASE}/scanner/universe?market=${encodeURIComponent(market)}`
      : `${API_BASE}/scanner/universe`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch universe');
    return await res.json();
  } catch (err) {
    console.error('Error in fetchScannerUniverse:', err);
    return { securities: [] };
  }
}

export async function fetchBatchQuotes(symbols = []) {
  if (!symbols || symbols.length === 0) return {};
  const symStr = Array.isArray(symbols) ? symbols.join(',') : symbols;
  try {
    const res = await fetch(`${API_BASE}/quotes?symbols=${encodeURIComponent(symStr)}`);
    if (!res.ok) throw new Error('Failed to fetch batch quotes');
    const data = await res.json();
    return data.quotes || {};
  } catch (err) {
    console.error('Error fetching batch quotes:', err);
    return {};
  }
}

export async function fetchMarketHeatmap(market = 'US', index = null) {
  try {
    const params = new URLSearchParams({ market });
    if (index) params.set('index', index);
    const res = await fetch(`${API_BASE}/heatmap?${params.toString()}`);
    if (!res.ok) throw new Error('Failed to fetch market heatmap');
    return await res.json();
  } catch (err) {
    console.error('Error fetching market heatmap:', err);
    throw err;
  }
}

export async function fetchStockComparison(symbols = [], period = '6mo') {
  if (!symbols || symbols.length === 0) return null;
  const symStr = Array.isArray(symbols) ? symbols.join(',') : symbols;
  try {
    const params = new URLSearchParams({ symbols: symStr, period });
    const res = await fetch(`${API_BASE}/compare?${params.toString()}`);
    if (!res.ok) throw new Error('Failed to fetch stock comparison');
    return await res.json();
  } catch (err) {
    console.error('Error fetching stock comparison:', err);
    throw err;
  }
}

/**
 * INDmoney MCP Integration Endpoints
 */
export async function getIndmoneyAuthUrl(userId, redirectUri = null) {
  if (!userId) throw new Error('userId is required');
  const params = new URLSearchParams({ user_id: userId });
  if (redirectUri) params.set('redirect_uri', redirectUri);
  const res = await fetch(`${API_BASE}/indmoney/auth-url?${params.toString()}`);
  if (!res.ok) throw new Error('Failed to generate INDmoney authorization URL');
  return await res.json();
}

export async function syncIndmoneyPortfolio(userId) {
  if (!userId) throw new Error('userId is required');
  const res = await fetch(`${API_BASE}/indmoney/sync`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ user_id: userId })
  });
  if (!res.ok) throw new Error('Failed to sync INDmoney portfolio');
  return await res.json();
}

export async function getIndmoneyStatus(userId) {
  if (!userId) return { connected: false };
  try {
    const res = await fetch(`${API_BASE}/indmoney/status?user_id=${encodeURIComponent(userId)}`);
    if (!res.ok) return { connected: false };
    return await res.json();
  } catch (err) {
    console.error('Error fetching INDmoney status:', err);
    return { connected: false };
  }
}

export async function disconnectIndmoney(userId) {
  if (!userId) throw new Error('userId is required');
  const res = await fetch(`${API_BASE}/indmoney/disconnect`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ user_id: userId })
  });
  if (!res.ok) throw new Error('Failed to disconnect INDmoney');
  return await res.json();
}

export async function getIndmoneySnapshot(userId) {
  if (!userId) return null;
  try {
    const res = await fetch(`${API_BASE}/indmoney/snapshot?user_id=${encodeURIComponent(userId)}`);
    if (!res.ok) return null;
    return await res.json();
  } catch (err) {
    console.error('Error fetching INDmoney snapshot:', err);
    return null;
  }
}

/**
 * Fetch INDmoney MCP News & Sentiment Analysis
 */
export async function fetchStockNews(symbol, userId = null) {
  if (!symbol) return null;
  try {
    const params = new URLSearchParams({ symbol });
    if (userId) params.set('user_id', userId);
    const res = await fetch(`${API_BASE}/indmoney/news?${params.toString()}`);
    if (!res.ok) {
      // Fallback endpoint if needed
      const fallbackRes = await fetch(`${API_BASE}/stock/${encodeURIComponent(symbol)}/news`);
      if (fallbackRes.ok) return await fallbackRes.json();
      return null;
    }
    return await res.json();
  } catch (err) {
    console.error('Error fetching stock news:', err);
    return null;
  }
}




