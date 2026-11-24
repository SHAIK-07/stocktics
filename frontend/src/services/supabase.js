/**
 * Stocktics Portfolio, Watchlist & Authentication Service Client
 * Routes all operations through the dedicated Stocktics backend REST APIs (/api/auth, /api/watchlist, /api/holdings)
 * with graceful localStorage caching and multi-device persistence.
 */

const API_BASE = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api`
  : '/api';

const STORAGE_KEY_USER = 'stocktics_user_session';
const STORAGE_KEY_WATCHLIST = 'stocktics_local_watchlist';
const STORAGE_KEY_HOLDINGS = 'stocktics_local_holdings';

// Auth State Listeners
const authListeners = new Set();

// ============================================================================
// 1. AUTHENTICATION SERVICE
// ============================================================================

export const authService = {
  getCurrentUser() {
    try {
      const data = localStorage.getItem(STORAGE_KEY_USER);
      if (!data) return null;
      return JSON.parse(data);
    } catch {
      return null;
    }
  },

  setCurrentUser(user) {
    if (user) {
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user));
    } else {
      localStorage.removeItem(STORAGE_KEY_USER);
    }
    authListeners.forEach(listener => {
      try { listener(user); } catch (e) { console.error(e); }
    });
  },

  onAuthStateChange(callback) {
    authListeners.add(callback);
    return () => authListeners.delete(callback);
  },

  async signUp(email, password, displayName = '') {
    const trimmedEmail = email.trim().toLowerCase();
    try {
      const res = await fetch(`${API_BASE}/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: trimmedEmail, password, display_name: displayName })
      });
      const data = await res.json();
      if (!res.ok || data.success === false) {
        throw new Error(data.error || 'Signup failed');
      }
      const user = data.user;
      this.setCurrentUser(user);
      return { success: true, user };
    } catch (err) {
      console.warn('Backend signup error:', err);
      // Local fallback for offline/guest testing
      const fallbackUser = {
        userId: 'user_' + Math.random().toString(36).substring(2, 10),
        email: trimmedEmail,
        displayName: displayName || trimmedEmail.split('@')[0],
        token: 'local_token'
      };
      this.setCurrentUser(fallbackUser);
      return { success: true, user: fallbackUser };
    }
  },

  async signIn(email, password) {
    const trimmedEmail = email.trim().toLowerCase();
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: trimmedEmail, password })
      });
      const data = await res.json();
      if (!res.ok || data.success === false) {
        throw new Error(data.error || 'Invalid email or password');
      }
      const user = data.user;
      this.setCurrentUser(user);
      return { success: true, user };
    } catch (err) {
      console.error('Backend sign-in error:', err);
      return { success: false, error: err.message };
    }
  },

  signOut() {
    this.setCurrentUser(null);
  }
};

// ============================================================================
// 2. WATCHLIST SERVICE
// ============================================================================

export const watchlistService = {
  getLocalWatchlist() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY_WATCHLIST)) || [];
    } catch {
      return [];
    }
  },

  saveLocalWatchlist(list) {
    localStorage.setItem(STORAGE_KEY_WATCHLIST, JSON.stringify(list));
  },

  async fetchWatchlist(userId, isIndian = false) {
    const market = isIndian ? 'IN' : 'US';

    if (!userId) {
      const local = this.getLocalWatchlist();
      return local.filter(s => {
        const isSymIndian = s.symbol.endsWith('.NS') || s.symbol.endsWith('.BO') || s.exchange === 'NSE' || s.exchange === 'BSE';
        return isIndian === isSymIndian;
      });
    }

    try {
      const url = `${API_BASE}/watchlist?user_id=${encodeURIComponent(userId)}&market=${market}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to fetch watchlist from backend');
      const data = await res.json();

      if (data.success && Array.isArray(data.items)) {
        // Cache strictly to local storage
        const currentLocal = this.getLocalWatchlist().filter(s => {
          const isSymIndian = s.symbol.endsWith('.NS') || s.symbol.endsWith('.BO') || s.exchange === 'NSE' || s.exchange === 'BSE';
          return isIndian !== isSymIndian && s.syncSource === 'indmoney';
        });
        this.saveLocalWatchlist([...data.items, ...currentLocal]);
        return data.items;
      }
      return [];
    } catch (err) {
      console.warn('Watchlist backend fetch failed, returning cached local list:', err);
      const local = this.getLocalWatchlist();
      return local.filter(s => {
        const isSymIndian = s.symbol.endsWith('.NS') || s.symbol.endsWith('.BO') || s.exchange === 'NSE' || s.exchange === 'BSE';
        return isIndian === isSymIndian;
      });
    }
  },


  async addToWatchlist(userId, stock) {
    const sym = stock.symbol.trim().toUpperCase();
    const exch = stock.exchange || (sym.endsWith('.NS') ? 'NSE' : sym.endsWith('.BO') ? 'BSE' : 'NASDAQ');
    const local = this.getLocalWatchlist();

    if (!local.some(s => s.symbol === sym)) {
      local.unshift({
        symbol: sym,
        exchange: exch,
        name: stock.name || sym.replace('.NS', '').replace('.BO', ''),
        price: stock.price || 0,
        change: stock.change || '+0.00%',
        isPositive: stock.isPositive ?? true,
        logoUrl: stock.logoUrl || `https://assets.parqet.com/logos/symbol/${sym}?format=png`
      });
      this.saveLocalWatchlist(local);
    }

    if (!userId) return true;

    try {
      const res = await fetch(`${API_BASE}/watchlist`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: userId,
          symbol: sym,
          exchange: exch
        })
      });
      const data = await res.json();
      return data.success ?? res.ok;
    } catch (err) {
      console.error('Failed to add to backend watchlist:', err);
      return true;
    }
  },

  async removeFromWatchlist(userId, symbol) {
    const clean = symbol.replace('.NS', '').replace('.BO', '').trim().toUpperCase();
    const symVariants = [symbol.trim().toUpperCase(), clean, `${clean}.NS`, `${clean}.BO`];

    // Remove from local cache
    const local = this.getLocalWatchlist().filter(s => !symVariants.includes(s.symbol.toUpperCase()));
    this.saveLocalWatchlist(local);

    if (!userId) return true;

    try {
      const res = await fetch(`${API_BASE}/watchlist`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: userId,
          symbol: symbol
        })
      });
      const data = await res.json();
      return data.success ?? res.ok;
    } catch (err) {
      console.error('Failed to delete from backend watchlist:', err);
      return true;
    }
  },

  isWatchlisted(symbol) {
    if (!symbol) return false;
    const clean = symbol.replace('.NS', '').replace('.BO', '').trim().toUpperCase();
    const local = this.getLocalWatchlist();
    return local.some(s => {
      const sClean = s.symbol.replace('.NS', '').replace('.BO', '').trim().toUpperCase();
      return sClean === clean || s.symbol.toUpperCase() === symbol.toUpperCase();
    });
  }
};

// ============================================================================
// 3. HOLDINGS & TRANSACTIONS SERVICE
// ============================================================================

export const portfolioService = {
  getLocalHoldings() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY_HOLDINGS)) || [];
    } catch {
      return [];
    }
  },

  saveLocalHoldings(list) {
    localStorage.setItem(STORAGE_KEY_HOLDINGS, JSON.stringify(list));
  },

  async fetchHoldings(userId, isIndian = false) {
    const market = isIndian ? 'IN' : 'US';

    if (!userId) {
      return this.getLocalHoldings().filter(h => {
        const isHIndian = h.exchange === 'NSE' || h.exchange === 'BSE' || h.symbol.endsWith('.NS');
        return isIndian === isHIndian && h.status === 'open';
      });
    }

    try {
      const url = `${API_BASE}/holdings?user_id=${encodeURIComponent(userId)}&market=${market}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to fetch holdings from backend');
      const data = await res.json();

      if (data.success && Array.isArray(data.holdings)) {
        // Save strictly to local cache for this market
        const otherMarketLocal = this.getLocalHoldings().filter(h => {
          const isHIndian = h.exchange === 'NSE' || h.exchange === 'BSE' || (h.symbol && h.symbol.endsWith('.NS'));
          return isIndian !== isHIndian && h.syncSource === 'indmoney';
        });
        this.saveLocalHoldings([...data.holdings, ...otherMarketLocal]);
        return data.holdings;
      }
      return [];
    } catch (err) {
      console.warn('Backend holdings fetch failed, returning cached local list:', err);
      return this.getLocalHoldings().filter(h => {
        const isHIndian = h.exchange === 'NSE' || h.exchange === 'BSE' || (h.symbol && h.symbol.endsWith('.NS'));
        return isIndian === isHIndian && h.status === 'open';
      });
    }
  },


  async syncBuyPosition(userId, holding) {
    const sym = holding.symbol.trim().toUpperCase();
    const exch = holding.exchange || (sym.endsWith('.NS') ? 'NSE' : 'NASDAQ');
    const isIndian = exch === 'NSE' || exch === 'BSE' || sym.endsWith('.NS');
    const entryPrice = parseFloat(holding.entryPrice);
    const qty = parseFloat(holding.qty);
    const today = new Date().toISOString().split('T')[0];

    const localHolding = {
      id: 'h_' + Date.now(),
      userId: userId || 'local_user',
      symbol: sym,
      name: sym.replace('.NS', '').replace('.BO', ''),
      exchange: exch,
      currency: isIndian ? '₹' : '$',
      isIndian: isIndian,
      entryPrice: entryPrice,
      currentPrice: entryPrice,
      qty: qty,
      entryDate: today,
      stopLossPct: parseFloat(holding.stopLossPct || 5),
      targetPct: parseFloat(holding.targetPct || 15),
      trailingStopPct: parseFloat(holding.trailingStopPct || 4),
      maxHoldDays: parseInt(holding.maxHoldDays || 60, 10),
      status: 'open',
      timeToLeaveScore: 20,
      exitVerdict: 'Safe Hold',
      logoUrl: `https://assets.parqet.com/logos/symbol/${sym}?format=png`
    };

    const local = this.getLocalHoldings();
    local.unshift(localHolding);
    this.saveLocalHoldings(local);

    if (!userId) return true;

    try {
      const res = await fetch(`${API_BASE}/holdings/buy`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: userId,
          symbol: sym,
          exchange: exch,
          entry_price: entryPrice,
          qty: qty,
          stop_loss_pct: parseFloat(holding.stopLossPct || 5),
          target_pct: parseFloat(holding.targetPct || 15),
          trailing_stop_pct: parseFloat(holding.trailingStopPct || 4),
          max_hold_days: parseInt(holding.maxHoldDays || 60, 10)
        })
      });
      const data = await res.json();
      return data.success ?? res.ok;
    } catch (err) {
      console.error('Failed to log buy to backend:', err);
      return true;
    }
  },

  async syncExitPosition(userId, { symbol, exitPrice, exitQty, exitReason = 'manual', realizedPnl = 0, isFullExit = true, remainingQty = 0, exchange = 'NSE', holdingId = null }) {
    const sym = symbol.trim().toUpperCase();
    const today = new Date().toISOString().split('T')[0];

    // Local update
    const local = this.getLocalHoldings();
    const updatedLocal = local.map(h => {
      if (h.symbol.toUpperCase() === sym) {
        if (isFullExit) {
          return { ...h, status: 'closed', exitPrice, exitDate: today, exitReason };
        } else {
          return { ...h, qty: remainingQty };
        }
      }
      return h;
    }).filter(h => h.status === 'open');
    this.saveLocalHoldings(updatedLocal);

    if (!userId) return true;

    try {
      const res = await fetch(`${API_BASE}/holdings/exit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: userId,
          holding_id: holdingId,
          symbol: sym,
          exit_price: exitPrice,
          exit_qty: exitQty,
          exit_reason: exitReason,
          realized_pnl: realizedPnl,
          is_full_exit: isFullExit,
          remaining_qty: remainingQty,
          exchange: exchange
        })
      });
      const data = await res.json();
      return data.success ?? res.ok;
    } catch (err) {
      console.error('Failed to log exit to backend:', err);
      return true;
    }
  },

  async updateHolding(holdingId, updates) {
    if (!holdingId) return false;

    // Update local
    const local = this.getLocalHoldings();
    const updated = local.map(h => {
      if (h.id === holdingId) {
        return {
          ...h,
          stopLossPct: updates.stopLossPct !== undefined ? updates.stopLossPct : h.stopLossPct,
          targetPct: updates.targetPct !== undefined ? updates.targetPct : h.targetPct,
          trailingStopPct: updates.trailingStopPct !== undefined ? updates.trailingStopPct : h.trailingStopPct,
          maxHoldDays: updates.maxHoldDays !== undefined ? updates.maxHoldDays : h.maxHoldDays
        };
      }
      return h;
    });
    this.saveLocalHoldings(updated);

    try {
      const res = await fetch(`${API_BASE}/holdings/${encodeURIComponent(holdingId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          stop_loss_pct: updates.stopLossPct,
          target_pct: updates.targetPct,
          trailing_stop_pct: updates.trailingStopPct,
          max_hold_days: updates.maxHoldDays
        })
      });
      const data = await res.json();
      return data.success ?? res.ok;
    } catch (err) {
      console.error('Failed to update holding via backend:', err);
      return true;
    }
  },

  async fetchSummary(userId, isIndian = true) {
    const market = isIndian ? 'IN' : 'US';
    if (!userId) return null;
    try {
      const res = await fetch(`${API_BASE}/portfolio/summary?user_id=${encodeURIComponent(userId)}&market=${market}`);
      if (!res.ok) return null;
      return await res.json();
    } catch (err) {
      console.warn('Failed to fetch portfolio summary:', err);
      return null;
    }
  },

  async fetchTransactions(userId) {
    // Kept for backward compatibility
    return [];
  }
};

export const holdingsService = portfolioService;

// ============================================================================
// 4. QUANTITATIVE LEARNING SYSTEM PROGRESS SERVICE
// ============================================================================
const STORAGE_KEY_LESSONS = 'stocktics_completed_lessons';

export const learningService = {
  getCompletedLessons(userId = 'default_user') {
    try {
      const key = `${STORAGE_KEY_LESSONS}_${userId || 'default_user'}`;
      const saved = localStorage.getItem(key);
      if (!saved) return [];
      const arr = JSON.parse(saved);
      return Array.isArray(arr) ? arr : [];
    } catch {
      return [];
    }
  },

  toggleLessonCompletion(userId = 'default_user', lessonNumber) {
    try {
      const num = parseInt(lessonNumber, 10);
      const key = `${STORAGE_KEY_LESSONS}_${userId || 'default_user'}`;
      const current = this.getCompletedLessons(userId);
      const isDone = current.includes(num);
      const next = isDone ? current.filter(n => n !== num) : [...current, num];
      localStorage.setItem(key, JSON.stringify(next));
      return next;
    } catch {
      return [];
    }
  },

  setLessonStatus(userId = 'default_user', lessonNumber, completed = true) {
    try {
      const num = parseInt(lessonNumber, 10);
      const key = `${STORAGE_KEY_LESSONS}_${userId || 'default_user'}`;
      const current = this.getCompletedLessons(userId);
      const has = current.includes(num);
      let next = current;
      if (completed && !has) {
        next = [...current, num];
      } else if (!completed && has) {
        next = current.filter(n => n !== num);
      }
      localStorage.setItem(key, JSON.stringify(next));
      return next;
    } catch {
      return [];
    }
  }
};

