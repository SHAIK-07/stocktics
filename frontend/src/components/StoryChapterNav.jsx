import React from 'react';
import {
  Building2,
  Shield,
  TrendingUp,
  Percent,
  Search,
  DollarSign,
  Layers,
  CheckCircle2,
  AlertTriangle,
  ChevronRight,
  Scale,
  Award,
  ShieldCheck,
  Briefcase,
  Compass,
  Calculator,
  Coins,
  Users,
  Target,
  Zap,
  Database,
  Activity,
  BarChart2,
  ListOrdered,
  Cpu,
  Eye,
  AlertOctagon,
  PieChart,
  ShieldAlert,
  ClipboardCheck
} from 'lucide-react';

const CHAPTER_ICONS = {
  market_liquidity: Building2,
  financial_solvency: Shield,
  revenue_growth: TrendingUp,
  profitability_margins: Percent,
  earnings_forensics: Search,
  cash_flow_engine: DollarSign,
  debt_solvency: Scale,
  capital_efficiency: Award,
  business_quality: ShieldCheck,
  management_capital_allocation: Briefcase,
  industry_sector_dynamics: Compass,
  pe_valuation: Calculator,
  ev_valuation: Layers,
  fcf_valuation: Coins,
  dcf_valuation: Calculator,
  relative_valuation: Users,
  factor_profile: Target,
  momentum_factors: Zap,
  value_factors: DollarSign,
  quality_factors: ShieldCheck,
  growth_factors: TrendingUp,
  multi_factor_synthesis: Layers,
  data_engineering_hygiene: Database,
  distribution_analytics: Activity,
  correlation_signal: BarChart2,
  stock_ranking: ListOrdered,
  pattern_discovery: Search,
  backtest_architecture: Cpu,
  look_ahead_defense: Eye,
  survivorship_defense: AlertOctagon,
  overfitting_defense: ShieldAlert,
  backtest_performance: Activity,
  portfolio_diversification: PieChart,
  risk_position_sizing: ShieldAlert,
  quantitative_portfolio: Briefcase,
  research_checklist: ClipboardCheck,
  buy_decision_framework: Target,
  hold_thesis_tracking: Shield,
  sell_exit_triggers: AlertOctagon,
  master_investing_system: Award
};

export default function StoryChapterNav({
  dimensions,
  activeChapterId,
  onSelectChapter,
  filterType,
  setFilterType,
  totalMetrics,
  positiveCount,
  negativeCount
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {/* Quick Filter Segmented Control */}
      <div className="nav-filter-card">
        <span className="nav-section-title">
          Filter Financial Facts
        </span>

        <div className="nav-filter-buttons">
          <button
            onClick={() => setFilterType('all')}
            className={`filter-btn ${filterType === 'all' ? 'active' : ''}`}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Layers size={14} /> All Metrics
            </span>
            <span className="filter-count-badge font-mono">{totalMetrics}</span>
          </button>

          <button
            onClick={() => setFilterType('positive')}
            className={`filter-btn ${filterType === 'positive' ? 'active positive' : ''}`}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle2 size={14} color="var(--gain-green)" /> Strengths
            </span>
            <span className="filter-count-badge positive font-mono">{positiveCount}</span>
          </button>

          <button
            onClick={() => setFilterType('negative')}
            className={`filter-btn ${filterType === 'negative' ? 'active negative' : ''}`}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertTriangle size={14} color="var(--loss-red)" /> Red Flags
            </span>
            <span className="filter-count-badge negative font-mono">{negativeCount}</span>
          </button>
        </div>
      </div>

      {/* Story Chapters Index */}
      <div className="nav-chapters-card">
        <span className="nav-section-title" style={{ marginBottom: '8px' }}>
          Company Financial Story
        </span>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {dimensions.map((dim, idx) => {
            const Icon = CHAPTER_ICONS[dim.id] || Layers;
            const isActive = activeChapterId === dim.id;
            const posCount = dim.metrics.filter(m => m.isPositive).length;
            const totalInDim = dim.metrics.length;
            const isAllGood = posCount === totalInDim;
            const chapterNumStr = String(dim.chapterNumber || idx + 1).padStart(2, '0');

            return (
              <div
                key={dim.id}
                onClick={() => onSelectChapter(dim.id)}
                className={`chapter-nav-item ${isActive ? 'active' : ''}`}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
                  <div className={`chapter-icon-box ${isActive ? 'active' : ''}`}>
                    <Icon size={16} />
                  </div>
                  <div style={{ minWidth: 0 }}>
                    <div className="chapter-title-text" style={{ color: isActive ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                      {dim.title}
                    </div>
                    <div className="chapter-sub-text font-mono">
                      CHAPTER {chapterNumStr}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexShrink: 0 }}>
                  <span className={`chapter-score-pill font-mono ${isAllGood ? 'good' : 'warning'}`}>
                    {posCount}/{totalInDim}
                  </span>
                  <ChevronRight size={14} color={isActive ? 'var(--fintech-primary)' : 'var(--text-muted)'} />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
