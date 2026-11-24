import React from 'react';
import { Layers, CheckCircle2, AlertTriangle, ArrowDown } from 'lucide-react';

export default function FilterBar({ filterType, setFilterType, dimensions, totalMetrics, positiveCount, negativeCount }) {
  const scrollToSection = (id) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <div className="filter-bar-container">
      {/* Filter Tabs */}
      <div className="filter-pills-group">
        <button
          className={`filter-tab-btn ${filterType === 'all' ? 'active' : ''}`}
          onClick={() => setFilterType('all')}
        >
          <Layers size={14} />
          <span>All Metrics</span>
          <span className="filter-count">{totalMetrics}</span>
        </button>

        <button
          className={`filter-tab-btn ${filterType === 'positive' ? 'active' : ''}`}
          onClick={() => setFilterType('positive')}
          style={{ color: filterType === 'positive' ? 'var(--gain-green)' : undefined }}
        >
          <CheckCircle2 size={14} color="var(--gain-green)" />
          <span>Positive Strengths</span>
          <span className="filter-count" style={{ color: 'var(--gain-green)' }}>{positiveCount}</span>
        </button>

        <button
          className={`filter-tab-btn ${filterType === 'negative' ? 'active' : ''}`}
          onClick={() => setFilterType('negative')}
          style={{ color: filterType === 'negative' ? 'var(--loss-red)' : undefined }}
        >
          <AlertTriangle size={14} color="var(--loss-red)" />
          <span>Risks & Red Flags</span>
          <span className="filter-count" style={{ color: 'var(--loss-red)' }}>{negativeCount}</span>
        </button>
      </div>

      {/* Quick Jump Buttons to Dimensions */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', overflowX: 'auto', padding: '4px 0' }}>
        {dimensions.map((dim, idx) => (
          <button
            key={dim.id}
            onClick={() => scrollToSection(dim.id)}
            style={{
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-hairline)',
              borderRadius: '6px',
              padding: '6px 12px',
              color: 'var(--text-secondary)',
              fontSize: '12px',
              fontWeight: '600',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
              transition: 'all 0.12s ease'
            }}
            onMouseOver={(e) => { e.currentTarget.style.borderColor = 'var(--text-tertiary)'; e.currentTarget.style.color = 'var(--text-primary)'; }}
            onMouseOut={(e) => { e.currentTarget.style.borderColor = 'var(--border-hairline)'; e.currentTarget.style.color = 'var(--text-secondary)'; }}
          >
            {dim.title.split('.')[1]?.trim() || dim.title}
          </button>
        ))}
      </div>
    </div>
  );
}
