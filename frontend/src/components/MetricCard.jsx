import React from 'react';
import { TrendingUp, TrendingDown, Minus, CheckCircle2, AlertTriangle, HelpCircle } from 'lucide-react';

export default function MetricCard({ metric }) {
  if (!metric) return null;

  const {
    name,
    formula,
    displayValue,
    benchmark,
    status,
    whatItMeans,
    investorTakeaway,
    isPositive
  } = metric;

  const getStatusIcon = () => {
    if (status === 'positive') return <TrendingUp size={13} />;
    if (status === 'negative') return <TrendingDown size={13} />;
    return <Minus size={13} />;
  };

  const getStatusText = () => {
    if (status === 'positive') return 'Healthy / Bullish';
    if (status === 'negative') return 'Risk / Bearish';
    return 'Neutral / Caution';
  };

  const statusBorderClass = status === 'positive' ? 'status-border-positive' : status === 'negative' ? 'status-border-negative' : 'status-border-neutral';

  return (
    <div className={`metric-card ${statusBorderClass}`}>
      {/* Header with Title, Formula Pill, and Status Badge */}
      <div className="metric-card-header">
        <div className="metric-name-block">
          <div className="metric-title">{name}</div>
          {formula && (
            <div className="metric-formula-badge font-mono">
              {formula}
            </div>
          )}
        </div>

        <div className={`status-badge ${status}`}>
          {getStatusIcon()}
          <span>{getStatusText()}</span>
        </div>
      </div>

      {/* Main Value & Target Benchmark Row */}
      <div className="metric-value-row">
        <div className="metric-main-value font-mono tabular-num" style={{ color: isPositive ? 'var(--gain-green)' : 'var(--loss-red)' }}>
          {displayValue}
        </div>
        {benchmark && (
          <div className="metric-benchmark font-mono">
            Target: {benchmark}
          </div>
        )}
      </div>

      {/* Educational Explanation Box */}
      <div className="metric-explanation-box">
        <div className="exp-section">
          <div className="exp-label">What This Value Explains</div>
          <div className="exp-text">{whatItMeans}</div>
        </div>

        {investorTakeaway && (
          <div className="exp-section">
            <div className="exp-label">Investor Forensic Takeaway</div>
            <div className="exp-takeaway">{investorTakeaway}</div>
          </div>
        )}
      </div>
    </div>
  );
}
