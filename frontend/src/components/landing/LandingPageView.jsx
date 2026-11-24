import React, { useState } from 'react';
import {
  Sparkles, Shield, TrendingUp, BarChart3, LineChart, Lock, ArrowRight,
  CheckCircle2, Globe, Layers, Activity, Award, Check, ShoppingBag,
  ShieldCheck, Grid, GitCompare, Eye, ChevronRight, AlertTriangle, GraduationCap, BookOpen
} from 'lucide-react';

const SHOWCASE_DATA = {
  US: {
    marketName: 'United States (S&P 500 / NASDAQ)',
    flag: '🇺🇸',
    currencySymbol: '$',
    stocks: [
      {
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        price: 126.80,
        change: '+3.82%',
        isPositive: true,
        pattern: 'Cup & Handle Breakout',
        patternScore: 95,
        pivot: 125.00,
        target: 148.00,
        stopLoss: 118.50,
        conviction: 94,
        pillars: { tech: 96, fund: 94, flow: 92, val: 90, mom: 98 },
        entryPrice: 115.00,
        pnl: '+10.26%',
        trailingStop: 121.20,
        ttlScore: 18,
        ttlVerdict: 'Safe Hold',
        marketCap: '$3.12T',
        sector: 'Semiconductors',
        logo: 'https://assets.parqet.com/logos/symbol/NVDA?format=png'
      },
      {
        symbol: 'AAPL',
        name: 'Apple Inc.',
        price: 225.40,
        change: '+1.24%',
        isPositive: true,
        pattern: 'Double Bottom / W-Base',
        patternScore: 91,
        pivot: 224.50,
        target: 255.00,
        stopLoss: 214.00,
        conviction: 90,
        pillars: { tech: 89, fund: 95, flow: 92, val: 83, mom: 91 },
        entryPrice: 218.00,
        pnl: '+3.39%',
        trailingStop: 219.80,
        ttlScore: 24,
        ttlVerdict: 'Safe Hold',
        marketCap: '$3.45T',
        sector: 'Technology',
        logo: 'https://assets.parqet.com/logos/symbol/AAPL?format=png'
      },
      {
        symbol: 'MSFT',
        name: 'Microsoft Corporation',
        price: 438.20,
        change: '+0.86%',
        isPositive: true,
        pattern: 'Flat Base Breakout',
        patternScore: 89,
        pivot: 440.00,
        target: 495.00,
        stopLoss: 422.00,
        conviction: 88,
        pillars: { tech: 88, fund: 96, flow: 90, val: 82, mom: 86 },
        entryPrice: 410.00,
        pnl: '+6.88%',
        trailingStop: 425.00,
        ttlScore: 28,
        ttlVerdict: 'Safe Hold',
        marketCap: '$3.26T',
        sector: 'Technology',
        logo: 'https://assets.parqet.com/logos/symbol/MSFT?format=png'
      },
      {
        symbol: 'AMZN',
        name: 'Amazon.com Inc.',
        price: 187.60,
        change: '+2.15%',
        isPositive: true,
        pattern: 'Volatility Contraction (VCP)',
        patternScore: 92,
        pivot: 186.00,
        target: 215.00,
        stopLoss: 176.00,
        conviction: 91,
        pillars: { tech: 93, fund: 92, flow: 89, val: 91, mom: 90 },
        entryPrice: 175.00,
        pnl: '+7.20%',
        trailingStop: 181.50,
        ttlScore: 20,
        ttlVerdict: 'Safe Hold',
        marketCap: '$1.95T',
        sector: 'Consumer Cyclical',
        logo: 'https://assets.parqet.com/logos/symbol/AMZN?format=png'
      },
      {
        symbol: 'TSLA',
        name: 'Tesla Inc.',
        price: 242.50,
        change: '-1.10%',
        isPositive: false,
        pattern: 'Bull Flag Consolidation',
        patternScore: 86,
        pivot: 245.00,
        target: 290.00,
        stopLoss: 228.00,
        conviction: 84,
        pillars: { tech: 85, fund: 80, flow: 82, val: 85, mom: 88 },
        entryPrice: 215.00,
        pnl: '+12.79%',
        trailingStop: 231.00,
        ttlScore: 35,
        ttlVerdict: 'Tighten Stop',
        marketCap: '$770B',
        sector: 'Automobile',
        logo: 'https://assets.parqet.com/logos/symbol/TSLA?format=png'
      }
    ],
    correlations: [
      { pair: 'NVDA ↔ MSFT', r: '+0.84', type: 'Strong Positive', color: 'pill-green' },
      { pair: 'AAPL ↔ AMZN', r: '+0.72', type: 'Strong Positive', color: 'pill-green' },
      { pair: 'MSFT ↔ AMZN', r: '+0.68', type: 'Moderate Positive', color: 'pill-emerald' },
      { pair: 'NVDA ↔ TSLA', r: '+0.46', type: 'Moderate Positive', color: 'pill-emerald' },
      { pair: 'AAPL ↔ TSLA', r: '-0.18', type: 'Uncorrelated / Inverse', color: 'pill-neutral' }
    ]
  },
  IN: {
    marketName: 'India (NSE / BSE / Nifty 50)',
    flag: '🇮🇳',
    currencySymbol: '₹',
    stocks: [
      {
        symbol: 'RELIANCE.NS',
        name: 'Reliance Industries Ltd',
        price: 2975.00,
        change: '+1.75%',
        isPositive: true,
        pattern: 'Ascending Triangle Breakout',
        patternScore: 93,
        pivot: 2960.00,
        target: 3350.00,
        stopLoss: 2840.00,
        conviction: 92,
        pillars: { tech: 94, fund: 95, flow: 93, val: 88, mom: 90 },
        entryPrice: 2850.00,
        pnl: '+4.39%',
        trailingStop: 2890.00,
        ttlScore: 22,
        ttlVerdict: 'Safe Hold',
        marketCap: '₹20.1T',
        sector: 'Energy',
        logo: 'https://assets.parqet.com/logos/symbol/RELIANCE.NS?format=png'
      },
      {
        symbol: 'TCS.NS',
        name: 'Tata Consultancy Services',
        price: 4240.00,
        change: '+0.92%',
        isPositive: true,
        pattern: 'Cup & Handle Breakout',
        patternScore: 90,
        pivot: 4220.00,
        target: 4750.00,
        stopLoss: 4050.00,
        conviction: 89,
        pillars: { tech: 89, fund: 96, flow: 91, val: 81, mom: 88 },
        entryPrice: 3950.00,
        pnl: '+7.34%',
        trailingStop: 4120.00,
        ttlScore: 26,
        ttlVerdict: 'Safe Hold',
        marketCap: '₹15.3T',
        sector: 'Technology',
        logo: 'https://assets.parqet.com/logos/symbol/TCS.NS?format=png'
      },
      {
        symbol: 'HDFCBANK.NS',
        name: 'HDFC Bank Limited',
        price: 1672.00,
        change: '+1.40%',
        isPositive: true,
        pattern: 'Double Bottom / W-Base',
        patternScore: 91,
        pivot: 1660.00,
        target: 1920.00,
        stopLoss: 1580.00,
        conviction: 90,
        pillars: { tech: 91, fund: 94, flow: 92, val: 84, mom: 89 },
        entryPrice: 1590.00,
        pnl: '+5.16%',
        trailingStop: 1620.00,
        ttlScore: 19,
        ttlVerdict: 'Safe Hold',
        marketCap: '₹12.7T',
        sector: 'Financial Services',
        logo: 'https://assets.parqet.com/logos/symbol/HDFCBANK.NS?format=png'
      },
      {
        symbol: 'ICICIBANK.NS',
        name: 'ICICI Bank Limited',
        price: 1248.00,
        change: '+2.10%',
        isPositive: true,
        pattern: 'Multi-Week Flat Base',
        patternScore: 94,
        pivot: 1235.00,
        target: 1420.00,
        stopLoss: 1170.00,
        conviction: 93,
        pillars: { tech: 95, fund: 96, flow: 92, val: 88, mom: 94 },
        entryPrice: 1160.00,
        pnl: '+7.59%',
        trailingStop: 1210.00,
        ttlScore: 16,
        ttlVerdict: 'Safe Hold',
        marketCap: '₹8.8T',
        sector: 'Financial Services',
        logo: 'https://assets.parqet.com/logos/symbol/ICICIBANK.NS?format=png'
      },
      {
        symbol: 'INFY.NS',
        name: 'Infosys Limited',
        price: 1925.00,
        change: '-0.65%',
        isPositive: false,
        pattern: 'Channel Breakout & Retest',
        patternScore: 87,
        pivot: 1910.00,
        target: 2180.00,
        stopLoss: 1820.00,
        conviction: 86,
        pillars: { tech: 87, fund: 93, flow: 88, val: 77, mom: 85 },
        entryPrice: 1780.00,
        pnl: '+8.15%',
        trailingStop: 1860.00,
        ttlScore: 30,
        ttlVerdict: 'Safe Hold',
        marketCap: '₹8.0T',
        sector: 'Technology',
        logo: 'https://assets.parqet.com/logos/symbol/INFY.NS?format=png'
      }
    ],
    correlations: [
      { pair: 'HDFCBANK ↔ ICICIBANK', r: '+0.88', type: 'Strong Positive', color: 'pill-green' },
      { pair: 'TCS ↔ INFY', r: '+0.81', type: 'Strong Positive', color: 'pill-green' },
      { pair: 'RELIANCE ↔ ICICIBANK', r: '+0.62', type: 'Moderate Positive', color: 'pill-emerald' },
      { pair: 'TCS ↔ RELIANCE', r: '+0.48', type: 'Moderate Positive', color: 'pill-emerald' },
      { pair: 'INFY ↔ HDFCBANK', r: '+0.12', type: 'Uncorrelated / Low', color: 'pill-neutral' }
    ]
  }
};

const GHOST_DATA = {
  US: [
    { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 178.50, change: '+2.10%', pattern: 'VCP Breakout (Stage 2)', pivot: 176.00, target: 198.00, score: 91, conviction: 91, sector: 'Communication' },
    { symbol: 'META', name: 'Meta Platforms', price: 512.30, change: '+1.85%', pattern: 'High-Tight Flag', pivot: 508.00, target: 565.00, score: 89, conviction: 90, sector: 'Technology' },
    { symbol: 'AMD', name: 'Advanced Micro Devices', price: 154.20, change: '+3.40%', pattern: 'Double Bottom Retest', pivot: 151.00, target: 175.00, score: 88, conviction: 88, sector: 'Semiconductors' },
    { symbol: 'NFLX', name: 'Netflix Inc.', price: 685.00, change: '+0.95%', pattern: 'Ascending Base Pivot', pivot: 678.00, target: 740.00, score: 87, conviction: 87, sector: 'Entertainment' }
  ],
  IN: [
    { symbol: 'SBIN.NS', name: 'State Bank of India', price: 815.00, change: '+1.60%', pattern: 'Cup & Handle Breakout', pivot: 808.00, target: 920.00, score: 90, conviction: 90, sector: 'Banking' },
    { symbol: 'LT.NS', name: 'Larsen & Toubro Ltd', price: 3620.00, change: '+2.10%', pattern: 'Stage 1 Flat Base', pivot: 3580.00, target: 4050.00, score: 92, conviction: 92, sector: 'Engineering' },
    { symbol: 'BHARTIARTL.NS', name: 'Bharti Airtel', price: 1480.00, change: '+1.25%', pattern: 'VCP Pivot Consolidation', pivot: 1465.00, target: 1650.00, score: 89, conviction: 89, sector: 'Telecom' },
    { symbol: 'KOTAKBANK.NS', name: 'Kotak Mahindra Bank', price: 1790.00, change: '+0.80%', pattern: 'Double Bottom Base', pivot: 1775.00, target: 1980.00, score: 86, conviction: 86, sector: 'Banking' }
  ]
};

export default function LandingPageView({ onOpenAuth }) {
  const [selectedMarket, setSelectedMarket] = useState('US');
  const marketData = SHOWCASE_DATA[selectedMarket];
  const ghostStocks = GHOST_DATA[selectedMarket];

  return (
    <div className="landing-page-container">
      {/* 1. HERO SECTION */}
      <section className="landing-hero-section">
        <div className="landing-hero-glow" />
        <div className="landing-badge-pill">
          <Sparkles size={14} className="text-emerald" />
          <span>INSTITUTIONAL QUANT INTELLIGENCE & RISK ENGINE</span>
          <span className="live-status-dot" />
        </div>

        <h1 className="landing-hero-title">
          Precision Financial Intelligence <br />
          <span className="hero-gradient-text">For High-Conviction Market Moves</span>
        </h1>

        <p className="landing-hero-subtitle">
          Audited fundamental statements, 33 algorithmic base pattern detectors,
          5-pillar conviction scoring, and institutional Time-to-Leave (TTL) exit risk analytics
          across US & Indian equities.
        </p>

        <div className="landing-hero-cta-group">
          <button
            className="landing-btn-primary"
            onClick={() => onOpenAuth('signup')}
          >
            <span>Create Free Account &mdash; 100% Free</span>
            <ArrowRight size={16} />
          </button>

          <button
            className="landing-btn-secondary"
            onClick={() => onOpenAuth('signin')}
          >
            <Lock size={15} />
            <span>Sign In to Platform</span>
          </button>
        </div>

        {/* Trust Badges Strip */}
        <div className="landing-trust-strip">
          <div className="trust-item">
            <CheckCircle2 size={16} className="text-emerald" />
            <span>100% Real Audited Filings (SEC / NSE)</span>
          </div>
          <div className="trust-divider" />
          <div className="trust-item">
            <CheckCircle2 size={16} className="text-emerald" />
            <span>33 Quantitative Base Detectors</span>
          </div>
          <div className="trust-divider" />
          <div className="trust-item">
            <CheckCircle2 size={16} className="text-emerald" />
            <span>Zero Exaggerated Hype &mdash; Pure Data</span>
          </div>
        </div>
      </section>

      {/* 2. DUAL-MARKET SAMPLE SELECTOR BAR */}
      <div className="landing-sample-nav-wrapper">
        <div className="landing-sample-nav-card">
          <div className="sample-nav-text">
            <span className="sample-nav-badge">LIVE PLATFORM SHOWCASE</span>
            <h3 className="sample-nav-title">Explore Real Sample Intelligence</h3>
            <p className="sample-nav-desc">
              Interact with top 5 high-conviction securities below. Sign in for full 500+ security scans.
            </p>
          </div>

          <div className="landing-market-toggle-pills">
            <button
              className={`market-toggle-pill ${selectedMarket === 'US' ? 'active' : ''}`}
              onClick={() => setSelectedMarket('US')}
            >
              🇺🇸 Top US Tech Leaders (S&P 500)
            </button>
            <button
              className={`market-toggle-pill ${selectedMarket === 'IN' ? 'active' : ''}`}
              onClick={() => setSelectedMarket('IN')}
            >
              🇮🇳 Indian Blue-Chips (Nifty 50)
            </button>
          </div>
        </div>
      </div>

      {/* =========================================================================
          FEATURE 1: LIVE PATTERN SCANNER & HIGH-CONVICTION SETUPS
          ========================================================================= */}
      <section className="landing-feature-module">
        <div className="module-header-row">
          <div className="module-title-group">
            <div className="module-badge">
              <Layers size={13} className="text-emerald" />
              <span>PILLAR 1: QUANT PATTERN SCANNER</span>
            </div>
            <h2 className="module-heading">Active High-Conviction Base Setups</h2>
            <p className="module-subheading">
              Algorithmic detection of Cup & Handles, Double Bottoms, and VCP patterns with mathematical pivots.
            </p>
          </div>
          <button className="module-unlock-cta" onClick={() => onOpenAuth('signup')}>
            <span>Unlock All 33 Setups</span>
            <ChevronRight size={14} />
          </button>
        </div>

        {/* 5 Real Unlocked Setup Cards */}
        <div className="landing-setups-grid">
          {marketData.stocks.map((st) => (
            <div
              key={st.symbol}
              className="landing-setup-card"
              onClick={() => onOpenAuth('signup')}
            >
              <div className="setup-card-top">
                <div className="setup-identity">
                  <img src={st.logo} alt="" className="setup-logo" onError={(e) => { e.target.style.display = 'none'; }} />
                  <div>
                    <div className="setup-sym-row">
                      <span className="setup-symbol font-mono">{st.symbol.replace('.NS', '')}</span>
                      <span className="setup-sector">{st.sector}</span>
                    </div>
                    <div className="setup-name">{st.name}</div>
                  </div>
                </div>
                <div className="setup-score-badge font-mono">
                  {st.patternScore} SCORE
                </div>
              </div>

              <div className="setup-pattern-banner">
                <span className="pattern-name">{st.pattern}</span>
                <span className="pattern-status">Validated Pivot</span>
              </div>

              <div className="setup-metrics-grid font-mono">
                <div className="metric-box">
                  <span className="label">Live Price</span>
                  <span className="value">{marketData.currencySymbol}{st.price.toFixed(2)}</span>
                </div>
                <div className="metric-box">
                  <span className="label">Entry Pivot</span>
                  <span className="value text-emerald">{marketData.currencySymbol}{st.pivot.toFixed(2)}</span>
                </div>
                <div className="metric-box">
                  <span className="label">Profit Target</span>
                  <span className="value gain-green">{marketData.currencySymbol}{st.target.toFixed(2)}</span>
                </div>
                <div className="metric-box">
                  <span className="label">Stop Loss</span>
                  <span className="value loss-red">{marketData.currencySymbol}{st.stopLoss.toFixed(2)}</span>
                </div>
              </div>

              <div className="setup-action-link">
                <span>View Full Quantitative Audit</span>
                <ArrowRight size={12} />
              </div>
            </div>
          ))}
        </div>

        {/* Blurred Locked Gated Section */}
        <div className="landing-blur-container">
          <div className="blurred-ghost-cards" aria-hidden="true">
            {ghostStocks.map((gst) => (
              <div key={gst.symbol} className="landing-setup-card ghost-card">
                <div className="setup-card-top">
                  <div className="setup-identity">
                    <div className="setup-icon-box ghost-avatar">{gst.symbol.slice(0, 2)}</div>
                    <div>
                      <span className="setup-symbol">{gst.symbol.replace('.NS', '')}</span>
                      <span className="setup-name">{gst.name}</span>
                    </div>
                  </div>
                  <span className="score-pill score-high">{gst.score} SCORE</span>
                </div>
                <div className="setup-pattern-row">
                  <span className="pattern-label">{gst.pattern}</span>
                </div>
                <div className="setup-metrics-grid">
                  <div className="setup-metric-item">
                    <span className="metric-lbl">LIVE PRICE</span>
                    <span className="metric-val">{marketData.currencySymbol}{gst.price.toFixed(2)}</span>
                  </div>
                  <div className="setup-metric-item">
                    <span className="metric-lbl">ENTRY PIVOT</span>
                    <span className="metric-val text-emerald">{marketData.currencySymbol}{gst.pivot.toFixed(2)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="blur-overlay-lock-card">
            <div className="lock-badge-pill">
              <Lock size={12} className="lock-icon" />
              <span>PRO QUANT SCANNER</span>
            </div>
            <h3>28+ More Base Breakout Setups Active</h3>
            <p>
              Free accounts get full access to all 33 base pattern detectors across US & Indian markets in real-time.
            </p>
            <button className="lock-cta-btn" onClick={() => onOpenAuth('signup')}>
              <span>Create Free Account to Unlock</span>
              <ArrowRight size={15} />
            </button>
            <div className="lock-trust-footnote">
              <span>✦ 100% Free Forever</span>
              <span className="dot">•</span>
              <span>Instant Access</span>
              <span className="dot">•</span>
              <span>No Card Required</span>
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          FEATURE 2: 5-PILLAR BUY PIPELINE
          ========================================================================= */}
      <section className="landing-feature-module">
        <div className="module-header-row">
          <div className="module-title-group">
            <div className="module-badge">
              <ShoppingBag size={13} className="text-emerald" />
              <span>PILLAR 2: CONVICTION BUY PIPELINE</span>
            </div>
            <h2 className="module-heading">Systematic Multi-Vector Buy Zones</h2>
            <p className="module-subheading">
              Composite 0&ndash;100 quant score evaluating Technicals, Fundamentals, Smart Money Flow, Valuation & Momentum.
            </p>
          </div>
          <button className="module-unlock-cta" onClick={() => onOpenAuth('signup')}>
            <span>Unlock Full Pipeline</span>
            <ChevronRight size={14} />
          </button>
        </div>

        {/* 5 Real Unlocked Pipeline Stocks Table */}
        <div className="landing-table-wrapper">
          <table className="landing-table font-mono">
            <thead>
              <tr>
                <th>Security</th>
                <th>Live Price</th>
                <th>24h Return</th>
                <th>Conviction Score</th>
                <th>5-Pillar Scorecard</th>
                <th>Suggested Buy Pivot</th>
                <th>Target & Gain</th>
              </tr>
            </thead>
            <tbody>
              {marketData.stocks.map((st) => (
                <tr key={st.symbol} onClick={() => onOpenAuth('signup')} className="clickable-row">
                  <td>
                    <div className="table-security-cell">
                      <img src={st.logo} alt="" className="table-avatar" onError={(e) => { e.target.style.display = 'none'; }} />
                      <div>
                        <span className="table-sym font-bold">{st.symbol.replace('.NS', '')}</span>
                        <span className="table-subname">{st.name}</span>
                      </div>
                    </div>
                  </td>
                  <td>{marketData.currencySymbol}{st.price.toFixed(2)}</td>
                  <td className={st.isPositive ? 'gain-green font-bold' : 'loss-red font-bold'}>
                    {st.change}
                  </td>
                  <td>
                    <div className="table-conviction-pill">
                      <span className="conviction-val">{st.conviction}</span>
                      <span className="conviction-max">/100</span>
                    </div>
                  </td>
                  <td>
                    <div className="table-pillars-compact">
                      <span title={`Technicals: ${st.pillars.tech}`}>T:{st.pillars.tech}</span>
                      <span title={`Fundamentals: ${st.pillars.fund}`}>F:{st.pillars.fund}</span>
                      <span title={`Money Flow: ${st.pillars.flow}`}>M:{st.pillars.flow}</span>
                      <span title={`Valuation: ${st.pillars.val}`}>V:{st.pillars.val}</span>
                    </div>
                  </td>
                  <td className="text-emerald font-bold">
                    {marketData.currencySymbol}{st.pivot.toFixed(2)}
                  </td>
                  <td className="gain-green font-bold">
                    {marketData.currencySymbol}{st.target.toFixed(2)} (+{(((st.target - st.price) / st.price) * 100).toFixed(1)}%)
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Blurred Locked Pipeline Rows */}
        <div className="landing-blur-container table-blur">
          <div className="blurred-ghost-table" aria-hidden="true">
            <table className="landing-table font-mono">
              <tbody>
                {ghostStocks.map((gst) => (
                  <tr key={gst.symbol}>
                    <td>
                      <div className="table-security-cell">
                        <div className="setup-icon-box mini ghost-avatar">{gst.symbol.slice(0, 2)}</div>
                        <div>
                          <span className="table-sym font-bold">{gst.symbol.replace('.NS', '')}</span>
                          <span className="table-subname">{gst.name}</span>
                        </div>
                      </div>
                    </td>
                    <td>{marketData.currencySymbol}{gst.price.toFixed(2)}</td>
                    <td className="gain-green font-bold">{gst.change}</td>
                    <td>
                      <div className="table-conviction-pill">
                        <span className="conviction-val">{gst.conviction}</span>
                        <span className="conviction-max">/100</span>
                      </div>
                    </td>
                    <td><span className="ghost-scorecard-bars">T:92 F:94 M:90</span></td>
                    <td>{marketData.currencySymbol}{gst.pivot.toFixed(2)}</td>
                    <td>{marketData.currencySymbol}{gst.target.toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="blur-overlay-lock-card">
            <div className="lock-badge-pill">
              <Lock size={12} className="lock-icon" />
              <span>CONVICTION BUY ENGINE</span>
            </div>
            <h3>40+ Stocks in Institutional Buy Zones</h3>
            <p>
              Log your buys, calculate position sizing, and filter by risk:reward ratios with 100% free registration.
            </p>
            <button className="lock-cta-btn" onClick={() => onOpenAuth('signup')}>
              <span>Unlock Complete Buy Pipeline</span>
              <ArrowRight size={15} />
            </button>
            <div className="lock-trust-footnote">
              <span>✦ 100% Free Forever</span>
              <span className="dot">•</span>
              <span>Instant Access</span>
              <span className="dot">•</span>
              <span>No Card Required</span>
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          FEATURE 3: HOLDINGS & TTL EXIT RISK ENGINE
          ========================================================================= */}
      <section className="landing-feature-module">
        <div className="module-header-row">
          <div className="module-title-group">
            <div className="module-badge">
              <ShieldCheck size={13} className="text-emerald" />
              <span>PILLAR 3: TIME-TO-LEAVE EXIT RISK</span>
            </div>
            <h2 className="module-heading">Active Holdings & Capital Protection</h2>
            <p className="module-subheading">
              Real-time portfolio management tracking dynamic trailing stops and quantitative Time-To-Leave scores.
            </p>
          </div>
          <button className="module-unlock-cta" onClick={() => onOpenAuth('signup')}>
            <span>Track Your Portfolio</span>
            <ChevronRight size={14} />
          </button>
        </div>

        {/* 5 Real Unlocked Holdings Table */}
        <div className="landing-table-wrapper">
          <table className="landing-table font-mono">
            <thead>
              <tr>
                <th>Holding Position</th>
                <th>Entry Price</th>
                <th>Live Market Price</th>
                <th>Unrealized P&L</th>
                <th>Trailing Stop Level</th>
                <th>TTL Risk Score</th>
                <th>Exit Verdict</th>
              </tr>
            </thead>
            <tbody>
              {marketData.stocks.map((st) => (
                <tr key={st.symbol} onClick={() => onOpenAuth('signup')} className="clickable-row">
                  <td>
                    <div className="table-security-cell">
                      <img src={st.logo} alt="" className="table-avatar" onError={(e) => { e.target.style.display = 'none'; }} />
                      <div>
                        <span className="table-sym font-bold">{st.symbol.replace('.NS', '')}</span>
                        <span className="table-subname">{st.sector}</span>
                      </div>
                    </div>
                  </td>
                  <td>{marketData.currencySymbol}{st.entryPrice.toFixed(2)}</td>
                  <td>{marketData.currencySymbol}{st.price.toFixed(2)}</td>
                  <td className="gain-green font-bold">{st.pnl}</td>
                  <td className="loss-red font-bold">
                    {marketData.currencySymbol}{st.trailingStop.toFixed(2)}
                  </td>
                  <td>
                    <span className="ttl-score-pill font-bold">
                      {st.ttlScore} / 100
                    </span>
                  </td>
                  <td>
                    <span className={`verdict-pill ${st.ttlScore > 30 ? 'pill-amber' : 'pill-green'}`}>
                      {st.ttlVerdict}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Blurred Locked Holdings Rows */}
        <div className="landing-blur-container table-blur">
          <div className="blurred-ghost-table" aria-hidden="true">
            <table className="landing-table font-mono">
              <tbody>
                {ghostStocks.map((gst) => (
                  <tr key={gst.symbol}>
                    <td>
                      <div className="table-security-cell">
                        <div className="setup-icon-box mini ghost-avatar">{gst.symbol.slice(0, 2)}</div>
                        <div>
                          <span className="table-sym font-bold">{gst.symbol.replace('.NS', '')}</span>
                          <span className="table-subname">{gst.sector}</span>
                        </div>
                      </div>
                    </td>
                    <td>{marketData.currencySymbol}{(gst.price * 0.94).toFixed(2)}</td>
                    <td>{marketData.currencySymbol}{gst.price.toFixed(2)}</td>
                    <td className="gain-green font-bold">+6.38%</td>
                    <td className="loss-red font-bold">{marketData.currencySymbol}{(gst.price * 0.96).toFixed(2)}</td>
                    <td><span className="ttl-score-pill font-bold">22 / 100</span></td>
                    <td><span className="verdict-pill pill-green">Safe Hold</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="blur-overlay-lock-card">
            <div className="lock-badge-pill">
              <Lock size={12} className="lock-icon" />
              <span>CAPITAL PROTECTION</span>
            </div>
            <h3>Protect Your Real Capital with Institutional Risk Engines</h3>
            <p>
              Never let winning trades turn into losing positions. Track your custom portfolio with automatic trailing stop alerts.
            </p>
            <button className="lock-cta-btn" onClick={() => onOpenAuth('signup')}>
              <span>Start Tracking Your Portfolio Free</span>
              <ArrowRight size={15} />
            </button>
            <div className="lock-trust-footnote">
              <span>✦ 100% Free Forever</span>
              <span className="dot">•</span>
              <span>Instant Access</span>
              <span className="dot">•</span>
              <span>No Card Required</span>
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          FEATURE 4 & 5 GRID: HEATMAP TREEMAP & STOCK COMPARISON WORKSTATIONS
          ========================================================================= */}
      <div className="landing-dual-module-grid">
        {/* Module 4: Treemap Heatmap Preview */}
        <section className="landing-feature-card-large">
          <div className="module-badge">
            <Grid size={13} className="text-emerald" />
            <span>PILLAR 4: SQUARIFIED TREEMAP</span>
          </div>
          <h2 className="module-card-title">Live Market Capital Heatmap</h2>
          <p className="module-card-desc">
            Visual squarified market breadth tracking capital concentration across industry sectors.
          </p>

          {/* Interactive 5-Stock Heatmap Canvas Preview */}
          <div className="landing-treemap-preview-box">
            <div className="preview-treemap-grid">
              {marketData.stocks.map((st, idx) => (
                <div
                  key={st.symbol}
                  className="preview-treemap-tile"
                  style={{
                    backgroundColor: st.isPositive ? '#00B386' : '#DC2626',
                    flex: idx === 0 ? '1.8' : idx === 1 ? '1.5' : '1'
                  }}
                  onClick={() => onOpenAuth('signup')}
                >
                  <span className="font-mono font-bold">{st.symbol.replace('.NS', '')}</span>
                  <span className="font-mono text-small">{st.change}</span>
                  <span className="font-mono text-micro">{st.marketCap}</span>
                </div>
              ))}
            </div>

            {/* Blurred Lower Half */}
            <div className="treemap-blur-strip" onClick={() => onOpenAuth('signup')}>
              <div className="blur-strip-content">
                <Lock size={15} className="text-emerald" />
                <span>+ 45 More Constituents in {marketData.marketName}</span>
                <span className="unlock-inline-tag">Unlock Full Heatmap</span>
              </div>
            </div>
          </div>
        </section>

        {/* Module 5: Comparison & Correlation Matrix Preview */}
        <section className="landing-feature-card-large">
          <div className="module-badge">
            <GitCompare size={13} className="text-emerald" />
            <span>PILLAR 5: STATISTICAL CORRELATION</span>
          </div>
          <h2 className="module-card-title">Cross-Asset Comparison & Pearson ($r$)</h2>
          <p className="module-card-desc">
            Normalized percentage returns superimposed on single timeframes with pairwise Pearson coefficients.
          </p>

          {/* Correlation Pairs List */}
          <div className="landing-correlation-preview-list">
            {marketData.correlations.map((c, idx) => (
              <div
                key={idx}
                className="preview-correlation-row"
                onClick={() => onOpenAuth('signup')}
              >
                <span className="font-mono font-bold">{c.pair}</span>
                <div className="correlation-row-right">
                  <span className={`correlation-pill ${c.color} font-mono`}>{c.r}</span>
                  <span className="correlation-type">{c.type}</span>
                </div>
              </div>
            ))}

            {/* Blurred Lower Half */}
            <div className="treemap-blur-strip" onClick={() => onOpenAuth('signup')}>
              <div className="blur-strip-content">
                <Lock size={15} className="text-emerald" />
                <span>Superimposed Multi-Line SVG Chart & 100+ Correlation Pairs</span>
                <span className="unlock-inline-tag">Unlock Compare</span>
              </div>
            </div>
          </div>
        </section>
      </div>

      {/* =========================================================================
          FEATURE 6: 40-LESSON QUANTITATIVE & FINANCIAL ACADEMY
          ========================================================================= */}
      <section className="landing-feature-module" style={{ marginTop: '32px' }}>
        <div className="module-header-row">
          <div className="module-title-group">
            <div className="module-badge">
              <GraduationCap size={13} className="text-emerald" />
              <span>PILLAR 6: DATA-DRIVEN FINANCIAL ACADEMY</span>
            </div>
            <h2 className="module-heading">40-Lesson Institutional Quantitative Curriculum</h2>
            <p className="module-subheading">
              Progress from stock market foundations and SEC 10-K forensic statements to quantitative factor profiling, multi-factor models, backtesting, and institutional risk management.
            </p>
          </div>
          <button className="module-unlock-cta" onClick={() => onOpenAuth('signup')}>
            <span>Explore 40 Lessons</span>
            <ChevronRight size={14} />
          </button>
        </div>

        <div className="landing-stages-preview-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px', marginTop: '16px' }}>
          {[
            { num: 1, title: 'Stock Market Foundations', range: 'Lessons 1–7', desc: 'Exchange structure, T+1 settlement, limit orders, 3-statement reconciliation & cash flow.' },
            { num: 2, title: 'Company Quality & Moats', range: 'Lessons 8–11', desc: 'DuPont ROE/ROIC decomposition, economic moats, pricing power, and capital allocation.' },
            { num: 3, title: 'Valuation & Multiple Arbitrage', range: 'Lessons 12–16', desc: 'P/E inversion, EV/EBITDA, owner earnings, DCF sensitivity, and relative peer benchmarking.' },
            { num: 4, title: 'Quantitative Factor Models', range: 'Lessons 17–22', desc: 'Value, momentum, quality, growth factors, and multi-factor decile spread ranking.' },
            { num: 5, title: 'Data & Pattern Discovery', range: 'Lessons 23–27', desc: 'Data hygiene, outlier winsorization, information coefficient (IC), and fundamental patterns.' },
            { num: 6, title: 'Backtesting & Bias Defense', range: 'Lessons 28–32', desc: 'Look-ahead bias defense, delisting survivorship graveyard, and curve-fitting avoidance.' },
            { num: 7, title: 'Portfolio Construction', range: 'Lessons 33–35', desc: 'Volatility-weighted position sizing, drawdown recovery, and multi-factor portfolio design.' },
            { num: 8, title: 'Real Investing Process', range: 'Lessons 36–40', desc: 'Pre-investment checklist, entry/hold/exit criteria, and the complete institutional playbook.' }
          ].map(stg => (
            <div
              key={stg.num}
              onClick={() => onOpenAuth('signup')}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-hairline)',
                borderRadius: '12px',
                padding: '16px',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
              className="clickable-stage-card"
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <span style={{ fontSize: '10px', fontWeight: '800', color: 'var(--fintech-primary)', background: 'var(--gain-green-bg)', padding: '2px 6px', borderRadius: '4px' }}>
                  STAGE {stg.num}
                </span>
                <span style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>{stg.range}</span>
              </div>
              <h4 style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '4px' }}>{stg.title}</h4>
              <p style={{ fontSize: '11px', color: 'var(--text-secondary)', lineHeight: '1.45', margin: 0 }}>{stg.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* =========================================================================
          BOTTOM CONVERSION BANNER (FREE FOREVER)
          ========================================================================= */}
      <section className="landing-bottom-cta-section">
        <div className="bottom-cta-card">
          <div className="cta-icon-badge">
            <Award size={28} className="text-emerald" />
          </div>
          <h2>Access Tier-1 Institutional Alpha. 100% Free Forever.</h2>
          <p>
            Join intelligent active investors analyzing audited financial statements,
            algorithmic base patterns, and real-time capital protection engines across US & Indian markets.
          </p>
          <div className="bottom-cta-buttons">
            <button
              className="landing-btn-primary"
              onClick={() => onOpenAuth('signup')}
            >
              <span>Create Free Account</span>
              <ArrowRight size={16} />
            </button>
            <button
              className="landing-btn-secondary"
              onClick={() => onOpenAuth('signin')}
            >
              <Lock size={15} />
              <span>Sign In to Existing Account</span>
            </button>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="landing-footer">
        <div className="footer-content">
          <div className="footer-brand">
            <span className="footer-brand-name">Stocktics &bull; Institutional Financial Intelligence</span>
          </div>
          <p className="footer-disclaimer">
            Stocktics is a quantitative financial technology research platform providing objective mathematical models.
            Not registered investment advisory. Audited data powered by SEC Edgar & NSE/BSE filings.
          </p>
        </div>
      </footer>
    </div>
  );
}
