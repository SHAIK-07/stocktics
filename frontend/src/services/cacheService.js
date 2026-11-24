/**
 * Stocktics Web Cache Service
 * 
 * Provides instant Stale-While-Revalidate (SWR) offline-first local persistence.
 * Mirrors the native Android Room/DataStore architecture:
 * 1. Read cached state synchronously at 0ms (no empty screen or zero flash).
 * 2. Silent background network fetch revalidates data.
 * 3. Cache updates smoothly in place.
 */

const CACHE_PREFIX = 'stocktics_swr_';
const DEFAULT_TTL_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

// In-memory fallback if localStorage is disabled / quota exceeded
const memoryStore = new Map();

class CacheService {
  /**
   * Builds namespaced key
   */
  _buildKey(key) {
    return `${CACHE_PREFIX}${key}`;
  }

  /**
   * Retrieves cached data synchronously.
   * Returns defaultValue if missing or corrupted.
   */
  get(key, defaultValue = null) {
    const fullKey = this._buildKey(key);
    try {
      const raw = localStorage.getItem(fullKey);
      if (!raw) {
        return memoryStore.get(fullKey) || defaultValue;
      }
      const record = JSON.parse(raw);
      if (!record || typeof record !== 'object') {
        return defaultValue;
      }

      // Check TTL if configured
      if (record.ttl && Date.now() - record.timestamp > record.ttl) {
        this.remove(key);
        return defaultValue;
      }

      return record.data !== undefined ? record.data : defaultValue;
    } catch (e) {
      console.warn(`[CacheService] Error reading cache key "${key}":`, e);
      return memoryStore.get(fullKey) || defaultValue;
    }
  }

  /**
   * Retrieves data along with its metadata (timestamp, etc.)
   */
  getWithMeta(key) {
    const fullKey = this._buildKey(key);
    try {
      const raw = localStorage.getItem(fullKey);
      if (!raw) return null;
      const record = JSON.parse(raw);
      return record;
    } catch {
      return null;
    }
  }

  /**
   * Stores data in cache with timestamp.
   */
  set(key, data, ttlMs = DEFAULT_TTL_MS) {
    if (data === undefined || data === null) return;
    const fullKey = this._buildKey(key);
    const record = {
      data,
      timestamp: Date.now(),
      ttl: ttlMs
    };

    try {
      localStorage.setItem(fullKey, JSON.stringify(record));
    } catch (e) {
      console.warn(`[CacheService] LocalStorage set failed for "${key}", falling back to memory:`, e);
      memoryStore.set(fullKey, data);
    }
  }

  /**
   * Removes specific cache entry.
   */
  remove(key) {
    const fullKey = this._buildKey(key);
    try {
      localStorage.removeItem(fullKey);
      memoryStore.delete(fullKey);
    } catch {}
  }

  /**
   * Formats relative time elapsed since the cache was last refreshed
   */
  formatTimeAgo(timestamp) {
    if (!timestamp) return '';
    const diffMs = Date.now() - timestamp;
    const diffSec = Math.floor(diffMs / 1000);
    if (diffSec < 60) return 'Just now';
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr}h ago`;
    const diffDays = Math.floor(diffHr / 24);
    return `${diffDays}d ago`;
  }

  /**
   * Standard key generator helpers to guarantee consistency across components
   */
  keys = {
    dashboardHoldings: (userId, market) => `holdings_${userId || 'anon'}_${market || 'IN'}`,
    dashboardWatchlist: (userId, market) => `watchlist_${userId || 'anon'}_${market || 'IN'}`,
    dashboardTransactions: (userId) => `transactions_${userId || 'anon'}`,
    dashboardMovers: (market) => `movers_${market || 'IN'}`,
    buyCandidates: (userId, market) => `buy_candidates_${userId || 'anon'}_${market || 'IN'}`,
    exitHoldings: (userId, market) => `exit_holdings_${userId || 'anon'}_${market || 'IN'}`
  };
}

export const cacheService = new CacheService();
export default cacheService;
