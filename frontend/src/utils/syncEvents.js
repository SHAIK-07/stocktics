/**
 * Global Real-Time Event Dispatcher for INDmoney Synchronization
 * Ensures that when a user connects or refreshes their INDmoney portfolio,
 * every page (Dashboard, Holdings/Sell, Buy Pipeline, Watchlist, Header)
 * automatically synchronizes and updates without manual reloads.
 */

const SYNC_EVENT_NAME = 'stocktics:indmoney-synced';

export function notifyIndmoneySynced(detail = {}) {
  try {
    const event = new CustomEvent(SYNC_EVENT_NAME, { detail });
    window.dispatchEvent(event);
  } catch (err) {
    console.error('Error dispatching sync event:', err);
  }
}

export function subscribeToIndmoneySync(callback) {
  const handler = (event) => {
    if (callback) callback(event.detail || {});
  };
  window.addEventListener(SYNC_EVENT_NAME, handler);
  return () => {
    window.removeEventListener(SYNC_EVENT_NAME, handler);
  };
}
