import React, { useState } from 'react';
import PriceHistoryChart from './charts/PriceHistoryChart';
import RevenueGrowthChart from './charts/RevenueGrowthChart';
import MarginEvolutionChart from './charts/MarginEvolutionChart';
import CashFlowDivergenceChart from './charts/CashFlowDivergenceChart';
import SloanAccrualMeter from './charts/SloanAccrualMeter';
import SolvencyStructureChart from './charts/SolvencyStructureChart';
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
import { LineChart, BarChart2 } from 'lucide-react';

const CHART_TABS = [
  { id: 'market_liquidity', label: '01 Scale' },
  { id: 'financial_solvency', label: '02 Solvency' },
  { id: 'revenue_growth', label: '03 Revenue' },
  { id: 'profitability_margins', label: '04 Margins' },
  { id: 'earnings_forensics', label: '05 Accruals' },
  { id: 'cash_flow_engine', label: '06 Cash Flow' },
  { id: 'debt_solvency', label: '07 Leverage' },
  { id: 'capital_efficiency', label: '08 Capital' },
  { id: 'business_quality', label: '09 Moat' },
  { id: 'management_capital_allocation', label: '10 Capital' },
  { id: 'industry_sector_dynamics', label: '11 Sector' },
  { id: 'pe_valuation', label: '12 P/E' },
  { id: 'ev_valuation', label: '13 EV' },
  { id: 'fcf_valuation', label: '14 FCF' },
  { id: 'dcf_valuation', label: '15 DCF' },
  { id: 'relative_valuation', label: '16 Rel' },
  { id: 'factor_profile', label: '17 Factor' },
  { id: 'momentum_factors', label: '18 Mom' },
  { id: 'value_factors', label: '19 Val' },
  { id: 'quality_factors', label: '20 Qual' },
  { id: 'growth_factors', label: '21 Growth' },
  { id: 'multi_factor_synthesis', label: '22 Multi' },
  { id: 'data_engineering_hygiene', label: '23 Data' },
  { id: 'distribution_analytics', label: '24 EDA' },
  { id: 'correlation_signal', label: '25 Corr' },
  { id: 'stock_ranking', label: '26 Rank' },
  { id: 'pattern_discovery', label: '27 Pattern' },
  { id: 'backtest_architecture', label: '28 Backtest' },
  { id: 'look_ahead_defense', label: '29 LookAhead' },
  { id: 'survivorship_defense', label: '30 Survivor' },
  { id: 'overfitting_defense', label: '31 Overfit' },
  { id: 'backtest_performance', label: '32 Perform' },
  { id: 'portfolio_diversification', label: '33 Diversify' },
  { id: 'risk_position_sizing', label: '34 Sizing' },
  { id: 'quantitative_portfolio', label: '35 Engine' },
  { id: 'research_checklist', label: '36 Checklist' },
  { id: 'buy_decision_framework', label: '37 Buy' },
  { id: 'hold_thesis_tracking', label: '38 Hold' },
  { id: 'sell_exit_triggers', label: '39 Sell' },
  { id: 'master_investing_system', label: '40 Master' }
];

export default function StoryRightColumn({ stockData, activeChapterId = 'market_liquidity' }) {
  const [selectedChartTab, setSelectedChartTab] = useState(activeChapterId);

  // Automatically sync chart with chapter scrolling
  React.useEffect(() => {
    if (activeChapterId) {
      setSelectedChartTab(activeChapterId);
    }
  }, [activeChapterId]);

  if (!stockData) return null;

  const { ticker, currencySymbol, charts } = stockData;

  return (
    <div className="right-charts-column" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* 1. Historical Stock Price & Volume Chart (1D, 5D, 1M, 6M, 1Y, 5Y) */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', paddingLeft: '4px' }}>
          <LineChart size={15} color="var(--fintech-primary)" />
          <span style={{ fontSize: '11.5px', fontWeight: '800', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Historical Stock Price & Volume
          </span>
        </div>
        <PriceHistoryChart ticker={ticker} currencySymbol={currencySymbol} />
      </div>

      {/* 2. Chapter Financial Timeseries with Quick Tabs */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px', paddingLeft: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <BarChart2 size={15} color="var(--fintech-blue)" />
            <span style={{ fontSize: '11.5px', fontWeight: '800', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Chapter Financial Timeseries
            </span>
          </div>
        </div>

        {/* Tab switcher for financial charts */}
        <div className="chapter-chart-tabs">
          {CHART_TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setSelectedChartTab(tab.id)}
              className={`chart-tab-btn ${selectedChartTab === tab.id ? 'active' : ''}`}
            >
              <span className="font-mono">{tab.label}</span>
            </button>
          ))}
        </div>

        {/* Chapter 1: Scale & Liquidity Info */}
        {selectedChartTab === 'market_liquidity' && (
          <div className="chart-container-card" style={{ margin: 0, padding: '18px 20px' }}>
            <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span>Market Valuation & Liquidity Profile</span>
              <span className="exchange-chip">{stockData.exchange}</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ color: 'var(--text-tertiary)' }}>Total Market Valuation:</span>
                <span className="font-mono" style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{stockData.formattedMarketCap}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ color: 'var(--text-tertiary)' }}>Currency Unit:</span>
                <span className="font-mono" style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{stockData.currency} ({currencySymbol})</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ color: 'var(--text-tertiary)' }}>52-Week Range:</span>
                <span className="font-mono" style={{ fontWeight: '700', color: 'var(--text-primary)' }}>
                  {stockData.formattedFiftyTwoWeekLow || `${currencySymbol}${stockData.fiftyTwoWeekLow?.toFixed(2)}`} - {stockData.formattedFiftyTwoWeekHigh || `${currencySymbol}${stockData.fiftyTwoWeekHigh?.toFixed(2)}`}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-tertiary)' }}>Sector / Industry:</span>
                <span style={{ fontWeight: '600', color: 'var(--text-secondary)', textAlign: 'right' }}>{stockData.sector}</span>
              </div>
            </div>
          </div>
        )}

        {/* Chapter 2: Solvency Balance Sheet Structure */}
        {selectedChartTab === 'financial_solvency' && charts?.solvency && (
          <SolvencyStructureChart data={charts.solvency} currencySymbol={currencySymbol} />
        )}

        {/* Chapter 3: Revenue Growth Trajectory */}
        {selectedChartTab === 'revenue_growth' && charts?.revenue && (
          <div style={{ margin: 0 }}>
            <RevenueGrowthChart data={charts.revenue} currencySymbol={currencySymbol} />
          </div>
        )}

        {/* Chapter 4: Margins Evolution */}
        {selectedChartTab === 'profitability_margins' && charts?.margins && (
          <div style={{ margin: 0 }}>
            <MarginEvolutionChart data={charts.margins} />
          </div>
        )}

        {/* Chapter 5: Sloan Accrual Gauge */}
        {selectedChartTab === 'earnings_forensics' && charts?.sloanGauge && (
          <div style={{ margin: 0 }}>
            <SloanAccrualMeter data={charts.sloanGauge} />
          </div>
        )}

        {/* Chapter 6: Cash Flow Engine */}
        {selectedChartTab === 'cash_flow_engine' && charts?.cashflow && (
          <div style={{ margin: 0 }}>
            <CashFlowDivergenceChart data={charts.cashflow} currencySymbol={currencySymbol} />
          </div>
        )}

        {/* Chapter 7: Balance Sheet Leverage & Debt Solvency */}
        {selectedChartTab === 'debt_solvency' && charts?.leverage && (
          <div style={{ margin: 0 }}>
            <DebtLeverageChart data={charts.leverage} altmanZ={charts.altmanZ} currencySymbol={currencySymbol} />
          </div>
        )}

        {/* Chapter 8: Capital Efficiency & DuPont */}
        {selectedChartTab === 'capital_efficiency' && charts?.roic && (
          <div style={{ margin: 0 }}>
            <DuPontRoicChart data={charts.roic} dupont={charts.dupont} currencySymbol={currencySymbol} />
          </div>
        )}

        {/* Chapter 9: Business Quality & Economic Moats */}
        {selectedChartTab === 'business_quality' && (
          <div style={{ margin: 0 }}>
            <BusinessQualityMoatRadar
              data={charts?.moat}
              moatPillars={charts?.moatPillars}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 10: Management Quality & Capital Allocation */}
        {selectedChartTab === 'management_capital_allocation' && (
          <div style={{ margin: 0 }}>
            <CapitalAllocationWaterfallChart
              data={charts?.capitalAllocation}
              capitalScorecard={charts?.capitalScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 11: Industry & Sector Dynamics */}
        {selectedChartTab === 'industry_sector_dynamics' && (
          <div style={{ margin: 0 }}>
            <IndustrySectorDynamicsChart
              data={charts?.sectorDynamics}
              sectorScorecard={charts?.sectorScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 12: P/E Ratio & Earnings Yield */}
        {selectedChartTab === 'pe_valuation' && (
          <div style={{ margin: 0 }}>
            <PeValuationChart
              data={charts?.peValuation}
              peScorecard={charts?.peScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 13: Price-to-Sales & EV Multiples */}
        {selectedChartTab === 'ev_valuation' && (
          <div style={{ margin: 0 }}>
            <EvValuationChart
              data={charts?.evValuation}
              evScorecard={charts?.evScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 14: Free Cash Flow & Owner Earnings */}
        {selectedChartTab === 'fcf_valuation' && (
          <div style={{ margin: 0 }}>
            <FcfValuationChart
              data={charts?.fcfValuation}
              fcfScorecard={charts?.fcfScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 15: Discounted Cash Flow (DCF) Valuation */}
        {selectedChartTab === 'dcf_valuation' && (
          <div style={{ margin: 0 }}>
            <DcfValuationChart
              data={charts?.dcfValuation}
              dcfScorecard={charts?.dcfScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 16: Relative Valuation & Peer Benchmarking */}
        {selectedChartTab === 'relative_valuation' && (
          <div style={{ margin: 0 }}>
            <RelativeValuationChart
              data={charts?.relativeValuation}
              relativeScorecard={charts?.relativeScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 17: Quantitative Factor Profile */}
        {selectedChartTab === 'factor_profile' && (
          <div style={{ margin: 0 }}>
            <FactorProfileChart
              data={charts?.factorProfile}
              factorScorecard={charts?.factorScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 18: Quantitative Momentum Factors & Trend */}
        {selectedChartTab === 'momentum_factors' && (
          <div style={{ margin: 0 }}>
            <MomentumFactorsChart
              data={charts?.momentumFactors}
              momentumScorecard={charts?.momentumScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 19: Quantitative Value Factors & Yield Hierarchy */}
        {selectedChartTab === 'value_factors' && (
          <div style={{ margin: 0 }}>
            <ValueFactorsChart
              data={charts?.valueFactors}
              valueScorecard={charts?.valueScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 20: Quality Factors & Piotroski F-Score */}
        {selectedChartTab === 'quality_factors' && (
          <div style={{ margin: 0 }}>
            <QualityFactorsChart
              data={charts?.qualityFactors}
              qualityScorecard={charts?.qualityScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 21: Growth Factors & SGR */}
        {selectedChartTab === 'growth_factors' && (
          <div style={{ margin: 0 }}>
            <GrowthFactorsChart
              data={charts?.growthFactors}
              growthScorecard={charts?.growthScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 22: Multi-Factor Synthesis & Blend Models */}
        {selectedChartTab === 'multi_factor_synthesis' && (
          <div style={{ margin: 0 }}>
            <MultiFactorSynthesisChart
              data={charts?.multiFactor}
              multiFactorScorecard={charts?.multiFactorScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 23: Stock Dataset Architecture & PIT Hygiene */}
        {selectedChartTab === 'data_engineering_hygiene' && (
          <div style={{ margin: 0 }}>
            <DatasetHygieneChart
              data={charts?.dataHygiene}
              datasetScorecard={charts?.datasetScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 24: Exploratory Data Analysis & Fat-Tail Risk */}
        {selectedChartTab === 'distribution_analytics' && (
          <div style={{ margin: 0 }}>
            <DistributionAnalyticsChart
              data={charts?.distributionAnalytics}
              distributionScorecard={charts?.distributionScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 25: Correlation Analysis & Information Coefficient */}
        {selectedChartTab === 'correlation_signal' && (
          <div style={{ margin: 0 }}>
            <CorrelationSignalChart
              data={charts?.correlationSignal}
              correlationScorecard={charts?.correlationScorecard}
              currencySymbol={currencySymbol}
            />
          </div>
        )}

        {/* Chapter 26: Multi-Factor Stock Ranking */}
        {selectedChartTab === 'stock_ranking' && (
          <div style={{ margin: 0 }}>
            <StockRankingChart
              data={charts?.percentileRanks || charts}
              rankingScorecard={charts?.rankingScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 27: Fundamental Pattern Discovery & Anomaly Hunting */}
        {selectedChartTab === 'pattern_discovery' && (
          <div style={{ margin: 0 }}>
            <PatternDiscoveryChart
              data={charts}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 28: Backtesting Engine Architecture */}
        {selectedChartTab === 'backtest_architecture' && (
          <div style={{ margin: 0 }}>
            <BacktestArchitectureChart
              data={charts}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 29: Look-Ahead Bias Defense */}
        {selectedChartTab === 'look_ahead_defense' && (
          <div style={{ margin: 0 }}>
            <LookAheadBiasChart
              data={charts?.lookAheadAudit || charts}
              lookAheadScorecard={charts?.lookAheadScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 30: Survivorship Bias Defense */}
        {selectedChartTab === 'survivorship_defense' && (
          <div style={{ margin: 0 }}>
            <SurvivorshipBiasChart
              data={charts?.survivorshipAudit || charts}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 31: Overfitting & Curve-Fitting Defense */}
        {selectedChartTab === 'overfitting_defense' && (
          <div style={{ margin: 0 }}>
            <OverfittingDefenseChart
              data={charts?.overfittingAudit || charts}
              overfittingScorecard={charts?.overfittingScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 32: Backtest Performance Evaluation */}
        {selectedChartTab === 'backtest_performance' && (
          <div style={{ margin: 0 }}>
            <BacktestPerformanceChart
              data={charts?.backtestPerformance || charts}
              performanceScorecard={charts?.backtestScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 33: Portfolio Diversification & Covariance */}
        {selectedChartTab === 'portfolio_diversification' && (
          <div style={{ margin: 0 }}>
            <PortfolioDiversificationChart
              data={charts?.portfolioDiversification || charts}
              diversificationScorecard={charts?.diversificationScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 34: Risk Management, Position Sizing & Ruin Defense */}
        {selectedChartTab === 'risk_position_sizing' && (
          <div style={{ margin: 0 }}>
            <RiskManagementSizingChart
              data={charts?.riskSizing || charts}
              riskScorecard={charts?.riskScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 35: Quantitative Systematic Portfolio Engine */}
        {selectedChartTab === 'quantitative_portfolio' && (
          <div style={{ margin: 0 }}>
            <QuantitativePortfolioChart
              data={charts?.quantitativePortfolio || charts}
              portfolioScorecard={charts?.portfolioEngineScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 36: Company Research Checklist */}
        {selectedChartTab === 'research_checklist' && (
          <div style={{ margin: 0 }}>
            <ResearchChecklistChart
              data={charts?.researchChecklist || charts}
              researchScorecard={charts?.researchScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 37: How to Decide When to Buy */}
        {selectedChartTab === 'buy_decision_framework' && (
          <div style={{ margin: 0 }}>
            <BuyDecisionFrameworkChart
              data={charts?.buyDecisionFramework || charts}
              buyScorecard={charts?.buyScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 38: How to Decide When to Hold */}
        {selectedChartTab === 'hold_thesis_tracking' && (
          <div style={{ margin: 0 }}>
            <HoldThesisTrackingChart
              data={charts?.holdThesisTracking || charts}
              holdScorecard={charts?.holdScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 39: How to Decide When to Sell */}
        {selectedChartTab === 'sell_exit_triggers' && (
          <div style={{ margin: 0 }}>
            <SellExitTriggersChart
              data={charts?.sellExitTriggers || charts}
              sellScorecard={charts?.sellScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}

        {/* Chapter 40: Complete US Stock Investing System */}
        {selectedChartTab === 'master_investing_system' && (
          <div style={{ margin: 0 }}>
            <MasterInvestingSystemChart
              data={charts?.masterInvestingSystem || charts}
              masterSystemScorecard={charts?.masterSystemScorecard}
              ticker={stockData?.symbol}
            />
          </div>
        )}
      </div>
    </div>
  );
}
