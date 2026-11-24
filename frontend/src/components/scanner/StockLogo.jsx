import React, { useState, useEffect } from 'react';

export default function StockLogo({ ticker, name, size = 32, className = '' }) {
  const [sourceIndex, setSourceIndex] = useState(0);

  // Reset source index when ticker changes
  useEffect(() => {
    setSourceIndex(0);
  }, [ticker]);

  if (!ticker) {
    return (
      <div
        className={`stock-logo-fallback ${className}`}
        style={{
          width: `${size}px`,
          height: `${size}px`,
          minWidth: `${size}px`,
          fontSize: `${Math.max(10, Math.floor(size * 0.38))}px`
        }}
      >
        ST
      </div>
    );
  }

  const rawTicker = ticker.toUpperCase().trim();
  const cleanTicker = rawTicker.replace('.NS', '').replace('.BO', '');
  const initial = cleanTicker ? cleanTicker.slice(0, 2).toUpperCase() : 'ST';
  const isIndian = rawTicker.endsWith('.NS') || rawTicker.endsWith('.BO');

  // Candidate sources in prioritized order: Parqet supports .NS for Indian stocks!
  const sources = isIndian
    ? [
        `https://assets.parqet.com/logos/symbol/${rawTicker}?format=png`,
        `https://s3-symbol-logo.tradingview.com/${cleanTicker.toLowerCase()}.svg`,
        `https://assets.parqet.com/logos/symbol/${cleanTicker}?format=png`
      ]
    : [
        `https://assets.parqet.com/logos/symbol/${cleanTicker}?format=png`,
        `https://s3-symbol-logo.tradingview.com/${cleanTicker.toLowerCase()}.svg`
      ];

  if (sourceIndex >= sources.length) {
    let hash = 0;
    for (let i = 0; i < cleanTicker.length; i++) {
      hash = cleanTicker.charCodeAt(i) + ((hash << 5) - hash);
    }
    const hue = Math.abs(hash % 360);

    return (
      <div
        className={`stock-logo-fallback ${className}`}
        style={{
          width: `${size}px`,
          height: `${size}px`,
          minWidth: `${size}px`,
          fontSize: `${Math.max(10, Math.floor(size * 0.38))}px`,
          background: `linear-gradient(135deg, hsl(${hue}, 40%, 20%), hsl(${hue}, 55%, 12%))`,
          color: `hsl(${hue}, 85%, 70%)`,
          border: `1px solid hsl(${hue}, 45%, 28%)`
        }}
        title={name || ticker}
      >
        {initial}
      </div>
    );
  }

  return (
    <img
      key={`${rawTicker}-${sourceIndex}`}
      src={sources[sourceIndex]}
      alt={name || ticker}
      className={`stock-logo-img ${className}`}
      style={{
        width: `${size}px`,
        height: `${size}px`,
        minWidth: `${size}px`
      }}
      onError={() => setSourceIndex((prev) => prev + 1)}
      loading="lazy"
    />
  );
}
