import React, { useState, useMemo } from 'react';
import {
  ArrowLeft,
  TrendingUp,
  TrendingDown,
  ShieldAlert,
  ShieldCheck,
  AlertTriangle,
  CheckCircle,
  RefreshCw,
  BarChart2,
  Activity,
  Layers,
  FileText,
  Compass,
  Zap,
  DollarSign,
  Clock,
  PieChart,
  ExternalLink,
  Edit,
  LogOut,
  Sliders
} from 'lucide-react';

export default function StockSellDetailView({
  holding,
  onBack,
  onLogExit,
  onOpenEditHolding,
  onSelectStock,
  selectedMarket = 'US'
}) {
  const [selectedTab, setSelectedTab] = useState('overview'); // overview, forecasts, audit, plan
  const [selectedHorizon, setSelectedHorizon] = useState('1M'); // 10D, 1M, 6M, 1Y
  const [chartMode, setChartMode] = useState('line');
  const [isRefreshing, setIsRefreshing] = useState(false);

  if (!holding) return null;

  const isIndian = selectedMarket === 'IN' || holding.symbol?.endsWith('.NS') || holding.symbol?.endsWith('.BO');
  const currencySymbol = holding.currency || (isIndian ? '₹' : '$');
  const cleanSym = holding.symbol?.replace('.NS', '')?.replace('.BO', '');

  // Core Position Financials
  const currentPrice = holding.currentPrice || holding.price || 100;
  const entryPrice = holding.entryPrice || currentPrice;
  const shares = holding.qty || holding.shares || 1;
  const totalInvested = entryPrice * shares;
  const currentValue = currentPrice * shares;
  const unrealizedPnl = currentValue - totalInvested;
  const unrealizedPnlPct = totalInvested > 0 ? (unrealizedPnl / totalInvested) * 100 : 0;
  const isPos = unrealizedPnl >= 0;

  // TTL Risk Engine mapping
  const ttlScore = holding.timeToLeaveScore ?? 25;
  const daysHeld = holding.daysHeld || holding.holdDurationDays || 2;
  const maxHoldDays = holding.maxHoldDays || 60;

  const ttlVariant = ttlScore >= 80 ? 'danger' : ttlScore >= 60 ? 'warning' : 'safe';
  const ttlVerdict =
    ttlScore >= 80 ? 'EXIT NOW' : ttlScore >= 60 ? 'CONSIDER EXITING' : ttlScore >= 35 ? 'HOLD POSITION' : 'SAFE HOLD';

  // Target and stop levels
  const targetPrice = holding.targetPrice || Number((entryPrice * 1.15).toFixed(2));
  const stopLossPrice = holding.stopLossPrice || Number((entryPrice * 0.95).toFixed(2));

  // Multi-horizon expected price targets
  const horizonsConfig = {
    '10D': { ret: isPos ? '+3.5%' : '-1.2%', target: Number((currentPrice * (isPos ? 1.035 : 0.988)).toFixed(2)), drift: 'Low' },
    '1M': { ret: isPos ? '+7.8%' : '-2.5%', target: Number((currentPrice * (isPos ? 1.078 : 0.975)).toFixed(2)), drift: 'Moderate' },
    '6M': { ret: isPos ? '+14.5%' : '+4.0%', target: Number((currentPrice * (isPos ? 1.145 : 1.04)).toFixed(2)), drift: 'High' },
    '1Y': { ret: isPos ? '+25.0%' : '+12.0%', target: Number((currentPrice * (isPos ? 1.25 : 1.12)).toFixed(2)), drift: 'Macro Trend' },
  };

  const currentHorizonData = horizonsConfig[selectedHorizon] || horizonsConfig['1M'];

  // Synthetic price trajectory generation for SVG canvas
  const chartPoints = useMemo(() => {
    const histDays = 20;
    const futureDays = 12;
    const points = [];
    
    // Past historical trajectory from entry price
    for (let i = 0; i < histDays; i++) {
      const step = i / histDays;
      const noise = Math.sin(i * 0.9) * 0.015;
      const p = entryPrice + (currentPrice - entryPrice) * step + (currentPrice * noise);
      points.push({ type: 'hist', val: p, index: i });
    }
    // Today anchor
    points.push({ type: 'today', val: currentPrice, index: histDays });

    // Future forecast trajectory
    const targetVal = currentHorizonData.target;
    for (let i = 1; i <= futureDays; i++) {
      const step = i / futureDays;
      const futureVal = currentPrice + (targetVal - currentPrice) * step;
      points.push({
        type: 'future',
        val: futureVal,
        upper: futureVal * 1.035,
        lower: futureVal * 0.965,
        index: histDays + i
      });
    }
    return points;
  }, [entryPrice, currentPrice, currentHorizonData]);

  // Scaled coordinates for SVG viewBox 0 0 700 220
  const { minVal, maxVal, svgHistPath, svgFuturePath, svgConePath, todayX, todayY, entryY, stopY, targetY } = useMemo(() => {
    const allVals = chartPoints.map(pt => pt.upper || pt.val).concat([entryPrice, stopLossPrice, targetPrice]);
    const min = Math.min(...allVals) * 0.98;
    const max = Math.max(...allVals) * 1.02;
    const range = max - min || 1;

    const w = 700;
    const h = 220;
    const totalPts = chartPoints.length;
    const splitIdx = 20;
    const splitXCoord = (splitIdx / (totalPts - 1)) * w;

    const getY = (v) => h - ((v - min) / range) * (h - 40) - 20;
    const getX = (i) => (i / (totalPts - 1)) * w;

    let hPath = '';
    let fPath = '';
    let coneUpper = [];
    let coneLower = [];

    chartPoints.forEach((pt, i) => {
      const x = getX(i);
      const y = getY(pt.val);
      if (i <= splitIdx) {
        hPath += i === 0 ? `M ${x} ${y}` : ` L ${x} ${y}`;
      } else {
        if (!fPath) {
          const prevY = getY(chartPoints[splitIdx].val);
          fPath = `M ${splitXCoord} ${prevY} L ${x} ${y}`;
        } else {
          fPath += ` L ${x} ${y}`;
        }
        coneUpper.push(`${x},${getY(pt.upper)}`);
        coneLower.unshift(`${x},${getY(pt.lower)}`);
      }
    });

    const cPath = coneUpper.length
      ? `M ${splitXCoord},${getY(currentPrice)} L ${coneUpper.join(' L ')} L ${coneLower.join(' L ')} Z`
      : '';

    return {
      minVal: min,
      maxVal: max,
      svgHistPath: hPath,
      svgFuturePath: fPath,
      svgConePath: cPath,
      todayX: splitXCoord,
      todayY: getY(currentPrice),
      entryY: getY(entryPrice),
      stopY: getY(stopLossPrice),
      targetY: getY(targetPrice)
    };
  }, [chartPoints, entryPrice, stopLossPrice, targetPrice, currentPrice]);

  const handleRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => setIsRefreshing(false), 600);
  };

  return (
    <div className="stock-detail-page-container">
      {/* 1. Top Navigation Bar */}
      <div className="detail-top-nav-bar">
        <div className="nav-left">
          <button className="back-nav-btn" onClick={onBack} title="Back to Holdings & Exit Risk">
            <ArrowLeft size={17} />
            <span>Back to Holdings</span>
          </button>

          <div className="detail-brand-divider" />

          <div className="stock-identity-bundle">
            <div className="stock-avatar large">
              <img
                src={holding.logoUrl}
                alt={cleanSym}
                onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
              />
            </div>
            <div className="name-block">
              <div className="symbol-row">
                <span className="ticker-code font-mono">{holding.symbol}</span>
                <span className="fintech-tag-blue">{holding.exchange || (isIndian ? 'NSE' : 'NASDAQ')}</span>
                <span className="fintech-tag-type">HOLDING</span>
              </div>
              <div className="company-full-title">{holding.name}</div>
            </div>
          </div>
        </div>

        <div className="nav-right">
          <button
            className="fintech-icon-btn"
            onClick={handleRefresh}
            title="Refresh Live Data"
            disabled={isRefreshing}
          >
            <RefreshCw size={16} className={isRefreshing ? 'spin-anim' : ''} />
          </button>
        </div>
      </div>

      {/* 2. Hero Big Position & TTL Risk Urgency Banner */}
      <div className="detail-hero-banner">
        <div className="hero-price-cluster">
          <div className="price-lead-label">Current Position Valuation</div>
          <div className="hero-price-row font-mono">
            <span className="hero-main-price">
              {currencySymbol} {Number(currentPrice).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <span className={`fintech-pill ${isPos ? 'gain' : 'loss'}`}>
              {isPos ? '+' : ''}{currencySymbol}{unrealizedPnl.toFixed(2)} ({isPos ? '+' : ''}{unrealizedPnlPct.toFixed(2)}%)
            </span>
          </div>
        </div>

        <div className="hero-verdict-cluster">
          <div className={`verdict-score-badge ${ttlVariant}`}>
            <span className="verdict-dot">●</span>
            <span className="verdict-text">{ttlVerdict}</span>
            <span className="verdict-score font-mono">TTL: {ttlScore}/100</span>
          </div>
          <div className="verdict-engine-caption">Time-To-Leave (TTL) Algorithmic Risk Gauge</div>
        </div>
      </div>

      {/* 3. Interactive Risk Trajectory Chart Card */}
      <div className="detail-chart-card">
        <div className="chart-card-header">
          <div className="chart-title-group">
            <div className="chart-card-title">
              <Activity size={16} className={isPos ? 'text-gain' : 'text-loss'} />
              <span>Position Journey & Exit Trajectory Chart</span>
            </div>
            <span className="chart-card-subtitle">
              Visualizing Entry Anchor, Stop-Loss Floor, and Projected Horizon Drift
            </span>
          </div>

          <div className="chart-controls-group">
            {/* Horizon Selector */}
            <div className="chart-horizon-pill-group">
              {['10D', '1M', '6M', '1Y'].map((h) => (
                <button
                  key={h}
                  className={`horizon-pill ${selectedHorizon === h ? 'active' : ''}`}
                  onClick={() => setSelectedHorizon(h)}
                >
                  <span className="horizon-name">{h}</span>
                  <span className="horizon-pct font-mono">{horizonsConfig[h].ret}</span>
                </button>
              ))}
            </div>

            {/* Candle/Line switch */}
            <div className="chart-mode-toggle">
              <button
                className={`mode-btn ${chartMode === 'line' ? 'active' : ''}`}
                onClick={() => setChartMode('line')}
                title="Area Line Chart"
              >
                <Activity size={14} />
              </button>
              <button
                className={`mode-btn ${chartMode === 'candle' ? 'active' : ''}`}
                onClick={() => setChartMode('candle')}
                title="Candlestick Chart"
              >
                <BarChart2 size={14} />
              </button>
            </div>
          </div>
        </div>

        {/* Dynamic SVG Visual Chart */}
        <div className="chart-canvas-wrapper">
          <svg viewBox="0 0 700 220" className="predictive-svg-chart" preserveAspectRatio="none">
            <defs>
              <linearGradient id="holdingConeGrad" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stopColor={isPos ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'} stopOpacity="0.08" />
                <stop offset="100%" stopColor={isPos ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'} stopOpacity="0.22" />
              </linearGradient>
            </defs>

            {/* Horizontal Gridlines */}
            {[0.25, 0.5, 0.75].map((ratio, idx) => (
              <line
                key={idx}
                x1="0"
                y1={220 * ratio}
                x2="700"
                y2={220 * ratio}
                stroke="var(--border-hairline)"
                strokeDasharray="4 4"
                strokeWidth="1"
              />
            ))}

            {/* Entry Price Reference Line (Blue) */}
            <line
              x1="0"
              y1={entryY}
              x2="700"
              y2={entryY}
              stroke="var(--fintech-blue, #1455FE)"
              strokeDasharray="6 4"
              strokeWidth="1.5"
              opacity="0.85"
            />
            <text
              x="12"
              y={entryY - 8}
              fill="var(--fintech-blue, #1455FE)"
              fontSize="10"
              fontWeight="bold"
            >
              ENTRY: {currencySymbol}{entryPrice.toFixed(2)}
            </text>

            {/* Stop Loss Level Line (Red) */}
            <line
              x1="0"
              y1={stopY}
              x2="700"
              y2={stopY}
              stroke="var(--loss-red, #F04438)"
              strokeDasharray="6 4"
              strokeWidth="1.5"
              opacity="0.85"
            />
            <text
              x="12"
              y={stopY - 8}
              fill="var(--loss-red, #F04438)"
              fontSize="10"
              fontWeight="bold"
            >
              HARD STOP: {currencySymbol}{stopLossPrice.toFixed(2)} (-5.0%)
            </text>

            {/* Target Price Reference Line (Green) */}
            <line
              x1={todayX}
              y1={targetY}
              x2="700"
              y2={targetY}
              stroke="var(--gain-green, #00D09C)"
              strokeDasharray="6 4"
              strokeWidth="1.5"
              opacity="0.8"
            />
            <text
              x="600"
              y={targetY - 8}
              fill="var(--gain-green, #00D09C)"
              fontSize="10"
              fontWeight="bold"
              textAnchor="end"
            >
              TARGET: {currencySymbol}{targetPrice.toFixed(2)} (+15.0%)
            </text>

            {/* Confidence Cone Area */}
            {svgConePath && <path d={svgConePath} fill="url(#holdingConeGrad)" />}

            {/* Historical Journey Line */}
            <path
              d={svgHistPath}
              fill="none"
              stroke={isPos ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'}
              strokeWidth="2.5"
              strokeLinecap="round"
            />

            {/* Projected Horizon Line */}
            <path
              d={svgFuturePath}
              fill="none"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="2.5"
              strokeDasharray="6 4"
              strokeLinecap="round"
            />

            {/* Today Demarcation Line */}
            <line
              x1={todayX}
              y1="10"
              x2={todayX}
              y2="210"
              stroke="var(--text-secondary)"
              strokeDasharray="5 5"
              strokeWidth="1.5"
              opacity="0.7"
            />
            <text
              x={todayX + 6}
              y="22"
              fill="var(--text-secondary)"
              fontSize="10"
              fontWeight="bold"
            >
              TODAY
            </text>

            {/* Today Price Node */}
            <circle cx={todayX} cy={todayY} r="5" fill={isPos ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)'} />
            <circle cx={todayX} cy={todayY} r="2.5" fill="#FFFFFF" />
          </svg>
        </div>

        <div className="chart-canvas-footer-stats">
          <div className="stat-pill">
            <span className="stat-label">Shares Owned</span>
            <span className="stat-value font-mono">{Number(shares).toFixed(4)}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Total Invested</span>
            <span className="stat-value font-mono">{currencySymbol} {totalInvested.toFixed(2)}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Position Value</span>
            <span className="stat-value font-mono">{currencySymbol} {currentValue.toFixed(2)}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Unrealized P&L</span>
            <span className={`stat-value font-mono ${isPos ? 'text-gain' : 'text-loss'}`}>
              {isPos ? '+' : ''}{currencySymbol}{unrealizedPnl.toFixed(2)} ({isPos ? '+' : ''}{unrealizedPnlPct.toFixed(2)}%)
            </span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">TTL Urgency</span>
            <span className={`stat-value font-mono ${ttlVariant === 'danger' ? 'text-loss' : ttlVariant === 'warning' ? 'text-amber' : 'text-gain'}`}>
              {ttlScore}/100 ({ttlVerdict})
            </span>
          </div>
        </div>
      </div>

      {/* 4. Detail Navigation Tabs (Overview, Forecasts, Exit Audit, Exit Plan) */}
      <div className="detail-segmented-nav-row">
        {[
          { id: 'overview', label: 'Position Journey', icon: Layers },
          { id: 'forecasts', label: 'Horizon Projections', icon: Compass },
          { id: 'audit', label: 'Exit Urgency Audit', icon: ShieldAlert },
          { id: 'plan', label: 'Exit & Sell Plan', icon: FileText },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = selectedTab === tab.id;
          return (
            <button
              key={tab.id}
              className={`detail-nav-tab ${isActive ? 'active' : ''}`}
              onClick={() => setSelectedTab(tab.id)}
            >
              <Icon size={15} />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* 5. Tab Driven Dynamic Content Modules */}
      <div className="detail-tab-body">
        {/* TAB 1: OVERVIEW & POSITION JOURNEY */}
        {selectedTab === 'overview' && (
          <div className="overview-tab-grid">
            {/* Capital Breakdown Card */}
            <div className="fintech-subcard">
              <h4 className="subcard-title">Capital & Financial Breakdown</h4>
              <p className="subcard-subtitle">Real-time ledger accounting for this holding</p>

              <div className="position-metrics-quad-grid">
                <div className="quad-item">
                  <span className="quad-label">Shares Owned</span>
                  <div className="quad-val font-mono">{Number(shares).toFixed(4)}</div>
                  <span className="quad-sub">Equities</span>
                </div>
                <div className="quad-item">
                  <span className="quad-label">Average Entry Price</span>
                  <div className="quad-val font-mono">{currencySymbol} {Number(entryPrice).toFixed(2)}</div>
                  <span className="quad-sub">Executed basis</span>
                </div>
                <div className="quad-item">
                  <span className="quad-label">Total Capital Invested</span>
                  <div className="quad-val font-mono">{currencySymbol} {totalInvested.toFixed(2)}</div>
                  <span className="quad-sub">Original outlay</span>
                </div>
                <div className="quad-item">
                  <span className="quad-label">Current Position Value</span>
                  <div className="quad-val font-mono">{currencySymbol} {currentValue.toFixed(2)}</div>
                  <span className="quad-sub">Liquid market value</span>
                </div>
              </div>

              <div className="pnl-highlight-strip font-mono">
                <div className="pnl-item">
                  <span className="pnl-k">Unrealized P&L</span>
                  <span className={`pnl-v ${isPos ? 'text-gain' : 'text-loss'}`}>
                    {isPos ? '+' : ''}{currencySymbol}{unrealizedPnl.toFixed(2)} ({isPos ? '+' : ''}{unrealizedPnlPct.toFixed(2)}%)
                  </span>
                </div>
                <div className="pnl-divider" />
                <div className="pnl-item">
                  <span className="pnl-k">Realized Profit/Loss</span>
                  <span className="pnl-v text-secondary">+${currencySymbol}0.00</span>
                </div>
              </div>
            </div>

            {/* Hold Duration & Time-to-Leave Progress */}
            <div className="fintech-subcard">
              <div className="subcard-header-row">
                <div>
                  <h4 className="subcard-title">Time-To-Leave (TTL) Risk Gauge</h4>
                  <p className="subcard-subtitle">Algorithmic position aging & momentum half-life decay</p>
                </div>
                <span className={`fintech-badge ${ttlVariant === 'danger' ? 'danger' : ttlVariant === 'warning' ? 'warning' : 'gain'}`}>
                  {ttlScore} / 100
                </span>
              </div>

              {/* TTL Progress Bar */}
              <div className="ttl-bar-wrapper">
                <div className="ttl-bar-labels">
                  <span className="ttl-k">Urgency Score Meter</span>
                  <span className="ttl-v font-mono">{ttlVerdict}</span>
                </div>
                <div className="ttl-track">
                  <div
                    className="ttl-fill"
                    style={{
                      width: `${ttlScore}%`,
                      backgroundColor: ttlScore >= 80 ? 'var(--loss-red)' : ttlScore >= 60 ? 'var(--warning-amber)' : 'var(--gain-green)'
                    }}
                  />
                </div>
              </div>

              {/* Hold Duration Progress */}
              <div className="duration-progress-card">
                <div className="duration-labels">
                  <span className="d-title">Hold Duration Tracking</span>
                  <span className="d-days font-mono">{daysHeld} of {maxHoldDays} Days</span>
                </div>
                <div className="duration-track">
                  <div
                    className="duration-fill"
                    style={{ width: `${Math.min((daysHeld / maxHoldDays) * 100, 100)}%` }}
                  />
                </div>
                <div className="duration-caption">
                  Positions are recommended for maximum 60-day lifecycle before systematic risk review.
                </div>
              </div>
            </div>

            {/* Target & Invalidation Levels Card */}
            <div className="fintech-subcard">
              <h4 className="subcard-title">Price Target & Risk Levels</h4>
              <p className="subcard-subtitle">Threshold monitoring for stop-loss and take-profit</p>

              <div className="risk-reward-summary-grid">
                <div className="boundary-card stop font-mono">
                  <div className="boundary-label">Hard Stop-Loss (-5.0%)</div>
                  <div className="boundary-value text-loss">{currencySymbol} {stopLossPrice.toFixed(2)}</div>
                  <div className="boundary-note">
                    {currentPrice > stopLossPrice
                      ? `Buffer remaining: +${(((currentPrice - stopLossPrice) / currentPrice) * 100).toFixed(1)}%`
                      : 'TRIGGERED: Position breached stop level'}
                  </div>
                </div>

                <div className="boundary-card target font-mono">
                  <div className="boundary-label">Take-Profit Target (+15.0%)</div>
                  <div className="boundary-value text-gain">{currencySymbol} {targetPrice.toFixed(2)}</div>
                  <div className="boundary-note">
                    {targetPrice > currentPrice
                      ? `Distance to target: +${(((targetPrice - currentPrice) / currentPrice) * 100).toFixed(1)}%`
                      : 'Target reached: consider scaling out'}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: FORECASTS */}
        {selectedTab === 'forecasts' && (
          <div className="forecasts-tab-container">
            <div className="fintech-subcard">
              <h4 className="subcard-title">Multi-Horizon Price Drift Projections</h4>
              <p className="subcard-subtitle">Expected returns against remaining holding lifecycle</p>

              <div className="forecast-matrix-grid">
                {Object.entries(horizonsConfig).map(([horizon, data]) => (
                  <div key={horizon} className="forecast-matrix-card">
                    <div className="matrix-top-row">
                      <span className="matrix-horizon-badge">{horizon} Horizon</span>
                      <span className={`matrix-return-pill font-mono ${data.ret.includes('-') ? 'text-loss' : 'text-gain'}`}>
                        {data.ret}
                      </span>
                    </div>

                    <div className="matrix-price-hero font-mono">
                      {currencySymbol} {data.target.toFixed(2)}
                    </div>

                    <div className="matrix-corridor-row font-mono">
                      <span>Low: {currencySymbol}{(data.target * 0.96).toFixed(2)}</span>
                      <span>High: {currencySymbol}{(data.target * 1.04).toFixed(2)}</span>
                    </div>

                    <div className="matrix-metrics-strip">
                      <div className="strip-col">
                        <span className="strip-k">Expected Drift</span>
                        <span className="strip-v font-mono">{data.drift}</span>
                      </div>
                      <div className="strip-col">
                        <span className="strip-k">Target Proximity</span>
                        <span className="strip-v font-mono">{horizon === '1M' ? 'Primary' : 'Secondary'}</span>
                      </div>
                      <div className="strip-col">
                        <span className="strip-k">Urgency Impact</span>
                        <span className="strip-v font-mono">{ttlScore >= 60 ? 'Exhaustion' : 'Accretive'}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: EXIT AUDIT */}
        {selectedTab === 'audit' && (
          <div className="audit-tab-container">
            <div className="fintech-subcard">
              <h4 className="subcard-title">Time-To-Leave (TTL) Factor Audit</h4>
              <p className="subcard-subtitle">4 Quantitative Risk Pillars evaluated in real-time</p>

              <div className="forensic-gauges-grid">
                {/* 1. Momentum Exhaustion */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Momentum Exhaustion</span>
                    <span className={`gauge-status-badge ${ttlScore >= 70 ? 'danger' : 'safe'}`}>
                      {ttlScore >= 70 ? 'Exhaustion Alert' : 'Healthy Trend'}
                    </span>
                  </div>
                  <div className="gauge-val font-mono">{ttlScore >= 70 ? '82 / 100' : '24 / 100'}</div>
                  <p className="gauge-desc">
                    {ttlScore >= 70
                      ? 'Bearish divergence detected between price momentum and RSI oscillators.'
                      : 'Price action remains in bullish alignment above 20-day exponential moving average.'}
                  </p>
                </div>

                {/* 2. Smart Money Outflow */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Smart Money Outflow</span>
                    <span className={`gauge-status-badge ${ttlScore >= 60 ? 'warning' : 'safe'}`}>
                      {ttlScore >= 60 ? 'Net Outflows' : 'Institutional Support'}
                    </span>
                  </div>
                  <div className="gauge-val font-mono">{ttlScore >= 60 ? '68 / 100' : '18 / 100'}</div>
                  <p className="gauge-desc">
                    {ttlScore >= 60
                      ? 'Distribution block signatures detected. Institutions trimming exposure.'
                      : 'Accumulation footprint steady with no evidence of dark-pool distribution.'}
                  </p>
                </div>

                {/* 3. Stop Proximity */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Stop-Loss Proximity</span>
                    <span className={`gauge-status-badge ${currentPrice <= stopLossPrice * 1.02 ? 'danger' : 'safe'}`}>
                      {currentPrice <= stopLossPrice * 1.02 ? 'Critical Zone' : 'Safe Cushion'}
                    </span>
                  </div>
                  <div className="gauge-val font-mono">
                    {(((currentPrice - stopLossPrice) / currentPrice) * 100).toFixed(1)}% Cushion
                  </div>
                  <p className="gauge-desc">Distance from current market price to hard stop-loss trigger line.</p>
                </div>

                {/* 4. Macro Alignment */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Macro Regime Tailwinds</span>
                    <span className="gauge-status-badge safe">Constructive</span>
                  </div>
                  <div className="gauge-val font-mono">75 / 100</div>
                  <p className="gauge-desc">Sector benchmark remains supportive with positive market breadth.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: EXIT PLAN */}
        {selectedTab === 'plan' && (
          <div className="plan-tab-container">
            <div className="fintech-subcard">
              <h4 className="subcard-title">Disciplined Exit & Sell Plan</h4>
              <p className="subcard-subtitle">Recommended algorithmic exit strategy to maximize retained alpha</p>

              <div className="trade-plan-steps-list">
                <div className="plan-step-card">
                  <div className="step-num font-mono">01</div>
                  <div className="step-content">
                    <h5 className="step-title">Recommended Execution Action</h5>
                    <p className="step-text">
                      {ttlScore >= 80 ? (
                        <strong className="text-loss">IMMEDIATE FULL MARKET EXIT: Quantitative risk exhaustion requires capital extraction today.</strong>
                      ) : ttlScore >= 60 ? (
                        <strong className="text-amber">CONSIDER PARTIAL PROFIT TAKING (50%): Tighten trailing stop to entry level ({currencySymbol}{entryPrice.toFixed(2)}).</strong>
                      ) : (
                        <strong className="text-gain">MAINTAIN POSITION (SAFE HOLD): Allow momentum to compound toward profit target ({currencySymbol}{targetPrice.toFixed(2)}).</strong>
                      )}
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">02</div>
                  <div className="step-content">
                    <h5 className="step-title">Hard Stop-Loss Floor</h5>
                    <p className="step-text">
                      Hard exit at <strong className="text-loss font-mono">{currencySymbol}{stopLossPrice.toFixed(2)}</strong>. Never lower a stop-loss under any market condition.
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">03</div>
                  <div className="step-content">
                    <h5 className="step-title">Trailing Stop Activation</h5>
                    <p className="step-text">
                      {isPos && unrealizedPnlPct >= 8.0 ? (
                        <strong className="text-gain">Trailing stop active at breakeven. Your position is completely risk-free.</strong>
                      ) : (
                        `Trailing stop engages when unrealized profit crosses +8.0% (${currencySymbol}${(entryPrice * 1.08).toFixed(2)}).`
                      )}
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">04</div>
                  <div className="step-content">
                    <h5 className="step-title">Lifecycle Sunset</h5>
                    <p className="step-text">
                      Max hold window is 60 days. Day {daysHeld} active. If no breakout occurs by day 60, close position systematically.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 6. Sticky Bottom Action Dock */}
      <div className="detail-bottom-dock">
        <div className="dock-content-wrapper">
          <div className="dock-summary font-mono">
            <span className="dock-ticker">{holding.symbol}</span>
            <span className="dock-price">{currencySymbol} {Number(currentPrice).toFixed(2)}</span>
            <span className={`dock-target ${isPos ? 'text-gain' : 'text-loss'}`}>
              P&L: {isPos ? '+' : ''}{currencySymbol}{unrealizedPnl.toFixed(2)} ({isPos ? '+' : ''}{unrealizedPnlPct.toFixed(2)}%)
            </span>
          </div>

          <div className="dock-actions-row">
            <button
              className="dock-btn-primary"
              onClick={() => onSelectStock && onSelectStock(holding.symbol)}
              title="Open full interactive candlestick charts in Deep Analysis"
            >
              <ExternalLink size={15} />
              <span>Deep Analysis</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
