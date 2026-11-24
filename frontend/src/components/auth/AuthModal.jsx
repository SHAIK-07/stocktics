import React, { useState } from 'react';
import { X, Mail, Lock, AlertCircle, ArrowRight, ShieldCheck } from 'lucide-react';
import { authService } from '../../services/supabase';

export default function AuthModal({ isOpen, onClose, onSuccess, initialMode = 'signin' }) {
  const [mode, setMode] = useState(initialMode); // 'signin' | 'signup'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    const cleanEmail = email.trim().toLowerCase();
    if (!cleanEmail || !cleanEmail.includes('@')) {
      setError('Please enter a valid email address.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    if (mode === 'signup' && password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setIsLoading(true);
    try {
      const result = mode === 'signup'
        ? await authService.signUp(cleanEmail, password)
        : await authService.signIn(cleanEmail, password);

      if (result.success) {
        if (onSuccess) onSuccess(result.user);
        onClose();
      } else {
        setError(result.error || 'Authentication failed. Please check credentials.');
      }
    } catch (err) {
      setError(err.message || 'Network error connecting to Supabase.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fintech-modal-overlay" onClick={onClose}>
      <div className="fintech-modal-card" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="fintech-modal-header">
          <div className="fintech-modal-title-group">
            <div className="fintech-modal-icon-badge">
              <ShieldCheck size={20} className="text-emerald" />
            </div>
            <div>
              <h3 className="fintech-modal-title">
                {mode === 'signup' ? 'Create Stocktics Account' : 'Welcome to Stocktics'}
              </h3>
              <p className="fintech-modal-subtitle">
                Institutional Quant Intelligence & Portfolio Engine
              </p>
            </div>
          </div>
          <button className="fintech-modal-close-btn" onClick={onClose} aria-label="Close">
            <X size={18} />
          </button>
        </div>

        {/* Tab Switcher */}
        <div className="fintech-modal-tabs">
          <button
            type="button"
            className={`fintech-modal-tab ${mode === 'signin' ? 'active' : ''}`}
            onClick={() => { setMode('signin'); setError(null); }}
          >
            Sign In
          </button>
          <button
            type="button"
            className={`fintech-modal-tab ${mode === 'signup' ? 'active' : ''}`}
            onClick={() => { setMode('signup'); setError(null); }}
          >
            Sign Up
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="fintech-alert-box error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="fintech-modal-form">
          <div className="fintech-form-field">
            <label className="fintech-field-label">Email Address</label>
            <div className="fintech-input-container">
              <Mail size={16} className="fintech-input-icon" />
              <input
                type="email"
                className="fintech-input"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                required
              />
            </div>
          </div>

          <div className="fintech-form-field">
            <label className="fintech-field-label">Password</label>
            <div className="fintech-input-container">
              <Lock size={16} className="fintech-input-icon" />
              <input
                type="password"
                className="fintech-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
                required
              />
            </div>
          </div>

          {mode === 'signup' && (
            <div className="fintech-form-field">
              <label className="fintech-field-label">Confirm Password</label>
              <div className="fintech-input-container">
                <Lock size={16} className="fintech-input-icon" />
                <input
                  type="password"
                  className="fintech-input"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  autoComplete="new-password"
                  required
                />
              </div>
            </div>
          )}

          <button
            type="submit"
            className="fintech-btn-primary"
            disabled={isLoading}
            style={{ width: '100%', marginTop: '8px' }}
          >
            {isLoading ? (
              <span className="fintech-btn-loading">
                <span className="spinner-dots" /> Connecting to Supabase...
              </span>
            ) : (
              <>
                <span>{mode === 'signup' ? 'Create Free Account' : 'Sign In'}</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        {/* Footer info */}
        <div className="fintech-modal-footer">
          <p className="fintech-footer-text">
            {mode === 'signin' ? "Don't have an account yet?" : 'Already registered on Android or Web?'}
            <button
              type="button"
              className="fintech-link-btn"
              onClick={() => { setMode(mode === 'signin' ? 'signup' : 'signin'); setError(null); }}
            >
              {mode === 'signin' ? 'Create one now' : 'Sign In instead'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
