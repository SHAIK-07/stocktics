import React from 'react';
import MetricCard from './MetricCard';
import RevenueGrowthChart from './charts/RevenueGrowthChart';
import MarginEvolutionChart from './charts/MarginEvolutionChart';
import CashFlowDivergenceChart from './charts/CashFlowDivergenceChart';
import SloanAccrualMeter from './charts/SloanAccrualMeter';
import TradeFrictionSimulator from './charts/TradeFrictionSimulator';
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
import { BookOpen } from 'lucide-react';

export default function MetricSection({ dimension, charts, stockData, filterType = 'all' }) {
  if (!dimension) return null;

  const { id, title, lessonRef, summary, metrics } = dimension;

  // Filter metrics based on filterType
  const filteredMetrics = metrics.filter(m => {
    if (filterType === 'positive') return m.status === 'positive';
    if (filterType === 'negative') return m.status === 'negative';
    return true;
  });

  if (filteredMetrics.length === 0 && filterType !== 'all') {
    return null;
  }

  return (
    <section id={id} className="dimension-section">
      <div className="dimension-header">
        <div className="dimension-title-group">
          <div className="dimension-title">
            <span>{title}</span>
            <span className="dimension-lesson-tag">{lessonRef}</span>
          </div>
          <div className="dimension-subtitle">{summary}</div>
        </div>
      </div>

      {/* Embedded Chart Visualizations for this Dimension */}
      {id === 'market_liquidity' && stockData && (
        <TradeFrictionSimulator stockPrice={stockData.currentPrice} currency={stockData.currency} />
      )}

      {id === 'revenue_growth' && charts?.revenue && (
        <RevenueGrowthChart data={charts.revenue} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'profitability_margins' && charts?.margins && (
        <MarginEvolutionChart data={charts.margins} />
      )}

      {id === 'earnings_forensics' && charts?.sloanGauge && (
        <SloanAccrualMeter data={charts.sloanGauge} />
      )}

      {id === 'cash_flow_engine' && charts?.cashflow && (
        <CashFlowDivergenceChart data={charts.cashflow} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'debt_solvency' && charts?.leverage && (
        <DebtLeverageChart data={charts.leverage} altmanZ={charts.altmanZ} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'capital_efficiency' && charts?.roic && (
        <DuPontRoicChart data={charts.roic} dupont={charts.dupont} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'business_quality' && (
        <BusinessQualityMoatRadar data={charts?.moat} moatPillars={charts?.moatPillars} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'management_capital_allocation' && (
        <CapitalAllocationWaterfallChart data={charts?.capitalAllocation} capitalScorecard={charts?.capitalScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'industry_sector_dynamics' && (
        <IndustrySectorDynamicsChart data={charts?.sectorDynamics} sectorScorecard={charts?.sectorScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'pe_valuation' && (
        <PeValuationChart data={charts?.peValuation} peScorecard={charts?.peScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'ev_valuation' && (
        <EvValuationChart data={charts?.evValuation} evScorecard={charts?.evScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'fcf_valuation' && (
        <FcfValuationChart data={charts?.fcfValuation} fcfScorecard={charts?.fcfScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'dcf_valuation' && (
        <DcfValuationChart data={charts?.dcfValuation} dcfScorecard={charts?.dcfScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'relative_valuation' && (
        <RelativeValuationChart data={charts?.relativeValuation} relativeScorecard={charts?.relativeScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'factor_profile' && (
        <FactorProfileChart data={charts?.factorProfile} factorScorecard={charts?.factorScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'momentum_factors' && (
        <MomentumFactorsChart data={charts?.momentumFactors} momentumScorecard={charts?.momentumScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'value_factors' && (
        <ValueFactorsChart data={charts?.valueFactors} valueScorecard={charts?.valueScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'quality_factors' && (
        <QualityFactorsChart data={charts?.qualityFactors} qualityScorecard={charts?.qualityScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'growth_factors' && (
        <GrowthFactorsChart data={charts?.growthFactors} growthScorecard={charts?.growthScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'multi_factor_synthesis' && (
        <MultiFactorSynthesisChart data={charts?.multiFactor} multiFactorScorecard={charts?.multiFactorScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'data_engineering_hygiene' && (
        <DatasetHygieneChart data={charts?.dataHygiene} datasetScorecard={charts?.datasetScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'distribution_analytics' && (
        <DistributionAnalyticsChart data={charts?.distributionAnalytics} distributionScorecard={charts?.distributionScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'correlation_signal' && (
        <CorrelationSignalChart data={charts?.correlationSignal} correlationScorecard={charts?.correlationScorecard} currencySymbol={stockData?.currencySymbol} />
      )}

      {id === 'stock_ranking' && (
        <StockRankingChart data={charts} ticker={stockData?.symbol} />
      )}

      {id === 'pattern_discovery' && (
        <PatternDiscoveryChart data={charts} ticker={stockData?.symbol} />
      )}

      {id === 'backtest_architecture' && (
        <BacktestArchitectureChart data={charts} ticker={stockData?.symbol} />
      )}

      {id === 'look_ahead_defense' && (
        <LookAheadBiasChart data={charts} ticker={stockData?.symbol} />
      )}

      {id === 'survivorship_defense' && (
        <SurvivorshipBiasChart data={charts?.survivorshipAudit || charts} ticker={stockData?.symbol} />
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

      {/* Metrics Grid */}
      <div className="metrics-grid">
        {filteredMetrics.map((m) => (
          <MetricCard key={m.id} metric={m} />
        ))}
      </div>
    </section>
  );
}
