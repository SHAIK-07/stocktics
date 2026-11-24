import React, { useState, useEffect, useRef } from 'react';
import { Newspaper, ChevronLeft, ChevronRight, ExternalLink, TrendingUp, TrendingDown, Minus, Zap, Globe } from 'lucide-react';
import { fetchStockNews } from '../../services/api';

export default function NewsSentimentModule({ symbol, companyName = '' }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('indmoney'); // 'indmoney' or 'yahoo'
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [activeCardIndex, setActiveCardIndex] = useState(0);
  const scrollRef = useRef(null);

  useEffect(() => {
    let isMounted = true;
    async function loadNews() {
      if (!symbol) return;
      setLoading(true);
      try {
        const res = await fetchStockNews(symbol);
        if (isMounted && res) {
          setData(res);
        }
      } catch (err) {
        console.error('Failed to load news sentiment:', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    }
    loadNews();
    return () => { isMounted = false; };
  }, [symbol]);

  const handleScroll = () => {
    if (scrollRef.current) {
      const { scrollLeft, clientWidth } = scrollRef.current;
      const idx = Math.round(scrollLeft / 310);
      setActiveCardIndex(idx);
    }
  };

  const scroll = (direction) => {
    if (scrollRef.current) {
      const scrollAmount = 310;
      scrollRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth'
      });
    }
  };

  if (loading) {
    return (
      <div style={{
        background: '#141923',
        border: '1px solid #222A3B',
        borderRadius: '14px',
        padding: '36px 24px',
        color: '#94A3B8',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '12px'
      }}>
        <div style={{
          width: '32px',
          height: '32px',
          border: '3px solid rgba(0, 208, 156, 0.2)',
          borderTopColor: '#00D09C',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite'
        }} />
        <div style={{ fontWeight: 600, color: '#F8FAFC', fontSize: '13.5px' }}>
          Fetching INDmoney Catalysts & Sentiment...
        </div>
        <div style={{ color: '#64748B', fontSize: '11px' }}>
          Scanning corporate filings and institutional event wire for {symbol}
        </div>
      </div>
    );
  }

  // Extract INDmoney events (Strict: never show Yahoo articles in INDmoney)
  const indmoneyData = data?.indmoney || {};
  const rawIndmoneyList = indmoneyData.key_events || data?.indmoney_news || [];
  const indmoneyList = (rawIndmoneyList || []).filter(item => {
    const pub = (item.publisher || '').toLowerCase();
    return pub.includes('indmoney') || pub.includes('ind money') || (!pub.includes('yahoo') && item.description);
  });
  const indSummary = indmoneyData.sentiment_summary || data?.sentiment_summary || {
    positive_count: indmoneyList.filter(a => (a.sentiment || '').toLowerCase() === 'positive').length,
    neutral_count: indmoneyList.filter(a => (a.sentiment || '').toLowerCase() === 'neutral').length,
    negative_count: indmoneyList.filter(a => (a.sentiment || '').toLowerCase() === 'negative').length,
    total_count: indmoneyList.length,
    label: ''
  };

  // Extract Yahoo news
  const yahooData = data?.yahoo || {};
  const yahooList = yahooData.articles || data?.yahoo_news || [];

  const filteredIndmoney = selectedFilter === 'all'
    ? indmoneyList
    : indmoneyList.filter(n => (n.sentiment || '').toLowerCase() === selectedFilter);

  const getSentimentTheme = (sentiment) => {
    switch ((sentiment || '').toLowerCase()) {
      case 'positive':
        return {
          text: '#00D09C',
          bg: '#0D1F18',
          border: '#1B4D3B',
          badgeBg: 'rgba(0, 208, 156, 0.12)',
          icon: <TrendingUp size={14} color="#FFFFFF" />
        };
      case 'negative':
        return {
          text: '#F04438',
          bg: '#261214',
          border: '#5A1E22',
          badgeBg: 'rgba(240, 68, 56, 0.12)',
          icon: <TrendingDown size={14} color="#FFFFFF" />
        };
      default:
        return {
          text: '#1455FE',
          bg: '#141923',
          border: '#222A3B',
          badgeBg: 'rgba(20, 85, 254, 0.12)',
          icon: <Minus size={14} color="#FFFFFF" />
        };
    }
  };

  const total = Math.max(1, indSummary.total_count || indmoneyList.length);
  const posPct = ((indSummary.positive_count / total) * 100).toFixed(0);
  const neuPct = ((indSummary.neutral_count / total) * 100).toFixed(0);
  const negPct = ((indSummary.negative_count / total) * 100).toFixed(0);

  return (
    <div style={{
      background: '#141923',
      border: '1px solid #222A3B',
      borderRadius: '16px',
      padding: '20px',
      color: '#F8FAFC',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
    }}>
      {/* ── Dual Source Switcher Bar ── */}
      <div style={{
        display: 'flex',
        gap: '6px',
        background: '#1C2333',
        padding: '4px',
        borderRadius: '10px',
        border: '1px solid #222A3B',
        marginBottom: '18px'
      }}>
        <button
          onClick={() => setActiveTab('indmoney')}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            padding: '9px 14px',
            borderRadius: '8px',
            border: 'none',
            background: activeTab === 'indmoney' ? '#00D09C' : 'transparent',
            color: activeTab === 'indmoney' ? '#0B0E14' : '#94A3B8',
            fontSize: '12.5px',
            fontWeight: activeTab === 'indmoney' ? '700' : '500',
            cursor: 'pointer',
            transition: 'all 0.2s'
          }}
        >
          <Zap size={15} />
          INDmoney Key Events ({indmoneyList.length})
        </button>

        <button
          onClick={() => setActiveTab('yahoo')}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            padding: '9px 14px',
            borderRadius: '8px',
            border: 'none',
            background: activeTab === 'yahoo' ? '#1455FE' : 'transparent',
            color: activeTab === 'yahoo' ? '#FFFFFF' : '#94A3B8',
            fontSize: '12.5px',
            fontWeight: activeTab === 'yahoo' ? '700' : '500',
            cursor: 'pointer',
            transition: 'all 0.2s'
          }}
        >
          <Globe size={15} />
          Yahoo Finance News ({yahooList.length})
        </button>
      </div>

      {/* ========================================================================= */}
      {/* VIEW 1: INDMONEY KEY EVENTS (Native INDmoney Carousel & Catalysts)       */}
      {/* ========================================================================= */}
      {activeTab === 'indmoney' && (
        <div>
          {/* Header Row */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <h3 style={{ margin: 0, fontSize: '17px', fontWeight: '700', color: '#F8FAFC' }}>
                Key events for {symbol}
              </h3>
              <span style={{ fontSize: '11px', color: '#94A3B8' }}>
                Curated institutional catalysts • Sentiment analysis
              </span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{
                background: 'rgba(0, 208, 156, 0.12)',
                color: '#00D09C',
                border: '1px solid rgba(0, 208, 156, 0.4)',
                borderRadius: '6px',
                padding: '4px 10px',
                fontSize: '11px',
                fontWeight: '700'
              }}>
                {indmoneyList.length} Events
              </span>

              <div style={{ display: 'flex', gap: '4px' }}>
                <button
                  onClick={() => scroll('left')}
                  style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '6px',
                    background: '#1C2333',
                    border: '1px solid #222A3B',
                    color: '#F8FAFC',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer'
                  }}
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  onClick={() => scroll('right')}
                  style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '6px',
                    background: '#1C2333',
                    border: '1px solid #222A3B',
                    color: '#F8FAFC',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer'
                  }}
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          </div>

          {/* ── Native INDmoney Style Carousel Cards ── */}
          {indmoneyList.length > 0 ? (
            <div style={{ marginBottom: '16px' }}>
              <div
                ref={scrollRef}
                onScroll={handleScroll}
                style={{
                  display: 'flex',
                  gap: '14px',
                  overflowX: 'auto',
                  paddingBottom: '8px',
                  scrollbarWidth: 'none',
                  scrollSnapType: 'x mandatory'
                }}
              >
                {indmoneyList.map((event, idx) => {
                  const theme = getSentimentTheme(event.sentiment);
                  return (
                    <div
                      key={idx}
                      style={{
                        minWidth: '310px',
                        maxWidth: '310px',
                        background: theme.bg,
                        border: `1px solid ${theme.border}`,
                        borderRadius: '14px',
                        padding: '16px',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between',
                        scrollSnapAlign: 'start',
                        flexShrink: 0,
                        transition: 'transform 0.2s',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.2)'
                      }}
                    >
                      <div>
                        {/* Circular Trend Badge */}
                        <div style={{
                          width: '28px',
                          height: '28px',
                          borderRadius: '50%',
                          background: theme.text,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          marginBottom: '12px'
                        }}>
                          {theme.icon}
                        </div>

                        {/* Title formatted with date: "Title - DD Mon, YYYY" */}
                        <h4 style={{
                          margin: '0 0 10px 0',
                          fontSize: '14.5px',
                          fontWeight: '700',
                          color: '#F8FAFC',
                          lineHeight: '1.4'
                        }}>
                          {event.title}
                        </h4>

                        {/* Full Narrative Explanation */}
                        <p style={{
                          margin: 0,
                          fontSize: '12px',
                          color: '#94A3B8',
                          lineHeight: '1.55',
                          display: '-webkit-box',
                          WebkitLineClamp: 5,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden'
                        }}>
                          {event.description || `Verified institutional catalyst regarding ${event.title}.`}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Pager Dots Indicator */}
              <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: '6px',
                marginTop: '10px'
              }}>
                {indmoneyList.slice(0, 6).map((_, i) => (
                  <span
                    key={i}
                    style={{
                      width: i === activeCardIndex ? '7px' : '5px',
                      height: i === activeCardIndex ? '7px' : '5px',
                      borderRadius: '50%',
                      background: i === activeCardIndex ? '#3B82F6' : '#475569',
                      transition: 'all 0.2s'
                    }}
                  />
                ))}
              </div>
            </div>
          ) : (
            <div style={{ color: '#94A3B8', fontSize: '13px', padding: '16px', textAlign: 'center' }}>
              No Key Events available for {symbol} at this time.
            </div>
          )}

          {/* ── Sentiment Summary Bar ── */}
          <div style={{
            background: '#0B0E14',
            border: '1px solid #222A3B',
            borderRadius: '12px',
            padding: '14px',
            marginBottom: '16px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '12px', fontWeight: '600', color: '#94A3B8' }}>
                Sentiment Distribution
              </span>
              <span style={{ fontSize: '11px', fontWeight: '700', color: '#F8FAFC' }}>
                {indSummary.label || `${indSummary.negative_count} negative • ${indSummary.neutral_count} neutral • ${indSummary.positive_count} positive`}
              </span>
            </div>

            {/* 3-Color Bar */}
            <div style={{
              display: 'flex',
              height: '8px',
              borderRadius: '4px',
              overflow: 'hidden',
              background: '#1C2333',
              marginBottom: '8px'
            }}>
              {indSummary.positive_count > 0 && (
                <div style={{ width: `${posPct}%`, background: '#00D09C' }} />
              )}
              {indSummary.neutral_count > 0 && (
                <div style={{ width: `${neuPct}%`, background: '#1455FE' }} />
              )}
              {indSummary.negative_count > 0 && (
                <div style={{ width: `${negPct}%`, background: '#F04438' }} />
              )}
            </div>

            {/* Legend */}
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px' }}>
              <span style={{ color: '#00D09C', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#00D09C' }} />
                Positive ({indSummary.positive_count})
              </span>
              <span style={{ color: '#1455FE', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#1455FE' }} />
                Neutral ({indSummary.neutral_count})
              </span>
              <span style={{ color: '#F04438', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#F04438' }} />
                Negative ({indSummary.negative_count})
              </span>
            </div>
          </div>

          {/* ── Filter Chips ── */}
          <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
            {[
              { key: 'all', label: `All (${indmoneyList.length})` },
              { key: 'positive', label: `Good (${indSummary.positive_count})`, color: '#00D09C' },
              { key: 'neutral', label: `Neutral (${indSummary.neutral_count})`, color: '#1455FE' },
              { key: 'negative', label: `Negative (${indSummary.negative_count})`, color: '#F04438' }
            ].map(({ key, label, color }) => {
              const isSel = selectedFilter === key;
              const activeColor = color || '#00D09C';
              return (
                <button
                  key={key}
                  onClick={() => setSelectedFilter(key)}
                  style={{
                    background: isSel ? 'rgba(0, 208, 156, 0.16)' : '#1C2333',
                    border: `1px solid ${isSel ? activeColor : '#222A3B'}`,
                    color: isSel ? activeColor : '#94A3B8',
                    borderRadius: '6px',
                    padding: '4px 10px',
                    fontSize: '11px',
                    fontWeight: isSel ? '700' : '500',
                    cursor: 'pointer'
                  }}
                >
                  {label}
                </button>
              );
            })}
          </div>

          {/* ── Complete INDmoney Events List ── */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {filteredIndmoney.map((article, idx) => {
              const theme = getSentimentTheme(article.sentiment);
              return (
                <div
                  key={idx}
                  style={{
                    background: '#1C2333',
                    border: '1px solid #222A3B',
                    borderRadius: '8px',
                    padding: '12px 14px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{
                      background: theme.badgeBg,
                      color: theme.text,
                      border: `1px solid ${theme.border}`,
                      borderRadius: '4px',
                      padding: '1px 6px',
                      fontSize: '9px',
                      fontWeight: '700'
                    }}>
                      {article.sentiment.toUpperCase()}
                    </span>
                    <span style={{ fontSize: '11px', fontWeight: '700', color: '#00D09C' }}>
                      {article.publisher}
                    </span>
                    <span style={{ fontSize: '10px', color: '#64748B' }}>
                      • {article.news_date}
                    </span>
                  </div>

                  <div style={{ fontSize: '13px', fontWeight: '600', color: '#F8FAFC', marginTop: '2px' }}>
                    {article.title}
                  </div>

                  {article.description && (
                    <div style={{
                      fontSize: '11.5px',
                      color: '#94A3B8',
                      lineHeight: '1.45',
                      marginTop: '2px'
                    }}>
                      {article.description}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* VIEW 2: YAHOO FINANCE NEWS (Real-time Market News Wire)                  */}
      {/* ========================================================================= */}
      {activeTab === 'yahoo' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <h3 style={{ margin: 0, fontSize: '17px', fontWeight: '700', color: '#F8FAFC' }}>
                Yahoo Finance News Wire
              </h3>
              <span style={{ fontSize: '11px', color: '#94A3B8' }}>
                Live coverage & publisher stories for {symbol}
              </span>
            </div>

            <span style={{
              background: 'rgba(20, 85, 254, 0.15)',
              color: '#1455FE',
              border: '1px solid rgba(20, 85, 254, 0.5)',
              borderRadius: '6px',
              padding: '4px 10px',
              fontSize: '11px',
              fontWeight: '700'
            }}>
              {yahooList.length} Articles
            </span>
          </div>

          {/* Lead Highlight Article */}
          {yahooList.length > 0 && (
            <div style={{
              background: '#0B0E14',
              border: '1px solid rgba(20, 85, 254, 0.5)',
              borderRadius: '12px',
              padding: '14px',
              marginBottom: '16px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <span style={{
                  background: 'rgba(20, 85, 254, 0.16)',
                  color: '#1455FE',
                  border: '1px solid #1455FE',
                  borderRadius: '4px',
                  padding: '2px 8px',
                  fontSize: '10px',
                  fontWeight: '700'
                }}>
                  ⚡ TODAY'S WIRE
                </span>
                <span style={{ fontSize: '11px', color: '#94A3B8' }}>
                  {yahooList[0].publisher}
                </span>
              </div>

              <h4 style={{ margin: '0 0 8px 0', fontSize: '14px', fontWeight: '700', color: '#F8FAFC', lineHeight: '1.4' }}>
                {yahooList[0].title}
              </h4>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '11px' }}>
                <span style={{ color: '#64748B' }}>
                  {yahooList[0].time_ago || 'Recent'} • {yahooList[0].publisher}
                </span>
                <a
                  href={yahooList[0].link}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    color: '#00D09C',
                    textDecoration: 'none',
                    fontWeight: '700',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}
                >
                  Read article <ExternalLink size={12} />
                </a>
              </div>
            </div>
          )}

          {/* Full Yahoo Articles Wire Feed */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {yahooList.map((article, idx) => (
              <div
                key={idx}
                style={{
                  background: '#1C2333',
                  border: '1px solid #222A3B',
                  borderRadius: '8px',
                  padding: '12px 14px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: '12px'
                }}
              >
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                    <span style={{ fontSize: '11px', fontWeight: '700', color: '#1455FE' }}>
                      {article.publisher}
                    </span>
                    <span style={{ fontSize: '10px', color: '#64748B' }}>
                      • {article.time_ago || 'Recently'}
                    </span>
                  </div>

                  <div style={{ fontSize: '13px', fontWeight: '600', color: '#F8FAFC' }}>
                    {article.title}
                  </div>
                </div>

                <a
                  href={article.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    color: '#1455FE',
                    display: 'flex',
                    alignItems: 'center',
                    padding: '6px 10px',
                    borderRadius: '6px',
                    background: '#0B0E14',
                    border: '1px solid #222A3B',
                    textDecoration: 'none',
                    fontSize: '11px',
                    fontWeight: '600',
                    gap: '4px',
                    flexShrink: 0
                  }}
                >
                  Read <ExternalLink size={12} />
                </a>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
