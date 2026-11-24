import React, { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, Building, ShieldCheck, Award } from 'lucide-react';

export default function StockBanner({ stockData }) {
  if (!stockData) return null;

  const {
    ticker,
    companyName,
    exchange,
    currency,
    currencySymbol,
    formattedPrice,
    currentPrice,
    change,
    changePercent,
    isUp,
    formattedMarketCap,
    sector,
    industry,
    website,
    logoUrl,
    scorecard
  } = stockData;

  const grade = scorecard?.grade || 'B';
  const compositeScore = scorecard?.compositeScore || 60;
  const rating = scorecard?.rating || 'INSTITUTIONAL COMPOUNDER';

  const getGradeClass = () => {
    if (grade.startsWith('A')) return 'grade-a';
    if (grade === 'B') return 'grade-b';
    if (grade === 'C') return 'grade-c';
    return 'grade-d';
  };

  const symbolInitials = ticker.replace(/[^A-Za-z]/g, '').slice(0, 2).toUpperCase();

  // Multi-tier logo resolution (Parqet official symbol logo -> Google favicon -> Initials)
  const initialLogoSrc = logoUrl || `https://assets.parqet.com/logos/symbol/${ticker}?format=png`;
  const [logoState, setLogoState] = useState({
    src: initialLogoSrc,
    stage: 0 // 0 = primary symbol logo, 1 = domain favicon fallback, 2 = initials fallback
  });

  useEffect(() => {
    setLogoState({
      src: logoUrl || `https://assets.parqet.com/logos/symbol/${ticker}?format=png`,
      stage: 0
    });
  }, [ticker, logoUrl]);

  const handleLogoError = () => {
    if (logoState.stage === 0) {
      let domain = '';
      if (website) {
        try {
          domain = new URL(website).hostname.replace(/^www\./, '');
        } catch {
          domain = '';
        }
      }
      if (domain) {
        setLogoState({
          src: `https://www.google.com/s2/favicons?domain=${domain}&sz=128`,
          stage: 1
        });
        return;
      }
    }
    setLogoState({ src: null, stage: 2 });
  };

  return (
    <div className="stock-banner-card">
      <div className="banner-top-row">
        {/* Left: Company Logo & Ticker Info */}
        <div className="company-title-block">
          <div className="company-avatar-badge">
            {logoState.stage < 2 && logoState.src ? (
              <img
                src={logoState.src}
                alt={`${companyName} Logo`}
                className="company-logo-img"
                onError={handleLogoError}
                loading="eager"
              />
            ) : (
              <span className="symbol-initials-text">{symbolInitials}</span>
            )}
          </div>

          <div className="company-meta-details">
            <div className="symbol-row">
              <span className="stock-symbol font-mono">{ticker}</span>
              <span className="exchange-chip">{exchange}</span>
              <span className="currency-chip">
                {currency} ({currencySymbol})
              </span>
            </div>

            <div className="company-name">{companyName}</div>

            <div className="sector-row">
              <Building size={13} />
              <span>{sector}</span>
              <span>•</span>
              <span>{industry}</span>
            </div>
          </div>
        </div>

        {/* Middle-Right: Composite Institutional Scorecard */}
        <div className="institutional-badge-card">
          <div className={`grade-circle ${getGradeClass()}`}>
            {grade}
          </div>
          <div className="rating-info">
            <div className="rating-title">{rating}</div>
            <div className="rating-subtitle">
              Audit Score: <strong style={{ color: 'var(--text-primary)' }}>{compositeScore}/100</strong>
            </div>
            <div className="rating-meta">
              {scorecard?.positiveCount} of {scorecard?.totalCount} Indicators Passed
            </div>
          </div>
        </div>

        {/* Far Right: Live Price & Action Buttons */}
        <div className="price-block">
          <div className="price-main font-mono tabular-num">
            {formattedPrice}
          </div>
          <div className={`change-pill ${isUp ? 'positive' : 'negative'}`}>
            {isUp ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
            <span className="font-mono">
              {isUp ? '+' : ''}{change} ({isUp ? '+' : ''}{changePercent}%)
            </span>
          </div>
        </div>
      </div>

      {/* Quick Summary Grid */}
      <div className="banner-stats-grid">
        <div className="stat-item">
          <span className="stat-label">Market Capitalization</span>
          <span className="stat-value font-mono">{formattedMarketCap}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Primary Exchange</span>
          <span className="stat-value font-mono">{exchange} ({currency})</span>
        </div>
      </div>
    </div>
  );
}
