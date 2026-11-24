import React, { useState } from 'react';
import { Sliders, Calculator, Zap, TrendingUp, AlertTriangle, ShieldCheck, ArrowRight, DollarSign } from 'lucide-react';

const TEST_SYMBOLS = [
  {
    symbol: 'AAPL',
    name: 'Apple Inc.',
    price: 228.00,
    mc: '$3.45T',
    pe: 34.2,
    fcf: '$108.8B',
    revenue: 383.3,
    grossMargin: 46.2,
    netMargin: 25.3,
    cfoRatio: 110.5,
    capex: 9.5,
    organicGrowth: 8.0,
    acquiredGrowth: 1.0,
    churn: 1.2,
    dol: 1.65,
    revDelta: 6.0,
    netIncome: 97.0,
    shares: 15400,
    cfo: 110.5,
    assets: 352.6,
    wc: -1.7,
    re: 70.0,
    ebit: 114.3,
    mcNum: 3450.0,
    tl: 290.4,
    sales: 383.3,
    ta: 352.6,
    l8NetMargin: 25.3,
    l8AssetTurnover: 1.09,
    l8EquityMult: 5.67,
    priceHike: 5.0,
    churnRate: 1.0,
    capexAlloc: 15,
    buybackAlloc: 70,
    divAlloc: 12,
    debtAlloc: 3,
    eps: 6.08,
    growthRate: 14.5
  },
  {
    symbol: 'NVDA',
    name: 'Nvidia Corp.',
    price: 125.50,
    mc: '$3.08T',
    pe: 48.5,
    fcf: '$60.9B',
    revenue: 120.0,
    grossMargin: 75.1,
    netMargin: 55.0,
    cfoRatio: 105.0,
    capex: 4.2,
    organicGrowth: 95.0,
    acquiredGrowth: 0.0,
    churn: 0.5,
    dol: 3.20,
    revDelta: 50.0,
    netIncome: 66.0,
    shares: 24500,
    cfo: 69.3,
    assets: 65.7,
    wc: 34.0,
    re: 40.0,
    ebit: 75.0,
    mcNum: 3080.0,
    tl: 22.0,
    sales: 120.0,
    ta: 65.7,
    l8NetMargin: 55.0,
    l8AssetTurnover: 1.83,
    l8EquityMult: 1.50,
    priceHike: 15.0,
    churnRate: 0.2,
    capexAlloc: 25,
    buybackAlloc: 65,
    divAlloc: 5,
    debtAlloc: 5,
    eps: 2.69,
    growthRate: 45.0
  },
  {
    symbol: 'MSFT',
    name: 'Microsoft Corp.',
    price: 440.00,
    mc: '$3.27T',
    pe: 36.1,
    fcf: '$74.1B',
    revenue: 245.1,
    grossMargin: 69.8,
    netMargin: 36.0,
    cfoRatio: 134.5,
    capex: 44.5,
    organicGrowth: 15.5,
    acquiredGrowth: 3.5,
    churn: 2.0,
    dol: 1.94,
    revDelta: 15.0,
    netIncome: 88.1,
    shares: 7430,
    cfo: 118.5,
    assets: 512.2,
    wc: 35.0,
    re: 140.0,
    ebit: 109.4,
    mcNum: 3270.0,
    tl: 243.7,
    sales: 245.1,
    ta: 512.2,
    l8NetMargin: 36.0,
    l8AssetTurnover: 0.48,
    l8EquityMult: 1.91,
    priceHike: 8.0,
    churnRate: 1.5,
    capexAlloc: 40,
    buybackAlloc: 40,
    divAlloc: 15,
    debtAlloc: 5,
    eps: 11.80,
    growthRate: 16.0
  },
  {
    symbol: 'GOOGL',
    name: 'Alphabet Inc.',
    price: 182.00,
    mc: '$2.24T',
    pe: 24.3,
    fcf: '$72.7B',
    revenue: 307.4,
    grossMargin: 57.5,
    netMargin: 27.2,
    cfoRatio: 121.0,
    capex: 32.2,
    organicGrowth: 13.0,
    acquiredGrowth: 1.0,
    churn: 1.5,
    dol: 1.85,
    revDelta: 13.0,
    netIncome: 83.6,
    shares: 12400,
    cfo: 101.2,
    assets: 402.4,
    wc: 60.0,
    re: 190.0,
    ebit: 95.0,
    mcNum: 2240.0,
    tl: 115.0,
    sales: 307.4,
    ta: 402.4,
    l8NetMargin: 27.2,
    l8AssetTurnover: 0.76,
    l8EquityMult: 1.40,
    priceHike: 4.0,
    churnRate: 1.0,
    capexAlloc: 35,
    buybackAlloc: 55,
    divAlloc: 5,
    debtAlloc: 5,
    eps: 6.74,
    growthRate: 15.0
  },
  {
    symbol: 'RELIANCE.NS',
    name: 'Reliance Industries',
    price: 2980.00,
    mc: '₹20.1T',
    pe: 27.8,
    fcf: '₹68.4K Cr',
    revenue: 118.0,
    grossMargin: 34.5,
    netMargin: 8.5,
    cfoRatio: 145.0,
    capex: 18.0,
    organicGrowth: 10.5,
    acquiredGrowth: 2.0,
    churn: 2.5,
    dol: 1.50,
    revDelta: 10.0,
    netIncome: 10.0,
    shares: 6760,
    cfo: 14.5,
    assets: 185.0,
    wc: -8.0,
    re: 45.0,
    ebit: 16.5,
    mcNum: 240.0,
    tl: 95.0,
    sales: 118.0,
    ta: 185.0,
    l8NetMargin: 8.5,
    l8AssetTurnover: 0.64,
    l8EquityMult: 2.10,
    priceHike: 6.0,
    churnRate: 2.0,
    capexAlloc: 50,
    buybackAlloc: 0,
    divAlloc: 20,
    debtAlloc: 30,
    eps: 107.0,
    growthRate: 12.0
  },
  {
    symbol: 'TSLA',
    name: 'Tesla Inc.',
    price: 245.00,
    mc: '$780B',
    pe: 65.4,
    fcf: '$3.6B',
    revenue: 96.8,
    grossMargin: 18.2,
    netMargin: 8.2,
    cfoRatio: 165.0,
    capex: 9.0,
    organicGrowth: 7.0,
    acquiredGrowth: 0.0,
    churn: 3.0,
    dol: 2.40,
    revDelta: 8.0,
    netIncome: 7.9,
    shares: 3180,
    cfo: 13.0,
    assets: 106.6,
    wc: 13.0,
    re: 28.0,
    ebit: 8.9,
    mcNum: 780.0,
    tl: 43.0,
    sales: 96.8,
    ta: 106.6,
    l8NetMargin: 8.2,
    l8AssetTurnover: 0.91,
    l8EquityMult: 1.68,
    priceHike: -3.0,
    churnRate: 4.0,
    capexAlloc: 60,
    buybackAlloc: 0,
    divAlloc: 0,
    debtAlloc: 40,
    eps: 2.48,
    growthRate: 20.0
  }
];

export default function LiveLessonSandbox({
  lessonNumber,
  onSelectStock
}) {
  const [selectedStock, setSelectedStock] = useState(TEST_SYMBOLS[0]);

  // Lesson 1: Order Slippage
  const [l1Shares, setL1Shares] = useState(500);

  // Lesson 2: 3-Statement & FCF Simulator
  const [l2Revenue, setL2Revenue] = useState(TEST_SYMBOLS[0].revenue);
  const [l2GrossMargin, setL2GrossMargin] = useState(TEST_SYMBOLS[0].grossMargin);
  const [l2NetMargin, setL2NetMargin] = useState(TEST_SYMBOLS[0].netMargin);
  const [l2CfoRatio, setL2CfoRatio] = useState(TEST_SYMBOLS[0].cfoRatio);
  const [l2Capex, setL2Capex] = useState(TEST_SYMBOLS[0].capex);

  // Lesson 3: Organic vs Acquired Growth
  const [l3OrganicGrowth, setL3OrganicGrowth] = useState(TEST_SYMBOLS[0].organicGrowth);
  const [l3AcquiredGrowth, setL3AcquiredGrowth] = useState(TEST_SYMBOLS[0].acquiredGrowth);
  const [l3Churn, setL3Churn] = useState(TEST_SYMBOLS[0].churn);

  // Lesson 4: Operating Leverage (DOL)
  const [l4Dol, setL4Dol] = useState(TEST_SYMBOLS[0].dol);
  const [l4RevDelta, setL4RevDelta] = useState(TEST_SYMBOLS[0].revDelta);

  // Lesson 5: Diluted EPS & Sloan Accrual
  const [l5NetIncome, setL5NetIncome] = useState(TEST_SYMBOLS[0].netIncome);
  const [l5Shares, setL5Shares] = useState(TEST_SYMBOLS[0].shares);
  const [l5Cfo, setL5Cfo] = useState(TEST_SYMBOLS[0].cfo);
  const [l5Assets, setL5Assets] = useState(TEST_SYMBOLS[0].assets);

  // Lesson 7: Solvency & Altman Z
  const [l7Wc, setL7Wc] = useState(TEST_SYMBOLS[0].wc);
  const [l7Re, setL7Re] = useState(TEST_SYMBOLS[0].re);
  const [l7Ebit, setL7Ebit] = useState(TEST_SYMBOLS[0].ebit);
  const [l7Mc, setL7Mc] = useState(TEST_SYMBOLS[0].mcNum);
  const [l7Tl, setL7Tl] = useState(TEST_SYMBOLS[0].tl);
  const [l7Sales, setL7Sales] = useState(TEST_SYMBOLS[0].sales);
  const [l7Ta, setL7Ta] = useState(TEST_SYMBOLS[0].ta);

  // Lesson 8: DuPont 3-Stage ROE
  const [l8NetMargin, setL8NetMargin] = useState(TEST_SYMBOLS[0].l8NetMargin);
  const [l8AssetTurnover, setL8AssetTurnover] = useState(TEST_SYMBOLS[0].l8AssetTurnover);
  const [l8EquityMult, setL8EquityMult] = useState(TEST_SYMBOLS[0].l8EquityMult);

  // Lesson 9: Pricing Power vs Churn
  const [l9PriceHike, setL9PriceHike] = useState(TEST_SYMBOLS[0].priceHike);
  const [l9ChurnRate, setL9ChurnRate] = useState(TEST_SYMBOLS[0].churnRate);

  // Lesson 10: Warren Buffett $1 Test & Capital Allocation
  const [l10CapexAlloc, setL10CapexAlloc] = useState(TEST_SYMBOLS[0].capexAlloc);
  const [l10BuybackAlloc, setL10BuybackAlloc] = useState(TEST_SYMBOLS[0].buybackAlloc);
  const [l10DivAlloc, setL10DivAlloc] = useState(TEST_SYMBOLS[0].divAlloc);
  const [l10DebtAlloc, setL10DebtAlloc] = useState(TEST_SYMBOLS[0].debtAlloc);

  // Lesson 12: Valuation P/E & Equity Risk Premium
  const [l12Price, setL12Price] = useState(TEST_SYMBOLS[0].price);
  const [l12Eps, setL12Eps] = useState(TEST_SYMBOLS[0].eps);
  const [l12TreasuryYield, setL12TreasuryYield] = useState(4.25);
  const [l12GrowthRate, setL12GrowthRate] = useState(TEST_SYMBOLS[0].growthRate);

  const handleStockClick = (item) => {
    setSelectedStock(item);
    // Update Lesson 2
    if (item.revenue !== undefined) setL2Revenue(item.revenue);
    if (item.grossMargin !== undefined) setL2GrossMargin(item.grossMargin);
    if (item.netMargin !== undefined) setL2NetMargin(item.netMargin);
    if (item.cfoRatio !== undefined) setL2CfoRatio(item.cfoRatio);
    if (item.capex !== undefined) setL2Capex(item.capex);
    // Update Lesson 3
    if (item.organicGrowth !== undefined) setL3OrganicGrowth(item.organicGrowth);
    if (item.acquiredGrowth !== undefined) setL3AcquiredGrowth(item.acquiredGrowth);
    if (item.churn !== undefined) setL3Churn(item.churn);
    // Update Lesson 4
    if (item.dol !== undefined) setL4Dol(item.dol);
    if (item.revDelta !== undefined) setL4RevDelta(item.revDelta);
    // Update Lesson 5
    if (item.netIncome !== undefined) setL5NetIncome(item.netIncome);
    if (item.shares !== undefined) setL5Shares(item.shares);
    if (item.cfo !== undefined) setL5Cfo(item.cfo);
    if (item.assets !== undefined) setL5Assets(item.assets);
    // Update Lesson 7
    if (item.wc !== undefined) setL7Wc(item.wc);
    if (item.re !== undefined) setL7Re(item.re);
    if (item.ebit !== undefined) setL7Ebit(item.ebit);
    if (item.mcNum !== undefined) setL7Mc(item.mcNum);
    if (item.tl !== undefined) setL7Tl(item.tl);
    if (item.sales !== undefined) setL7Sales(item.sales);
    if (item.ta !== undefined) setL7Ta(item.ta);
    // Update Lesson 8
    if (item.l8NetMargin !== undefined) setL8NetMargin(item.l8NetMargin);
    if (item.l8AssetTurnover !== undefined) setL8AssetTurnover(item.l8AssetTurnover);
    if (item.l8EquityMult !== undefined) setL8EquityMult(item.l8EquityMult);
    // Update Lesson 9
    if (item.priceHike !== undefined) setL9PriceHike(item.priceHike);
    if (item.churnRate !== undefined) setL9ChurnRate(item.churnRate);
    // Update Lesson 10
    if (item.capexAlloc !== undefined) setL10CapexAlloc(item.capexAlloc);
    if (item.buybackAlloc !== undefined) setL10BuybackAlloc(item.buybackAlloc);
    if (item.divAlloc !== undefined) setL10DivAlloc(item.divAlloc);
    if (item.debtAlloc !== undefined) setL10DebtAlloc(item.debtAlloc);
    // Update Lesson 12
    setL12Price(item.price);
    if (item.eps !== undefined) setL12Eps(item.eps);
    if (item.growthRate !== undefined) setL12GrowthRate(item.growthRate);
    // Keep user in the sandbox lab: do NOT trigger tab navigation!
  };

  // Render specific lab content based on lessonNumber
  const renderLabContent = () => {
    switch (lessonNumber) {
      // -------------------------------------------------------------
      // LESSON 1: ORDER EXECUTION & SLIPPAGE SIMULATOR
      // -------------------------------------------------------------
      case 1: {
        const ask = selectedStock.price;
        const bid = ask - 0.01;
        const spreadPct = ((ask - bid) / ((ask + bid) / 2)) * 100;
        const baseCost = l1Shares * ask;
        const slippageBps = l1Shares > 5000 ? (l1Shares / 1000) * 0.4 : 0.02;
        const slippageDollars = (baseCost * slippageBps) / 100;
        const fillVwap = ask * (1 + slippageBps / 100);

        return (
          <div className="sandbox-panel">
            <div className="sandbox-slider-group">
              <div className="sandbox-slider-header">
                <span className="sandbox-slider-label">Simulated Market Order Size</span>
                <span className="sandbox-slider-val font-mono">{l1Shares.toLocaleString()} shares</span>
              </div>
              <input
                type="range"
                min="10"
                max="50000"
                step="50"
                value={l1Shares}
                onChange={(e) => setL1Shares(Number(e.target.value))}
                className="fintech-slider"
              />
            </div>

            <div className="sandbox-grid-metrics">
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Quoted Best Ask</span>
                <span className="sandbox-tile-val font-mono">${ask.toFixed(2)}</span>
                <span className="sandbox-tile-sub">Top of order book</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Simulated VWAP Fill</span>
                <span className="sandbox-tile-val font-mono gain">${fillVwap.toFixed(3)}</span>
                <span className="sandbox-tile-sub font-mono">Total: ${(baseCost + slippageDollars).toLocaleString(undefined, { maximumFractionDigits: 0 })}</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Execution Slippage Paid</span>
                <span className={`sandbox-tile-val font-mono ${slippageDollars > 20 ? 'loss' : 'gain'}`}>
                  ${slippageDollars.toFixed(2)} ({slippageBps.toFixed(3)}%)
                </span>
                <span className="sandbox-tile-sub">Spread friction + book sweep</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Market Spread Friction</span>
                <span className="sandbox-tile-val font-mono">{spreadPct.toFixed(4)}%</span>
                <span className="sandbox-tile-sub font-mono">Bid: ${bid.toFixed(2)} / Ask: ${ask.toFixed(2)}</span>
              </div>
            </div>

            <div className="sandbox-insight-box">
              <ShieldCheck size={16} className="sandbox-insight-icon" />
              <div className="sandbox-insight-text">
                <strong>Limit Order Defense:</strong> A market order for {l1Shares.toLocaleString()} shares sweeps through multiple price levels in the order book, creating an instant friction penalty of ${slippageDollars.toFixed(2)}. Placing a Limit Order at ${ask.toFixed(2)} guarantees zero slippage.
              </div>
            </div>
          </div>
        );
      }

      // -------------------------------------------------------------
      // LESSON 2: 3-STATEMENT & FREE CASH CONVERSION WATERFALL
      // -------------------------------------------------------------
      case 2: {
        const grossProfit = (l2Revenue * (l2GrossMargin / 100));
        const netIncome = (l2Revenue * (l2NetMargin / 100));
        const cfo = (netIncome * (l2CfoRatio / 100));
        const fcf = cfo - l2Capex;
        const fcfConversion = netIncome > 0 ? (fcf / netIncome) * 100 : 0;
        const isEliteCash = fcfConversion >= 80;

        return (
          <div className="sandbox-panel">
            <div className="sandbox-two-col-sliders">
              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Annual Revenue ($B)</span>
                  <span className="sandbox-slider-val font-mono">${l2Revenue.toFixed(1)}B</span>
                </div>
                <input
                  type="range"
                  min="10"
                  max="600"
                  step="5"
                  value={l2Revenue}
                  onChange={(e) => setL2Revenue(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Gross Margin %</span>
                  <span className="sandbox-slider-val font-mono">{l2GrossMargin.toFixed(1)}%</span>
                </div>
                <input
                  type="range"
                  min="20"
                  max="90"
                  step="1"
                  value={l2GrossMargin}
                  onChange={(e) => setL2GrossMargin(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">CFO-to-Net-Income Ratio %</span>
                  <span className="sandbox-slider-val font-mono">{l2CfoRatio.toFixed(1)}%</span>
                </div>
                <input
                  type="range"
                  min="50"
                  max="200"
                  step="5"
                  value={l2CfoRatio}
                  onChange={(e) => setL2CfoRatio(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Capital Expenditures (CapEx $B)</span>
                  <span className="sandbox-slider-val font-mono">${l2Capex.toFixed(1)}B</span>
                </div>
                <input
                  type="range"
                  min="5"
                  max="100"
                  step="1"
                  value={l2Capex}
                  onChange={(e) => setL2Capex(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>
            </div>

            <div className="sandbox-grid-metrics">
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Gross Profit</span>
                <span className="sandbox-tile-val font-mono">${grossProfit.toFixed(1)}B</span>
                <span className="sandbox-tile-sub">Revenue - COGS</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">GAAP Net Income</span>
                <span className="sandbox-tile-val font-mono">${netIncome.toFixed(1)}B</span>
                <span className="sandbox-tile-sub">Accounting paper profit</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Cash from Operations (CFO)</span>
                <span className="sandbox-tile-val font-mono gain">${cfo.toFixed(1)}B</span>
                <span className="sandbox-tile-sub">Customer cash collected</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Free Cash Flow (FCF)</span>
                <span className={`sandbox-tile-val font-mono ${fcf > 0 ? 'gain' : 'loss'}`}>
                  ${fcf.toFixed(1)}B
                </span>
                <span className="sandbox-tile-sub">CFO - CapEx</span>
              </div>
            </div>

            <div className={`sandbox-status-card ${isEliteCash ? 'success' : 'warning'}`}>
              <div className="sandbox-status-header">
                <span className="sandbox-status-tag">FCF CONVERSION: {fcfConversion.toFixed(1)}%</span>
                <span className="sandbox-status-badge">{isEliteCash ? 'Elite Cash Backing' : 'High CapEx Reinvestment'}</span>
              </div>
              <p className="sandbox-status-desc">
                {isEliteCash
                  ? 'Outstanding cash conversion (>80%). Net income is fully backed by audited bank deposits, leaving immense capital for organic share buybacks and dividends.'
                  : 'Cash conversion is below 80%. High capital reinvestment into infrastructure or working capital absorption reduces cash flow available to shareholders.'}
              </p>
            </div>
          </div>
        );
      }

      // -------------------------------------------------------------
      // LESSON 4: OPERATING LEVERAGE (DOL) SIMULATOR
      // -------------------------------------------------------------
      case 4: {
        const ebitGrowth = l4RevDelta * l4Dol;

        return (
          <div className="sandbox-panel">
            <div className="sandbox-two-col-sliders">
              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Degree of Operating Leverage (DOL)</span>
                  <span className="sandbox-slider-val font-mono">{l4Dol.toFixed(2)}x</span>
                </div>
                <input
                  type="range"
                  min="1.0"
                  max="4.0"
                  step="0.05"
                  value={l4Dol}
                  onChange={(e) => setL4Dol(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Revenue Expansion (%)</span>
                  <span className={`sandbox-slider-val font-mono ${l4RevDelta >= 0 ? 'gain' : 'loss'}`}>
                    {l4RevDelta >= 0 ? '+' : ''}{l4RevDelta.toFixed(1)}%
                  </span>
                </div>
                <input
                  type="range"
                  min="-30"
                  max="50"
                  step="1"
                  value={l4RevDelta}
                  onChange={(e) => setL4RevDelta(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>
            </div>

            <div className="sandbox-grid-metrics">
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Operating Income (EBIT) Growth</span>
                <span className={`sandbox-tile-val font-mono ${ebitGrowth >= 0 ? 'gain' : 'loss'}`} style={{ fontSize: '20px' }}>
                  {ebitGrowth >= 0 ? '+' : ''}{ebitGrowth.toFixed(1)}%
                </span>
                <span className="sandbox-tile-sub">DOL × Revenue Growth %</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Profit Multiplier Effect</span>
                <span className="sandbox-tile-val font-mono">{l4Dol.toFixed(2)}x Magnification</span>
                <span className="sandbox-tile-sub">Fixed cost dilution efficiency</span>
              </div>
            </div>

            <div className="sandbox-insight-box">
              <Zap size={16} className="sandbox-insight-icon" />
              <div className="sandbox-insight-text">
                <strong>The Operating Leverage Double-Edged Sword:</strong> Because fixed software & datacenter costs remain steady, a +{l4RevDelta.toFixed(0)}% increase in revenue yields a massive +{ebitGrowth.toFixed(1)}% expansion in operating profits. However, in a cyclical downturn, profits contract by the same multiplier.
              </div>
            </div>
          </div>
        );
      }

      // -------------------------------------------------------------
      // LESSON 8: DUPONT 3-STAGE ROE DECOMPOSITION
      // -------------------------------------------------------------
      case 8: {
        const roa = l8NetMargin * l8AssetTurnover;
        const roe = roa * l8EquityMult;
        const isDebtDriven = l8EquityMult > 4.0;

        return (
          <div className="sandbox-panel">
            <div className="sandbox-two-col-sliders">
              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">1. Net Profit Margin (%)</span>
                  <span className="sandbox-slider-val font-mono">{l8NetMargin.toFixed(1)}%</span>
                </div>
                <input
                  type="range"
                  min="5"
                  max="45"
                  step="0.5"
                  value={l8NetMargin}
                  onChange={(e) => setL8NetMargin(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">2. Asset Turnover Velocity (x)</span>
                  <span className="sandbox-slider-val font-mono">{l8AssetTurnover.toFixed(2)}x</span>
                </div>
                <input
                  type="range"
                  min="0.3"
                  max="2.5"
                  step="0.05"
                  value={l8AssetTurnover}
                  onChange={(e) => setL8AssetTurnover(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">3. Financial Leverage Multiplier (x)</span>
                  <span className="sandbox-slider-val font-mono">{l8EquityMult.toFixed(2)}x</span>
                </div>
                <input
                  type="range"
                  min="1.0"
                  max="8.0"
                  step="0.1"
                  value={l8EquityMult}
                  onChange={(e) => setL8EquityMult(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>
            </div>

            <div className="sandbox-grid-metrics">
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Return on Assets (ROA)</span>
                <span className="sandbox-tile-val font-mono">{roa.toFixed(1)}%</span>
                <span className="sandbox-tile-sub">Margin × Asset Turnover</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Final Return on Equity (ROE)</span>
                <span className="sandbox-tile-val font-mono gain" style={{ fontSize: '20px' }}>
                  {roe.toFixed(1)}%
                </span>
                <span className="sandbox-tile-sub">ROA × Leverage Multiplier</span>
              </div>
            </div>

            <div className={`sandbox-status-card ${isDebtDriven ? 'warning' : 'success'}`}>
              <div className="sandbox-status-header">
                <span className="sandbox-status-tag">DUPONT QUALITY AUDIT</span>
                <span className="sandbox-status-badge">{isDebtDriven ? 'Leverage-Amplified ROE' : 'Operational Pricing Power'}</span>
              </div>
              <p className="sandbox-status-desc">
                {isDebtDriven
                  ? `Notice: While ROE is high at ${roe.toFixed(1)}%, ${((l8EquityMult - 1) / l8EquityMult * 100).toFixed(0)}% of it is generated through balance sheet debt leverage (Equity Multiplier = ${l8EquityMult.toFixed(1)}x). Always check ROIC to evaluate pure operational returns!`
                  : `High quality ROE driven by organic pricing power (${l8NetMargin.toFixed(1)}% margin) and capital velocity (${l8AssetTurnover.toFixed(2)}x turnover) rather than dangerous financial borrowing.`}
              </p>
            </div>
          </div>
        );
      }

      // -------------------------------------------------------------
      // LESSON 12: P/E MULTIPLE & EARNINGS YIELD ARBITRAGE
      // -------------------------------------------------------------
      case 12:
      default: {
        const pe = l12Eps > 0 ? (l12Price / l12Eps) : 0;
        const earningsYield = pe > 0 ? (1 / pe) * 100 : 0;
        const erp = earningsYield - l12TreasuryYield;
        const peg = l12GrowthRate > 0 ? (pe / l12GrowthRate) : 0;

        return (
          <div className="sandbox-panel">
            <div className="sandbox-two-col-sliders">
              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Share Price ($)</span>
                  <span className="sandbox-slider-val font-mono">${l12Price.toFixed(2)}</span>
                </div>
                <input
                  type="range"
                  min="20"
                  max="600"
                  step="2"
                  value={l12Price}
                  onChange={(e) => setL12Price(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">Diluted TTM EPS ($)</span>
                  <span className="sandbox-slider-val font-mono">${l12Eps.toFixed(2)}</span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="25"
                  step="0.25"
                  value={l12Eps}
                  onChange={(e) => setL12Eps(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">10-Year US Treasury Risk-Free Yield (%)</span>
                  <span className="sandbox-slider-val font-mono">{l12TreasuryYield.toFixed(2)}%</span>
                </div>
                <input
                  type="range"
                  min="1.5"
                  max="7.0"
                  step="0.1"
                  value={l12TreasuryYield}
                  onChange={(e) => setL12TreasuryYield(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>

              <div className="sandbox-slider-group">
                <div className="sandbox-slider-header">
                  <span className="sandbox-slider-label">5-Year Projected EPS CAGR (%)</span>
                  <span className="sandbox-slider-val font-mono">{l12GrowthRate.toFixed(1)}%</span>
                </div>
                <input
                  type="range"
                  min="3"
                  max="50"
                  step="1"
                  value={l12GrowthRate}
                  onChange={(e) => setL12GrowthRate(Number(e.target.value))}
                  className="fintech-slider"
                />
              </div>
            </div>

            <div className="sandbox-grid-metrics">
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Trailing P/E Multiple</span>
                <span className="sandbox-tile-val font-mono">{pe.toFixed(1)}x</span>
                <span className="sandbox-tile-sub">Price / Diluted EPS</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Earnings Yield (E/P)</span>
                <span className="sandbox-tile-val font-mono gain">{earningsYield.toFixed(2)}%</span>
                <span className="sandbox-tile-sub">(1 / P/E) × 100</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Equity Risk Premium (ERP)</span>
                <span className={`sandbox-tile-val font-mono ${erp >= 0 ? 'gain' : 'loss'}`}>
                  {erp >= 0 ? '+' : ''}{erp.toFixed(2)}%
                </span>
                <span className="sandbox-tile-sub">Yield - 10Y Treasury ({l12TreasuryYield}%)</span>
              </div>
              <div className="sandbox-metric-tile">
                <span className="sandbox-tile-label">Peter Lynch PEG Ratio</span>
                <span className={`sandbox-tile-val font-mono ${peg <= 1.5 ? 'gain' : 'loss'}`}>
                  {peg.toFixed(2)}x
                </span>
                <span className="sandbox-tile-sub font-mono">{peg <= 1.2 ? 'GARP Bargain' : peg <= 2.0 ? 'Fair Value' : 'Expensive'}</span>
              </div>
            </div>

            <div className="sandbox-insight-box">
              <TrendingUp size={16} className="sandbox-insight-icon" />
              <div className="sandbox-insight-text">
                {erp < 0
                  ? `Negative ERP Alert: The earnings yield (${earningsYield.toFixed(2)}%) is lower than risk-free government bonds (${l12TreasuryYield.toFixed(2)}%). Investors are paying a premium and counting on high ${l12GrowthRate}% future earnings expansion to break even.`
                  : `Positive Equity Risk Premium (+${erp.toFixed(2)}%): The company's earnings yield comfortably exceeds the risk-free rate, providing a valuation margin of safety.`}
              </div>
            </div>
          </div>
        );
      }
    }
  };

  return (
    <div className="sandbox-card">
      <div className="sandbox-header">
        <div className="sandbox-title-wrap">
          <div className="sandbox-badge">
            <Calculator size={14} />
            <span>QUANTITATIVE LABORATORY</span>
          </div>
          <h3 className="sandbox-title">Interactive Mathematical Sandbox & Valuation Lab</h3>
          <p className="sandbox-desc">
            Adjust the sliders below to run real-time stress tests and see the underlying formulas recalculate dynamically.
          </p>
        </div>

        {/* Live Stock Quick Switcher */}
        <div className="sandbox-stock-pills-wrap">
          <span className="sandbox-stock-pills-label">Test on Real Stocks:</span>
          <div className="sandbox-stock-pills">
            {TEST_SYMBOLS.map((stk) => (
              <button
                key={stk.symbol}
                className={`sandbox-stock-pill ${selectedStock.symbol === stk.symbol ? 'active' : ''}`}
                onClick={() => handleStockClick(stk)}
                title={`${stk.name} - Current: $${stk.price}`}
              >
                <span className="stk-symbol font-mono">{stk.symbol}</span>
                <span className="stk-price font-mono">${stk.price}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Render Dynamic Lab */}
      {renderLabContent()}
    </div>
  );
}
