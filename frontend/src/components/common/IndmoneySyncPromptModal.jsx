import React, { useState } from 'react';
import { X, ShieldCheck, Zap, TrendingUp, Lock, ArrowRight, RefreshCw } from 'lucide-react';
import { startIndmoneyAuthFlow } from './IndmoneySyncButton';

export default function IndmoneySyncPromptModal({ userId, isOpen, onClose, onSyncSuccess }) {
  const [isConnecting, setIsConnecting] = useState(false);

  if (!isOpen || !userId) return null;

  const handleConnect = async () => {
    setIsConnecting(true);
    await startIndmoneyAuthFlow(userId, () => {
      setIsConnecting(false);
      if (onSyncSuccess) onSyncSuccess();
      onClose();
    });
  };

  const handleDismiss = () => {
    // Save dismiss preference so it doesn't bother the user again for 2 days
    try {
      localStorage.setItem('stocktics_indmoney_prompt_dismissed', Date.now().toString());
    } catch {}
    onClose();
  };

  return (
    <div className="fintech-modal-overlay">
      <div className="fintech-modal-card indmoney-prompt-card">
        {/* Close Button */}
        <button className="fintech-modal-close-btn" onClick={handleDismiss}>
          <X size={18} />
        </button>

        {/* Header Icon */}
        <div className="indmoney-prompt-icon-wrapper">
          <div className="indmoney-prompt-icon">
            <Zap size={24} className="text-gain" />
          </div>
          <div className="indmoney-brand-tag">INDMONEY MCP INTEGRATION</div>
        </div>

        {/* Modal Title */}
        <h3 className="indmoney-prompt-title">Sync Your Portfolio with INDmoney</h3>
        <p className="indmoney-prompt-subtitle">
          Connect your INDmoney account once to automatically import your Indian & US stock holdings,
          average buy prices, quantities, and watchlists with <strong>zero manual logging</strong>.
        </p>

        {/* Benefits Grid */}
        <div className="indmoney-benefits-grid">
          <div className="benefit-item">
            <div className="benefit-bullet">
              <Zap size={14} className="text-gain" />
            </div>
            <div>
              <div className="benefit-title">Zero Manual Logging</div>
              <div className="benefit-desc">Live quantities, average buy prices, and P&L imported directly from your broker.</div>
            </div>
          </div>

          <div className="benefit-item">
            <div className="benefit-bullet">
              <TrendingUp size={14} className="text-gain" />
            </div>
            <div>
              <div className="benefit-title">Quant Watchlist Scanner</div>
              <div className="benefit-desc">Automatically feeds your INDmoney watchlists into our multi-pillar Buy Pipeline.</div>
            </div>
          </div>

          <div className="benefit-item">
            <div className="benefit-bullet">
              <ShieldCheck size={14} className="text-gain" />
            </div>
            <div>
              <div className="benefit-title">Time-to-Leave (TTL) Exit Risk</div>
              <div className="benefit-desc">Real-time alerts when your holdings approach Stop-Loss or Target profit targets.</div>
            </div>
          </div>

          <div className="benefit-item">
            <div className="benefit-bullet">
              <Lock size={14} className="text-gain" />
            </div>
            <div>
              <div className="benefit-title">100% Read-Only & SEBI Compliant</div>
              <div className="benefit-desc">No trading or transfer permissions. Powered by INDmoney's official OAuth 2.1 protocol.</div>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="indmoney-prompt-actions">
          <button
            className="indmoney-cta-primary"
            onClick={handleConnect}
            disabled={isConnecting}
          >
            {isConnecting ? (
              <>
                <RefreshCw size={16} className="spin-anim" />
                <span>Connecting INDmoney...</span>
              </>
            ) : (
              <>
                <span>Connect & Sync with INDmoney</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>

          <button className="indmoney-cta-secondary" onClick={handleDismiss}>
            Maybe Later
          </button>
        </div>
      </div>
    </div>
  );
}
