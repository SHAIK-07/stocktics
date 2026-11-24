import React, { useState, useEffect } from 'react';
import { RefreshCw, Link2, Unlink } from 'lucide-react';
import { getIndmoneyAuthUrl, syncIndmoneyPortfolio, getIndmoneyStatus, disconnectIndmoney } from '../../services/api';
import { notifyIndmoneySynced, subscribeToIndmoneySync } from '../../utils/syncEvents';

export async function startIndmoneyAuthFlow(userId, onComplete) {
  if (!userId) {
    alert('Please log in to Stocktics first to connect your INDmoney account.');
    return;
  }
  try {
    const res = await getIndmoneyAuthUrl(userId);
    if (res.auth_url) {

      const width = 500;
      const height = 700;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;
      const popup = window.open(
        res.auth_url,
        'INDmoneyAuth',
        `width=${width},height=${height},top=${top},left=${left},status=no,toolbar=no,menubar=no`
      );

      const timer = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(timer);
          notifyIndmoneySynced({ type: 'AUTH_COMPLETED' });
          if (onComplete) onComplete();
        }
      }, 1500);
    }
  } catch (err) {
    console.error('Error starting INDmoney connection:', err);
    alert('Could not start INDmoney connection: ' + err.message);
  }
}

export default function IndmoneySyncButton({ userId, onSyncComplete, compact = false }) {
  const [status, setStatus] = useState({ connected: false, loading: true });
  const [isSyncing, setIsSyncing] = useState(false);
  const [showMenu, setShowMenu] = useState(false);
  const [syncFeedback, setSyncFeedback] = useState(null);

  const checkStatus = async () => {
    if (!userId) {
      setStatus({ connected: false, loading: false });
      return;
    }
    try {
      const data = await getIndmoneyStatus(userId);
      setStatus({ ...data, loading: false });
    } catch {
      setStatus({ connected: false, loading: false });
    }
  };

  useEffect(() => {
    checkStatus();

    // Listen to global sync events across all pages
    const unsub = subscribeToIndmoneySync(() => {
      checkStatus();
      if (onSyncComplete) onSyncComplete();
    });

    const handleMessage = (event) => {
      if (event.data?.type === 'INDMONEY_AUTH_SUCCESS') {
        setIsSyncing(false);
        checkStatus();
        notifyIndmoneySynced({ type: 'POPUP_SUCCESS' });
        if (onSyncComplete) onSyncComplete();
      }
    };
    window.addEventListener('message', handleMessage);

    return () => {
      unsub();
      window.removeEventListener('message', handleMessage);
    };
  }, [userId]);

  const handleConnect = async () => {
    setIsSyncing(true);
    await startIndmoneyAuthFlow(userId, () => {
      setIsSyncing(false);
      checkStatus();
      if (onSyncComplete) onSyncComplete();
    });
  };

  const handleSyncNow = async () => {
    if (isSyncing || !userId) return;
    setIsSyncing(true);
    setSyncFeedback(null);
    try {
      const res = await syncIndmoneyPortfolio(userId);
      if (res.success) {
        setSyncFeedback(`Synced ${res.indian_stocks_synced || 0} IN, ${res.us_stocks_synced || 0} US stocks!`);
        await checkStatus();
        notifyIndmoneySynced({ type: 'MANUAL_SYNC_SUCCESS', res });
        if (onSyncComplete) onSyncComplete();
      } else {
        setSyncFeedback('Sync failed: ' + (res.error || 'Unknown error'));
      }
    } catch (err) {
      setSyncFeedback('Sync failed: ' + err.message);
    } finally {
      setIsSyncing(false);
      setTimeout(() => setSyncFeedback(null), 4000);
    }
  };

  const handleDisconnect = async () => {
    if (!window.confirm('Disconnect INDmoney? Auto-sync will stop, but existing holdings will remain.')) return;
    try {
      await disconnectIndmoney(userId);
      setShowMenu(false);
      await checkStatus();
      notifyIndmoneySynced({ type: 'DISCONNECTED' });
      if (onSyncComplete) onSyncComplete();
    } catch (err) {
      alert('Error disconnecting: ' + err.message);
    }
  };

  if (status.loading) {
    return (
      <div className="indmoney-pill loading">
        <RefreshCw size={13} className="spin-anim" />
        <span>Checking INDmoney...</span>
      </div>
    );
  }

  if (status.connected) {
    return (
      <div className="indmoney-sync-wrapper">
        <div className="indmoney-pill connected" onClick={() => setShowMenu(!showMenu)}>
          <span className="live-dot" />
          <span className="pill-title">INDmoney Synced</span>
          <button
            className="pill-refresh-btn"
            onClick={(e) => {
              e.stopPropagation();
              handleSyncNow();
            }}
            disabled={isSyncing}
            title="Refresh Holdings & Watchlist from INDmoney"
          >
            <RefreshCw size={12} className={isSyncing ? 'spin-anim' : ''} />
          </button>
        </div>

        {syncFeedback && (
          <div className="indmoney-feedback-toast">
            {syncFeedback}
          </div>
        )}

        {showMenu && (
          <div className="indmoney-dropdown-menu">
            <div className="menu-header">
              <span className="menu-title">INDmoney MCP Sync</span>
              <span className="menu-status">Connected</span>
            </div>
            {status.last_synced_at && (
              <div className="menu-meta">
                Last synced: {new Date(status.last_synced_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
            )}
            <div className="menu-actions">
              <button className="menu-btn" onClick={handleSyncNow} disabled={isSyncing}>
                <RefreshCw size={13} className={isSyncing ? 'spin-anim' : ''} />
                <span>{isSyncing ? 'Syncing...' : 'Sync Portfolio Now'}</span>
              </button>
              <button className="menu-btn danger" onClick={handleDisconnect}>
                <Unlink size={13} />
                <span>Disconnect</span>
              </button>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <button
      className={`indmoney-connect-btn ${compact ? 'compact' : ''}`}
      onClick={handleConnect}
      disabled={isSyncing}
      title="Connect your INDmoney account to auto-import your Indian & US portfolio and watchlists"
    >
      <Link2 size={14} className={isSyncing ? 'spin-anim' : ''} />
      <span>{isSyncing ? 'Connecting...' : 'Sync with INDmoney'}</span>
    </button>
  );
}
