package com.stocktics.presentation.learning

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

data class CurriculumStage(
    val stageNumber: Int,
    val title: String,
    val lessonRange: String
)

val CURRICULUM_STAGES = listOf(
    CurriculumStage(1, "Stage 1 — Stock Market Foundations", "Lessons 1–7"),
    CurriculumStage(2, "Stage 2 — Company Quality", "Lessons 8–11"),
    CurriculumStage(3, "Stage 3 — Valuation", "Lessons 12–16"),
    CurriculumStage(4, "Stage 4 — Quantitative Investing", "Lessons 17–22"),
    CurriculumStage(5, "Stage 5 — Data & Pattern Discovery", "Lessons 23–27"),
    CurriculumStage(6, "Stage 6 — Backtesting", "Lessons 28–32"),
    CurriculumStage(7, "Stage 7 — Portfolio Construction", "Lessons 33–35"),
    CurriculumStage(8, "Stage 8 — Real Investing Process", "Lessons 36–40")
)

data class FullLessonDefinition(
    val lessonNumber: Int,
    val stageNumber: Int,
    val stageTitle: String,
    val title: String,
    val icon: ImageVector,
    val coreQuestion: String,
    val primaryFormula: String,
    val researcherRule: String,
    val whatDataDoesNotProve: String,
    val assetInfographicPath: String? = null,
    val keyCheckpoints: List<String>,
    val fullNotesOverview: String,
    val conceptsSummary: Map<String, String>,
    val compoundingRealityCheck: String? = null,
    val fictionalExample: String? = null,
    val realStockExample: String? = null,
    val commonMistakes: List<Pair<String, String>>,
    val goldenRules: List<String>
)

val ALL_40_LESSONS = listOf(
    // ------------------------------------------------------------------------
    // STAGE 1: STOCK MARKET FOUNDATIONS (Lessons 1 - 7)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 1,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "What Happens When I Buy or Sell a US Stock?",
        icon = Icons.Rounded.Speed,
        coreQuestion = "What exactly happens when I place an order on a US exchange? Why is nominal share price completely disconnected from enterprise valuation?",
        primaryFormula = "Market Cap = P × N_shares  |  Relative Spread % = ((Ask - Bid) / Mid) × 100",
        researcherRule = "Never judge valuation by nominal share price alone. Always evaluate Market Capitalization. Transact with Limit Orders between 10:00 AM and 3:30 PM ET to eliminate spread slippage.",
        whatDataDoesNotProve = "A narrow bid-ask spread proves the stock is liquid; it does NOT prove the business is good, undervalued, or safe from drawdowns.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_01_infographic.jpg",
        keyCheckpoints = listOf(
            "Stock & Share: Fractional legal equity ownership with a residual claim on net assets and cash flows.",
            "Exchanges: NYSE (Designated Market Makers) vs. Nasdaq (Decentralized electronic ECN dealers).",
            "Market Cap: Total value of the pie (Price × Shares Outstanding). A $10 stock can be 10x larger than a $1,000 stock.",
            "Order Book: Bid (buyer demand), Ask (seller supply), and Bid-Ask Spread (liquidity cost).",
            "Order Types: Market Orders demand speed but guarantee NO price; Limit Orders guarantee price discipline.",
            "Settlement: Accelerated T+1 settlement cycle via DTCC/NSCC (effective May 2024).",
            "Trading Sessions: Pre-market (4:00-9:30 AM ET), Regular (9:30 AM-4:00 PM ET), After-hours (4:00-8:00 PM ET).",
            "International Realities (India): RBI LRS $250k/yr quota, 20% TCS above ₹7L, 25% US dividend withholding tax under DTAA, and USD/INR FX compounding."
        ),
        fullNotesOverview = """
            When you buy or sell a US stock, you are not merely trading a digital ticket—you are acquiring proportional legal ownership in a public corporation registered under the SEC. 
            
            Every transaction is routed through a broker's Smart Order Router (SOR) to national exchanges (NYSE/Nasdaq) or wholesale internalizers under Reg NMS (Rule 611 NBBO protection). The trade clears through the NSCC and settles on an accelerated T+1 cycle managed by the DTCC.
            
            For international investors funding from India under the RBI LRS scheme, net portfolio returns are compounded through dual-currency shifts:
            R_total,INR = (1 + R_stock,USD) × (1 + R_USD/INR) - 1.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Ownership & Equity" to "A share is a proportional claim on corporate assets after satisfying debt liabilities.",
            "NYSE vs. Nasdaq" to "NYSE uses Designated Market Makers (DMMs) for auctions; Nasdaq uses multi-dealer electronic networks.",
            "Market Orders vs. Limit Orders" to "Market orders cross the spread and suffer slippage. Limit orders provide liquidity at your exact price.",
            "Settlement T+1" to "US equities legally transfer ownership and cash 1 business day after trade execution.",
            "Trading Sessions" to "Mid-day regular hours (10:00 AM-3:30 PM ET) offer the lowest spreads. Off-hours carry 10x to 50x spread penalties."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Over 3 years, $10,000 becomes $309,127 (31x). Over 5 years, it becomes $3,044,816 (304x).
            Warren Buffett's 58-year CAGR is ~19.8%, and the S&P 500 averages ~10.2%. A 213.8% annual return requires a theoretical Sharpe Ratio > 8.0—statistically impossible without catastrophic risk of ruin. In this curriculum, our focus is evidence-based risk-adjusted compounding.
        """.trimIndent(),
        fictionalExample = """
            Nova Robotics Inc. (NVRO):
            2,000,000 shares outstanding @ $50.00 = $100M Market Cap.
            Ask Book: 400 shares @ $50.05, 600 @ $50.10, 1,200 @ $50.25.
            A market buy of 1,500 shares sweeps all 3 levels:
            (400×$50.05 + 600×$50.10 + 500×$50.25) / 1,500 = $50.137 VWAP.
            Slippage paid = $0.087/share (0.17% instant loss).
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL):
            ~15.12 Billion shares outstanding @ ~$228.00 = ~$3.45 Trillion Market Cap.
            Regular Hours Spread: $228.01 Bid / $228.02 Ask = $0.01 (0.0044% friction).
            Pre-Market Spread: $227.60 Bid / $228.40 Ask = $0.80 (0.351% friction) — 80x wider!
        """.trimIndent(),
        commonMistakes = listOf(
            "Using Market Orders in Off-Hours" to "Executing 1% to 5% away from fair value due to thin order books.",
            "The Low-Price Trap" to "Assuming a $2 stock is 'cheaper' than a $200 stock while ignoring share dilution.",
            "Trading at 9:30 AM Market Open" to "Rushing into opening volatility and uncrossed auction imbalances.",
            "Ignoring Currency & Remittance Drag" to "Overlooking bank FX markups (1-1.5%) and dividend withholding taxes."
        ),
        goldenRules = listOf(
            "The Market Cap Rule: Always calculate Market Cap = P × N_shares before forming any valuation opinion.",
            "The Limit Order Rule: Always use Limit Orders to guarantee price certainty.",
            "The Core Liquidity Window: Transact between 10:00 AM ET and 3:30 PM ET.",
            "The T+1 Rule: Remember proceeds take 1 full business day to settle before withdrawal.",
            "The Compounding Sanity Rule: Benchmark against realistic institutional CAGRs (10-25%)."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 2,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Understanding Financial Statements",
        icon = Icons.Rounded.AccountBalance,
        coreQuestion = "How do the Income Statement, Balance Sheet, and Cash Flow Statement interconnect to reveal corporate health? Why does accounting Net Income diverge from Free Cash Flow?",
        primaryFormula = "Assets = Liabilities + Equity  |  Gross Profit = Rev - COGS  |  FCF = CFO - CapEx  |  FCF Conversion = (FCF / NI) × 100",
        researcherRule = "Never evaluate an Income Statement in isolation. Always reconcile Net Income against Cash Flow from Operations (CFO). Accounting revenue and earnings are vulnerable to aggressive accruals; cash in the bank cannot be fabricated without criminal collusion.",
        whatDataDoesNotProve = "Surging accounting Net Income does NOT prove that a business is solvent. If receivables balloon or CapEx exceeds operating cash flow, a highly 'profitable' company can suffer technical insolvency.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_02_infographic.jpg",
        keyCheckpoints = listOf(
            "SEC Filings Ground Truth: Form 10-K (Annual Audited Report), Form 10-Q (Quarterly Reviewed Report), and Form 8-K (Material Disclosures).",
            "Income Statement (Accrual Waterfall): Revenue → COGS → Gross Profit → OpEx (R&D + SG&A) → Operating Income (EBIT) → Net Income.",
            "Balance Sheet (Point-in-Time Snapshot): The fundamental identity Assets = Liabilities + Stockholders' Equity. Working Capital and Net Debt.",
            "Cash Flow Statement (Three Pillars): Cash from Operations (CFO), Cash from Investing (CapEx), and Cash from Financing (Debt, Dividends, Buybacks).",
            "Free Cash Flow (FCF): The golden metric (CFO - CapEx) representing cash distributable to equity holders without impairing operations.",
            "The Sloan Accrual Anomaly: Why academic finance proves that firms with high accruals and low cash flow systematically underperform over 3-5 year horizons."
        ),
        fullNotesOverview = """
            Financial statements are not accounting bureaucracy; they are the structured telemetry of an operating business. 
            
            Under US securities law, all publicly traded firms file standardized, audited disclosures to the SEC via EDGAR: Form 10-K (annual audited report within 60-90 days), Form 10-Q (quarterly report 3 times a year), and Form 8-K (material unscheduled events within 4 business days).
            
            The three financial statements represent three different mathematical perspectives of the enterprise:
            1. Income Statement (Accrual Flow): Measures economic performance over a period under GAAP matching rules.
            2. Balance Sheet (Stock/Snapshot): Measures accumulated assets, liabilities, and book equity at an exact date (Assets = Liabilities + Equity).
            3. Cash Flow Statement (Pure Liquidity Flow): Strips away all non-cash accruals and reconciles the beginning and ending cash balances through Operations, Investing, and Financing.
            
            The ultimate analytical objective is determining whether accounting earnings convert into real, unencumbered Free Cash Flow (FCF = CFO - CapEx) that compounds book value per share.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Form 10-K & 10-Q" to "Regulatory ground truth filed with the SEC under strict GAAP standards, audited by certified independent CPAs.",
            "Accrual Accounting" to "Revenue is recognized when performance obligations are satisfied, not when cash is received.",
            "Balance Sheet Equation" to "Assets (economic resources) = Liabilities (creditor claims) + Stockholders' Equity (residual owner claims).",
            "Cash from Operations" to "Net income adjusted for non-cash items (Depreciation & Amortization, SBC) and net changes in Working Capital.",
            "Free Cash Flow (FCF)" to "Operating Cash Flow minus Capital Expenditures. The true cash surplus available for dividends, buybacks, or debt retirement."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            World-class compounders like Microsoft, Apple, or Alphabet compound Free Cash Flow at 12% to 25% annualized over long cycles. Any strategy promising 213.8% annual returns is mathematically reliant on extreme leverage or high-frequency tail risk that inevitably blows up. In this curriculum, our valuation models are anchored in durable Free Cash Flow generation.
        """.trimIndent(),
        fictionalExample = """
            Dexa Systems Inc. (DXA):
            Income Statement: Revenue = $500M (+40% YoY), Net Income = $100M (20% Net Margin).
            Wall Street celebrates the 'growth' earnings beat.
            However, Cash Flow Statement reveals:
            Accounts Receivable ballooned from $40M to $160M (+$120M uncollected cash).
            Cash Flow from Operations (CFO) = -$20M!
            CapEx = $30M → Free Cash Flow = -$50M.
            The company burned $50M in actual cash despite reporting $100M of accounting 'profit'. Six months later, customers defaulted, forcing a $90M write-off and emergency dilutive equity financing, cratering the stock 65%.
        """.trimIndent(),
        realStockExample = """
            Microsoft Corporation (NASDAQ: MSFT, FY2024 Audited Form 10-K):
            • Revenue: $245.12B (+15.67% YoY)
            • Cost of Revenue (COGS): $74.11B
            • Gross Profit: $171.01B (69.77% Gross Margin)
            • Operating Expenses: $61.58B (R&D $29.51B, SG&A $32.07B)
            • Operating Income (EBIT): $109.43B (44.64% Operating Margin)
            • Net Income: $88.14B (35.96% Net Margin)
            • Cash from Operations (CFO): $118.55B
            • Capital Expenditures (CapEx): $44.48B (Cloud AI data center buildout)
            • Free Cash Flow: $74.07B
            • FCF Conversion Rate: 84.04% ($74.07B FCF / $88.14B Net Income)
            Scorecard: Elite cash quality. CFO exceeds Net Income by $30.41B.
        """.trimIndent(),
        commonMistakes = listOf(
            "Evaluating P/E Without Checking FCF" to "A company with a low P/E can be rapidly burning cash due to aggressive receivables or high CapEx requirements.",
            "Ignoring Stock-Based Compensation (SBC)" to "SBC is added back to CFO as a non-cash expense, but it dilutes existing shareholders if not offset by buybacks.",
            "Treating EBITDA as Free Cash Flow" to "Charlie Munger warned that EBITDA ignores CapEx and depreciation necessary to replace decaying physical assets.",
            "Overlooking Net Debt & Interest Burden" to "High accounting profits can be completely wiped out if floating debt matures into high interest rate regimes."
        ),
        goldenRules = listOf(
            "The Three-Statement Rule: Never evaluate an Income Statement without cross-checking the Balance Sheet and Cash Flow Statement.",
            "The 80% FCF Conversion Rule: Require 3-year cumulative Free Cash Flow to exceed 80% of reported Net Income.",
            "The Net Debt Solvency Gate: Require Net Debt / EBITDA to be below 3.0x (or negative Net Debt / Net Cash).",
            "The Working Capital Canary: Watch Accounts Receivable growing faster than Revenue as a leading indicator of revenue distortion.",
            "The Maintenance CapEx Test: Verify whether CapEx exceeds Depreciation to confirm that management is reinvesting in future growth."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 3,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Revenue and Business Growth",
        icon = Icons.AutoMirrored.Rounded.ShowChart,
        coreQuestion = "How fast, how profitably, and through what structural mechanisms is the business expanding its top-line customer revenue? Is growth organic or debt-fueled M&A?",
        primaryFormula = "YoY Growth % = ((Rev_t - Rev_{t-1}) / Rev_{t-1}) × 100  |  CAGR = (Rev_n / Rev_0)^(1/n) - 1  |  NRR % = ((Start + Exp - Cont - Churn) / Start) × 100  |  Delta g = g_t - g_{t-1}",
        researcherRule = "Differentiate between organic revenue growth and acquisition-driven roll-ups that mask underlying customer churn. Track the 2nd derivative of revenue (acceleration vs. deceleration); valuation multiples correlate with the change in growth rate rather than absolute growth alone.",
        whatDataDoesNotProve = "High historical revenue growth does NOT guarantee high future growth due to the Law of Large Numbers and TAM saturation. Nor does it prove solvency or profit if unit economics are negative.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_03_infographic.jpg",
        keyCheckpoints = listOf(
            "ASC 606 Revenue Recognition: Performance obligations, transaction price allocation, and unearned Deferred Revenue liabilities.",
            "YoY vs. QoQ Seasonality: Why annual comps eliminate quarterly holiday distortions while QoQ detects immediate macro inflection points.",
            "Compound Annual Growth Rate (CAGR): Smoothing multi-year top-line expansion across 3 to 5-year macro cycles.",
            "Organic vs. Acquired (M&A) Growth: Uncovering serial acquirers masking negative 15% organic churn with debt-fueled buyouts.",
            "Recurring Revenue Flywheel: Annual Recurring Revenue (ARR), Net Revenue Retention (NRR > 120%), and Customer Churn.",
            "Revenue Quality Scorecard: Gross margin attachment, contract duration, cash conversion (DSO), and SEC Footnote customer concentration.",
            "The 2nd Derivative of Revenue: Why stock valuation multiples expand on acceleration (Delta g > 0) and compress violently on deceleration."
        ),
        fullNotesOverview = """
            Top-line revenue is the ultimate constraint on corporate value creation. A company can optimize operating expenses, streamline administrative overhead, and perform financial engineering through share repurchases, but it cannot cut its way to long-term compounding without revenue growth.
            
            Under US GAAP (ASC 606), revenue is recognized when control of goods or services is transferred to customers. An institutional quantitative analyst evaluates revenue along five critical dimensions:
            1. Temporal Compounding: Measuring multi-year CAGR rather than single-year base-effect anomalies.
            2. Structural Origin: Isolating organic expansion from debt-financed acquisition roll-ups.
            3. Contractual Predictability: Quantifying recurring subscription revenue (ARR) and cohort expansion (Net Retention Rate NRR).
            4. Quality & Concentration: Ensuring high gross margin attachment and verifying that no single customer accounts for >10% of total sales.
            5. Second-Derivative Momentum: Tracking whether revenue growth is accelerating (Delta g > 0) or decelerating (Delta g < 0), which directly drives multiple expansion and contraction.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "ASC 606 Revenue Recognition" to "GAAP standard requiring revenue to be earned and realizable upon satisfying performance obligations.",
            "YoY vs. QoQ Seasonality" to "Year-over-Year comps remove predictable holiday and seasonal spikes. QoQ tracks immediate quarterly momentum.",
            "Multi-Year CAGR" to "Smoothed compound annual growth rate over n years: (Rev_n / Rev_0)^(1/n) - 1.",
            "Organic vs Inorganic M&A" to "Organic growth reflects true customer demand; M&A roll-ups frequently mask organic deterioration.",
            "Net Retention Rate (NRR)" to "Measures recurring spend from existing cohorts. NRR > 120% signifies an elite compounding SaaS flywheel.",
            "2nd-Derivative Acceleration" to "The rate of change of growth (Delta g = g_t - g_{t-1}). Multiples expand when growth accelerates and compress on deceleration."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Real enterprises cannot grow revenue at 213.8% annually over extended horizons. The Law of Large Numbers dictates that as an enterprise scales beyond $50B-$100B, its growth mathematically converges toward industry GDP growth (5-15%). Even NVIDIA's historic AI hyper-growth experienced severe cyclical deceleration in FY23 (+0.2% YoY). Sustainable long-term compounders generate 12% to 25% revenue CAGR with resilient unit margins.
        """.trimIndent(),
        fictionalExample = """
            OrganicFlow Systems vs. Rollup Holdings:
            Both report $100M Year 1 and $140M Year 2 (+40.0% reported growth).
            • OrganicFlow Systems: $0 acquisitions. Starting $100M ARR cohort had $5M churn and $45M expansion (NRR = 140%). Pure organic compounding with zero debt.
            • Rollup Holdings: Base operations declined from $100M to $85M (-15% organic decline). Acquired two legacy firms for $120M of debt to add $55M of revenue. Total reported = $85M + $55M = $140M (+40%).
            Analyst Verdict: Surface screeners see two 40% growth firms. Forensic analysis reveals OrganicFlow is an elite compounder, while Rollup Holdings is hiding organic collapse behind dilutive debt.
        """.trimIndent(),
        realStockExample = """
            NVIDIA Corporation (NASDAQ: NVDA, 5-Year Audited Form 10-K):
            • FY21 Revenue: $16.68B (+52.7% YoY)
            • FY22 Revenue: $26.91B (+61.4% YoY) | Acceleration: +8.7%
            • FY23 Revenue: $26.97B (+0.2% YoY) | Deceleration: -61.2% (Crypto winter & PC inventory overhang, stock crashed 65%)
            • FY24 Revenue: $60.92B (+125.9% YoY) | Hyper-Acceleration: +125.7% (GenAI H100 GPU explosion)
            • FY25 Revenue: $130.50B (+114.2% YoY) | Data Center reached $115.1B (88.2% of company total)
            • 4-Year CAGR: ((130,497 / 16,675)^(1/4) - 1) = 67.2% annualized top-line compound rate.
        """.trimIndent(),
        commonMistakes = listOf(
            "Linear Growth Extrapolation" to "Assuming a company growing 50% will continue for 5 years while ignoring TAM limits and the Law of Large Numbers.",
            "Ignoring Share Dilution" to "Celebrating 20% revenue growth when shares outstanding grew 25%, meaning Revenue Per Share actually declined.",
            "Customer Concentration Blindness" to "Failing to inspect 10-K footnotes where a single customer accounts for >20% of total revenue.",
            "Mistaking M&A for Organic Moat" to "Being fooled by serial acquirers accumulating massive goodwill balances and subsequent impairment write-downs.",
            "Paying Peak Multiples at Deceleration" to "Buying high-growth stocks at 30x P/S right as the 2nd derivative turns negative, causing severe multiple compression."
        ),
        goldenRules = listOf(
            "The Revenue Per Share Rule: Always verify that Revenue Per Share (RPS) is growing to ensure growth is not offset by dilution.",
            "The 3-Year CAGR Anchor: Benchmark top-line durability using a 3-year rolling CAGR rather than a single year's volatile spike.",
            "The 75% Organic Quality Gate: Require at least 75% of reported top-line expansion to stem from organic customer demand.",
            "The Second-Derivative Mandate: Track quarterly acceleration and deceleration; valuation multiples re-rate on the 2nd derivative.",
            "The Margin-Attached Test: Demand that revenue growth be accompanied by stable or expanding Gross Margins to avoid discounting traps."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 4,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Profitability and Margins",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "Does the business possess pricing power and operating leverage as volume increases? Is margin expansion structural or cyclical?",
        primaryFormula = "Gross Margin % = (GP / Rev) × 100  |  Operating Margin % = (EBIT / Rev) × 100  |  Net Margin % = (NI / Rev) × 100  |  DOL = (% Delta EBIT) / (% Delta Rev) = GP / EBIT",
        researcherRule = "Operating leverage is a double-edged sword: fixed overhead creates explosive profit growth when revenue expands, but causes severe earnings collapse when sales contract. Track the Degree of Operating Leverage (DOL) and distinguish structural product-mix shifts from temporary cyclical peaks.",
        whatDataDoesNotProve = "High gross margins do NOT guarantee net profits if corporate SG&A or R&D spending are undisciplined. Nor do peak cyclical margins prove a permanent economic moat.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_04_infographic.jpg",
        keyCheckpoints = listOf(
            "The 4-Tier Margin Architecture: Gross Margin (product pricing power), Operating Margin EBIT (core efficiency), EBITDA Margin (pre-D&A cash proxy), and Net Margin (residual equity claim).",
            "Pricing Power & Inflation Moat: How high gross margins (>60%) shield companies from supplier inflation and supply chain shocks.",
            "Operating Leverage Mechanics: High fixed costs + low variable costs = dramatic profit explosion when sales expand.",
            "Degree of Operating Leverage (DOL): Measuring the elasticity of EBIT growth relative to revenue growth (% Delta EBIT / % Delta Revenue).",
            "Structural Mix Shift: How Apple expanded Gross Margin from 38% to 46% by scaling Services (74% margin) to $96B.",
            "Cyclical vs. Structural Margin Expansion: Differentiating temporary commodity shortage pricing from permanent software/moat expansion.",
            "Margin Contraction Red Flag: The deadly combination of decelerating revenue and collapsing margins causing violent earnings contraction."
        ),
        fullNotesOverview = """
            Revenue is vanity, margin is sanity, cash is reality. A company can generate $100 Billion in sales, but if its cost structure consumes $101 Billion, each incremental transaction destroys shareholder value.
            
            Every audited corporate Income Statement decomposes profitability into four progressive tiers:
            1. Gross Margin: Direct pricing power over raw inputs and manufacturing costs (GP = Rev - COGS).
            2. Operating Margin (EBIT): Core operational efficiency after deducting R&D and SG&A overhead.
            3. EBITDA Margin: Pre-interest operating cash proxy used for debt-service analysis (EBIT + D&A).
            4. Net Profit Margin: The ultimate residual profit belonging to equity holders after all taxes, interest, and non-operating items.
            
            The central mathematical engine of corporate profitability is Operating Leverage (Degree of Operating Leverage, DOL). When a company possesses fixed overhead costs, incremental revenue flows directly to operating income at high conversion rates. Furthermore, companies with multiple business segments can achieve multi-year structural margin expansion by orchestrating a product-mix shift toward higher-margin software and recurring services.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Gross Margin (Pricing Power)" to "Measures value added over raw inputs. High gross margin reflects brand prestige and proprietary IP.",
            "Operating Margin (EBIT)" to "Measures core operational profitability after deducting R&D and SG&A, independent of debt and taxes.",
            "EBITDA Margin" to "Earnings before interest, taxes, and non-cash depreciation & amortization write-offs.",
            "Net Profit Margin" to "The bottom-line residual fraction of each revenue dollar belonging to common equity holders.",
            "Degree of Operating Leverage" to "The profit elasticity multiplier (DOL = % Delta EBIT / % Delta Rev). Drives exponential profit growth when sales scale.",
            "Structural Mix Shift" to "Expanding company-wide margins by growing high-margin service/software segments faster than legacy hardware."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Real operating margins cannot expand infinitely. Corporate margins are structurally bounded by competitive entry, customer price elasticity, taxation, and labor costs. The world's most profitable monopolies operate with net margins between 25% and 55%. True long-term wealth creation comes from identifying companies that can sustain 20%+ operating margins over 10-year horizons with disciplined capital allocation.
        """.trimIndent(),
        fictionalExample = """
            FlexiScale SaaS vs. ConsultingPro Services:
            Both generate $100M Year 1 and grow +20% to $120M in Year 2.
            • FlexiScale SaaS (High Fixed Costs): $10M variable costs + $70M fixed R&D = $20M EBIT (DOL = 4.5x). In Year 2, revenue rises to $120M, variable costs rise to $12M, fixed costs stay at $70M. Operating Income jumps from $20M to $38M (+90% EBIT growth! Margin expands from 20.0% to 31.7%).
            • ConsultingPro Services (Low Fixed Costs): $60M variable labor + $20M fixed rent = $20M EBIT (DOL = 2.0x). In Year 2, revenue rises to $120M, variable costs rise to $72M, fixed costs stay at $20M. Operating Income rises from $20M to $28M (+40% EBIT growth. Margin expands only to 23.3%).
            Verdict: FlexiScale's operating leverage delivered 2.25x greater profit expansion from the exact same 20% revenue growth!
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL, 5-Year Audited Form 10-K):
            • FY20 to FY24: Net Sales grew from $274.5B to $391.0B (+42.5%).
            • Gross Margin expanded +798 bps from 38.23% to 46.21%.
            • Operating Margin expanded +736 bps from 24.15% to 31.51%.
            • The Services Mix Driver: Services expanded from $53.8B (19.6% mix) to $96.2B (24.6% mix). Services carries a massive 73.9% gross margin vs. Products at 37.1%.
            • Operating Leverage Demonstration (FY20 to FY21): Revenue grew +33.26%, while Operating Income surged +64.36% (Realized DOL = 1.94x).
            • Resilience: In FY23, revenue dipped -2.8%, but Gross Margin actually expanded from 43.3% to 44.1%, proving extraordinary pricing power.
        """.trimIndent(),
        commonMistakes = listOf(
            "Confusing Gross Margin with Net Profit" to "Assuming an 80% gross margin guarantees profits while ignoring that SG&A and marketing burn 85% of sales.",
            "Ignoring Operating Leverage in Recessions" to "Forgetting that DOL cuts both ways: fixed overhead rapidly turns high-leverage compounders into loss-makers when sales drop.",
            "Treating EBITDA as Free Cash Flow" to "Ignoring that EBITDA omits real required capital expenditures needed to replace decaying physical assets.",
            "Buying Peak Cyclical Margins" to "Buying cyclical commodity firms at low P/E multiples during peak shortage margins right before earnings collapse.",
            "Overlooking Stock-Based Compensation (SBC)" to "Relying on non-GAAP 'Adjusted Operating Margin' that excludes billions in dilutive SBC grants."
        ),
        goldenRules = listOf(
            "The 3-Tier Waterfall Mandate: Never evaluate Gross, Operating, or Net Margin in isolation. Track how each dollar decays through the Income Statement.",
            "The 40% Gross Margin Quality Threshold: Require non-financial firms to maintain Gross Margin >= 40% as an economic inflation buffer.",
            "The Structural vs. Cyclical Test: Verify whether margin expansion stems from a permanent mix shift or temporary shortage pricing.",
            "The Degree of Operating Leverage Monitor: Track DOL quarterly. When revenue decelerates, high DOL creates severe profit compression.",
            "The Non-GAAP Skepticism Rule: Always reconcile management's Adjusted Operating Margin against GAAP operating margin to identify aggressive add-backs."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 5,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Earnings and EPS Quality",
        icon = Icons.Rounded.Speed,
        coreQuestion = "Is reported EPS growth real economic value creation or financial engineering via share buybacks and stock-based compensation? How do GAAP and non-GAAP earnings diverge?",
        primaryFormula = "Diluted EPS = (Net Income - Preferred Div) / Diluted Shares  |  Sloan Accrual Ratio = (Net Income - CFO) / Total Assets  |  Net Buyback Accretion % = ((Shares_{t-1} - Shares_t) / Shares_{t-1}) × 100  |  SBC Drag % = (SBC / Revenue) × 100",
        researcherRule = "Never evaluate EPS growth without examining the denominator (diluted share count) and cash alignment. If Net Income outpaces Cash from Operations (Sloan Accrual Anomaly > 0), earnings quality is artificially inflated and mean-reversion risk is severe. Reconcile non-GAAP add-backs to uncover real operational costs.",
        whatDataDoesNotProve = "An EPS 'beat' does NOT make a stock cheap or fundamentally sound. Management can manufacture a consensus beat by 2 cents through aggressive share buybacks, lowered tax rates, or capitalized expenses while core operating cash flow and revenue deteriorate.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_05_infographic.jpg",
        keyCheckpoints = listOf(
            "ASC 260 Earnings Per Share: Basic EPS vs. Diluted EPS (Treasury Stock Method, unvested RSUs, stock options, and convertible debt overhang).",
            "Stock-Based Compensation (SBC) Reality: Why SBC is a real economic expense to existing shareholders disguised as a 'non-cash add-back' on the Cash Flow Statement.",
            "Accretive vs. Dilutive Share Buybacks: Distinguishing genuine share count reduction from defensive buybacks that merely soak up executive dilution.",
            "The Non-GAAP Earnings Disparity: How corporate management manufactures 'Adjusted EPS' by adding back restructuring, litigation, amortization, and stock compensation.",
            "The Sloan Accrual Anomaly: Richard Sloan's empirical proof that high-accrual earnings systematically underperform cash-rich earnings over 1 to 3-year horizons.",
            "Beneish M-Score & Forensic Manipulation: Detecting aggressive revenue recognition, deferred expenses, and asset inflation before accounting restatements.",
            "Meta Platforms Form 10-K Forensic Case Study: Audited FY20 to FY24 breakdown of $100B+ repurchases, share count reduction from 2.85B to 2.54B (-10.9%), and EPS surging to $15.19."
        ),
        fullNotesOverview = """
            Earnings Per Share (EPS) is the single most quoted metric in financial media, yet it is the most vulnerable to accounting distortion and balance-sheet financial engineering. An institutional equity researcher deconstructs EPS into its mathematical components: the numerator (Net Income) and the denominator (Weighted Average Diluted Shares Outstanding).
            
            Under US GAAP (ASC 260), Diluted EPS reflects the potential dilution that could occur if securities or contracts to issue common stock (such as options, warrants, and restricted stock units under the Treasury Stock Method) were exercised. However, corporate managers frequently report 'Adjusted Non-GAAP EPS' that excludes Stock-Based Compensation (SBC), restructuring charges, and acquisition costs. In reality, SBC is a real economic transfer of wealth from public shareholders to corporate insiders.
            
            Furthermore, Professor Richard Sloan's landmark research identified the Accrual Anomaly: companies whose net income is driven by non-cash working capital accruals rather than Cash Flow from Operations (CFO) experience severe subsequent earnings declines. High-quality earnings are cash-backed, conservatively recognized, and accompanied by shrinking net share counts.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Basic vs. Diluted EPS" to "Basic uses actual shares outstanding; Diluted accounts for options, warrants, and RSUs via the Treasury Stock Method.",
            "Stock-Based Compensation (SBC)" to "Real equity compensation expense. Adding it back in Non-GAAP creates an illusion of higher operating profitability.",
            "Treasury Stock Method" to "GAAP formula assuming proceeds from in-the-money options are used by the company to buy back shares at market price.",
            "Accretive Share Buybacks" to "Repurchases executed below intrinsic value that permanently reduce shares outstanding and expand per-share owner earnings.",
            "Sloan Accrual Anomaly" to "Accruals = (Net Income - CFO) / Total Assets. Negative accruals indicate high cash quality; high positive accruals signal earnings decay.",
            "Non-GAAP Adjustments" to "Management-defined earnings metrics that add back recurring costs to artificially inflate reported operating margins."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Corporate earnings cannot compound at 213.8% annually over multi-year horizons. Over the last 50 years, S&P 500 operating EPS has grown at a long-term compound rate of ~6.8% nominal (including inflation). Even elite mega-cap technology monopolies compound EPS at 15% to 28% annualized during their highest-growth decades. Any investment thesis predicated on triple-digit annual EPS growth inevitably experiences violent mean-reversion as market saturation and competitive forces take hold.
        """.trimIndent(),
        fictionalExample = """
            DilutionTech Inc. vs. TrueCompounder Corp:
            Both companies report $100M in Net Income in Year 1 and $125M in Year 2 (+25.0% Net Income growth).
            • DilutionTech Inc.: Issued $30M in Stock-Based Compensation (SBC). Executed a $25M share buyback, but new RSU issuance outpaced repurchases. Weighted diluted shares grew from 50M to 52M (+4.0% dilution).
              Result: Diluted EPS grew from $2.00 to $2.40 (+20.0% EPS growth). Real economic EPS after subtracting SBC ($95M / 52M) was only $1.83!
            • TrueCompounder Corp: Zero SBC. Paid employees cash bonuses. Executed a $25M share buyback, shrinking shares from 50M to 47.5M (-5.0% net share reduction).
              Result: Diluted EPS surged from $2.00 to $2.63 (+31.5% EPS growth!).
            Verdict: TrueCompounder delivered 57% greater EPS expansion from the exact same net income, because buybacks permanently accreted to remaining owners rather than masking insider dilution!
        """.trimIndent(),
        realStockExample = """
            Meta Platforms (NASDAQ: META, 5-Year Audited Form 10-K):
            • FY20 to FY24 Diluted EPS Expansion: $10.09 to $15.19 (+50.5% total growth).
            • Net Income Growth: $29.15B (FY20) to $39.10B (FY24) (+34.1%).
            • The Share Buyback Multiplier: Diluted shares contracted from 2,854M to 2,543M (-10.9% net share reduction) through >$100B in cumulative share repurchases.
            • Dilution Analysis: 16.4 percentage points of Meta's 50.5% EPS growth came entirely from shrinking the share count!
            • The SBC Cost Reality: Meta granted $13.72B in SBC in FY23 (10.2% of revenue) and $14.12B in FY24 (8.8% of revenue). Meta's aggressive share buybacks ($20.0B in FY23, $27.0B in FY24) were necessary to overcome this massive insider dilution and deliver net per-share accretion.
            • Cash Quality: Meta's 5-year average (CFO / Net Income) ratio is 1.42x, confirming exceptional cash-backed earnings with negative Sloan accruals.
        """.trimIndent(),
        commonMistakes = listOf(
            "Trusting Non-GAAP 'Adjusted EPS' Blindly" to "Accepting management add-backs of Stock-Based Compensation, restructuring, and litigation as 'one-time' non-recurring costs.",
            "Treating All Share Buybacks as Accretive" to "Failing to check if diluted shares actually decreased, or if buybacks merely soaked up executive RSU option grants.",
            "Ignoring the Sloan Accrual Divergence" to "Buying companies where reported Net Income is surging while Operating Cash Flow is flat or negative.",
            "Overlooking Convertible Debt Overhang" to "Using Basic EPS instead of Diluted EPS, ignoring convertible notes that convert into shares upon price appreciation.",
            "Paying High Multiples for Buyback-Engineered EPS" to "Valuing a company at 25x P/E when revenue is flat and all EPS growth is debt-financed financial engineering."
        ),
        goldenRules = listOf(
            "The Diluted Share Count Rule: Verify that weighted diluted shares outstanding are declining or stable over rolling 3-year periods.",
            "The Sloan Accrual Quality Gate: Require the Sloan Accrual Ratio ((Net Income - CFO) / Total Assets) to be <= 0.0. Operating cash flow must exceed net income.",
            "The SBC-to-Revenue Ceiling: Flag any enterprise where Stock-Based Compensation exceeds 8% of total revenue as an excessive insider dilution tax.",
            "The GAAP Reconciliation Mandate: Always inspect the reconciliation table between GAAP Net Income and Non-GAAP Adjusted Net Income.",
            "The Owner Earnings EPS Standard: Compute true owner EPS by subtracting annual Stock-Based Compensation from reported earnings."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 6,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Cash Flow Quality",
        icon = Icons.Rounded.AccountBalance,
        coreQuestion = "Why do accounting profits and cash flows differ? How much pure cash can owners extract without harming the business?",
        primaryFormula = "Free Cash Flow (FCF) = CFO - CapEx  |  FCF Conversion % = (FCF / Net Income) × 100  |  FCF Margin % = (FCF / Rev) × 100  |  CapEx Intensity % = (CapEx / CFO) × 100",
        researcherRule = "Accounting earnings are an opinion; cash flow is a verified fact. Always demand 3-year cumulative FCF conversion > 80%. Watch for the Working Capital Canary: if Accounts Receivable or unsold inventory balloon while operating cash flow turns negative, reported net income is phantom profit.",
        whatDataDoesNotProve = "Surging accounting profit does NOT prove a company is solvent. A company can report record Net Income on paper while burning cash and heading toward bankruptcy if cash is locked in uncollected customer receivables or excessive inventory.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_06_infographic.jpg",
        keyCheckpoints = listOf(
            "The 3 Pillars of Cash Flow: Operating Activities (CFO), Investing Activities (CFI), and Financing Activities (CFF).",
            "Free Cash Flow (FCF = CFO - CapEx): The unencumbered cash surplus available to equity holders for buybacks, dividends, or debt retirement.",
            "FCF Conversion Rate: Dividing Free Cash Flow by GAAP Net Income; elite compounders consistently convert >80% of net income into cash.",
            "Why Accounting Profit and Cash Diverge: Non-cash expenses (D&A, SBC) and Working Capital swings (Accounts Receivable, Inventory, Accounts Payable).",
            "Maintenance vs. Growth CapEx: Distinguishing mandatory asset replacement from discretionary capacity expansion.",
            "The Negative Working Capital Float Flywheel: How companies collect cash upfront from customers before paying suppliers on 60-90 day terms.",
            "Alphabet Inc. Case Study: 5-Year audited SEC 10-K breakdown showing $312B in cumulative Free Cash Flow and the 2024 AI CapEx expansion to $52.5B."
        ),
        fullNotesOverview = """
            Accounting earnings are an opinion; cash flow is an indisputable reality. A company can report record Net Income on its Income Statement while marching toward insolvency on its Cash Flow Statement.
            
            Under US GAAP (ASC 230), the Statement of Cash Flows reconciles beginning and ending cash balances across three fundamental activities:
            1. Cash from Operations (CFO): Pure cash generated from customers after adjusting for non-cash items (Depreciation & Amortization, Stock-Based Compensation) and Working Capital swings.
            2. Cash from Investing (CFI): Capital deployed into long-term assets, primarily Capital Expenditures (CapEx) for factories, equipment, and AI server infrastructure.
            3. Cash from Financing (CFF): Cash distributed to or received from capital providers, including share repurchases, dividends, and debt issuance.
            
            The ultimate analytical standard for common equity investors is Free Cash Flow (FCF = CFO - CapEx). Free cash flow represents the true unencumbered cash surplus that management can deploy for dividends, value-accretive buybacks, or debt retirement without impairing the ongoing competitive engine of the enterprise.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Cash Flow from Operations (CFO)" to "Net income adjusted for non-cash expenses (D&A, SBC) and net changes in working capital accounts.",
            "Free Cash Flow (FCF)" to "CFO minus Capital Expenditures. The gold standard metric of owner cash generation.",
            "FCF Conversion Rate" to "FCF divided by Net Income. Ratios >80% indicate clean earnings with minimal accrual distortion.",
            "FCF Margin" to "Free cash flow generated as a percentage of total revenue (FCF / Revenue).",
            "CapEx Reinvestment Intensity" to "CapEx divided by CFO. Shows how much cash flow must be plowed back into physical equipment.",
            "Working Capital Swings" to "Changes in Accounts Receivable and Inventory that trap or release operational liquidity."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Real businesses cannot compound Free Cash Flow at 213.8% annually over multi-year horizons. The world's greatest cash-generating monopolies (Alphabet, Microsoft, Apple) compound Free Cash Flow at 12% to 22% annualized over multi-decade cycles. Attempting to achieve triple-digit annual cash yields forces investors into high-yield debt traps, predatory lending, or speculative crypto schemes that carry a 100% risk of total capital destruction. In this curriculum, our models are anchored in durable audited cash generation.
        """.trimIndent(),
        fictionalExample = """
            AccrualKing Industries vs. CashMachine Systems:
            Both companies report $100M in Net Income in the same fiscal year.
            • AccrualKing Industries: Net Income = $100M. However, Accounts Receivable jumped -$60M (unpaid invoices) and unsold inventory rose -$40M. CFO was only $30M. CapEx was -$55M.
              Result: Free Cash Flow = -$25M (Cash Burn!). The company burned $25M in actual cash despite reporting $100M of paper profit, and was forced to issue high-interest debt to survive.
            • CashMachine Systems: Net Income = $100M. Working capital was tightly managed. CFO was $130M. CapEx was -$25M.
              Result: Free Cash Flow = +$105M (105% FCF Conversion!). The company funded a $50M share buyback and added $55M to its cash reserves without borrowing a dollar.
            Verdict: Surface screeners see two $100M profit firms. Forensic cash flow analysis reveals CashMachine is a cash fortress, while AccrualKing is approaching technical insolvency!
        """.trimIndent(),
        realStockExample = """
            Alphabet Inc. (NASDAQ: GOOGL, 5-Year Audited Form 10-K):
            • FY20 to FY24 Cumulative Free Cash Flow: $312.1 Billion ($62.4B annual average).
            • FY20 FCF: $42.84B on $40.27B Net Income (106.4% FCF Conversion).
            • FY21 FCF: $67.01B on $76.03B Net Income (88.1% FCF Conversion).
            • FY22 FCF: $60.01B on $59.97B Net Income (100.1% FCF Conversion).
            • FY23 FCF: $69.50B on $73.80B Net Income (94.2% FCF Conversion).
            • FY24 FCF: $72.74B on $100.63B Net Income (72.3% FCF Conversion) — CapEx expanded 62.8% from $32.3B to $52.5B to build custom TPU and Nvidia GPU AI data centers.
            • Capital Allocation: Deployed $264.4 Billion into share buybacks over 5 years, funded entirely by Free Cash Flow without taking on net corporate debt!
        """.trimIndent(),
        commonMistakes = listOf(
            "Confusing EBITDA with Free Cash Flow" to "Treating EBITDA as cash, ignoring that EBITDA omits mandatory capital expenditures needed to replace physical assets.",
            "Ignoring Stock-Based Compensation (SBC)" to "Celebrating high FCF without deducting SBC, which dilutes existing shareholders if not offset by buybacks.",
            "Penalizing Productive Growth CapEx" to "Selling a stock because FCF temporarily dips due to high-ROI growth investments (e.g. AI servers).",
            "Ignoring Accounts Receivable Build-Up" to "Failing to notice uncollected receivables growing 3x faster than revenue on the cash flow statement.",
            "Valuing Cyclicals on Peak Single-Year FCF" to "Using a single boom year's cash flow to value cyclical businesses whose cash flows collapse in downturns."
        ),
        goldenRules = listOf(
            "The 80% FCF Conversion Rule: Require cumulative 3-year Free Cash Flow to exceed at least 80% of reported GAAP Net Income.",
            "The SBC-Adjusted FCF Test: Deduct annual Stock-Based Compensation from Free Cash Flow to determine true net owner cash.",
            "The CapEx Reinvestment Ceiling: Avoid companies where maintenance CapEx eats >50% of Operating Cash Flow over full cycles.",
            "The Working Capital Canary: Watch for continuous negative working capital drains (inventory piling up, unpaid invoices ballooning).",
            "The 5-Year Cumulative Standard: Evaluate cash flow over a 5-year cycle to eliminate single-year lumpiness and seasonal distortions."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 7,
        stageNumber = 1,
        stageTitle = "Stage 1 — Stock Market Foundations",
        title = "Balance Sheet and Solvency",
        icon = Icons.Rounded.Security,
        coreQuestion = "Can the company withstand severe recessions, debt maturities, and interest rate spikes without risk of insolvency?",
        primaryFormula = "Net Debt = Total Debt - Cash & Equiv  |  Debt/EBITDA = Total Debt / EBITDA  |  Interest Coverage = EBIT / Interest Exp  |  Debt Payoff Horizon = Total Debt / FCF  |  Altman Z = 1.2X1 + 1.4X2 + 3.3X3 + 0.6X4 + 0.999X5",
        researcherRule = "Income statements measure the glory of the boom; the balance sheet decides who survives the bust. Always demand Net Debt / EBITDA < 2.0x, Debt / FCF < 3.0 years, and EBIT Interest Coverage > 5.0x. An authentic Net Cash Fortress (Cash > Total Debt) eliminates bankruptcy risk and enables predatory counter-cyclical acquisitions during market panics.",
        whatDataDoesNotProve = "Zero debt does NOT automatically prove superior management if excessive cash is hoarded at subpar treasury yields rather than deployed into high-ROIC reinvestment or accretive buybacks. Conversely, negative working capital does NOT prove insolvency when driven by an elite customer cash float.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_07_infographic.jpg",
        keyCheckpoints = listOf(
            "The Liquidity Hierarchy: Cash & Cash Equivalents, Short-Term Marketable Securities, and Non-Current Liquid Bonds.",
            "Net Debt vs. Net Cash Fortress: Subtracting cash reserves from funded interest-bearing debt obligations.",
            "Working Capital Dynamics: Current Assets minus Current Liabilities; analyzing the negative working capital float advantage.",
            "Debt-to-EBITDA & Net Debt-to-EBITDA: Standard institutional credit metrics (<1.5x pristine, >3.5x elevated distress risk).",
            "Debt-to-Free Cash Flow Payoff Horizon: Quantifying the exact number of years of pure cash generation required to pay off all debt.",
            "Interest Coverage Ratio (EBIT / Interest Expense): Ensuring operating earnings cushion against rate shocks (>5.0x safe, <2.0x danger).",
            "The Altman Z-Score: The 5-factor multivariate credit scoring formula predicting insolvency probability (Safe > 2.99, Distress < 1.81).",
            "Apple Inc. Audited 10-K Solvency Case Study: Managing a \$106.6B debt load backed by \$65.3B in liquid cash with 31.3x interest coverage."
        ),
        fullNotesOverview = """
            The balance sheet is the ultimate anchor of corporate survival. While an Income Statement can record non-cash revenue and an operating quarter can fluctuate with seasonal demand, debt contracts are legally binding obligations: if interest or principal cannot be paid when due, equity holders can be wiped out completely.
            
            Under US GAAP (ASC 210), financial assets and obligations are segregated into Current (< 1 year) and Non-Current (> 1 year). In data-driven fundamental analysis, we scrutinize three interconnected solvency dimensions:
            1. Short-Term Liquidity: Working Capital (Current Assets - Current Liabilities) and the Quick Ratio, verifying that upcoming obligations can be settled without emergency distress financing.
            2. Structural Leverage: Total Funded Debt relative to enterprise earning power (Debt/EBITDA) and cash extraction power (Debt/FCF).
            3. Coverage Safety: The Interest Coverage Ratio (EBIT / Interest Expense), which proves how much operating profit can contract before the company fails to service debt.
            
            For non-financial corporations, the ultimate quantitative solvency benchmark is the Altman Z-Score, which synthesizes working capital, cumulative retained earnings, asset productivity, market equity cushion, and asset turnover into a single predictive credit metric.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Cash & Equivalents" to "Currency, bank demand deposits, and liquid short-term paper maturing within 90 days.",
            "Total Funded Debt" to "Short-term borrowings, current portions of notes, senior notes, term loans, and finance leases.",
            "Net Debt" to "Total Debt minus liquid Cash and Marketable Securities. Negative Net Debt equals a Net Cash Fortress.",
            "Debt-to-EBITDA" to "Total Debt divided by annual EBITDA. Standard institutional credit threshold: < 2.5x safe, > 3.5x risky.",
            "Debt-to-FCF Horizon" to "Total Debt divided by Free Cash Flow. The number of operating years needed to extinguish 100% of debt.",
            "Interest Coverage Ratio" to "Operating Income (EBIT) divided by Interest Expense. Multiples >5.0x protect against monetary tightening.",
            "Working Capital Float" to "Negative working capital where customers pay cash upfront before suppliers are paid on 60-90 day terms.",
            "Altman Z-Score" to "Five-factor multivariate formula predicting bankruptcy risk (Safe Zone > 2.99, Distress Zone < 1.81)."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Consider the borrowing rates of corporate debt: elite investment-grade companies issue debt at 4% to 6% annually, while junk-rated companies pay 9% to 12% annually. When a company is burdened with debt carrying even a 10% annual interest rate, the debt service frequently triggers financial distress and bankruptcy. Chasing an unrealistic 213.8% annual return invariably lures investors into overleveraged, high-beta companies that face catastrophic equity wiping during credit contractions. In this curriculum, we prioritize capital preservation through pristine balance sheets.
        """.trimIndent(),
        fictionalExample = """
            FortressHoldings Corp vs. LeveragedZombie Inc:
            Both companies generate $100M in annual EBITDA during a normal economic year.
            • LeveragedZombie Inc: Carries $550M in Total Debt (5.5x Debt/EBITDA) with $10M in cash. Annual interest expense at 7.0% is $38.5M. EBIT is $60M, giving an Interest Coverage of 1.56x.
              Recession Strike: Revenue dips 15%, causing EBITDA to drop to $65M and EBIT to drop to $25M.
              Disaster: Operating EBIT ($25M) is now LESS than mandatory interest expense ($38.5M)! The company suffers a -$13.5M pre-tax deficit, breaches its debt covenants, and faces debt restructuring that wipes out 90% of common equity.
            • FortressHoldings Corp: Carries $80M in Total Debt with $120M in Cash ($40M Net Cash Fortress!). Annual interest expense is $4.0M.
              Recession Strike: Same 15% revenue drop. FortressHoldings generates $21M in EBIT after $4M interest.
              The Fortress Advantage: With $120M in cash, FortressHoldings acquires LeveragedZombie's core assets from bankruptcy at a 70% discount!
            Verdict: Leverage is financial kryptonite in a downturn. Fortress balance sheets generate predatory counter-cyclical growth!
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL, 5-Year Audited Form 10-K):
            • FY20 to FY24 Debt & Cash Trajectory: Total Debt contracted from $112.4B (FY20) to $106.6B (FY24), while Cash & Current Marketable Securities totaled $65.3B (Total liquid cash & long-term securities exceeded $156B).
            • Net Cash Neutral Strategy: Deliberately issued ultra-low-cost fixed debt (1.5% - 3.5%) to fund massive share repurchases while preserving liquid reserves.
            • Debt / EBITDA Multiple: Consistently held below 1.0x (FY24: $106.6B Debt / $134.7B EBITDA = 0.79x).
            • Debt / FCF Payoff Horizon: Outstanding debt represents only 0.98 years of Apple's $108.8B Free Cash Flow!
            • Interest Coverage Ratio: In FY24, generated $123.2B in EBIT against $3.94B in interest expense (31.3x Interest Coverage).
            • Altman Z-Score: > 8.0, placing Apple at the pinnacle of the institutional Safe Zone (> 2.99).
        """.trimIndent(),
        commonMistakes = listOf(
            "Ignoring the Debt Maturity Wall" to "Failing to check the 10-K footnotes for upcoming debt maturities that must be refinanced at higher prevailing interest rates.",
            "Confusing Negative Working Capital with Insolvency" to "Mistaking elite negative working capital cash floats (Apple, Amazon) for liquidity shortages.",
            "Relying Blindly on Debt-to-Equity" to "Using Book Equity, which is distorted downward when companies execute massive accretive share buybacks.",
            "Treating Operating Leases as Off-Balance Sheet" to "Ignoring non-cancellable retail and airline lease obligations that function as contractual debt.",
            "Assuming High Cash Reserves Guarantee Safety" to "Ignoring that large cash piles can be rapidly incinerated by value-destroying M&A or negative operating cash burn."
        ),
        goldenRules = listOf(
            "The Net Debt / EBITDA Ceiling: Never allocate to non-utility companies where Net Debt exceeds 2.5x normalized EBITDA.",
            "The 3-Year Debt Payoff Standard: Require total funded debt obligations to be extinguishable within 3.0 years of Free Cash Flow.",
            "The 5.0x Interest Coverage Fortress: Require operating income (EBIT) to cover annual interest expense by at least 5.0x.",
            "The Net Cash Premium: Assign premium valuation multiples to companies operating in an authentic Net Cash position (Cash > Total Debt).",
            "The Altman Z-Score Safety Gate: Demand an Altman Z-Score > 2.99, strictly avoiding companies residing in the Distress Zone (< 1.81)."
        )
    ),

    // ------------------------------------------------------------------------
    // STAGE 2: COMPANY QUALITY (Lessons 8 - 11)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 8,
        stageNumber = 2,
        stageTitle = "Stage 2 — Company Quality",
        title = "ROE, ROA and ROIC (Capital Efficiency)",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "How efficiently does management allocate capital to generate superior economic profits? Is high ROE driven by pricing power or debt leverage?",
        primaryFormula = "ROIC = NOPAT / Invested Capital  |  DuPont ROE = Net Margin × Asset Turnover × Equity Multiplier  |  EVA Spread = ROIC - WACC",
        researcherRule = "Over multi-decade horizons, equity compounding mathematically converges to Return on Invested Capital (ROIC). Always dissect ROE using 3-Stage DuPont analysis to ensure returns are powered by operational margins and asset velocity, not excessive financial debt leverage. Demand ROIC > 15% and an ROIC - WACC spread > 500 bps.",
        whatDataDoesNotProve = "High ROE does NOT prove operational brilliance if an enterprise carries heavy debt (high Equity Multiplier). Conversely, low ROA does NOT prove a weak business if the company holds an immense, non-operating cash fortress in short-term Treasuries.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_08_infographic.jpg",
        keyCheckpoints = listOf(
            "The Capital Efficiency Hierarchy: Return on Invested Capital (ROIC) vs. Return on Assets (ROA) vs. Return on Equity (ROE).",
            "Net Operating Profit After Tax (NOPAT): EBIT × (1 - Effective Tax Rate), eliminating financial structure distortions.",
            "Invested Capital (Operating Standard): Total Debt + Stockholders' Equity - Cash & Marketable Securities.",
            "The 3-Stage DuPont Breakdown: Decomposing ROE into Net Margin (Profitability) × Asset Turnover (Efficiency) × Equity Multiplier (Leverage).",
            "The Financial Leverage Trap: How high debt artificially manufactures a 150% ROE while ROIC stays below the cost of capital.",
            "Economic Value Added (EVA) & ROIC-WACC Spread: Proving whether corporate reinvestment creates or destroys economic wealth.",
            "The Reinvestment Runway: Combining high ROIC with the capacity to reinvest cash at that high rate to compound intrinsic value.",
            "Audited Apple & Microsoft Case Studies: Generating sustained 40%+ ROIC through asset-light digital ecosystems."
        ),
        fullNotesOverview = """
            Capital allocation is the CEO's ultimate responsibility. Over a multi-decade horizon, two businesses with identical earnings growth can deliver vastly different returns to shareholders depending on the Return on Invested Capital (ROIC) achieved on reinvested cash.
            
            As Charlie Munger famously noted, over long periods an investor's compound return cannot deviate significantly from the underlying business's return on capital. In quantitative fundamental analysis, we analyze three distinct capital return metrics:
            1. Return on Equity (ROE = Net Income / Equity): Measures the accounting return on common shareholders' book capital. However, ROE is highly susceptible to financial engineering: adding debt reduces equity and inflates ROE, creating an illusion of high operational quality.
            2. Return on Assets (ROA = Net Income / Total Assets): Measures how productive total balance sheet assets are. While useful, ROA unfairly penalizes companies maintaining large liquid cash fortresses.
            3. Return on Invested Capital (ROIC = NOPAT / Invested Capital): The institutional gold standard. NOPAT (EBIT × [1 - Tax Rate]) isolates core operating profits, while Invested Capital (Total Debt + Equity - Cash) reflects the real capital deployed into operating assets.
            
            To unmask what truly drives corporate returns, we apply the 3-Stage DuPont Decomposition:
            ROE = (Net Income / Revenue) × (Revenue / Total Assets) × (Total Assets / Equity)
            ROE = Net Profit Margin × Asset Turnover × Equity Multiplier
            Elite compounders achieve high ROE through high Net Margins (pricing power) and rapid Asset Turnover (operational velocity)—never through an inflated Equity Multiplier (excess debt leverage).
        """.trimIndent(),
        conceptsSummary = mapOf(
            "ROIC (Return on Invested Capital)" to "NOPAT divided by Invested Capital. Measures the core economic productivity of capital independent of financing choices.",
            "NOPAT" to "Net Operating Profit After Tax = EBIT × (1 - Tax Rate). Pure operational profit after deducting cash taxes.",
            "Invested Capital" to "Operating capital tied up in the business: Total Debt + Equity - Cash (or Net Working Capital + Net PP&E).",
            "ROA (Return on Assets)" to "Net Income divided by Total Assets. Measures total asset productivity.",
            "ROE (Return on Equity)" to "Net Income divided by Book Stockholders' Equity. Susceptible to leverage distortion and buyback skew.",
            "3-Stage DuPont Analysis" to "Decomposes ROE into Profitability (Net Margin) × Efficiency (Asset Turnover) × Leverage (Equity Multiplier).",
            "Equity Multiplier" to "Total Assets divided by Stockholders' Equity. Multipliers > 4.0x indicate heavy debt leverage.",
            "ROIC - WACC Spread (EVA)" to "ROIC minus Weighted Average Cost of Capital. Positive spread indicates economic wealth creation; negative spread destroys wealth."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Consider the returns on capital earned by the greatest business monopolies in human history: Apple achieves ~50% ROIC, Microsoft generates ~30% ROIC, and Alphabet delivers ~28% ROIC. No legitimate public enterprise in the world generates 213.8% return on invested capital year after year. Assuming you can compound capital at 10% per month forces you into hyper-leveraged financial schemes or speculative penny stocks where a single adverse shift in cost of capital or macroeconomic liquidity wipes out 100% of your net worth. Sustainable wealth is built by owning businesses that compound capital at 15% to 30% ROIC over decades.
        """.trimIndent(),
        fictionalExample = """
            Conservative Compounder Corp vs. Leveraged Zombie Inc:
            Both firms report $100M in revenue, $20M in EBIT, $16M in Net Income (20% tax rate), and have $100M in total assets.
            • Conservative Compounder Corp: Financed with $80M Equity and $20M Debt.
              - Net Margin: $16M / $100M = 16.0%
              - Asset Turnover: $100M / $100M = 1.00x
              - Equity Multiplier: $100M / $80M = 1.25x
              - ROE = 16% × 1.00 × 1.25 = 20.0%
              - Invested Capital = $20M Debt + $80M Equity - $10M Cash = $90M
              - NOPAT = $20M × (1 - 0.20) = $16M
              - ROIC = $16M / $90M = 17.8% (Elite > 15%!)
            • Leveraged Zombie Inc: Financed with $10M Equity and $90M Debt (9.0x leverage!).
              - Net Margin: $16M / $100M = 16.0%
              - Asset Turnover: $100M / $100M = 1.00x
              - Equity Multiplier: $100M / $10M = 10.0x!
              - ROE = 16% × 1.00 × 10.0 = 160.0%!
              - Invested Capital = $90M Debt + $10M Equity - $5M Cash = $95M
              - ROIC = $16M / $95M = 16.8%
            Analyst Verdict: A superficial screener sorting by ROE crowns Leveraged Zombie Inc (160% vs 20%). But DuPont unmasks the truth: Zombie's 160% ROE is 100% manufactured by dangerous debt leverage. When interest rates rise or sales dip 10%, Zombie defaults into bankruptcy while Conservative Compounder acquires its assets at liquidation prices!
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL, 5-Year Audited Form 10-K):
            • FY24 Capital Efficiency Highlights:
              - Revenue: $391.04B
              - Operating Income (EBIT): $123.22B
              - Effective Tax Rate: 24.1% ($29.75B Tax / $123.49B Pre-Tax)
              - NOPAT: $123.22B × (1 - 0.241) = $93.52B
              - Total Debt: $106.63B | Stockholders' Equity: $66.99B | Cash & ST Securities: $65.27B
              - Invested Capital: $106.63B + $66.99B - $65.27B = $108.35B
              - Return on Invested Capital (ROIC): $93.52B / $108.35B = 86.3%!
              - Return on Assets (ROA): $93.74B / $364.98B = 25.7% (Top 1% of S&P 500)
              - 3-Stage DuPont Breakdown:
                • Net Profit Margin: 23.97%
                • Asset Turnover: 1.07x
                • Equity Multiplier: 5.45x (Elevated due to $600B+ cumulative share buybacks!)
                • ROE: 23.97% × 1.07 × 5.45 = 139.9%
              - ROIC - WACC Spread: 86.3% ROIC - 8.5% WACC = +7,780 bps value creation spread!
        """.trimIndent(),
        commonMistakes = listOf(
            "Screening Solely on High ROE" to "Falling into the leverage trap where a high ROE is manufactured by dangerous debt rather than pricing power.",
            "Ignoring Negative Book Equity from Buybacks" to "Panicking when elite compounders (Apple, Domino's, AutoZone) have distorted or negative book equity from accretive share repurchases.",
            "Penalizing Cash Fortresses with ROA" to "Assuming a company with 10% ROA is mediocre, failing to see that $50B in cash reserves held in T-bills is diluting total assets.",
            "Confusing Revenue Growth with Value Creation" to "Cheering 30% top-line revenue growth when the business earns an 8% ROIC against a 9% WACC, destroying shareholder wealth with every dollar invested.",
            "Extrapolating Cyclical Peak ROIC" to "Valuing cyclical commodity or semiconductor producers at peak-cycle 35% ROIC, ignoring that returns mean-revert to single digits in downturns."
        ),
        goldenRules = listOf(
            "The 15% ROIC Quality Gate: Demand a 5-year median Return on Invested Capital > 15.0% as an essential filter for economic moat.",
            "The DuPont Dissection Mandate: Always break down ROE into Net Margin, Asset Turnover, and Equity Multiplier to identify the real return driver.",
            "The Positive WACC Spread Rule: Require an ROIC - WACC spread of at least +500 bps (5.0%) before allocating capital to growth stories.",
            "The Reinvestment Runway Test: Prioritize companies that not only earn high ROIC, but possess a long reinvestment runway to deploy retained earnings at high returns.",
            "The Operating Invested Capital Anchor: For heavy buyback companies, use Operating Invested Capital rather than book equity to measure true economic productivity."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 9,
        stageNumber = 2,
        stageTitle = "Stage 2 — Company Quality",
        title = "Business Quality & Economic Moats",
        icon = Icons.Rounded.Security,
        coreQuestion = "What structural competitive barriers protect this company's profits from competitive destruction? Does it have genuine pricing power, high switching costs, or network effects?",
        primaryFormula = "Pricing Power Volatility: σ(GM) < 2.0%  |  Metcalfe's Law: V ∝ n²  |  OpEx Dilution: (SGA + R&D) / Revenue ↓  |  5-Yr ROIC Persistence ≥ 15%",
        researcherRule = "A true economic moat allows a business to raise prices without losing customer volume, sustained over 10+ years. In competitive capitalism, high ROIC attracts waves of competing capital unless protected by high Switching Costs, Two-Sided Network Effects, Cost Scale Advantages, Intangibles (Patents/Brands), or Efficient Scale. Verify pricing power through Gross Margin stability across inflationary shocks.",
        whatDataDoesNotProve = "A world-famous consumer brand name or high revenue growth does NOT prove an economic moat if consumers defect when prices rise 5%. Furthermore, high gross margins alone do not guarantee a moat if customer churn requires 50% of revenue spent on marketing just to keep customer volume flat.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_09_infographic.jpg",
        keyCheckpoints = listOf(
            "The 5 Structural Moat Sources: Network Effects, Switching Costs, Cost Advantages, Intangibles (Brands & Patents), and Efficient Scale.",
            "Quantitative Proof of Pricing Power: Gross Margin Volatility σ(GM) < 2.0% across multi-year inflationary cycles.",
            "Two-Sided Network Effects & Metcalfe's Law (V ∝ n²): How multi-sided lock-in creates self-reinforcing monopolies (Visa, Mastercard, Meta).",
            "High Switching Costs & Operational Entrenchment: Mission-critical enterprise software where replacement cost exceeds software fees (Microsoft, Oracle, Workday).",
            "Cost Scale Advantages & Fixed Cost Dilution: Spreading supply chain, warehousing, and tech overhead over massive revenue volume (Costco, Amazon, Walmart).",
            "Intangible Assets & Pricing Power Brands: Intellectual property, FDA pharmaceutical patents, and luxury brand equity that commands premium pricing without volume loss.",
            "Customer Concentration Footnote Analysis: Reading SEC Form 10-K Note Disclosures to ensure no single customer accounts for > 10% of total revenue.",
            "Audited Visa Inc. vs. Delta Air Lines Case Study: Demonstrating an 82% Gross Margin global toll booth vs. a 22% Gross Margin capital-intensive commodity price-taker."
        ),
        fullNotesOverview = """
            Capitalism is an engine of creative destruction. When an industry demonstrates high profitability (high ROIC), it acts as a beacon attracting competing capital. Without durable structural barriers—an Economic Moat—competition inevitably drives excess economic profits down to the cost of capital.
            
            Warren Buffett and Charlie Munger popularized the concept of an economic moat as the single most critical determinant of multi-decade investment compounding. Morningstar and quantitative fintech research categorize moats into five structural sources:
            1. Network Effects (Metcalfe's Law): The value of the service increases exponentially as more participants join. In two-sided networks (such as Visa and Mastercard), consumers hold the card because every merchant accepts it, and merchants accept it because every consumer carries it.
            2. High Switching Costs: When the monetary, operational, and psychological friction of abandoning a product exceeds the cost of annual software licenses. Enterprise ERPs (Oracle, SAP) and cloud operating systems (Microsoft 365, AWS) become deeply embedded into corporate workflows.
            3. Cost Scale Advantages: Companies with massive scale leverage lower per-unit fixed costs (Costco, Walmart, TSMC). As scale expands, Operating Expense Ratio (SGA / Revenue) steadily decreases, widening the margin gap against smaller competitors.
            4. Intangible Assets: Patents, regulatory licenses, and premium brand pricing power. A true brand moat exists only when customers willingly pay a premium over commoditized alternatives (e.g., Apple vs. generic smartphones, Hermès vs. generic leather goods).
            5. Efficient Scale: In niche markets with limited demand (pipelines, regional freight railroads, specialized port terminals), a single incumbent satisfies total market demand profitably, but entry by a second competitor would cause catastrophic losses for both.
            
            Quantitative Proof of Pricing Power:
            Qualitative stories are insufficient. We verify pricing power by calculating Gross Margin Volatility (Standard Deviation of Gross Margin over 5–10 years). True moats exhibit σ(GM) < 2.0%, proving that management successfully passes along input cost inflation (raw materials, wages, logistics) to customers without sacrificing profit margins.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Economic Moat" to "A durable structural competitive advantage that protects a company's high ROIC from competitive erosion over multi-decade horizons.",
            "Pricing Power" to "The ability to raise prices ahead of inflation without experiencing a decline in sales volume or losing market share to competitors.",
            "Gross Margin Volatility (σ_GM)" to "Standard deviation of Gross Margin over 5-10 years. Values < 2.0% indicate elite pricing power and competitive insulation.",
            "Two-Sided Network Effect" to "A dynamic where each new user on one side of a platform increases the value for users on the complementary side (e.g., cardholders & merchants).",
            "Metcalfe's Law" to "Mathematical theorem stating that the value of a telecommunications or payment network scales proportionally to the square of connected users (V ∝ n²).",
            "Switching Costs" to "The monetary, technical, and operational expenses incurred by a customer when migrating from an incumbent provider to a competitor.",
            "Cost Advantage / Scale Moat" to "Lower unit production or distribution costs achieved through massive buying volume, unique supply-chain assets, or technological automation.",
            "Customer Concentration Risk" to "When a single customer accounts for > 10% of revenue (mandated disclosure in SEC 10-K footnotes), posing catastrophic counterparty risk."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Consider the returns on capital earned by the greatest business monopolies in human history: Visa generates an 82% gross margin and 55% operating margin with 30%+ ROIC. Yet even Visa's market capitalization compounds at 18–22% annualized over long cycles—not 213.8%. Assuming you can achieve 10% monthly returns forces you into hyper-leveraged trading or speculative commodity penny stocks lacking any structural economic moat. A single supply surge or margin squeeze wipes out 100% of your invested capital. Real generational wealth is accumulated by holding wide-moat compounders that reliably out-earn their cost of capital through entire business cycles.
        """.trimIndent(),
        fictionalExample = """
            Wide Moat Software Corp vs. Commodity Hardware Assembler Inc:
            Both companies generate $500M in revenue during an economic boom year.
            • Wide Moat Software Corp (High Switching Costs & Network Toll):
              - Cost of Goods Sold: $100M (80.0% Gross Margin)
              - R&D and SGA: $200M (Operating Margin: 40.0%)
              - Input Inflation Shock: Supply chain costs and developer wages surge 20%.
              - Pricing Action: Software Corp raises seat licenses by 10%. Because switching software would cost clients $5M in migration downtime, churn is only 0.5%.
              - Next Year Result: Revenue rises to $550M; Gross Margin remains rock-solid at 79.5% (Δ = -0.5%).
            • Commodity Hardware Assembler Inc (Price-Taker, No Moat):
              - Cost of Goods Sold: $425M (15.0% Gross Margin)
              - R&D and SGA: $50M (Operating Margin: 5.0%)
              - Input Inflation Shock: Memory chip and aluminum casing prices surge 20%.
              - Pricing Action: Assembler attempts a 5% price hike. Competitors in Asia undercut them; buyers defect immediately.
              - Next Year Result: Gross Margin collapses to 8.0%. Operating profit turns into a -$15M net loss!
            Analyst Verdict: Commodity businesses operate at the mercy of macroeconomic tides. Wide-moat compounders dictate their own pricing and thrive through inflationary shocks.
        """.trimIndent(),
        realStockExample = """
            Visa Inc. (NYSE: V) vs. Delta Air Lines (NYSE: DAL) — Audited 5-Year Form 10-K Comparison:
            • Visa Inc. (Wide Moat — Two-Sided Network Effect Toll Booth):
              - FY24 Revenue: $35.93B | Operating Income: $23.63B
              - 5-Year Gross Margin Range: 80.0% to 82.5% (σ_GM = 0.95% — Pristine Pricing Power!)
              - 5-Year Operating Margin Range: 64.0% to 67.0%
              - Capital Intensity (CapEx / Revenue): Only 3.1% ($1.12B CapEx on $36B Revenue)
              - 5-Year ROIC Average: 31.4% (Significantly > 15% Quality Gate!)
              - Economic Moat Assessment: WIDE MOAT. With 4.5 billion cards in circulation accepted by 150+ million merchant locations, displacing Visa's network is virtually impossible. Every tap collects an inflation-indexed toll.
            • Delta Air Lines (NYSE: DAL) (No Moat — Commodity Price-Taker):
              - FY24 Revenue: $61.64B | Operating Income: $6.02B
              - 5-Year Gross Margin Range: 18.2% to 23.5% (σ_GM = 4.82% — Highly Volatile!)
              - 5-Year Operating Margin Range: 2.1% to 9.8% (Extreme cyclical swings)
              - Capital Intensity (CapEx / Revenue): 9.5% ($5.87B annual aircraft & maintenance CapEx)
              - 5-Year ROIC Average: 6.8% (Destroys value relative to an 8.5% airline industry WACC)
              - Economic Moat Assessment: NO MOAT. Air travel is a commoditized product where consumers sort flights by lowest ticket price on search aggregators. Jet fuel and union labor spikes cannot be passed on without hurting passenger loads.
        """.trimIndent(),
        commonMistakes = listOf(
            "Confusing a Famous Brand with a Pricing Power Moat" to "Assuming brand awareness equals an economic moat. If customers will not pay a premium price for the product, the brand has zero economic value.",
            "Ignoring Single Customer Concentration Footnotes" to "Buying an apparently high-growth supplier without reading Form 10-K footnotes, only to discover one customer accounts for 40% of sales and can crush margins at will.",
            "Overlooking Gross Margin Volatility in Inflation" to "Failing to check Gross Margin stability during inflation shocks; businesses that cannot pass on cost increases will suffer swift margin destruction.",
            "Believing High Market Share Guarantees a Moat" to "Confusing historical market share with structural defensibility. Kodak, Blackberry, and Nokia held dominant market shares right before disruption.",
            "Underestimating Moat Decay from Technological Disruption" to "Assuming a moat lasts forever without monitoring R&D efficiency, developer lock-in, and emerging technological substitutes."
        ),
        goldenRules = listOf(
            "The Gross Margin Stability Gate: Require Gross Margin standard deviation σ(GM) < 2.0% across 5 years to verify authentic pricing power.",
            "The 5-Year ROIC Moat Screen: Demand Return on Invested Capital > 15.0% for at least 5 consecutive years as empirical proof of an economic barrier.",
            "The 10% Customer Concentration Ceiling: Ensure no single customer generates > 10% of total revenue in SEC Form 10-K footnote disclosures.",
            "The Scale OpEx Dilution Rule: Look for an Operating Expense ratio (SGA / Revenue) that declines as revenue expands, proving operating scale advantages.",
            "The Metcalfe Network Test: For platform businesses, confirm that user growth creates self-reinforcing liquidity that raises switching costs for all participants."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 10,
        stageNumber = 2,
        stageTitle = "Stage 2 — Company Quality",
        title = "Management Quality & Capital Allocation",
        icon = Icons.Rounded.Psychology,
        coreQuestion = "How do corporate managers deploy surplus Free Cash Flow, and are executive incentives mathematically aligned with long-term per-share compounding?",
        primaryFormula = "Warren Buffett's $1 Test: Δ Market Cap / Σ Retained Earnings ≥ $1.00  |  Shareholder Yield = Div Yield + Buyback Yield  |  Net Share Cannibal Rate: Δ Shares < 0",
        researcherRule = "Never judge management by quarterly earnings charisma or public relations. Judge CEOs by their mathematical capital deployment track record: internal reinvestment at ROIC > WACC, disciplined share cannibalism below intrinsic value, avoiding value-destructive M&A goodwill write-downs, and substantial open-market insider ownership (Form 4).",
        whatDataDoesNotProve = "Announcing a massive $10 Billion share buyback program does NOT prove shareholder alignment if the company issues $10 Billion in Stock-Based Compensation (SBC), resulting in 0% net share reduction. Furthermore, high dividend yields do not prove safety if funded by borrowed balance sheet debt.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_10_infographic.jpg",
        keyCheckpoints = listOf(
            "The 5 Capital Allocation Pathways: Organic Reinvestment, Disciplined Bolt-on M&A, Debt Extinguishment, Value-Accretive Buybacks, and Cash Dividends.",
            "Warren Buffett's $1 Retained Earnings Test: Confirming that every $1 of retained earnings creates at least $1 of market capitalization over 5 years.",
            "The 'Buyback Cannibal' Dynamic: How retiring 30%+ of share count mechanically accelerates per-share EPS compounding (Apple, AutoZone).",
            "The 'Vanity Buyback' Dilution Treadmill: Detecting when repurchases merely offset excessive executive Stock-Based Compensation.",
            "M&A Goodwill Impairment Traps: How empire-building mega-mergers create massive balance sheet goodwill that gets written down during downturns.",
            "Executive Compensation Forensics (SEC Form DEF 14A): Verifying PSUs are tied to ROIC and per-share FCF rather than raw top-line revenue.",
            "Insider Skin in the Game & Form 4 Analysis: Dissecting open-market purchases (Code P) vs. automated 10b5-1 selling (Code S).",
            "Audited Apple Inc. vs. General Electric Case Study: Contrasting $500B+ disciplined share retirement with $22B+ M&A goodwill destruction."
        ),
        fullNotesOverview = """
            Once an enterprise achieves profitability and generates surplus Free Cash Flow, the Chief Executive Officer's single most critical responsibility is Capital Allocation.
            
            As William Thorndike documented in 'The Outsiders', over a ten-year tenure, a CEO who retains 10% of annual corporate net worth will personally allocate a capital sum greater than 60% of the entire initial enterprise value. Extraordinary long-term equity compounding is rarely produced by operational brilliance alone; it is produced by superior capital allocators who treat corporate cash as a precious, scarce resource.
            
            The 5 Capital Allocation Options:
            1. Organic Reinvestment: Funding internal R&D, software development, and CapEx. Management must demand that expected returns comfortably exceed the Weighted Average Cost of Capital (ROIC > WACC).
            2. Strategic M&A: Small, disciplined bolt-on acquisitions with proven operational synergies. Research proves over 70% of large, transformational mega-mergers fail to earn their cost of capital, generating massive goodwill impairments.
            3. Debt Retirement: Paying down high-coupon borrowings to eliminate bankruptcy risk and expand financial flexibility.
            4. Share Buybacks: The ultimate compounding tool when executed correctly. Repurchasing shares when the stock trades below intrinsic value permanently reduces share count, concentrating remaining shareholders' ownership of future earnings.
            5. Cash Dividends: Returning discretionary cash directly to owners when no high-return internal or M&A opportunities exist.
            
            Warren Buffett's $1 Retained Earnings Test:
            Management must not retain earnings simply to build corporate empires. For every $1.00 of cumulative net earnings retained in the business over a 5-year rolling cycle, management must create at least $1.00 of market capitalization. Elite compounders generate $2.00 to $4.00+ per dollar retained.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Capital Allocation" to "The process by which CEOs decide how to deploy corporate Free Cash Flow among reinvestment, M&A, debt reduction, buybacks, and dividends.",
            "Warren Buffett's $1 Test" to "Mathematical standard: Market Cap gain divided by Retained Earnings over a 5-year cycle must exceed 1.0x.",
            "Buyback Cannibal" to "A company that systematically retires 2% to 5%+ of its net diluted share count annually, driving per-share compounding even in mature markets.",
            "Vanity Buybacks / Dilution Treadmill" to "When repurchases merely offset shares issued to employees via Stock-Based Compensation (SBC), resulting in zero net share reduction.",
            "M&A Goodwill Impairment" to "When an acquired company underperforms, management is forced to write down goodwill on the balance sheet, wiping out book equity.",
            "SEC Form DEF 14A (Proxy Statement)" to "Annual regulatory filing detailing executive compensation metrics, performance hurdle gates, and peer benchmarking.",
            "SEC Form 4 (Transaction Code P)" to "Mandatory filing within 2 business days of an insider trade; Code P indicates an open-market purchase with personal cash.",
            "Total Shareholder Yield" to "The combination of annual Cash Dividend Yield plus Net Share Buyback Yield."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Consider the greatest capital allocators in modern business history: Warren Buffett compounded Berkshire Hathaway at 19.8% annualized over 60 years; Henry Singleton compounded Teledyne at 20.4% over 25 years. No legitimate public enterprise in the world compounds capital at 213.8% annually. Believing you can achieve 10% per month blinds investors to the structural reality of corporate finance, leading them straight into promotional penny-stock dilution schemes where management issues millions of cheap shares to enrich insiders while retail shareholders are wiped out. True wealth is accumulated by partnering with disciplined capital allocators who compound per-share value at 15% to 25% over decades.
        """.trimIndent(),
        fictionalExample = """
            Disciplined Cannibal Corp vs. Empire Building Conglomerate Inc:
            Both companies start with 100M shares, generate $500M in Net Income, and have a $10B Market Cap ($100/share, 20x P/E).
            • Disciplined Cannibal Corp:
              - Retains $100M for CapEx at 25% ROIC.
              - Uses remaining $400M to repurchase shares at $100/share (retires 4.0M shares).
              - Five Years Later: Net Income grows moderately to $650M (+30%). But share count shrunk from 100M to 78M (-22%).
              - EPS surges from $5.00 to $8.33 (+66.6% per-share growth!). Stock trades at $166.
            • Empire Building Conglomerate Inc:
              - CEO borrows $3B in debt to acquire a flashy competitor at peak cycle ($2.5B of Goodwill booked).
              - Issues 30M new shares to pay acquisition fees and executive bonuses.
              - Five Years Later: Net Income appears higher at $700M (+40%). But share count expanded from 100M to 135M (+35%).
              - EPS collapses from $5.00 to $5.18 (+3.6% total over 5 years!).
              - Acquired division falters; management writes off $1.5B in Goodwill. Stock sinks to $75.
            Analyst Verdict: Conglomerate's CEO built a massive empire and collected huge bonuses, but destroyed per-share value. Cannibal Corp's disciplined CEO created immense shareholder wealth.
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL) vs. General Electric (NYSE: GE) — 5-Year Audited SEC 10-K Comparison:
            • Apple Inc. (The Quintessential Capital Allocator):
              - 5-Year Capital Returned to Shareholders: $422B in Share Buybacks + $73B in Cash Dividends ($495B total!).
              - Net Share Count Evolution: Diluted shares dropped from 17.53B in FY20 to 15.20B in FY24 (-13.3% net shrinkage, -3.5% CAGR).
              - M&A Track Record: Zero mega-mergers. Only small, disciplined bolt-on acquisitions ($100M-$500M) integrated directly into Apple Silicon and iOS.
              - Goodwill on Balance Sheet: Exactly $0.00 (Zero M&A impairment risk!).
              - Warren Buffett's $1 Retained Earnings Test: Created $1,470B in market cap while retaining $369.7B in earnings = $3.98 of value created per $1.00 retained!
            • General Electric (Pre-Breakup Empire Building Destroyer):
              - M&A Track Record: Spent $10.6B cash acquiring Alstom's power business at peak cycle.
              - Goodwill Write-Down: Booked a catastrophic $22.8B goodwill impairment charge in 2018 when power turbine demand evaporated.
              - Dividend Destruction: Slashed quarterly dividend by 92% (from $0.24 to $0.01 per share) to preserve cash.
              - Net Share Count Evolution: Flat to dilutive (+1.9% share count expansion over 5 years to cover compensation).
              - Warren Buffett's $1 Retained Earnings Test: Retained earnings generated negative market value creation.
        """.trimIndent(),
        commonMistakes = listOf(
            "Cheering Buyback Announcements Without Verifying Net Shares" to "Applauding a $5B buyback press release while failing to realize the share count actually increased due to massive executive stock option grants.",
            "Falling in Love with 'Empire-Building' Mega-Mergers" to "Mistaking large acquisitions for real growth. Studies show over 70% of large corporate M&A deals fail to earn their cost of capital.",
            "Rewarding Dividends Funded with Borrowed Debt" to "Assuming a 6% dividend yield is safe, ignoring that operating cash flow does not cover the payout and management is issuing senior bonds to fund dividends.",
            "Ignoring Executive Stock-Based Compensation (SBC) Dilution" to "Failing to account for SBC expense when evaluating cash flow, treating non-cash equity compensation as 'free money'.",
            "Evaluating CEOs by Charisma Rather than ROIC" to "Believing visionary conference-call presentations while ROIC steadily decays below the cost of capital."
        ),
        goldenRules = listOf(
            "The Net Share Shrinkage Test: Demand a net reduction in diluted shares outstanding (Δ Shares < 0) over 3-to-5 year cycles.",
            "The Warren Buffett $1 Retained Earnings Gate: Confirm that for every $1.00 of retained earnings over 5 years, management creates at least $1.00 of market value.",
            "The ROIC Incentive Mandate: Require executive long-term compensation (PSUs) to be explicitly tied to Return on Invested Capital (ROIC) and Free Cash Flow per share.",
            "The Clean Balance Sheet Ceiling: Ensure Goodwill represents less than 20% of total balance sheet assets to minimize impairment write-down vulnerability.",
            "The Insider Open-Market Purchase Signal: Prioritize companies where founders and C-suite executives hold substantial equity stakes and make open-market purchases (Form 4 Code P) with personal capital."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 11,
        stageNumber = 2,
        stageTitle = "Stage 2 — Company Quality",
        title = "Industry and Sector Dynamics",
        icon = Icons.Rounded.PieChart,
        coreQuestion = "Is this business swimming with a secular macro tailwind or fighting cyclical industry headwinds?",
        primaryFormula = "HHI = Sum(s_i^2)  |  Margin Spread = OM_stock - OM_peer_median  |  Beta_sector = Cov(R_i, R_m) / Var(R_m)",
        researcherRule = "Warren Buffett's Cardinal Dictum: 'When a management team with a reputation for brilliance tackles a business with a reputation for bad economics, it is the reputation of the business that remains intact.' Always align capital with structural industry tailwinds.",
        whatDataDoesNotProve = "Being the lowest-cost producer in a structurally dying or commoditized industry does not guarantee positive equity compounding over 10-year holding horizons.",
        keyCheckpoints = listOf(
            "The 11 GICS US Market Sectors and Macro Business Cycle Clock (Early, Mid, Late, Recession)",
            "Herfindahl-Hirschman Index (HHI): Classifying Fragmented (<1500), Moderate (1500-2500), and Oligopolistic (>2500) market power",
            "Secular Structural Compounders (Cloud SaaS, Advanced Semis) vs. Cyclical Price-Takers (Shipping, Auto, Commodity Mining)",
            "Operating Margin corridor and Peer Median Spread benchmarking",
            "The Cyclical Peak 'Value Trap': Why 4x P/E multiples at peak capacity utilization are dangerous",
            "Porter's Five Forces: Supplier monopsony, customer bargaining, substitute threats, entry barriers, and rivalry",
            "Regulatory and Antitrust Barriers: The FTC/DoJ hurdle and moats created by compliance complexity",
            "Normalized Mid-Cycle Earnings Power: Stripping away boom-bust extremes to assess true intrinsic cash generation"
        ),
        fullNotesOverview = """
            Welcome to the Capstone Investigation of Stage 2 (Company Quality): Industry and Sector Dynamics.

            1. The Primacy of Industry Structure:
            Academic finance and empirical hedge fund performance data reveal a sobering reality: more than 50% of the variance in a company's Return on Invested Capital (ROIC) is determined not by managerial brilliance, but by the structural economics of the industry in which it competes. An average management team operating inside an enterprise software oligopoly with 85% gross margins and high switching costs will routinely outperform a genius management team operating an international container shipping fleet subject to brutal commodity spot price wars.

            2. The 11 GICS Sectors & The Macro Business Cycle Clock:
            The Global Industry Classification Standard (GICS) categorizes global equities into 11 distinct sectors, each exhibiting unique economic sensitivity across the 4 stages of the business cycle:
            • Early Cycle (Recovery): Financials, Consumer Discretionary, Real Estate thrive as credit spreads compress and consumer confidence rebounds.
            • Mid Cycle (Peak Expansion): Information Technology and Industrials compound earnings as capital expenditure and enterprise software budgets expand.
            • Late Cycle (Overheating): Energy and Materials outperform as capacity shortages push commodity prices and inflation higher.
            • Recession (Contraction): Consumer Staples, Utilities, and Health Care preserve equity capital as essential non-discretionary demand resists macro contagion.

            3. Quantifying Industry Concentration — The Herfindahl-Hirschman Index (HHI):
            Antitrust regulators (SEC, DoJ, FTC) and tier-1 quant funds measure industry concentration via HHI:
            HHI = Sum(s_i^2), where s_i represents each participant's market share percentage.
            • HHI < 1,500 (Fragmented): Dozens of small competitors engage in cutthroat price discounting, driving industry ROIC below the cost of capital (e.g., legacy trucking, commodity restaurants).
            • 1,500 <= HHI <= 2,500 (Moderately Concentrated): A few leading brands compete with differentiated pricing (e.g., athletic footwear, automotive).
            • HHI > 2,500 (Concentrated Oligopoly): Two or three rational titans dominate market share, establishing a 'pricing umbrella' that shields operating margins from ruinous price wars (e.g., Cloud Hyperscalers: AWS/Azure/GCP; Payment Networks: Visa/Mastercard; Aircraft Manufacturing: Boeing/Airbus).

            4. The Cyclical Value Trap Anomaly:
            In secular growth industries (Cloud, MedTech), a 30x P/E ratio can often be cheap if earnings are compounding at 20% annually. In stark contrast, in cyclical commodity industries (Oil exploration, Steel mills, Dry bulk shipping), a stock trading at 4x P/E at the absolute peak of a macro boom is often a lethal Value Trap. The low P/E occurs precisely because earnings are cyclically inflated. When the economic cycle turns, operating earnings collapse by 70% to 100%, and the 'cheap' 4x P/E stock suddenly trades at 50x trough earnings or enters Chapter 11 bankruptcy. To value cyclicals accurately, institutional analysts normalize earnings using Mid-Cycle Operating Margins.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "GICS Sector Taxonomy" to "Standardized 11-sector classification hierarchy separating secular compounders from cyclical commodity businesses.",
            "Herfindahl-Hirschman Index (HHI)" to "Mathematical concentration metric: Sum of squared market share percentages; HHI > 2,500 signifies an oligopolistic pricing umbrella.",
            "Secular vs. Cyclical Dynamics" to "Secular compounders expand irrespective of macro GDP growth; cyclical price-takers fluctuate violently with macro commodity and credit cycles.",
            "Porter's Five Forces" to "Analytical framework auditing threat of new entrants, buyer power, supplier power, threat of substitutes, and internal competitive rivalry.",
            "Oligopoly Pricing Umbrella" to "The dynamic where dominant market leaders set rational prices that allow efficient competitors to generate high ROIC without predatory discounting.",
            "Normalized Mid-Cycle Operating Profit" to "Estimated earnings power calculated by applying median 10-year operating margins to normalized mid-cycle revenue.",
            "Macro Business Cycle Clock" to "The 4-stage macroeconomic rotation sequence: Early Cycle Recovery, Mid Cycle Expansion, Late Cycle Overheating, and Recessionary Contraction.",
            "Cyclicality Beta (β)" to "Statistical measure of a stock's operating earnings sensitivity relative to broad macroeconomic fluctuations."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Consider the immutable laws of macroeconomic cycles: In cyclical sectors like Energy, Shipping, and Semi-Cap equipment, operating earnings routinely drop 60% to 80% during sector downturns. An investor chasing 10% monthly returns will almost invariably buy highly leveraged cyclical stocks at peak-cycle valuations, mistaking a temporary commodity boom for perpetual growth. When the cyclical wave crests, the drawdown wipes out years of capital. True long-term compounding requires understanding which phase of the sector cycle you are entering and anchoring to secular compounders with durable pricing umbrellas.
        """.trimIndent(),
        fictionalExample = """
            Secular Cloud Solutions Inc. vs. Cyclical Heavy Machining Corp:
            Both companies generate $1.0B in revenue in Year 1.
            • Secular Cloud Solutions Inc. (Enterprise Software Oligopoly, HHI = 2,800):
              - Subscription model with 95% net retention.
              - 40% Operating Margin maintained continuously through boom and recession.
              - Operating Profit compounds from $400M to $650M (+62%) over 5 years.
              - Stock compounds at 18% annualized through mild macro slowdowns.
            • Cyclical Heavy Machining Corp (Fragmented Industrial Mining Machinery, HHI = 1,200):
              - Year 1 (Peak Commodity Boom): Revenue $1.0B, Operating Margin 20%, Operating Profit $200M. P/E appears low at 8x ($16/share).
              - Year 3 (Commodity Trough): Mining capex freezes. Revenue plunges to $600M. High fixed operating leverage causes operating margin to collapse to 3.0%. Operating Profit sinks to $18M (-91%!).
              - Stock plummets from $16 to $4 (-75% drawdown).
              - Naive investors who bought at 'cheap 8x P/E' suffered catastrophic losses because they ignored cyclical sector dynamics.
        """.trimIndent(),
        realStockExample = """
            Microsoft Corporation (NASDAQ: MSFT) vs. Caterpillar Inc. (NYSE: CAT) — 10-Year Audited SEC 10-K Comparison:
            • Microsoft Corporation (Secular Cloud/Enterprise Software Compounder):
              - Operating Margin Trajectory: 28.0% in FY15, steadily expanding to 44.6% in FY24 (Standard deviation of only 5.4% across a decade).
              - Return on Invested Capital (ROIC): Compounded from 18.2% to 33.5%, completely unaffected by commodity or industrial recessions.
              - Industry Concentration: Commercial Cloud (Azure, Office 365, GitHub) operates in an oligopoly (HHI > 2,600) with immense pricing power.
              - 10-Year Total Return: > 950% (Disciplined secular compounding).
            • Caterpillar Inc. (Cyclical Heavy Machinery Industrial):
              - Operating Margin Trajectory: Trough of 4.2% in 2016 (commodity capex bust), surging to 20.8% in 2024 (infrastructure stimulus boom). Peak-to-trough swing of 16.6%!
              - Return on Invested Capital (ROIC): Decayed to 3.5% in 2016 before recovering to 18.2% in 2024.
              - Industry Concentration: Heavy earth-moving machinery operates with significant cyclical capital intensity and macro sensitivity (Beta > 1.20).
              - Analyst Verdict: Caterpillar is an exceptionally managed company, but its per-share equity returns are tethered to cyclical macro infrastructure waves, whereas Microsoft rides structural secular digital transformation.
        """.trimIndent(),
        commonMistakes = listOf(
            "Buying Cyclical Stocks at Low P/E Multiples at Peak Cycle" to "Believing a stock trading at 4x P/E is cheap, failing to recognize that earnings are at a historical cyclical peak and about to collapse.",
            "Analyzing Companies Without Comparing to Industry Medians" to "Assuming a 15% operating margin is good for a software company (where peer median is 25%) or bad for a grocery chain (where peer median is 4%).",
            "Confusing Cyclical Commodity Recoveries with Secular Structural Growth" to "Assuming a shipping or oil stock experiencing a wartime freight rate spike has suddenly transformed into a permanent compounder.",
            "Ignoring Regulatory and Antitrust Concentration Ceilings" to "Assuming a mega-cap company in an HHI > 3,000 oligopoly can acquire its rivals without facing severe FTC/DoJ regulatory blocks.",
            "Failing to Normalize Earnings Across a Full 7-Year Business Cycle" to "Valuing cyclical industrials and materials producers on peak 12-month trailing earnings rather than normalized mid-cycle cash flow."
        ),
        goldenRules = listOf(
            "The Relative Margin Spread Rule: Demand that a company sustain an Operating Margin at least 300 to 500 bps above its industry peer group median across economic cycles.",
            "The HHI Concentration Filter: Prioritize industries with HHI > 2,000 (concentrated oligopolies) where market leaders maintain pricing umbrellas rather than ruinous price wars.",
            "The Normalized Mid-Cycle Valuation Mandate: Never evaluate a cyclical company on peak trailing earnings; always calculate valuation multiples against 7-year normalized mid-cycle earnings.",
            "The Secular Tailwind Verification: Confirm that company revenue growth is driven by secular adoption trends (e.g., cloud migration, automation, aging demographics) rather than temporary macro commodity inflation.",
            "The Operating Leverage Stress Test: Model the impact of a 20% decline in industry volume on the company's fixed cost absorption to ensure operating margins remain positive during a recession."
        )
    ),

    // ------------------------------------------------------------------------
    // STAGE 3: VALUATION (Lessons 12 - 16)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 12,
        stageNumber = 3,
        stageTitle = "Stage 3 — Valuation",
        title = "P/E Ratio, Forward P/E, and Earnings Yield",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "How much are you paying for each dollar of corporate earnings? What hurdle rate does that multiple imply?",
        primaryFormula = "Trailing P/E = Price / TTM EPS  |  Forward P/E = Price / Fwd EPS  |  Earnings Yield = (1 / P/E) × 100  |  PEG = P/E / Growth Rate",
        researcherRule = "Always invert P/E into Earnings Yield (E/P) and compare to the 10-Year US Treasury yield. If Earnings Yield < Risk-Free Rate, earnings growth must be massive to justify holding equity risk.",
        whatDataDoesNotProve = "A low P/E ratio does not prove a stock is undervalued; it often signals cyclical peak earnings, permanent structural decline, or aggressive accounting capitalization.",
        keyCheckpoints = listOf(
            "Trailing P/E vs. Forward P/E: Audited historical 12-month truth vs. Wall Street consensus forecast assumptions",
            "The Earnings Yield Inversion: Converting abstract price multiples into bond-equivalent yields (E/P = 1 / P/E × 100)",
            "The Equity Risk Premium (ERP): Measuring the spread between Earnings Yield and the 10-Year US Treasury risk-free rate",
            "Peter Lynch's PEG Ratio: Dividing P/E by annual EPS growth rate (PEG < 1.0x indicates growth at a reasonable price)",
            "Historical P/E Valuation Bands: Tracking 10-year channels (15x, 20x, 25x, 30x) to evaluate multiple expansion vs. contraction",
            "Quality-Adjusted Multiples: Understanding why a 25x P/E on 30% ROIC is cheaper than a 10x P/E on 5% ROIC",
            "Forward Multiple Compression: Modeling how expected future earnings growth deflates future valuation multiples",
            "Accounting Distortions: Sanitizing EPS for non-operating asset sales, restructuring charges, and share dilution"
        ),
        fullNotesOverview = """
            1. The Anatomy of the Price-to-Earnings (P/E) Multiple:
            The P/E ratio is the most ubiquitous valuation metric in finance, but also the most misunderstood:
            • Trailing P/E = Current Share Price / Diluted TTM EPS = Market Capitalization / Net Income.
              It is rooted in audited historical accounting truth from SEC 10-K/10-Q filings, but it is backward-looking.
            • Forward P/E = Current Share Price / Consensus Projected NTM EPS.
              It reflects future market expectations over the next 12 months, but it carries analyst forecast risk. If forward earnings expectations are cut, the stock suffers a devastating double-whammy: downward earnings revision accompanied by multiple contraction.

            2. The Earnings Yield Inversion & The Equity Risk Premium (ERP):
            Wall Street quotes multiples ($20x$, $35x$), but institutions analyze yields. The Earnings Yield is the mathematical reciprocal of P/E:
            Earnings Yield (E/P) = (1 / P/E) × 100 = EPS / Share Price.
            • At 10x P/E, the business generates a 10.0% earnings return on purchase price.
            • At 20x P/E, the business generates a 5.0% earnings return on purchase price.
            • At 25x P/E, the business generates a 4.0% earnings return on purchase price.
            • At 50x P/E, the business generates a 2.0% earnings return on purchase price.
            Institutional Benchmark: If the 10-Year US Treasury bond yields 4.25% (guaranteed risk-free), paying 25x P/E (4.0% Earnings Yield) provides a NEGATIVE Equity Risk Premium (-0.25%). The investor is taking equity market drawdown risk without receiving compensation over the risk-free rate, which is only rational if the company commands exceptional secular growth.

            3. Peter Lynch's PEG Ratio — Growth at a Reasonable Price (GARP):
            Fidelity Magellan fund manager Peter Lynch pioneered the PEG Ratio to normalize P/E for compounding speed:
            PEG Ratio = Trailing P/E / 5-Year Annual EPS Growth Rate (%).
            • PEG < 1.0x: Growth at a deep discount; earnings are expanding faster than the valuation multiple.
            • 1.0x <= PEG <= 2.0x: Fair institutional valuation corridor for durable economic moats.
            • PEG > 2.0x: Elevated valuation premium; vulnerable to severe re-rating if growth slows by even 300 bps.

            4. Historical Valuation Corridor Bands:
            Companies tend to trade within distinct historical multiple corridors over 5 to 10 years. By plotting share price against 15x, 20x, 25x, and 30x EPS lines, investors can clearly see whether a stock is trading at the bottom of its historical band (margin of safety) or piercing through the top of its historical band (speculative exuberance).
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Trailing P/E Multiple" to "Current share price divided by audited trailing 12-month diluted EPS.",
            "Forward P/E Multiple" to "Current share price divided by consensus analyst forecast EPS for the next 12 months.",
            "Earnings Yield (E/P)" to "The reciprocal of P/E expressed as a percentage: EPS / Price; represents annual earnings return per dollar invested.",
            "Equity Risk Premium (ERP)" to "Earnings Yield minus the 10-Year US Treasury yield; measures the excess return demanded for holding equity risk.",
            "Peter Lynch PEG Ratio" to "P/E ratio divided by the long-term EPS growth rate; identifies growth at a reasonable price.",
            "Historical P/E Bands" to "Long-term valuation channels plotting stock price against constant multiple tiers (e.g. 15x, 20x, 25x, 30x).",
            "Forward Multiple Compression" to "The natural reduction in valuation multiple as projected earnings growth materializes over time.",
            "Cyclical Value Trap" to "A stock that appears artificially cheap at 5x P/E because peak-cycle earnings are about to collapse."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            To achieve this return, an investor must buy companies at cheap multiples whose multiples then expand exponentially. However, empirical market data proves that paying 50x to 80x P/E for popular growth stocks almost always results in MULTIPLE COMPRESSION. Even if the underlying company doubles its earnings over 3 years, if its P/E multiple compresses from 60x to 25x, the stock return is:
            (1 + 1.00 Earnings Growth) × (25 / 60 Multiple Contraction) - 1 = -16.7% Total Return!
            You cannot achieve durable compounding without valuation discipline.
        """.trimIndent(),
        fictionalExample = """
            Secular SaaS Inc. (High Multiple Compounder) vs. Commodity Metal Corp (Cheap Value Trap):
            • Secular SaaS Inc. ($150 Share Price, $5.00 EPS, 30x P/E):
              - Trailing P/E seems high at 30x (Earnings Yield = 3.33%).
              - However, net income compounds organically at 25% annually due to 95% net retention.
              - In 5 years, EPS triples to $15.25. Even if P/E compresses from 30x to 22x:
                Stock Price = 22 × $15.25 = $335.50 (+124% gain).
            • Commodity Metal Corp ($20 Share Price, $4.00 Peak EPS, 5x P/E):
              - Naive value screeners flag it as an incredible bargain at 5x P/E (20% Earnings Yield!).
              - Year 2 (Commodity Trough): Metal prices drop 35%. Operating leverage crushes net income from $4.00 to $0.50 EPS.
              - Even if the market awards it an 8x multiple on trough earnings:
                Stock Price = 8 × $0.50 = $4.00 (-80% catastrophic drawdown!).
              - What appeared to be a 5x bargain was in reality a lethal cyclical trap.
        """.trimIndent(),
        realStockExample = """
            Alphabet Inc. (NASDAQ: GOOGL) vs. Intel Corporation (NASDAQ: INTC) — 10-Year Audited SEC 10-K Case Study:
            • Alphabet Inc. (Secular Compounder):
              - Between 2015 and 2024, GOOGL typically traded between 22x and 28x P/E.
              - EPS compounded from $1.14 (split-adjusted) in FY15 to $8.04 in FY24 (7.05x earnings expansion!).
              - Because earnings expanded so massively, the stock compounded from $38 to $192 (+405% return) while maintaining a healthy median 24x multiple.
              - Average PEG ratio stayed disciplined at 1.1x to 1.4x throughout the decade.
            • Intel Corporation (Cyclical Value Trap):
              - In 2018-2020, Intel traded at what appeared to be an irresistible 10x to 12x P/E with a 9% Earnings Yield, while competitors traded at 30x+.
              - However, Intel had ceased organic innovation; AMD and TSMC captured the server datacenter market.
              - Peak EPS of $4.94 in 2020 collapsed to $1.94 in 2022, $0.40 in 2023, and net losses in 2024.
              - The stock cratered from $68 to $20 (-70% drawdown), proving that a low P/E on decaying earnings is the fastest way to destroy capital.
        """.trimIndent(),
        commonMistakes = listOf(
            "Buying Low P/E Cyclical Stocks at the Peak of the Macro Boom" to "Assuming a 4x P/E in shipping, steel, or oil is a bargain, unaware that earnings are at a historical peak and about to plunge 80%.",
            "Ignoring the 10-Year US Treasury Benchmark" to "Paying 35x P/E (2.85% Earnings Yield) when safe government bonds yield 4.25%, accepting negative risk premiums without growth verification.",
            "Confusing Forward P/E with Audited Reality" to "Treating optimistic sell-side consensus forward EPS estimates as guaranteed, setting up massive drawdowns upon earnings guidance misses.",
            "Failing to Normalize EPS for Non-Recurring Items" to "Calculating P/E on unadjusted net income that was artificially inflated by a one-time patent settlement, subsidiary sale, or tax credit.",
            "Ignoring Peter Lynch's PEG Discipline" to "Paying 50x P/E for a mature business growing at only 10% (PEG = 5.0x), leading to violent multiple compression."
        ),
        goldenRules = listOf(
            "The Earnings Yield Spread Rule: Demand that a stock's Earnings Yield (E/P) exceeds the 10-Year Treasury yield (ERP > 0) unless EPS growth exceeds 15% CAGR.",
            "The Peter Lynch PEG Filter: Refuse to pay a PEG ratio greater than 2.0x for mature compounders; target PEG < 1.2x for growth at a reasonable price.",
            "The Historical Valuation Band Mandate: Always plot share price against 5-year 15x, 20x, 25x, and 30x multiple corridors before executing an entry.",
            "The Forward Compression Verification: Ensure that forward P/E is supported by verified order book backlog, recurring revenue, or organic unit economics rather than hopeful analyst projections.",
            "The Quality-Adjusted Multiple Principle: Remember that a 25x P/E on a 30% ROIC monopoly moat with zero debt is fundamentally cheaper than a 10x P/E on a capital-intensive commodity producer."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 13,
        stageNumber = 3,
        stageTitle = "Stage 3 — Valuation",
        title = "Price-to-Sales (P/S) & EV Multiples",
        icon = Icons.Rounded.Analytics,
        coreQuestion = "Why is Enterprise Value (EV) a more comprehensive measure of corporate purchase cost than Market Capitalization, and how do top-line and EBITDA multiples eliminate debt blindness?",
        primaryFormula = "EV = Market Cap + Total Debt - Cash  |  EV/EBITDA  |  EV/EBIT  |  P/S = Market Cap / Revenue",
        researcherRule = "Always calculate Enterprise Value when analyzing companies with debt. An equity multiple like P/E or P/S is completely blind to debt liabilities and will trick you into buying leveraged value traps.",
        whatDataDoesNotProve = "A low Price-to-Sales multiple (e.g. 0.3x) proves top-line scale; it does NOT prove the business has positive unit economics, pricing power, or solvency to survive its debt burden.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_13_infographic.jpg",
        keyCheckpoints = listOf(
            "The Enterprise Value Bridge: EV represents the total acquisition cost—purchasing all equity and assuming all outstanding funded debt obligations while sweeping surplus cash reserves.",
            "Net Debt Anatomy: Net Debt = Total Debt - Cash & Cash Equivalents. When cash exceeds debt, the company possesses a 'Net Cash Fortress' and EV is strictly less than Market Cap.",
            "Price-to-Sales (P/S) Utility: Best suited for early-stage hypergrowth software, turnaround retail, or cyclical firms with temporarily suppressed margins where earnings are uninformative.",
            "The P/S Trap: Revenue is not cash. A company with $10B in sales at -15% operating margins is burning equity capital, making any positive P/S multiple an illusion.",
            "EV/Sales vs P/S: EV/Sales normalizes top-line valuation for debt leverage. Two companies with $1B sales and identical $2B market caps look identical on P/S (2.0x), but if Company B carries $3B in debt, its EV/Sales is 5.0x.",
            "EV/EBITDA (Capital Structure Neutrality): Evaluates operating cash generation before interest, tax, depreciation, and amortization, allowing cross-border and cross-capital-structure comparisons.",
            "EV/EBIT (The True Capital Charge): When physical depreciation and amortization reflect actual economic wear and tear that must be reinvested, EV/EBIT is strictly superior to EV/EBITDA.",
            "Sector Multiples Hierarchy: Capital-light SaaS (EV/Sales & P/S), Capital-heavy industrials/telecoms (EV/EBITDA), Manufacturing/logistics (EV/EBIT), Financials/Banks (P/E & P/B only; EV is meaningless for banks)."
        ),
        fullNotesOverview = """
            Enterprise Value (EV) is the cornerstone metric of institutional mergers, acquisitions, and private equity underwriting. While retail investors look exclusively at nominal share price or equity market capitalization, institutional allocators ask: 'What does it cost to buy the entire enterprise and own 100% of its operational cash flows?'
            
            When you purchase 100% of a company's common stock, you legally inherit its balance sheet debt liabilities. If a company has a ${'$'}40B equity market cap but carries ${'$'}140B in automotive debt and funded loans, the true takeover cost is ${'$'}180B minus cash reserves.
            
            By contrasting Enterprise Value against operating profit streams (EV/EBITDA, EV/EBIT, EV/Sales), investors achieve capital-structure neutrality: whether a company finances its assets with 90% debt or 100% equity, EV multiples evaluate the core engine's earning power independent of corporate financing choices.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Enterprise Value" to "The aggregate theoretical acquisition cost: Market Cap + Total Debt + Minority Interest - Cash.",
            "Net Debt" to "Funded debt liabilities minus liquid cash and short-term equivalents.",
            "EV/EBITDA" to "Debt-neutral operational multiple comparing total enterprise cost to cash operating earnings.",
            "EV/EBIT" to "Capital-charge adjusted multiple factoring in economic depreciation and equipment replacement.",
            "Price/Sales" to "Top-line multiple comparing equity valuation to gross annual revenue."
        ),
        compoundingRealityCheck = """
            Ignoring debt when looking at valuation multiples is the fastest route to permanent capital loss. 
            A retail investor buys an industrial stock at 5x P/E and 0.4x P/S, celebrating an apparent deep value bargain. 
            Two years later, interest rates rise, refinancing costs surge, and operating earnings are entirely swallowed by debt service. 
            The equity is wiped out in restructuring. 
            Had the investor calculated EV/EBITDA (which was an exorbitant 28x due to ${'$'}15B in hidden debt), the trap would have been instantly obvious.
        """.trimIndent(),
        fictionalExample = """
            Consider SaaS Corp (Market Cap ${'$'}10B, Debt ${'$'}0, Cash ${'$'}2B, Revenue ${'$'}1B) vs OldCo (Market Cap ${'$'}10B, Debt ${'$'}15B, Cash ${'$'}1B, Revenue ${'$'}1B).
            
            Both trade at an identical Price-to-Sales (P/S) of 10.0x.
            
            Now calculate Enterprise Value:
            - SaaS Corp EV = ${'$'}10B + ${'$'}0 - ${'$'}2B = ${'$'}8.0B (EV/Sales = 8.0x)
            - OldCo EV = ${'$'}10B + ${'$'}15B - ${'$'}1B = ${'$'}24.0B (EV/Sales = 24.0x)
            
            OldCo is actually 3x MORE EXPENSIVE than SaaS Corp on an enterprise basis, despite having identical market caps and sales!
        """.trimIndent(),
        realStockExample = """
            Salesforce (CRM) vs. Ford Motor (F) (Audited Capital Structure Contrast):
            
            - Salesforce (CRM):
              • Equity Market Cap: ~${'$'}295B | Total Debt: ${'$'}12.5B | Cash: ${'$'}14.2B | Net Debt: -${'$'}1.7B (Net Cash)
              • Enterprise Value: ~${'$'}293B (EV < Market Cap!)
              • Revenue: ~${'$'}35B | P/S: 8.4x | EV/Sales: 8.4x | EV/EBITDA: ~33x
              • Capital Structure: 96% Equity, 4% Debt. Zero refinancing insolvency hazard.
              
            - Ford Motor (F):
              • Equity Market Cap: ~${'$'}44B | Total Funded Debt: ~${'$'}148B | Cash: ~${'$'}28B | Net Debt: ~${'$'}120B
              • Enterprise Value: ~${'$'}164B (EV is nearly 4x Market Cap!)
              • Revenue: ~${'$'}176B | P/S: 0.25x (Apparent bargain!) | EV/Sales: 0.93x | EV/EBITDA: ~13x
              • Capital Structure: 23% Equity, 77% Debt Claims. Highly vulnerable to credit downgrades and rate spikes.
        """.trimIndent(),
        commonMistakes = listOf(
            "Comparing P/E or P/S between leveraged and debt-free companies" to "Severely distorts relative valuation by ignoring interest obligations and bankruptcy risk.",
            "Applying EV multiples to commercial banks or insurance companies" to "Debt for a bank is raw material/inventory (deposits), not corporate leverage. Use P/E and P/TBV for financials.",
            "Treating EBITDA as pure free cash flow" to "EBITDA ignores CapEx, working capital drains, taxes, and debt interest. As Charlie Munger famously warned: 'Does management think the tooth fairy pays for CapEx?'",
            "Buying high P/S stocks assuming high margins will naturally follow" to "Many SaaS and consumer tech firms never expand gross margins sufficiently to justify 15x+ EV/Sales.",
            "Failing to deduct non-operating cash from EV" to "Overstates enterprise cost for cash fortress compounders like Alphabet or Berkshire Hathaway."
        ),
        goldenRules = listOf(
            "The Enterprise Value Primacy: Always evaluate EV alongside Market Cap to expose balance sheet debt claims.",
            "The Net Cash Discount: When Net Debt is negative, EV is smaller than Market Cap, making cash-rich compounders cheaper than headline multiples suggest.",
            "The EV/EBIT CapEx Filter: In capital-intensive sectors, demand EV/EBIT rather than EV/EBITDA to account for true economic depreciation.",
            "The Margin-Adjusted P/S Rule: Never pay more than 5x P/S unless gross margins exceed 70% and net margin potential exceeds 20%.",
            "The Credit Cycle Check: Check debt maturity schedules before buying low EV/EBITDA cyclicals at economic peaks."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 14,
        stageNumber = 3,
        stageTitle = "Stage 3 — Valuation",
        title = "Free Cash Flow & Owner Earnings Valuation",
        icon = Icons.Rounded.AccountBalance,
        coreQuestion = "What discretionary cash yield would you receive if you owned 100% of the business, and how do we calculate Warren Buffett's true Owner Earnings?",
        primaryFormula = "FCF Yield = (FCF / Market Cap) × 100  |  Price / FCF  |  Owner Earnings = NI + D&A - Maint CapEx ± ΔWC",
        researcherRule = "Accounting net income is an opinion; Free Cash Flow is a hard fact. Demand an FCF Yield > 5% on companies with high ROIC to ensure a robust mathematical margin of safety.",
        whatDataDoesNotProve = "A high temporary FCF yield caused by slashing maintenance CapEx or liquidating inventory proves current cash collection; it does NOT prove sustainable earnings power.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_14_infographic.jpg",
        keyCheckpoints = listOf(
            "Free Cash Flow Anatomy: FCF = Operating Cash Flow (CFO) - Capital Expenditures (CapEx). It represents cash left over after maintaining and expanding the physical asset base.",
            "Free Cash Flow Yield: FCF Yield = (FCF / Market Cap) × 100%. Expresses discretionary owner cash generation as an annualized bond-equivalent yield.",
            "Price to Free Cash Flow (P/FCF): The cash equivalent of P/E. A stock trading at 20x P/FCF offers a 5.0% FCF yield.",
            "Warren Buffett's 1986 Owner Earnings: Reported Net Income + Depreciation & Non-Cash Charges - Average Maintenance CapEx ± Working Capital Adjustments.",
            "Maintenance CapEx vs Growth CapEx: Maintenance CapEx is required to maintain existing unit sales and competitive positioning; Growth CapEx is optional reinvestment for expansion.",
            "Cash Conversion Ratio: (FCF / Net Income) × 100%. Quality compounders consistently convert >90% (often >100%) of accounting net income into cash.",
            "The CapEx Deficit Trap: Companies that under-invest in maintenance CapEx temporarily inflate FCF, only to suffer massive equipment breakdowns and obsolescence later.",
            "FCF Yield vs Risk-Free Rate: Compare corporate FCF Yield against the 10-Year US Treasury yield. If FCF yield is lower than risk-free government bonds, the business must deliver sustained growth to justify holding equity risk."
        ),
        fullNotesOverview = """
            Free Cash Flow (FCF) is the ultimate arbiter of corporate truth. While GAAP accounting accruals allow management considerable discretion over revenue recognition, depreciation schedules, and non-cash expense categorization, cash deposited into corporate treasury accounts cannot be fabricated.
            
            In his landmark 1986 letter to Berkshire Hathaway shareholders, Warren Buffett demystified corporate accounting by formalizing 'Owner Earnings': the net cash dollar amount an owner could pull out of the business annually without impairing its competitive moat or productive capacity.
            
            By evaluating stocks through the dual lenses of FCF Yield and Price/FCF, disciplined allocators cut through paper accounting profits, identify genuine cash-compounding machines, and avoid capital treadmills that devour shareholder cash just to stand still.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Free Cash Flow (FCF)" to "Cash from operations minus capital expenditures: the true discretionary cash generated.",
            "FCF Yield" to "Free cash flow produced expressed as an annual percentage of equity market capitalization.",
            "Price / FCF" to "Equity market valuation divided by trailing twelve month free cash flow.",
            "Owner Earnings" to "Net income + non-cash D&A minus required maintenance capital expenditures.",
            "Cash Conversion" to "The percentage of reported net accounting profit converted into hard free cash flow."
        ),
        compoundingRealityCheck = """
            A company can report growing EPS for five consecutive years while simultaneously marching toward bankruptcy. 
            How? By booking revenue on aggressive credit terms (ballooning accounts receivable) and capitalizing operating costs as balance sheet assets. 
            On the income statement, profits soar; on the cash flow statement, Operating Cash Flow is negative, and Free Cash Flow is bleeding. 
            Investors who check FCF Yield spot the divergence immediately and protect their capital.
        """.trimIndent(),
        fictionalExample = """
            Company Alpha vs Company Beta (Both report ${'$'}100M Net Income):
            
            - Company Alpha (Software/Asset-Light):
              • Operating Cash Flow: ${'$'}120M
              • CapEx: ${'$'}15M (Maintenance ${'$'}5M, Growth ${'$'}10M)
              • Free Cash Flow: ${'$'}105M (105% Cash Conversion)
              • Owner Earnings: ${'$'}100M + ${'$'}10M D&A - ${'$'}5M Maint CapEx = ${'$'}105M
              • Market Cap: ${'$'}2,000M -> FCF Yield = 5.25% (P/FCF = 19x)
              
            - Company Beta (Heavy Manufacturing/Foundry):
              • Operating Cash Flow: ${'$'}110M
              • CapEx: ${'$'}130M (Maintenance ${'$'}90M, Growth ${'$'}40M)
              • Free Cash Flow: -${'$'}20M (Negative Cash Flow!)
              • Owner Earnings: ${'$'}100M + ${'$'}70M D&A - ${'$'}90M Maint CapEx = ${'$'}80M
              • Market Cap: ${'$'}2,000M -> FCF Yield = Negative (Capital Consumer)
              
            Company Alpha can fund buybacks, dividends, and acquisitions with organic cash. Company Beta must continually issue debt or dilute equity just to replace aging machinery.
        """.trimIndent(),
        realStockExample = """
            Apple (AAPL) vs. Intel (INTC) (Audited 10-Year Cash Flow Divergence):
            
            - Apple (AAPL) — The Elite Cash Machine:
              • Uses outsourced contract manufacturing (TSMC, Foxconn) with extraordinary supply chain bargaining power.
              • Annual Operating Cash Flow: ~${'$'}115B - ${'$'}120B
              • Annual CapEx: Only ~${'$'}9B - ${'$'}11B (under 10% of CFO!)
              • Discretionary Free Cash Flow: ~${'$'}105B - ${'$'}110B
              • Cash Conversion: Consistently >100% of Net Income.
              • Deploys ~${'$'}80B - ${'$'}90B annually into share repurchases, retiring >35% of total share count over a decade.
              
            - Intel (INTC) — The Capital Treadmill:
              • Must build leading-edge semiconductor fabrication plants costing ${'$'}15B - ${'$'}20B per fab.
              • Annual Operating Cash Flow: ~${'$'}11B - ${'$'}15B
              • Annual CapEx: ~${'$'}24B - ${'$'}26B (Exceeds operating cash flow!)
              • Discretionary Free Cash Flow: Negative ${'$'}(10B) to ${'$'}(15B) in recent fiscal cycles.
              • Cash Conversion: Negative. Forced to slash dividends, halt buybacks, and seek government subsidies and external equity partners.
        """.trimIndent(),
        commonMistakes = listOf(
            "Treating Operating Cash Flow as Free Cash Flow" to "OCF does not subtract CapEx. Capital expenditures are mandatory to keep the business operational.",
            "Ignoring Maintenance vs Growth CapEx distinction" to "Treats high-growth compounders investing in high-ROIC expansion as if they were capital destroyers.",
            "Chasing extreme FCF Yields (>12%) without verifying CapEx adequacy" to "Often indicates management slashed maintenance CapEx to cosmetically boost short-term cash flow before an exit.",
            "Failing to adjust for Stock-Based Compensation (SBC)" to "SBC is added back to Operating Cash Flow as a non-cash expense, but it dilutes equity owners. True owner FCF should deduct net dilution.",
            "Ignoring working capital swings in cyclical industries" to "A one-time inventory liquidation boosts cash flow temporarily but is completely unsustainable."
        ),
        goldenRules = listOf(
            "The Cash Conversion Hurdle: Demand that a company convert at least 85% of 5-year cumulative net income into free cash flow.",
            "The Discretionary Cash Flow Test: Only buy companies that generate positive FCF across full economic cycles, including recessions.",
            "The FCF Yield Benchmark: Target an FCF Yield exceeding the 10-Year US Treasury yield + 1.5% for equity risk compensation.",
            "The Owner Earnings Reality Check: Deduct estimated maintenance CapEx rather than total CapEx when assessing long-term compounding power.",
            "The Reinvestment Rate Equation: True shareholder value = FCF Yield + (Reinvestment Rate × ROIC)."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 15,
        stageNumber = 3,
        stageTitle = "Stage 3 — Valuation",
        title = "Discounted Cash Flow (DCF) Valuation",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "What is the intrinsic mathematical value of all future cash flows discounted back to today's dollars, and how large is our Margin of Safety?",
        primaryFormula = "Fair Value = [Sum(FCF_t / (1 + WACC)^t) + PV(Terminal Value) + Cash - Debt] / Shares",
        researcherRule = "A DCF is an educated range, never a precise decimal. Always demand at least a 25% to 30% Margin of Safety below your central valuation estimate.",
        whatDataDoesNotProve = "A mathematically elegant DCF model does not prove future revenue or margin stability; garbage in equals garbage out with high precision.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_15_infographic.jpg",
        keyCheckpoints = listOf(
            "The Present Value Concept: A dollar collected 5 years from now is worth less than a dollar today due to inflation, opportunity cost, and investment risk.",
            "WACC (Discount Rate): Weighted Average Cost of Capital synthesizes the cost of equity (CAPM: Risk-Free Rate + Beta × ERP) and after-tax cost of debt.",
            "Discrete Cash Flow Forecasts: Modeling realistic Free Cash Flows for years 1 through 5 based on unit economics, reinvestment rates, and secular industry TAM.",
            "Terminal Value (Gordon Growth): TV = [FCF_n × (1 + g)] / (WACC - g). The perpetual growth rate (g) should never exceed long-term GDP growth (2.0% - 3.0%).",
            "Terminal Value Sensitivity: In high-growth DCF models, 65% to 80% of total enterprise value resides in the terminal value assumption. Small WACC shifts cause massive valuation swings.",
            "Enterprise Value to Equity Value Bridge: Equity Intrinsic Value = Enterprise Present Value + Cash & Equivalents - Total Funded Debt.",
            "Fair Value per Share: Divide total equity intrinsic value by current diluted shares outstanding (penalizing for future stock-based compensation dilution).",
            "The Margin of Safety Filter: Buying at fair value yields market-rate returns; buying at a 25% - 35% discount protects against forecasting errors and macro shocks."
        ),
        fullNotesOverview = """
            Discounted Cash Flow (DCF) analysis represents the bedrock theory of finance: any asset's fundamental worth is strictly equal to the net cash it can distribute to its owners over its lifetime, discounted back to present value.
            
            Unlike multiple-based valuation (P/E, EV/EBITDA), which merely tells you what other investors are paying right now, a DCF forces the analyst to explicitly state their operational assumptions: growth velocity, profit margins, reinvestment needs, and cost of capital.
            
            By running two-stage DCF models across conservative, base, and bear scenarios, institutional allocators determine whether the current market quote offers an asymmetric risk/reward ratio with a durable Margin of Safety.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Present Value (PV)" to "The current worth of a future sum of money given a specified rate of return (discount rate).",
            "WACC" to "Weighted Average Cost of Capital: the hurdle rate reflecting a firm's blended cost of equity and debt.",
            "Terminal Value (TV)" to "The estimated present value of all cash flows beyond the discrete projection horizon.",
            "Gordon Growth Model" to "Perpetual annuity formula assuming cash flows grow at a constant sustainable rate indefinitely.",
            "Margin of Safety" to "The percentage discount between current stock price and calculated intrinsic fair value."
        ),
        compoundingRealityCheck = """
            Small tweaks to DCF inputs create massive illusions. 
            Lowering your discount rate from 9.0% to 7.5% and raising terminal growth from 2.5% to 3.5% can easily double the outputted 'fair value' of a stock without the underlying company selling a single additional product!
            Professional analysts combat this confirmation bias by running rigorous sensitivity matrices and demanding a strict 25%+ Margin of Safety.
        """.trimIndent(),
        fictionalExample = """
            DCF Walkthrough: SoftwareCorp Inc.
            
            • TTM Free Cash Flow: ${'$'}100M
            • 5-Year Growth Forecast: 10% annually (Year 1: ${'$'}110M -> Year 5: ${'$'}161M)
            • WACC (Discount Rate): 9.0%
            • Terminal Growth Rate (g): 2.5% (below long-term GDP)
            • Cash: ${'$'}50M | Total Debt: ${'$'}30M | Diluted Shares: 50M | Current Price: ${'$'}48.00
            
            Calculations:
            1. Sum of 5-Year Discounted FCFs:
               • PV(Y1 to Y5) = ${'$'}101M + ${'$'}93M + ${'$'}85M + ${'$'}78M + ${'$'}72M = ${'$'}429M
            2. Terminal Value:
               • TV = [${'$'}161M × 1.025] / (0.09 - 0.025) = ${'$'}165M / 0.065 = ${'$'}2,538M
               • PV(Terminal Value) = ${'$'}2,538M / (1.09)^5 = ${'$'}1,649M (79% of Enterprise Value)
            3. Enterprise Intrinsic Value: ${'$'}429M + ${'$'}1,649M = ${'$'}2,078M
            4. Equity Value: ${'$'}2,078M + ${'$'}50M (Cash) - ${'$'}30M (Debt) = ${'$'}2,098M
            5. Fair Value per Share: ${'$'}2,098M / 50M shares = ${'$'}41.96 per share
            
            Conclusion: Current market price of ${'$'}48.00 is a 14% premium above fair value. To achieve a 25% Margin of Safety (${'$'}31.47), wait for a market pullback.
        """.trimIndent(),
        realStockExample = """
            Apple (AAPL) vs. Microsoft (MSFT) DCF Structural Sensitivity:
            
            - Apple (AAPL):
              • FCF Baseline: ~${'$'}108B
              • Growth Engine: 5% - 7% mature services growth and iPhone replacement cycles.
              • Capital Structure: Substantial buybacks retire ~3% of shares annually, boosting per-share intrinsic value even during modest top-line growth.
              • DCF Sensitivity: Because Apple generates immense near-term cash, discrete 5-year FCF comprises a healthier ~28% of intrinsic value, making it less sensitive to extreme terminal rate errors.
              
            - Microsoft (MSFT):
              • FCF Baseline: ~${'$'}74B
              • Growth Engine: Azure cloud and enterprise AI monetization driving 14% - 16% growth.
              • Reinvestment: High CapEx (~${'$'}50B+) into AI datacenters temporarily depresses current FCF.
              • DCF Sensitivity: Over 78% of Microsoft's DCF value rests on terminal assumptions and cash flows in years 6 through 15, demanding higher conviction in long-term AI secular adoption.
        """.trimIndent(),
        commonMistakes = listOf(
            "Using terminal growth rates higher than global GDP (>3.5%)" to "Mathematically implies the company will eventually become larger than the entire global economy.",
            "Ignoring future share dilution from Stock-Based Compensation (SBC)" to "If share count expands by 3% annually, per-share intrinsic value is severely eroded.",
            "Lowering WACC to justify an expensive stock price" to "Artificially dropping the discount rate from 9% to 6% disguises market risk and destroys safety margins.",
            "Treating DCF output as an exact truth rather than a probability distribution" to "A single point estimate creates false confidence. Always review the WACC-Growth sensitivity table."
        ),
        goldenRules = listOf(
            "The Margin of Safety Rule: Never purchase equity without at least a 20% to 30% discount below conservative intrinsic DCF value.",
            "The Terminal Reality Check: Ensure terminal perpetual growth rate is strictly capped at 2.0% to 2.5%.",
            "The WACC Hurdle: Never use a WACC below 8.0% for equities, regardless of how low central bank interest rates drop.",
            "The 3-Scenario Discipline: Always model Base (50% prob), Bull (25% prob), and Bear (25% prob) cases before deploying capital.",
            "The Cash Reinvestment Rule: Verify that projected FCF accounts for all required maintenance and growth CapEx."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 16,
        stageNumber = 3,
        stageTitle = "Stage 3 — Valuation",
        title = "Relative Valuation & Peer Benchmarking",
        icon = Icons.Rounded.BarChart,
        coreQuestion = "How does this stock trade relative to its peer group and 5-year historical bands, and why is a great company NOT automatically a great investment?",
        primaryFormula = "Peer Premium % = [(Multiple - Peer Median) / Peer Median] × 100  |  Hist Percentile = Rank(Multiple) / N",
        researcherRule = "A great business at 3x fair value is a catastrophic investment. Compare multiples across direct competitors, historical ranges, and growth-adjusted metrics.",
        whatDataDoesNotProve = "Trading at a 30% discount to peers does NOT prove a stock is undervalued if the entire sector is trapped in an unsustainable speculative bubble.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_16_infographic.jpg",
        keyCheckpoints = listOf(
            "The Relative Valuation Principle: Evaluating corporate multiples (P/E, EV/EBITDA, P/S, FCF Yield) against direct competitors operating under identical economic conditions.",
            "Direct Peer Group Selection: True peers must share similar unit economics, cyclicality, geographical exposure, and capital intensity. Comparing a fabless designer to a capital-heavy foundry is invalid.",
            "Peer Group Median Normalization: Always compare against median rather than mean to prevent a single distressed or outlier company from skewing the sector benchmark.",
            "The 5-Year Historical Valuation Corridor: Comparing a stock's current multiple against its own 5-year minimum, 25th percentile, median, 75th percentile, and peak multiples.",
            "Growth-Adjusted Multiples (PEG & EV/Growth): Normalizing multiples by projected growth rates. An EV/EBITDA of 20x growing at 30% is cheaper than an EV/EBITDA of 12x growing at 2%.",
            "The Golden Axiom: Great Company ≠ Great Investment. The entry multiple paid mathematically dictates forward 5-year annualized shareholder return.",
            "Multiple Expansion vs Multiple Contraction: Returns = Earnings Growth + Multiple Change + Dividend Yield. Multiple contraction can completely wipe out 20% annual EPS growth.",
            "The Sector Bubble Warning: In 1999/2000 and 2021, relative valuation gave false comfort because every stock in the cohort was 300% overvalued. Always anchor relative multiples to absolute DCF bounds."
        ),
        fullNotesOverview = """
            Relative valuation is the most widely practiced valuation methodology on Wall Street. By benchmarking a stock against its direct competitors and historical norms, investors assess how the market currently prices a dollar of earnings, sales, or cash flow.
            
            However, relative valuation holds a dangerous psychological trap: it measures popularity, not intrinsic worth. If an entire sector enters a speculative mania, the 'cheapest' stock in the cohort may still be drastically overvalued.
            
            Disciplined quantitative investors deploy relative valuation as a secondary sanity filter alongside absolute DCF analysis, searching for high-quality businesses temporarily trading at deep discounts to their historical and peer valuation corridors.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Peer Multiple" to "Valuation metric (e.g. EV/EBITDA) compared against the median of direct industry competitors.",
            "Historical Corridor" to "A company's historical multiple range over a full economic cycle (typically 5 to 10 years).",
            "Multiple Contraction" to "The decline in a valuation multiple over time, which acts as a powerful headwind to investor returns.",
            "PEG Ratio" to "Price-to-Earnings divided by growth rate, normalizing valuation for growth trajectory.",
            "Peer Premium/Discount" to "The percentage difference between a target firm's multiple and its peer group median."
        ),
        compoundingRealityCheck = """
            Consider Cisco Systems (CSCO) in March 2000:
            Cisco was undeniably one of the greatest networking equipment companies in history, growing revenues at 40%+ annually with dominant moats.
            However, at the peak of the dot-com bubble, investors paid 130x P/E and 35x P/S.
            Over the following 20 years, Cisco continued to grow revenues and profits substantially. 
            Yet, because investors drastically overpaid on a relative and absolute basis, the stock took more than two decades just to break even! Entry valuation matters infinitely.
        """.trimIndent(),
        fictionalExample = """
            Relative Valuation Audit: MedTech Corp vs Peers
            
            • MedTech Corp: Trailing P/E: 28.0x | EV/EBITDA: 18.5x | Rev Growth: 14% | ROIC: 22%
            • Peer 1 (HealthGlobal): P/E: 22.0x | EV/EBITDA: 14.0x | Rev Growth: 6% | ROIC: 11%
            • Peer 2 (BioInstruments): P/E: 24.5x | EV/EBITDA: 15.2x | Rev Growth: 8% | ROIC: 13%
            • Peer 3 (CareTech): P/E: 26.0x | EV/EBITDA: 16.0x | Rev Growth: 9% | ROIC: 14%
            
            Benchmarking Analysis:
            • Peer Group Median P/E: 24.5x
            • Peer Group Median EV/EBITDA: 15.2x
            • MedTech Corp Peer Premium: +14.3% on P/E, +21.7% on EV/EBITDA
            
            Growth-Adjusted PEG Comparison:
            • MedTech Corp PEG: 28.0 / 14 = 2.0x
            • Peer Median PEG: 24.5 / 8 = 3.06x
            
            Conclusion: Although MedTech Corp trades at a 14% multiple premium to peers, its 14% growth and 22% ROIC deliver a superior growth-adjusted PEG (2.0x vs 3.06x). The quality premium is justified.
        """.trimIndent(),
        realStockExample = """
            NVIDIA (NVDA) vs. AMD (AMD) Semiconductor Peer Benchmarking:
            
            - NVIDIA (NVDA):
              • Dominates data center AI accelerators with >80% market share and CUDA software moat.
              • Gross Margins: ~75% | ROIC: ~58%
              • Forward P/E trades between 35x - 45x during explosive compute expansion cycles.
              • Peer Premium: Consistently trades at a 30% - 60% premium to broader semiconductor benchmarks.
              • Justification: Operating profit margins and ROIC exceed peer medians by more than 2.5x.
              
            - AMD (AMD):
              • Direct competitor in GPUs (ROCm) and x86 CPUs (EPYC).
              • Gross Margins: ~50% - 53% | ROIC: ~12% - 15%
              • Forward P/E trades between 25x - 32x.
              • Valuation Verdict: When AMD trades at a similar P/E multiple to NVIDIA despite lower software capture and half the ROIC, capital flows toward NVIDIA as the superior risk-adjusted relative quality vehicle.
        """.trimIndent(),
        commonMistakes = listOf(
            "Comparing companies across different business models" to "Comparing software-as-a-service (80% gross margins) to hardware distributors (15% gross margins) under a single multiple.",
            "Confusing 'cheapest multiple' with 'best investment'" to "The lowest P/E in a sector is almost always a structurally impaired value trap with declining market share.",
            "Ignoring cyclical peak multiple inversion" to "Cyclical commodity firms trade at their lowest P/E at the cyclical peak and highest P/E at the bottom.",
            "Relying on peer multiples during market manias" to "If the entire cohort is trading at 50x P/S, being the 'cheapest' at 35x P/S will not prevent an 80% loss."
        ),
        goldenRules = listOf(
            "The Peer Normalization Rule: Always use median peer metrics rather than mean to eliminate distortions from distressed outliers.",
            "The Quality-Multiple Alignment: Never pay a peer-premium multiple unless ROIC and organic growth meaningfully outpace the peer median.",
            "The 5-Year Historical Corridor Check: Never buy a stock trading above the 90th percentile of its 5-year historical valuation band without clear structural thesis changes.",
            "The Growth-Adjusted PEG Rule: Require PEG < 1.5x relative to peer group medians before paying up for sector growth leaders.",
            "The Exit Multiple Sanity Check: Model multiple contraction back to sector median in your 5-year forward return projections."
        )
    ),

    // ------------------------------------------------------------------------
    // STAGE 4: QUANTITATIVE INVESTING (Lessons 17 - 22)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 17,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "What is a Factor? (Quantitative Style Investing)",
        icon = Icons.Rounded.School,
        coreQuestion = "What measurable corporate characteristics persistently explain differences in long-term stock returns across thousands of equities?",
        primaryFormula = "R_i - R_f = Alpha + Beta_mkt(R_m - R_f) + Beta_SMB(SMB) + Beta_HML(HML) + Beta_RMW(RMW) + Beta_CMA(CMA)",
        researcherRule = "Individual stocks are packages of underlying systematic factors. Know your factor tilts to avoid unintended macro and style factor concentrations.",
        whatDataDoesNotProve = "Academic factor outperformance does not guarantee positive returns every quarter; all factors suffer multi-year cyclical drawdowns.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_17_infographic.jpg",
        keyCheckpoints = listOf(
            "The Systematic Factor Revolution: Instead of treating stocks as unique narratives, quantitative finance proves that >80% of cross-sectional return variation stems from a handful of systematic factors.",
            "The Fama-French Multi-Factor Model: Nobels Eugene Fama and Kenneth French proved that Market Beta alone (CAPM) fails to explain returns, formalizing Size (SMB), Value (HML), Profitability (RMW), and Investment (CMA).",
            "The Value Factor (HML): Historically, buying low-multiple stocks (high Book-to-Market, high Earnings Yield, high FCF Yield) generates excess returns over expensive glamor stocks.",
            "The Quality Factor: Companies with high ROIC, stable earnings, low debt leverage, and low accounting accruals persistently beat junk equities on a risk-adjusted basis.",
            "The Momentum Factor (Carhart 4-Factor): Stocks that have outperformed peers over the trailing 3 to 12 months (excluding the most recent month) tend to persist in their outperformance.",
            "The Low Volatility Anomaly: Counter-intuitively, low-beta and low-volatility stocks historically generate higher Sharpe ratios and lower drawdowns than lottery-ticket high-beta stocks.",
            "The Investment Conservatism Factor (CMA): Firms that reinvest capital conservatively (low asset growth, disciplined CapEx) vastly outperform firms that aggressively empire-build or dilute equity.",
            "Factor Cyclicality & Regime Dependency: Factors move in multi-year macroeconomic waves. Value leads during inflationary expansions; Quality and Low Volatility dominate recessions."
        ),
        fullNotesOverview = """
            Quantitative Factor Investing marks the transition from single-stock discretionary picking to institutional statistical portfolio management.
            
            A 'Factor' is an academically validated, persistent, and economically grounded corporate characteristic that explains why certain groups of equities earn excess risk-adjusted returns over full market cycles.
            
            By scoring and ranking companies across the six foundational pillars — Value, Quality, Momentum, Size, Low Volatility, and Profitability — quantitative analysts decode a portfolio's true systematic exposures and eliminate emotional biases from capital allocation.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Factor" to "A persistent, quantifiable characteristic that systematically explains differences in stock returns.",
            "Value (HML)" to "High Minus Low: the historical return premium of cheap book/earnings stocks over growth stocks.",
            "Size (SMB)" to "Small Minus Big: the historical return premium associated with smaller market capitalization companies.",
            "Quality (RMW)" to "Robust Minus Weak: the outperformance of highly profitable, high-ROIC firms over weak margin operators.",
            "Investment (CMA)" to "Conservative Minus Aggressive: the outperformance of capital-disciplined firms over heavy asset spenders.",
            "Factor Tilt" to "A portfolio's intentional statistical overweight toward specific style factors (e.g. Quality + Value tilt)."
        ),
        compoundingRealityCheck = """
            Many investors believe they are brilliant stock pickers when they are actually just riding a powerful factor tailwind.
            During the 2020-2021 liquidity boom, buying unprofitable tech stocks looked like genius because the 'High Beta + High Growth' factor was surging. 
            When interest rates rose in 2022, that exact factor suffered an 80% collapse. 
            Dissecting your investments through factor attribution reveals whether your returns come from true alpha or accidental factor exposure.
        """.trimIndent(),
        fictionalExample = """
            Factor Decomposition: Two Portfolios Compared
            
            • Portfolio A (Narrative Tech Fan):
              • Holdings: 15 High-growth, unprofitable SaaS & EV stocks.
              • Factor Exposures: Value = 12th Percentile | Quality = 28th Percentile | Momentum = 92nd Percentile | Beta = 1.65 (Extremely High).
              • Risk: Extreme vulnerability to rising discount rates and momentum crashes.
              
            • Portfolio B (Systematic Multi-Factor Compounder):
              • Holdings: 25 High-ROIC companies with low debt, trading at reasonable FCF yields.
              • Factor Exposures: Value = 65th Percentile | Quality = 94th Percentile | Momentum = 60th Percentile | Beta = 0.88 (Low Volatility).
              • Performance: Endures market drawdowns with half the volatility while capturing superior 10-year risk-adjusted compounding.
        """.trimIndent(),
        realStockExample = """
            Berkshire Hathaway (BRK.B) — The Ultimate Quantitative Multi-Factor Machine:
            
            Academic researchers (Frazzini, Kabiller, and Pedersen in 'Buffett's Alpha') decomposed Warren Buffett's 50-year track record into systematic factors:
            • Value Factor: Enormous exposure to low Price-to-Book and high Earnings Yield equities.
            • Quality Factor: Heavy statistical tilt toward high ROIC, robust operating margins, and low leverage.
            • Low Beta Factor: Buffett systematically selects stocks with beta < 0.8, avoiding speculative market lottery tickets.
            • Conclusion: Buffett's outperformance is not magic; it is the ruthless, disciplined, five-decade execution of a multi-factor Quality + Value + Low-Risk quantitative strategy!
        """.trimIndent(),
        commonMistakes = listOf(
            "Chasing the latest hot factor after multi-year run-ups" to "Buying momentum at cyclical peaks right before a brutal factor rotation into value.",
            "Abandoning sound factors during normal cyclical drawdowns" to "Giving up on Value in 2020 right before it embarked on a multi-year recovery.",
            "Ignoring factor crowding" to "When too many quantitative funds pile into the exact same low-volatility names, the factor becomes expensive and vulnerable.",
            "Confusing single-stock narratives with factor exposures" to "Believing you are buying a unique corporate vision when you are simply taking on 2.0x market beta."
        ),
        goldenRules = listOf(
            "The Multi-Factor Diversification Rule: Never rely on a single factor; blend non-correlated factors (e.g. Quality + Value + Momentum) for portfolio resilience.",
            "The Discipline Requirement: Harvest factor premiums by committing to systematic rebalancing across multi-year cycles.",
            "The Quality Anchor: Use Quality (high ROIC, low accruals) as a primary filter to eliminate value traps before scoring value multiples.",
            "The Low Beta Advantage: Institutional Sharpe ratios are maximized by compounding low-volatility, cash-generative leaders.",
            "The Reinvestment Check: Favor companies scoring in the top quintile of Conservative Investment (CMA) with disciplined share buybacks."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 18,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "Momentum Factors",
        icon = Icons.AutoMirrored.Rounded.ShowChart,
        coreQuestion = "Why do stocks that have outperformed over 3 to 12 months tend to continue outperforming?",
        primaryFormula = "12-1M Momentum = (Price_t-1 / Price_t-12) - 1  (excluding most recent month to avoid mean reversion)",
        researcherRule = "Momentum is a persistent quantitative anomaly driven by investor underreaction, but suffers severe crashes at market inflection points. Always combine momentum with quality filters.",
        whatDataDoesNotProve = "Short-term momentum does not prove durable fundamental health or intrinsic value; a speculative bubble can exhibit high momentum right before collapsing.",
        assetInfographicPath = "learnings/lesson_18_infographic.jpg",
        keyCheckpoints = listOf(
            "12-1 Month Relative Price Momentum Window",
            "Moving Average Trend Confirmation (50 DMA vs 200 DMA)",
            "Behavioral Underreaction & Disposition Effect Drivers",
            "Momentum Crash Risk & Market Turning Points"
        ),
        fullNotesOverview = """
            Price momentum is one of the most robust and empirically verified market anomalies in quantitative finance, first systematically documented by Jegadeesh and Titman (1993).
            
            Equities that rank in the top decile of 3-to-12-month relative price performance systematically outperform bottom-decile laggards over the subsequent 1 to 6 months.
            
            This phenomenon is grounded in behavioral economics: investors and Wall Street analysts exhibit 'underreaction' by updating their earnings expectations too slowly when fundamental surprises occur, while the 'disposition effect' leads market participants to sell winners too prematurely, creating sustained price trends.
            
            However, pure price momentum exhibits fat-tailed crash risk during macroeconomic regime transitions (such as early 2009 or late 2020), when beaten-down cyclical laggards violently rebound. Institutional quant funds hedge this risk by pairing momentum with fundamental quality and technical trend gates (e.g. 50-day and 200-day moving average spreads).
        """.trimIndent(),
        conceptsSummary = mapOf(
            "12-1M Momentum" to "Calculates intermediate price return over 12 months while strictly omitting the most recent month to prevent short-term liquidity reversal.",
            "Investor Underreaction" to "The behavioral bias where analysts adjust forecasts gradually rather than immediately reflecting fundamental positive surprises.",
            "Disposition Effect" to "The behavioral tendency to sell winning stocks too quickly and hold losing stocks too long, dampening initial price adjustment.",
            "Momentum Crash" to "A sudden, violent drawdown occurring when low-quality, high-beta laggards rally dramatically off market bottoms.",
            "Trend Filter" to "Using 50 DMA and 200 DMA positioning to verify that an equity is in an established structural uptrend before initiating momentum exposure."
        ),
        compoundingRealityCheck = """
            Momentum is a high-octane performance enhancer, but an unfiltered momentum strategy without stop-losses or fundamental checks is catastrophic.
            During euphoric speculative cycles (such as 2000 tech or 2021 hyper-growth), momentum strategies chase stocks trading at 100x sales with no earnings.
            When liquidity contracts, these unanchored high-momentum leaders experience 70% to 90% drawdowns, permanently destroying compound growth.
            Disciplined quantitative investors treat momentum as a timing and weighting overlay on top of proven fundamental cash flow generators.
        """.trimIndent(),
        fictionalExample = """
            Unfiltered Momentum vs Quality-Gated Momentum:
            
            • Strategy A (Blind Momentum Chaser):
              • Screens purely for top 10% 6-month price performers.
              • Holds 20 hyper-volatile meme stocks and unprofitable biotech companies.
              • Year 1: Gains +85% during bull market euphoria.
              • Year 2: Market regime turns; strategy suffers a -65% momentum crash, erasing all accumulated wealth.
              
            • Strategy B (Quality-Gated 12-1M Momentum):
              • Screens for top 20% momentum ONLY among stocks with positive FCF and ROIC > 15%.
              • Enforces a 200 DMA trend filter and quarterly rebalancing.
              • Year 1: Gains +52% by participating in strong corporate leaders.
              • Year 2: Limits drawdown to -14% by rotating into defensive quality winners, achieving 3x higher 10-year terminal net worth.
        """.trimIndent(),
        realStockExample = """
            NVIDIA (NVDA) vs Peloton (PTON) — Momentum Sustained vs Momentum Collapsed:
            
            • NVIDIA (2023–2024):
              • NVDA held Decile 10 12-1M momentum for consecutive quarters.
              • Crucially, price momentum was supported by explosive 200%+ EPS growth and massive FCF margin expansion, preventing a valuation collapse.
              • Trading consistently above 50 DMA and 200 DMA confirmed technical trend health.
              
            • Peloton (2020–2021):
              • PTON surged over +400% during 2020 lockdowns, reaching top-decile momentum.
              • However, its fundamental quality was abysmal: negative operating cash flows and extreme inventory accumulation.
              • When pandemic demand normalized, PTON suffered a brutal 95% momentum crash as multiple compression decimated unanchored capital.
        """.trimIndent(),
        commonMistakes = listOf(
            "Chasing momentum at extreme 200DMA extensions (>50% above)" to "Buying right before an inevitable mean-reversion pullback.",
            "Failing to cut losses when momentum breaks down" to "Holding an intermediate momentum winner that turns into a multi-year laggard.",
            "Ignoring momentum crashes during market bottoms" to "Shorting low-momentum laggards at the exact bottom of a cyclical recession.",
            "Using 1-month momentum instead of 12-1M" to "Suffering from 1-month liquidity reversal and excessive trading churn."
        ),
        goldenRules = listOf(
            "The 12-1 Month Standard: Always exclude the most recent month to eliminate short-term microstructure noise.",
            "The Trend Confirmation Gate: Ensure stock price trades above its upward-sloping 50-day and 200-day moving averages.",
            "The Quality Filter: Never buy high momentum stocks that burn cash or feature negative Altman Z-scores.",
            "The Crash Defense Rule: Trim high-beta momentum exposure when market regime signals shift into early-cycle recovery.",
            "The Rebalancing Discipline: Systematically rotate momentum winners quarterly rather than holding emotionally."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 19,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "Value Factors",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "Which quantitative value metrics produce the most reliable statistical outperformance historically?",
        primaryFormula = "Composite Value Rank = Rank(FCF Yield) + Rank(EV/EBITDA) + Rank(Earnings Yield)",
        researcherRule = "Accounting Price-to-Book has lost statistical efficacy in modern intangible economies. Institutional value models rely on cash flow yields and enterprise value multiples.",
        whatDataDoesNotProve = "A low valuation multiple does not prove a company is undervalued; it often reflects permanent business decay or looming bankruptcy.",
        assetInfographicPath = "learnings/lesson_19_infographic.jpg",
        keyCheckpoints = listOf(
            "The Empirical Cash Yield Hierarchy (FCF Yield > EV/EBITDA > E/P)",
            "Why Accounting Book Value (P/B) Decayed in Modern Markets",
            "Value Trap Defense: Solvency, Coverage, and Accruals",
            "Multi-Metric Value Composite Scoring"
        ),
        fullNotesOverview = """
            Value investing in quantitative finance is the systematic harvesting of the 'Value Premium' (the historical outperformance of cheap assets relative to expensive assets).
            
            While classical Graham & Dodd value focused on Price-to-Book (P/B), modern financial research shows that P/B has degraded dramatically over the past two decades. In today's digital, asset-light economy, companies invest billions in intangible capital (R&D, brand equity, software algorithms, and human talent) that accounting standards immediately expense on the income statement rather than capitalizing on the balance sheet. Consequently, high-ROIC technology firms exhibit artificially depressed book value.
            
            Modern institutional quantitative value models discard P/B in favor of cash-generative valuation metrics:
            1. Free Cash Flow Yield (FCF / EV): The gold standard of valuation yields, unpolluted by non-cash accounting accruals.
            2. EV / EBITDA: Capital-structure neutral metric that accounts for corporate debt obligations.
            3. Earnings Yield (E / P): Net income divided by market cap, allowing apples-to-apples comparison against risk-free treasury yields.
            
            By building a multi-metric composite and screening out balance sheet insolvency risks (Altman Z < 1.8), quant investors capture value upside while systematically avoiding value traps.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "FCF Yield" to "Free Cash Flow divided by Enterprise Value (or Market Cap); measures pure cold cash yield generated per dollar of corporate enterprise.",
            "EV / EBITDA" to "Capital-structure neutral valuation multiple comparing operational cash flow generation to total enterprise value.",
            "Earnings Yield" to "The reciprocal of the P/E ratio (E / P); represents the percentage of corporate profit earned per share price.",
            "Value Trap" to "A company that appears optically cheap on historical multiples but suffers structural cash flow erosion toward distress.",
            "Intangible Asset Shift" to "The macroeconomic transition where corporate value derives from software, data, and IP that GAAP expensing excludes from balance sheet book value."
        ),
        compoundingRealityCheck = """
            Buying a stock at 6x P/E feels safe, but if its earnings contract by 25% per year, the multiple will expand to 15x while the stock drops 60%.
            Conversely, buying a high-ROIC cash flow compounder at 18x P/E with a 6% FCF yield that reinvests capital at 20% ROIC generates immense wealth.
            True value is never defined by a low multiple alone; value is the discounted stream of all future free cash flows. 
            If cash flow is declining, no multiple is cheap enough!
        """.trimIndent(),
        fictionalExample = """
            The Value Trap vs The Cash Flow Bargain:
            
            • Company A (Legacy Print Media Inc.):
              • P/E: 5.8x | P/B: 0.6x (Looks extremely cheap).
              • Fundamentals: Revenue falling 12% annually, FCF is negative \${'$'}40M, Altman Z is 1.3 (distress).
              • Result: 3 years later, the company files Chapter 11 bankruptcy. Total loss of shareholder capital.
              
            • Company B (Specialty Logistics Corp.):
              • P/E: 14.5x | EV/EBITDA: 6.8x | FCF Yield: 8.2%.
              • Fundamentals: Stable 5% revenue growth, zero net debt, Altman Z of 4.2.
              • Result: High cash generation funds substantial share buybacks; stock re-rates to 18x P/E, delivering a +94% total return.
        """.trimIndent(),
        realStockExample = """
            Exxon Mobil (XOM) vs Intel (INTC) — Cash Flow Turnaround vs Value Trap:
            
            • Exxon Mobil (2020–2022):
              • In late 2020, XOM traded at historical lows with an 8%+ dividend yield and severe market skepticism.
              • However, disciplined capex reductions created massive structural FCF leverage. 
              • As oil normalized, FCF surged over \${'$'}35 Billion, driving an extraordinary 250% total return for disciplined value investors.
              
            • Intel (2021–2024):
              • INTC consistently screened as a 'cheap' tech stock at 10x-12x trailing P/E and ~1.0x book value.
              • Yet beneath the surface, aggressive foundry capex wiped out Free Cash Flow, turning FCF negative by billions.
              • The stock continuously underperformed while the broader semiconductor index reached all-time highs.
        """.trimIndent(),
        commonMistakes = listOf(
            "Using Price-to-Book (P/B) to screen asset-light tech stocks" to "Missing massive intangible value because R&D was expensed.",
            "Assuming cheap P/E equals safety" to "Buying cyclical peaks or dying legacy businesses with unsustainable trailing earnings.",
            "Ignoring debt in value screens" to "Buying low equity multiples when the enterprise is drowning in unserviceable debt.",
            "Confusing value investing with buying falling knives" to "Allocating capital before cash flow stabilization occurs."
        ),
        goldenRules = listOf(
            "The Cash Yield Hierarchy: Prioritize FCF Yield and EV/EBITDA over accounting P/E and Book Value.",
            "The Solvency Shield: Reject any value candidate with Altman Z-Score below 1.8 or Net Debt / EBITDA above 3.5x.",
            "The Intangible Economy Rule: Never evaluate software, pharma, or digital platforms using accounting Price-to-Book.",
            "The Multi-Metric Value Composite: Blend FCF yield, EV/EBITDA, and earnings yield to prevent single-metric distortions.",
            "The Catalyst Condition: Demand evidence of operating stabilization before entering deep-value contrarian positions."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 20,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "Quality Factors",
        icon = Icons.Rounded.Security,
        coreQuestion = "How do we build a quantitative score that isolates durable competitive moats and protects against bear market drawdowns?",
        primaryFormula = "Piotroski F-Score (9 Pts) + Novy-Marx Gross Profitability (Gross Profit / Total Assets)",
        researcherRule = "Quality factors act as structural portfolio defense during bear markets and compound during recoveries. Demand quality as a non-negotiable filter.",
        whatDataDoesNotProve = "High quality does not mean immune to valuation risk; paying 100x earnings for quality guarantees disappointing long-term annualized returns.",
        assetInfographicPath = "learnings/lesson_20_infographic.jpg",
        keyCheckpoints = listOf(
            "The 9-Point Piotroski F-Score Framework",
            "Novy-Marx Gross Profitability Factor (Gross Profit / Total Assets)",
            "Sloan Accrual Quality & Cash Conversion",
            "Drawdown Defense & Quality Moat Shield"
        ),
        fullNotesOverview = """
            Quality is the premier structural anchor of quantitative portfolio construction. 
            While Value offers multiple expansion and Momentum provides timing velocity, Quality is what prevents catastrophic capital loss during economic recessions and bear markets.
            
            Two academic breakthroughs revolutionized quantitative quality investing:
            
            1. Joseph Piotroski's 9-Point F-Score (Stanford, 2000):
               A binary scoring model that audits three critical corporate dimensions:
               • Profitability (4 pts): Positive ROA, positive Operating Cash Flow (CFO), ROA year-over-year improvement, and accrual check (CFO > Net Income).
               • Leverage & Liquidity (3 pts): Reduction in long-term debt, expansion in Current Ratio, and absence of share dilution.
               • Operating Efficiency (2 pts): Expansion in Gross Margin and improvement in Asset Turnover.
               Firms scoring 8 or 9 out of 9 historically outperform low-scoring firms (0 to 3) by over 7.5% annualized with vastly lower default rates.
            
            2. Robert Novy-Marx's Gross Profitability (2013):
               Novy-Marx proved that Gross Profit divided by Total Assets (Gross Profit / Assets) has roughly the same power as Book-to-Market in predicting future cross-sectional stock returns. Because gross profit sits cleanly at the top of the income statement, it isolates true pricing power and economic moat before corporate overhead or accounting adjustments.
            
            In institutional execution, portfolios tilted toward high Piotroski F-Scores and robust Gross Profitability capture substantial upside while suffering significantly smaller drawdowns during market panics.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Piotroski F-Score" to "A 9-point financial health scorecard evaluating profitability, leverage/liquidity, and operating efficiency.",
            "Novy-Marx Gross Profitability" to "Gross Profit divided by Total Assets; isolates pure operating pricing power before capital allocation choices.",
            "Accrual Quality" to "The degree to which reported accounting net income is confirmed by actual operating cash flow (CFO > NI).",
            "Quality Moat Shield" to "The structural tendency of high-quality, high-ROIC balance sheets to experience significantly lower drawdowns in market corrections.",
            "Financial Leverage Trend" to "Tracking whether a company is actively de-leveraging or using debt to mask slowing organic growth."
        ),
        compoundingRealityCheck = """
            During the final stages of speculative bull markets, junk companies and unprofitable concept stocks often surge the highest.
            Novice investors abandon quality and chase low-grade momentum out of FOMO.
            However, when liquidity drains and recessions strike, speculative junk loses 80% to 100% of its market value.
            Fortress-quality businesses with strong balance sheets, high gross margins, and positive cash flows not only survive bear markets, but aggressively acquire distressed competitors at discounted prices, accelerating their long-term compounding.
        """.trimIndent(),
        fictionalExample = """
            Piotroski Audit: Elite Quality vs Fragile Speculator:
            
            • Firm A (CloudFortress Software):
              • Profitability: ROA +16%, CFO \${'$'}450M, ROA up +2.1%, CFO exceeds NI by \${'$'}80M (4/4 Points).
              • Leverage/Liquidity: Long-term debt reduced by 15%, Current Ratio 2.2, Share count reduced by 2% via buybacks (3/3 Points).
              • Efficiency: Gross Margin widened to 74%, Asset Turnover stable (2/2 Points).
              • Piotroski F-Score: 9 / 9 | Novy-Marx: 44%.
              • Bear Market Drawdown: -14% vs market -34%.
              
            • Firm B (HypeMobility Inc.):
              • Profitability: Negative ROA, CFO is -\${'$'}85M, Net Income looks positive due to asset sale (0/4 Points).
              • Leverage/Liquidity: Debt increased 40%, Current Ratio deteriorated to 1.05, Shares diluted +18% (0/3 Points).
              • Efficiency: Gross margins shrinking under price cuts (0/2 Points).
              • Piotroski F-Score: 0 / 9 | Novy-Marx: 9%.
              • Bear Market Drawdown: -88%, followed by severe restructuring.
        """.trimIndent(),
        realStockExample = """
            Microsoft (MSFT) — The Benchmark of Quantitative Quality:
            
            • Over the past two decades, Microsoft has maintained an average Piotroski F-Score of 8 to 9 out of 9.
            • Gross Profit / Total Assets consistently exceeds 40%, placing MSFT in the top 5th percentile of the S&P 500.
            • Free Cash Flow conversion continuously exceeds 100% of Net Income, proving zero accounting accrual manipulation.
            • During the 2008 Financial Crisis and the 2022 rate-hiking cycle, this institutional quality profile enabled MSFT to maintain AAA credit rating and rebound to new highs while speculative peers permanently collapsed.
        """.trimIndent(),
        commonMistakes = listOf(
            "Overpaying for quality" to "Buying great businesses at 80x P/E and earning zero return over 10 years.",
            "Confusing historical reputation with current quantitative quality" to "Assuming a blue-chip dividend aristocrat is high quality while its Piotroski F-Score drops to 3.",
            "Ignoring share dilution in quality scores" to "Overlooking 5%+ annual dilution from executive stock-based compensation.",
            "Relying purely on net margin instead of Gross Profitability" to "Missing companies with artificial net income boosted by one-off asset sales."
        ),
        goldenRules = listOf(
            "The Piotroski Baseline: Require an F-Score of at least 6/9 for core long-term portfolio candidates.",
            "The Cash Confirmation Test: Immediately disqualify any company whose Operating Cash Flow is persistently below Net Income.",
            "The Novy-Marx Profitability Screen: Favor companies with Gross Profit / Total Assets exceeding 30%.",
            "The Anti-Dilution Mandate: Demand that share count remains stable or decreases via accretive share repurchases.",
            "The Valuation-Quality Balance: Combine elite quality with reasonable valuation yields to achieve optimal Sharpe ratios."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 21,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "Growth Factors",
        icon = Icons.AutoMirrored.Rounded.ShowChart,
        coreQuestion = "How do we statistically measure durable growth versus transitory hype?",
        primaryFormula = "Growth Score = Rank(3Y Rev CAGR) + Rank(3Y EPS CAGR) + Rank(EPS Revision Delta)",
        researcherRule = "Growth creates authentic shareholder value only when returns on incremental capital exceed the cost of capital (ROIC > WACC). Internal financing capacity is governed by the Sustainable Growth Rate.",
        whatDataDoesNotProve = "Rapid top-line revenue growth without expanding margins or high ROIC often destroys shareholder capital through continuous dilution.",
        assetInfographicPath = "learnings/lesson_21_infographic.jpg",
        keyCheckpoints = listOf(
            "Historical 3Y/5Y Revenue, EPS, and FCF CAGRs",
            "The Sustainable Growth Rate: SGR = ROE × (1 - Payout Ratio)",
            "Growth Acceleration vs Deceleration Inflection Signals",
            "Wall Street Consensus Earnings Estimate Revisions"
        ),
        fullNotesOverview = """
            Quantitative growth investing evaluates the durability, quality, and internal financing capacity of a firm's expansion engine.
            
            Unlike naive retail growth investing which chases unanchored top-line revenue surges, institutional quantitative growth requires that growth generate positive Net Present Value (NPV). Growth is purely a volume multiplier: if a company's Return on Invested Capital (ROIC) exceeds its Cost of Capital (WACC), growth creates immense value. But if ROIC is below WACC, faster growth accelerates value destruction.
            
            Two mathematical pillars govern institutional growth evaluation:
            1. The Sustainable Growth Rate (SGR):
               SGR = ROE × (1 - Dividend Payout Ratio)
               This represents the maximum rate at which a company can grow revenues using organically retained earnings without taking on unserviceable debt or issuing dilutive equity.
            2. The Second Derivative of Growth (Growth Acceleration):
               Valuation multiples are most sensitive to changes in the rate of growth. When a company's revenue growth accelerates (e.g. from 15% to 25% YoY), its valuation multiple expands dramatically. Conversely, growth deceleration triggers rapid multiple compression.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Sustainable Growth Rate" to "The maximum rate of revenue and earnings growth a firm can sustain without issuing new equity or taking on additional debt leverage.",
            "Growth Acceleration" to "An expanding year-over-year growth rate, which historically triggers substantial institutional multiple expansion.",
            "Earnings Revision Delta" to "The percentage change in consensus Wall Street EPS estimates over 30 to 90 days; a powerful intermediate price driver.",
            "Quality-Gated Growth" to "The quantitative rule that growth only creates shareholder value if ROIC exceeds the weighted average cost of capital.",
            "Capex Reinvestment Rate" to "The proportion of cash flow reinvested into organic property, plant, equipment, and R&D."
        ),
        compoundingRealityCheck = """
            A speculative company growing revenue at 60% that burns cash and dilutes shareholders by 20% annually destroys capital.
            Conversely, a disciplined compounder growing revenues at 16% with a 30% ROE and 25% ROIC creates extraordinary generational wealth.
            Always demand that growth is measured per share (EPS CAGR and FCF per share) rather than gross corporate top line.
        """.trimIndent(),
        fictionalExample = """
            Unprofitable Hyper-Growth vs Sustainable Compounder:
            
            • Firm A (BioCloud SaaS):
              • Revenue Growth: 55% YoY | FCF: -\${'$'}60M | ROIC: -14% | Share Dilution: +18% annually.
              • SGR: -20% (Completely dependent on capital markets for survival).
              • When tech liquidity froze, the stock collapsed 85% as it had to issue distressed convertible debt.
              
            • Firm B (FinSecure Systems):
              • Revenue Growth: 22% YoY | FCF Margin: 28% | ROIC: 32% | Share Count: Declining -2% via buybacks.
              • SGR: 26% (Fully self-funding organic expansion).
              • Stock compounded at 24% annualized over 5 years with minimal volatility.
        """.trimIndent(),
        realStockExample = """
            NVIDIA (NVDA) vs Peloton (PTON) — Sustainable Growth vs Destructive Hype:
            
            • NVIDIA (2023–2024):
              • Exploded revenues by over 200% while simultaneously expanding Gross Margins from 65% to 75%.
              • ROIC exceeded 50%, proving that every dollar reinvested into GPUs generated phenomenal economic profit.
              
            • Peloton (2020–2021):
              • Doubled sales during lockdowns, but generated negative operating margins and negative ROIC.
              • When growth decelerated in 2021, massive fixed production overhead destroyed enterprise profitability, resulting in a 95% drawdown.
        """.trimIndent(),
        commonMistakes = listOf(
            "Extrapolating short-term growth linearly into infinity" to "Ignoring competitive mean reversion and market saturation.",
            "Valuing revenue growth without checking operating cash conversion" to "Buying revenue treadmills that never translate sales into free cash flow.",
            "Ignoring growth deceleration warnings" to "Holding high-multiple growth stocks through initial quarterly slowdowns.",
            "Overlooking share dilution in per-share CAGR" to "Celebrating +20% net income growth while share count increased by +25%."
        ),
        goldenRules = listOf(
            "The ROIC Hurdle: Demand that ROIC exceeds WACC by at least 5% before paying a growth premium.",
            "The SGR Benchmark: Ensure organic growth rates remain within internal sustainable financing limits.",
            "The Revision Alignment Rule: Prioritize companies with positive 30-day and 90-day consensus earnings estimate upward revisions.",
            "The Per-Share Discipline: Measure growth exclusively on an EPS and FCF-per-share basis to account for equity dilution.",
            "The Margin Defense Rule: Verify that operating margins are expanding or stable alongside top-line revenue expansion."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 22,
        stageNumber = 4,
        stageTitle = "Stage 4 — Quantitative Investing",
        title = "Factor Combinations and Multi-Factor Models",
        icon = Icons.Rounded.School,
        coreQuestion = "Why does combining non-correlated factors (Quality + Value + Momentum) create superior risk-adjusted returns?",
        primaryFormula = "Composite Alpha = 0.35 × Quality + 0.30 × Value + 0.20 × Momentum + 0.15 × Solvency",
        researcherRule = "Never rely on a single factor. Multi-factor integration eliminates multi-year factor winter drawdowns and optimizes the portfolio Sharpe ratio.",
        whatDataDoesNotProve = "A multi-factor model is probabilistic, not prophetic; it provides a structural mathematical edge over large cross-sectional samples, not a guarantee for any single quarter.",
        assetInfographicPath = "learnings/lesson_22_infographic.jpg",
        keyCheckpoints = listOf(
            "Orthogonal Factor Integration (Quality, Value, Momentum, Solvency)",
            "Eliminating Single-Factor 'Winter' Drawdowns",
            "Factor Interaction Synergy: GARP and Trend-Reversal Value",
            "Constructing Balanced Multi-Factor Composite Scores"
        ),
        fullNotesOverview = """
            Lesson 22 serves as the grand capstone of Stage 4: Quantitative Investing.
            
            While individual factors—Value, Quality, Momentum, and Low Volatility—generate proven empirical premiums over 30-year horizons, each individual factor suffers from multi-year cyclical drawdowns ('factor winters').
            For example:
            • Value suffered a severe 10-year winter between 2010 and 2020.
            • Momentum experiences violent -40% crashes during market regime bottom reversals.
            • Quality lags behind speculative beta during the final euphoric blow-off stages of bull markets.
            
            By blending orthogonal (lowly-correlated) factors into a single composite rank, quantitative asset managers achieve mathematical portfolio smoothing. 
            When Value is lagging, Momentum and Quality carry portfolio performance. When Momentum crashes, Value and Quality protect capital.
            
            Key multi-factor synergies include:
            • GARP (Growth at a Reasonable Price): Quality + Value filters eliminate expensive hype while capturing compounders.
            • Trend-Reversal Value: Value + Momentum filters eliminate value traps by waiting for upward price confirmation before buying cheap assets.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Multi-Factor Synthesis" to "Combining multiple distinct quantitative style factors into a single composite rank to maximize portfolio Sharpe ratio.",
            "Factor Winter" to "A multi-year cyclical period where a specific standalone factor underperforms the broader market index.",
            "Orthogonal Factors" to "Style factors that exhibit low or negative statistical correlation with each other (e.g. Value and Momentum).",
            "GARP (Growth at a Reasonable Price)" to "A multi-factor synergy combining Quality and Growth factor filters with sensible Value multiple thresholds.",
            "Factor Dispersion" to "The standard deviation of an asset's ranks across individual factor pillars, measuring balance vs extreme tilt."
        ),
        compoundingRealityCheck = """
            Investors who bet everything on a single factor almost always surrender at the worst possible moment—the exact trough of its factor winter.
            A balanced multi-factor approach delivers steady risk-adjusted alpha, giving investors the psychological endurance to stay invested across full multi-decade market cycles.
        """.trimIndent(),
        fictionalExample = """
            Single Factor Winter vs Multi-Factor Resilience:
            
            • Fund 1 (Pure Momentum Chaser):
              • Year 1: +82% during speculative tech mania.
              • Year 2: -58% during market regime rotation into value.
              • 2-Year Wealth: \${'$'}100 becomes \${'$'}182, then collapses to \${'$'}76 (Loss of capital).
              
            • Fund 2 (Multi-Factor Blend: 35% Quality, 30% Value, 20% Momentum, 15% Solvency):
              • Year 1: +26% (participated in quality momentum leaders).
              • Year 2: +14% (value and quality holdings protected capital).
              • 2-Year Wealth: \${'$'}100 becomes \${'$'}126, then grows to \${'$'}143 (+43% net gain with zero panic).
        """.trimIndent(),
        realStockExample = """
            Berkshire Hathaway (BRK.B) — The 50-Year Multi-Factor Masterpiece:
            
            • Warren Buffett's outperformance has been academically decomposed into three core style factors:
              1. Quality Factor: High ROIC, conservative accounting accruals, and pristine balance sheets.
              2. Value Factor: Disciplined entry multiples and high cash flow yields.
              3. Low Volatility Factor: Low beta (< 0.8), avoiding market lottery tickets.
            • By refusing to rely on any single factor and systematically rebalancing across five decades, Berkshire generated twice the Sharpe ratio of the S&P 500.
        """.trimIndent(),
        commonMistakes = listOf(
            "Overfitting factor weights to match past backtests" to "Creating complex fragile models that fail in live out-of-sample trading.",
            "Chasing the single highest performing factor of the prior year" to "Buying factor cyclical tops right before multi-year factor rotations.",
            "Neglecting factor correlation" to "Combining three metrics that are 90% correlated, failing to achieve true diversification.",
            "Abandoning sound factors during drawdowns" to "Capitulating at the exact cyclical trough of a factor's performance."
        ),
        goldenRules = listOf(
            "The Orthogonal Principle: Blend factors with pairwise correlations below 0.30 (e.g. Quality, Value, Momentum).",
            "The Simple Weighting Mandate: Avoid complex non-linear weights; use robust transparent weights (e.g. 35/30/20/15).",
            "The Rebalancing Discipline: Rebalance multi-factor portfolios quarterly to harvest factor momentum and value re-ratings.",
            "The Quality Baseline: Always enforce Quality as a primary gate before applying Value or Momentum tilts.",
            "The Stage 4 Capstone Rule: Recognize that portfolio returns are governed by systematic factor exposures rather than stock stories."
        )
    ),

    // ------------------------------------------------------------------------
    // STAGE 5: DATA AND PATTERN DISCOVERY (Lessons 23 - 27)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 23,
        stageNumber = 5,
        stageTitle = "Stage 5 — Data & Pattern Discovery",
        title = "Building the Stock Dataset",
        icon = Icons.Rounded.Analytics,
        coreQuestion = "How do quantitative researchers construct clean, survivorship-bias-free datasets for cross-sectional analysis?",
        primaryFormula = "Dataset Matrix: N_tickers × T_timestamps × K_features",
        researcherRule = "Garbage in, garbage out. Cleaning corporate action adjustments, splits, delistings, and point-in-time filing lags is 80% of institutional quantitative research.",
        whatDataDoesNotProve = "A massive dataset does not guarantee predictive alpha if look-ahead bias or survivorship bias contaminates the feature matrix.",
        assetInfographicPath = "learnings/lesson_23_infographic.jpg",
        keyCheckpoints = listOf(
            "The 3D Financial Tensor: Tickers (N) × Time (T) × Features (K)",
            "Point-in-Time Fundamentals & Eliminating Look-Ahead Bias",
            "Corporate Action Adjustments (Splits, Dividends, Spinoffs)",
            "Survivorship Bias Elimination: Accounting for Delisted Equities"
        ),
        fullNotesOverview = """
            Lesson 23 launches Stage 5: Data & Pattern Discovery, transitioning from theoretical finance to institutional data science.
            
            Financial data is fundamentally different from typical machine learning data. In image recognition or natural language processing, timestamps rarely invalidate the training process. In quantitative finance, data hygiene errors are fatal to capital.
            
            The three fatal data traps in quantitative research:
            1. Look-Ahead Bias:
               Using information in a backtest before it was publicly available. A company's fiscal Q1 quarter may end on March 31, but its official SEC 10-Q filing date is May 10. A model that trades on March 31 using Q1 numbers assumes psychic foresight and generates imaginary backtest alpha. Point-in-Time (PIT) datasets timestamp data strictly on the public release date.
            2. Survivorship Bias:
               Backtesting only on companies that currently exist in today's S&P 500. This systematically deletes all companies that went bankrupt, were liquidated, or were acquired at distressed valuations (e.g. Enron, Lehman Brothers, WorldCom), inflating backtested CAGR by 300+ basis points.
            3. Corporate Action Discontinuity:
               Failing to backward-adjust stock prices for stock splits, stock dividends, and spinoffs, creating false -90% price drops that trigger erroneous algorithmic orders.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Point-in-Time (PIT) Data" to "Historical data recorded strictly with the timestamp at which it became publicly available, preventing look-ahead bias.",
            "Look-Ahead Bias" to "The fatal backtesting error of using future information that was not yet known to market participants at that moment.",
            "Survivorship Bias" to "The systematic error of only testing stocks that survived to the present day, ignoring failed and bankrupt companies.",
            "Split Factor Adjustment" to "Mathematically adjusting historical share prices and share counts to ensure return continuity across stock split events.",
            "Cross-Sectional Matrix" to "A 2D or 3D data structure organizing multiple assets across uniform historical time intervals."
        ),
        compoundingRealityCheck = """
            Retail algorithmic traders routinely show backtests promising 50% annualized returns.
            When deployed with real capital, these strategies immediately lose money because their backtest was contaminated with look-ahead bias and survivorship bias.
            Data hygiene is the ultimate competitive moat in quantitative finance.
        """.trimIndent(),
        fictionalExample = """
            Look-Ahead Bias vs Point-in-Time Reality:
            
            • Strategy A (Flawed Retail Backtest):
              • Traded on March 31 using Q1 earnings reported on May 8.
              • Backtested CAGR: +34.2% | Max Drawdown: -11%.
              • Live Execution: Loses 18% in year 1 because the market priced in earnings weeks before the algorithm could actually trade them.
              
            • Strategy B (Institutional Point-in-Time Engine):
              • Strictly restricts trading signals until SEC filing timestamp + 24 hours.
              • Includes 280 delisted bankrupt equities in the historical testing universe.
              • Backtested CAGR: +15.8% | Max Drawdown: -22%.
              • Live Execution: Replicates backtest with 96% fidelity, delivering dependable compounding.
        """.trimIndent(),
        realStockExample = """
            The Enron and Lehman Brothers Survivorship Trap:
            
            • If you backtest a value strategy from 2000 to 2010 using only current surviving stocks, Enron (2001) and Lehman Brothers (2008) are completely omitted.
            • Your value screen looks brilliant because it never registered that those low-multiple stocks went to zero!
            • Adding dead and delisted equities back into the database provides the true, sobering reality of credit risk.
        """.trimIndent(),
        commonMistakes = listOf(
            "Using calendar quarter-end dates instead of SEC filing dates" to "Trading on earnings numbers weeks before the public market received them.",
            "Testing on surviving universe members only" to "Artificially inflating strategy returns by ignoring bankruptcies.",
            "Ignoring cash dividend adjustments in total return calculations" to "Understating returns of dividend-paying value stocks by 2-4% annually.",
            "Failing to winsorize data entry typos and anomalies" to "Letting single corrupted data points distort portfolio factor regressions."
        ),
        goldenRules = listOf(
            "The Point-in-Time Rule: Always align financial statements with their official SEC public filing timestamp (lag date).",
            "The Survivorship Mandate: Include dead, merged, and bankrupt companies in all historical quantitative testing universes.",
            "The Corporate Action Continuity Test: Verify that all price series are adjusted for stock splits and cash dividends.",
            "The Missing Data Protocol: Explicitly document and handle missing entries rather than silently forward-filling indefinitely.",
            "The Cross-Sectional Sanity Check: Inspect data distributions before feeding inputs into ranking algorithms."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 24,
        stageNumber = 5,
        stageTitle = "Stage 5 — Data & Pattern Discovery",
        title = "Exploratory Data Analysis (EDA)",
        icon = Icons.Rounded.BarChart,
        coreQuestion = "What do statistical distributions, outliers, and skewness tell us about market variables?",
        primaryFormula = "Skewness = E[(X - mu)^3] / sigma^3  |  Kurtosis = E[(X - mu)^4] / sigma^4",
        researcherRule = "Financial returns are not normally distributed. They exhibit fat tails and negative skewness. Never assume Gaussian bell curves in risk models.",
        whatDataDoesNotProve = "The mathematical mean of a heavily skewed distribution does not represent the typical stock's return.",
        assetInfographicPath = "learnings/lesson_24_infographic.jpg",
        keyCheckpoints = listOf(
            "The Fallacy of Gaussian Normality in Financial Markets",
            "Skewness and Excess Kurtosis (Fat-Tailed Leptokurtic Distributions)",
            "Mean vs Median Divergence in Equity Cross-Sections",
            "Outlier Treatment: Winsorization vs Trimming"
        ),
        fullNotesOverview = """
            Exploratory Data Analysis (EDA) is the quantitative practice of understanding the true statistical topology of financial data before constructing models.
            
            Classical finance textbooks (CAPM, Modern Portfolio Theory) rely on the convenient mathematical assumption that stock returns follow a Gaussian Normal Distribution (a symmetric bell curve).
            In reality, empirical equity returns violently violate normality:
            1. Excess Kurtosis (Fat Tails):
               Financial distributions are leptokurtic. Extreme events (market crashes, circuit-breaker limit-down moves) happen orders of magnitude more often than normal distributions predict. Under a normal curve, a 5-sigma daily move should happen once every 7,000 years; in real US markets, it occurs roughly once every 5 to 7 years.
            2. Negative Skewness:
               Stock returns exhibit a long negative left tail. Equities grind upward slowly (positive drift) and crash downward with ferocious speed.
            3. Extreme Outliers & The Mean vs Median Gap:
               Financial variables like Market Cap, Net Income, and P/E ratios are power-law distributed. Mega-caps like Apple and Microsoft pull the universe arithmetic mean far above the median. Quantitative researchers prioritize median and interquartile ranges (IQR) and use Winsorization (clamping outliers at 1st and 99th percentiles) to prevent distorted models.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Fat Tails (Leptokurtosis)" to "The statistical reality where extreme outlier events occur with vastly higher probability than normal distributions predict.",
            "Negative Skewness" to "A distribution shape characterized by frequent small gains and rare, violent, catastrophic drawdowns.",
            "Winsorization" to "A robust data transformation method that clamps extreme outliers to specific percentile cutoffs without deleting observations.",
            "Interquartile Range (IQR)" to "The spread between the 25th (Q1) and 75th (Q3) percentiles; measures core dispersion free from outlier distortion.",
            "Mean-Median Divergence" to "The mathematical gap between average and median caused by mega-cap outliers pulling the arithmetic mean."
        ),
        compoundingRealityCheck = """
            In 1998, Long-Term Capital Management (LTCM)—run by Nobel laureates—collapsed and required a Federal Reserve bailout because their risk models assumed market returns were normally distributed.
            They calculated that a simultaneous crash in Russia and Wall Street was a '10-sigma' impossibility. When fat-tailed reality struck, they lost \${'$'}4.6 Billion in weeks.
            Never build risk models that assume financial normality!
        """.trimIndent(),
        fictionalExample = """
            Gaussian Assumptions vs Fat-Tailed Survival:
            
            • Fund A (Gaussian VaR Model):
              • Sets stop-losses assuming maximum daily drawdown cannot exceed -4.5% (a 3-sigma event).
              • Employs 8x leverage to boost returns.
              • March 2020 Covid Crash: Market drops -12% in a single day. Fund A faces immediate liquidation and total ruin.
              
            • Fund B (Fat-Tailed Leptokurtic Model):
              • Assumes excess kurtosis > 6 and models 15% single-day shock scenarios.
              • Caps leverage at 1.5x and maintains a 10% cash liquidity reserve.
              • March 2020: Absorbs the crash smoothly, deploys cash into generational bargains, and finishes the year up +38%.
        """.trimIndent(),
        realStockExample = """
            Black Monday (October 19, 1987) — The Ultimate Normality Violation:
            
            • The Dow Jones Industrial Average plunged -22.6% in a single trading session.
            • Under a standard normal distribution, a 20-sigma event has a probability of 1 in 10^88 (older than the age of the universe).
            • Yet it happened in a single afternoon. Financial markets operate under power-law fat-tail mathematics.
        """.trimIndent(),
        commonMistakes = listOf(
            "Assuming financial variables follow a bell curve" to "Underestimating extreme downside risk by orders of magnitude.",
            "Relying on the sample mean in skewed datasets" to "Drawing misleading conclusions about the 'average' stock multiple.",
            "Deleting outliers entirely from datasets" to "Discarding real market stress events and corrupting historical reality.",
            "Confusing volatility with true risk" to "Treating upside volatility the same as catastrophic downside fat-tail drawdowns."
        ),
        goldenRules = listOf(
            "The Fat-Tail Assumption: Always assume financial return series have excess kurtosis and prepare for multi-sigma events.",
            "The Median Priority: Inspect medians and percentiles alongside arithmetic means for all financial metrics.",
            "The Winsorization Protocol: Clamp cross-sectional factor outliers at the 1st and 99th percentiles before computing z-scores.",
            "The Box-Plot Standard: Visualize interquartile ranges and outlier whiskers to detect distributional skew.",
            "The Tail-Risk Defense Rule: Size portfolio positions so that a 5-sigma daily event will not cause insolvency."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 25,
        stageNumber = 5,
        stageTitle = "Stage 5 — Data & Pattern Discovery",
        title = "Correlation Analysis and Non-Causation",
        icon = Icons.Rounded.Analytics,
        coreQuestion = "Does statistical correlation between a fundamental metric and future returns prove economic causation?",
        primaryFormula = "Pearson r = Cov(X, Y) / (sigma_X × sigma_Y)  |  Spearman Rank Correlation",
        researcherRule = "Correlation does NOT imply causation. Always demand an economic mechanism explaining why an observed statistical relationship exists.",
        whatDataDoesNotProve = "A high historical correlation does not prove a future profitable relationship if it was discovered through unguided data-snooping.",
        assetInfographicPath = "learnings/lesson_25_infographic.jpg",
        keyCheckpoints = listOf(
            "Pearson (Linear) vs Spearman (Rank) Correlation in Finance",
            "The Information Coefficient (IC): Quantifying Factor Predictive Power",
            "Spurious Correlation Traps & Multiple Hypothesis Testing",
            "Cross-Factor Correlation Matrices & Signal-to-Noise Ratio"
        ),
        fullNotesOverview = """
            Correlation analysis is the primary diagnostic lens through which quantitative researchers test whether a fundamental metric has genuine predictive power.
            
            However, two critical hazards exist:
            1. Pearson vs Spearman Rank Correlation:
               Pearson linear correlation measures whether two variables lie on a straight line. Financial relationships are rarely linear; a company with 20% ROIC is much better than 10% ROIC, but a company with 80% ROIC is not 4x better. 
               Quantitative researchers use non-parametric Spearman rank correlation, which tests whether higher factor ranks monotonically produce higher forward return ranks.
            2. The Information Coefficient (IC):
               In institutional quantitative portfolio management, factor efficacy is tracked via the Information Coefficient (IC)—the cross-sectional Spearman correlation between factor scores today and forward 1-month returns:
               IC = Spearman_Corr(Rank(Factor_t), Rank(Return_t+1))
               According to Grinold's Fundamental Law of Active Management:
               Information Ratio = IC × sqrt(Breadth)
               An average monthly IC of just +0.03 to +0.05 across 1,000 equities generates massive institutional alpha!
            3. Spurious Correlation & Data Mining (p-hacking):
               If you test 100 random noise variables against forward returns, 5 will show statistical significance (p < 0.05) purely by chance. A statistical correlation without an economic mechanism is useless.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Information Coefficient (IC)" to "The Spearman rank correlation between an equity factor's score today and forward return over the subsequent holding period.",
            "Spearman Rank Correlation" to "A non-parametric correlation measure that evaluates monotonic relationships based on relative ranking rather than linear values.",
            "Spurious Correlation" to "A statistical correlation between two variables that is purely coincidental and lacks any causal economic link.",
            "Information Ratio of IC (IC IR)" to "The mean Information Coefficient divided by its standard deviation over time; measures factor signal stability.",
            "Multiple Hypothesis Testing (p-hacking)" to "The quantitative fallacy of testing thousands of random data features until finding one that works by chance."
        ),
        compoundingRealityCheck = """
            Quantitative researcher David Leinweber famously discovered that between 1981 and 1993, butter production in Bangladesh had a 75% correlation with the S&P 500. 
            Anyone trading on that correlation lost everything when the coincidence broke down.
            Never put real capital behind a statistical correlation unless you can explain the fundamental economic mechanism of why investors are paid to take that risk.
        """.trimIndent(),
        fictionalExample = """
            Spurious Mining vs Causally-Grounded Quant Research:
            
            • Researcher A (Data Miner):
              • Tests 2,000 random corporate variables without economic hypotheses.
              • Finds that CEO shoe size and corporate headquarters latitude correlate with returns at r = 0.52 (p < 0.01).
              • Launches a fund: Suffers a 40% loss in year 1 because the correlation was pure statistical noise.
              
            • Researcher B (Economic Quant):
              • Hypothesizes that companies with high Free Cash Flow Yield outperform because cash cannot be fabricated.
              • Tests FCF Yield across 30 years, finding a steady average IC of +0.045 with an IC IR of 0.65.
              • Launches strategy: Delivers consistent 350 bps of annual alpha with low turnover.
        """.trimIndent(),
        realStockExample = """
            The ROIC and Free Cash Flow Yield IC Across US Markets:
            
            • Over three decades of S&P 500 and Russell 1000 data, ROIC and FCF Yield consistently post positive Information Coefficients (+0.04 to +0.06).
            • The causal link is economically irrefutable: companies that produce high real cash return on capital have more internal funding to reinvest and reward shareholders.
        """.trimIndent(),
        commonMistakes = listOf(
            "Using Pearson linear correlation on non-linear equity data" to "Missing strong monotonic relationships that aren't straight lines.",
            "Confusing correlation with causation" to "Trading on coincidental patterns without an underlying economic mechanism.",
            "Failing to track IC time series volatility" to "Assuming a factor that has positive mean IC is always profitable every month.",
            "Ignoring cross-factor correlation" to "Building a portfolio with 5 'different' factors that all correlate at 0.85 with each other."
        ),
        goldenRules = listOf(
            "The Spearman Standard: Always evaluate equity factors using Spearman rank correlation rather than Pearson linear correlation.",
            "The Economic Hypothesis Prerequisite: Formulate the financial causation theory before examining correlation coefficients.",
            "The IC Threshold: Require an average Information Coefficient of at least +0.03 with a t-statistic greater than 2.0.",
            "The IC Stability Rule: Target an Information Ratio of IC (IC / Std(IC)) greater than 0.5 for core strategy signals.",
            "The Orthogonal Check: Screen correlation matrices to ensure newly added factor signals carry low correlation to existing signals."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 26,
        stageNumber = 5,
        stageTitle = "Stage 5 — Data & Pattern Discovery",
        title = "Ranking Stocks and Multi-Factor Percentiles",
        icon = Icons.Rounded.Calculate,
        coreQuestion = "How do quantitative managers convert disparate, non-linear fundamental ratios into standardized, actionable stock rankings?",
        primaryFormula = "Percentile Rank = ((Rank_i - 0.5) / N_stocks) × 100  |  Standardized Z-Score = (X_i - μ) / σ",
        researcherRule = "Never compare raw valuation multiples across divergent sectors. A P/E of 28x in software may sit at the 35th sector percentile (cheap), whereas a 28x P/E in electric utilities sits at the 99th percentile (extreme speculative bubble). Always sector-neutralize factor ranks.",
        whatDataDoesNotProve = "Being ranked #1 in a multi-factor composite does not immunize a stock from macro systemic drawdowns. An equity with a 99th percentile quality score will still experience price drawdowns when the S&P 500 undergoes a liquidity contraction.",
        assetInfographicPath = "learnings/lesson_26_infographic.jpg",
        keyCheckpoints = listOf(
            "Cross-sectional percentile ranking across 0th to 100th percentiles",
            "Winsorized Z-score standardization: Z = (X - μ) / σ",
            "Sector-relative normalization vs. broad market universe ranking",
            "Factor weighting sensitivity testing (assumptions vs empirical alpha)"
        ),
        fullNotesOverview = """
            Quantitative portfolio construction requires converting raw, non-linear financial metrics into uniform, comparable scoring scales. Raw metrics suffer from dimensionality mismatch: ROIC is expressed in percentages, EV/EBITDA in enterprise multiples, Sloan accruals in decimals, and momentum in price ratios. Direct summation or linear averaging is mathematically invalid.
            
            Standardization Frameworks:
            1. Cross-Sectional Percentile Ranking: Converts any metric into a uniform distribution bounded between 0.0 and 100.0. Percentile ranking eliminates outlier sensitivity because an extreme P/E ratio of 5,000x receives the identical top rank as a 120x P/E ratio.
            2. Standardized Z-Scores: Preserves the relative magnitude of dispersion around the cross-sectional mean (μ) normalized by standard deviation (σ). Extreme values must be winsorized at ±3.0 standard deviations to prevent individual corporate write-downs from distorting regression weights.
            3. Sector Neutralization: Evaluates equities strictly relative to their GICS sector peer groups. This neutralizes macro sector bets, ensuring the quantitative strategy does not inadvertently allocate 100% of capital to high-beta tech during bull markets or 100% to low-beta utilities during recessions.
            4. Composite Factor Synthesis: Synthesizes individual factor ranks into a unified institutional composite score:
               Composite Alpha = 0.30(Quality) + 0.30(Growth) + 0.25(Value) + 0.15(Momentum)
               Quantitative managers must test factor weight sensitivity across historical regimes rather than assuming arbitrary heuristic weights.
        """.trimIndent(),
        compoundingRealityCheck = """
            Consider an unhedged quantitative strategy that ranks stocks purely on raw P/E ratio without sector neutralization. In a rising interest rate cycle, the strategy automatically allocates 85% of its portfolio to regional banks and energy companies because their absolute P/E multiples (7x–9x) appear cheaper than tech companies (25x–30x). When regional banks experience deposit flight and liquidity runs, the portfolio suffers a catastrophic 42% drawdown—not because the value factor failed, but because the manager failed to sector-neutralize the ranking algorithm.
        """.trimIndent(),
        fictionalExample = """
            Quant Alpha Partners manages a \${'$'}500M institutional long-only fund. The junior analyst constructs a stock screen filtering for stocks with ROIC > 25% and P/E < 15x. The screen returns zero utility companies, zero consumer staples, and 45 cyclical semiconductor and energy companies right at the peak of their commodity cycles. The senior portfolio manager modifies the ranking algorithm to rank each stock within its GICS sector:
            - Microsoft ranks in the 92nd percentile of Tech valuation (attractively priced relative to software peers).
            - NextEra Energy ranks in the 88th percentile of Utilities valuation.
            By enforcing sector neutrality, the portfolio achieves genuine multi-factor diversification and mitigates cyclical sector collapse.
        """.trimIndent(),
        realStockExample = """
            In 2021, Apple (AAPL) traded at a trailing P/E of approximately 31x, which looked historically expensive on an absolute basis compared to the broader market median of 22x. However, on a sector-relative quality-adjusted basis, Apple's ROIC of 58% placed it in the 99th percentile of all global technology hardware companies, while its Free Cash Flow conversion placed it in the top 2% of the S&P 500. Investors who mechanically discarded Apple based on an absolute P/E hurdle missed out on subsequent multi-year market outperformance and massive share repurchase compounding.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Percentile Ranking" to "Standardizing non-linear financial metrics into a bounded 0-100 uniform distribution.",
            "Z-Score Standardization" to "Measuring cross-sectional standard deviations from the sample mean: (X - μ) / σ.",
            "Sector Neutralization" to "Ranking equities strictly against sector peers to eliminate unintentional macro sector bets.",
            "Decile Leaderboard" to "Sorting a stock universe into 10 equal buckets to evaluate monotonic spread returns."
        ),
        commonMistakes = listOf(
            "Comparing raw multiples across sectors" to "Directly comparing software multiples to banking multiples creates unintended sector concentration.",
            "Using raw outliers in linear regressions" to "A single company with an extreme 10,000x P/E will skew regression weights unless winsorized.",
            "Treating factor weights as permanent laws" to "Assuming fixed 30/30/25/15 weights work equally in all macro environments without testing."
        ),
        goldenRules = listOf(
            "The Sector Neutrality Mandate: Always normalize valuation and margin metrics against sector medians before composite scoring.",
            "The Winsorization Rule: Clamp extreme standardized Z-scores to ±3.0 standard deviations to preserve model robustness.",
            "The Monotonic Decile Test: Verify that portfolio returns increase monotonically from Decile 10 (worst) to Decile 1 (best).",
            "The Weight Sensitivity Test: Stress-test strategy performance across varying factor weightings (±10% shifts) to rule out curve-fitting."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 27,
        stageNumber = 5,
        stageTitle = "Stage 5 — Data & Pattern Discovery",
        title = "Fundamental Pattern Discovery and Anomaly Hunting",
        icon = Icons.Rounded.Analytics,
        coreQuestion = "How do quantitative researchers discover non-random fundamental confluence patterns and rigorously test them for falsification?",
        primaryFormula = "Anomaly Confluence Score = f(High ROIC Spread, Accelerating Margins, Net Share Cannibalism, FCF Cash Yield)",
        researcherRule = "A quantitative researcher seeks falsification, not confirmation. Do not search for charts that confirm your bullish bias; actively design empirical data hurdles designed to disprove your hypothesis. If a pattern cannot survive aggressive cross-sectional stress-testing, it is random market noise.",
        whatDataDoesNotProve = "Visual correlation on a backtested chart does not prove economic causation. A backtest showing high returns for companies with rising R&D spending could merely be capturing the 2010–2021 low interest rate tech regime rather than an evergreen fundamental law.",
        assetInfographicPath = "learnings/lesson_27_infographic.jpg",
        keyCheckpoints = listOf(
            "The Compounder Confluence: High Secular Growth + High ROIC (> 20%)",
            "The Deep Value Cash Machine: High Free Cash Flow Yield (> 7%) + Low Valuation",
            "The Turnaround Divergence: Falling Stock Price + Expanding Operating Margins",
            "Capital Allocation Confluence: Debt Reduction + Share Cannibal Buybacks"
        ),
        fullNotesOverview = """
            Quantitative pattern discovery is the systematic search for persistent economic anomalies where financial statement realities diverge from market pricing. Rather than relying on technical chart squiggles (head-and-shoulders, cup-and-handle), institutional quants hunt for fundamental confluence patterns grounded in corporate finance principles.
            
            Core Fundamental Anomalies:
            1. The Compounder Anomaly (High Growth + High ROIC): Companies that generate ROIC far in excess of their Weighted Average Cost of Capital (WACC) and reinvest a large fraction of earnings at those same high rates experience exponential intrinsic value compounding that the market consistently underestimates due to human cognitive difficulty with geometric compounding.
            2. The Deep Value Cash Machine (High FCF Yield + Low Multiple): Companies with depressed accounting multiples (low P/E or low EV/EBITDA) that simultaneously generate massive, unencumbered Free Cash Flow. The high cash yield provides a structural margin of safety and enables self-funded corporate debt paydowns or share repurchases.
            3. The Turnaround Divergence (Price Contraction + Fundamental Acceleration): A powerful catalyst pattern where negative investor sentiment causes a stock's price to drop 20%–40%, while internal forensic financials reveal gross margin expansion, operating expense leverage, and accelerating net income.
            4. The Capital Allocation Confluence (Share Cannibal + Debt Deleveraging): Management teams that deliberately retire debt while aggressively repurchasing undervalued shares (reducing share count by > 2% annually) transfer enterprise value directly to remaining equity holders.
        """.trimIndent(),
        compoundingRealityCheck = """
            In 2016, Microsoft was undergoing its transition to cloud (Azure) under Satya Nadella. Retail investors focused on stagnant PC shipment figures. However, a quantitative confluence screen flagged:
            - ROIC accelerating from 18% toward 30%+
            - Commercial Cloud revenue compounding at > 50% YoY
            - Free Cash Flow conversion exceeding 110% of reported net income
            - Annual share count declining steadily by 1.5% through buybacks
            Investors who identified this fundamental confluence compounded wealth at over 28% annualized over the subsequent 8 years.
        """.trimIndent(),
        fictionalExample = """
            Apex Research investigates whether 'Falling Stock Price + Rising Gross Margin' generates abnormal alpha. They construct a 15-year backtest covering 3,000 US equities from 2008 to 2023.
            - Initial Hypothesis: Buying stocks down 25% over 6 months whose gross margins expanded by > 150 bps generates outsized recovery alpha.
            - Falsification Test: They test the rule during recessionary shocks (2008, 2020) and rising rate environments (2022).
            - The Result: The pattern generates +6.4% annualized excess alpha, but only when paired with a solvency filter (Altman Z > 2.0). Without the solvency filter, high-debt companies went bankrupt, destroying portfolio returns.
        """.trimIndent(),
        realStockExample = """
            Between 2022 and 2023, Meta Platforms (META) experienced an extreme turnaround divergence pattern. The stock plummeted from \${'$'}380 to \${'$'}88 (-76%) due to fears over Reality Labs Metaverse capital spending. However, by late 2022, Mark Zuckerberg announced the 'Year of Efficiency':
            - CapEx was reduced by \${'$'}4B.
            - Operating margins troughed at 20% and rapidly rebounded toward 40%.
            - Free Cash Flow surged back to \${'$'}43B annually.
            - Aggressive share repurchases retired \${'$'}20B of equity at cyclical trough prices.
            The fundamental turnaround divergence triggered a +450% stock price recovery within 18 months.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Compounder Anomaly" to "Sustained high reinvestment rate paired with >20% return on invested capital.",
            "Turnaround Divergence" to "Disconnection between declining market price sentiment and improving internal operating margins.",
            "Capital Allocation Confluence" to "Simultaneous corporate balance sheet deleveraging and aggressive share count retirement.",
            "Hypothesis Falsification" to "Rigorously testing data conditions that would disprove a strategy thesis before risking capital."
        ),
        commonMistakes = listOf(
            "Confusing chart patterns with fundamental anomalies" to "Trading visual price charts without verifying underlying earnings, margins, and cash flow generation.",
            "Ignoring the solvency shield" to "Buying low-multiple turnaround candidates with high debt leverage leads directly to bankruptcy value traps.",
            "Testing patterns without out-of-sample data" to "Optimizing an anomaly pattern on historical data without verifying its stability on unseen market regimes."
        ),
        goldenRules = listOf(
            "The Falsification Requirement: Write down the exact operational metric that would invalidate your investment thesis before purchasing shares.",
            "The Solvency Gate: Never enter a turnaround divergence candidate unless Altman Z is > 1.81 and Free Cash Flow is positive.",
            "The Capital Confluence Rule: Favor businesses where operating cash flow simultaneously reduces debt liabilities and repurchases undervalued equity.",
            "The Regime Invariance Check: Verify that fundamental anomalies deliver positive Information Coefficients in both bull and bear market regimes."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 28,
        stageNumber = 6,
        stageTitle = "Stage 6 — Backtesting",
        title = "Backtesting Architecture and Historical Simulation",
        icon = Icons.AutoMirrored.Rounded.ShowChart,
        coreQuestion = "How do quantitative engineers construct discrete time-stepped simulation engines that reflect live market execution reality?",
        primaryFormula = "Portfolio_Equity(t) = Portfolio_Equity(t-1) × [1 + Σ w_i(t) R_i(t) - Frictional_Drag(t)]",
        researcherRule = "A backtest is not a profit guarantee; it is a sanity check designed to eliminate fatally flawed investment concepts. If your backtest does not model bid-ask spread friction, market impact slippage, borrow fees, and execution timing delays, your live returns will deliver a fraction of backtest paper wealth.",
        whatDataDoesNotProve = "A stellar backtest with a 3.0 Sharpe ratio does not prove trading genius. In quantitative finance, unrealistically high backtest returns almost always indicate hidden look-ahead bias, survivorship bias, or severe parameter overfitting.",
        assetInfographicPath = "learnings/lesson_28_infographic.jpg",
        keyCheckpoints = listOf(
            "Discrete time-stepped simulation pipeline: Data -> Signal -> Order -> Execution",
            "Formalizing deterministic entry rules, exit rules, and position sizing caps",
            "Modeling realistic friction: Commissions, bid-ask spread crossing, and market impact",
            "Rebalancing cadences (monthly vs quarterly) and portfolio turnover attribution"
        ),
        fullNotesOverview = """
            Backtesting is the computational simulation of an investment hypothesis across historical market regimes. An institutional backtesting engine executes a discrete-event time loop, stepping forward day-by-day or month-by-month, strictly isolating historical information sets.
            
            Core Architecture of an Institutional Backtest Engine:
            1. Universe Selection Module: At each timestamp t, generates the eligible investable universe based on historical liquidity filters (e.g. Market Cap > \${'$'}1B, Median Daily Volume > \${'$'}5M).
            2. Signal Generation Engine: Computes quantitative factor scores and ranks equities strictly using information available at timestamp t.
            3. Order Allocation & Position Sizing: Applies weighting algorithms (equal-weight, cap-weight, risk-parity) subject to portfolio risk boundaries (e.g. max 5% single-stock exposure, max 25% sector exposure).
            4. Realistic Execution Simulator: Assumes market orders cross the bid-ask spread and deducts execution slippage (e.g. 10–25 bps per rebalance based on volume participation rates) and brokerage borrow fees for short positions.
            5. Performance & Attribution Ledger: Tracks daily Net Asset Value (NAV), benchmark tracking error against the S&P 500 (SPY), maximum drawdown, Sharpe ratio, Sortino ratio, and annualized portfolio turnover.
        """.trimIndent(),
        compoundingRealityCheck = """
            A retail quantitative model rebalances a 30-stock micro-cap momentum portfolio weekly. On paper, the backtest generates a staggering 48% annualized return. However, in live production:
            - Micro-cap bid-ask spreads average 0.60%.
            - Market impact on buying 50,000 shares moves the ask price up by 0.45%.
            - Total round-trip execution drag is 1.05% per trade.
            With 150% annual portfolio turnover, execution slippage incinerates 28% of total performance annually, turning a theoretical market-beating strategy into an unprofitable capital treadmill.
        """.trimIndent(),
        fictionalExample = """
            Systematic Capital builds an institutional multi-factor backtest engine in Python. They simulate an S&P 500 Quality-Value strategy from 2005 to 2024.
            - Step 1: Universe rebalanced on the first trading day of each month.
            - Step 2: Signal generated using SEC 10-Q filings with an enforced 45-day lag.
            - Step 3: Top 50 stocks selected, equally weighted at 2% each.
            - Step 4: 15 basis points deducted per rebalance for execution slippage and transaction costs.
            - Result: Strategy generates 13.8% Net CAGR with a 19.2% max drawdown, beating the S&P 500 (10.2% CAGR, 50.8% max drawdown in 2008) with verifiable execution fidelity.
        """.trimIndent(),
        realStockExample = """
            During the March 2020 COVID-19 liquidity shock, quantitative strategies with monthly rebalancing cadences faced extreme bid-ask spread widening. S&P 500 ETF (SPY) spreads remained tight at 1 cent, but individual small-cap value equities saw spreads blow out from 0.05% to over 2.50%. Backtests that assumed fixed 0.05% slippage severely underestimated the transaction drag of rebalancing during market panics.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Discrete Simulation" to "Stepping forward through historical calendar time without accessing future data states.",
            "Execution Slippage" to "The difference between theoretical signal price and actual filled execution price.",
            "Portfolio Turnover" to "The percentage of total portfolio capital bought and sold over an annualized period.",
            "Benchmark Attribution" to "Decomposing returns into systematic market beta vs idiosyncratic alpha."
        ),
        commonMistakes = listOf(
            "Assuming zero friction and instant execution" to "Ignoring bid-ask spreads and market impact creates fictional paper returns that vanish in live trading.",
            "Testing on uninvestable illiquid stocks" to "Including penny stocks with zero volume that could never be purchased at model sizes in reality.",
            "Ignoring rebalancing transaction drag" to "High-frequency weekly rebalancing strategies bleed capital through continuous spread crossing."
        ),
        goldenRules = listOf(
            "The Slippage Haircut: Always deduct at least 15–25 basis points of execution friction on every portfolio rebalance.",
            "The Liquidity Threshold: Restrict historical backtest universes to equities with average daily volume exceeding \${'$'}5M.",
            "The Turnover Cap: Design rebalancing rules that maintain annual portfolio turnover below 100% to maximize tax and fee efficiency.",
            "The Benchmark Integrity Rule: Always benchmark multi-factor strategies against both market-cap S&P 500 (SPY) and equal-weight S&P (RSP)."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 29,
        stageNumber = 6,
        stageTitle = "Stage 6 — Backtesting",
        title = "Look-Ahead Bias and Information Leakage Defense",
        icon = Icons.Rounded.Security,
        coreQuestion = "Why is look-ahead bias the single most destructive error in quantitative simulation, and how do institutional engineers eliminate it?",
        primaryFormula = "Information Availability Constraint: Execution_Timestamp(t) >= SEC_Public_Dissemination_Timestamp(t) + Buffer",
        researcherRule = "Never assume financial statements were available on the fiscal quarter-end date. Form 10-Q reports lag quarter-end by 30 to 45 days, and Form 10-K reports lag fiscal year-end by 60 to 90 days. Backtesting with unlagged quarter-end fundamentals produces fictional alpha that will collapse the moment live capital is deployed.",
        whatDataDoesNotProve = "A backtest with 40% annualized returns that buys stocks on December 31 based on 'Q4 earnings' does not prove algorithmic skill; it proves the computer bought stocks with advance knowledge of earnings that were not publicly filed until late February.",
        assetInfographicPath = "learnings/lesson_29_infographic.jpg",
        keyCheckpoints = listOf(
            "The 45-Day SEC 10-Q/K filing lag buffer requirement",
            "Point-In-Time (PIT) asynchronous database architecture",
            "Eliminating same-day close-to-open execution contamination",
            "Restatement hazards: Using preliminary numbers instead of modern restated figures"
        ),
        fullNotesOverview = """
            Look-Ahead Bias is the cardinal sin of quantitative finance. It occurs whenever an algorithm at simulation timestamp t accesses or conditions on any piece of data that was not legally, publicly, or physically disseminated into the market at or before timestamp t.
            
            Deadly Forms of Look-Ahead Contamination:
            1. Fiscal Quarter-End vs Filing Date Contamination: A company's fiscal Q3 ends on September 30. However, management and auditors require weeks to close books and file Form 10-Q with the SEC. Under SEC rules:
               - Large Accelerated Filers have 40 days to file Form 10-Q.
               - Accelerated Filers have 45 days.
               - Non-Accelerated Filers have 45 days.
               If your model buys a stock on October 1 based on Q3 earnings, you are effectively trading on insider information from the future.
            2. Asynchronous Restatement Contamination: In 2024, a data vendor provides a clean historical database of 2018 corporate earnings. However, in 2020, the company restated its 2018 revenue downward due to an accounting audit. If your backtest uses the 2024 restated figures for 2018 trades, you are testing on numbers that no trader on Earth could have seen in 2018.
            3. Same-Day Execution Contamination: Calculating a 200-day moving average using today's market close price, but executing the trade at today's market open price.
            4. Index Reconstitution Leakage: Backtesting 2015 trades using the 2024 S&P 500 constituent list.
        """.trimIndent(),
        compoundingRealityCheck = """
            An algorithmic fund backtests a quarterly earnings momentum strategy. The code mistakenly pulls earnings using `fiscal_period_end_date` rather than `sec_filing_date`.
            - Simulated Return: +34.2% annualized CAGR from 2012 to 2022.
            - Reality: The model was buying stocks that beat earnings 40 days before the public knew they beat earnings!
            When launched live with \${'$'}20M of real capital, the strategy generated -4.8% over the first year because it could no longer see the future.
        """.trimIndent(),
        fictionalExample = """
            Dr. Aris Vance, Chief Risk Officer at Chronos Quant, audits a junior quant's strategy claiming a 2.8 Sharpe ratio on S&P 500 equities.
            - Step 1: Aris checks the data join: `df.merge(fundamentals, on=['ticker', 'quarter_end'])`.
            - Step 2: Aris immediately spots the look-ahead flaw: trades are executed on January 2 using Q4 fundamentals that were not filed until February 24.
            - Step 3: Aris inserts a mandatory 45-day filing lag: `trade_date >= filing_date + pd.Timedelta(days=1)`.
            - Step 4: The simulated Sharpe ratio collapses from 2.8 down to 0.72. The strategy is decommissioned before losing firm capital.
        """.trimIndent(),
        realStockExample = """
            Nvidia (NVDA) reported Q4 Fiscal 2024 earnings for the quarter ended January 28, 2024. The formal SEC 10-K was filed on February 21, 2024, after market close. The stock gapped up +16% on February 22. Any backtest that executed a factor score on January 29, 2024, credited itself with a +16% single-day gain that was impossible to capture legitimately without look-ahead contamination.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Look-Ahead Bias" to "Inadvertently using future data in historical backtest trading rules.",
            "Filing Lag Buffer" to "Enforcing a mandatory 30-45 day window between quarter end and signal execution.",
            "Point-In-Time (PIT) Data" to "Databases that record the exact historical publication timestamp of every metric.",
            "Preliminary vs Restated" to "Using the original financial figures seen by the market rather than subsequent audits."
        ),
        commonMistakes = listOf(
            "Using fiscal quarter-end dates as trade dates" to "Companies do not report financials on the last day of the fiscal quarter.",
            "Ignoring time of day in earnings releases" to "Executing trades at 9:30 AM open on earnings that were only released at 4:05 PM after close.",
            "Using survivorship-biased current index constituents" to "Testing past decades using today's S&P 500 members introduces future selection bias."
        ),
        goldenRules = listOf(
            "The Mandatory 45-Day Rule: Enforce a strict minimum 45-day delay after fiscal quarter-end before fundamental signals become actionable.",
            "The Next-Day Execution Rule: If a signal is generated at market close (4:00 PM), execute trades no earlier than the next morning's market open (9:30 AM).",
            "The Point-In-Time Mandate: Always license or construct Point-In-Time databases that stamp data with SEC EDGAR public dissemination timestamps.",
            "The Preliminary Data Standard: Backtest exclusively on preliminary unadjusted earnings figures that live traders actually observed."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 30,
        stageNumber = 6,
        stageTitle = "Stage 6 — Backtesting",
        title = "Survivorship Bias and The Delisting Graveyard",
        icon = Icons.Rounded.Security,
        coreQuestion = "Why does testing today's successful companies against historical data produce dangerously inflated ghost alpha, and how do we resurrect the delisting graveyard?",
        primaryFormula = "Ghost Alpha Drag = E[R_survivors] - E[R_true_constituents] ≈ +3.5% to +7.2% per annum",
        researcherRule = "History is written by the survivors; financial markets punish those who forget the dead. If your backtest only includes companies that trade on the market today, you have artificially erased every corporate bankruptcy, accounting fraud, and distressed liquidation in history. Always backtest on historical point-in-time constituent snapshots.",
        whatDataDoesNotProve = "Backtesting Apple, Amazon, Microsoft, and Nvidia from 2000 to 2024 does not prove an investment strategy works. In 2000, nobody knew which tech companies would survive the dot-com crash. Selecting the mega-cap winners of history with hindsight bias is an optical illusion, not systematic research.",
        assetInfographicPath = "learnings/lesson_30_infographic.jpg",
        keyCheckpoints = listOf(
            "The Delisting Graveyard: Incorporating bankrupt and acquired equities",
            "Terminal Liquidation Returns: Modeling -80% to -100% terminal losses",
            "Quantifying the Ghost Alpha Gap (+3% to +8% annual artificial inflation)",
            "Dynamic Point-in-Time constituent universe reconstruction"
        ),
        fullNotesOverview = """
            Survivorship Bias occurs when a backtest selects its historical test universe using only stocks that exist today. By ignoring companies that went bankrupt, were acquired at distressed valuations, or were delisted, the backtest artificially inflates strategy performance by +300 to +800 basis points (+3% to +8%) annually.
            
            The Anatomy of the Delisting Graveyard:
            1. Corporate Mortality: In any given decade, approximately 30% to 50% of publicly traded US equities disappear from major exchanges due to bankruptcies, private buyouts, distress sales, or failure to meet listing standards.
            2. The S&P 500 Turnover Reality: The S&P 500 index is not static. On average, 20 to 25 companies are removed and replaced every single year. Over a 20-year horizon, over 300 companies are kicked out of the index.
            3. The Dead Equities of History:
               - Enron (Peak Market Cap: \${'$'}60B -> Bankrupt in 2001, -100% loss)
               - WorldCom (Peak Market Cap: \${'$'}180B -> Bankrupt in 2002, -100% loss)
               - Lehman Brothers (Peak Market Cap: \${'$'}45B -> Bankrupt in 2008, -100% loss)
               - Washington Mutual (Largest bank failure in US history, -100% loss)
               - Silicon Valley Bank (S&P 500 member -> Seized by FDIC in 2023, -100% loss)
            If your backtest runs on today's S&P 500 constituent list from 2000 to 2024, it literally never owns Enron, WorldCom, Lehman Brothers, or Silicon Valley Bank! It only owns the triumphant survivors.
            4. Terminal Liquidation Returns: When a company is delisted due to insolvency, its final recovery return is rarely zero; it is typically -80% to -100%. A proper backtesting engine must incorporate delisting return adjustments.
        """.trimIndent(),
        compoundingRealityCheck = """
            Consider two backtests of a 'Low P/E Value Strategy' from 2000 to 2010:
            - Backtest A (Flawed Survivor-Only Universe): Pulls stocks that exist in 2024 and had P/E < 12 in 2000. It produces an extraordinary +18.4% annualized return.
            - Backtest B (Survivorship-Free Universe with Graveyard): Includes all 2,000 stocks that traded in 2000. It owns Enron, WorldCom, and dozens of bankrupt dot-coms. It produces a realistic +11.2% annualized return.
            The +7.2% difference is pure Ghost Alpha—an illusion that will evaporate in live trading.
        """.trimIndent(),
        fictionalExample = """
            Quant Edge Capital pitches a \${'$'}100M institutional fund based on a 20-year backtest of an equal-weight quantitative momentum model.
            - An institutional due diligence auditor requests the data lineage: 'Does your universe include stocks delisted between 2004 and 2024?'
            - The quant admits: 'We used Yahoo Finance ticker symbols active as of yesterday.'
            - The auditor forces a re-run using CRSP (Center for Research in Security Prices) survivorship-free data with delisting returns.
            - The strategy CAGR plunges from +22.4% down to +13.1%, and maximum drawdown spikes from -24% to -52%. The institutional allocation is rejected.
        """.trimIndent(),
        realStockExample = """
            In February 2023, Silicon Valley Bank (SIVB) was an active constituent of the S&P 500, praised by quantitative value models for its low P/E multiple (9.5x) and high historical ROE. On March 10, 2023, the bank collapsed and was seized by the FDIC. Shareholders suffered a -100% total loss. Any backtest running today that downloads the S&P 500 list from a free web API does not include SIVB, completely ignoring a -100% wipeout that real-world systematic managers had to endure.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Survivorship Bias" to "Artificially inflating backtest returns by excluding companies that failed or were delisted.",
            "Delisting Graveyard" to "The complete historical ledger of bankrupt, merged, and defunct equities.",
            "Ghost Alpha" to "Fictional excess return generated by selecting historical survivors with hindsight.",
            "Point-In-Time Universe" to "Reconstructing the exact constituent membership that traded on each historical simulation date."
        ),
        commonMistakes = listOf(
            "Downloading historical data for current tickers only" to "Testing only today's listed stocks completely eliminates all historical corporate failures.",
            "Assuming delisted stocks cash out at 0% return" to "Delisted stocks often experience catastrophic -90% to -100% terminal gap drops upon exchange suspension.",
            "Ignoring S&P 500 constituent turnover" to "Over 300 companies have been kicked out of the S&P 500 over the past two decades."
        ),
        goldenRules = listOf(
            "The Point-In-Time Membership Rule: Reconstruct historical universes using only equities that were actively listed on that exact historical calendar date.",
            "The Graveyard Inclusion Mandate: Always incorporate delisted equities and assign realistic terminal liquidation losses (-80% to -100%).",
            "The Survivor Siphon Check: Compare strategy performance with and without delisted tickers; if returns drop by > 300 bps, your model suffers from survivorship leakage.",
            "The Total Universe Standard: Benchmark quantitative strategies against official total-market survivorship-free index series (e.g. CRSP US Total Market)."
        )
    ),
    FullLessonDefinition(
        lessonNumber = 31,
        stageNumber = 6,
        stageTitle = "Stage 6 — Backtesting",
        title = "Overfitting and Curve-Fitting",
        icon = Icons.Rounded.Psychology,
        coreQuestion = "How do quantitative researchers prevent creating complex models that merely memorize historical noise and collapse out-of-sample?",
        primaryFormula = "WFE = (OOS_CAGR / IS_CAGR) × 100  |  AIC = 2k - 2*ln(L)  |  BIC = k*ln(n) - 2*ln(L)",
        researcherRule = "Every additional parameter added to an investment model consumes degrees of freedom. Simple, intuitive models with sound economic rationale generalize reliably; complex 25-rule polynomial fits always fail.",
        whatDataDoesNotProve = "A 99% in-sample win rate does NOT prove strategy brilliance; it is almost mathematically guaranteed to be curve-fitted noise that implodes upon live capital allocation.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_31_infographic.jpg",
        keyCheckpoints = listOf(
            "The Overfitting Trap: Distinguishing between persistent economic alpha signals and idiosyncratic historical market noise.",
            "Degrees of Freedom & Parameter Parsimony: Restricting models to 3–5 intuitive, economically justified factors.",
            "Train / Test Data Segregation: Freezing model weights and reserving 30%+ of the time series strictly for blind testing.",
            "Walk-Forward Cross-Validation: Sliding window analysis testing parameter stability across diverse macro cycles.",
            "Walk-Forward Efficiency (WFE): Demanding that Out-of-Sample performance delivers at least 60–70% of In-Sample CAGR.",
            "Akaike & Bayesian Information Criteria (AIC/BIC): Statistical penalties that punish unnecessary model complexity.",
            "Deflated Sharpe Ratio (DSR): Adjusting Sharpe ratios downwards to account for multiple hypothesis testing."
        ),
        fullNotesOverview = """
            Overfitting is the deadliest sin in quantitative finance. When an analyst tests hundreds of factor combinations, indicators, and threshold cutoffs against historical market data, the backtesting engine will inevitably discover parameter combinations that produced astronomical historical returns purely by chance.
            
            This phenomenon—variously termed data snooping, curve-fitting, or p-hacking—creates an optical illusion of profitability. The strategy did not learn an underlying economic law of the market; it simply memorized the specific, random noise of that historical period. When deployed with live capital, the noise changes, and the strategy collapses into severe drawdowns.
            
            Institutional Quantitative Defenses:
            1. Parameter Parsimony (Occam's Razor): Enforcing a strict limit of 3 to 5 core parameters with verified economic explanations.
            2. Strict In-Sample (IS) vs. Out-of-Sample (OOS) Splits: Calibrating factors on 70% of historical data, then freezing all parameters to run a single blind test on the remaining 30%.
            3. Walk-Forward Cross-Validation: Stepping forward sequentially through history to verify that model performance persists across high-inflation, zero-rate, and bear market regimes.
            4. Walk-Forward Efficiency (WFE): Calculating the ratio of out-of-sample annualized return to in-sample annualized return; strategies with WFE < 50% are discarded.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Curve-Fitting" to "Tuning parameters until a model fits historical noise rather than genuine recurring economic signal.",
            "In-Sample vs. Out-of-Sample" to "Training on historical segment (IS) while reserving unseen future data (OOS) for blind validation.",
            "Walk-Forward Efficiency" to "Ratio of Out-of-Sample CAGR to In-Sample CAGR; measures real-world generalization power.",
            "Parameter Parsimony" to "The principle that simpler models with fewer variables are vastly more robust in live markets."
        ),
        compoundingRealityCheck = """
            Consider a strategy that adds 12 bespoke indicators to eliminate every historical drawdown from 2010 to 2020:
            - In-Sample: 38% CAGR, 2.8 Sharpe ratio, 4% max drawdown.
            - Out-of-Sample (2021-2024): -14% CAGR, -0.3 Sharpe ratio, 48% drawdown.
            The model did not find alpha; it memorized 2010-2020 noise.
        """.trimIndent(),
        fictionalExample = """
            Apex Quant Labs tests 5,000 indicator permutations to find a 'perfect' equity timing strategy.
            The resulting strategy requires a 47.3-day RSI, combined with Tuesday-only entries when lunar cycles align.
            When deployed with live money, it loses 25% in its first four months as random market noise diverges from the fitted curve.
        """.trimIndent(),
        realStockExample = """
            Renaissance Technologies (Medallion Fund) maintains legendary returns because candidate signals must survive deflated Sharpe ratio tests, out-of-sample isolation, and walk-forward efficiency gates before receiving capital. Over 85% of backtested academic factor variations are rejected as curve-fitted noise.
        """.trimIndent(),
        commonMistakes = listOf(
            "Adding Filters to Patch Historical Drawdowns" to "Adding a rule like 'avoid trading on Tuesday if VIX > 28' fixes one historical dip but guarantees live failure.",
            "Testing on the Full Dataset First" to "Once you have observed the out-of-sample data, any subsequent model tweaking is tainted by leakage.",
            "Ignoring Multiple Testing Penalties" to "Running 1,000 backtests and picking the single best result is pure statistical randomness, not skill.",
            "Over-optimizing Moving Average Windows" to "Fine-tuning an indicator from 50 to 47.5 days fits noise; robust strategies work across 40 to 60 days."
        ),
        goldenRules = listOf(
            "The Three-Parameter Rule: If an equity strategy requires more than 3 to 5 tunable parameters, treat it as curve-fitted noise.",
            "The 70% Walk-Forward Rule: Require Walk-Forward Efficiency to exceed 70% before allocating live investor capital.",
            "The Blind Out-of-Sample Gate: Never look at or optimize against the reserved out-of-sample time series until the model is finalized.",
            "The Economic Rationale Mandate: Reject any empirical pattern that lacks a clear economic, behavioral, or structural foundation."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 32,
        stageNumber = 6,
        stageTitle = "Stage 6 — Backtesting",
        title = "Evaluating Backtest Performance",
        icon = Icons.Rounded.BarChart,
        coreQuestion = "Which statistical risk and return metrics tell the unvarnished truth about an investment strategy's survivability and alpha?",
        primaryFormula = "Sharpe = (R_p - R_f) / sigma_p  |  Sortino = (R_p - R_f) / sigma_down  |  Calmar = CAGR / |MDD|  |  PF = Gross_Wins / Gross_Losses",
        researcherRule = "CAGR without Maximum Drawdown is meaningless financial marketing. If a strategy drops 55%, the vast majority of investors will capitulate and redeem at the exact market bottom.",
        whatDataDoesNotProve = "A high historical CAGR does NOT prove strategy superiority if volatility is extreme, turnover costs erode gains, or the win rate is concentrated in a tiny cluster of outlier days.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_32_infographic.jpg",
        keyCheckpoints = listOf(
            "Compound Annual Growth Rate (CAGR): Compounded annual wealth accumulation smoothed across cycles.",
            "Maximum Drawdown (MDD) & Underwater Curve: Peak-to-trough capital decline and time required to achieve new highs.",
            "Sharpe Ratio vs. Sortino Ratio: Why penalizing upside volatility in Sharpe is flawed, and why Sortino isolates downside danger.",
            "Calmar Ratio: Annualized return divided by maximum drawdown; measures the return per unit of catastrophic risk.",
            "Profit Factor & Win Rate: Gross trading profits divided by gross trading losses; institutional baseline requires PF > 1.8.",
            "Portfolio Turnover & Execution Drag: Quantifying how much alpha is destroyed by commissions, bid-ask spread, and market impact.",
            "Benchmark Relative Alpha & Tracking Error: Evaluating consistency and active share relative to the S&P 500."
        ),
        fullNotesOverview = """
            Evaluating a backtest like an institutional allocator requires looking far beyond the headline return. Retail investors routinely fall in love with high CAGR figures, ignoring the fact that the path taken to achieve that return was fraught with 50%+ drawdowns that no human capital allocator could emotionally endure.
            
            An institutional assessment interrogates four dimensions of performance:
            1. Risk-Adjusted Efficiency: The Sharpe ratio measures excess return over cash per unit of total standard deviation. The Sortino ratio refines this by only penalizing downside deviations below the risk-free rate.
            2. Tail Risk & Capital Preservation: The underwater equity curve, Maximum Drawdown (MDD), and Ulcer Index reveal the psychological pain and capital impairment experienced during market panics.
            3. Expectancy & Edge: Profit Factor (total gains / total losses), win rate, and average win / average loss ratio demonstrate whether the strategy possesses an authentic statistical edge or relies on a single lucky outlier.
            4. Net-of-Friction Realism: Subtracting turnover costs, borrowing fees, and slippage to ensure the theoretical paper alpha survives in live market microstructure.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Sharpe Ratio" to "Excess return above the risk-free rate divided by total portfolio standard deviation.",
            "Sortino Ratio" to "Excess return divided by downside deviation; avoids penalizing bullish upside volatility.",
            "Maximum Drawdown (MDD)" to "The maximum percentage drop from an equity peak to a subsequent trough.",
            "Calmar Ratio" to "CAGR divided by absolute Maximum Drawdown; quantifies return per unit of tail risk.",
            "Profit Factor" to "Gross trading profits divided by gross trading losses; institutional grade requires > 1.8."
        ),
        compoundingRealityCheck = """
            Consider a strategy with +22% CAGR but a -62% Maximum Drawdown during a recession:
            - An investor with \\${'$'}1,000,000 watches their net worth plummet to \\${'$'}380,000.
            - Under intense emotional panic and spousal/client pressure, 90% of capital allocators liquidate at the bottom.
            A lower-return strategy (+15% CAGR) with only a -18% drawdown is vastly superior because investors actually stay invested.
        """.trimIndent(),
        fictionalExample = """
            Alpha Momentum Fund advertises a 30% backtested annual gain.
            An institutional due diligence check uncovers:
            - Sharpe Ratio: 0.65 (stomach-churning annualized volatility of 38%).
            - Turnover: 450% per year, inflicting -4.2% annual drag in commissions and spread slippage.
            - Net-of-fee realized Sharpe drops to 0.40. The institutional mandate is denied.
        """.trimIndent(),
        realStockExample = """
            During the dot-com collapse (2000-2002), high-flying tech funds that boasted 60%+ CAGR in 1999 suffered -75% to -92% drawdowns. Recovering from an 80% loss requires a +400% gain just to break even, which took over 15 years.
        """.trimIndent(),
        commonMistakes = listOf(
            "Focusing Solely on CAGR" to "Ignoring drawdowns; a 20% CAGR strategy with a 65% drawdown will cause investor capitulation.",
            "Ignoring Risk-Free Rate Benchmarks" to "Generating 8% CAGR in a 5.5% Treasury environment represents negligible risk-adjusted alpha.",
            "Treating Drawdown as Purely Temporary" to "Underestimating recovery duration; some strategies take over 8 years to regain nominal equity highs.",
            "Omitting Transaction Turnover Drag" to "A strategy trading 300% annual turnover can forfeit 3–5% of annualized return to execution friction."
        ),
        goldenRules = listOf(
            "The Institutional Sharpe Gate: Require a minimum backtested Sharpe Ratio of 1.0 (and Sortino > 1.5) net of all friction.",
            "The 25% Maximum Drawdown Rule: Reject any non-leveraged long equity strategy whose historical drawdown exceeds 25%.",
            "The Profit Factor Standard: Insist on a Profit Factor >= 2.0 to ensure statistical resilience across varying market regimes.",
            "The Calmar Hurdle: Require a Calmar Ratio >= 0.75 (CAGR at least 75% of Maximum Drawdown)."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 33,
        stageNumber = 7,
        stageTitle = "Stage 7 — Portfolio Construction",
        title = "Portfolio Basics and Diversification",
        icon = Icons.Rounded.PieChart,
        coreQuestion = "How many stocks do you actually need to eliminate company-specific risk, and how does asset covariance create the only 'free lunch' in finance?",
        primaryFormula = "sigma_p^2 = Sum(w_i^2 * sigma_i^2) + 2 * Sum(w_i * w_j * Cov_ij)  |  N_eff = 1 / Sum(w_i^2)",
        researcherRule = "Diversification is the only free lunch in investing. Blending 20 to 30 fundamentally sound, low-covariance stocks eliminates over 90% of idiosyncratic corporate disaster risk without sacrificing portfolio expected return.",
        whatDataDoesNotProve = "Owning 50 stocks in the same sector is NOT diversification; they all share exposure to identical interest rate, regulatory, and multiple compression factors.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_33_infographic.jpg",
        keyCheckpoints = listOf(
            "Idiosyncratic vs. Systematic Risk: Eliminating firm-specific disaster risk while accepting undiversifiable market beta.",
            "The Markowitz Covariance Matrix: Why non-correlated assets reduce overall portfolio variance below the weighted average volatility.",
            "The Optimal Holdings Curve: The empirical mathematical proof showing 90%+ risk reduction achieved at 20–30 stocks.",
            "Concentration vs. Diworsification: Avoiding excessive diversification that dilutes research focus and mirrors an index fund.",
            "Effective N (Herfindahl Index): Measuring true portfolio concentration rather than nominal stock counts.",
            "Sector & Industry Exposure Ceilings: Enforcing a strict 25% max allocation per economic sector.",
            "Weighting Methodologies: Comparing Equal Weighting, Market-Cap Weighting, and Volatility-Parity Weighting."
        ),
        fullNotesOverview = """
            Modern Portfolio Theory, established by Harry Markowitz, revolutionized capital allocation by demonstrating that the risk of a portfolio is determined primarily by how assets interact with each other (covariance), rather than their individual volatility.
            
            When two assets have a low or negative correlation, their price fluctuations partially offset each other. A properly structured portfolio can achieve a lower total standard deviation than any of its individual constituents while preserving expected return—the celebrated 'free lunch' of quantitative finance.
            
            Key Structural Rules for Institutional Equity Portfolios:
            1. The 20–30 Stock Sweet Spot: Empirical analysis confirms that holding beyond 30 individual US equities produces diminishing risk-reduction returns while creating analytical overhead and diworsification.
            2. Single Stock Concentration Cap: Limiting any single position to a maximum of 8.0% of portfolio equity protects the fund from terminal corporate bankruptcies or fraud.
            3. Sector Concentration Cap: Capping sector exposure at 25.0% prevents macroeconomic sector rotations from causing catastrophic drawdown.
            4. Effective N Monitoring: Using the inverse Herfindahl index (1 / sum(w_i^2)) to ensure that a nominally 30-stock portfolio is not secretly dominated by 3 mega-cap holdings.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Idiosyncratic Risk" to "Firm-specific risk (fraud, product failure, CEO departure) that can be eliminated via diversification.",
            "Systematic Risk" to "Market-wide risk (recession, interest rates, geopolitical shock) that cannot be diversified away.",
            "Covariance Matrix" to "Statistical matrix tracking how every pair of assets in a portfolio moves relative to each other.",
            "Diworsification" to "Adding low-quality, highly correlated assets that dilute returns without reducing risk."
        ),
        compoundingRealityCheck = """
            An investor holding 5 stocks has a 20% chance that a single Enron or Silicon Valley Bank fraud impairs 20% of their total wealth.
            An investor holding 25 stocks with an 8% cap limits single-stock catastrophe to a manageable bump that compounding easily absorbs within two quarters.
        """.trimIndent(),
        fictionalExample = """
            Tech Compounders Fund owns 35 software and semiconductor equities. The manager claims broad diversification.
            In 2022, rising interest rates cause the entire tech sector to de-rate by 40%. The portfolio drops 42% because cross-holding correlation was 0.88.
            True diversification requires balancing tech momentum with consumer defensive, healthcare, and energy cash-flow compounders.
        """.trimIndent(),
        realStockExample = """
            Ray Dalio's Bridgewater All-Weather strategy proved that blending assets biased to growth (equities), inflation (commodities), and deflation (Treasuries) achieves superior Sharpe ratios by minimizing correlation rather than chasing isolated stock picks.
        """.trimIndent(),
        commonMistakes = listOf(
            "Equating Holding Count with Diversification" to "Owning 25 different semiconductor stocks is a sector bet, not a diversified portfolio.",
            "Over-Diversifying into Index Hugging" to "Holding 150+ stocks with high fees guarantees underperformance relative to low-cost ETFs.",
            "Ignoring Hidden Factor Correlations" to "Failing to realize that tech growth and regional banks both carried violent duration mismatch risks.",
            "Letting Winning Positions Overconcentrate" to "Allowing a single stock to grow from 5% to 40% of a portfolio reintroduces catastrophic single-stock ruin."
        ),
        goldenRules = listOf(
            "The 25-Stock Golden Mean: Target 20 to 30 high-conviction holdings across non-overlapping economic sectors.",
            "The 8% Single Position Ceiling: Never allow any individual common equity holding to exceed 8.0% of total portfolio capital.",
            "The 25% Sector Corridor: Enforce a strict 25.0% maximum allocation to any single GICS economic sector.",
            "The Low-Covariance Mandate: Pair momentum winners with defensive high-ROIC cash-flow compounders to compress portfolio beta."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 34,
        stageNumber = 7,
        stageTitle = "Stage 7 — Portfolio Construction",
        title = "Risk Management and Drawdown Defense",
        icon = Icons.Rounded.Security,
        coreQuestion = "How do quantitative managers use position sizing, the Kelly Criterion, and asymmetric recovery math to mathematically prevent capital ruin?",
        primaryFormula = "f* = 0.5 * (p*b - q) / b  |  Ruin = ((1 - A) / (1 + A))^C  |  Recov = (1 / (1 - DD)) - 1",
        researcherRule = "Rule #1: Never lose capital. Rule #2: Never forget Rule #1. Preventing catastrophic drawdowns is the most powerful compounding force in finance because the mathematics of loss recovery is deeply asymmetric.",
        whatDataDoesNotProve = "Setting a rigid 5% stop-loss does NOT guarantee safety; during volatile gap-down opens, orders execute far below stops, causing death by a thousand cuts.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_34_infographic.jpg",
        keyCheckpoints = listOf(
            "The Asymmetric Recovery Law: Why a 50% loss requires a 100% gain, and an 80% loss requires a 400% gain to break even.",
            "Risk of Ruin Mathematics: Calculating the probability of total account insolvency under varying win rates and sizing fractions.",
            "The Kelly Criterion for Position Sizing: Optimal capital allocation for geometric wealth maximization.",
            "Half-Kelly Institutional Implementation: Harvesting 75% of maximum growth with a 50% reduction in volatility and tail risk.",
            "Volatility-Parity Position Sizing: Sizing positions inversely to volatility so that each stock contributes equal dollar risk.",
            "Dynamic Cash Allocation as an Offensive Weapon: Holding dry powder to deploy aggressively at cyclical market bottoms.",
            "Calendar vs. Threshold Portfolio Rebalancing: Rebalancing rules that harvest volatility without triggering tax penalties."
        ),
        fullNotesOverview = """
            Compounding is the eighth wonder of the world, but it has a fatal vulnerability: multiplication by zero equals zero. If a portfolio incurs a 90% drawdown, recovery requires a 900% gain—a feat that often takes several decades.
            
            Risk management is not about predicting market crashes; it is about designing a portfolio architecture that is mathematically incapable of suffering terminal impairment.
            
            The Three Pillars of Institutional Risk Management:
            1. Sizing via the Kelly Criterion: The Kelly formula calculates the mathematically optimal fraction of bankroll to wager on an investment with positive expected value: f* = (p*b - q) / b. Because full Kelly produces extreme volatility and deep drawdowns, institutional quant funds universally deploy Half-Kelly (0.5 * f*), which delivers 75% of peak geometric growth with half the variance.
            2. Volatility-Parity Equalization: High-beta stocks (e.g., TSLA, NVDA) inherently carry 3x the volatility of defensive compounders (e.g., JNJ, PG). Equalizing dollar weight gives the high-beta stocks complete dominance over portfolio variance. Vol-parity sizes position weight inversely to volatility: w_i = (1 / sigma_i) / sum(1 / sigma_k).
            3. Dynamic Cash Buffers: Holding 5% to 15% cash during overheated valuation regimes serves as an airbag that dampens drawdowns and transforms into dry powder to scoop up bargains during panic selloffs.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Kelly Criterion" to "Mathematical formula determining optimal position size to maximize long-term geometric compounding.",
            "Half-Kelly Sizing" to "Allocating 50% of the theoretical Kelly fraction to dramatically reduce drawdown risk.",
            "Asymmetric Recovery" to "The mathematical reality that percentage gains required to recover from losses accelerate exponentially.",
            "Volatility-Parity" to "Weighting assets inversely to volatility so each position contributes equal portfolio risk."
        ),
        compoundingRealityCheck = """
            The Mathematics of Ruin:
            - Drop 10%: Need +11.1% to recover.
            - Drop 20%: Need +25.0% to recover.
            - Drop 30%: Need +42.9% to recover.
            - Drop 50%: Need +100.0% to recover.
            - Drop 80%: Need +400.0% to recover.
            Once you lose 50%, your capital base is cut in half, doubling the effort required to get back to even.
        """.trimIndent(),
        fictionalExample = """
            A discretionary trader wagers 30% of their account on high-volatility call options.
            Three consecutive bad earnings prints create a 75% account drawdown.
            The account now requires a 300% gain just to return to the starting balance—forcing reckless gambling that leads to total insolvency.
        """.trimIndent(),
        realStockExample = """
            Long-Term Capital Management (LTCM) in 1998 had Nobel laureates on staff, but their models used full-Kelly aggressive leverage. When the Russian debt crisis hit, unexpected correlation spikes wiped out \\${'$'}4.6 billion in six weeks, forcing a Federal Reserve-orchestrated bailout.
        """.trimIndent(),
        commonMistakes = listOf(
            "Averaging Down on a Failing Thesis" to "Throwing good money after bad into fundamentally impaired value traps.",
            "Using Full-Kelly Sizing in Real Markets" to "Theoretical Kelly assumes known probabilities; real market uncertainty causes Full-Kelly to induce ruin.",
            "Treating Cash as a Drag on Returns" to "Failing to recognize cash as a valuable strategic option with zero volatility and immediate liquidity.",
            "Failing to Rebalance Winners" to "Allowing successful high-volatility positions to consume the entire portfolio risk budget."
        ),
        goldenRules = listOf(
            "The Half-Kelly Rule: Never size a discretionary or systematic equity position beyond 50% of its theoretical Kelly optimal fraction.",
            "The 1.5% Risk Budget Gate: Never risk more than 1.5% of total portfolio equity on the downside stop of any single investment.",
            "The Volatility-Parity Scaling: Size high-volatility growth stocks smaller than low-volatility compounders to preserve risk balance.",
            "The Dry Powder Mandate: Maintain an intentional 5–10% cash liquidity reserve to exploit systemic market panic sales."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 35,
        stageNumber = 7,
        stageTitle = "Stage 7 — Portfolio Construction",
        title = "Building a Quantitative Portfolio",
        icon = Icons.Rounded.School,
        coreQuestion = "How do we unify universe definition, quality gates, multi-factor ranking, and rebalancing into an institutional systematic portfolio engine?",
        primaryFormula = "Universe -> Quality Filter -> Factor Ranking -> Top N Selection -> Vol Weight -> Rebalance -> Audit",
        researcherRule = "A systematic investor replaces emotion, headlines, and subjective opinions with a rigorous, closed-loop execution factory. The system dictates the buys, sizes, and rebalances; the human ensures operational integrity.",
        whatDataDoesNotProve = "A disciplined systematic portfolio does NOT avoid market downturns; it ensures psychological adherence, prevents catastrophic human error, and out-compounds emotional market participants over multi-year horizons.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_35_infographic.jpg",
        keyCheckpoints = listOf(
            "The 6-Stage Quantitative Factory: Universe -> Gate -> Rank -> Select -> Weight -> Rebalance.",
            "Stage 1: Liquidity Universe Definition (Market Cap > \\$2B, Average Daily Volume > \\$15M).",
            "Stage 2: Fundamental Quality & Solvency Gates (ROIC > 12%, Altman Z > 2.5, Accruals < 5%).",
            "Stage 3: Multi-Factor Composite Scoring (40% Quality + 30% Value + 30% Momentum).",
            "Stage 4: Cross-Sectional Selection (Selecting Top 25 Factor Leaders with 25% max sector caps).",
            "Stage 5: Risk-Parity & Half-Kelly Sizing (Max 8% weight per security, dynamic cash buffer).",
            "Stage 6: Rebalance Calendar & Drift Auditing (Quarterly execution with 5% rebalancing bands)."
        ),
        fullNotesOverview = """
            Building an institutional quantitative portfolio engine represents the culmination of the entire research journey. You have mastered financial statement forensics, discounted cash flow modeling, factor synthesis, and backtesting defense. Now, these tools combine into a unified, rule-based portfolio engine.
            
            The Institutional Portfolio Architecture:
            1. Liquid Universe Screening: Filtering out small-cap and micro-cap securities whose bid-ask spreads and liquidity constraints make paper alpha unrealizable.
            2. Fundamental Quality Gates: Eliminating companies with structural debt distress (Altman Z < 2.5), forensic accounting anomalies (Beneish M-Score > -1.78), or persistent negative cash flows.
            3. Multi-Factor Scoring & Ranking: Combining uncorrelated alpha drivers into a single cross-sectional percentile score: Quality (ROIC, Gross Margin, Piotroski), Value (FCF Yield, EV/EBITDA), and Momentum (12-1M Price Momentum).
            4. Diversified Portfolio Assembly: Extracting the top 20 to 25 ranked securities across non-overlapping sectors, enforcing a 25% maximum sector limit.
            5. Disciplined Rebalancing Protocol: Rebalancing quarterly or when position weights drift by more than 5% from target, re-harvesting the volatility premium while minimizing turnover friction.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Systematic Portfolio" to "An investment strategy governed strictly by rule-based quantitative algorithms without discretionary intervention.",
            "Quality Gate" to "Pre-screening hurdle that eliminates financially distressed or low-margin businesses before factor ranking.",
            "Composite Alpha Score" to "Weighted cross-sectional ranking synthesizing quality, value, and momentum factor dimensions.",
            "Rebalance Drift Band" to "Tolerance corridor (e.g. +/- 5%) triggering portfolio rebalancing only when positions materially deviate."
        ),
        compoundingRealityCheck = """
            Emotional retail investors chase hot stocks at peaks, panic-sell during bear market bottoms, and switch strategies every 6 months.
            A systematic quantitative investor adheres to their rebalancing rules, automatically buying undervalued quality compounders when others panic and harvesting profits when euphoria peaks.
        """.trimIndent(),
        fictionalExample = """
            Horizon Quant Partners launches a \\${'$'}50M multi-factor fund with a 6-stage quantitative pipeline.
            During the 2022 market downturn, the engine systematically rebalanced away from high-P/E unprofitable tech into high-FCF energy and healthcare compounders.
            The portfolio finished 2022 at +4.8% while the broader S&P 500 fell -18.1%.
        """.trimIndent(),
        realStockExample = """
            AQR Capital and Dimensional Fund Advisors manage hundreds of billions using systematic factor models. They do not predict macro headlines; they systematically buy companies with high profitability (Novy-Marx gross profitability), low relative valuation (Fama-French HML), and positive price momentum.
        """.trimIndent(),
        commonMistakes = listOf(
            "Discretionary Override at Market Lows" to "Overriding the quantitative engine during bear markets because of frightening news headlines.",
            "High-Frequency Turnover Overtrading" to "Rebalancing too frequently, causing transaction costs and short-term capital gains taxes to devour alpha.",
            "Neglecting Sector Exposure Limits" to "Allowing a quantitative model to place 60% of portfolio capital into a single hot sector.",
            "Abandoning Strategy After Short-Term Underperformance" to "Terminating a multi-factor system during standard 6-to-12-month cyclical factor drawdowns."
        ),
        goldenRules = listOf(
            "The Zero-Discretion Mandate: Never manually override systematic rebalancing rules based on emotional market forecasts.",
            "The Quarterly Rebalance Discipline: Execute portfolio rebalancing on a fixed calendar schedule (quarterly) with a 5% drift threshold.",
            "The Holistic Factor Synthesis: Require portfolio holdings to satisfy quality, valuation, and momentum criteria simultaneously.",
            "The Lifelong Systematic Edge: True wealth compounding is achieved through unwavering adherence to a mathematically sound investing process."
        )
    ),

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // STAGE 8: REAL INVESTING PROCESS (Lessons 36 - 40)
    // ------------------------------------------------------------------------
    FullLessonDefinition(
        lessonNumber = 36,
        stageNumber = 8,
        stageTitle = "Stage 8 — Real Investing Process",
        title = "How to Research a Company: The Checklist",
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        coreQuestion = "What repeatable research process must an evidence-based investor execute before committing real capital?",
        primaryFormula = "Research Checklist = Business & Moat (30%) + Financial Solvency (25%) + Capital Allocation (25%) + Invalidation Risks (20%)",
        researcherRule = "Never invest in a company you cannot explain to an intelligent 12-year-old in 90 seconds. A checklist is not a formality; it is aviation-grade life support protecting your capital from narrative deception and cognitive bias.",
        whatDataDoesNotProve = "Checking every box on a due diligence checklist does NOT guarantee immunity from macro recessions or black-swan sector disruptions; it ensures your risk of avoidable catastrophic failure is minimized to near zero.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_36_infographic.jpg",
        keyCheckpoints = listOf(
            "Item 1 (Business Overview): Identifying unit economics, revenue visibility (>70% recurring), and customer concentration (<10% per client).",
            "Item 7 (MD&A): Reading management's unfiltered operational narrative, margin drivers, and organic volume trends.",
            "Moat Identification: Verifying high switching costs, network effects, or scale cost advantages via 5-year gross margin stability.",
            "Solvency & Cash Forensics: Confirming Net Debt / EBITDA < 2.0x, Interest Coverage > 5.0x, FCF conversion > 85%, and negative Sloan accruals.",
            "Capital Allocation Track Record: Measuring 5-year average ROIC vs. WACC spread and share repurchase timing sanity.",
            "Item 1A (Risk Factors): Extracting the top 3 existential threats to the business model.",
            "Pre-Commitment Invalidation Thesis: Formulating 3 explicit quantitative conditions that will trigger an immediate exit.",
            "Apple Inc. 10-K Forensic Audit Case Study: How audited 10-Ks revealed Services ecosystem expansion while hardware cyclicals fluctuated."
        ),
        fullNotesOverview = """
            Professional surgeons and commercial airline pilots never operate without checklists. In investing, the human brain is wired to fall in love with charismatic CEO narratives, hot product hype, and stock ticker momentum. A rigorous, repeatable Due Diligence Checklist is your cognitive circuit breaker.
            
            Every comprehensive company research audit spans four non-negotiable pillars:
            1. Business Mechanics & Economic Moat: How does the enterprise generate cash? Is revenue recurring or transactional? Does the firm possess true pricing power (sustained high gross margins)?
            2. Financial Solvency & Earnings Forensics: Does the balance sheet represent a cash fortress? Are earnings backed by cold, hard free cash flow, or are they inflated by working capital accruals?
            3. Management Quality & Capital Allocation: Does management reinvest capital into high-ROIC internal projects, or do they squander cash on empire-building M&A and ill-timed buybacks?
            4. Invalidation Thesis: What specific fundamental breakdown would prove the investment thesis wrong?
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Due Diligence Checklist" to "A standardized, multi-pillar analytical framework executed prior to deploying capital.",
            "10-K Item 1 & Item 7" to "Statutory SEC filings disclosing business structure (Item 1) and Management Discussion & Analysis (Item 7).",
            "Pricing Power Proof" to "Gross margin consistency across 5+ years during periods of inflation and supply chain stress.",
            "Accrual Quality Filter" to "Ensuring Sloan Accrual Index is negative, proving net income is backed by cash flow.",
            "Invalidation Thesis" to "A pre-written contract defining exact metrics that will cause the investor to immediately exit."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Bypassing thorough research to chase explosive speculative microcaps or meme stocks promising 10% monthly gains invariably results in complete capital annihilation. Over 90% of retail traders who skip 10-K filings lose their entire account within 12 months. Wealth compounding is built on painstaking, methodical due diligence.
        """.trimIndent(),
        fictionalExample = """
            ChecklistHero Corp vs. NarrativeChaser Inc:
            Two investors evaluate HyperCloud SaaS:
            • NarrativeChaser reads social media threads celebrating 50% revenue growth and buys at 35x Sales. He never reads the 10-K. 6 months later, SBC dilution of 25% is revealed, operating cash flow is -$80M, and customer churn spikes. The stock plunges 75%, wiping out his capital.
            • ChecklistHero downloads the 10-K, runs the 4-pillar checklist: discovers gross margins are collapsing from 72% to 54% due to cloud infrastructure costs, customer acquisition cost (CAC) payback exceeds 36 months, and stock-based compensation consumes 40% of revenue. The checklist fails on 3 pillars. ChecklistHero walks away and preserves 100% of his capital!
        """.trimIndent(),
        realStockExample = """
            Apple Inc. (NASDAQ: AAPL, 10-K Forensic Checklist Walkthrough):
            • Pillar 1 (Business): 2.2B+ active devices; Services revenue grew to $85.2B with 74% gross margins, converting hardware one-off buyers into recurring subscription annuities.
            • Pillar 2 (Financials): $108.8B FCF against $93.7B Net Income (116% FCF conversion). Net Debt/EBITDA < 0.8x with 31.3x interest coverage.
            • Pillar 3 (Capital Allocation): 5-year average ROIC > 45%; repurchased $264B+ of stock, reducing shares by ~3% annually.
            • Pillar 4 (Risks): Antitrust scrutiny on App Store take-rate (Item 1A); geopolitical assembly concentration in Asia.
            • Verdict: Composite Checklist Score = 9.4 / 10.0 (Tier-1 Institutional Pass).
        """.trimIndent(),
        commonMistakes = listOf(
            "Skipping the SEC 10-K" to "Relying on secondary news articles and analyst summaries rather than primary audited filings.",
            "Ignoring Item 1A Risk Disclosures" to "Failing to read the mandatory legal disclosures detailing regulatory, customer, or legal vulnerabilities.",
            "Confusing Revenue Hype with Cash Profit" to "Falling in love with 40% top-line growth while ignoring negative operating cash flow.",
            "Failing to Pre-Commit to Invalidation" to "Buying without documenting what negative events would trigger an exit, leading to emotional bag-holding.",
            "Overlooking Customer Concentration" to "Investing in a supplier where a single OEM customer accounts for >30% of total revenue."
        ),
        goldenRules = listOf(
            "The Primary Source Mandate: Never commit capital without reading the most recent 10-K Item 1, Item 7, and the Consolidated Statement of Cash Flows.",
            "The 90-Second Rule: If you cannot clearly describe the company's revenue model, moat, and competitive threat in 90 seconds, pass.",
            "The Pre-Commitment Contract: Document your bull thesis and 3-point invalidation criteria before entering your broker's order screen.",
            "The Moat Proof Standard: Demand 5 years of stable or expanding gross margins as mathematical proof of competitive pricing power."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 37,
        stageNumber = 8,
        stageTitle = "Stage 8 — Real Investing Process",
        title = "How to Decide When to Buy",
        icon = Icons.Rounded.CheckCircle,
        coreQuestion = "What exact confluence of quality, valuation, and margin of safety triggers a disciplined buy decision?",
        primaryFormula = "Buy Trigger = (Quality Score >= 80) AND (Solvency Safe: Z > 2.99) AND (Price <= Fair Value * (1 - MoS)) AND (Expected IRR >= 12%)",
        researcherRule = "Patience is an investor's supreme competitive edge. Waiting for your exact pitch at a comfortable Margin of Safety is where true multi-year alpha is minted. Never swing at high pitches out of FOMO.",
        whatDataDoesNotProve = "A stock falling into your target Margin of Safety zone does NOT mean it has reached the absolute bottom. Stocks can remain undervalued or overshoot downward during irrational panic panics.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_37_infographic.jpg",
        keyCheckpoints = listOf(
            "The Multi-Pillar Buy Hurdle: Synthesizing business quality, balance sheet fortress, and valuation discount.",
            "Conservative Intrinsic Valuation: Multi-stage DCF modeling and Owner Cash Yield analysis under stress-tested growth rates.",
            "The Margin of Safety (MoS) Principle: Requiring a 20% to 35% discount to fair value before initiating purchases.",
            "Expected 5-Year Internal Rate of Return (IRR): Requiring prospective annualized returns >= 12% to justify equity risk.",
            "Tranche Sizing Protocol: Dividing capital into 3 tranches (35% initial, 35% price dip widening MoS, 30% fundamental confirmation).",
            "Eliminating FOMO: Resisting the psychological urge to buy near historical all-time highs when multiples exceed historical 90th percentiles.",
            "Pre-Trade Entry Log: Recording exact purchase thesis, fair value estimate, and target allocation percentage.",
            "Alphabet Inc. (GOOGL) 2022-2023 Bear Market Buy Case Study: Buying a wide-moat monopoly at 15x P/E with 30% Margin of Safety."
        ),
        fullNotesOverview = """
            A buy decision is a formal, irreversible allocation of capital. Once executed, you have transformed liquid cash into an equity claim subject to market volatility. Therefore, buying must never be an impulsive reaction to a green candlestick or a bullish headline.
            
            Institutional capital allocation requires a simultaneous confluence of four gates:
            1. Quality Gate: The business must demonstrate sustainable ROIC > 15%, durable economic moats, and pristine earnings forensics.
            2. Solvency Gate: Net Debt / EBITDA must be < 2.0x (or Net Cash Fortress) with an Altman Z-Score > 2.99.
            3. Valuation Gate: Current price must trade at a significant discount (Margin of Safety >= 20%) to conservative intrinsic value.
            4. Return Gate: The 5-year expected Internal Rate of Return (IRR = Free Cash Flow Yield + Organic Growth +/- Multiple Change) must exceed the equity hurdle rate (12% annually).
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Buy Decision Gate" to "A multi-factor mathematical threshold that must be 100% satisfied before order submission.",
            "Margin of Safety (MoS)" to "The percentage discount between current stock price and conservative intrinsic fair value.",
            "Expected 5-Year IRR" to "Forecasted annualized compound return derived from dividend yield, cash flow growth, and valuation reversion.",
            "Tranche Sizing" to "Staged execution dividing an allocation into 3 separate purchases to average entry price effectively.",
            "FOMO Circuit Breaker" to "A rule prohibiting stock purchases when valuation multiples exceed 10-year historical 90th percentiles."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Chasing 10% per month creates severe psychological urgency, forcing investors to buy overextended momentum stocks at the peak of euphoric bubbles. When the bubble inevitably deflates, a 50% loss requires a 100% gain just to break even. True compounders achieve 15% to 25% annualized returns by patiently waiting for market dislocations to buy world-class assets at a wide Margin of Safety.
        """.trimIndent(),
        fictionalExample = """
            ImpulseTrader vs. DisciplinedAllocator:
            CloudTitan Corp is fairly valued at $100/share based on DCF modeling.
            • ImpulseTrader sees the stock surge from $95 to $135 on upbeat news. Fearing he will miss out, he allocates 100% of his capital at $135 (negative Margin of Safety!). A general tech pullback hits, and the stock drops back to $90. Panicking, he sells at a 33% loss.
            • DisciplinedAllocator refuses to buy at $135. He waits patiently on the sidelines. When the pullback drives CloudTitan down to $78 (a 22% Margin of Safety to $100 fair value), he deploys Tranche 1 (35%). When market panic pushes it to $70, he deploys Tranche 2 (35%). His blended cost basis is $74. When the stock recovers to fair value ($100), DisciplinedAllocator locks in a +35% gain!
        """.trimIndent(),
        realStockExample = """
            Alphabet Inc. (NASDAQ: GOOGL, November 2022 - January 2023 Buy Case Study):
            • Market Panic: Fears that ChatGPT would instantly kill Google Search drove GOOGL stock down from $150 to $85.
            • Valuation Audit: At $88/share, GOOGL traded at ~15x trailing GAAP P/E and a 6.8% Free Cash Flow yield, backed by $115B in cash and zero net debt.
            • DCF Fair Value: Conservative 3-stage DCF assuming Search revenue growth slowed to only 7% yielded a fair value of $130/share.
            • Margin of Safety: Current Price ($88) offered a 32.3% Margin of Safety to fair value!
            • Execution: Disciplined allocators bought aggressively in the $85-$92 zone. Over the next 18 months, GOOGL recovered to $180+, generating a +105% total return!
        """.trimIndent(),
        commonMistakes = listOf(
            "Buying Out of FOMO at All-Time Highs" to "Entering full positions when valuation multiples are stretched to 2-3 standard deviations above historical norms.",
            "Buying Value Traps Without Quality" to "Buying a dying business simply because it looks cheap on P/E, ignoring structural revenue contraction.",
            "All-In Execution in a Single Order" to "Deploying 100% of capital in one tranche, leaving zero cash reserves if the stock suffers normal price volatility.",
            "Ignoring the Expected Return Hurdle" to "Buying an overvalued compounder with an expected prospective 5-year return of only 3% per year.",
            "Failing to Pre-Calculate Margin of Safety" to "Buying based on vague optimism without having a formal DCF or FCF yield model in place."
        ),
        goldenRules = listOf(
            "The 20% Margin of Safety Mandate: Never initiate a long equity position unless the current price offers at least a 20% discount to conservative intrinsic value.",
            "The 12% Expected IRR Standard: Demand an expected 5-year annualized return of >= 12% before risking capital in common stock.",
            "The Three-Tranche Rule: Scale into positions via 3 tranches (35% / 35% / 30%) to eliminate the fear of catching falling knives.",
            "The Quality-First Gate: Never compromise on balance sheet solvency or business quality for the sake of a low valuation multiple."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 38,
        stageNumber = 8,
        stageTitle = "Stage 8 — Real Investing Process",
        title = "How to Decide When to Hold",
        icon = Icons.Rounded.Security,
        coreQuestion = "How do you cultivate the iron discipline of inactivity and let compounding work through terrifying market panics?",
        primaryFormula = "Hold Thesis = Underlying ROIC > WACC  AND  FCF per Share Compounding  AND  Moat Intact",
        researcherRule = "The big money in investing is not in the buying or the selling, but in the waiting. Do not interrupt compounding unnecessarily. Every unnecessary trade incurs transaction friction, short-term taxes, and the fatal risk of selling winners too early.",
        whatDataDoesNotProve = "A 25% drop in stock price does NOT prove the business is deteriorating; market quotes reflect emotional auction sentiment, not audited balance sheets.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_38_infographic.jpg",
        keyCheckpoints = listOf(
            "The Discipline of Inactivity: Why sitting on your hands is the most lucrative and psychologically demanding skill in finance.",
            "Quarterly 10-Q Scorecard: Monitoring organic revenue growth, operating margin durability, and FCF per share expansion.",
            "Distinguishing Market Noise from Fundamental Impairment: Separating macroeconomic rate headlines from company-specific economic moats.",
            "The Cost of Interrupting Compounding: Illustrating how early exits for quick 20% profits destroy 10-year 500% compounding runs.",
            "Monitoring Capital Reinvestment: Verifying that incremental retained earnings continue to earn returns above the cost of capital.",
            "Management Guidance Verification: Tracking whether executive delivery matches promised targets across 4 consecutive quarters.",
            "Position Weight Drift: Managing position sizing as elite compounders naturally expand to represent larger percentages of total portfolio equity.",
            "Microsoft Corp. (MSFT) 10-Year Holding Case Study: Holding through 3 separate 25%+ market drawdowns to capture a 1,200% return."
        ),
        fullNotesOverview = """
            Charlie Munger famously observed: 'The first rule of compounding is to never interrupt it unnecessarily.' Yet, holding great businesses is the hardest discipline in finance. The modern financial ecosystem is designed to provoke hyperactive trading through flashing red ticker screens, panic headlines, and brokerage notifications.
            
            When you own a piece of a world-class enterprise, price fluctuations are merely offers from Benjamin Graham's manic-depressive 'Mr. Market'. If you owned a local apartment building producing reliable, growing rental cash flow, you would not sell it simply because an erratic passerby offered you 15% less today than yesterday.
            
            Holding requires an objective quarterly audit:
            1. Is Free Cash Flow per share continuing to grow?
            2. Are gross margins defending against inflation, proving moat pricing power?
            3. Is Return on Invested Capital (ROIC) remaining comfortably above WACC?
            If the answers are YES, the correct action is: DO NOTHING. Let the power of compound interest multiply your capital.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "The Hold Discipline" to "The strategic refusal to trade when underlying business fundamentals and compounding power remain intact.",
            "Quarterly 10-Q Audit" to "Systematic inspection of quarterly financial reports focusing exclusively on cash flow, margins, and ROIC.",
            "Price Volatility vs. Business Risk" to "Price volatility is quotation variance; business risk is the permanent destruction of earning power.",
            "Mr. Market Parable" to "Viewing market prices as daily convenience quotes rather than an authoritative appraisal of corporate value.",
            "Tax-Deferred Compounding" to "The enormous mathematical advantage of deferring capital gains taxes by holding multi-year winners."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Attempting to compound at 10% monthly compels investors to constantly jump in and out of positions, day-trading every headline. In reality, hyperactive trading generates massive short-term capital gains taxes (up to 37% in the US) and bid-ask slippage. Academic studies show that the most active 20% of traders underperform the market by 6.5% annually. Holding great compounders quietly crushes active trading over full market cycles.
        """.trimIndent(),
        fictionalExample = """
            HyperTrader vs. CompounderPatience:
            Both invest $100,000 into MegaTech Corp, which compounds earnings at 18% annually for 10 years (stock grows 5.2x).
            • HyperTrader monitors prices daily. When MegaTech drops 15% during a market panic in Year 2, he sells out of fear. When it rallies 30%, he buys back higher. Over 10 years, he executes 48 trades, pays $22,000 in trading taxes and fees, and misses the 10 biggest rally days. His ending balance is $185,000 (+85%).
            • CompounderPatience reviews each 10-Q, confirms FCF per share is compounding, and simply does nothing. Over 10 years, he executes ZERO sales. He pays zero capital gains taxes during the compounding period. His ending balance is $523,000 (+423%)!
            Verdict: HyperTrader worked 1,000 hours to earn less than half the wealth of CompounderPatience, who did nothing!
        """.trimIndent(),
        realStockExample = """
            Microsoft Corporation (NASDAQ: MSFT, 2014 - 2024 Holding Case Study):
            • The Compounding Run: Under Satya Nadella, MSFT transformed into a cloud powerhouse (Azure), growing FCF per share from $3.20 (FY14) to $10.15 (FY24).
            • The Volatility Test: During this 10-year holding period, MSFT suffered three severe drawdowns:
              1. 2015-2016: -19% drop during energy/China slowdown fears.
              2. 2018: -22% drop during Fed rate hikes.
              3. 2022: -38% drop during tech inflation selloff.
            • The Fundamental Reality: Throughout every single drawdown, Azure revenue grew >25% YoY, commercial cloud gross margins remained >70%, and share buybacks retired stock at discounted prices.
            • The Reward: Investors who held firm through all three terrifying drawdowns saw their capital grow by +1,250%!
        """.trimIndent(),
        commonMistakes = listOf(
            "Trimming Winners to Take Quick Profits" to "Selling your fastest-growing compounder to lock in a 20% gain, violating the rule of letting winners run.",
            "Watering the Weeds and Cutting the Flowers" to "Selling great performing companies to average down into failing, low-quality value traps.",
            "Panicking Over Quarterly Earnings Misses" to "Dumping a stock because EPS missed analyst consensus by 2 cents due to foreign exchange timing.",
            "Confusing Macro Noise with Moat Impairment" to "Selling an enterprise monopoly because the Federal Reserve raised interest rates by 25 basis points.",
            "Over-Monitoring Daily Price Quotations" to "Checking stock prices 20 times a day, which increases anxiety and triggers emotional trading errors."
        ),
        goldenRules = listOf(
            "The Compounding Inactivity Mandate: As long as ROIC exceeds WACC and per-share Free Cash Flow compounds, your default action is: DO NOTHING.",
            "The 10-Q Focus Standard: Read quarterly earnings reports for fundamental KPI progress (margins, FCF, unit economics), not short-term Wall Street EPS estimates.",
            "The Price Noise Filter: Never sell an investment solely because the share price dropped 10%, 20%, or 30% in a broad market selloff.",
            "The Tax-Efficiency Law: Recognize that holding winning investments defers capital gains taxes indefinitely, allowing your gross profits to compound."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 39,
        stageNumber = 8,
        stageTitle = "Stage 8 — Real Investing Process",
        title = "How to Decide When to Sell",
        icon = Icons.Rounded.Speed,
        coreQuestion = "Under what specific, objective, rule-based conditions should an evidence-based investor exit a position?",
        primaryFormula = "Exit Triggers: (Trigger 1: Thesis Broken) OR (Trigger 2: Extreme Valuation Euphoria) OR (Trigger 3: Opportunity Spread > 8%) OR (Trigger 4: Max Weight > 25%)",
        researcherRule = "Never sell out of panic, fear, or because a stock fell 10%. Selling must be as systematic, unemotional, and rule-based as buying. Document your exit triggers before you buy, not in the midst of a market crash.",
        whatDataDoesNotProve = "A surging stock price does NOT mean you shouldn't consider trimming if valuation multiples have detached into speculative mania territory.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_39_infographic.jpg",
        keyCheckpoints = listOf(
            "The 4 Valid Exit Triggers: Eliminating emotional selling by codifying precise institutional exit protocols.",
            "Trigger 1: Thesis Broken (Competitive moat erosion, structural gross margin collapse, fraudulent accounting accruals).",
            "Trigger 2: Extreme Valuation Euphoria (P/FCF > 3x historical median, pricing in mathematically impossible 10-year growth).",
            "Trigger 3: Superior Opportunity Cost (Alternative compounder of equal quality offers a 2x higher risk-adjusted expected IRR).",
            "Trigger 4: Portfolio Risk Rebalancing (A winning position has compounded to exceed the maximum risk concentration limit of 20-25%).",
            "Why Stop-Loss Rules Fail in Value Investing: Distinguishing mechanical trend-following stops from fundamental business appraisal.",
            "The Immediate Liquidation Rule: Why broken thesis stocks must be sold 100% immediately without hoping to 'get back to even'.",
            "Intel Corp. (INTC) Thesis Breakdown Case Study: How loss of manufacturing process leadership triggered a necessary fundamental exit."
        ),
        fullNotesOverview = """
            Selling is the most emotionally fraught decision in finance. When a stock falls, loss aversion tempts investors to hold onto broken businesses hoping to 'get back to even.' When a stock rises, greed tempts investors to sell elite compounders prematurely to lock in trivial gains.
            
            To achieve institutional performance, you must eliminate discretion and implement rule-based exit triggers:
            1. Trigger 1 (Thesis Broken): If the core reason you bought the company is invalidated—gross margins collapse structurally, customer churn is permanent, or accounting manipulation emerges—SELL 100% IMMEDIATELY. Do not hesitate, do not negotiate.
            2. Trigger 2 (Extreme Valuation Mania): If market euphoria pushes valuation to extremes (e.g. 100x FCF with 8% growth), the prospective 5-year return turns negative. Trim 30% to 50% to lock in capital and rebalance risk.
            3. Trigger 3 (Superior Opportunity Cost): Capital is scarce. If you identify an alternative investment with equal moat quality offering double the risk-adjusted IRR, recycle capital into the higher-conviction asset.
            4. Trigger 4 (Portfolio Concentration Rebalancing): If a multi-bagger grows to represent >25% of your total net worth, prudence mandates trimming back to your target allocation ceiling.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Rule-Based Exit Triggers" to "Objective mathematical criteria governing when capital must be liquidated or trimmed.",
            "Thesis Invalidation" to "The permanent structural impairment of a company's competitive moat or economic engine.",
            "Valuation Detachment" to "When share prices rise so far beyond intrinsic value that future prospective returns approach zero.",
            "Opportunity Cost Recycling" to "Reallocating capital from a fully valued holding into an undervalued compounder with superior IRR.",
            "Sunk Cost Fallacy" to "Refusing to sell a broken stock because selling would formally crystallize an emotional paper loss."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Believing in 10% monthly compounding leads to frantic selling: taking fast profits after small 5% bumps and placing tight 8% trailing stop-losses. This guarantees you will be whipsawed out of every generational winner during standard market volatility. True wealth compounding requires holding winning compounders for 5 to 10+ years, selling only when fundamental thesis reality dictates.
        """.trimIndent(),
        fictionalExample = """
            EmotionalBagholder vs. RuleBasedSeller:
            Both own LegacyTech Inc at $100/share:
            • Over 3 years, LegacyTech's market share collapses from 45% to 18% as open-source rivals disrupt its software. Gross margins drop from 68% to 42%, and Free Cash Flow turns negative. The stock drops to $60.
            • RuleBasedSeller runs the quarterly audit: Moat broken, pricing power gone. Trigger 1 fires. He liquidates 100% of his position at $60. He reallocates the proceeds into CloudLeader Inc at $120. Over the next 3 years, CloudLeader doubles to $240, fully restoring his capital!
            • EmotionalBagholder refuses to sell: 'I'll sell when it gets back to $100.' LegacyTech continues its spiral, breaches debt covenants, and drops to $8. EmotionalBagholder loses 92% of his wealth!
        """.trimIndent(),
        realStockExample = """
            Intel Corporation (NASDAQ: INTC, 2018 - 2023 Thesis Breakdown Case Study):
            • Original Thesis: World's undisputed semiconductor manufacturing leader with massive x86 server and PC monopolistic cash flows.
            • The Breakdown (2018-2020): Intel delayed its 10nm and 7nm process nodes repeatedly, allowing rival TSMC and fabless competitor AMD to seize manufacturing supremacy and data center market share.
            • Financial Evidence: Gross margins plummeted from 64% (2018) to under 40% (2023). Free Cash Flow collapsed from +$14B to -$11B (severe cash burn).
            • The Rule-Based Exit: Disciplined investors recognized that the technological moat was fundamentally broken and sold in the $50-$55 range. Investors who stubbornly held onto nostalgic memories saw the stock plunge below $20.
        """.trimIndent(),
        commonMistakes = listOf(
            "Holding Broken Businesses to 'Get Back to Even'" to "Refusing to accept a 20% loss on a structurally impaired business, turning a manageable loss into an 80% catastrophe.",
            "Selling Winners on Arbitrary Trailing Stops" to "Placing tight mechanical stop-losses that trigger during normal market volatility, selling world-class compounders.",
            "Selling Because the Stock Price Dropped 10%" to "Allowing short-term market quotations to dictate exit decisions rather than audited corporate earnings.",
            "Ignoring Competitive Moat Erosion" to "Downplaying market share losses and margin compression as temporary macro noise.",
            "Failing to Pre-Write Invalidation Triggers" to "Deciding when to sell during an emotional panic rather than referring to a pre-trade written contract."
        ),
        goldenRules = listOf(
            "The Immediate Invalidation Exit: If a company's fundamental thesis is broken, sell 100% of the position immediately without waiting for price recovery.",
            "The Anti-Arbitrary Stop Mandate: Never exit an investment purely because the share price declined by a fixed percentage.",
            "The Opportunity Cost Principle: Only sell an intact, fairly valued compounder if you have identified an alternative of equal quality offering a higher IRR.",
            "The Concentration Ceiling Rule: Trim positions back to your target allocation ceiling when a multi-bagger grows to represent >25% of total portfolio equity."
        )
    ),

    FullLessonDefinition(
        lessonNumber = 40,
        stageNumber = 8,
        stageTitle = "Stage 8 — Real Investing Process",
        title = "Build My Complete US Stock Investing System",
        icon = Icons.Rounded.School,
        coreQuestion = "How do we unify the entire 40-lesson curriculum into a repeatable, lifelong, closed-loop investing operating engine?",
        primaryFormula = "Master System = Universe -> PIT Data -> Fundamentals -> Factor Scoring -> Intrinsic Valuation -> Kelly Sizing -> Execution -> Audit",
        researcherRule = "The ultimate investing objective: DATA -> UNDERSTANDING -> HYPOTHESIS -> TEST -> EVIDENCE -> DECISION -> AUDIT. You are no longer a retail speculator; you are an evidence-based capital allocator operating a professional quantitative fundamental system.",
        whatDataDoesNotProve = "No investing system can eliminate market volatility or guarantee a profit on every trade. A sound system guarantees that the laws of probability and compound interest are permanently stacked in your favor over a lifetime.",
        assetInfographicPath = "file:///android_asset/learnings/lesson_40_infographic.jpg",
        keyCheckpoints = listOf(
            "The 10-Step Master Systematic Investing Pipeline: From universe filtering to post-trade performance attribution.",
            "Step 1 & 2 (Universe & PIT Data): Screening liquid US equities (> $50M daily volume) using Point-in-Time financial statements.",
            "Step 3 & 4 (Fundamentals & Quality): Calculating ROIC, DuPont ROE, FCF Conversion, and Sloan Accruals to eliminate zombies.",
            "Step 5 & 6 (Factor Scoring & Valuation): Synthesizing multi-factor z-scores and computing DCF intrinsic values with 20-30% Margin of Safety.",
            "Step 7 & 8 (Sizing & Risk Management): Allocating capital via Half-Kelly position sizing across 15-25 diversified holdings.",
            "Step 9 & 10 (Execution & Attribution): Staged 3-tranche buying, quarterly 10-Q scorecard audits, and benchmark tracking against the S&P 500.",
            "The Quantitative Invalidation Journal: Maintaining an audited record of every hypothesis, trade rationale, and post-exit review.",
            "Graduation to Lifelong Capital Allocator: The psychological transition to unemotional, evidence-based investing mastery."
        ),
        fullNotesOverview = """
            Congratulations on completing the complete 40-Lesson Stocktics Master Learning System.
            
            You have journeyed through eight comprehensive stages:
            - Stage 1: Stock Market Foundations & Accounting Mechanics
            - Stage 2: Quality & Moat Architecture
            - Stage 3: Valuation Modeling & Intrinsic Value
            - Stage 4: Quantitative Factor Models
            - Stage 5: Data Science, Anomaly Hunting & Data Hygiene
            - Stage 6: Backtesting Engine Architecture & Bias Defense
            - Stage 7: Portfolio Construction & Risk Sizing
            - Stage 8: Real Investing Process & Execution
            
            You have acquired the analytical toolkit of institutional asset managers. You no longer gamble on hype, tips, or fear. You inspect audited 10-Ks, calculate economic moats, isolate Free Cash Flow, demand margins of safety, and manage risk with mathematical precision. Adhere to your system with unyielding discipline, and let compound interest build generational wealth.
        """.trimIndent(),
        conceptsSummary = mapOf(
            "Closed-Loop Operating System" to "A continuous, self-correcting investment framework integrating screening, valuation, sizing, and post-trade audits.",
            "Point-in-Time Pipeline" to "Executing all fundamental scoring strictly on data that was legally published prior to the decision date.",
            "Multi-Factor Synthesis" to "Combining Value, Momentum, Quality, and Solvency to generate balanced, market-beating decile baskets.",
            "The Half-Kelly Fortress" to "Scaling position sizes conservatively to maximize long-term wealth growth while preventing risk of ruin.",
            "Performance Attribution" to "Decomposing total portfolio returns into factor alpha, sector exposure, and broad market beta."
        ),
        compoundingRealityCheck = """
            Compounding Expectation Reality Check:
            Seeking 10% per month implies:
            (1 + 0.10)^12 - 1 = 213.84% Annual CAGR.
            Reflect upon the greatest investors in human history: Warren Buffett compounded at 20% annually over 55 years; Jim Simons' Medallion Fund compounded at ~39% net of fees; Peter Lynch compounded at 29% annually. None achieved 213% per year. Wealth is created not through reckless get-rich-quick gambles, but through the patient, inexorable compounding of 15% to 25% annual returns sustained over decades. Master the system, preserve your capital, and let time do the heavy lifting.
        """.trimIndent(),
        fictionalExample = """
            The Master Operating System in Action:
            An investor executes the complete 40-lesson pipeline:
            1. Universe: S&P 500 (500 stocks, daily volume > $100M).
            2. Hygiene Filter: Eliminates look-ahead bias; removes delisted survivorship ghosts.
            3. Quality Screen: Retains 150 companies with ROIC > 15%, Net Debt/EBITDA < 2.0x, and negative Sloan accruals.
            4. Factor Ranking: Scores remaining stocks across Value, Momentum, and Quality; selects top decile (15 stocks).
            5. Valuation Filter: DCF models reveal 6 stocks trading at >= 20% Margin of Safety.
            6. Sizing: Allocates capital using Half-Kelly weights (5% - 8% per position), holding 15% dry powder cash.
            7. Monitoring: Audits quarterly 10-Qs; holds intact compounders; exits broken thesis positions immediately.
            Result: The portfolio generates steady alpha with 30% lower maximum drawdown than the benchmark index!
        """.trimIndent(),
        realStockExample = """
            Berkshire Hathaway & Renaissance Technologies (The Two Paradigms of Master Investing):
            • The Fundamental Master (Warren Buffett / Charlie Munger): Focused on Stages 1-3 & Stage 8. Identified wide-moat compounders (Apple, American Express, Coca-Cola), demanded conservative margins of safety, and held them for decades without interrupting compounding.
            • The Quantitative Master (Jim Simons / Renaissance Technologies): Focused on Stages 4-7. Built automated pipelines, factor models, statistical anomaly discovery, and strict position sizing.
            • The Synthesis (Stocktics): Unifies the depth of fundamental forensic analysis with the mathematical rigor of quantitative factor models and portfolio sizing.
        """.trimIndent(),
        commonMistakes = listOf(
            "Abandoning the System After Short-Term Drawdowns" to "Ditching a mathematically validated strategy during a normal cyclical factor underperformance period.",
            "Returning to Discretionary Emotional Trading" to "Overriding the systematic model because of sensationalist media headlines or personal hunches.",
            "Neglecting the Invalidation Journal" to "Failing to document trade rationale and errors, preventing continuous learning and self-correction.",
            "Over-Leveraging Portfolio Holdings" to "Introducing margin debt that creates liquidation vulnerability during severe market flash crashes.",
            "Believing You Have Outgrown the Basics" to "Neglecting balance sheet solvency checks because a company appears to have an invincible narrative."
        ),
        goldenRules = listOf(
            "The Closed-Loop Creed: DATA -> UNDERSTANDING -> HYPOTHESIS -> TEST -> EVIDENCE -> DECISION -> AUDIT.",
            "The System Integrity Mandate: Never manually override your systematic rules based on emotional market fear or greed.",
            "The Lifelong Journal Rule: Maintain an audited Quantitative Journal recording the exact thesis, Margin of Safety, and post-mortem of every trade.",
            "The Compounding Patience Law: Respect the math of compound interest. A 15% to 20% annual return sustained over 25 years transforms modest savings into extraordinary generational wealth."
        )
    )
)
