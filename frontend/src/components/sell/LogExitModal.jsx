import React, { useState, useEffect } from 'react';
import { X, TrendingDown, CheckCircle2 } from 'lucide-react';
import { portfolioService, authService } from '../../services/supabase';

export default function LogExitModal({ isOpen, onClose, holding, onSuccess }) {
  const user = authService.getCurrentUser();
  const maxQty = parseFloat(holding?.qty) || 1;
  const entryPrice = parseFloat(holding?.entryPrice) || 0;
  const currentPrice = parseFloat(holding?.currentPrice) || entryPrice;
  const currencySymbol = holding?.currency || (holding?.isIndian ? '₹' : '$');

  const [exitType, setExitType] = useState('full'); // 'full' | 'partial'
  const [exitQty, setExitQty] = useState('1');
  const [exitPrice, setExitPrice] = useState('100.00');
  const [exitReason, setExitReason] = useState('manual');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    if (holding) {
      const mq = parseFloat(holding.qty) || 1;
      const cp = parseFloat(holding.currentPrice) || parseFloat(holding.entryPrice) || 0;
      setExitQty(String(mq));
      setExitPrice(cp > 0 ? cp.toFixed(2) : '100.00');
    }
  }, [holding]);

  if (!isOpen || !holding) return null;

  const numExitQty = exitType === 'full' ? maxQty : Math.min(parseFloat(exitQty) || 0, maxQty);
  const numExitPrice = parseFloat(exitPrice) || 0;
  const totalProceeds = numExitPrice * numExitQty;
  const totalCost = entryPrice * numExitQty;
  const realizedPnl = totalProceeds - totalCost;

  const realizedPnlPct = entryPrice > 0 ? ((numExitPrice - entryPrice) / entryPrice) * 100 : 0;
  const isPositive = realizedPnl >= 0;
  const isFullExit = exitType === 'full' || numExitQty >= maxQty;
  const remainingQty = Math.max(0, maxQty - numExitQty);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (numExitPrice <= 0 || numExitQty <= 0 || isSubmitting) return;

    setIsSubmitting(true);
    try {
      const exitData = {
        symbol: holding.symbol,
        exchange: holding.exchange,
        exitPrice: numExitPrice,
        exitQty: numExitQty,
        exitReason,
        realizedPnl,
        isFullExit,
        remainingQty
      };

      const success = await portfolioService.syncExitPosition(user?.userId, exitData);
      if (success) {
        setSuccessMsg(`Successfully logged exit for ${holding.symbol}!`);
        setTimeout(() => {
          if (onSuccess) onSuccess();
          onClose();
        }, 900);
      }
    } catch (err) {
      console.error('Failed to log exit:', err);
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
                src={holding.logoUrl || `https://assets.parqet.com/logos/symbol/${holding.symbol}?format=png`}
                alt={holding.symbol}
                onError={(e) => { e.target.src = '/stocktics_logo.png'; }}
              />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <h3 className="fintech-modal-title font-mono">{holding.symbol}</h3>
                <span className="exchange-badge">{holding.exchange}</span>
              </div>
              <p className="fintech-modal-subtitle">Log Position Exit / Sell</p>
            </div>
          </div>
          <button className="fintech-modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {successMsg ? (
          <div className="fintech-success-state">
            <CheckCircle2 size={44} className="text-emerald" />
            <h4>Exit Recorded</h4>
            <p>{successMsg}</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="fintech-modal-form">
            {/* Exit Mode Selector */}
            <div className="fintech-modal-tabs" style={{ marginBottom: '16px' }}>
              <button
                type="button"
                className={`fintech-modal-tab ${exitType === 'full' ? 'active' : ''}`}
                onClick={() => { setExitType('full'); setExitQty(String(maxQty)); }}
              >
                Full Exit ({maxQty} Shares)
              </button>
              <button
                type="button"
                className={`fintech-modal-tab ${exitType === 'partial' ? 'active' : ''}`}
                onClick={() => setExitType('partial')}
              >
                Partial Exit
              </button>
            </div>

            {/* Price & Quantity Grid */}
            <div className="fintech-grid-2">
              <div className="fintech-form-field">
                <label className="fintech-field-label">Exit Price ({currencySymbol})</label>
                <div className="fintech-input-container">
                  <input
                    type="number"
                    step="any"
                    className="fintech-input font-mono"
                    value={exitPrice}
                    onChange={(e) => setExitPrice(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="fintech-form-field">
                <label className="fintech-field-label">Quantity to Sell</label>
                <div className="fintech-input-container">
                  <input
                    type="number"
                    step="any"
                    max={maxQty}
                    className="fintech-input font-mono"
                    value={exitType === 'full' ? maxQty : exitQty}
                    onChange={(e) => setExitQty(e.target.value)}
                    disabled={exitType === 'full'}
                    required
                  />
                </div>
              </div>
            </div>

            {/* Exit Reason */}
            <div className="fintech-form-field">
              <label className="fintech-field-label">Exit Trigger / Rationale</label>
              <div className="fintech-input-container">
                <select
                  className="fintech-input"
                  value={exitReason}
                  onChange={(e) => setExitReason(e.target.value)}
                >
                  <option value="manual">Manual Discretionary Exit</option>
                  <option value="target">Profit Target Achieved (+15%)</option>
                  <option value="stop_loss">Stop-Loss Triggered (-5%)</option>
                  <option value="trailing_stop">Trailing Stop Dynamic Invalidation</option>
                  <option value="model_sell">Quant Engine TTL Exit Signal</option>
                </select>
              </div>
            </div>

            {/* Realized P&L summary banner */}
            <div className={`fintech-pnl-summary-card ${isPositive ? 'gain' : 'loss'}`}>
              <div>
                <span className="summary-label">Realized Profit / Loss</span>
                <div className="summary-value font-mono">
                  {isPositive ? '+' : ''}{currencySymbol}{realizedPnl.toFixed(2)} ({isPositive ? '+' : ''}{realizedPnlPct.toFixed(2)}%)
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className="summary-label">Total Proceeds</span>
                <div className="summary-sub font-mono">
                  {currencySymbol}{totalProceeds.toFixed(2)}
                </div>
              </div>
            </div>

            {/* Submit button */}
            <button
              type="submit"
              className="fintech-btn-danger"
              disabled={numExitPrice <= 0 || numExitQty <= 0 || isSubmitting}
              style={{ width: '100%', marginTop: '12px' }}
            >
              {isSubmitting ? (
                <span>Syncing Exit to Supabase...</span>
              ) : (
                <>
                  <TrendingDown size={16} />
                  <span>Confirm Exit & Close Position</span>
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
