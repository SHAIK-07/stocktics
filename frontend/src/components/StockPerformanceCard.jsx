import React from 'react';
import { Zap, TrendingUp, TrendingDown } from 'lucide-react';

export default function StockPerformanceCard({ performance, onSelectPeriod }) {
  const HORIZONS = [
    { key: '1W', label: '1W' },
    { key: '1M', label: '1M' },
    { key: '3M', label: '3M' },
    { key: '6M', label: '6M' },
    { key: 'YTD', label: 'YTD' },
    { key: '1Y', label: '1Y' },
  ];

  return (
    <div className="stock-performance-card">
      <div className="performance-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Zap size={16} color="var(--fintech-primary)" />
          <span className="performance-title">Performance</span>
        </div>
        <span className="performance-chip font-mono">Trailing Returns</span>
      </div>

      <div className="performance-grid">
        {HORIZONS.map((h) => {
          const val = performance ? performance[h.key] : null;
          const isAvailable = val != null && !isNaN(val);
          const isPos = isAvailable && val >= 0;
          const isNeg = isAvailable && val < 0;

          return (
            <div
              key={h.key}
              className={`perf-pill ${isPos ? 'pos' : isNeg ? 'neg' : 'neutral'}`}
              onClick={() => onSelectPeriod && onSelectPeriod(h.key)}
              title={isAvailable ? `${h.label} Return: ${isPos ? '+' : ''}${val}%` : 'Calculating...'}
            >
              <div className="perf-val font-mono">
                {isAvailable ? `${isPos ? '+' : ''}${val.toFixed(2)}%` : '—'}
              </div>
              <div className="perf-label font-mono">{h.label}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
