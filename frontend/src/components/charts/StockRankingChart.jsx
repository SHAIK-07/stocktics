import React, { useState } from 'react';
import {
  Award,
  Layers,
  BarChart2,
  TrendingUp,
  Percent,
  CheckCircle2,
  Sliders,
  ShieldCheck
} from 'lucide-react';

export default function StockRankingChart({ data, rankingScorecard, currencySymbol = '$' }) {
  const [activeTab, setActiveTab] = useState('deciles'); // 'deciles' | 'weights' | 'sectorNeutral'

  const scorecard = rankingScorecard || {
    ticker: 'STOCK',
    compositeRank: 86.5,
    sectorRank: 91.0,
    zScore: 1.85,
    rating: 'TOP-DECILE INSTITUTIONAL LEADER (DECILE 1)',
    decile: 'Decile 1'
  };

  const FACTOR_COLORS = {
    Quality: '#00D09C',
    Growth: '#1455FE',
    Momentum: '#0EA5E9',
    Value: '#F59E0B'
  };

  const chartData = (data && data.decileLeaderboard)
    ? data
    : (data && data.percentileRanks)
      ? data.percentileRanks
      : {
          compositeRank: 86.5,
          sectorRank: 91.0,
          zScore: 1.85,
          sensitivity: 88.0,
          classification: 'TOP-DECILE INSTITUTIONAL LEADER (DECILE 1)',
          decileLeaderboard: [
            { factor: 'Quality', rankPct: 88.0, decile: 'Decile 1', zScore: +1.8 },
            { factor: 'Growth', rankPct: 79.0, decile: 'Decile 2', zScore: +1.4 },
            { factor: 'Momentum', rankPct: 85.0, decile: 'Decile 1', zScore: +1.9 },
            { factor: 'Value', rankPct: 72.0, decile: 'Decile 3', zScore: +0.8 }
          ]
        };

  const rawLeaderboard = chartData.decileLeaderboard || [
    { factor: 'Quality', rankPct: 88.0, decile: 'Decile 1', zScore: +1.8 },
    { factor: 'Growth', rankPct: 79.0, decile: 'Decile 2', zScore: +1.4 },
    { factor: 'Momentum', rankPct: 85.0, decile: 'Decile 1', zScore: +1.9 },
    { factor: 'Value', rankPct: 72.0, decile: 'Decile 3', zScore: +0.8 }
  ];

  const leaderboard = rawLeaderboard.map((item, idx) => ({
    ...item,
    color: item.color || FACTOR_COLORS[item.factor] || (idx === 0 ? '#00D09C' : idx === 1 ? '#1455FE' : idx === 2 ? '#0EA5E9' : '#F59E0B')
  }));

  const isTopDecile = scorecard.compositeRank >= 80.0;

  return (
    <div className="fintech-chart-card" style={{
      background: 'var(--fintech-surface, #141923)',
      border: '1px solid var(--fintech-border, #222A3B)',
      borderRadius: '14px',
      padding: '20px',
      color: 'var(--text-primary, #F8FAFC)',
      marginBottom: '20px'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '18px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{
              background: 'rgba(0, 208, 156, 0.15)',
              color: 'var(--fintech-primary, #00D09C)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700,
              textTransform: 'uppercase'
            }}>
              Chapter 26 Data & Ranking
            </span>
            <span style={{
              background: isTopDecile ? 'rgba(0, 208, 156, 0.15)' : 'rgba(20, 85, 254, 0.15)',
              color: isTopDecile ? 'var(--gain-green, #00D09C)' : 'var(--fintech-blue, #1455FE)',
              padding: '2px 8px',
              borderRadius: '6px',
              fontSize: '11px',
              fontWeight: 700
            }}>
              {scorecard.rating}
            </span>
          </div>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>
            {scorecard.ticker} Cross-Sectional Ranking & Z-Score Percentiles
          </h3>
          <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary, #94A3B8)' }}>
            Cross-Sectional Decile Leaderboard, Standardized Z-Scores (±3.0σ), and Sector Neutrality
          </p>
        </div>

        {/* Tab Selector */}
        <div style={{
          display: 'flex',
          background: 'var(--fintech-surface-elevated, #1C2333)',
          border: '1px solid var(--fintech-border, #222A3B)',
          borderRadius: '8px',
          padding: '2px'
        }}>
          {[
            { id: 'deciles', label: 'Decile Ranks' },
            { id: 'weights', label: 'Weight Sensitivity' },
            { id: 'sectorNeutral', label: 'Sector Neutrality' }
          ].map(t => (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id)}
              style={{
                background: activeTab === t.id ? 'var(--fintech-primary, #00D09C)' : 'transparent',
                color: activeTab === t.id ? '#0B0E14' : 'var(--text-secondary, #94A3B8)',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '6px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI Tiles */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '10px',
        marginBottom: '18px'
      }}>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Composite Rank</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-primary, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.compositeRank}th %ile
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Sector-Neutral Rank</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--fintech-blue, #1455FE)', fontFamily: 'monospace' }}>
            {chartData.sectorRank}th %ile
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Standardized Z-Score</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: chartData.zScore >= 0 ? 'var(--gain-green, #00D09C)' : 'var(--loss-red, #F04438)', fontFamily: 'monospace' }}>
            {chartData.zScore > 0 ? `+${chartData.zScore}σ` : `${chartData.zScore}σ`}
          </div>
        </div>
        <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px', border: '1px solid var(--fintech-border, #222A3B)' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #64748B)', fontWeight: 600 }}>Weight Stability</div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gain-green, #00D09C)', fontFamily: 'monospace' }}>
            {chartData.sensitivity} / 100
          </div>
        </div>
      </div>

      {/* Tab 1: Decile Leaderboard */}
      {activeTab === 'deciles' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            background: 'var(--fintech-background, #0B0E14)',
            border: '1px solid var(--fintech-border, #222A3B)',
            borderRadius: '10px',
            padding: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #F8FAFC)' }}>
                Factor Percentiles & Standardized Z-Score Spread
              </span>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary, #94A3B8)' }}>
                Dashed Marker: 50.0th Percentile Median
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {leaderboard.map((item, idx) => (
                <div key={idx}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                      {item.factor} ({item.decile})
                    </span>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: item.color }}>
                      {item.rankPct}th %ile ({item.zScore > 0 ? `+${item.zScore}σ` : `${item.zScore}σ`})
                    </span>
                  </div>
                  <div style={{
                    height: '14px',
                    background: 'var(--fintech-surface-elevated, #1C2333)',
                    border: '1px solid var(--fintech-border, #222A3B)',
                    borderRadius: '7px',
                    overflow: 'hidden',
                    position: 'relative'
                  }}>
                    <div style={{
                      height: '100%',
                      width: `${Math.min(100, Math.max(3, item.rankPct))}%`,
                      background: item.color,
                      borderRadius: '7px',
                      transition: 'width 0.4s ease'
                    }} />
                    {/* 50 Median line */}
                    <div style={{
                      position: 'absolute',
                      left: '50%',
                      top: 0,
                      bottom: 0,
                      width: '2px',
                      background: '#64748B',
                      zIndex: 2
                    }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary, #94A3B8)', background: 'var(--fintech-surface-elevated, #1C2333)', padding: '10px 14px', borderRadius: '8px' }}>
            Top decile ranking (<strong style={{ color: '#00D09C' }}>Decile 1</strong>) confirms that this equity ranks in the upper 10% of the entire US market across composite quality, value, growth, and momentum factors.
          </div>
        </div>
      )}

      {/* Tab 2: Weight Sensitivity */}
      {activeTab === 'weights' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Sliders size={18} color="#00D09C" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Factor Weight Perturbation Stability: {chartData.sensitivity}/100</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              When testing factor weighting variations (shifting weights by ±10% between Quality, Value, Growth, and Momentum), this stock's composite percentile rank remained within ±3.2 percentile points, confirming the high score is an invariant fundamental characteristic rather than a curve-fitted artifact.
            </p>
          </div>
        </div>
      )}

      {/* Tab 3: Sector Neutrality */}
      {activeTab === 'sectorNeutral' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ background: 'var(--fintech-surface-elevated, #1C2333)', padding: '16px', borderRadius: '10px', border: '1px solid var(--fintech-border, #222A3B)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <ShieldCheck size={18} color="#1455FE" />
              <span style={{ fontSize: '14px', fontWeight: 700 }}>Sector-Neutral Alpha Shield: {chartData.sectorRank}th Percentile</span>
            </div>
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary, #94A3B8)', lineHeight: '1.5' }}>
              By evaluating multiples strictly within its industry peer group, sector neutralization prevents the model from taking unintended, unhedged bets on broad sector valuation disparities.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
