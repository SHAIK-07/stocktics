import React from 'react';
import { ShieldCheck, AlertTriangle, AlertOctagon } from 'lucide-react';

export default function SloanAccrualMeter({ data }) {
  if (!data) return null;
  const { value, classification } = data;

  // Visual position calculation from -0.12 to +0.12 scale
  const minScale = -0.10;
  const maxScale = 0.10;
  const clampedVal = Math.max(minScale, Math.min(maxScale, value));
  const pointerPercent = ((clampedVal - minScale) / (maxScale - minScale)) * 100;

  const isSafe = value <= 0.0;
  const isNeutral = value > 0.0 && value <= 0.05;
  const isDanger = value > 0.05;

  return (
    <div className="chart-container-card full-width-chart">
      <div className="sloan-meter-container" style={{ border: 'none', padding: 0, background: 'transparent' }}>
        <div className="sloan-meter-left" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {isSafe ? (
              <ShieldCheck size={20} color="var(--gain-green)" />
            ) : isNeutral ? (
              <AlertTriangle size={20} color="var(--warning-amber)" />
            ) : (
              <AlertOctagon size={20} color="var(--loss-red)" />
            )}
            <span style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-primary)' }}>
              Sloan Accrual Forensic Gauge: <span className="font-mono">{value > 0 ? `+${value}` : value}</span>
            </span>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
            Earnings Quality Status:{' '}
            <strong style={{ color: isSafe ? 'var(--gain-green)' : isNeutral ? 'var(--warning-amber)' : 'var(--loss-red)', fontSize: '12.5px' }}>
              {classification}
            </strong>
          </div>
        </div>

        <div className="sloan-meter-bar-wrapper" style={{ marginTop: '12px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)', marginBottom: '6px' }}>
            <span style={{ color: 'var(--gain-green)', fontWeight: '700' }}>&lt; 0.00 (Cash Backed / Highest Quality)</span>
            <span style={{ color: 'var(--warning-amber)', fontWeight: '700' }}>0.00 to +0.05 (Normal Accruals)</span>
            <span style={{ color: 'var(--loss-red)', fontWeight: '700' }}>&gt; +0.05 (High Risk Phantom Profits)</span>
          </div>

          {/* 3-Zone Track */}
          <div className="sloan-bar-track" style={{ height: '14px', borderRadius: '7px', position: 'relative', overflow: 'visible' }}>
            <div className="sloan-bar-segment safe" title="Safe Cash-Backed Zone" style={{ borderRadius: '7px 0 0 7px' }} />
            <div className="sloan-bar-segment neutral" title="Acceptable Accruals" />
            <div className="sloan-bar-segment danger" title="High Risk Accruals" style={{ borderRadius: '0 7px 7px 0' }} />

            {/* Pointer Marker */}
            <div style={{
              position: 'absolute',
              top: '-5px',
              bottom: '-5px',
              left: `${pointerPercent}%`,
              width: '6px',
              background: '#FFFFFF',
              borderRadius: '3px',
              boxShadow: '0 0 10px rgba(255, 255, 255, 1)',
              transform: 'translateX(-50%)',
              transition: 'left 0.5s ease',
              zIndex: 2
            }} />
          </div>
        </div>
      </div>
    </div>
  );
}
