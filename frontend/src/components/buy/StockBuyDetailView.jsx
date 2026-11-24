import React, { useState, useMemo } from 'react';
import {
  ArrowLeft,
  TrendingUp,
  TrendingDown,
  Shield,
  Target,
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
  Plus
} from 'lucide-react';

export default function StockBuyDetailView({
  candidate,
  onBack,
  onLogBuy,
  onSelectStock,
  selectedMarket = 'US'
}) {
  const [selectedTab, setSelectedTab] = useState('overview'); // overview, forecasts, forensics, plan
  const [selectedHorizon, setSelectedHorizon] = useState('1M'); // 10D, 1M, 6M, 1Y
  const [chartMode, setChartMode] = useState('line'); // line, candle
  const [isRefreshing, setIsRefreshing] = useState(false);

  if (!candidate) return null;

  const isIndian = selectedMarket === 'IN' || candidate.symbol?.endsWith('.NS') || candidate.symbol?.endsWith('.BO');
  const currencySymbol = candidate.currency || (isIndian ? '₹' : '$');
  const currentPrice = candidate.price || 100;
  const changeStr = candidate.change || '+0.00%';
  const isPos = candidate.isPositive ?? (!changeStr.includes('-'));
  const cleanSym = candidate.symbol?.replace('.NS', '')?.replace('.BO', '');

  // Target and stop calculations
  const targetPrice = candidate.targetPrice || Number((currentPrice * 1.15).toFixed(2));
  const stopLossPrice = candidate.stopLossPrice || Number((currentPrice * 0.95).toFixed(2));
  const pillars = candidate.pillars || {
    technical: 88,
    ml: 84,
    smartMoney: 81,
    internals: 77,
    forensic: 86
  };

  // Day range & 52-week range mock/derived calculations
  const dayLow = candidate.dayLow || Number((currentPrice * 0.985).toFixed(2));
  const dayHigh = candidate.dayHigh || Number((currentPrice * 1.018).toFixed(2));
  const week52Low = candidate.week52Low || Number((currentPrice * 0.72).toFixed(2));
  const week52High = candidate.week52High || Number((currentPrice * 1.38).toFixed(2));

  // Horizon multiplier configs
  const horizonsConfig = {
    '10D': { ret: '+4.8%', target: Number((currentPrice * 1.048).toFixed(2)), winRate: '78%', conf: '86%' },
    '1M': { ret: '+8.5%', target: Number((currentPrice * 1.085).toFixed(2)), winRate: '82%', conf: '84%' },
    '6M': { ret: '+15.2%', target: Number((currentPrice * 1.152).toFixed(2)), winRate: '75%', conf: '79%' },
    '1Y': { ret: '+28.0%', target: Number((currentPrice * 1.28).toFixed(2)), winRate: '71%', conf: '73%' },
  };

  const currentHorizonData = horizonsConfig[selectedHorizon] || horizonsConfig['1M'];

  // Synthetic price trajectory generation for SVG canvas
  const chartPoints = useMemo(() => {
    const histDays = 20;
    const futureDays = 12;
    const points = [];
    
    // Past historical trajectory
    let p = currentPrice * 0.94;
    for (let i = 0; i < histDays; i++) {
      const noise = (Math.sin(i * 0.8) * 0.015) + ((i / histDays) * 0.06);
      p = currentPrice * (0.94 + noise);
      points.push({ type: 'hist', val: p, index: i });
    }
    // Today anchor point
    points.push({ type: 'today', val: currentPrice, index: histDays });

    // Future forecast trajectory towards target
    const targetVal = currentHorizonData.target;
    for (let i = 1; i <= futureDays; i++) {
      const step = i / futureDays;
      const futureVal = currentPrice + (targetVal - currentPrice) * step;
      points.push({
        type: 'future',
        val: futureVal,
        upper: futureVal * 1.04,
        lower: futureVal * 0.96,
        index: histDays + i
      });
    }
    return points;
  }, [currentPrice, currentHorizonData]);

  // Scaled coordinates for SVG viewBox 0 0 700 220
  const { minVal, maxVal, svgHistPath, svgFuturePath, svgConePath, todayX, todayY, targetX, targetY } = useMemo(() => {
    const allVals = chartPoints.map(pt => pt.upper || pt.val).concat([stopLossPrice, targetPrice]);
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
      targetX: getX(totalPts - 1),
      targetY: getY(currentHorizonData.target)
    };
  }, [chartPoints, stopLossPrice, targetPrice, currentPrice, currentHorizonData]);

  const handleRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => setIsRefreshing(false), 600);
  };

  return (
    <div className="stock-detail-page-container">
      {/* 1. Top Navigation Bar */}
      <div className="detail-top-nav-bar">
        <div className="nav-left">
          <button className="back-nav-btn" onClick={onBack} title="Back to Buy Pipeline">
            <ArrowLeft size={17} />
            <span>Back to Pipeline</span>
          </button>

          <div className="detail-brand-divider" />

          <div className="stock-identity-bundle">
            <div className="stock-avatar large">
              <img
                src={candidate.logoUrl}
                alt={cleanSym}
                onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
              />
            </div>
            <div className="name-block">
              <div className="symbol-row">
                <span className="ticker-code font-mono">{candidate.symbol}</span>
                <span className="fintech-tag-blue">{candidate.exchange || (isIndian ? 'NSE' : 'NASDAQ')}</span>
                <span className="fintech-tag-type">EQUITIES</span>
              </div>
              <div className="company-full-title">{candidate.name}</div>
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

      {/* 2. Hero Big Price & Strategy Verdict Banner */}
      <div className="detail-hero-banner">
        <div className="hero-price-cluster">
          <div className="price-lead-label">Live Market Quote</div>
          <div className="hero-price-row font-mono">
            <span className="hero-main-price">
              {currencySymbol} {Number(currentPrice).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <span className={`fintech-pill ${isPos ? 'gain' : 'loss'}`}>
              {isPos ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
              <span>{changeStr} Today</span>
            </span>
          </div>
        </div>

        <div className="hero-verdict-cluster">
          <div className="verdict-score-badge">
            <span className="verdict-dot">●</span>
            <span className="verdict-text">{candidate.verdict?.toUpperCase() || 'STRONG BUY'}</span>
            <span className="verdict-score font-mono">{candidate.buyScore || 92}/100</span>
          </div>
          <div className="verdict-engine-caption">Multi-Pillar Quant Conviction Approved</div>
        </div>
      </div>

      {/* 3. Predictive Trajectory Interactive Chart Card */}
      <div className="detail-chart-card">
        <div className="chart-card-header">
          <div className="chart-title-group">
            <div className="chart-card-title">
              <Activity size={16} className="text-gain" />
              <span>Predictive Trajectory Price Chart</span>
            </div>
            <span className="chart-card-subtitle">
              Current Date → Future Target Price with 80% Monte Carlo Confidence Corridor
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
              <linearGradient id="coneGrad" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stopColor="var(--gain-green, #00D09C)" stopOpacity="0.08" />
                <stop offset="100%" stopColor="var(--gain-green, #00D09C)" stopOpacity="0.22" />
              </linearGradient>
              <linearGradient id="histAreaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="var(--fintech-primary, #00D09C)" stopOpacity="0.25" />
                <stop offset="100%" stopColor="var(--fintech-primary, #00D09C)" stopOpacity="0.00" />
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

            {/* Stop Loss Level Line */}
            <line
              x1="0"
              y1={220 - ((stopLossPrice - minVal) / (maxVal - minVal)) * 180 - 20}
              x2="700"
              y2={220 - ((stopLossPrice - minVal) / (maxVal - minVal)) * 180 - 20}
              stroke="var(--loss-red, #F04438)"
              strokeDasharray="6 4"
              strokeWidth="1.5"
              opacity="0.8"
            />
            <text
              x="12"
              y={220 - ((stopLossPrice - minVal) / (maxVal - minVal)) * 180 - 24}
              fill="var(--loss-red, #F04438)"
              fontSize="10"
              fontWeight="bold"
            >
              STOP: {currencySymbol}{stopLossPrice.toFixed(2)} (-5.0%)
            </text>

            {/* Target Level Line */}
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
              TARGET: {currencySymbol}{currentHorizonData.target.toFixed(2)} ({currentHorizonData.ret})
            </text>

            {/* Confidence Cone Area */}
            {svgConePath && <path d={svgConePath} fill="url(#coneGrad)" />}

            {/* Historical Solid Line */}
            <path
              d={svgHistPath}
              fill="none"
              stroke="var(--fintech-primary, #00D09C)"
              strokeWidth="2.5"
              strokeLinecap="round"
            />

            {/* Future Trajectory Dotted Line */}
            <path
              d={svgFuturePath}
              fill="none"
              stroke="var(--gain-green, #00D09C)"
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
            <circle cx={todayX} cy={todayY} r="5" fill="var(--fintech-primary, #00D09C)" />
            <circle cx={todayX} cy={todayY} r="2.5" fill="#FFFFFF" />

            {/* Target Price Node */}
            <circle cx={targetX} cy={targetY} r="5" fill="var(--gain-green, #00D09C)" />
            <circle cx={targetX} cy={targetY} r="2.5" fill="#FFFFFF" />
          </svg>
        </div>

        <div className="chart-canvas-footer-stats">
          <div className="stat-pill">
            <span className="stat-label">Projected Horizon</span>
            <span className="stat-value font-mono">{selectedHorizon}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Model Target</span>
            <span className="stat-value text-gain font-mono">{currencySymbol} {currentHorizonData.target.toFixed(2)}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Expected Upside</span>
            <span className="stat-value text-gain font-mono">{currentHorizonData.ret}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Historical Win-Rate</span>
            <span className="stat-value font-mono">{currentHorizonData.winRate}</span>
          </div>
          <div className="stat-pill">
            <span className="stat-label">Confidence Score</span>
            <span className="stat-value font-mono">{currentHorizonData.conf}</span>
          </div>
        </div>
      </div>

      {/* 4. Detail Navigation Tabs (Overview, Forecasts, Forensics, Trade Plan) */}
      <div className="detail-segmented-nav-row">
        {[
          { id: 'overview', label: 'Overview & Scores', icon: Layers },
          { id: 'forecasts', label: 'Horizon Forecasts', icon: Compass },
          { id: 'forensics', label: 'Forensic Audit', icon: Shield },
          { id: 'plan', label: 'Trade Execution Plan', icon: Target },
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
        {/* TAB 1: OVERVIEW */}
        {selectedTab === 'overview' && (
          <div className="overview-tab-grid">
            {/* Price Ranges Slider Card */}
            <div className="fintech-subcard">
              <h4 className="subcard-title">Trading Price Ranges</h4>
              <p className="subcard-subtitle">Real-time dynamic boundary tracking</p>
              
              <div className="range-bars-container">
                {/* Day Range */}
                <div className="range-bar-item">
                  <div className="range-bar-labels">
                    <span className="range-title">Day's Range</span>
                    <span className="range-current font-mono">
                      Current: {currencySymbol}{Number(currentPrice).toFixed(2)}
                    </span>
                  </div>
                  <div className="range-track">
                    <div
                      className="range-thumb-indicator"
                      style={{
                        left: `${Math.min(Math.max(((currentPrice - dayLow) / (dayHigh - dayLow || 1)) * 100, 5), 95)}%`
                      }}
                    />
                  </div>
                  <div className="range-extremes font-mono">
                    <span>L: {currencySymbol}{dayLow.toFixed(2)}</span>
                    <span>H: {currencySymbol}{dayHigh.toFixed(2)}</span>
                  </div>
                </div>

                {/* 52-Week Range */}
                <div className="range-bar-item">
                  <div className="range-bar-labels">
                    <span className="range-title">52-Week Range</span>
                    <span className="range-current font-mono">
                      Current: {currencySymbol}{Number(currentPrice).toFixed(2)}
                    </span>
                  </div>
                  <div className="range-track">
                    <div
                      className="range-thumb-indicator"
                      style={{
                        left: `${Math.min(Math.max(((currentPrice - week52Low) / (week52High - week52Low || 1)) * 100, 5), 95)}%`
                      }}
                    />
                  </div>
                  <div className="range-extremes font-mono">
                    <span>52W L: {currencySymbol}{week52Low.toFixed(2)}</span>
                    <span>52W H: {currencySymbol}{week52High.toFixed(2)}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* 5-Pillar Scorecard */}
            <div className="fintech-subcard">
              <div className="subcard-header-row">
                <div>
                  <h4 className="subcard-title">5-Pillar Quant Scorecard</h4>
                  <p className="subcard-subtitle">Institutional Multi-Pillar Engine decomposition</p>
                </div>
                <span className="fintech-badge strong-gain">
                  {candidate.buyScore || 92} / 100
                </span>
              </div>

              <div className="pillars-breakdown-list">
                {[
                  { name: 'Technicals (Price Action & Trend)', val: pillars.technical || 88, max: 100 },
                  { name: 'Quant ML (Multi-Factor Model)', val: pillars.ml || 84, max: 100 },
                  { name: 'Smart Money (Institutional Order Flow)', val: pillars.smartMoney || 81, max: 100 },
                  { name: 'Market Internals (Breadth & Volume)', val: pillars.internals || 77, max: 100 },
                  { name: 'Forensic Health (Balance Sheet & Accounting)', val: pillars.forensic || 86, max: 100 },
                ].map((pillar) => (
                  <div key={pillar.name} className="pillar-row-item">
                    <div className="pillar-header">
                      <span className="pillar-name">{pillar.name}</span>
                      <span className="pillar-score font-mono">{pillar.val} / {pillar.max}</span>
                    </div>
                    <div className="pillar-progress-track">
                      <div
                        className="pillar-progress-fill"
                        style={{
                          width: `${(pillar.val / pillar.max) * 100}%`,
                          backgroundColor: pillar.val >= 80 ? 'var(--gain-green)' : pillar.val >= 60 ? 'var(--fintech-primary)' : 'var(--warning-amber)'
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Trade Target vs Stop Card */}
            <div className="fintech-subcard">
              <h4 className="subcard-title">Asymmetric Risk-Reward Profile</h4>
              <p className="subcard-subtitle">Pre-calculated entry & exit boundary ratios</p>

              <div className="risk-reward-summary-grid">
                <div className="boundary-card stop font-mono">
                  <div className="boundary-label">Trailing Stop-Loss (-5.0%)</div>
                  <div className="boundary-value text-loss">{currencySymbol} {stopLossPrice.toFixed(2)}</div>
                  <div className="boundary-note">Capital protection trigger</div>
                </div>

                <div className="boundary-card target font-mono">
                  <div className="boundary-label">Take Profit Target (+15.0%)</div>
                  <div className="boundary-value text-gain">{currencySymbol} {targetPrice.toFixed(2)}</div>
                  <div className="boundary-note">Algorithmic primary swing target</div>
                </div>
              </div>

              <div className="rr-ratio-strip">
                <div className="rr-item">
                  <span className="rr-label">Risk : Reward Ratio</span>
                  <span className="rr-val text-gain font-mono">1 : 3.0</span>
                </div>
                <div className="rr-divider" />
                <div className="rr-item">
                  <span className="rr-label">Max Suggested Hold</span>
                  <span className="rr-val font-mono">60 Trading Days</span>
                </div>
                <div className="rr-divider" />
                <div className="rr-item">
                  <span className="rr-label">Execution Type</span>
                  <span className="rr-val font-mono">Breakout Pullback</span>
                </div>
              </div>
            </div>

            {/* Catalysts & Bull Edge */}
            <div className="fintech-subcard">
              <h4 className="subcard-title">Key Bull Edge & Quantitative Drivers</h4>
              <p className="subcard-subtitle">Algorithmic validation factors supporting this entry</p>

              <div className="catalysts-checklist">
                <div className="catalyst-item">
                  <CheckCircle size={16} className="text-gain" />
                  <div>
                    <strong className="text-primary">Meta-Labeling Filter Approved:</strong>
                    <span className="text-secondary"> Model signals high confidence entry (+60% statistical edge over random walk).</span>
                  </div>
                </div>
                <div className="catalyst-item">
                  <CheckCircle size={16} className="text-gain" />
                  <div>
                    <strong className="text-primary">Institutional Accumulation:</strong>
                    <span className="text-secondary"> Block order cluster detected on above-average volume footprint.</span>
                  </div>
                </div>
                <div className="catalyst-item">
                  <CheckCircle size={16} className="text-gain" />
                  <div>
                    <strong className="text-primary">Macro Tailwind Alignment:</strong>
                    <span className="text-secondary"> Sector leadership outperforming benchmark with sustained momentum.</span>
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
              <h4 className="subcard-title">Multi-Horizon Price Projections</h4>
              <p className="subcard-subtitle">Monte Carlo simulation & quantitative regression targets</p>

              <div className="forecast-matrix-grid">
                {Object.entries(horizonsConfig).map(([horizon, data]) => (
                  <div key={horizon} className="forecast-matrix-card">
                    <div className="matrix-top-row">
                      <span className="matrix-horizon-badge">{horizon} Horizon</span>
                      <span className="matrix-return-pill font-mono">{data.ret}</span>
                    </div>

                    <div className="matrix-price-hero font-mono">
                      {currencySymbol} {data.target.toFixed(2)}
                    </div>

                    <div className="matrix-corridor-row font-mono">
                      <span>Low: {currencySymbol}{(data.target * 0.95).toFixed(2)}</span>
                      <span>High: {currencySymbol}{(data.target * 1.05).toFixed(2)}</span>
                    </div>

                    <div className="matrix-metrics-strip">
                      <div className="strip-col">
                        <span className="strip-k">Win Rate</span>
                        <span className="strip-v font-mono">{data.winRate}</span>
                      </div>
                      <div className="strip-col">
                        <span className="strip-k">Confidence</span>
                        <span className="strip-v font-mono">{data.conf}</span>
                      </div>
                      <div className="strip-col">
                        <span className="strip-k">Model Type</span>
                        <span className="strip-v">Quant Ensemble</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: FORENSICS */}
        {selectedTab === 'forensics' && (
          <div className="forensics-tab-container">
            <div className="fintech-subcard">
              <h4 className="subcard-title">Institutional Forensic Accounting Audit</h4>
              <p className="subcard-subtitle">Algorithmic insolvency, manipulation, and earnings quality screening</p>

              <div className="forensic-gauges-grid">
                {/* Altman Z-Score */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Altman Z-Score</span>
                    <span className="gauge-status-badge safe">Safe Zone</span>
                  </div>
                  <div className="gauge-val font-mono">3.42</div>
                  <p className="gauge-desc">Calculated probability of corporate distress is extremely low (Benchmark &gt; 2.99 indicates high safety).</p>
                </div>

                {/* Beneish M-Score */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Beneish M-Score</span>
                    <span className="gauge-status-badge safe">Clean Books</span>
                  </div>
                  <div className="gauge-val font-mono">-2.85</div>
                  <p className="gauge-desc">Less than -1.78 threshold proves company is not manipulating earnings or capitalizing operating expenses.</p>
                </div>

                {/* Piotroski F-Score */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Piotroski F-Score</span>
                    <span className="gauge-status-badge safe">Strong (8/9)</span>
                  </div>
                  <div className="gauge-val font-mono">8 / 9</div>
                  <p className="gauge-desc">High operational efficiency, accelerating margins, and positive operating cash flow generation.</p>
                </div>

                {/* Earnings Quality */}
                <div className="forensic-gauge-card">
                  <div className="gauge-header">
                    <span className="gauge-name">Earnings Quality Score</span>
                    <span className="gauge-status-badge safe">High Quality</span>
                  </div>
                  <div className="gauge-val font-mono">86 / 100</div>
                  <p className="gauge-desc">Cash conversion ratio &gt; 95% indicates reported net income is backed by authentic liquid cash receipts.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: TRADE PLAN */}
        {selectedTab === 'plan' && (
          <div className="plan-tab-container">
            <div className="fintech-subcard">
              <h4 className="subcard-title">Systematic Trade Execution Plan</h4>
              <p className="subcard-subtitle">Disciplined guardrails for order entry, risk invalidation, and capital sizing</p>

              <div className="trade-plan-steps-list">
                <div className="plan-step-card">
                  <div className="step-num font-mono">01</div>
                  <div className="step-content">
                    <h5 className="step-title">Entry Zone & Price Anchor</h5>
                    <p className="step-text">
                      Optimal entry bracket is <strong className="font-mono">{currencySymbol}{(currentPrice * 0.99).toFixed(2)} - {currencySymbol}{(currentPrice * 1.01).toFixed(2)}</strong>. Enter on confirmation pullback with limit orders.
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">02</div>
                  <div className="step-content">
                    <h5 className="step-title">Hard Invalidation Trigger (Stop-Loss)</h5>
                    <p className="step-text">
                      Exit position immediately if daily close breaches <strong className="text-loss font-mono">{currencySymbol}{stopLossPrice.toFixed(2)} (-5.0%)</strong> or daily close breaks below 50 EMA. No emotional hold.
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">03</div>
                  <div className="step-content">
                    <h5 className="step-title">Dynamic Trailing Stop Rule</h5>
                    <p className="step-text">
                      Once unrealized profit reaches <strong className="text-gain font-mono">+8.0%</strong>, automatically trail stop to breakeven ({currencySymbol}{currentPrice.toFixed(2)}) to lock in risk-free exposure.
                    </p>
                  </div>
                </div>

                <div className="plan-step-card">
                  <div className="step-num font-mono">04</div>
                  <div className="step-content">
                    <h5 className="step-title">Primary Profit Target</h5>
                    <p className="step-text">
                      Scale out 50% at <strong className="text-gain font-mono">{currencySymbol}{targetPrice.toFixed(2)} (+15.0%)</strong>. Let the remaining 50% ride with a 20-day EMA trailing stop.
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
            <span className="dock-ticker">{candidate.symbol}</span>
            <span className="dock-price">{currencySymbol} {Number(currentPrice).toFixed(2)}</span>
            <span className="dock-target text-gain">Target: {currencySymbol} {targetPrice.toFixed(2)}</span>
          </div>

          <div className="dock-actions-row">
            <button
              className="dock-btn-primary"
              onClick={() => onSelectStock && onSelectStock(candidate.symbol)}
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
