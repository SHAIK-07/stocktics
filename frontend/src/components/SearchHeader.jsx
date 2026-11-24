import React, { useState, useEffect, useRef } from 'react';
import {
  Search, Globe, ChevronRight, Sun, Moon, Sparkles, Layers,
  LayoutDashboard, ShoppingBag, ShieldCheck, User, LogOut, LogIn, Grid, GitCompare, LineChart,
  Link2, RefreshCw, GraduationCap
} from 'lucide-react';
import { searchStocks, getIndmoneyStatus, syncIndmoneyPortfolio } from '../services/api';
import { authService } from '../services/supabase';
import { startIndmoneyAuthFlow } from './common/IndmoneySyncButton';
import { subscribeToIndmoneySync, notifyIndmoneySynced } from '../utils/syncEvents';


const QUICK_PICKS = [
  { symbol: 'AAPL', name: 'Apple Inc.', market: 'US' },
  { symbol: 'NVDA', name: 'Nvidia Corp.', market: 'US' },
  { symbol: 'RELIANCE.NS', name: 'Reliance Industries', market: 'IN' },
  { symbol: 'TCS.NS', name: 'Tata Consultancy', market: 'IN' },
  { symbol: 'MSFT', name: 'Microsoft Corp.', market: 'US' },
  { symbol: 'HDFCBANK.NS', name: 'HDFC Bank', market: 'IN' },
  { symbol: 'GOOGL', name: 'Alphabet Inc.', market: 'US' },
  { symbol: 'TSLA', name: 'Tesla Inc.', market: 'US' }
];

export default function SearchHeader({
  currentTicker,
  onSelectTicker,
  stockData,
  activeTab = 'dashboard',
  onTabChange = () => {},
  market = 'IN',
  onMarketChange = () => {},
  onOpenAuth = () => {}
}) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [user, setUser] = useState(authService.getCurrentUser());
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const dropdownRef = useRef(null);
  const profileMenuRef = useRef(null);

  // INDmoney MCP Connection State
  const [indStatus, setIndStatus] = useState({ connected: false });
  const [isSyncingInd, setIsSyncingInd] = useState(false);

  const checkIndStatus = async (uid = user?.userId) => {
    if (!uid) {
      setIndStatus({ connected: false });
      return;
    }
    try {
      const s = await getIndmoneyStatus(uid);
      setIndStatus(s);
    } catch {
      setIndStatus({ connected: false });
    }
  };

  useEffect(() => {
    const unsub = authService.onAuthStateChange((newUser) => {
      setUser(newUser);
      if (newUser?.userId) checkIndStatus(newUser.userId);
    });
    return () => unsub();
  }, []);

  useEffect(() => {
    if (user?.userId) {
      checkIndStatus(user.userId);
    }
    const unsubSync = subscribeToIndmoneySync(() => {
      if (user?.userId) checkIndStatus(user.userId);
    });
    return () => unsubSync();
  }, [user?.userId]);

  const handleIndmoneyAction = async () => {
    if (!user?.userId) return;
    if (indStatus.connected) {
      setIsSyncingInd(true);
      try {
        const res = await syncIndmoneyPortfolio(user.userId);
        if (res.success) {
          notifyIndmoneySynced({ type: 'HEADER_SYNC_SUCCESS' });
          checkIndStatus(user.userId);
        }
      } catch (err) {
        console.error('Profile sync error:', err);
      } finally {
        setIsSyncingInd(false);
      }
    } else {
      setIsProfileMenuOpen(false);
      await startIndmoneyAuthFlow(user.userId, () => {
        checkIndStatus(user.userId);
        notifyIndmoneySynced({ type: 'AUTH_COMPLETED' });
      });
    }
  };


  // Close profile menu on click outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (profileMenuRef.current && !profileMenuRef.current.contains(e.target)) {
        setIsProfileMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Theme Management (Dark / Light)
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem('stocktics_theme') || 'dark';
    } catch {
      return 'dark';
    }
  });

  useEffect(() => {
    try {
      document.documentElement.setAttribute('data-theme', theme);
      localStorage.setItem('stocktics_theme', theme);
    } catch (e) {
      console.error(e);
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };


  // Debounced search
  useEffect(() => {
    if (!query || query.trim().length < 1) {
      setResults([]);
      setIsOpen(false);
      return;
    }

    const timer = setTimeout(async () => {
      setIsLoading(true);
      const res = await searchStocks(query);
      setResults(res);
      setIsOpen(res.length > 0);
      setIsLoading(false);
    }, 250);

    return () => clearTimeout(timer);
  }, [query]);

  // Click outside to close dropdown
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNavClick = (tab) => {
    if (!user) {
      onOpenAuth('signin');
      return;
    }
    onTabChange(tab);
  };

  const handleSelect = (symbol) => {
    if (!user) {
      onOpenAuth('signin');
      return;
    }
    onSelectTicker(symbol);
    setQuery('');
    setIsOpen(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      if (!user) {
        onOpenAuth('signin');
        return;
      }
      if (results.length > 0) {
        handleSelect(results[0].symbol);
      } else if (query.trim()) {
        handleSelect(query.trim());
      }
    }
  };

  const handleSearchFocus = () => {
    if (!user) {
      onOpenAuth('signin');
      return;
    }
    if (results.length > 0) setIsOpen(true);
  };

  return (
    <>
    <div className="top-header-wrapper">
      <header className="top-header floating-capsule">
        {/* Brand */}
        <div
          className="brand-section"
          onClick={() => {
            if (!user) {
              onOpenAuth('signin');
              return;
            }
            if (activeTab !== 'dashboard') {
              onTabChange('dashboard');
            } else {
              handleScrollToTop();
            }
          }}
          style={{ cursor: 'pointer' }}
          title="Stocktics - Institutional Intelligence"
        >
          <div className="brand-logo-container">
            <img
              src="/stocktics_logo.png"
              alt="Stocktics Logo"
              className="brand-logo-img"
              onError={(e) => {
                e.target.style.display = 'none';
                if (e.target.nextElementSibling) e.target.nextElementSibling.style.display = 'flex';
              }}
            />
            <span className="brand-logo-fallback" style={{ display: 'none' }}>ST</span>
          </div>
          <div className="brand-info">
            <span className="brand-title">
              Stock<span className="brand-title-accent">tics</span>
            </span>
          </div>
        </div>

        {/* Center Zone: Nav Links or Active Telemetry Widget */}
        <div className="header-center-zone">
          <div className="header-nav-switcher">
            <button
              className={`header-nav-btn ${activeTab === 'dashboard' ? 'active' : ''}`}
              onClick={() => handleNavClick('dashboard')}
            >
              <LayoutDashboard size={13} className="tab-sparkle" />
              <span>Dashboard</span>
            </button>
            <button
              className={`header-nav-btn ${activeTab === 'buy' || activeTab === 'sell' ? 'active' : ''}`}
              onClick={() => handleNavClick('buy')}
            >
              <ShoppingBag size={13} className="tab-sparkle" />
              <span>Pipeline</span>
            </button>
            <button
              className={`header-nav-btn ${activeTab === 'scanner' ? 'active' : ''}`}
              onClick={() => handleNavClick('scanner')}
            >
              <Sparkles size={13} className="tab-sparkle" />
              <span>Setups</span>
            </button>
            <button
              className={`header-nav-btn ${activeTab === 'analysis' ? 'active' : ''}`}
              onClick={() => handleNavClick('analysis')}
            >
              <LineChart size={13} className="tab-sparkle" />
              <span>Deep Analysis</span>
            </button>
            <button
              className={`header-nav-btn ${activeTab === 'compare' ? 'active' : ''}`}
              onClick={() => handleNavClick('compare')}
            >
              <GitCompare size={13} className="tab-sparkle" />
              <span>Compare</span>
            </button>
            <button
              className={`header-nav-btn ${activeTab === 'learning' ? 'active' : ''}`}
              onClick={() => handleNavClick('learning')}
            >
              <GraduationCap size={13} className="tab-sparkle" />
              <span>Academy</span>
            </button>
          </div>
        </div>

        {/* Right Zone: Search Bar & Theme Switcher */}
        <div className="header-right-zone">
          <div className="search-wrapper" ref={dropdownRef}>
            <Search className="search-icon-pos" size={16} />
            <input
              type="text"
              className="search-input-box"
              placeholder="Search stocks..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              onFocus={handleSearchFocus}
            />
            {query && (
              <button
                onClick={() => { setQuery(''); setIsOpen(false); }}
                style={{
                  position: 'absolute',
                  right: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'transparent',
                  border: 'none',
                  color: 'var(--text-tertiary)',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: '700'
                }}
              >
                ✕
              </button>
            )}

            {/* Autocomplete Dropdown */}
            {isOpen && (
              <div className="search-dropdown">
                {results.map((item) => {
                  const isNse = item.symbol.endsWith('.NS') || item.exchange === 'NSI' || item.exchange === 'NSE';
                  const isBse = item.symbol.endsWith('.BO') || item.exchange === 'BSE';

                  return (
                    <div
                      key={item.symbol}
                      className="search-dropdown-item"
                      onClick={() => handleSelect(item.symbol)}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <img
                          src={`https://assets.parqet.com/logos/symbol/${item.symbol}?format=png`}
                          alt=""
                          style={{ width: '26px', height: '26px', borderRadius: '6px', objectFit: 'contain', background: '#FFFFFF', padding: '3px', flexShrink: 0 }}
                          onError={(e) => { e.target.style.display = 'none'; }}
                        />
                        <div className="item-sym-name">
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span className="item-symbol font-mono" style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-primary)' }}>
                              {item.symbol}
                            </span>
                            <span className={`item-badge-pill ${isNse || isBse ? 'nse' : 'us'}`}>
                              {item.exchange || (isNse ? 'NSE' : isBse ? 'BSE' : 'US')}
                            </span>
                          </div>
                          <span className="item-fullname" style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                            {item.name}
                          </span>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-tertiary)', fontSize: '11px', fontFamily: 'var(--font-mono)' }}>
                        <span style={{ padding: '2px 6px', background: 'var(--bg-surface-elevated)', borderRadius: '4px' }}>
                          {item.country || (isNse || isBse ? 'India' : 'USA')}
                        </span>
                        <ChevronRight size={14} />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Global Market Switcher (IN / US) */}
          <div className="global-market-toggle-capsule" role="group" aria-label="Market selection">
            <button
              type="button"
              className={`global-market-pill ${market !== 'US' ? 'active' : ''}`}
              onClick={() => onMarketChange('IN')}
              title="Switch entire app to Indian Markets (NSE / BSE)"
            >
              <span className="market-flag">🇮🇳</span>
              <span className="market-code">IN</span>
            </button>
            <button
              type="button"
              className={`global-market-pill ${market === 'US' ? 'active' : ''}`}
              onClick={() => onMarketChange('US')}
              title="Switch entire app to US Markets (NYSE / NASDAQ)"
            >
              <span className="market-flag">🇺🇸</span>
              <span className="market-code">US</span>
            </button>
          </div>

          {/* Theme Mode Switcher Button */}
          <button
            className="theme-toggle-btn"
            onClick={toggleTheme}
            title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            aria-label={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          >
            {theme === 'dark' ? (
              <Sun size={17} className="theme-toggle-icon sun" />
            ) : (
              <Moon size={17} className="theme-toggle-icon moon" />
            )}
          </button>

          {/* User Profile / Auth Action Button */}
          {user ? (
            <div className="user-profile-wrapper" ref={profileMenuRef} style={{ position: 'relative' }}>
              <button
                className="user-profile-btn"
                onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                title={user.email}
              >
                <div className="user-avatar-circle">
                  {(user.email || 'U')[0].toUpperCase()}
                </div>
                <span className="user-email-snip">{user.email?.split('@')[0]}</span>
              </button>

              {isProfileMenuOpen && (
                <div className="user-profile-dropdown" style={{ minWidth: '240px' }}>
                  <div className="dropdown-user-info">
                    <div className="dropdown-user-email">{user.email}</div>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '6px' }}>
                      <div className="dropdown-user-badge">Supabase Connected</div>
                      {indStatus.connected ? (
                        <div
                          className="dropdown-user-badge"
                          style={{
                            background: 'rgba(0, 208, 156, 0.15)',
                            color: '#00D09C',
                            border: '1px solid rgba(0, 208, 156, 0.3)'
                          }}
                        >
                          INDmoney Synced
                        </div>
                      ) : (
                        <div
                          className="dropdown-user-badge"
                          style={{
                            background: 'rgba(245, 158, 11, 0.12)',
                            color: '#F59E0B',
                            border: '1px solid rgba(245, 158, 11, 0.25)'
                          }}
                        >
                          INDmoney Not Synced
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="dropdown-divider" />

                  {/* Dedicated INDmoney Sync Button in Profile */}
                  <button
                    className="dropdown-item-btn"
                    style={{
                      color: '#00D09C',
                      background: 'rgba(0, 208, 156, 0.08)',
                      border: '1px solid rgba(0, 208, 156, 0.25)',
                      borderRadius: '8px',
                      margin: '2px 0 8px 0',
                      padding: '8px 12px',
                      fontWeight: 600
                    }}
                    onClick={handleIndmoneyAction}
                    disabled={isSyncingInd}
                  >
                    {indStatus.connected ? (
                      <>
                        <RefreshCw size={14} className={isSyncingInd ? 'spin-anim' : ''} />
                        <span>{isSyncingInd ? 'Syncing...' : 'Sync INDmoney Portfolio'}</span>
                      </>
                    ) : (
                      <>
                        <Link2 size={14} />
                        <span>Sync with INDmoney</span>
                      </>
                    )}
                  </button>

                  <button
                    className="dropdown-item-btn"
                    onClick={() => {
                      onTabChange('dashboard');
                      setIsProfileMenuOpen(false);
                    }}
                  >
                    <LayoutDashboard size={14} />
                    <span>My Dashboard</span>
                  </button>

                  <button
                    className="dropdown-item-btn text-loss"
                    onClick={() => {
                      authService.signOut();
                      setIsProfileMenuOpen(false);
                    }}
                  >
                    <LogOut size={14} />
                    <span>Sign Out</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              className="header-signin-btn"
              onClick={() => onOpenAuth('signin')}
              title="Sign in to your account"
            >
              <LogIn size={13} />
              <span>Sign In</span>
            </button>
          )}
        </div>
      </header>
    </div>

      {/* Quick Picks Ticker Strip (Only shown on Deep Analysis tab) */}
      {activeTab === 'analysis' && (
        <div className="quick-picks-bar">
          <span style={{ fontSize: '11.5px', fontWeight: '700', color: 'var(--text-tertiary)', display: 'flex', alignItems: 'center', gap: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            <Globe size={14} color="var(--fintech-primary)" /> Benchmarks:
          </span>
          {QUICK_PICKS.map((pick) => (
            <button
              key={pick.symbol}
              className={`quick-pick-btn ${currentTicker.toUpperCase() === pick.symbol.toUpperCase() ? 'active' : ''}`}
              onClick={() => handleSelect(pick.symbol)}
            >
              <img
                src={`https://assets.parqet.com/logos/symbol/${pick.symbol}?format=png`}
                alt=""
                style={{ width: '15px', height: '15px', borderRadius: '3px', objectFit: 'contain', background: '#FFFFFF', padding: '1px' }}
                onError={(e) => { e.target.style.display = 'none'; }}
              />
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: '700' }}>{pick.symbol}</span>
              <span style={{
                fontSize: '9.5px',
                fontWeight: '800',
                padding: '1px 5px',
                borderRadius: '3px',
                background: pick.market === 'IN' ? 'rgba(0, 208, 156, 0.15)' : 'rgba(20, 85, 254, 0.15)',
                color: pick.market === 'IN' ? 'var(--gain-green)' : '#60A5FA'
              }}>
                {pick.market}
              </span>
            </button>
          ))}
        </div>
      )}
    </>
  );
}
