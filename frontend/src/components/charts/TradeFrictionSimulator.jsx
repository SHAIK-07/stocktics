import React, { useState } from 'react';
import { Calculator, ArrowRight, ShieldAlert } from 'lucide-react';

export default function TradeFrictionSimulator({ stockPrice, currency = 'USD' }) {
  const [capital, setCapital] = useState(10000);
  const [fxMarkupPct, setFxMarkupPct] = useState(1.2); // 1.2% FX drag for cross-border
  const [spreadBps, setSpreadBps] = useState(8);

  const isUsd = currency.toUpperCase() === 'USD';
  const halfSpreadDollars = (stockPrice * (spreadBps / 10000)) / 2;
  const effectiveEntryPrice = stockPrice + halfSpreadDollars;
  const sharesBought = capital / effectiveEntryPrice;
  const slippageLoss = sharesBought * halfSpreadDollars;
  const fxDragLoss = isUsd ? (capital * (fxMarkupPct / 100)) : 0;
  const totalFriction = slippageLoss + fxDragLoss;
  const breakevenGainPct = (totalFriction / capital) * 100;

  return (
    <div className="chart-container-card" style={{ marginTop: '16px' }}>
      <div className="chart-header">
        <div className="chart-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Calculator size={16} color="var(--fintech-primary)" />
          <span>Interactive Trade Friction & True Breakeven Simulator (Lesson 1)</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
        <div>
          <label style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontWeight: '600', textTransform: 'uppercase' }}>
            Investment Capital ({isUsd ? '$ USD' : '₹ INR'})
          </label>
          <input
            type="number"
            value={capital}
            onChange={(e) => setCapital(Math.max(100, Number(e.target.value)))}
            style={{
              width: '100%',
              height: '38px',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-hairline)',
              borderRadius: '8px',
              color: 'var(--text-primary)',
              padding: '0 10px',
              fontFamily: 'var(--font-mono)',
              marginTop: '4px'
            }}
          />
        </div>

        <div>
          <label style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontWeight: '600', textTransform: 'uppercase' }}>
            Bid-Ask Spread ({spreadBps} bps)
          </label>
          <input
            type="range"
            min="2"
            max="40"
            value={spreadBps}
            onChange={(e) => setSpreadBps(Number(e.target.value))}
            style={{ width: '100%', marginTop: '12px' }}
          />
        </div>

        {isUsd && (
          <div>
            <label style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontWeight: '600', textTransform: 'uppercase' }}>
              International FX Markup ({fxMarkupPct}%)
            </label>
            <input
              type="range"
              min="0"
              max="2.5"
              step="0.1"
              value={fxMarkupPct}
              onChange={(e) => setFxMarkupPct(Number(e.target.value))}
              style={{ width: '100%', marginTop: '12px' }}
            />
          </div>
        )}
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '12px',
        padding: '12px',
        background: 'var(--bg-surface-elevated)',
        borderRadius: '8px',
        border: '1px solid var(--border-subtle)'
      }}>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>Effective Entry Price</div>
          <div style={{ fontSize: '14px', fontWeight: '700', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
            {isUsd ? '$' : '₹'}{effectiveEntryPrice.toFixed(2)}
          </div>
        </div>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>Slippage + FX Friction Drag</div>
          <div style={{ fontSize: '14px', fontWeight: '700', color: 'var(--loss-red)', fontFamily: 'var(--font-mono)' }}>
            -{isUsd ? '$' : '₹'}{totalFriction.toFixed(2)}
          </div>
        </div>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>Required Breakeven Gain</div>
          <div style={{ fontSize: '14px', fontWeight: '700', color: 'var(--gain-green)', fontFamily: 'var(--font-mono)' }}>
            +{breakevenGainPct.toFixed(2)}% to Breakeven
          </div>
        </div>
      </div>
    </div>
  );
}
