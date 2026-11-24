import React, { useState } from 'react';
import MetricCard from './MetricCard';
import SolvencyStructureChart from './charts/SolvencyStructureChart';
import RevenueGrowthChart from './charts/RevenueGrowthChart';
import MarginEvolutionChart from './charts/MarginEvolutionChart';
import SloanAccrualMeter from './charts/SloanAccrualMeter';
import CashFlowDivergenceChart from './charts/CashFlowDivergenceChart';
import DebtLeverageChart from './charts/DebtLeverageChart';
import DuPontRoicChart from './charts/DuPontRoicChart';
import BusinessQualityMoatRadar from './charts/BusinessQualityMoatRadar';
import CapitalAllocationWaterfallChart from './charts/CapitalAllocationWaterfallChart';
import IndustrySectorDynamicsChart from './charts/IndustrySectorDynamicsChart';
import PeValuationChart from './charts/PeValuationChart';
import EvValuationChart from './charts/EvValuationChart';
import FcfValuationChart from './charts/FcfValuationChart';
import DcfValuationChart from './charts/DcfValuationChart';
import RelativeValuationChart from './charts/RelativeValuationChart';
import FactorProfileChart from './charts/FactorProfileChart';
import MomentumFactorsChart from './charts/MomentumFactorsChart';
import ValueFactorsChart from './charts/ValueFactorsChart';
import QualityFactorsChart from './charts/QualityFactorsChart';
import GrowthFactorsChart from './charts/GrowthFactorsChart';
import MultiFactorSynthesisChart from './charts/MultiFactorSynthesisChart';
import DatasetHygieneChart from './charts/DatasetHygieneChart';
import DistributionAnalyticsChart from './charts/DistributionAnalyticsChart';
import CorrelationSignalChart from './charts/CorrelationSignalChart';
import StockRankingChart from './charts/StockRankingChart';
import PatternDiscoveryChart from './charts/PatternDiscoveryChart';
import BacktestArchitectureChart from './charts/BacktestArchitectureChart';
import LookAheadBiasChart from './charts/LookAheadBiasChart';
import SurvivorshipBiasChart from './charts/SurvivorshipBiasChart';
import OverfittingDefenseChart from './charts/OverfittingDefenseChart';
import BacktestPerformanceChart from './charts/BacktestPerformanceChart';
import PortfolioDiversificationChart from './charts/PortfolioDiversificationChart';
import RiskManagementSizingChart from './charts/RiskManagementSizingChart';
import QuantitativePortfolioChart from './charts/QuantitativePortfolioChart';
import ResearchChecklistChart from './charts/ResearchChecklistChart';
import BuyDecisionFrameworkChart from './charts/BuyDecisionFrameworkChart';
import HoldThesisTrackingChart from './charts/HoldThesisTrackingChart';
import SellExitTriggersChart from './charts/SellExitTriggersChart';
import MasterInvestingSystemChart from './charts/MasterInvestingSystemChart';
import { Star, CheckCircle2, ShieldCheck, TrendingUp, BarChart2, ChevronDown, FileText } from 'lucide-react';

export default function StoryCenterColumn({ dimensions, charts, currencySymbol = '$', stockData, filterType = 'all' }) {
  const [expandedChapters, setExpandedChapters] = useState({});

  const toggleChapter = (chapterId) => {
    setExpandedChapters(prev => ({
      ...prev,
      [chapterId]: !prev[chapterId]
    }));
  };

  if (!dimensions || dimensions.length === 0) return null;

  return (
    <div className="story-chapters-flow">
      {dimensions.map((dim, idx) => {
        const { id, title, summary, metrics, chapterNumber } = dim;
        const chapterNum = chapterNumber || idx + 1;
        const chapterNumStr = String(chapterNum).padStart(2, '0');

        // Filter metrics for this chapter
        const filteredMetrics = metrics.filter(m => {
          if (filterType === 'positive') return m.status === 'positive';
          if (filterType === 'negative') return m.status === 'negative';
          return true;
        });

        // Focus metrics for this chapter
        const focusMetrics = metrics.filter(m => m.isFocusMetric);
        const posCount = metrics.filter(m => m.isPositive).length;
        const totalInDim = metrics.length;
        const isAllGood = posCount === totalInDim;

        return (
          <section
            key={id}
            id={`chapter-${id}`}
            className="chapter-story-section"
          >
            {/* 1. Chapter Header Banner */}
            <div className="chapter-story-hero">
              <div className="chapter-hero-top">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span className="chapter-hero-badge font-mono">
                    CHAPTER {chapterNumStr}
                  </span>
                  <h2 className="chapter-hero-title">
                    {title}
                  </h2>
                </div>

                <span className={`chapter-score-pill font-mono ${isAllGood ? 'good' : 'warning'}`}>
                  {posCount}/{totalInDim} Passed
                </span>
              </div>

              <p className="chapter-hero-desc">
                {summary}
              </p>
            </div>

            {/* 2. Primary Key Values to Focus on (Highlighted Prominently) */}
            {focusMetrics.length > 0 && filterType === 'all' && (
              <div className="focus-figures-card">
                <div className="focus-card-header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Star size={15} fill="var(--fintech-primary)" color="var(--fintech-primary)" />
                    <span className="focus-title">Key Focus Figures</span>
                  </div>
                  <span className="focus-subtitle">Priority Indicators for Chapter {chapterNumStr}</span>
                </div>

                <div className="focus-grid">
                  {focusMetrics.map((fm) => {
                    const is52W = fm.id === 'fifty_two_week_position' || fm.name.toLowerCase().includes('52-week');

                    let lowVal = fm.formattedLow || stockData?.formattedFiftyTwoWeekLow;
                    let highVal = fm.formattedHigh || stockData?.formattedFiftyTwoWeekHigh;
                    if (!lowVal && fm.displayValue && fm.displayValue.includes('Low:')) {
                      const matchLow = fm.displayValue.match(/Low:\s*([^\|\)]+)/);
                      if (matchLow) lowVal = matchLow[1].trim();
                    }
                    if (!highVal && fm.displayValue && fm.displayValue.includes('High:')) {
                      const matchHigh = fm.displayValue.match(/High:\s*([^\)]+)/);
                      if (matchHigh) highVal = matchHigh[1].trim();
                    }
                    if (!lowVal && stockData?.fiftyTwoWeekLow) lowVal = `${currencySymbol}${stockData.fiftyTwoWeekLow.toFixed(2)}`;
                    if (!highVal && stockData?.fiftyTwoWeekHigh) highVal = `${currencySymbol}${stockData.fiftyTwoWeekHigh.toFixed(2)}`;

                    const pct = Math.max(0, Math.min(100, typeof fm.value === 'number' && !isNaN(fm.value) ? fm.value : parseFloat(fm.displayValue) || 50));

                    return (
                      <div key={fm.id} className="focus-item-card">
                        <div className="focus-item-header">
                          <span className="focus-item-name">{fm.name}</span>
                          <span className={`status-badge ${fm.status}`}>
                            {fm.status === 'positive' ? 'Healthy' : fm.status === 'negative' ? 'Risk' : 'Neutral'}
                          </span>
                        </div>

                        {is52W ? (
                          <div className="focus-52w-slider-block">
                            <div className="range-header" style={{ marginBottom: '4px' }}>
                              <span
                                className="focus-item-value font-mono tabular-num"
                                style={{ color: fm.isPositive ? 'var(--gain-green)' : 'var(--loss-red)', fontSize: '22px', lineHeight: 1.1 }}
                              >
                                {pct.toFixed(1)}%
                              </span>
                              <span className="range-pos-label font-mono">
                                {pct.toFixed(0)}% OF RANGE
                              </span>
                            </div>

                            <div className="range-bar-track" style={{ margin: '8px 0 6px 0' }}>
                              <div className="range-bar-fill" style={{ width: `${pct}%` }} />
                              <div className="range-bar-dot" style={{ left: `${pct}%` }} />
                            </div>

                            <div className="range-footer font-mono" style={{ marginBottom: '10px', fontSize: '11px' }}>
                              <span>52W L: {lowVal || 'N/A'}</span>
                              <span>52W H: {highVal || 'N/A'}</span>
                            </div>

                            {fm.formula && (
                              <div className="focus-formula font-mono" title={fm.formula}>
                                {fm.formula}
                              </div>
                            )}

                            <div className="focus-benchmark font-mono">
                              Target: {fm.benchmark}
                            </div>
                          </div>
                        ) : (
                          <>
                            <div
                              className="focus-item-value font-mono tabular-num"
                              style={{ color: fm.isPositive ? 'var(--gain-green)' : 'var(--loss-red)' }}
                            >
                              {fm.displayValue}
                            </div>

                            {fm.formula && (
                              <div className="focus-formula font-mono" title={fm.formula}>
                                {fm.formula}
                              </div>
                            )}

                            <div className="focus-benchmark font-mono">
                              Target: {fm.benchmark}
                            </div>
                          </>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* 3. Chapter Financial Timeseries Visualization (Full-Width with High Visibility) */}
            <div className="chapter-chart-wrapper">
              {id === 'market_liquidity' && stockData && (
                <div className="chart-container-card full-width-chart">
                  <div className="chart-header">
                    <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <BarChart2 size={17} color="var(--fintech-primary)" />
                      <span>Market Valuation & Traded Liquidity Summary</span>
                    </div>
                    <span className="exchange-chip font-mono">{stockData.exchange}</span>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px', marginTop: '8px' }}>
                    <div className="stat-item" style={{ background: 'var(--bg-surface)', padding: '12px 14px', borderRadius: '8px', border: '1px solid var(--border-hairline)' }}>
                      <span className="stat-label">Market Capitalization</span>
                      <span className="stat-value font-mono">{stockData.formattedMarketCap}</span>
                    </div>
                    <div className="stat-item" style={{ background: 'var(--bg-surface)', padding: '12px 14px', borderRadius: '8px', border: '1px solid var(--border-hairline)' }}>
                      <span className="stat-label">Currency Unit</span>
                      <span className="stat-value font-mono">{stockData.currency} ({currencySymbol})</span>
                    </div>
                    <div className="stat-item" style={{ background: 'var(--bg-surface)', padding: '12px 14px', borderRadius: '8px', border: '1px solid var(--border-hairline)' }}>
                      <span className="stat-label">52-Week Range</span>
                      <span className="stat-value font-mono" style={{ fontSize: '13px' }}>
                        {stockData.formattedFiftyTwoWeekLow || `${currencySymbol}${stockData.fiftyTwoWeekLow?.toFixed(2)}`} - {stockData.formattedFiftyTwoWeekHigh || `${currencySymbol}${stockData.fiftyTwoWeekHigh?.toFixed(2)}`}
                      </span>
                    </div>
                    <div className="stat-item" style={{ background: 'var(--bg-surface)', padding: '12px 14px', borderRadius: '8px', border: '1px solid var(--border-hairline)' }}>
                      <span className="stat-label">Sector</span>
                      <span className="stat-value" style={{ fontSize: '13px' }}>{stockData.sector}</span>
                    </div>
                  </div>
                </div>
              )}

              {id === 'financial_solvency' && charts?.solvency && (
                <SolvencyStructureChart data={charts.solvency} currencySymbol={currencySymbol} />
              )}

              {id === 'revenue_growth' && charts?.revenue && (
                <RevenueGrowthChart data={charts.revenue} currencySymbol={currencySymbol} />
              )}

              {id === 'profitability_margins' && charts?.margins && (
                <MarginEvolutionChart data={charts.margins} currencySymbol={currencySymbol} />
              )}

              {id === 'earnings_forensics' && charts?.sloanGauge && (
                <SloanAccrualMeter data={charts.sloanGauge} />
              )}

              {id === 'cash_flow_engine' && charts?.cashflow && (
                <CashFlowDivergenceChart data={charts.cashflow} currencySymbol={currencySymbol} />
              )}

              {id === 'debt_solvency' && charts?.leverage && (
                <DebtLeverageChart data={charts.leverage} altmanZ={charts.altmanZ} currencySymbol={currencySymbol} />
              )}

              {id === 'capital_efficiency' && charts?.roic && (
                <DuPontRoicChart data={charts.roic} dupont={charts.dupont} currencySymbol={currencySymbol} />
              )}

              {id === 'business_quality' && (
                <BusinessQualityMoatRadar data={charts?.moat} moatPillars={charts?.moatPillars} currencySymbol={currencySymbol} />
              )}

              {id === 'management_capital_allocation' && (
                <CapitalAllocationWaterfallChart data={charts?.capitalAllocation} capitalScorecard={charts?.capitalScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'industry_sector_dynamics' && (
                <IndustrySectorDynamicsChart data={charts?.sectorDynamics} sectorScorecard={charts?.sectorScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'pe_valuation' && (
                <PeValuationChart data={charts?.peValuation} peScorecard={charts?.peScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'ev_valuation' && (
                <EvValuationChart data={charts?.evValuation} evScorecard={charts?.evScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'fcf_valuation' && (
                <FcfValuationChart data={charts?.fcfValuation} fcfScorecard={charts?.fcfScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'dcf_valuation' && (
                <DcfValuationChart data={charts?.dcfValuation} dcfScorecard={charts?.dcfScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'relative_valuation' && (
                <RelativeValuationChart data={charts?.relativeValuation} relativeScorecard={charts?.relativeScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'factor_profile' && (
                <FactorProfileChart data={charts?.factorProfile} factorScorecard={charts?.factorScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'momentum_factors' && (
                <MomentumFactorsChart data={charts?.momentumFactors} momentumScorecard={charts?.momentumScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'value_factors' && (
                <ValueFactorsChart data={charts?.valueFactors} valueScorecard={charts?.valueScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'quality_factors' && (
                <QualityFactorsChart data={charts?.qualityFactors} qualityScorecard={charts?.qualityScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'growth_factors' && (
                <GrowthFactorsChart data={charts?.growthFactors} growthScorecard={charts?.growthScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'multi_factor_synthesis' && (
                <MultiFactorSynthesisChart data={charts?.multiFactor} multiFactorScorecard={charts?.multiFactorScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'data_engineering_hygiene' && (
                <DatasetHygieneChart data={charts?.dataHygiene} datasetScorecard={charts?.datasetScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'distribution_analytics' && (
                <DistributionAnalyticsChart data={charts?.distributionAnalytics} distributionScorecard={charts?.distributionScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'correlation_signal' && (
                <CorrelationSignalChart data={charts?.correlationSignal} correlationScorecard={charts?.correlationScorecard} currencySymbol={currencySymbol} />
              )}

              {id === 'stock_ranking' && (
                <StockRankingChart
                  data={charts?.percentileRanks || charts}
                  rankingScorecard={charts?.rankingScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'pattern_discovery' && (
                <PatternDiscoveryChart data={charts} ticker={stockData?.symbol} />
              )}

              {id === 'backtest_architecture' && (
                <BacktestArchitectureChart data={charts} ticker={stockData?.symbol} />
              )}

              {id === 'look_ahead_defense' && (
                <LookAheadBiasChart
                  data={charts?.lookAheadAudit || charts}
                  lookAheadScorecard={charts?.lookAheadScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'survivorship_defense' && (
                <SurvivorshipBiasChart
                  data={charts?.survivorshipAudit || charts}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'overfitting_defense' && (
                <OverfittingDefenseChart
                  data={charts?.overfittingAudit || charts}
                  overfittingScorecard={charts?.overfittingScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'backtest_performance' && (
                <BacktestPerformanceChart
                  data={charts?.backtestPerformance || charts}
                  performanceScorecard={charts?.backtestScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'portfolio_diversification' && (
                <PortfolioDiversificationChart
                  data={charts?.portfolioDiversification || charts}
                  diversificationScorecard={charts?.diversificationScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'risk_position_sizing' && (
                <RiskManagementSizingChart
                  data={charts?.riskSizing || charts}
                  riskScorecard={charts?.riskScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'quantitative_portfolio' && (
                <QuantitativePortfolioChart
                  data={charts?.quantitativePortfolio || charts}
                  portfolioScorecard={charts?.portfolioEngineScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'research_checklist' && (
                <ResearchChecklistChart
                  data={charts?.researchChecklist || charts}
                  researchScorecard={charts?.researchScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'buy_decision_framework' && (
                <BuyDecisionFrameworkChart
                  data={charts?.buyDecisionFramework || charts}
                  buyScorecard={charts?.buyScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'hold_thesis_tracking' && (
                <HoldThesisTrackingChart
                  data={charts?.holdThesisTracking || charts}
                  holdScorecard={charts?.holdScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'sell_exit_triggers' && (
                <SellExitTriggersChart
                  data={charts?.sellExitTriggers || charts}
                  sellScorecard={charts?.sellScorecard}
                  ticker={stockData?.symbol}
                />
              )}

              {id === 'master_investing_system' && (
                <MasterInvestingSystemChart
                  data={charts?.masterInvestingSystem || charts}
                  masterSystemScorecard={charts?.masterSystemScorecard}
                  ticker={stockData?.symbol}
                />
              )}
            </div>

            {/* 4. Detailed Metric Fact Cards Collapsible Dropdown */}
            {(() => {
              const isExpanded = !!expandedChapters[id];
              return (
                <div className="chapter-metrics-collapsible">
                  <button
                    type="button"
                    onClick={() => toggleChapter(id)}
                    className={`metrics-dropdown-trigger ${isExpanded ? 'is-expanded' : ''}`}
                    aria-expanded={isExpanded}
                  >
                    <div className="metrics-dropdown-left">
                      <div className="metrics-dropdown-icon">
                        <FileText size={16} />
                      </div>
                      <div className="metrics-dropdown-info">
                        <span className="metrics-dropdown-title">
                          Detailed Metric Fact Sheet
                        </span>
                        <span className="metrics-dropdown-count">
                          {filteredMetrics.length} {filteredMetrics.length === 1 ? 'Metric' : 'Metrics'}
                        </span>
                      </div>
                    </div>

                    <div className="metrics-dropdown-right">
                      <span className="metrics-dropdown-action-text">
                        {isExpanded ? 'Collapse Metrics' : 'Expand Metrics'}
                      </span>
                      <div className={`metrics-dropdown-chevron-box ${isExpanded ? 'is-expanded' : ''}`}>
                        <ChevronDown size={15} />
                      </div>
                    </div>
                  </button>

                  {isExpanded && (
                    <div className="chapter-metrics-list animate-fadeInDown">
                      {filteredMetrics.map((metric) => (
                        <MetricCard key={metric.id} metric={metric} />
                      ))}

                      {filteredMetrics.length === 0 && (
                        <div className="empty-metrics-notice">
                          {filterType === 'negative' ? (
                            <span style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: 'var(--gain-green)' }}>
                              <CheckCircle2 size={16} /> 0 Red Flags in Chapter {chapterNumStr} — All indicators are healthy.
                            </span>
                          ) : filterType === 'positive' ? (
                            <span>No positive metrics matching this filter in Chapter {chapterNumStr}.</span>
                          ) : (
                            <span>No metrics available in Chapter {chapterNumStr}.</span>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })()}
          </section>
        );
      })}
    </div>
  );
}
