import React, { useState, useRef, useEffect } from 'react';
import {
  Layers,
  CheckCircle2,
  AlertTriangle,
  Compass,
  ChevronDown,
  ShieldCheck,
  BookOpen
} from 'lucide-react';

const CHAPTER_SHORT_NAMES = [
  { id: 'market_liquidity', num: '01', name: 'Scale & Liquidity' },
  { id: 'financial_solvency', num: '02', name: 'Solvency Structure' },
  { id: 'revenue_growth', num: '03', name: 'Revenue Trajectory' },
  { id: 'profitability_margins', num: '04', name: 'Margin Quality' },
  { id: 'earnings_forensics', num: '05', name: 'Forensics & Accruals' },
  { id: 'cash_flow_engine', num: '06', name: 'Cash Flow Machine' },
  { id: 'debt_solvency', num: '07', name: 'Debt & Bankruptcy Risk' },
  { id: 'capital_efficiency', num: '08', name: 'ROIC & DuPont Analysis' },
  { id: 'business_quality', num: '09', name: 'Economic Moats & Quality' },
  { id: 'management_capital_allocation', num: '10', name: 'Management & Capital Allocation' },
  { id: 'industry_sector_dynamics', num: '11', name: 'Industry & Sector Dynamics' },
  { id: 'pe_valuation', num: '12', name: 'P/E Ratio & Valuation' },
  { id: 'ev_valuation', num: '13', name: 'P/S & EV Multiples' },
  { id: 'fcf_valuation', num: '14', name: 'Free Cash Flow Valuation' },
  { id: 'dcf_valuation', num: '15', name: 'DCF Intrinsic Valuation' },
  { id: 'relative_valuation', num: '16', name: 'Relative & Peer Valuation' },
  { id: 'factor_profile', num: '17', name: 'Quantitative Factor Profile' },
  { id: 'momentum_factors', num: '18', name: 'Momentum Factors & Trend' },
  { id: 'value_factors', num: '19', name: 'Value Factors & Cash Yield' },
  { id: 'quality_factors', num: '20', name: 'Quality Factors & Piotroski' },
  { id: 'growth_factors', num: '21', name: 'Growth Factors & SGR' },
  { id: 'multi_factor_synthesis', num: '22', name: 'Multi-Factor Synthesis' },
  { id: 'data_engineering_hygiene', num: '23', name: 'Dataset Architecture & PIT' },
  { id: 'distribution_analytics', num: '24', name: 'EDA & Fat-Tail Risk' },
  { id: 'correlation_signal', num: '25', name: 'Correlation & Information Coeff' },
  { id: 'stock_ranking', num: '26', name: 'Multi-Factor Ranking' },
  { id: 'pattern_discovery', num: '27', name: 'Pattern Discovery & Anomalies' },
  { id: 'backtest_architecture', num: '28', name: 'Backtest Engine Architecture' },
  { id: 'look_ahead_defense', num: '29', name: 'Look-Ahead Bias Defense' },
  { id: 'survivorship_defense', num: '30', name: 'Survivorship Bias Defense' },
  { id: 'overfitting_defense', num: '31', name: 'Overfitting & Curve-Fitting' },
  { id: 'backtest_performance', num: '32', name: 'Backtest Performance & Ratios' },
  { id: 'portfolio_diversification', num: '33', name: 'Portfolio Diversification' },
  { id: 'risk_position_sizing', num: '34', name: 'Risk Sizing & Ruin Defense' },
  { id: 'quantitative_portfolio', num: '35', name: 'Systematic Portfolio Engine' },
  { id: 'research_checklist', num: '36', name: 'Company Research Checklist' },
  { id: 'buy_decision_framework', num: '37', name: 'When to Buy Decision' },
  { id: 'hold_thesis_tracking', num: '38', name: 'When to Hold Scorecard' },
  { id: 'sell_exit_triggers', num: '39', name: 'When to Sell Exit Triggers' },
  { id: 'master_investing_system', num: '40', name: 'Complete Master System' }
];

export default function HorizontalFilterBar({
  filterType,
  setFilterType,
  totalMetrics = 24,
  positiveCount = 18,
  negativeCount = 6,
  activeChapterId = 'market_liquidity'
}) {
  const [filterOpen, setFilterOpen] = useState(false);
  const [jumpOpen, setJumpOpen] = useState(false);

  const filterRef = useRef(null);
  const jumpRef = useRef(null);
  const activeItemRef = useRef(null);

  // Click outside to close dropdowns
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (filterRef.current && !filterRef.current.contains(event.target)) {
        setFilterOpen(false);
      }
      if (jumpRef.current && !jumpRef.current.contains(event.target)) {
        setJumpOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // When jump dropdown opens, smoothly scroll active item into view
  useEffect(() => {
    if (jumpOpen && activeItemRef.current) {
      activeItemRef.current.scrollIntoView({ block: 'nearest' });
    }
  }, [jumpOpen]);

  const scrollToChapter = (chapterId) => {
    const el = document.getElementById(`chapter-${chapterId}`);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    setJumpOpen(false);
  };

  const currentChapter = CHAPTER_SHORT_NAMES.find((c) => c.id === activeChapterId) || CHAPTER_SHORT_NAMES[0];

  return (
    <div className="horizontal-filter-bar">
      {/* Left Area: Sleek Dropdown Filters & Jump To */}
      <div className="filter-dropdowns-group">
        {/* 1. Filter Facts Dropdown */}
        <div className="custom-dropdown-container" ref={filterRef}>
          <div className="dropdown-label-prefix">
            <Layers size={13} color="var(--fintech-primary)" />
            <span>Filter Facts:</span>
          </div>

          <button
            type="button"
            className={`custom-dropdown-trigger ${filterType !== 'all' ? filterType : ''}`}
            onClick={() => {
              setFilterOpen(!filterOpen);
              setJumpOpen(false);
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              {filterType === 'positive' ? (
                <CheckCircle2 size={13} color="var(--gain-green)" />
              ) : filterType === 'negative' ? (
                <AlertTriangle size={13} color="var(--loss-red)" />
              ) : null}
              <span>
                {filterType === 'positive'
                  ? 'Strengths'
                  : filterType === 'negative'
                  ? 'Red Flags'
                  : 'All Metrics'}
              </span>
              <span className={`trigger-badge font-mono ${filterType}`}>
                {filterType === 'positive'
                  ? positiveCount
                  : filterType === 'negative'
                  ? negativeCount
                  : totalMetrics}
              </span>
            </div>
            <ChevronDown size={13} className={`chevron-icon ${filterOpen ? 'open' : ''}`} />
          </button>

          {filterOpen && (
            <div className="custom-dropdown-menu">
              <div
                className={`dropdown-menu-item ${filterType === 'all' ? 'selected' : ''}`}
                onClick={() => {
                  setFilterType('all');
                  setFilterOpen(false);
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Layers size={13} color="var(--text-secondary)" />
                  <span>All Metrics</span>
                </div>
                <span className="trigger-badge font-mono">{totalMetrics}</span>
              </div>

              <div
                className={`dropdown-menu-item ${filterType === 'positive' ? 'selected positive' : ''}`}
                onClick={() => {
                  setFilterType('positive');
                  setFilterOpen(false);
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <CheckCircle2 size={13} color="var(--gain-green)" />
                  <span>Strengths (Bullish)</span>
                </div>
                <span className="trigger-badge positive font-mono">{positiveCount}</span>
              </div>

              <div
                className={`dropdown-menu-item ${filterType === 'negative' ? 'selected negative' : ''}`}
                onClick={() => {
                  setFilterType('negative');
                  setFilterOpen(false);
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <AlertTriangle size={13} color="var(--loss-red)" />
                  <span>Red Flags (Bearish)</span>
                </div>
                <span className="trigger-badge negative font-mono">{negativeCount}</span>
              </div>
            </div>
          )}
        </div>

        {/* 2. Jump To Chapter Dropdown */}
        <div className="custom-dropdown-container" ref={jumpRef}>
          <div className="dropdown-label-prefix">
            <Compass size={13} color="var(--fintech-blue)" />
            <span>Jump to:</span>
          </div>

          <button
            type="button"
            className="custom-dropdown-trigger chapter-trigger"
            onClick={() => {
              setJumpOpen(!jumpOpen);
              setFilterOpen(false);
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span className="jump-num-pill font-mono">{currentChapter.num}</span>
              <span>{currentChapter.name}</span>
            </div>
            <ChevronDown size={13} className={`chevron-icon ${jumpOpen ? 'open' : ''}`} />
          </button>

          {jumpOpen && (
            <div className="custom-dropdown-menu chapter-menu" style={{ maxHeight: '188px', overflowY: 'auto' }}>
              {CHAPTER_SHORT_NAMES.map((ch) => (
                <div
                  key={ch.id}
                  ref={activeChapterId === ch.id ? activeItemRef : null}
                  className={`dropdown-menu-item ${activeChapterId === ch.id ? 'selected' : ''}`}
                  onClick={() => scrollToChapter(ch.id)}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span className="jump-num-pill font-mono">{ch.num}</span>
                    <span>{ch.name}</span>
                  </div>
                  {activeChapterId === ch.id && (
                    <span style={{ fontSize: '10px', color: 'var(--fintech-primary)', fontWeight: '800' }}>
                      ACTIVE
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Right Area: Analytical Coverage & Filing Audit Integrity (moved from StockBanner) */}
      <div className="filter-audit-meta-group">
        <div className="audit-meta-item">
          <BookOpen size={14} color="var(--fintech-primary)" />
          <span className="audit-meta-label">Analytical Coverage:</span>
          <span className="audit-meta-val font-mono">
            {totalMetrics} Metrics Across 40 Institutional Chapters
          </span>
        </div>

        <div className="audit-meta-divider" />

        <div className="audit-meta-item">
          <ShieldCheck size={16} color="var(--gain-green)" />
          <span className="audit-meta-label">Filing Audit Integrity:</span>
          <span className="audit-meta-val" style={{ color: 'var(--gain-green)', fontWeight: '700' }}>
            100% Real Audited Statements
          </span>
        </div>
      </div>
    </div>
  );
}
