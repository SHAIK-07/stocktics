import React, { useState, useEffect } from 'react';
import { X, TrendingUp, ShieldAlert, Target, Clock, CheckCircle2 } from 'lucide-react';
import { portfolioService, authService } from '../../services/supabase';

export default function LogBuyModal({ isOpen, onClose, stock, onSuccess }) {
  const user = authService.getCurrentUser();
  const rawPrice = stock?.price != null ? (typeof stock.price === 'number' ? stock.price : parseFloat(String(stock.price).replace(/[^0-9.]/g, ''))) : 100;
  const isIndian = stock?.symbol?.endsWith('.NS') || stock?.symbol?.endsWith('.BO') || stock?.exchange === 'NSE' || stock?.exchange === 'BSE';
  const currencySymbol = isIndian ? '₹' : '$';

  const [price, setPrice] = useState('100.00');
  const [qty, setQty] = useState('1');
  const [stopLossPct, setStopLossPct] = useState('5.0');
  const [targetPct, setTargetPct] = useState('15.0');
  const [trailingStopPct, setTrailingStopPct] = useState('4.0');
  const [maxHoldDays, setMaxHoldDays] = useState('60');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    if (stock) {
      const p = stock.price != null ? (typeof stock.price === 'number' ? stock.price : parseFloat(String(stock.price).replace(/[^0-9.]/g, ''))) : 100;
      setPrice(p > 0 ? p.toFixed(2) : '100.00');
    }
  }, [stock]);

  if (!isOpen || !stock) return null;

  const numPrice = parseFloat(price) || 0;
  const numQty = parseFloat(qty) || 0;
  const numSlPct = parseFloat(stopLossPct) || 0;
  const numTgtPct = parseFloat(targetPct) || 0;


  const totalAmount = numPrice * numQty;
  const targetPrice = numPrice * (1 + numTgtPct / 100);
  const stopLossPrice = numPrice * (1 - numSlPct / 100);
  const isValid = numPrice > 0 && numQty > 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isValid || isSubmitting) return;

    setIsSubmitting(true);
    try {
      const holdingData = {
        symbol: stock.symbol,
        exchange: stock.exchange || (isIndian ? 'NSE' : 'NASDAQ'),
        entryPrice: numPrice,
        qty: numQty,
        stopLossPct: numSlPct,
        targetPct: numTgtPct,
        trailingStopPct: parseFloat(trailingStopPct) || 4.0,
        maxHoldDays: parseInt(maxHoldDays, 10) || 60
      };

      const success = await portfolioService.syncBuyPosition(user?.userId, holdingData);
      if (success) {
        setSuccessMsg(`Successfully logged buy position for ${stock.symbol}!`);
        setTimeout(() => {
          if (onSuccess) onSuccess();
          onClose();
        }, 900);
      }
    } catch (err) {
      console.error('Failed to log buy:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fintech-modal-overlay" onClick={onClose}>
      <div className="fintech-modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '520px' }}>
        {/* Header */}
        <div className="fintech-modal-header">
          <div className="fintech-modal-title-group">
            <div className="company-logo-badge">
              <img
                src={stock.logoUrl || `https://assets.parqet.com/logos/symbol/${stock.symbol}?format=png`}
                alt={stock.symbol}
                onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
              />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <h3 className="fintech-modal-title font-mono">{stock.symbol}</h3>
                <span className="exchange-badge">{stock.exchange || (isIndian ? 'NSE' : 'NASDAQ')}</span>
              </div>
              <p className="fintech-modal-subtitle">{stock.name || 'Equity Holding'}</p>
            </div>
          </div>
          <button className="fintech-modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {successMsg ? (
          <div className="fintech-success-state">
            <CheckCircle2 size={44} className="text-emerald" />
            <h4>Position Logged</h4>
            <p>{successMsg}</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="fintech-modal-form">
            {/* Price & Quantity */}
            <div className="fintech-grid-2">
              <div className="fintech-form-field">
                <label className="fintech-field-label">Buy Price ({currencySymbol})</label>
                <div className="fintech-input-container">
                  <input
                    type="number"
                    step="any"
                    className="fintech-input font-mono"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="fintech-form-field">
                <label className="fintech-field-label">Quantity (Shares)</label>
                <div className="fintech-input-container">
                  <input
                    type="number"
                    step="any"
                    className="fintech-input font-mono"
                    value={qty}
                    onChange={(e) => setQty(e.target.value)}
                    required
                  />
                </div>
              </div>
            </div>

            {/* Total Investment preview */}
            <div className="fintech-summary-banner">
              <div>
                <span className="summary-label">Total Investment</span>
                <div className="summary-value font-mono">
                  {currencySymbol} {totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className="summary-label">Execution Strategy</span>
                <div className="summary-sub">Quant Engine Monitored</div>
              </div>
            </div>

            {/* Risk Controls: Stop Loss & Target */}
            <div className="fintech-grid-2">
              <div className="fintech-form-field">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="fintech-field-label">Stop-Loss %</label>
                  <span className="fintech-field-hint font-mono text-loss">
                    {currencySymbol}{stopLossPrice.toFixed(2)}
                  </span>
                </div>
                <div className="fintech-input-container">
                  <ShieldAlert size={15} className="text-loss" />
                  <input
                    type="number"
                    step="0.1"
                    className="fintech-input font-mono"
                    value={stopLossPct}
                    onChange={(e) => setStopLossPct(e.target.value)}
                  />
                  <span className="input-suffix">%</span>
                </div>
              </div>

              <div className="fintech-form-field">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="fintech-field-label">Profit Target %</label>
                  <span className="fintech-field-hint font-mono text-gain">
                    {currencySymbol}{targetPrice.toFixed(2)}
                  </span>
                </div>
                <div className="fintech-input-container">
                  <Target size={15} className="text-gain" />
                  <input
                    type="number"
                    step="0.1"
                    className="fintech-input font-mono"
                    value={targetPct}
                    onChange={(e) => setTargetPct(e.target.value)}
                  />
                  <span className="input-suffix">%</span>
                </div>
              </div>
            </div>

            {/* Advanced Exit Controls */}
            <div className="fintech-grid-2">
              <div className="fintech-form-field">
                <label className="fintech-field-label">Trailing Stop %</label>
                <div className="fintech-input-container">
                  <input
                    type="number"
                    step="0.1"
                    className="fintech-input font-mono"
                    value={trailingStopPct}
                    onChange={(e) => setTrailingStopPct(e.target.value)}
                  />
                  <span className="input-suffix">%</span>
                </div>
              </div>

              <div className="fintech-form-field">
                <label className="fintech-field-label">Max Hold Horizon (Days)</label>
                <div className="fintech-input-container">
                  <Clock size={15} />
                  <input
                    type="number"
                    className="fintech-input font-mono"
                    value={maxHoldDays}
                    onChange={(e) => setMaxHoldDays(e.target.value)}
                  />
                  <span className="input-suffix">days</span>
                </div>
              </div>
            </div>

            {/* Submit button */}
            <button
              type="submit"
              className="fintech-btn-primary"
              disabled={!isValid || isSubmitting}
              style={{ width: '100%', marginTop: '12px' }}
            >
              {isSubmitting ? (
                <span>Syncing to Supabase...</span>
              ) : (
                <>
                  <TrendingUp size={16} />
                  <span>Log Buy Position</span>
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
