import React, { useState, useEffect } from 'react';
import { fetchStockHistory } from '../../services/api';
import ProChartModal from '../charts/ProChartModal';
import FintechLoader from '../common/FintechLoader';

/**
 * StockPatternInspectionModal
 * Renders the maximized interactive candlestick chart modal for ANY pattern matching stock,
 * complete with indicators, pattern formation diagnostic, key levels (pivot, stop-loss, target),
 * what is going to happen text, and a prominent Deep Analysis CTA button.
 */
export default function StockPatternInspectionModal({
  isOpen,
  onClose,
  stock,
  onGoToDeepAnalysis
}) {
  const [timeframe, setTimeframe] = useState('1mo');
  const [historyData, setHistoryData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isOpen || !stock?.ticker) return;

    let isMounted = true;
    setIsLoading(true);

    fetchStockHistory(stock.ticker, timeframe)
      .then((data) => {
        if (isMounted) {
          setHistoryData(data);
          setIsLoading(false);
        }
      })
      .catch((err) => {
        console.error('Failed to load stock history in Pattern Modal:', err);
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, stock?.ticker, timeframe]);

  if (!isOpen || !stock) return null;

  const currencySymbol = stock.market === 'IN' || stock.ticker.endsWith('.NS') || stock.ticker.endsWith('.BO') ? '₹' : '$';

  return (
    <>
      {isLoading && !historyData && (
        <div className="pro-modal-backdrop" onClick={onClose}>
          <div
            className="pro-modal-container"
            onClick={(e) => e.stopPropagation()}
            style={{ maxWidth: '640px', padding: '36px' }}
          >
            <FintechLoader
              title={`Loading Maximized Chart for ${stock.ticker}...`}
              subtitle={`Fetching ${timeframe} candlestick data and computing quantitative indicators...`}
            />
          </div>
        </div>
      )}

      {historyData && (
        <ProChartModal
          isOpen={isOpen}
          onClose={onClose}
          ticker={stock.ticker}
          currencySymbol={currencySymbol}
          historyData={historyData}
          timeframe={timeframe}
          setTimeframe={setTimeframe}
          companyName={stock.name || stock.companyName || stock.ticker}
          logoUrl={`https://assets.parqet.com/logos/symbol/${stock.ticker}?format=png`}
          fiftyTwoWeekHigh={historyData.highest}
          fiftyTwoWeekLow={historyData.lowest}
          patternData={stock}
          onGoToDeepAnalysis={onGoToDeepAnalysis}
        />
      )}
    </>
  );
}
