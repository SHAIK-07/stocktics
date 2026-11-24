import React, { useState, useEffect } from 'react';
import { X, Save, Edit3, ShieldAlert, Target, Clock } from 'lucide-react';
import { portfolioService, authService } from '../../services/supabase';

export default function EditHoldingModal({ isOpen, onClose, holding, onSuccess }) {
  const user = authService.getCurrentUser();
  const currencySymbol = holding?.currency || (holding?.isIndian ? '₹' : '$');

  const [price, setPrice] = useState('100');
  const [qty, setQty] = useState('1');
  const [stopLossPct, setStopLossPct] = useState('5.0');
  const [targetPct, setTargetPct] = useState('15.0');
  const [trailingStopPct, setTrailingStopPct] = useState('4.0');
  const [maxHoldDays, setMaxHoldDays] = useState('60');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (holding) {
      setPrice(String(holding.entryPrice || '100'));
      setQty(String(holding.qty || '1'));
      setStopLossPct(String(holding.stopLossPct || '5.0'));
      setTargetPct(String(holding.targetPct || '15.0'));
      setTrailingStopPct(String(holding.trailingStopPct || '4.0'));
      setMaxHoldDays(String(holding.maxHoldDays || '60'));
    }
  }, [holding]);

  if (!isOpen || !holding) return null;


  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      const updatedHolding = {
        ...holding,
        entryPrice: parseFloat(price) || holding.entryPrice,
        qty: parseFloat(qty) || holding.qty,
        stopLossPct: parseFloat(stopLossPct) || 5,
        targetPct: parseFloat(targetPct) || 15,
        trailingStopPct: parseFloat(trailingStopPct) || 4,
        maxHoldDays: parseInt(maxHoldDays, 10) || 60
      };

      const success = await portfolioService.syncUpdatePosition(user?.userId, updatedHolding);
      if (success) {
        if (onSuccess) onSuccess();
        onClose();
      }
    } catch (err) {
      console.error('Failed to update holding:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fintech-modal-overlay" onClick={onClose}>
      <div className="fintech-modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '500px' }}>
        <div className="fintech-modal-header">
          <div className="fintech-modal-title-group">
            <div className="fintech-modal-icon-badge">
              <Edit3 size={18} className="text-emerald" />
            </div>
            <div>
              <h3 className="fintech-modal-title">Edit {holding.symbol} Position</h3>
              <p className="fintech-modal-subtitle">Adjust risk rules and position metrics</p>
            </div>
          </div>
          <button className="fintech-modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="fintech-modal-form">
          <div className="fintech-grid-2">
            <div className="fintech-form-field">
              <label className="fintech-field-label">Entry Price ({currencySymbol})</label>
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
              <label className="fintech-field-label">Quantity</label>
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

          <div className="fintech-grid-2">
            <div className="fintech-form-field">
              <label className="fintech-field-label">Stop-Loss %</label>
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
              <label className="fintech-field-label">Target %</label>
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

          <button
            type="submit"
            className="fintech-btn-primary"
            disabled={isSubmitting}
            style={{ width: '100%', marginTop: '10px' }}
          >
            {isSubmitting ? (
              <span>Saving...</span>
            ) : (
              <>
                <Save size={16} />
                <span>Save Position Rules</span>
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
