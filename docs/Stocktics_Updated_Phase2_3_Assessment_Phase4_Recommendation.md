# Stocktics — Updated Phase 2/3 Assessment & Phase 4 Recommendation

## Executive Summary

The additional empirical audit materially strengthens the Stocktics research.

Most of the original red flags have now been investigated:

- Exact metric ties were explained by the small test set (`n = 229`).
- The MLP scaling pipeline was verified as train-only.
- MLP performance was tested across five random seeds.
- Negative regression R² was manually recomputed.
- Track 3 was expanded from one forecast window to 30 rolling windows.
- Mean-reversion testing was expanded to 20 stocks, 8 sectors, and 399 trades.
- Transaction costs and slippage were explicitly tested.
- Cross-sectional correlation was considered through an effective-degrees-of-freedom adjustment.
- Multi-horizon confidence intervals were tested over 50 rolling forecast origins.

The project can now move into **Phase 4 development**.

However, Phase 4 should be built as a **decision, filtering, and risk layer**, not simply as another collection of prediction models.

---

# 1. Updated Assessment

The current status should be considered:

```text
Phase 2 Fundamental        🟢 Strong
Phase 3 Quant              🟢 Strong Research Foundation
Validation                 🟢 Much Improved
Production Robustness      🟡 Still Requires Validation
Phase 4 Decision Engine    🟢 Ready for Development
```

The important distinction is:

> The research is ready to build Phase 4, but the final Phase 4 system itself still needs rigorous out-of-sample validation before real capital is trusted to it.

---

# 2. Components That Should Definitely Stay

## 🟢 Triple Barrier Labeling

Keep this as a core component.

The methodology is better suited to trading than a simple fixed-horizon return label because it recognizes:

```text
Profit Target
Stop Loss
Vertical Time Barrier
```

This prevents the label from ignoring a stop-loss or profit-target event that occurred during the holding period.

Current concept:

```text
Entry
  │
  ├── Upper Barrier → Profit
  │
  ├── Lower Barrier → Loss
  │
  └── Vertical Barrier → Timeout
```

This should remain part of the production research pipeline.

---

## 🟢 27-Indicator Quantitative Feature Library

Keep the existing feature library.

Important feature categories include:

```text
Momentum
Volatility
Relative Strength
Trend
Volume
Market Beta
Macro
Regime / Memory
```

Particularly useful features include:

```text
Garman-Klass Volatility
Parkinson Volatility
Hurst Exponent
Beta vs NIFTY
Relative Strength vs NIFTY
ATR
52-Week Position
USDINR
Crude
```

Do not blindly expand to hundreds of indicators.

The priority should be **better features**, not simply more features.

---

## 🟢 Meta-Labeling

Keep the meta-labeling architecture.

The structure is:

```text
Primary Model
      ↓
Potential Trade
      ↓
Meta Classifier
      ↓
Take / Reject
```

This is particularly useful because the primary model does not need to be perfect.

The meta-model can focus on:

> "Should this particular signal actually be traded?"

That makes it an excellent candidate for Phase 4.

---

# 3. Exact Metric Ties — Resolved

The identical accuracy values were explained by the small test set.

The test set contains:

```text
n = 229
```

Therefore:

```text
131 / 229 = 57.21%
132 / 229 = 57.64%
133 / 229 = 58.08%
```

A single prediction changes the reported accuracy by approximately:

```text
0.4367 percentage points
```

SVC, BiLSTM and Super Learner all produced:

```text
132 / 229
= 57.64%
```

This is no longer considered evidence of a calculation problem.

### Engineering improvement

Always report:

```text
Accuracy: 57.64%
Correct: 132
Total: 229
```

Never report the percentage alone.

---

# 4. MLP Result — Keep, But Report the Distribution

The MLP result was properly investigated.

The scaler was verified as:

```text
fit on training data only
transform test data
```

No test distribution was used to fit the scaler.

The MLP was also evaluated across five seeds:

```text
Seed 42      65.50%
Seed 100     ...
Seed 2023    ...
Seed 7       ...
Seed 999     ...
```

The reported aggregate is:

```text
5-seed average:
62.27% ± 1.53%
```

Therefore, do not use:

```text
MLP Accuracy = 65.50%
```

as the primary headline.

Use:

```text
MLP 5-Seed OOS Accuracy:
62.27% ± 1.53%
```

The 65.50% result can remain as the Seed-42 observation.

This makes the claim substantially more credible.

---

# 5. Regression — Reinterpret Rather Than Delete

The negative R² values were manually verified.

For example:

```text
R² = 1 - SSres / SStot
```

with:

```text
SSres > SStot
```

can legitimately produce negative R².

Therefore the calculation itself is not necessarily broken.

The important insight is:

> A model can predict direction somewhat better than chance while being poor at predicting exact return magnitude.

Therefore, regression should not be the primary:

```text
"Exact return prediction engine"
```

Instead use it as:

```text
Expected Return Ranking
+
Magnitude Estimation
```

Evaluate using:

```text
Directional Accuracy
Rank IC
Spearman Correlation
Top-Decile Return
Bottom-Decile Return
Portfolio Spread
```

---

# 6. Track 3 — Corrected Interpretation

The original 1% MAPE was a single lucky/sideways period and should not be used as a general performance claim.

The expanded test uses:

```text
30 rolling one-month windows
```

The updated results are approximately:

| Model | Mean 1M MAPE | Median MAPE | Worst Window | Terminal Error |
|---|---:|---:|---:|---:|
| Monte Carlo GBM | 3.27% | 2.65% | 13.10% | 4.97% |
| AR(5) | 3.23% | 2.61% | 13.05% | 4.94% |
| Holt-Winters | 4.12% | 3.45% | 15.80% | 6.20% |

### Correct interpretation

These models are useful as:

```text
Baseline
Scenario Generator
Trajectory Reference
```

They should not be presented as highly accurate deterministic price predictors.

---

# 7. Mean-Reversion Strategy — Much Stronger Evidence, But Thin Economic Edge

The backtest is now significantly more credible because it uses:

```text
20 Stocks
8 Sectors
399 Trades
7 Years
```

The overall gross statistics are:

```text
Trades:       399
Wins:         266
Losses:       133
Win Rate:     66.67%
Profit Factor: 1.26x
```

However, realistic costs change the interpretation.

### Cost sensitivity

```text
0 bps:
Profit Factor = 1.26x

35 bps:
Profit Factor = 1.06x

50 bps:
Profit Factor = 0.98x
```

This is one of the most important findings in the entire research.

## Conclusion

The raw mean-reversion strategy has a **thin economic edge after transaction costs**.

That does NOT mean it should be removed.

It means:

> The strategy needs intelligent trade selection and risk filtering.

This strongly supports the Phase 4 architecture.

---

# 8. Do Not Overstate Statistical Significance

The backtest has 399 trades, but trades are not completely independent.

The analysis estimates:

```text
Average pairwise correlation ≈ 0.330
Average concurrent trades ≈ 2.22
Effective N ≈ 285
```

This is a useful adjustment.

The appropriate conclusion is:

> The observed win rate is statistically distinguishable from 50% under the current cluster-adjustment assumptions.

Do not translate that into:

> "The strategy is proven profitable."

Profitability still depends on:

```text
Payoff Distribution
Transaction Costs
Slippage
Drawdown
Regime Stability
Position Sizing
Execution
```

---

# 9. JSW Steel Result

The JSW Steel result was specifically audited.

The backtest window had:

```text
No splits
No bonus issues
No unadjusted corporate actions
```

The strategy generated:

```text
26 trades
22 wins
4 losses
```

The reported gross compounded return was:

```text
+98.97%
```

and approximately:

```text
+82.06% net
```

under the stated 35 bps transaction-cost assumption.

This appears to be a legitimate strategy result rather than a corporate-action price-data error.

However, the result should still be treated as a **stock/regime-specific observation**, not proof that the strategy will generate similar returns everywhere.

---

# 10. Multi-Horizon Forecasting — Important Product Rule

The expanded 50-origin audit produces:

| Horizon | Mean Error | 80% CI Coverage | Product Treatment |
|---|---:|---:|---|
| 10D | 3.07% | 80.0% | Statistical range allowed |
| 1M | 4.13% | 82.0% | Statistical range allowed |
| 6M | 19.80% | 52.0% | Scenario range only |
| 1Y | 30.24% | 48.0% | Scenario range only |

This leads to a very good product rule.

## Short horizon

For:

```text
10D
1M
```

the application may show:

```text
Expected Return
Expected Price
80% Statistical Range
Probability
```

## Long horizon

For:

```text
6M
1Y
```

do not present a misleading Gaussian/GBM confidence band.

Instead show:

```text
Bear Case
Base Case
Bull Case
DCF Range
Fundamental Drivers
```

This should be a hard UI/product rule.

---

# 11. Major Phase 4 Design Change

Do NOT start Phase 4 with fixed weights such as:

```text
Technical       30%
Quant ML        25%
Smart Money     20%
Market Internals 15%
Sentiment       10%
```

Those weights should not be assumed to be optimal.

Instead create independent component scores.

```text
Fundamental Score       0–100
Technical Score         0–100
Quant ML Score          0–100
Meta Signal Score       0–100
Valuation Score         0–100
Smart Money Score       0–100
Market Regime Score     0–100
Sector Score            0–100
Sentiment Score         0–100
Forensic Risk           0–100
```

Then learn/test the weighting methodology using historical training data and walk-forward validation.

---

# 12. Recommended Final Stocktics Architecture

```text
                         STOCK
                           │
             ┌─────────────┼─────────────┐
             ↓             ↓             ↓
        FUNDAMENTAL    TECHNICAL       MARKET
             │             │             │
             ↓             ↓             ↓
        VALUATION        QUANT          REGIME
             │             │             │
             ↓             ↓             ↓
         FORENSIC       ML MODELS      SECTOR
             │             │             │
             └─────────────┼─────────────┘
                           ↓
                    PRIMARY SIGNAL
                           ↓
                    TRIPLE BARRIER
                           ↓
                    META-LABEL MODEL
                           ↓
                      RISK ENGINE
                           ↓
                  DECISION SCORING
                           ↓
                ┌──────────┼──────────┐
                ↓          ↓          ↓
               BUY       HOLD       SELL
```

---

# 13. BUY Engine

The BUY engine should evaluate opportunity.

Conceptually:

```text
Fundamental Quality
+
Technical Setup
+
Quant ML Probability
+
Meta-Label Confidence
+
Valuation
+
Smart Money
+
Market Regime
+
Sector Strength
-
Risk
```

Output:

```text
BUY SCORE: 0–100
```

Example:

```text
BUY SCORE: 78/100
```

---

# 14. SELL Engine

SELL should NOT simply be:

```text
100 - BUY SCORE
```

SELL is a separate problem.

The SELL engine should evaluate:

```text
Technical Breakdown
+
ML Reversal
+
Fundamental Deterioration
+
Forensic Deterioration
+
Valuation Excess
+
Market Regime Change
+
Risk Increase
```

Output:

```text
SELL / EXIT SCORE: 0–100
```

This makes the system more realistic.

---

# 15. Market Regime Engine

Add this before finalizing Phase 4.

Possible regimes:

```text
🟢 Bull
🟡 Sideways
🔴 Bear
⚠️ High Volatility
```

Possible inputs:

```text
NIFTY Trend
NIFTY 200 SMA
India VIX
Market Breadth
Sector Breadth
Market Volatility
```

Example:

```text
Stock Signal: BUY
Market Regime: BEAR
```

The final system may downgrade or reject the trade.

---

# 16. Sector-Relative Analysis

Every stock should be compared against:

```text
Stock
   ↓
Sector
   ↓
Market
```

Example:

```text
Stock:      +8%
Sector:    +15%
NIFTY:      +4%
```

The stock is positive but underperforming its sector.

Add:

```text
Stock Relative Strength
Sector Relative Strength
Market Relative Strength
```

---

# 17. Earnings / Event Layer

Add:

```text
Quarterly Results
Revenue Surprise
EBITDA Surprise
PAT Surprise
Management Guidance
Dividend
Bonus
Split
Buyback
Promoter Transactions
```

Also:

```text
Days to Next Earnings
```

The system should understand that a BUY signal immediately before earnings has a different risk profile from a BUY signal after strong earnings.

---

# 18. Explainability Layer

The final application should explain why a stock received its score.

Example:

```text
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
       STOCKTICS DECISION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Action:       BUY
Conviction:   78 / 100

Fundamental:       82
Technical:         74
Quant ML:          78
Valuation:         71
Smart Money:       76
Market Regime:     68
Forensic Risk:     21
Overall Risk:      34
```

Then show:

```text
Why BUY?

✓ Strong earnings growth
✓ Positive relative strength
✓ ML probability above threshold
✓ Healthy cash conversion
✓ Favorable market regime
```

And:

```text
Risks

⚠ Valuation moderately expensive
⚠ Volatility elevated
⚠ Earnings event approaching
```

Finally:

```text
Invalidation Conditions

• Technical breakdown
• Earnings deterioration
• Forensic risk above threshold
• Market regime turns bearish
```

---

# 19. What Should Not Be Core Decision Signals

These components can remain, but their role should be reduced.

## Exact Price Prediction

Use as an auxiliary output, not the main investment decision.

## Holt-Winters

Use as a baseline.

## AR(5)

Use as a benchmark.

## Monte Carlo GBM

Use for:

```text
Scenario Distribution
Risk Range
Probability Analysis
```

not deterministic prediction.

## Raw Accuracy

Do not use classification accuracy as the primary measure of trading success.

---

# 20. Final Validation Requirement

Before deploying Phase 4 with real capital, perform one final completely unseen test.

Recommended structure:

```text
Historical Data
      ↓
Model Development
      ↓
Walk-Forward Validation
      ↓
Phase 4 Development
      ↓
FINAL UNSEEN TEST
```

The final test period must remain untouched during:

```text
Feature Selection
Hyperparameter Selection
Weight Selection
Threshold Selection
Strategy Selection
```

This is critical.

---

# 21. Final Recommended Development Sequence

```text
PHASE 2
Fundamental + Forensic
        ↓
PHASE 3
Quant + ML + Triple Barrier
        ↓
PHASE 3.5
Robustness + Anti-Overfitting
        ↓
PHASE 4
Decision Scoring Engine
        ↓
PHASE 4.5
Portfolio / Position Sizing
        ↓
FINAL UNSEEN TEST
        ↓
PAPER TRADING
        ↓
PRODUCTION
```

---

# 22. The Most Important Insight From the Current Research

The most valuable conclusion is not:

> "The strategy has a 66.67% win rate."

It is:

> **The raw technical strategy has a relatively thin economic edge after realistic transaction costs.**

That tells us exactly what Stocktics needs to do.

The application should not try to win by creating more and more prediction models.

It should win by:

```text
Better Signal Selection
+
Better Regime Detection
+
Better Risk Filtering
+
Better Meta-Labeling
+
Better Position Sizing
+
Better Portfolio Construction
```

That is the purpose of Phase 4.

---

# 23. Target End-State

Stocktics should ultimately avoid presenting itself as:

> "The model predicts the stock will reach ₹1,540."

Instead:

```text
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        STOCKTICS DECISION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Action:              BUY
Conviction:          78 / 100

10D Expected Return:  +1.8%
1M Expected Return:   +3.7%
6M Scenario:          +8.9%
1Y Scenario:         +14.2%

Probability of Gain:
10D                    64%
1M                     69%

Fundamental Score      82
Technical Score        74
Quant ML Score         78
Meta Score             81
Valuation Score        71
Smart Money Score      76
Market Regime Score    68
Forensic Risk Score    21
Overall Risk Score     34

Why BUY?
✓ Strong earnings growth
✓ Positive relative strength
✓ ML probability above threshold
✓ Healthy cash conversion
✓ Favorable market regime

Risks:
⚠ Valuation moderately expensive
⚠ Volatility elevated
⚠ Earnings event approaching

Invalidation:
• Technical breakdown
• Earnings deterioration
• Forensic risk above threshold
• Market regime turns bearish
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

The final product should communicate **probability, evidence, risk, and reasoning**, rather than false precision.

---

# 24. Final Decision

## Proceed to Phase 4: YES

But build Phase 4 as:

```text
Decision Layer
+
Meta-Filter
+
Risk Engine
+
Regime Engine
+
Explainability
```

not as another standalone prediction model.

The current research foundation is strong enough to justify moving forward, while the final system still needs strict walk-forward and unseen-period validation before production use.
