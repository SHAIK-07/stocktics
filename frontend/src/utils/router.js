/**
 * Stocktics Lightweight HTML5 History Router
 * Synchronizes browser address bar, back/forward history, and refreshes for:
 * - Recommendations:      /recommendations?market=IN | /recommendations?market=US | /recommendations?market=ALL
 * - Dedicated Pattern:    /pattern/:pattern_name (e.g. /pattern/rsi_divergence, /pattern/turtle_breakout)
 * - Deep Stock Analysis:  /stock/:ticker (e.g. /stock/ITC.NS, /stock/AAPL)
 */

export function parseCurrentUrl() {
  const path = window.location.pathname;
  const searchParams = new URLSearchParams(window.location.search);

  // 1. /pattern/:pattern_name (Dedicated Pattern Page)
  const patternMatch = path.match(/^\/pattern\/([^/?#]+)/i);
  if (patternMatch) {
    const rawPattern = decodeURIComponent(patternMatch[1]).toLowerCase();
    const marketParam = searchParams.get('market')?.toUpperCase();
    const validMarket = ['IN', 'US', 'ALL'].includes(marketParam) ? marketParam : 'ALL';
    return {
      tab: 'pattern',
      pattern: rawPattern,
      market: validMarket,
      ticker: 'AAPL'
    };
  }



  // 1B. /patterns (Patterns Catalog Library Page)
  if (path.match(/^\/patterns\/?$/i)) {
    return {
      tab: 'patterns',
      pattern: null,
      market: 'ALL',
      ticker: 'AAPL'
    };
  }

  // 1C. /chart/:ticker (TradingView-Grade 70/30 Maximized Workstation)
  const chartMatch = path.match(/^\/chart\/([^/?#]+)/i);
  if (chartMatch) {
    const rawTicker = decodeURIComponent(chartMatch[1]).toUpperCase();
    const patternParam = searchParams.get('pattern');
    const marketParam = searchParams.get('market')?.toUpperCase();
    const validMarket = ['IN', 'US', 'ALL'].includes(marketParam)
      ? marketParam
      : (rawTicker.endsWith('.NS') || rawTicker.endsWith('.BO') ? 'IN' : 'US');
    return {
      tab: 'chart',
      ticker: rawTicker,
      market: validMarket,
      pattern: patternParam || null
    };
  }

  // 1D. /dashboard (Institutional Fintech Dashboard)
  if (path.match(/^\/(dashboard)?\/?$/i) && !searchParams.has('ticker') && !path.includes('recommendations') && !path.includes('pattern') && !path.includes('buy') && !path.includes('sell') && !path.includes('holdings')) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    return {
      tab: 'dashboard',
      market: ['IN', 'US'].includes(marketParam) ? marketParam : 'US',
      ticker: 'AAPL'
    };
  }

  // 1E. /buy or /pipeline (Trading Pipeline)
  if (path.match(/^\/(buy|pipeline)\/?$/i)) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    return {
      tab: 'buy',
      market: ['IN', 'US'].includes(marketParam) ? marketParam : 'US',
      ticker: 'AAPL'
    };
  }

  // 1F. /sell or /holdings (Holdings & Exit Risk)
  if (path.match(/^\/(sell|holdings)\/?$/i)) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    return {
      tab: 'sell',
      market: ['IN', 'US'].includes(marketParam) ? marketParam : 'US',
      ticker: 'AAPL'
    };
  }

  // 1G. /heatmap (Market Heatmap Treemap)
  if (path.match(/^\/heatmap\/?$/i)) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    return {
      tab: 'heatmap',
      market: ['IN', 'US'].includes(marketParam) ? marketParam : 'US',
      ticker: 'AAPL'
    };
  }

  // 1H. /compare (Stock Comparison Tool)
  if (path.match(/^\/compare\/?$/i)) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    return {
      tab: 'compare',
      market: ['IN', 'US'].includes(marketParam) ? marketParam : 'US',
      ticker: 'AAPL'
    };
  }

  // 1I. /learning or /academy (Quantitative Curriculum & Academy)
  const learningMatch = path.match(/^\/(learning|academy)(?:\/(\d+))?\/?$/i);
  if (learningMatch) {
    const lessonNum = learningMatch[2] ? parseInt(learningMatch[2], 10) : (searchParams.get('lesson') ? parseInt(searchParams.get('lesson'), 10) : null);
    return {
      tab: 'learning',
      lesson: lessonNum,
      ticker: 'AAPL',
      market: 'US'
    };
  }


  // 2. /stock/:ticker or /analysis/:ticker (Deep Analysis)
  const stockMatch = path.match(/^\/(stock|analysis)\/([^/?#]+)/i);
  if (stockMatch) {
    const rawTicker = decodeURIComponent(stockMatch[2]).toUpperCase();
    return {
      tab: 'analysis',
      ticker: rawTicker,
      market: rawTicker.endsWith('.NS') || rawTicker.endsWith('.BO') ? 'IN' : 'US',
      pattern: null
    };
  }

  // 3. Query param ?ticker=:ticker
  if (searchParams.has('ticker')) {
    const rawTicker = searchParams.get('ticker').toUpperCase();
    return {
      tab: 'analysis',
      ticker: rawTicker,
      market: rawTicker.endsWith('.NS') || rawTicker.endsWith('.BO') ? 'IN' : 'US',
      pattern: null
    };
  }

  // 4. Recommendations / Scanner
  if (path.includes('recommendations') || path.includes('scanner')) {
    const marketParam = searchParams.get('market')?.toUpperCase();
    const validMarket = ['IN', 'US', 'ALL'].includes(marketParam) ? marketParam : 'US';
    const patternParam = searchParams.get('pattern');

    return {
      tab: 'scanner',
      ticker: 'AAPL',
      market: validMarket,
      pattern: patternParam || null
    };
  }

  return {
    tab: 'dashboard',
    ticker: 'AAPL',
    market: 'US',
    pattern: null
  };
}

export function buildUrl({ tab, ticker, market, pattern }) {
  if (tab === 'dashboard') {
    return '/dashboard';
  }

  if (tab === 'buy') {
    return '/buy';
  }

  if (tab === 'sell') {
    return '/sell';
  }
  if (tab === 'patterns') {
    return '/patterns';
  }
  if (tab === 'heatmap') {
    return '/heatmap';
  }
  if (tab === 'compare') {
    return '/compare';
  }
  if (tab === 'learning') {
    return routeState?.lesson ? `/learning/${routeState.lesson}` : '/learning';
  }


  if (tab === 'pattern') {
    const params = new URLSearchParams();
    if (market && market !== 'ALL') {
      params.set('market', market);
    }
    const qs = params.toString();
    return `/pattern/${encodeURIComponent(pattern || 'breakout')}${qs ? `?${qs}` : ''}`;
  }

  if (tab === 'analysis') {
    return `/stock/${encodeURIComponent(ticker || 'AAPL')}`;
  }

  if (tab === 'chart') {
    const params = new URLSearchParams();
    if (pattern) params.set('pattern', pattern);
    if (market && market !== 'ALL') params.set('market', market);
    const qs = params.toString();
    return `/chart/${encodeURIComponent(ticker || 'AAPL')}${qs ? `?${qs}` : ''}`;
  }

  const params = new URLSearchParams();
  params.set('market', market || 'IN');

  if (pattern) {
    params.set('pattern', pattern);
  }

  return `/recommendations?${params.toString()}`;
}

export function navigateTo(routeState, { replace = false } = {}) {
  const url = buildUrl(routeState);
  const currentPath = window.location.pathname + window.location.search;

  if (currentPath === url) return;

  if (replace) {
    window.history.replaceState(routeState, '', url);
  } else {
    window.history.pushState(routeState, '', url);
  }
}
