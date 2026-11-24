package com.stocktics.presentation.learning

import androidx.compose.material.icons.automirrored.rounded.ArrowBack

import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stocktics.data.db.StockticsDatabase
import com.stocktics.data.session.UserSessionManager
import com.stocktics.data.stocks.DeepStockResearchPayload
import com.stocktics.data.stocks.StatementMetricRow
import com.stocktics.data.stocks.StockSearchItem
import com.stocktics.data.stocks.YahooFinanceClient
import com.stocktics.ui.components.BadgeVariant
import com.stocktics.ui.components.StockCompanyLogo
import com.stocktics.ui.components.StockticsBadge
import com.stocktics.ui.components.StockticsCard
import com.stocktics.ui.components.StockticsMetricRow
import com.stocktics.ui.components.StockticsPrimaryButton
import com.stocktics.ui.components.StockticsSecondaryButton
import com.stocktics.ui.theme.FintechBackground
import com.stocktics.ui.theme.FintechBlue
import com.stocktics.ui.theme.FintechBlueDark
import com.stocktics.ui.theme.FintechBorder
import com.stocktics.ui.theme.FintechPrimary
import com.stocktics.ui.theme.FintechSurface
import com.stocktics.ui.theme.FintechSurfaceElevated
import com.stocktics.ui.theme.IsDarkTheme
import com.stocktics.ui.theme.GainGreen
import com.stocktics.ui.theme.LossRed
import com.stocktics.ui.theme.TextPrimary
import com.stocktics.ui.theme.TextSecondary
import com.stocktics.ui.theme.TextTertiary
import com.stocktics.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


data class LessonFormulaItem(
    val title: String,
    val formula: String,
    val example: String
)

private fun getLessonFormulas(lessonNumber: Int, fallbackRaw: String): List<LessonFormulaItem> {
    return when (lessonNumber) {
        1 -> listOf(
            LessonFormulaItem(
                title = "Market Capitalization",
                formula = "Market Cap = Share Price (P) × Shares Outstanding (N)",
                example = "Example: $228.00/share × 15.1B shares = $3.45 Trillion total valuation"
            ),
            LessonFormulaItem(
                title = "Relative Bid-Ask Spread",
                formula = "Relative Spread % = ((Ask - Bid) / Midpoint Price) × 100",
                example = "Example: Bid $228.01, Ask $228.02 (Mid $228.015) → Spread = 0.0044% (friction cost)"
            ),
            LessonFormulaItem(
                title = "Execution Slippage",
                formula = "Slippage % = ((Execution VWAP - Quoted Ask) / Quoted Ask) × 100",
                example = "Example: Quoted $50.05, market sweep fills at $50.137 VWAP → Slippage = 0.17% ($0.087/share loss)"
            )
        )
        2 -> listOf(
            LessonFormulaItem(
                title = "Balance Sheet Identity",
                formula = "Total Assets = Total Liabilities + Stockholders' Equity",
                example = "Example: $512B Assets = $243B Debt/Liabilities + $269B Stockholders' Equity (Net Worth)"
            ),
            LessonFormulaItem(
                title = "Gross Profit",
                formula = "Gross Profit = Revenue - Cost of Goods Sold (COGS)",
                example = "Example: $245.1B Revenue - $74.1B COGS = $171.0B Gross Profit (69.8% Gross Margin)"
            ),
            LessonFormulaItem(
                title = "Free Cash Flow (FCF)",
                formula = "Free Cash Flow = Cash from Operations (CFO) - CapEx",
                example = "Example: $118.5B CFO - $44.5B CapEx = $74.1B pure cash generated for owners"
            ),
            LessonFormulaItem(
                title = "FCF Conversion Rate",
                formula = "FCF Conversion % = (Free Cash Flow / Net Income) × 100",
                example = "Example: $74.1B FCF / $88.1B Net Income = 84.1% Conversion (>80% indicates elite cash earnings)"
            )
        )
        3 -> listOf(
            LessonFormulaItem(
                title = "Year-over-Year (YoY) Growth",
                formula = "YoY Growth % = ((Revenue_t - Revenue_{t-1}) / Revenue_{t-1}) × 100",
                example = "Example: ($60.9B FY24 - $27.0B FY23) / $27.0B = +125.9% YoY expansion (NVIDIA)"
            ),
            LessonFormulaItem(
                title = "Compound Annual Growth Rate (CAGR)",
                formula = "CAGR = (Revenue_n / Revenue_0)^(1/n) - 1",
                example = "Example: ($130.5B / $16.7B)^(1/4) - 1 = 67.2% annualized compound growth over 4 years"
            ),
            LessonFormulaItem(
                title = "Net Retention Rate (NRR / DBNRR)",
                formula = "NRR % = ((Start ARR + Expansion - Contraction - Churn) / Start ARR) × 100",
                example = "Example: ($100M + $45M - $0M - $5M) / $100M = 140.0% (>120% signifies elite SaaS compounding)"
            ),
            LessonFormulaItem(
                title = "Revenue Acceleration / Deceleration",
                formula = "Delta g = Growth Rate_t - Growth Rate_{t-1}",
                example = "Example: +125.9% FY24 - (+0.2% FY23) = +125.7% acceleration (triggers multiple expansion)"
            )
        )
        4 -> listOf(
            LessonFormulaItem(
                title = "Gross Profit Margin",
                formula = "Gross Margin % = ((Revenue - COGS) / Revenue) × 100",
                example = "Example: ($391.0B Rev - $210.4B COGS) / $391.0B = 46.2% pricing power margin (Apple)"
            ),
            LessonFormulaItem(
                title = "Operating Profit Margin (EBIT)",
                formula = "Operating Margin % = (Operating Income / Revenue) × 100",
                example = "Example: $123.2B EBIT / $391.0B Revenue = 31.5% core operational efficiency"
            ),
            LessonFormulaItem(
                title = "Net Profit Margin",
                formula = "Net Margin % = (Net Income / Revenue) × 100",
                example = "Example: $93.7B Net Income / $391.0B Revenue = 24.0% final bottom-line conversion"
            ),
            LessonFormulaItem(
                title = "Degree of Operating Leverage (DOL)",
                formula = "DOL = (% Delta EBIT) / (% Delta Revenue) = Gross Profit / EBIT",
                example = "Example: +64.4% EBIT growth / +33.3% Rev growth = 1.94x leverage multiplier (Apple FY21)"
            )
        )
        5 -> listOf(
            LessonFormulaItem(
                title = "Diluted Earnings Per Share (EPS)",
                formula = "Diluted EPS = (Net Income - Preferred Dividends) / Weighted Diluted Shares",
                example = "Example: ($39.10B - $0) / 2,543M shares = $15.19 per share (Meta FY24)"
            ),
            LessonFormulaItem(
                title = "Sloan Accrual Quality Ratio",
                formula = "Sloan Ratio = (Net Income - Operating Cash Flow (CFO)) / Total Assets",
                example = "Example: ($39.1B - $71.1B CFO) / $229.6B Assets = -0.139 (< 0 proves high cash quality)"
            ),
            LessonFormulaItem(
                title = "Net Share Buyback Accretion",
                formula = "Net Buyback Yield % = ((Shares_{t-1} - Shares_t) / Shares_{t-1}) × 100",
                example = "Example: (2,854M - 2,543M) / 2,854M = +10.9% permanent share count reduction"
            ),
            LessonFormulaItem(
                title = "Stock-Based Compensation Drag",
                formula = "SBC Drag % = (Stock-Based Comp Expense / Total Revenue) × 100",
                example = "Example: $14.12B SBC / $160.5B Revenue = 8.8% dilution cost to existing owners"
            )
        )
        6 -> listOf(
            LessonFormulaItem(
                title = "Free Cash Flow (FCF)",
                formula = "Free Cash Flow = Cash from Operations (CFO) - CapEx",
                example = "Example: $125.2B CFO - $52.5B CapEx = $72.7B pure unencumbered cash (Alphabet FY24)"
            ),
            LessonFormulaItem(
                title = "FCF Conversion Rate",
                formula = "FCF Conversion % = (Free Cash Flow / Net Income) × 100",
                example = "Example: $69.5B FCF / $73.8B Net Income = 94.2% (>80% indicates elite cash conversion)"
            ),
            LessonFormulaItem(
                title = "FCF Margin",
                formula = "FCF Margin % = (Free Cash Flow / Total Revenue) × 100",
                example = "Example: $72.7B FCF / $350.0B Revenue = 20.8% pure cash generation margin"
            ),
            LessonFormulaItem(
                title = "CapEx Reinvestment Intensity",
                formula = "CapEx Intensity % = (Capital Expenditures / Operating Cash Flow) × 100",
                example = "Example: $52.5B CapEx / $125.2B CFO = 41.9% of cash plowed into AI data centers"
            )
        )
        7 -> listOf(
            LessonFormulaItem(
                title = "Net Debt",
                formula = "Net Debt = Total Debt - Cash & Cash Equivalents",
                example = "Example: $106.6B Debt - $65.3B Cash = $41.3B Net Debt (< $0 = Net Cash Fortress)"
            ),
            LessonFormulaItem(
                title = "Debt-to-EBITDA Multiple",
                formula = "Debt / EBITDA = Total Debt / Normalized EBITDA",
                example = "Example: $106.6B Debt / $134.7B EBITDA = 0.79x (< 2.0x indicates low credit risk)"
            ),
            LessonFormulaItem(
                title = "Debt Payoff Horizon (Debt / FCF)",
                formula = "Debt Payoff Horizon (Years) = Total Debt / Free Cash Flow",
                example = "Example: $106.6B Debt / $108.8B FCF = 0.98 Years to fully extinguish debt"
            ),
            LessonFormulaItem(
                title = "Interest Coverage Ratio",
                formula = "Interest Coverage = Operating Income (EBIT) / Interest Expense",
                example = "Example: $123.2B EBIT / $3.94B Interest = 31.3x (> 5.0x safe during rate hikes)"
            ),
            LessonFormulaItem(
                title = "The Altman Z-Score",
                formula = "Altman Z = 1.2(WC/TA) + 1.4(RE/TA) + 3.3(EBIT/TA) + 0.6(MC/TL) + 0.999(Sales/TA)",
                example = "Example: Z > 2.99 Safe Zone | 1.81 - 2.99 Grey Zone | Z < 1.81 Distress Zone"
            )
        )
        8 -> listOf(
            LessonFormulaItem(
                title = "Return on Invested Capital (ROIC)",
                formula = "ROIC % = (NOPAT / Invested Capital) × 100",
                example = "Example: $93.5B NOPAT / $108.4B Invested Capital = 86.3% (Apple FY24)"
            ),
            LessonFormulaItem(
                title = "3-Stage DuPont Identity (ROE)",
                formula = "ROE = Net Margin × Asset Turnover × Equity Multiplier",
                example = "Example: 24.0% Net Margin × 1.07x Turnover × 5.45x Leverage = 139.9% ROE"
            ),
            LessonFormulaItem(
                title = "Net Operating Profit After Tax (NOPAT)",
                formula = "NOPAT = EBIT × (1 - Effective Tax Rate)",
                example = "Example: $123.2B EBIT × (1 - 0.241) = $93.5B pure after-tax operating earnings"
            ),
            LessonFormulaItem(
                title = "Return on Assets (ROA)",
                formula = "ROA % = (Net Income / Total Assets) × 100",
                example = "Example: $93.7B Net Income / $365.0B Assets = 25.7% total asset productivity"
            ),
            LessonFormulaItem(
                title = "Economic Value Added (EVA Spread)",
                formula = "EVA Spread % = ROIC - WACC Hurdle Rate",
                example = "Example: 86.3% ROIC - 8.5% WACC = +77.8% (> +500 bps defines elite value creators)"
            )
        )
        9 -> listOf(
            LessonFormulaItem(
                title = "Gross Margin Volatility (Pricing Power)",
                formula = "σ(GM) = sqrt((1/N) × Σ(GM_t - GM_mean)²)",
                example = "Example: σ(GM) = 0.95% < 2.0% proves flawless pricing power through inflation shocks (Visa Inc.)"
            ),
            LessonFormulaItem(
                title = "Metcalfe's Law (Network Effect Value)",
                formula = "Network Value (V) ∝ n² (Scales quadratically with connected participants)",
                example = "Example: 4.5B cardholders × 150M merchants creates insurmountable two-sided adoption lock-in"
            ),
            LessonFormulaItem(
                title = "Scale Moat & OpEx Dilution",
                formula = "OpEx Efficiency Ratio = (SG&A + R&D) / Total Revenue ↓",
                example = "Example: Overhead diluted over $250B+ revenue creates an unbreachable cost moat (Costco/Amazon)"
            ),
            LessonFormulaItem(
                title = "Customer Concentration Risk Gate",
                formula = "Top Single Customer % of Revenue < 10.0% (SEC Form 10-K Footnote Disclosure)",
                example = "Example: Top customer = 4.8% (< 10%) protects against monopsonistic buyer squeeze"
            ),
            LessonFormulaItem(
                title = "5-Year ROIC Moat Persistence",
                formula = "Moat Persistence = Count(ROIC_t ≥ 15.0%) ≥ 5 consecutive years",
                example = "Example: 5-year average ROIC of 31.4% verifies durable structural insulation from competition"
            )
        )
        10 -> listOf(
            LessonFormulaItem(
                title = "Warren Buffett's $1 Retained Earnings Test",
                formula = "Buffett Ratio = Δ Market Cap / Cumulative Retained Earnings ≥ $1.00",
                example = "Example: Apple created $1,470B market value on $369.7B retained earnings = $3.98 per $1.00 retained"
            ),
            LessonFormulaItem(
                title = "Total Shareholder Yield",
                formula = "Shareholder Yield % = Cash Dividend Yield % + Net Buyback Yield %",
                example = "Example: 0.5% Dividend Yield + 2.8% Net Buyback Yield = 3.3% total annual cash returned to owners"
            ),
            LessonFormulaItem(
                title = "Net Diluted Share Cannibal Rate",
                formula = "Net Share Change % = ((Shares_t - Shares_{t-1}) / Shares_{t-1}) × 100",
                example = "Example: Diluted shares dropped from 17.53B to 15.20B (-13.3% net reduction, -3.5% CAGR)"
            ),
            LessonFormulaItem(
                title = "M&A Goodwill Risk Intensity",
                formula = "Goodwill Ratio % = (Goodwill / Total Balance Sheet Assets) × 100 < 20.0%",
                example = "Example: Apple Goodwill = 0.0% (zero write-down risk) vs GE Goodwill = 28.5% triggering $22B+ write-down"
            ),
            LessonFormulaItem(
                title = "Executive Incentive Alignment Hurdle",
                formula = "Long-Term PSUs ∝ 3-Year Return on Invested Capital (ROIC) ≥ 18.0%",
                example = "Example: SEC Form DEF 14A ties 80% of CEO stock grants to ROIC and per-share FCF, not vanity revenue"
            )
        )
        11 -> listOf(
            LessonFormulaItem(
                title = "Herfindahl-Hirschman Index (HHI)",
                formula = "HHI = Sum(s_i^2)  [Where s_i = Market Share %]",
                example = "Example: HHI = 2,650 > 2,500 defines a concentrated oligopoly with disciplined pricing umbrellas"
            ),
            LessonFormulaItem(
                title = "Relative Operating Margin Spread",
                formula = "Margin Spread % = Company Operating Margin % - Peer Median Operating Margin %",
                example = "Example: 42.1% Microsoft - 22.0% Peer Median = +20.1% structural cost & pricing superiority"
            ),
            LessonFormulaItem(
                title = "Sector Cyclicality Beta (Economic Sensitivity)",
                formula = "Beta = Cov(R_sector, R_market) / Var(R_market)",
                example = "Example: Beta = 0.85 (Defensive secular compounder) vs Beta = 1.35 (High-volatility cyclical industrial)"
            ),
            LessonFormulaItem(
                title = "Normalized Mid-Cycle Operating Profit",
                formula = "Mid-Cycle EBIT = Normalized Revenue × 7-Year Median Operating Margin %",
                example = "Example: Caterpillar normalized margin of 13.5% yields $6.8B normalized EBIT vs $13.5B peak cyclical earnings"
            ),
            LessonFormulaItem(
                title = "Operating Leverage Stress Test",
                formula = "Degree of Operating Leverage (DOL) = % Δ Operating Income / % Δ Sales Volume",
                example = "Example: High fixed-cost manufacturer with DOL = 3.5x suffers a 70% profit collapse during a 20% sales drop"
            )
        )
        12 -> listOf(
            LessonFormulaItem(
                title = "Trailing Price-to-Earnings (TTM P/E)",
                formula = "Trailing P/E = Current Share Price / Diluted TTM EPS",
                example = "Example: $192.20 Price / $8.04 Diluted EPS = 23.9x P/E multiple (Alphabet FY24)"
            ),
            LessonFormulaItem(
                title = "Forward Price-to-Earnings (NTM P/E)",
                formula = "Forward P/E = Current Share Price / Consensus Projected NTM EPS",
                example = "Example: $192.20 Price / $9.80 Projected EPS = 19.6x (shows forward multiple compression)"
            ),
            LessonFormulaItem(
                title = "Earnings Yield Inversion (E/P)",
                formula = "Earnings Yield % = (1 / P/E) × 100 = (Diluted EPS / Share Price) × 100",
                example = "Example: (1 / 23.9x) × 100 = 4.18% annual earnings return per dollar invested"
            ),
            LessonFormulaItem(
                title = "Equity Risk Premium (ERP)",
                formula = "Equity Risk Premium % = Earnings Yield % - 10-Year US Treasury Yield %",
                example = "Example: 4.18% Earnings Yield - 4.25% 10Y Yield = -0.07% (requires growth to justify equity risk)"
            ),
            LessonFormulaItem(
                title = "Peter Lynch's PEG Ratio",
                formula = "PEG Ratio = Trailing P/E / 5-Year Annual EPS Growth Rate (%)",
                example = "Example: 23.9x P/E / 22.0% EPS CAGR = 1.09x (< 1.2x indicates growth at a reasonable price)"
            )
        )

        else -> {
            val parts = fallbackRaw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            parts.mapIndexed { idx, part ->
                val title = if (part.contains("=")) part.substringBefore("=").trim() else "Formula ${idx + 1}"
                LessonFormulaItem(
                    title = title,
                    formula = part,
                    example = "Apply verified SEC financial statement or exchange order book data into this quantitative relation."
                )
            }
        }
    }
}


data class LessonFlowStep(
    val stepNumber: String = "",
    val title: String,
    val description: String,
    val valueBadge: String? = null,
    val transitionText: String? = null
)

data class LessonVisualFlow(
    val title: String,
    val subtitle: String,
    val flowBadge: String,
    val steps: List<LessonFlowStep>
)

/**
 * Only provides visual flows for lessons that have a genuine step-by-step
 * process or formula waterfall that is best explained visually.
 * Returns null for other lessons so no card is shown.
 */
fun getLessonVisualFlow(lessonNumber: Int): LessonVisualFlow? {
    return when (lessonNumber) {
        1 -> LessonVisualFlow(
            title = "THE 4-STEP JOURNEY OF YOUR BUY ORDER",
            subtitle = "What actually happens behind the scenes after you tap Buy",
            flowBadge = "Process Flow",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "You Place Order on Phone App",
                    description = "You select shares & tap Buy. Broker checks your available cash balance.",
                    valueBadge = "Instant",
                    transitionText = "Order routed via Smart Order Router (SOR)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Order Routing to Market Makers / Exchanges",
                    description = "Sent to wholesalers (Citadel / Virtu) or direct to lit exchanges (NYSE / Nasdaq).",
                    valueBadge = "Speed: < 2ms",
                    transitionText = "Matched at the National Best Price (NBBO)"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Trade Executed & Price Locked",
                    description = "Shares are bought at best available price. Trade confirmation appears on your screen.",
                    valueBadge = "Filled",
                    transitionText = "Overnight clearing via DTCC / NSCC"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Next-Day Settlement (T+1)",
                    description = "Legal digital ownership of shares is delivered to your account; cash is settled.",
                    valueBadge = "T+1 Day"
                )
            )
        )
        2 -> LessonVisualFlow(
            title = "HOW MONEY FLOWS: REVENUE TO REAL CASH",
            subtitle = "The cash waterfall showing why accounting profit is not real cash in the bank",
            flowBadge = "Cash Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Top-Line Revenue (Total Sales)",
                    description = "Total customer money brought in before deducting any expenses.",
                    valueBadge = "$245.1B",
                    transitionText = "Minus Cost to produce goods & services (-$74.1B)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Gross Profit",
                    description = "Money left after paying direct production costs. High margin = strong pricing power.",
                    valueBadge = "$171.0B",
                    transitionText = "Minus Operating costs: salaries, R&D, servers (-$61.6B)"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Operating Income (EBIT)",
                    description = "Profit generated by core business operations before interest and taxes.",
                    valueBadge = "$109.4B",
                    transitionText = "Minus Taxes & Interest (-$21.3B)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Net Income (Accounting Profit)",
                    description = "The reported bottom-line profit on paper. But this is NOT yet cash you can spend!",
                    valueBadge = "$88.1B",
                    transitionText = "Minus Real Cash spent on factories & AI data centers (CapEx -$44.5B)"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Free Cash Flow (Real Cash in Hand)",
                    description = "True physical cash surplus left in the company bank account for dividends, buybacks, or saving.",
                    valueBadge = "$74.1B"
                )
            )
        )
        3 -> LessonVisualFlow(
            title = "THE REVENUE EXPANSION & QUALITY ENGINE",
            subtitle = "How top-line sales decompose into organic compounders vs. high-risk roll-ups",
            flowBadge = "Growth Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Starting Base Revenue (Prior Year)",
                    description = "The baseline recurring foundation entering the fiscal period.",
                    valueBadge = "$27.0B",
                    transitionText = "Existing customer base renews & expands spend"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Organic Customer Expansion (NRR > 120%)",
                    description = "Pricing power and upsell volume from existing customers (Data Center GPU compute).",
                    valueBadge = "+$33.9B",
                    transitionText = "Zero customer churn / high retention moat"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "New Logo Customer Acquisition",
                    description = "Fresh enterprise clients added organically through market innovation.",
                    valueBadge = "+$70.0B",
                    transitionText = "High gross margin attachment (>75%)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "2nd-Derivative Acceleration (Delta g > 0)",
                    description = "Growth rate accelerates from +0.2% to +125.9%, triggering violent multiple expansion.",
                    valueBadge = "+125.7% Accel",
                    transitionText = "Compounding into massive enterprise scale"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Total Scaled Revenue (NVIDIA Case)",
                    description = "Top-line revenue expands to $130.5B with an elite 67.2% 4-year compound annual growth rate.",
                    valueBadge = "$130.5B"
                )
            )
        )
        4 -> LessonVisualFlow(
            title = "THE 4-TIER MARGIN & OPERATING LEVERAGE CASCADE",
            subtitle = "How top-line sales filter through cost structures into expanding profit margins",
            flowBadge = "Margin Cascade",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Total Net Sales (Revenue)",
                    description = "Gross top-line customer billings across hardware and digital ecosystem.",
                    valueBadge = "$391.0B (100%)",
                    transitionText = "Deduct Cost of Goods Sold (-$210.4B)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Gross Profit (Product Pricing Power)",
                    description = "Direct margin buffer. Expanded from 38.2% to 46.2% via Services mix.",
                    valueBadge = "$180.7B (46.2%)",
                    transitionText = "Deduct Operating Overhead: R&D & SG&A (-$57.5B)"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Operating Income (EBIT)",
                    description = "Core business earnings before debt interest and taxes. 1.94x DOL multiplier.",
                    valueBadge = "$123.2B (31.5%)",
                    transitionText = "Deduct Taxes & Financing costs (-$29.5B)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Net Income (Residual Equity Profit)",
                    description = "Final bottom-line profit left in the business for common equity owners.",
                    valueBadge = "$93.7B (24.0%)",
                    transitionText = "Fueling $100B+ annual share buybacks & dividends"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Structural Services Mix Moat",
                    description = "Services ($96.2B @ 73.9% margin) permanently elevates consolidated corporate returns.",
                    valueBadge = "+798 bps Shift"
                )
            )
        )
        5 -> LessonVisualFlow(
            title = "THE EPS DILUTION & EARNINGS QUALITY WATERFALL",
            subtitle = "How accounting profit converts into diluted per-share cash for owners",
            flowBadge = "EPS Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Reported GAAP Net Income",
                    description = "Consolidated bottom-line accounting profit on the audited Form 10-K.",
                    valueBadge = "$39.10B",
                    transitionText = "Divided by 2,500M basic shares outstanding"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Basic Earnings Per Share (EPS)",
                    description = "Initial per-share earnings before accounting for unvested employee options.",
                    valueBadge = "$15.64 / sh",
                    transitionText = "Add dilution: +43M in-the-money RSUs and options"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Diluted GAAP EPS (Treasury Method)",
                    description = "The official diluted per-share figure reported under ASC 260.",
                    valueBadge = "$15.19 / sh",
                    transitionText = "Cash reality check: CFO = $71.1B ($27.96 Cash / sh)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Sloan Quality Check (Accrual Anomaly)",
                    description = "Operating cash flow exceeds net income by 1.82x (Sloan Ratio = -0.139). Zero accounting distortion.",
                    valueBadge = "Elite Cash Backing",
                    transitionText = "Massive buybacks ($27B) offset $14.1B SBC dilution"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Net Accretive Share Shrinkage",
                    description = "Meta permanently shrunk share count by -10.9% over 4 years, delivering +50.5% EPS growth!",
                    valueBadge = "-10.9% Shares"
                )
            )
        )
        6 -> LessonVisualFlow(
            title = "THE CASH FLOW & FREE CASH CONVERSION WATERFALL",
            subtitle = "How accounting net income transforms into verified unencumbered cash for owners",
            flowBadge = "Cash Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "GAAP Net Income (Accounting Profit)",
                    description = "Accrual profit on paper, including non-cash charges and uncollected sales.",
                    valueBadge = "$100.6B",
                    transitionText = "Add back non-cash Depreciation & Amortization (+$15.3B)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Working Capital & Non-Cash Adjustments",
                    description = "Stock-based compensation add-back (+$22.5B) minus working capital absorption (-$13.2B).",
                    valueBadge = "+$24.6B Net Adj",
                    transitionText = "Reconciled to Cash Flow from Operations"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Cash from Operations (CFO)",
                    description = "Pure verified customer cash collected from running the global business.",
                    valueBadge = "$125.2B (CFO)",
                    transitionText = "Minus Capital Expenditures: AI data centers & servers (-$52.5B)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Free Cash Flow (FCF = CFO - CapEx)",
                    description = "True unencumbered surplus cash available for buybacks, dividends, and acquisitions.",
                    valueBadge = "$72.7B Pure FCF",
                    transitionText = "Deployed into share repurchases and balance sheet cash"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Share Buyback Value Return",
                    description = "Alphabet retired $62.2B in common shares in FY24, funded 100% by organic Free Cash Flow.",
                    valueBadge = "$62.2B Buybacks"
                )
            )
        )
        7 -> LessonVisualFlow(
            title = "THE BALANCE SHEET SOLVENCY & LEVERAGE WATERFALL",
            subtitle = "How liquidity cushions, debt maturities, and operating profits guarantee survival",
            flowBadge = "Solvency Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Liquid Cash & Marketable Securities",
                    description = "Immediate cash and short-term US Treasuries to survive sudden revenue shocks.",
                    valueBadge = "$65.3B Cash",
                    transitionText = "Current Assets cover near-term operational needs"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Working Capital & Short-Term Claims",
                    description = "Current Assets ($153.0B) vs Current Liabilities ($176.4B); supported by negative cash float.",
                    valueBadge = "-$23.4B Float",
                    transitionText = "Funded long-term debt structure"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Total Funded Debt Obligations",
                    description = "Commercial paper, term debt, and senior notes issued at fixed low interest coupons.",
                    valueBadge = "$106.6B Debt",
                    transitionText = "Net Debt = Total Debt - Liquid Cash ($41.3B)"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Operating Earnings Debt Service Shield",
                    description = "Apple generates $123.2B in EBIT to service only $3.94B in annual interest expense.",
                    valueBadge = "31.3x Coverage",
                    transitionText = "Debt extinguishable in < 1 year of Free Cash Flow"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Altman Z-Score Bankruptcy Fortress",
                    description = "Empirical credit score above 8.0, placing the enterprise safely in the Institutional Safe Zone.",
                    valueBadge = "Z > 8.0 Safe"
                )
            )
        )
        8 -> LessonVisualFlow(
            title = "THE DUPONT DECOMPOSITION & CAPITAL RETURN WATERFALL",
            subtitle = "How operating profitability, asset velocity, and financial structure determine ROE & ROIC",
            flowBadge = "DuPont Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Net Profit Margin (Operating Pricing Power)",
                    description = "Percentage of each revenue dollar retained after all operating costs and taxes.",
                    valueBadge = "24.0% Margin",
                    transitionText = "Multiplied by Asset Turnover (Sales / Total Assets)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Asset Turnover Velocity (Efficiency)",
                    description = "How rapidly the physical and digital balance sheet generates top-line sales volume.",
                    valueBadge = "1.07x Turnover",
                    transitionText = "Yields Return on Assets: 24.0% × 1.07 = 25.7% ROA"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Equity Multiplier (Financial Leverage)",
                    description = "Total Assets divided by Stockholders' Equity. Quantifies debt leverage amplification.",
                    valueBadge = "5.45x Multiplier",
                    transitionText = "DuPont result: ROA × Leverage Multiplier = Reported ROE"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Unmasked Return on Equity (DuPont ROE)",
                    description = "Apple's 139.9% ROE reflects elite 24% margins amplified by massive accretive buybacks.",
                    valueBadge = "139.9% ROE",
                    transitionText = "Eliminate debt distortion: NOPAT / Invested Capital"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Return on Invested Capital (ROIC)",
                    description = "Pure economic return on operating capital: generates an extraordinary +7,780 bps spread over WACC.",
                    valueBadge = "86.3% ROIC"
                )
            )
        )
        9 -> LessonVisualFlow(
            title = "THE ECONOMIC MOAT & COMPETITIVE DEFENSE WATERFALL",
            subtitle = "How structural barriers protect high ROIC from competitive profit erosion",
            flowBadge = "Moat Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Raw ROIC Excess Profit Beacon",
                    description = "High 30%+ ROIC signals extraordinary economic returns, attracting competing capital.",
                    valueBadge = "31.4% ROIC",
                    transitionText = "Protected by Two-Sided Network Effects"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Metcalfe Network Toll Booth",
                    description = "4.5B cardholders and 150M merchants form a locked network where new entrants face insurmountable friction.",
                    valueBadge = "V ∝ n² Moat",
                    transitionText = "Enforces enterprise mission-critical switching costs"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Switching Costs & Integration Lock-In",
                    description = "Enterprise workflows and payment gateways become too operationally risky to swap out for small discounts.",
                    valueBadge = "0.5% Churn",
                    transitionText = "Demonstrates pricing power across inflationary cycles"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Pricing Power & Margin Stability",
                    description = "Gross Margin volatility remains under 1.0% (σ_GM = 0.95%), proving costs are effortlessly passed through.",
                    valueBadge = "σ(GM) = 0.95%",
                    transitionText = "Scale spreads overhead over global volume"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Long-Run Economic Moat Compounding",
                    description = "Sustained ROIC > WACC spread over decades compounds intrinsic shareholder value without cyclical margin collapse.",
                    valueBadge = "Wide Moat Compounded"
                )
            )
        )
        10 -> LessonVisualFlow(
            title = "THE CAPITAL ALLOCATION & VALUE CREATION WATERFALL",
            subtitle = "How CEO capital deployment decisions mechanically compound or destroy per-share value",
            flowBadge = "Allocation Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Organic Surplus Free Cash Flow",
                    description = "Operating cash collected after maintaining core business assets and paying cash taxes.",
                    valueBadge = "$108.8B FCF",
                    transitionText = "Prioritize internal reinvestment at ROIC > WACC"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "High-ROIC Internal Reinvestment",
                    description = "R&D and targeted CapEx deployed at 30%+ ROIC to widen proprietary ecosystem moat.",
                    valueBadge = "30%+ ROIC",
                    transitionText = "Avoid expensive empire-building acquisitions"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Disciplined Bolt-On M&A Shield",
                    description = "Acquiring targeted IP and engineering teams with zero destructive mega-deal goodwill write-downs.",
                    valueBadge = "$0 Goodwill",
                    transitionText = "Channel excess cash into aggressive buybacks"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "The Buyback Cannibal Mechanism",
                    description = "Retiring 35%+ of diluted share count below intrinsic value, accelerating per-share EPS growth.",
                    valueBadge = "-13.3% Shares",
                    transitionText = "Verified via Warren Buffett's $1 Test"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Buffett $1 Retained Value Compounding",
                    description = "Created $3.98 in market capitalization for every single $1.00 of cumulative retained earnings.",
                    valueBadge = "$3.98 / $1.00"
                )
            )
        )
        11 -> LessonVisualFlow(
            title = "THE SECTOR DYNAMICS & MACRO CYCLE RESILIENCE WATERFALL",
            subtitle = "How industry structure and the macro cycle dictate operating margin survival",
            flowBadge = "Sector Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Macro Business Cycle Positioning",
                    description = "Determining whether the sector is in Early Recovery, Mid Expansion, Late Overheating, or Recession.",
                    valueBadge = "Mid-Cycle Expansion",
                    transitionText = "Auditing industry competitive structure (HHI)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Oligopoly Concentration (HHI > 2,500)",
                    description = "Concentrated market share shields participants with a pricing umbrella, avoiding price wars.",
                    valueBadge = "HHI = 2,650 Oligopoly",
                    transitionText = "Benchmarking relative margin spread vs peers"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "Relative Operating Margin Spread",
                    description = "Commanding an operating margin +2,010 bps higher than the peer median across full 10-year cycles.",
                    valueBadge = "+20.1% vs Peers",
                    transitionText = "Stress-testing operating leverage against macro shocks"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Recession Operating Leverage Defense",
                    description = "Variable cost structure and high gross margin keep trough margins safely positive during downturns.",
                    valueBadge = "39.3% Trough Margin",
                    transitionText = "Calculating normalized mid-cycle earnings power"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Secular Tailwind Intrinsic Compounding",
                    description = "Revenue and EPS compound through structural adoption (Cloud/AI) rather than cyclical commodity inflation.",
                    valueBadge = "Secular Compounder"
                )
            )
        )
        12 -> LessonVisualFlow(
            title = "THE P/E MULTIPLE & EARNINGS YIELD ARBITRAGE CASCADE",
            subtitle = "How net profit converts into valuation multiples, bond-equivalent yields, and growth hurdles",
            flowBadge = "Valuation Waterfall",
            steps = listOf(
                LessonFlowStep(
                    stepNumber = "1",
                    title = "Audited Net Income & Diluted EPS",
                    description = "Accounting bottom-line profit divided by diluted common shares outstanding.",
                    valueBadge = "$8.04 TTM EPS",
                    transitionText = "Apply market pricing multiple (Price / EPS)"
                ),
                LessonFlowStep(
                    stepNumber = "2",
                    title = "Trailing Valuation Multiple",
                    description = "Current market price divided by audited historical 12-month earnings.",
                    valueBadge = "23.9x Trailing P/E",
                    transitionText = "Invert multiple into bond-equivalent earnings yield"
                ),
                LessonFlowStep(
                    stepNumber = "3",
                    title = "The Earnings Yield Inversion",
                    description = "Translates the abstract multiple into an annual yield percentage (1 / P/E × 100).",
                    valueBadge = "4.18% Earnings Yield",
                    transitionText = "Benchmark against 10-Year US Treasury risk-free rate"
                ),
                LessonFlowStep(
                    stepNumber = "4",
                    title = "Equity Risk Premium (ERP) Hurdle",
                    description = "Spread over 10Y Treasury (4.25%); demands secular growth to compensate for equity volatility.",
                    valueBadge = "-0.07% ERP Spread",
                    transitionText = "Adjust for compounding velocity via Peter Lynch PEG"
                ),
                LessonFlowStep(
                    stepNumber = "5",
                    title = "Growth-Adjusted PEG Valuation",
                    description = "Dividing 23.9x P/E by 22% EPS growth yields 1.09x PEG (Growth At a Reasonable Price).",
                    valueBadge = "1.09x PEG (GARP)"
                )
            )
        )

        // Only return flows for topics that have a natural process/waterfall.
        // For all other lessons, return null so no card is displayed.
        else -> null
    }
}

@Composable
fun StockticsVisualFlowCard(
    visualFlow: LessonVisualFlow,
    modifier: Modifier = Modifier
) {
    StockticsCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountTree,
                        contentDescription = null,
                        tint = FintechPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = visualFlow.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = visualFlow.subtitle,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StockticsBadge(
                    text = visualFlow.flowBadge,
                    variant = BadgeVariant.INFO
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column {
                visualFlow.steps.forEachIndexed { index, step ->
                    val isLast = index == visualFlow.steps.lastIndex
                    val isFinalResult = isLast && visualFlow.steps.size > 2

                    // Step Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFinalResult) FintechPrimary.copy(alpha = 0.08f) else FintechBackground)
                            .border(
                                width = 1.dp,
                                color = if (isFinalResult) FintechPrimary.copy(alpha = 0.5f) else FintechBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Number Badge
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isFinalResult) FintechPrimary else FintechSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isFinalResult) FintechPrimary else FintechBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step.stepNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFinalResult) Color.Black else FintechPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Step Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFinalResult) FintechPrimary else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = step.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            // Value Badge (e.g. $245.1B or Instant)
                            if (step.valueBadge != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FintechSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isFinalResult) FintechPrimary else FintechBorder,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = step.valueBadge,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFinalResult) GainGreen else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Downward Transition Connector Arrow
                    if (!isLast && step.transitionText != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = FintechPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = step.transitionText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextTertiary
                            )
                        }
                    } else if (!isLast) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}


// ==============================================================================
// DEDICATED LIVE DATA LABS FOR LESSONS 1 TO 6
// ==============================================================================

@Composable
private fun Lesson1LiveLab(
    payload: DeepStockResearchPayload?,
    priceVal: Double,
    simulatedOrderShares: Float,
    onSharesChange: (Float) -> Unit
) {
    val mcStr = payload?.metrics16?.marketCap ?: "N/A"
    val bidStr = payload?.metrics16?.bid ?: "$228.01"
    val askStr = payload?.metrics16?.ask ?: "$228.02"

    StockticsMetricRow(label = "Nominal Share Price (P)", value = "$${String.format(Locale.US, "%,.2f", priceVal)}")
    StockticsMetricRow(label = "Market Capitalization (P × N)", value = mcStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Top-of-Book Best Bid", value = bidStr)
    StockticsMetricRow(label = "Top-of-Book Best Ask", value = askStr)
    StockticsMetricRow(label = "Trading Volume", value = payload?.metrics16?.volume ?: "N/A")
    StockticsMetricRow(label = "30-Day Avg Volume", value = payload?.metrics16?.avgVolume ?: "N/A")

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Limit Order Book Depth Visual Bar Chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIMIT ORDER BOOK DEPTH (L2 TICK DATA)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.5.sp
                )
                StockticsBadge(text = "CONSOLIDATED SIP", variant = BadgeVariant.MUTED)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Order Book Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Bid Size", fontSize = 9.sp, color = GainGreen, modifier = Modifier.weight(1f))
                Text("Bid Price", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Ask Price", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                Text("Ask Size", fontSize = 9.sp, color = LossRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

            // 5 Sample Depth Rows
            val baseP = if (priceVal > 0) priceVal else 228.0
            val bidDepths = listOf(
                Pair(baseP - 0.01, 1850),
                Pair(baseP - 0.02, 2400),
                Pair(baseP - 0.03, 3100),
                Pair(baseP - 0.04, 4600),
                Pair(baseP - 0.05, 6200)
            )
            val askDepths = listOf(
                Pair(baseP + 0.01, 1400),
                Pair(baseP + 0.02, 2100),
                Pair(baseP + 0.03, 2900),
                Pair(baseP + 0.04, 3900),
                Pair(baseP + 0.05, 5400)
            )

            bidDepths.indices.forEach { idx ->
                val (bPrice, bSize) = bidDepths[idx]
                val (aPrice, aSize) = askDepths[idx]
                val bWidthFrac = (bSize / 6500f).coerceIn(0.1f, 1f)
                val aWidthFrac = (aSize / 6500f).coerceIn(0.1f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bid Side Bar + Price
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bWidthFrac)
                                .height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(GainGreen.copy(alpha = 0.15f))
                        )
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Text("${bSize}", fontSize = 9.sp, color = GainGreen, modifier = Modifier.weight(1f))
                            Text("$${String.format(Locale.US, "%.2f", bPrice)}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Ask Side Price + Bar
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxWidth(aWidthFrac)
                                .height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(LossRed.copy(alpha = 0.15f))
                        )
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Text("$${String.format(Locale.US, "%.2f", aPrice)}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${aSize}", fontSize = 9.sp, color = LossRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive Order Slippage Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Interactive Order Slippage Simulator",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${simulatedOrderShares.toInt()} Shares",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPrimary
                )
            }

            Slider(
                value = simulatedOrderShares,
                onValueChange = onSharesChange,
                valueRange = 10f..5000f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = FintechPrimary,
                    activeTrackColor = FintechPrimary,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(28.dp)
            )

            val estSpreadCents = if (priceVal > 500) 0.05 else 0.01
            val estSlippagePct = (simulatedOrderShares / 10000f * estSpreadCents).coerceIn(0.001, 0.45)
            val dollarCost = simulatedOrderShares * priceVal
            val slippageLoss = dollarCost * (estSlippagePct / 100.0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order Capital: $${String.format(Locale.US, "%,.0f", dollarCost)}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Slippage Loss: ~$${String.format(Locale.US, "%.2f", slippageLoss)} (${String.format(Locale.US, "%.4f", estSlippagePct)}%)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (estSlippagePct > 0.1) WarningAmber else GainGreen
                )
            }
        }
    }
}

@Composable
private fun Lesson2LiveLab(
    payload: DeepStockResearchPayload?,
    selectedSymbol: String,
    simRevenueB: Float,
    simNetMarginPct: Float,
    simCfoRatioPct: Float,
    simCapexB: Float,
    statementTabIndex: Int,
    onTabChange: (Int) -> Unit,
    onRevenueChange: (Float) -> Unit,
    onNetMarginChange: (Float) -> Unit,
    onCfoRatioChange: (Float) -> Unit,
    onCapexChange: (Float) -> Unit
) {
    Text(
        text = "AUDITED 3-STATEMENT INSPECTOR: ${payload?.symbol ?: selectedSymbol}",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))

    // Balance Sheet Identity Equilibrium Bar Chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BALANCE SHEET IDENTITY EQUILIBRIUM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                StockticsBadge(text = "ASSETS = LIABILITIES + EQUITY", variant = BadgeVariant.GAIN)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Total Assets vs Liabilities + Equity Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.50f)
                        .background(FintechPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Total Assets: $512B (100%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.24f)
                        .background(WarningAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Debt $244B", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.26f)
                        .background(FintechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Equity $268B", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Left: Total Economic Assets ($512B)  |  Right: Funded by Debt ($244B) + Equity Net Worth ($268B)", fontSize = 9.sp, color = TextSecondary)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val statementTabs = listOf("Income Statement", "Balance Sheet", "Cash Flow")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statementTabs.forEachIndexed { idx, tabTitle ->
            val isSelected = statementTabIndex == idx
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                    .border(1.dp, if (isSelected) FintechPrimary else FintechBorder, RoundedCornerShape(8.dp))
                    .clickable { onTabChange(idx) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val activeRows = when (statementTabIndex) {
        0 -> payload?.financialStatements?.incomeStatement?.takeIf { it.isNotEmpty() } ?: listOf(
            StatementMetricRow("Total Revenue", "$ 211.9B", "$ 245.1B", "$ 278.0B", "$ 312.0B"),
            StatementMetricRow("Gross Profit", "$ 146.1B", "$ 171.0B", "$ 195.0B", "$ 220.0B"),
            StatementMetricRow("Operating Income (EBIT)", "$ 88.5B", "$ 109.4B", "$ 126.0B", "$ 144.0B"),
            StatementMetricRow("Net Income", "$ 72.4B", "$ 88.1B", "$ 101.0B", "$ 116.0B")
        )
        1 -> payload?.financialStatements?.balanceSheet?.takeIf { it.isNotEmpty() } ?: listOf(
            StatementMetricRow("Total Assets", "$ 411.9B", "$ 512.2B", "$ 560.0B", "$ 620.0B"),
            StatementMetricRow("Total Liabilities", "$ 205.8B", "$ 243.7B", "$ 260.0B", "$ 285.0B"),
            StatementMetricRow("Cash & ST Investments", "$ 111.3B", "$ 75.5B", "$ 88.0B", "$ 98.0B"),
            StatementMetricRow("Stockholders' Equity", "$ 206.2B", "$ 268.5B", "$ 300.0B", "$ 335.0B")
        )
        else -> payload?.financialStatements?.cashFlow?.takeIf { it.isNotEmpty() } ?: listOf(
            StatementMetricRow("Cash from Operations (CFO)", "$ 87.6B", "$ 118.5B", "$ 132.0B", "$ 148.0B"),
            StatementMetricRow("Capital Expenditures (CapEx)", "-$ 28.1B", "-$ 44.5B", "-$ 48.0B", "-$ 52.0B"),
            StatementMetricRow("Free Cash Flow (FCF)", "$ 59.5B", "$ 74.1B", "$ 84.0B", "$ 96.0B")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Metric Line Item", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, modifier = Modifier.weight(1.4f))
                Text(text = "FY23", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                Text(text = "FY24", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                Text(text = "FY25E", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = FintechBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))
            activeRows.take(4).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = row.metric, fontSize = 10.sp, color = TextPrimary, modifier = Modifier.weight(1.4f), maxLines = 1)
                    Text(text = row.year2023, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                    Text(text = row.year2024, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                    Text(text = row.year2025, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Interactive FCF Quality Simulator Box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val netIncomeCalc = simRevenueB * (simNetMarginPct / 100f)
            val cfoCalc = netIncomeCalc * (simCfoRatioPct / 100f)
            val fcfCalc = cfoCalc - simCapexB
            val fcfConversionPct = if (netIncomeCalc > 0) (fcfCalc / netIncomeCalc) * 100f else 0f
            val accrualSpread = cfoCalc - netIncomeCalc

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FCF & Sloan Accrual Simulator",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Test cash conversion vs reported earnings",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }

                StockticsBadge(
                    text = if (fcfConversionPct >= 80f) "ELITE FCF" else if (fcfConversionPct >= 50f) "MODERATE" else "HIGH ACCRUAL",
                    variant = if (fcfConversionPct >= 80f) BadgeVariant.GAIN else if (fcfConversionPct >= 50f) BadgeVariant.NEUTRAL else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Revenue
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Revenue", fontSize = 10.sp, color = TextSecondary)
                Text(text = "$${simRevenueB.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simRevenueB,
                onValueChange = onRevenueChange,
                valueRange = 10f..400f,
                steps = 38,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: Net Margin %
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Net Profit Margin", fontSize = 10.sp, color = TextSecondary)
                Text(text = "${simNetMarginPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simNetMarginPct,
                onValueChange = onNetMarginChange,
                valueRange = 5f..50f,
                steps = 44,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 3: CFO / Net Income Ratio %
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Cash from Ops / Net Income (CFO/NI)", fontSize = 10.sp, color = TextSecondary)
                Text(text = "${simCfoRatioPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simCfoRatioPct >= 100f) GainGreen else WarningAmber)
            }
            Slider(
                value = simCfoRatioPct,
                onValueChange = onCfoRatioChange,
                valueRange = 30f..180f,
                steps = 29,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 4: CapEx
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Capital Expenditures (CapEx)", fontSize = 10.sp, color = TextSecondary)
                Text(text = "$${simCapexB.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LossRed)
            }
            Slider(
                value = simCapexB,
                onValueChange = onCapexChange,
                valueRange = 0f..80f,
                steps = 39,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Net Income", fontSize = 9.sp, color = TextSecondary)
                    Text(text = "$${String.format(Locale.US, "%.1f", netIncomeCalc)}B", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Cash from Ops (CFO)", fontSize = 9.sp, color = TextSecondary)
                    Text(text = "$${String.format(Locale.US, "%.1f", cfoCalc)}B", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Free Cash Flow (FCF)", fontSize = 9.sp, color = TextSecondary)
                    Text(
                        text = "$${String.format(Locale.US, "%.1f", fcfCalc)}B (${fcfConversionPct.toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (fcfCalc >= 0) GainGreen else LossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Accrual Quality: ${if (accrualSpread >= 0) "CFO exceeds earnings by +$${String.format(Locale.US, "%.1f", accrualSpread)}B (Conservative Accounting)" else "CFO lags Net Income by -$${String.format(Locale.US, "%.1f", -accrualSpread)}B (Aggressive Accrual Risk)"}",
                fontSize = 9.sp,
                color = if (accrualSpread >= 0) GainGreen else LossRed
            )
        }
    }
}

@Composable
private fun Lesson3LiveLab(
    payload: DeepStockResearchPayload?,
    simOrganicGrowthPct: Float,
    simAcquiredGrowthPct: Float,
    simChurnPct: Float,
    onOrganicChange: (Float) -> Unit,
    onAcquiredChange: (Float) -> Unit,
    onChurnChange: (Float) -> Unit
) {
    // Topline Metrics for Lesson 3
    val revRow = payload?.financialStatements?.incomeStatement?.find { it.metric.contains("Revenue", ignoreCase = true) }
    val revTTM = revRow?.year2024 ?: "$245.1B"
    val psRatio = payload?.valuationStats?.find { it.first.contains("Price / Sales", ignoreCase = true) }?.second ?: "7.8x"
    val evRev = payload?.valuationStats?.find { it.first.contains("EV / Revenue", ignoreCase = true) }?.second ?: "7.5x"
    val yoyRev = payload?.profitabilityStats?.find { it.first.contains("Revenue Growth", ignoreCase = true) }?.second ?: "+15.7%"

    StockticsMetricRow(label = "Total Revenue (TTM Topline)", value = revTTM, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Quarterly Revenue Growth (YoY)", value = yoyRev, valueColor = GainGreen)
    StockticsMetricRow(label = "3-Year Audited Revenue CAGR", value = "+14.8%")
    StockticsMetricRow(label = "Price-to-Sales (P/S Multiple)", value = psRatio)
    StockticsMetricRow(label = "Enterprise Value / Revenue (EV/Sales)", value = evRev)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. 4-Year Revenue Growth Bar Chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("4-YEAR REVENUE EXPANSION TRAJECTORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text("Audited SEC History & Forward Consensus", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(text = "CAGR +14.8%", variant = BadgeVariant.GAIN)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Vertical Bars
            val years = listOf("FY22", "FY23", "FY24", "FY25E")
            val revVals = listOf(
                Pair(revRow?.year2023?.replace("$", "")?.replace("B", "")?.trim()?.toDoubleOrNull() ?: 211.9, revRow?.year2023 ?: "$211.9B"),
                Pair(revRow?.year2024?.replace("$", "")?.replace("B", "")?.trim()?.toDoubleOrNull() ?: 245.1, revRow?.year2024 ?: "$245.1B"),
                Pair(revRow?.year2025?.replace("$", "")?.replace("B", "")?.trim()?.toDoubleOrNull() ?: 278.0, revRow?.year2025 ?: "$278.0B"),
                Pair(revRow?.year2026?.replace("$", "")?.replace("B", "")?.trim()?.toDoubleOrNull() ?: 312.0, revRow?.year2026 ?: "$312.0B")
            )
            val maxRev = revVals.maxOf { it.first }.coerceAtLeast(10.0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                years.indices.forEach { idx ->
                    val (v, label) = revVals[idx]
                    val heightFrac = (v / maxRev).toFloat().coerceIn(0.2f, 1f)
                    val yoyPill = if (idx == 0) "Base" else if (idx == 1) "+15.7%" else if (idx == 2) "+13.4%" else "+12.2%"

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(yoyPill, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (idx > 0) GainGreen else TextTertiary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight(heightFrac * 0.75f)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (idx == 2) FintechPrimary else FintechPrimary.copy(alpha = 0.65f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(years[idx], fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Organic vs Inorganic Growth Decomposition Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val netGrowth = (simOrganicGrowthPct + simAcquiredGrowthPct - simChurnPct).coerceAtLeast(0f)
            val organicRatio = if (netGrowth > 0) (simOrganicGrowthPct / (simOrganicGrowthPct + simAcquiredGrowthPct) * 100f) else 100f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Growth Quality Decomposition", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Organic Expansion vs Acquired M&A Drag", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (organicRatio >= 70f) "ORGANIC MOAT" else "M&A ROLLUP",
                    variant = if (organicRatio >= 70f) BadgeVariant.GAIN else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sliders
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Base Organic Growth", fontSize = 10.sp, color = TextSecondary)
                Text("+${simOrganicGrowthPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simOrganicGrowthPct,
                onValueChange = onOrganicChange,
                valueRange = 0f..35f,
                steps = 34,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("M&A Acquired Revenue", fontSize = 10.sp, color = TextSecondary)
                Text("+${simAcquiredGrowthPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simAcquiredGrowthPct,
                onValueChange = onAcquiredChange,
                valueRange = 0f..25f,
                steps = 24,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Customer Churn / Contraction", fontSize = 10.sp, color = TextSecondary)
                Text("-${simChurnPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LossRed)
            }
            Slider(
                value = simChurnPct,
                onValueChange = onChurnChange,
                valueRange = 0f..15f,
                steps = 14,
                colors = SliderDefaults.colors(thumbColor = LossRed, activeTrackColor = LossRed, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stacked Bar Representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechBackground)
            ) {
                val totalParts = (simOrganicGrowthPct + simAcquiredGrowthPct + simChurnPct).coerceAtLeast(1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(simOrganicGrowthPct.coerceAtLeast(0.1f) / totalParts)
                        .background(GainGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Organic ${simOrganicGrowthPct.toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(simAcquiredGrowthPct.coerceAtLeast(0.1f) / totalParts)
                        .background(FintechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M&A ${simAcquiredGrowthPct.toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(simChurnPct.coerceAtLeast(0.1f) / totalParts)
                        .background(LossRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Churn ${simChurnPct.toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net Topline Growth Rate", fontSize = 10.sp, color = TextSecondary)
                Text("+${String.format(Locale.US, "%.1f", netGrowth)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Text(
                text = if (organicRatio >= 70f) "High Organic Ratio (${organicRatio.toInt()}%): High customer retention justifies premium P/S multiple." else "Low Organic Ratio (${organicRatio.toInt()}%): Relying on debt/equity dilution to purchase revenue.",
                fontSize = 9.sp,
                color = if (organicRatio >= 70f) GainGreen else WarningAmber
            )
        }
    }
}

@Composable
private fun Lesson4LiveLab(
    payload: DeepStockResearchPayload?,
    simDolRevChangePct: Float,
    simFixedCostRatioPct: Float,
    onDolRevChange: (Float) -> Unit,
    onFixedCostRatioChange: (Float) -> Unit
) {
    // Profitability Metrics for Lesson 4
    val grossMargin = "46.2%"
    val ebitdaMargin = "33.8%"
    val opMargin = payload?.profitabilityStats?.find { it.first.contains("Operating Margin", ignoreCase = true) }?.second ?: "30.7%"
    val netMargin = payload?.profitabilityStats?.find { it.first.contains("Profit Margin", ignoreCase = true) }?.second ?: "24.0%"
    val roe = "156.1%"

    StockticsMetricRow(label = "Gross Profit Margin (Pricing Power)", value = grossMargin, valueColor = GainGreen)
    StockticsMetricRow(label = "EBITDA Margin (Operating Cash Engine)", value = ebitdaMargin, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Operating (EBIT) Margin", value = opMargin, valueColor = FintechBlue)
    StockticsMetricRow(label = "Net Profit Margin (Bottom-Line)", value = netMargin, valueColor = GainGreen)
    StockticsMetricRow(label = "Return on Equity (ROE)", value = roe)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. DEDICATED 4-TIER PROFITABILITY MARGIN WATERFALL BAR CHART
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "4-TIER PROFITABILITY MARGIN CASCADE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Visual breakdown vs S&P 500 median benchmarks",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
                StockticsBadge(text = "HIGH-MOAT TECH", variant = BadgeVariant.GAIN)
            }

            Spacer(modifier = Modifier.height(12.dp))

            data class MarginBarItem(
                val tierName: String,
                val marginVal: Float,
                val benchmarkVal: Float,
                val barColor: Color,
                val tag: String
            )

            val marginTiers = listOf(
                MarginBarItem("1. Gross Margin", 46.2f, 45.0f, GainGreen, "PRICING POWER"),
                MarginBarItem("2. EBITDA Margin", 33.8f, 25.0f, FintechPrimary, "CASH RUN-RATE"),
                MarginBarItem("3. Operating (EBIT) Margin", 30.7f, 18.0f, FintechBlue, "SG&A DISCIPLINE"),
                MarginBarItem("4. Net Profit Margin", 24.0f, 12.0f, GainGreen, "NET CONVERSION")
            )

            marginTiers.forEach { tier ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tier.tierName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("• ${tier.tag}", fontSize = 8.sp, color = TextTertiary)
                        }
                        Text(
                            text = "${tier.marginVal}% (Bench: ${tier.benchmarkVal}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tier.barColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Comparative Bar with Benchmark Tick
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FintechSurfaceElevated)
                    ) {
                        // Company actual bar
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(tier.marginVal / 100f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tier.barColor)
                        )

                        // S&P 500 Benchmark line
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(tier.benchmarkVal / 100f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp, 6.dp).background(Color.White))
                Spacer(modifier = Modifier.width(4.dp))
                Text("White vertical tick = S&P 500 Industry Median", fontSize = 8.sp, color = TextTertiary)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive Operating Leverage (DOL) Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val dolMultiplier = (100f / (100f - simFixedCostRatioPct).coerceAtLeast(10f))
            val ebitSurgePct = simDolRevChangePct * dolMultiplier

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Operating Leverage Simulator (DOL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("How fixed overhead multiplies EBIT expansion", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "DOL: ${String.format(Locale.US, "%.2f", dolMultiplier)}x",
                    variant = if (dolMultiplier > 2f) BadgeVariant.GAIN else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Revenue Shift
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Revenue Shift", fontSize = 10.sp, color = TextSecondary)
                Text("${if (simDolRevChangePct >= 0) "+" else ""}${simDolRevChangePct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simDolRevChangePct >= 0) GainGreen else LossRed)
            }
            Slider(
                value = simDolRevChangePct,
                onValueChange = onDolRevChange,
                valueRange = -20f..25f,
                steps = 44,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: Fixed Cost Ratio
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fixed Cost Structure % of OpEx", fontSize = 10.sp, color = TextSecondary)
                Text("${simFixedCostRatioPct.toInt()}% Fixed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simFixedCostRatioPct,
                onValueChange = onFixedCostRatioChange,
                valueRange = 20f..80f,
                steps = 59,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Comparative Result Bars
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Topline Revenue Shift:", fontSize = 10.sp, color = TextSecondary)
                Text("${if (simDolRevChangePct >= 0) "+" else ""}${String.format(Locale.US, "%.1f", simDolRevChangePct)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FintechBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((Math.abs(simDolRevChangePct) / 50f).coerceIn(0.05f, 1f))
                        .background(FintechBlue)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Operating Income (EBIT) Multiplied Swing:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${if (ebitSurgePct >= 0) "+" else ""}${String.format(Locale.US, "%.1f", ebitSurgePct)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (ebitSurgePct >= 0) GainGreen else LossRed)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FintechBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((Math.abs(ebitSurgePct) / 80f).coerceIn(0.05f, 1f))
                        .background(if (ebitSurgePct >= 0) GainGreen else LossRed)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Degree of Operating Leverage (${String.format(Locale.US, "%.2f", dolMultiplier)}x): Because fixed costs stay flat, a ${simDolRevChangePct.toInt()}% shift in revenue produces an amplified ${String.format(Locale.US, "%.1f", ebitSurgePct)}% explosion in operating profit!",
                fontSize = 9.sp,
                color = if (ebitSurgePct >= 0) GainGreen else WarningAmber
            )
        }
    }
}

@Composable
private fun Lesson5LiveLab(
    payload: DeepStockResearchPayload?,
    simNetIncomeGrowthPct: Float,
    simSbcDilutionPct: Float,
    simBuybackPct: Float,
    onNetIncomeChange: (Float) -> Unit,
    onSbcChange: (Float) -> Unit,
    onBuybackChange: (Float) -> Unit
) {
    val netIncStr = "$88.1B"
    val cfoStr = "$118.5B"
    val cfoRatioStr = "1.35x"
    val sloanAccrualStr = "-5.93%"
    val dilutionTrend = "-2.8% (Accretive Buybacks)"

    StockticsMetricRow(label = "GAAP Reported Net Income", value = netIncStr)
    StockticsMetricRow(label = "Cash Flow from Operations (CFO)", value = cfoStr, valueColor = GainGreen)
    StockticsMetricRow(label = "CFO / Net Income Cash Backing Ratio", value = cfoRatioStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Sloan Accrual Ratio (Quality Gauge)", value = sloanAccrualStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Diluted Share Count YoY Trend", value = dilutionTrend, valueColor = FintechBlue)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Net Income vs CFO Cash Backing Bar Chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EARNINGS QUALITY: GAAP NET INCOME VS CFO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text("Testing whether profits are backed by actual cash inflows", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(text = "CASH RICH (1.35x)", variant = BadgeVariant.GAIN)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comparative Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$88.1B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(FintechBlue)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("GAAP Net Income", fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("$118.5B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(82.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(GainGreen)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Operating Cash (CFO)", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = GainGreen, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sloan Accrual Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechSurfaceElevated)
                    .padding(8.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sloan Accrual: -5.93%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                        Text("Safe Zone (< -3%)", fontSize = 9.sp, color = GainGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(FintechBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.35f)
                                .background(GainGreen)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("Conservative accounting: Cash collection leads reported accounting revenue.", fontSize = 8.sp, color = TextSecondary)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. SBC Dilution vs Buyback EPS Cannibal Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val netSharePct = simSbcDilutionPct - simBuybackPct
            val realEpsGrowth = ((1f + simNetIncomeGrowthPct / 100f) / (1f + netSharePct / 100f) - 1f) * 100f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SBC Dilution vs Buyback Simulator", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Headline net income growth vs real EPS compounding", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (realEpsGrowth > simNetIncomeGrowthPct) "ACCRETIVE EPS" else "DILUTED",
                    variant = if (realEpsGrowth > simNetIncomeGrowthPct) BadgeVariant.GAIN else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Net Income Growth
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stated Net Income Growth", fontSize = 10.sp, color = TextSecondary)
                Text("+${simNetIncomeGrowthPct.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simNetIncomeGrowthPct,
                onValueChange = onNetIncomeChange,
                valueRange = 0f..30f,
                steps = 29,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: SBC Dilution
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SBC Shares Issued (Dilution Drag)", fontSize = 10.sp, color = TextSecondary)
                Text("+${simSbcDilutionPct.toInt()}% shares", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LossRed)
            }
            Slider(
                value = simSbcDilutionPct,
                onValueChange = onSbcChange,
                valueRange = 0f..8f,
                steps = 16,
                colors = SliderDefaults.colors(thumbColor = LossRed, activeTrackColor = LossRed, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 3: Buybacks
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Share Buybacks (Shares Retired)", fontSize = 10.sp, color = TextSecondary)
                Text("-${simBuybackPct.toInt()}% shares", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simBuybackPct,
                onValueChange = onBuybackChange,
                valueRange = 0f..8f,
                steps = 16,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Headline Net Income Growth:", fontSize = 10.sp, color = TextSecondary)
                Text("+${simNetIncomeGrowthPct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Real Per-Share Diluted EPS Growth:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${if (realEpsGrowth >= 0) "+" else ""}${String.format(Locale.US, "%.1f", realEpsGrowth)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (realEpsGrowth >= simNetIncomeGrowthPct) GainGreen else WarningAmber)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Net share count delta: ${if (netSharePct > 0) "+${netSharePct.toInt()}% dilution" else "${netSharePct.toInt()}% retired"}. Stealth dilution reduces investor compounding unless buybacks outpace options.",
                fontSize = 9.sp,
                color = if (realEpsGrowth >= simNetIncomeGrowthPct) GainGreen else LossRed
            )
        }
    }
}

@Composable
private fun Lesson6LiveLab(
    payload: DeepStockResearchPayload?,
    simCapExAllocPct: Float,
    simBuybackAllocPct: Float,
    simDividendAllocPct: Float,
    simDebtAllocPct: Float,
    onCapExChange: (Float) -> Unit,
    onBuybackChange: (Float) -> Unit,
    onDividendChange: (Float) -> Unit,
    onDebtChange: (Float) -> Unit
) {
    val cfoStr = "$118.5B"
    val capexStr = "-$44.5B"
    val fcfStr = "$74.0B"
    val fcfConv = "84.0%"
    val capexIntensity = "18.2%"

    StockticsMetricRow(label = "Cash Flow from Operations (CFO)", value = cfoStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Capital Expenditures (CapEx)", value = capexStr, valueColor = LossRed)
    StockticsMetricRow(label = "Free Cash Flow (FCF = CFO - CapEx)", value = fcfStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "FCF Conversion Rate (FCF / Net Income)", value = fcfConv, valueColor = GainGreen)
    StockticsMetricRow(label = "CapEx Intensity (CapEx / Revenue)", value = capexIntensity)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. FREE CASH FLOW WATERFALL STEP BAR CHART
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FREE CASH FLOW WATERFALL: CFO - CAPEX = FCF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text("Converting operational cash into distributable shareholder wealth", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(text = "84% CONVERSION", variant = BadgeVariant.GAIN)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waterfall Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Step 1: CFO
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("+$118.5B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(75.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GainGreen)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("CFO Cash", fontSize = 9.sp, color = TextSecondary)
                }

                // Step 2: CapEx
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("-$44.5B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LossRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LossRed)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Less CapEx", fontSize = 9.sp, color = TextSecondary)
                }

                // Step 3: FCF
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("=$74.0B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FintechPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Free Cash Flow", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Capital Allocation Hierarchy Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Capital Allocation Decision Simulator", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Distribute $100M of Annual Free Cash Flow", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (simBuybackAllocPct + simCapExAllocPct >= 65f) "COMPOUNDER" else "INCOME RETURN",
                    variant = BadgeVariant.INFO
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sliders
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Reinvestment (Growth CapEx & R&D)", fontSize = 10.sp, color = TextSecondary)
                Text("${simCapExAllocPct.toInt()}% ($${simCapExAllocPct.toInt()}M)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simCapExAllocPct,
                onValueChange = onCapExChange,
                valueRange = 0f..60f,
                steps = 59,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Share Repurchases (Buybacks)", fontSize = 10.sp, color = TextSecondary)
                Text("${simBuybackAllocPct.toInt()}% ($${simBuybackAllocPct.toInt()}M)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simBuybackAllocPct,
                onValueChange = onBuybackChange,
                valueRange = 0f..60f,
                steps = 59,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cash Dividends", fontSize = 10.sp, color = TextSecondary)
                Text("${simDividendAllocPct.toInt()}% ($${simDividendAllocPct.toInt()}M)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Slider(
                value = simDividendAllocPct,
                onValueChange = onDividendChange,
                valueRange = 0f..40f,
                steps = 39,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Debt Paydown & Cash Reserves", fontSize = 10.sp, color = TextSecondary)
                Text("${simDebtAllocPct.toInt()}% ($${simDebtAllocPct.toInt()}M)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
            }
            Slider(
                value = simDebtAllocPct,
                onValueChange = onDebtChange,
                valueRange = 0f..40f,
                steps = 39,
                colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-segment Allocation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FintechBackground)
            ) {
                val totalAlloc = (simCapExAllocPct + simBuybackAllocPct + simDividendAllocPct + simDebtAllocPct).coerceAtLeast(1f)
                Box(
                    modifier = Modifier.fillMaxHeight().weight(simCapExAllocPct.coerceAtLeast(0.1f) / totalAlloc).background(GainGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CapEx", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier.fillMaxHeight().weight(simBuybackAllocPct.coerceAtLeast(0.1f) / totalAlloc).background(FintechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Buyback", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier.fillMaxHeight().weight(simDividendAllocPct.coerceAtLeast(0.1f) / totalAlloc).background(FintechPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Div", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier.fillMaxHeight().weight(simDebtAllocPct.coerceAtLeast(0.1f) / totalAlloc).background(WarningAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Debt", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (simCapExAllocPct >= 40f) "High Reinvestment Strategy: Prioritizing high-ROIC compounding and moat expansion." else if (simBuybackAllocPct >= 40f) "Capital Return Strategy: Aggressive EPS cannibalization and intrinsic value accretion." else "Balanced Capital Allocation: Sustainable dividend payout with moderate debt protection.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson7LiveLab(
    payload: DeepStockResearchPayload?,
    simRateHikeBps: Float,
    simDebtPaydownB: Float,
    onRateHikeChange: (Float) -> Unit,
    onDebtPaydownChange: (Float) -> Unit
) {
    val totalDebtStr = "$106.6B"
    val cashStr = "$65.3B"
    val netDebtStr = "$41.3B"
    val debtEbitdaStr = "0.79x"
    val interestCovStr = "31.3x"
    val debtFcfStr = "0.98 Yrs"
    val altmanZStr = "8.45"

    StockticsMetricRow(label = "Total Funded Debt", value = totalDebtStr, valueColor = LossRed)
    StockticsMetricRow(label = "Cash & Liquid Marketable Securities", value = cashStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Net Debt (Total Debt - Cash)", value = netDebtStr, valueColor = FintechBlue)
    StockticsMetricRow(label = "Debt-to-EBITDA Multiple", value = debtEbitdaStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Debt-to-FCF Payoff Horizon", value = debtFcfStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Interest Coverage Ratio (EBIT / Interest)", value = interestCovStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Altman Z-Score (Bankruptcy Risk)", value = "$altmanZStr (Safe Zone)", valueColor = GainGreen)

    Spacer(modifier = Modifier.height(12.dp))

    // Interactive Solvency & Rate Shock Stress Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SOLVENCY & RATE SHOCK STRESS SIMULATOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                    Text("Simulate interest rate hikes & debt repayment on interest coverage", fontSize = 9.sp, color = TextSecondary)
                }
                val isPristine = interestCovStr.replace("x", "").toFloatOrNull()?.let { it > 10f } ?: true
                StockticsBadge(
                    text = if (isPristine) "FORTRESS SHIELD" else "VULNERABLE",
                    variant = if (isPristine) BadgeVariant.GAIN else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sliders
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Interest Rate Hike Shock", fontSize = 10.sp, color = TextSecondary)
                Text("+${simRateHikeBps.toInt()} bps (+${(simRateHikeBps / 100f)}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LossRed)
            }
            Slider(
                value = simRateHikeBps,
                onValueChange = onRateHikeChange,
                valueRange = 0f..500f,
                steps = 19,
                colors = SliderDefaults.colors(thumbColor = LossRed, activeTrackColor = LossRed, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Organic Debt Paydown from FCF", fontSize = 10.sp, color = TextSecondary)
                Text("$${simDebtPaydownB.toInt()}B Extinguished", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simDebtPaydownB,
                onValueChange = onDebtPaydownChange,
                valueRange = 0f..50f,
                steps = 49,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Altman Z-Score Gauge Bar
            Text("ALTMAN Z-SCORE ZONAL BENCHMARK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier.weight(1.81f).fillMaxHeight().background(LossRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Distress <1.81", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier.weight(1.18f).fillMaxHeight().background(WarningAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Grey Zone", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier.weight(3.0f).fillMaxHeight().background(GainGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Safe Zone >2.99", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Apple trades with Z = 8.45, deeply inside the Safe Zone. Debt is ultra-low risk and backed by massive operating cash flows.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson8LiveLab(
    payload: DeepStockResearchPayload?,
    simNetMarginDuPont: Float,
    simAssetTurnover: Float,
    simEquityMultiplier: Float,
    simWaccHurdle: Float,
    onNetMarginChange: (Float) -> Unit,
    onAssetTurnoverChange: (Float) -> Unit,
    onEquityMultiplierChange: (Float) -> Unit,
    onWaccHurdleChange: (Float) -> Unit
) {
    val roicStr = "86.3%"
    val roeStr = "139.9%"
    val roaStr = "25.7%"
    val netMarginStr = "24.0%"
    val assetTurnoverStr = "1.07x"
    val equityMultStr = "5.45x"
    val evaSpreadStr = "+77.8%"

    StockticsMetricRow(label = "Return on Invested Capital (ROIC)", value = roicStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Return on Equity (DuPont ROE)", value = roeStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Return on Assets (ROA)", value = roaStr, valueColor = FintechBlue)
    StockticsMetricRow(label = "Net Profit Margin", value = netMarginStr)
    StockticsMetricRow(label = "Asset Turnover Ratio", value = assetTurnoverStr)
    StockticsMetricRow(label = "Equity Multiplier (Financial Leverage)", value = equityMultStr, valueColor = if (simEquityMultiplier > 4f) WarningAmber else TextPrimary)
    StockticsMetricRow(label = "Economic Value Added (ROIC - WACC Spread)", value = evaSpreadStr, valueColor = GainGreen)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Interactive 3-Stage DuPont Decomposition Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val calcRoe = (simNetMarginDuPont * simAssetTurnover * simEquityMultiplier)
            val isLeverageTrap = simEquityMultiplier >= 4.0f && simNetMarginDuPont < 12.0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3-STAGE DUPONT EQUATION SIMULATOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ROE = Net Margin × Asset Turnover × Equity Multiplier",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
                StockticsBadge(
                    text = if (isLeverageTrap) "LEVERAGE TRAP!" else if (calcRoe >= 25f) "ELITE ROE" else "MODEST ROE",
                    variant = if (isLeverageTrap) BadgeVariant.LOSS else if (calcRoe >= 25f) BadgeVariant.GAIN else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Net Margin %
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1. Net Margin (Pricing Power)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", simNetMarginDuPont)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simNetMarginDuPont,
                onValueChange = onNetMarginChange,
                valueRange = 2f..40f,
                steps = 37,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: Asset Turnover
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("2. Asset Turnover (Efficiency / Velocity)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.2f", simAssetTurnover)}x", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simAssetTurnover,
                onValueChange = onAssetTurnoverChange,
                valueRange = 0.2f..2.5f,
                steps = 45,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 3: Equity Multiplier
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("3. Equity Multiplier (Debt Leverage Multiplier)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.2f", simEquityMultiplier)}x", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simEquityMultiplier > 4f) LossRed else WarningAmber)
            }
            Slider(
                value = simEquityMultiplier,
                onValueChange = onEquityMultiplierChange,
                valueRange = 1.0f..8.0f,
                steps = 69,
                colors = SliderDefaults.colors(
                    thumbColor = if (simEquityMultiplier > 4f) LossRed else WarningAmber,
                    activeTrackColor = if (simEquityMultiplier > 4f) LossRed else WarningAmber,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Resulting DuPont ROE banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, if (isLeverageTrap) LossRed else FintechPrimary, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Resulting DuPont Return on Equity (ROE)", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", calcRoe)}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLeverageTrap) LossRed else GainGreen
                        )
                    }
                    Text(
                        text = if (isLeverageTrap) "High Debt Warning: ROE is artificially inflated by leverage!" else "Balanced Quality: Healthy margin and turnover support.",
                        fontSize = 9.sp,
                        color = if (isLeverageTrap) LossRed else TextSecondary,
                        modifier = Modifier.weight(1f).padding(start = 12.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive ROIC vs WACC Value Creation Spread Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val baseRoic = 28.5f // Baseline tech compounder ROIC
            val evaSpread = baseRoic - simWaccHurdle

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ROIC vs WACC Value Creation Spread", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Does reinvested capital create or destroy shareholder value?", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (evaSpread >= 10f) "ELITE VALUE CREATOR" else if (evaSpread > 0f) "VALUE ACCRETIVE" else "WEALTH DESTROYER",
                    variant = if (evaSpread >= 10f) BadgeVariant.GAIN else if (evaSpread > 0f) BadgeVariant.INFO else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cost of Capital Hurdle Rate (WACC)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", simWaccHurdle)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
            }
            Slider(
                value = simWaccHurdle,
                onValueChange = onWaccHurdleChange,
                valueRange = 5.0f..15.0f,
                steps = 49,
                colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Value Spread Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Economic Value Added Spread:", fontSize = 10.sp, color = TextSecondary)
                Text(
                    text = "${if (evaSpread >= 0) "+" else ""}${String.format(Locale.US, "%.1f", evaSpread)}% (${(evaSpread * 100).toInt()} bps)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (evaSpread >= 0) GainGreen else LossRed
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FintechBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((Math.abs(evaSpread) / 30f).coerceIn(0.05f, 1f))
                        .background(if (evaSpread >= 0) GainGreen else LossRed)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (evaSpread >= 0) "Every $100 reinvested creates $${String.format(Locale.US, "%.1f", evaSpread)} in net economic value above hurdle rate." else "Growth with ROIC < WACC actively destroys investor wealth!",
                fontSize = 9.sp,
                color = if (evaSpread >= 0) TextSecondary else LossRed
            )
        }
    }
}

@Composable
private fun Lesson9LiveLab(
    payload: DeepStockResearchPayload?,
    simPricingPowerPct: Float,
    simChurnRatePct: Float,
    simScaleAdvantagePct: Float,
    simCustomerConcentrationPct: Float,
    onPricingPowerChange: (Float) -> Unit,
    onChurnRateChange: (Float) -> Unit,
    onScaleAdvantageChange: (Float) -> Unit,
    onCustomerConcentrationChange: (Float) -> Unit
) {
    val gmStabilityStr = "0.95% (Pristine)"
    val roic5YrStr = "31.4% (Wide Moat)"
    val moatClassStr = "Two-Sided Network Toll"
    val opexRatioStr = "18.2% (Diluting)"
    val concentrationStr = "${String.format(Locale.US, "%.1f", simCustomerConcentrationPct)}% (Footnote)"

    StockticsMetricRow(label = "Gross Margin Volatility (σ_GM)", value = gmStabilityStr, valueColor = GainGreen)
    StockticsMetricRow(label = "5-Year Median ROIC", value = roic5YrStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Primary Structural Moat Source", value = moatClassStr, valueColor = FintechBlue)
    StockticsMetricRow(label = "Operating Expense Ratio (OpEx / Rev)", value = opexRatioStr)
    StockticsMetricRow(
        label = "Top Single Customer Concentration",
        value = concentrationStr,
        valueColor = if (simCustomerConcentrationPct > 10f) LossRed else GainGreen
    )
    StockticsMetricRow(
        label = "Consolidated Economic Moat Rating",
        value = if (simCustomerConcentrationPct > 10f) "NARROW MOAT (CONCENTRATION RISK)" else "WIDE MOAT COMPOUNDER",
        valueColor = if (simCustomerConcentrationPct > 10f) WarningAmber else GainGreen
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Interactive Pricing Power & Churn Resilience Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val netVolumeShiftPct = -(simChurnRatePct * 1.5f)
            val netRevenueGrowthPct = simPricingPowerPct + netVolumeShiftPct
            val hasGenuinePricingPower = simPricingPowerPct >= 5.0f && netRevenueGrowthPct > 2.0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRICING POWER & CHURN RESILIENCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Can management raise prices without losing customer volume?",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
                StockticsBadge(
                    text = if (hasGenuinePricingPower) "INELASTIC PRICING" else if (netRevenueGrowthPct < 0f) "PRICE-TAKER (DEFECTION)" else "NEUTRAL RESILIENCE",
                    variant = if (hasGenuinePricingPower) BadgeVariant.GAIN else if (netRevenueGrowthPct < 0f) BadgeVariant.LOSS else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Annual Price Hike Imposed
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1. Price Increase Imposed (+Inflation Passthrough)", fontSize = 10.sp, color = TextSecondary)
                Text("+${String.format(Locale.US, "%.1f", simPricingPowerPct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simPricingPowerPct,
                onValueChange = onPricingPowerChange,
                valueRange = 0f..20f,
                steps = 39,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: Churn / Volume Loss
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("2. Induced Customer Churn (Switching Defection)", fontSize = 10.sp, color = TextSecondary)
                Text("-${String.format(Locale.US, "%.1f", simChurnRatePct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simChurnRatePct > 4f) LossRed else WarningAmber)
            }
            Slider(
                value = simChurnRatePct,
                onValueChange = onChurnRateChange,
                valueRange = 0.1f..15.0f,
                steps = 59,
                colors = SliderDefaults.colors(
                    thumbColor = if (simChurnRatePct > 4f) LossRed else WarningAmber,
                    activeTrackColor = if (simChurnRatePct > 4f) LossRed else WarningAmber,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Net Economic Impact Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, if (netRevenueGrowthPct < 0f) LossRed else FintechPrimary, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Net Organic Revenue Impact", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "${if (netRevenueGrowthPct >= 0) "+" else ""}${String.format(Locale.US, "%.1f", netRevenueGrowthPct)}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netRevenueGrowthPct >= 0) GainGreen else LossRed
                        )
                    }
                    Text(
                        text = if (hasGenuinePricingPower)
                            "Elite Moat: Inelastic customer demand allows full inflation passthrough with negligible defection."
                        else if (netRevenueGrowthPct < 0f)
                            "Severe Commoditization: Price increases trigger customer migration to rival products!"
                        else
                            "Moderate Moat: Modest pricing elasticity; brand loyalty preserves equilibrium.",
                        fontSize = 9.sp,
                        color = if (netRevenueGrowthPct < 0f) LossRed else TextSecondary,
                        modifier = Modifier.weight(1f).padding(start = 12.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive Single Customer Concentration Footnote Stress Test
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, if (simCustomerConcentrationPct > 10f) LossRed else FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val isHighConcentration = simCustomerConcentrationPct > 10.0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CUSTOMER CONCENTRATION STRESS TEST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("SEC Form 10-K Note Disclosure (ASC 280-10-50-42)", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (isHighConcentration) "10-K CONCENTRATION WARNING" else "DIVERSIFIED BASE",
                    variant = if (isHighConcentration) BadgeVariant.LOSS else BadgeVariant.GAIN
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Top Customer % of Consolidated Revenue", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", simCustomerConcentrationPct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isHighConcentration) LossRed else GainGreen)
            }
            Slider(
                value = simCustomerConcentrationPct,
                onValueChange = onCustomerConcentrationChange,
                valueRange = 1.0f..50.0f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = if (isHighConcentration) LossRed else GainGreen,
                    activeTrackColor = if (isHighConcentration) LossRed else GainGreen,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isHighConcentration)
                    "CRITICAL RISK: A single customer accounts for ${String.format(Locale.US, "%.1f", simCustomerConcentrationPct)}% of sales (> 10% statutory disclosure limit). This customer wields monopsonistic pricing power, capping vendor gross margins and posing existential cancellation risk."
                else
                    "RESILIENT: No single customer exceeds the 10% SEC footnote threshold. Loss of any individual client will not impair operating solvency or moat durability.",
                fontSize = 9.sp,
                color = if (isHighConcentration) LossRed else TextSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Interactive Scale Advantage & OpEx Dilution
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ECONOMIES OF SCALE & OPEX DILUTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Spreading fixed tech, warehousing, and logistics overhead over volume", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (simScaleAdvantagePct >= 10f) "COST SCALE MOAT" else "LIMITED SCALE ADVANTAGE",
                    variant = if (simScaleAdvantagePct >= 10f) BadgeVariant.GAIN else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("OpEx Efficiency Dilution via Volume Scale", fontSize = 10.sp, color = TextSecondary)
                Text("-${String.format(Locale.US, "%.1f", simScaleAdvantagePct)}% OpEx", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simScaleAdvantagePct,
                onValueChange = onScaleAdvantageChange,
                valueRange = 0.0f..25.0f,
                steps = 50,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Scale advantage widens operating margin spread by ${String.format(Locale.US, "%.1f", simScaleAdvantagePct)}% against sub-scale competitors, enabling lower customer prices (Costco model) while earning superior ROIC.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson10LiveLab(
    payload: DeepStockResearchPayload?,
    simAnnualBuybackBillions: Float,
    simSbcDilutionPct: Float,
    simBuybackPriceToFcf: Float,
    simInsiderOwnershipPct: Float,
    onAnnualBuybackChange: (Float) -> Unit,
    onSbcDilutionChange: (Float) -> Unit,
    onBuybackPriceChange: (Float) -> Unit,
    onInsiderOwnershipChange: (Float) -> Unit
) {
    val shareShrinkageStr = "-3.5% (Cannibal)"
    val buffettRatioStr = "$3.98 / $1.00"
    val shareholderYieldStr = "3.4% (Net Yield)"
    val goodwillRatioStr = "0.0% (Clean Assets)"
    val insiderOwnershipStr = "${String.format(Locale.US, "%.1f", simInsiderOwnershipPct)}% Skin in Game"

    StockticsMetricRow(label = "5-Year Diluted Share Count CAGR", value = shareShrinkageStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Warren Buffett $1 Retained Earnings Test", value = buffettRatioStr, valueColor = FintechPrimary)
    StockticsMetricRow(label = "Total Shareholder Yield (Buybacks + Divs)", value = shareholderYieldStr, valueColor = FintechBlue)
    StockticsMetricRow(label = "Goodwill to Total Assets (M&A Risk)", value = goodwillRatioStr, valueColor = GainGreen)
    StockticsMetricRow(label = "Insider Equity Alignment (Form 4)", value = insiderOwnershipStr, valueColor = if (simInsiderOwnershipPct >= 5f) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Consolidated Capital Allocation Rating", value = "TIER-1 ELITE CAPITAL ALLOCATOR", valueColor = GainGreen)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Interactive Buyback Cannibal vs SBC Dilution Simulator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val marketCapEst = 3000f
            val grossReductionPct = (simAnnualBuybackBillions / marketCapEst) * 100f
            val netShareReductionPct = grossReductionPct - simSbcDilutionPct
            val isDilutionTreadmill = netShareReductionPct <= 0.2f
            val isEliteCannibal = netShareReductionPct >= 1.8f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BUYBACK CANNIBAL VS. SBC TREADMILL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Are buybacks retiring shares or just masking executive dilution?",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
                StockticsBadge(
                    text = if (isEliteCannibal) "BUYBACK CANNIBAL" else if (isDilutionTreadmill) "DILUTION TREADMILL" else "MODERATE RETIREMENT",
                    variant = if (isEliteCannibal) BadgeVariant.GAIN else if (isDilutionTreadmill) BadgeVariant.LOSS else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider 1: Annual Cash Spent on Buybacks
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1. Cash Allocated to Share Buybacks", fontSize = 10.sp, color = TextSecondary)
                Text("$${String.format(Locale.US, "%.0f", simAnnualBuybackBillions)}B / Year", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Slider(
                value = simAnnualBuybackBillions,
                onValueChange = onAnnualBuybackChange,
                valueRange = 5f..100f,
                steps = 19,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            // Slider 2: Stock-Based Compensation Dilution Drag
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("2. Annual Stock-Based Compensation (SBC) Dilution", fontSize = 10.sp, color = TextSecondary)
                Text("+${String.format(Locale.US, "%.1f", simSbcDilutionPct)}% New Shares", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simSbcDilutionPct > 2.5f) LossRed else WarningAmber)
            }
            Slider(
                value = simSbcDilutionPct,
                onValueChange = onSbcDilutionChange,
                valueRange = 0.0f..6.0f,
                steps = 60,
                colors = SliderDefaults.colors(
                    thumbColor = if (simSbcDilutionPct > 2.5f) LossRed else WarningAmber,
                    activeTrackColor = if (simSbcDilutionPct > 2.5f) LossRed else WarningAmber,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Net Annual Share Shrinkage Result
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechSurfaceElevated)
                    .border(1.dp, if (isDilutionTreadmill) LossRed else FintechPrimary, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Net Diluted Share Count Change", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = "${if (netShareReductionPct > 0) "-" else "+"}${String.format(Locale.US, "%.2f", Math.abs(netShareReductionPct))}% / Year",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netShareReductionPct > 0) GainGreen else LossRed
                        )
                    }
                    Text(
                        text = if (isEliteCannibal)
                            "Authentic Cannibal: Share count shrinks rapidly, mechanically increasing EPS and intrinsic value per share."
                        else if (isDilutionTreadmill)
                            "Vanity Buyback Warning: Buyback cash is 100% consumed offsetting employee stock grants!"
                        else
                            "Balanced Program: Moderate share retirement outpacing executive grant dilution.",
                        fontSize = 9.sp,
                        color = if (isDilutionTreadmill) LossRed else TextSecondary,
                        modifier = Modifier.weight(1f).padding(start = 12.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Interactive Buyback Valuation Timing (Price / FCF Gate)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechSurfaceElevated)
            .border(1.dp, if (simBuybackPriceToFcf > 32f) LossRed else FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val isOvervalued = simBuybackPriceToFcf > 32f
            val isUndervalued = simBuybackPriceToFcf <= 20f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BUYBACK TIMING & VALUATION GATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Is management buying shares below intrinsic value or at bubble peaks?", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (isUndervalued) "VALUE ACCRETIVE" else if (isOvervalued) "DESTRUCTIVE OVERPAY" else "FAIR VALUATION",
                    variant = if (isUndervalued) BadgeVariant.GAIN else if (isOvervalued) BadgeVariant.LOSS else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Price-to-Free Cash Flow (P/FCF) Multiple When Repurchasing", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", simBuybackPriceToFcf)}x P/FCF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isOvervalued) LossRed else if (isUndervalued) GainGreen else WarningAmber)
            }
            Slider(
                value = simBuybackPriceToFcf,
                onValueChange = onBuybackPriceChange,
                valueRange = 10.0f..45.0f,
                steps = 35,
                colors = SliderDefaults.colors(
                    thumbColor = if (isOvervalued) LossRed else if (isUndervalued) GainGreen else WarningAmber,
                    activeTrackColor = if (isOvervalued) LossRed else if (isUndervalued) GainGreen else WarningAmber,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOvervalued)
                    "CAPITAL DESTRUCTION: Repurchasing shares at ${String.format(Locale.US, "%.1f", simBuybackPriceToFcf)}x FCF destroys intrinsic value per share. Cash should be held in short-term Treasuries or distributed as a special dividend."
                else if (isUndervalued)
                    "VALUE ACCRETIVE: Buying at ${String.format(Locale.US, "%.1f", simBuybackPriceToFcf)}x FCF compounds shareholder equity at high double-digit internal rates of return."
                else
                    "NEUTRAL CAPITAL DEPLOYMENT: Repurchasing near fair value preserves capital without significant multiple arbitrage.",
                fontSize = 9.sp,
                color = if (isOvervalued) LossRed else TextSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Insider Skin in the Game (SEC Form 4 Alignment)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            val isHighAlignment = simInsiderOwnershipPct >= 5.0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("INSIDER SKIN IN THE GAME & FORM 4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Management equity ownership & personal capital commitment", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (isHighAlignment) "HIGH INSIDER ALIGNMENT" else "LOW SKIN IN THE GAME",
                    variant = if (isHighAlignment) BadgeVariant.GAIN else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("C-Suite & Board Insider Ownership %", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.1f", simInsiderOwnershipPct)}% of Common Equity", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simInsiderOwnershipPct,
                onValueChange = onInsiderOwnershipChange,
                valueRange = 0.1f..35.0f,
                steps = 70,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isHighAlignment)
                    "High Alignment: Executives own ${String.format(Locale.US, "%.1f", simInsiderOwnershipPct)}% of the enterprise. Their net worth compounds with per-share equity, eliminating agency risk and reckless M&A."
                else
                    "Agent Risk: Insiders hold less than 5% equity. Ensure executive compensation (DEF 14A) is strictly tied to 3-year ROIC and Free Cash Flow rather than top-line revenue.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson11LiveLab(
    payload: DeepStockResearchPayload?,
    simSectorCyclePhase: Int,
    simIndustryHhi: Float,
    simRelativeMarginSpreadPct: Float,
    simInputCostShockPct: Float,
    onCyclePhaseChange: (Int) -> Unit,
    onIndustryHhiChange: (Float) -> Unit,
    onRelativeMarginSpreadChange: (Float) -> Unit,
    onInputCostShockChange: (Float) -> Unit
) {
    val sectorStr = payload?.profile?.sector ?: "Information Technology"
    val industryStr = payload?.profile?.industry ?: "Software — Infrastructure"
    val betaVal = payload?.valuationStats?.find { it.first.contains("Beta", ignoreCase = true) }?.second?.replace(",", "")?.toFloatOrNull() ?: 0.88f
    val isOligopoly = simIndustryHhi >= 2200f
    val isFragmented = simIndustryHhi < 1500f
    val isWideOutperformer = simRelativeMarginSpreadPct >= 5.0f

    val cyclePhaseName = when (simSectorCyclePhase) {
        0 -> "Recession (Contraction)"
        1 -> "Early Recovery"
        2 -> "Mid-Cycle Expansion"
        else -> "Late-Cycle Overheating"
    }

    // Dynamic Stress-Tested Trough Margin Calculation
    val baseMargin = 38.0f + simRelativeMarginSpreadPct
    val cycleDampener = when (simSectorCyclePhase) {
        0 -> -12.0f * (if (betaVal > 1.0f) 1.5f else 0.7f)
        1 -> 2.0f
        2 -> 5.0f
        else -> -2.0f
    }
    val costShockDampener = simInputCostShockPct * 0.45f
    val simulatedOperatingMargin = (baseMargin + cycleDampener - costShockDampener).coerceAtLeast(-5.0f)

    StockticsMetricRow(label = "GICS Sector / Industry", value = "$sectorStr • $industryStr", valueColor = FintechPrimary)
    StockticsMetricRow(label = "Sector Cyclicality Beta (Sensitivity)", value = "${String.format(Locale.US, "%.2f", betaVal)} (${if (betaVal < 1.0f) "Defensive / Secular" else "High Beta Cyclical"})", valueColor = FintechBlue)
    StockticsMetricRow(label = "Industry Concentration (HHI Index)", value = "${simIndustryHhi.toInt()} (${if (isOligopoly) "Oligopoly Umbrella" else if (isFragmented) "Fragmented Price Wars" else "Moderately Concentrated"})", valueColor = if (isOligopoly) GainGreen else if (isFragmented) LossRed else WarningAmber)
    StockticsMetricRow(label = "Relative Margin Spread vs Peer Median", value = "${if (simRelativeMarginSpreadPct >= 0) "+" else ""}${String.format(Locale.US, "%.1f", simRelativeMarginSpreadPct)}% Spread", valueColor = if (isWideOutperformer) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Simulated Cycle Operating Margin", value = "${String.format(Locale.US, "%.1f", simulatedOperatingMargin)}% (${if (simulatedOperatingMargin >= 20.0f) "High Cash Cushion" else if (simulatedOperatingMargin >= 5.0f) "Squeezed Margin" else "Operating Loss Threat"})", valueColor = if (simulatedOperatingMargin >= 15.0f) GainGreen else if (simulatedOperatingMargin >= 5.0f) WarningAmber else LossRed)
    StockticsMetricRow(label = "Consolidated Sector Quality Rating", value = if (isOligopoly && isWideOutperformer) "SECULAR TAILWIND COMPOUNDER" else "CYCLICAL / COMMODITY SENSITIVE", valueColor = if (isOligopoly && isWideOutperformer) GainGreen else WarningAmber)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Macro Business Cycle Clock Selector
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("1. MACRO BUSINESS CYCLE CLOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Where is the sector currently situated in the economic sequence?", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = cyclePhaseName,
                    variant = if (simSectorCyclePhase == 2) BadgeVariant.GAIN else if (simSectorCyclePhase == 0) BadgeVariant.LOSS else BadgeVariant.INFO
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Segmented Cycle Phase Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val phases = listOf("Recession", "Recovery", "Mid-Cycle", "Late-Cycle")
                phases.forEachIndexed { idx, label ->
                    val isSelected = simSectorCyclePhase == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) (if (idx == 0) LossRed else if (idx == 2) FintechPrimary else FintechSurfaceElevated) else FintechSurface)
                            .border(1.dp, if (isSelected) FintechPrimary else FintechBorder, RoundedCornerShape(8.dp))
                            .clickable { onCyclePhaseChange(idx) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) (if (idx == 0) Color.White else Color.Black) else TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (simSectorCyclePhase) {
                    0 -> "Recession Contraction: Corporate budgets freeze and GDP contracts. Secular software companies absorb minor churn, while capital-heavy industrials suffer 50%+ profit collapses."
                    1 -> "Early Recovery: Credit conditions loosen and consumer sentiment rebounds. Early cyclicals and financial institutions lead market compounding."
                    2 -> "Mid-Cycle Expansion: Healthy enterprise capital expenditure, surging tech adoption, and prolonged peak operating margins."
                    else -> "Late-Cycle Overheating: Inflation accelerates, interest rates peak, and commodity input shocks pressure manufacturing margins."
                },
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Industry Concentration (HHI Slider)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2. INDUSTRY CONCENTRATION (HHI)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Herfindahl-Hirschman Index: Oligopoly vs Fragmented Pricing", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (isOligopoly) "OLIGOPOLY UMBRELLA" else if (isFragmented) "PRICE DISCOUNTERS" else "MODERATE",
                    variant = if (isOligopoly) BadgeVariant.GAIN else if (isFragmented) BadgeVariant.LOSS else BadgeVariant.NEUTRAL
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Herfindahl-Hirschman Index (HHI Score)", fontSize = 10.sp, color = TextSecondary)
                Text("${simIndustryHhi.toInt()} HHI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isOligopoly) GainGreen else WarningAmber)
            }
            Slider(
                value = simIndustryHhi,
                onValueChange = onIndustryHhiChange,
                valueRange = 800f..4000f,
                steps = 32,
                colors = SliderDefaults.colors(
                    thumbColor = if (isOligopoly) GainGreen else if (isFragmented) LossRed else WarningAmber,
                    activeTrackColor = if (isOligopoly) GainGreen else if (isFragmented) LossRed else WarningAmber,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOligopoly)
                    "High Pricing Umbrella: Top 2-3 players control the market (HHI > 2,500). Competitors compete on quality rather than destructive price cuts, protecting 30%+ operating margins."
                else if (isFragmented)
                    "Cutthroat Fragmentation: Dozens of small commodity price-takers (HHI < 1,500). Excess industry capacity results in recurrent price wars where ROIC decays below WACC."
                else
                    "Moderate Concentration: Differentiated products with localized pricing umbrellas (e.g. consumer branded goods).",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Input Cost / Commodity Inflation Shock Slider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("3. COMMODITY / INPUT COST SHOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Testing margin defense when energy, labor, or raw materials spike", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = if (simInputCostShockPct <= 0f) "STABLE COSTS" else "+${simInputCostShockPct.toInt()}% INFLATION SHOCK",
                    variant = if (simInputCostShockPct > 15f) BadgeVariant.LOSS else if (simInputCostShockPct > 0f) BadgeVariant.NEUTRAL else BadgeVariant.GAIN
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Simulated Raw Material / Energy Cost Spike", fontSize = 10.sp, color = TextSecondary)
                Text("${if (simInputCostShockPct >= 0) "+" else ""}${simInputCostShockPct.toInt()}% Cost Spike", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simInputCostShockPct > 15f) LossRed else TextPrimary)
            }
            Slider(
                value = simInputCostShockPct,
                onValueChange = onInputCostShockChange,
                valueRange = 0.0f..30.0f,
                steps = 30,
                colors = SliderDefaults.colors(
                    thumbColor = if (simInputCostShockPct > 15f) LossRed else FintechPrimary,
                    activeTrackColor = if (simInputCostShockPct > 15f) LossRed else FintechPrimary,
                    inactiveTrackColor = FintechBorder
                ),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (simInputCostShockPct > 15f)
                    "Severe Cost Spike: Low-margin price-takers will see their margins wiped out completely. Secular oligopolies pass costs along via pricing power, preserving double-digit ROIC."
                else
                    "Manageable Cost Environment: Input costs remain within normal operating variance.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson12LiveLab(
    payload: DeepStockResearchPayload?,
    simStockPrice: Float,
    simTtmEps: Float,
    simFwdEps: Float,
    simGrowthRatePct: Float,
    simTreasuryYieldPct: Float,
    onStockPriceChange: (Float) -> Unit,
    onTtmEpsChange: (Float) -> Unit,
    onFwdEpsChange: (Float) -> Unit,
    onGrowthRateChange: (Float) -> Unit,
    onTreasuryYieldChange: (Float) -> Unit
) {
    val trailingPe = if (simTtmEps > 0f) simStockPrice / simTtmEps else 0.0f
    val forwardPe = if (simFwdEps > 0f) simStockPrice / simFwdEps else trailingPe
    val earningsYield = if (trailingPe > 0f) (1.0f / trailingPe) * 100.0f else 0.0f
    val equityRiskPremium = earningsYield - simTreasuryYieldPct
    val forwardCompressionPct = if (trailingPe > 0f) ((trailingPe - forwardPe) / trailingPe) * 100.0f else 0.0f
    val pegRatio = if (simGrowthRatePct > 0f) trailingPe / simGrowthRatePct else 99.0f

    val isGarp = pegRatio <= 1.2f && equityRiskPremium >= 0f
    val isFair = pegRatio <= 2.0f
    val isOvervalued = trailingPe > 40.0f || pegRatio > 2.5f
    val isValueTrap = simGrowthRatePct <= 0f

    StockticsMetricRow(label = "Simulated Share Price", value = "$${String.format(Locale.US, "%.2f", simStockPrice)}", valueColor = FintechPrimary)
    StockticsMetricRow(label = "Trailing P/E (TTM Multiple)", value = "${String.format(Locale.US, "%.1f", trailingPe)}x (${if (trailingPe <= 20f) "Value Channel" else if (trailingPe <= 30f) "Fair Multiple" else "Premium Channel"})", valueColor = if (trailingPe <= 25f) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Forward P/E (NTM Multiple)", value = "${String.format(Locale.US, "%.1f", forwardPe)}x (${if (forwardCompressionPct >= 10f) "-${forwardCompressionPct.toInt()}% Compression" else "Limited Compression"})", valueColor = FintechBlue)
    StockticsMetricRow(label = "Earnings Yield (E/P Inversion)", value = "${String.format(Locale.US, "%.2f", earningsYield)}%", valueColor = if (earningsYield >= 4.5f) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Equity Risk Premium vs 10Y Yield", value = "${if (equityRiskPremium >= 0) "+" else ""}${String.format(Locale.US, "%.2f", equityRiskPremium)}% vs 10Y (${String.format(Locale.US, "%.2f", simTreasuryYieldPct)}%)", valueColor = if (equityRiskPremium >= 0f) GainGreen else LossRed)
    StockticsMetricRow(label = "Peter Lynch PEG Ratio", value = "${String.format(Locale.US, "%.2f", pegRatio)}x (${if (isGarp) "Bargain (PEG < 1.2)" else if (isFair) "Fair Value (1.2 - 2.0)" else "Elevated (> 2.0)"})", valueColor = if (isGarp) GainGreen else if (isFair) FintechBlue else LossRed)
    StockticsMetricRow(label = "Valuation Multiple Classification", value = if (isValueTrap) "VALUE TRAP HAZARD" else if (isGarp) "TIER-1 ATTRACTIVELY VALUED (GARP)" else if (isFair) "FAIR VALUE SECULAR COMPOUNDER" else "SPECULATIVE VALUATION PREMIUM", valueColor = if (isGarp) GainGreen else if (isFair) FintechBlue else LossRed)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Stock Price & EPS Sliders Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("1. PRICE & DILUTED EARNINGS (P/E)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Testing multiple expansion and trailing valuation", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "${String.format(Locale.US, "%.1f", trailingPe)}x P/E",
                    variant = if (trailingPe <= 22f) BadgeVariant.GAIN else if (trailingPe <= 32f) BadgeVariant.INFO else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Market Share Price ($)", fontSize = 10.sp, color = TextSecondary)
                Text("$${simStockPrice.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simStockPrice,
                onValueChange = onStockPriceChange,
                valueRange = 20.0f..500.0f,
                steps = 48,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // TTM EPS Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Trailing 12M Diluted EPS ($)", fontSize = 10.sp, color = TextSecondary)
                Text("$${String.format(Locale.US, "%.2f", simTtmEps)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Slider(
                value = simTtmEps,
                onValueChange = onTtmEpsChange,
                valueRange = 1.0f..25.0f,
                steps = 48,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // FWD EPS Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Consensus Forward 12M EPS ($)", fontSize = 10.sp, color = TextSecondary)
                Text("$${String.format(Locale.US, "%.2f", simFwdEps)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simFwdEps,
                onValueChange = onFwdEpsChange,
                valueRange = 1.0f..30.0f,
                steps = 58,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Growth Rate & Treasury Yield Benchmark Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2. PEG & EQUITY RISK PREMIUM HURDLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Benchmarking against 10Y US Treasury and growth speed", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "${if (equityRiskPremium >= 0) "+" else ""}${String.format(Locale.US, "%.1f", equityRiskPremium)}% ERP",
                    variant = if (equityRiskPremium >= 0f) BadgeVariant.GAIN else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5-Year EPS Growth Rate Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("5-Year Projected EPS Growth Rate (%)", fontSize = 10.sp, color = TextSecondary)
                Text("${simGrowthRatePct.toInt()}% CAGR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simGrowthRatePct > 15f) GainGreen else WarningAmber)
            }
            Slider(
                value = simGrowthRatePct,
                onValueChange = onGrowthRateChange,
                valueRange = 0.0f..40.0f,
                steps = 40,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 10-Year Treasury Yield Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("10-Year US Treasury Risk-Free Yield (%)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.2f", simTreasuryYieldPct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simTreasuryYieldPct,
                onValueChange = onTreasuryYieldChange,
                valueRange = 1.0f..7.0f,
                steps = 60,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isGarp)
                    "Growth at a Reasonable Price: PEG is below 1.2x and the Earnings Yield clears the risk-free rate, establishing an institutional margin of safety."
                else if (isOvervalued)
                    "Valuation Warning: Elevated multiple or PEG > 2.0x requires sustained double-digit compounding without any earnings misses."
                else
                    "Fair Compounder Zone: Multiple reflects quality; monitor execution and quarterly forward revisions.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson13LiveLab(
    payload: DeepStockResearchPayload?,
    simMarketCap: Float,
    simTotalDebt: Float,
    simCash: Float,
    simRevenue: Float,
    simEbitda: Float,
    simEbit: Float,
    onMarketCapChange: (Float) -> Unit,
    onTotalDebtChange: (Float) -> Unit,
    onCashChange: (Float) -> Unit,
    onRevenueChange: (Float) -> Unit,
    onEbitdaChange: (Float) -> Unit,
    onEbitChange: (Float) -> Unit
) {
    val netDebt = simTotalDebt - simCash
    val ev = simMarketCap + netDebt
    val ps = if (simRevenue > 0f) simMarketCap / simRevenue else 0f
    val evSales = if (simRevenue > 0f) ev / simRevenue else 0f
    val evEbitda = if (simEbitda > 0f) ev / simEbitda else 0f
    val evEbit = if (simEbit > 0f) ev / simEbit else 0f
    val debtToEvPct = if (ev > 0f && netDebt > 0f) (netDebt / ev) * 100f else 0f

    val isCashFortress = netDebt < 0f
    val isBalanced = evEbitda in 8f..22f && debtToEvPct <= 35f
    val isOverleveraged = debtToEvPct > 50f || evEbitda > 30f

    StockticsMetricRow(label = "Equity Market Capitalization", value = "\$${String.format(Locale.US, "%.1f", simMarketCap)}B", valueColor = FintechPrimary)
    StockticsMetricRow(label = "Net Funded Debt (Debt - Cash)", value = "${if (netDebt >= 0) "+" else ""}\$${String.format(Locale.US, "%.1f", netDebt)}B (${if (isCashFortress) "Net Cash Fortress" else "Funded Liability"})", valueColor = if (isCashFortress) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Enterprise Value (EV)", value = "\$${String.format(Locale.US, "%.1f", ev)}B (${if (ev < simMarketCap) "EV < Market Cap" else "+${String.format(Locale.US, "%.1f", ev - simMarketCap)}B Debt Load"})", valueColor = FintechBlue)
    StockticsMetricRow(label = "Price-to-Sales (P/S Multiple)", value = "${String.format(Locale.US, "%.2f", ps)}x", valueColor = TextPrimary)
    StockticsMetricRow(label = "EV/Sales (Debt-Adjusted)", value = "${String.format(Locale.US, "%.2f", evSales)}x (${if (evSales > ps * 1.5f) "Substantial Debt Expansion" else "Neutral Top-Line"})", valueColor = if (evSales <= 4.0f) GainGreen else WarningAmber)
    StockticsMetricRow(label = "EV/EBITDA Multiple", value = "${String.format(Locale.US, "%.1f", evEbitda)}x (${if (evEbitda <= 14f) "Attractive Value" else if (evEbitda <= 24f) "Fair Multiple" else "Premium Channel"})", valueColor = if (evEbitda <= 18f) GainGreen else if (evEbitda <= 26f) FintechBlue else LossRed)
    StockticsMetricRow(label = "EV/EBIT (Capital Charge Adjusted)", value = "${String.format(Locale.US, "%.1f", evEbit)}x", valueColor = TextSecondary)
    StockticsMetricRow(label = "Enterprise Classification", value = if (isOverleveraged) "HIGH LEVERAGE BURDEN" else if (isCashFortress && isBalanced) "TIER-1 NET CASH FORTRESS" else if (isBalanced) "BALANCED EV COMPOUNDER" else "PREMIUM MULTIPLE", valueColor = if (isBalanced || isCashFortress) GainGreen else if (isOverleveraged) LossRed else FintechBlue)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Enterprise Value Anatomy Sliders Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("1. ENTERPRISE VALUE BRIDGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Market Cap + Funded Debt - Cash Reserves", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "\$${String.format(Locale.US, "%.0f", ev)}B EV",
                    variant = if (isCashFortress) BadgeVariant.GAIN else if (debtToEvPct > 45f) BadgeVariant.LOSS else BadgeVariant.INFO
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Market Cap Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Market Capitalization (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simMarketCap.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simMarketCap,
                onValueChange = onMarketCapChange,
                valueRange = 10.0f..500.0f,
                steps = 49,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Total Debt Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Funded Debt (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simTotalDebt.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simTotalDebt > simCash) LossRed else GainGreen)
            }
            Slider(
                value = simTotalDebt,
                onValueChange = onTotalDebtChange,
                valueRange = 0.0f..200.0f,
                steps = 40,
                colors = SliderDefaults.colors(thumbColor = LossRed, activeTrackColor = LossRed, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Cash Reserves Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cash & Short-Term Reserves (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simCash.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GainGreen)
            }
            Slider(
                value = simCash,
                onValueChange = onCashChange,
                valueRange = 0.0f..100.0f,
                steps = 20,
                colors = SliderDefaults.colors(thumbColor = GainGreen, activeTrackColor = GainGreen, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Operational Earnings Sliders Card (Revenue, EBITDA, EBIT)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2. OPERATIONAL MULTIPLES (EV/EBITDA & EV/EBIT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Capital structure neutrality test", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "${String.format(Locale.US, "%.1f", evEbitda)}x EV/EBITDA",
                    variant = if (evEbitda <= 15f) BadgeVariant.GAIN else if (evEbitda <= 25f) BadgeVariant.INFO else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Revenue Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Annual Revenue (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simRevenue.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simRevenue,
                onValueChange = onRevenueChange,
                valueRange = 5.0f..250.0f,
                steps = 49,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // EBITDA Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Annual EBITDA (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${String.format(Locale.US, "%.1f", simEbitda)}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Slider(
                value = simEbitda,
                onValueChange = onEbitdaChange,
                valueRange = 1.0f..60.0f,
                steps = 59,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // EBIT Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Annual EBIT Operating Profit (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${String.format(Locale.US, "%.1f", simEbit)}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
            }
            Slider(
                value = simEbit,
                onValueChange = onEbitChange,
                valueRange = 0.5f..40.0f,
                steps = 79,
                colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isCashFortress)
                    "Net Cash Fortress: Surplus cash reduces enterprise valuation, providing downside protection and capital deployment flexibility."
                else if (debtToEvPct > 40f)
                    "Leverage Warning: Over 40% of enterprise cost is debt liabilities. Equity returns are highly vulnerable to refinancing cycles."
                else
                    "Capital Structure Neutrality: EV multiples capture true earning power independent of corporate debt financing.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun Lesson14LiveLab(
    payload: DeepStockResearchPayload?,
    simOcf: Float,
    simTotalCapex: Float,
    simMaintCapexPct: Float,
    simMarketCap: Float,
    simNetIncome: Float,
    simTreasuryYieldPct: Float,
    onOcfChange: (Float) -> Unit,
    onTotalCapexChange: (Float) -> Unit,
    onMaintCapexPctChange: (Float) -> Unit,
    onMarketCapChange: (Float) -> Unit,
    onNetIncomeChange: (Float) -> Unit,
    onTreasuryYieldChange: (Float) -> Unit
) {
    val fcf = simOcf - simTotalCapex
    val maintCapex = simTotalCapex * (simMaintCapexPct / 100f)
    val growthCapex = simTotalCapex - maintCapex
    val estDa = maintCapex * 0.95f
    val ownerEarnings = simNetIncome + estDa - maintCapex

    val fcfYield = if (simMarketCap > 0f) (fcf / simMarketCap) * 100f else 0f
    val ownerEarningsYield = if (simMarketCap > 0f) (ownerEarnings / simMarketCap) * 100f else 0f
    val priceToFcf = if (fcf > 0f) simMarketCap / fcf else 99f
    val cashConversionPct = if (simNetIncome > 0f) (fcf / simNetIncome) * 100f else 0f
    val cashSpreadVsTreasury = fcfYield - simTreasuryYieldPct

    val isCashMachine = fcf > 0f && cashConversionPct >= 90f && fcfYield >= simTreasuryYieldPct
    val isCapitalConsumer = fcf <= 0f || cashConversionPct < 40f
    val isAttractiveYield = fcfYield >= 5.0f

    StockticsMetricRow(label = "Operating Cash Flow (CFO)", value = "\$${String.format(Locale.US, "%.1f", simOcf)}B", valueColor = TextPrimary)
    StockticsMetricRow(label = "Total CapEx (Maint + Growth)", value = "\$${String.format(Locale.US, "%.1f", simTotalCapex)}B (Maint: \$${String.format(Locale.US, "%.1f", maintCapex)}B)", valueColor = if (simTotalCapex > simOcf * 0.5f) WarningAmber else TextSecondary)
    StockticsMetricRow(label = "Free Cash Flow (CFO - CapEx)", value = "${if (fcf >= 0) "+" else ""}\$${String.format(Locale.US, "%.1f", fcf)}B", valueColor = if (fcf >= 0) GainGreen else LossRed)
    StockticsMetricRow(label = "Buffett Owner Earnings", value = "\$${String.format(Locale.US, "%.1f", ownerEarnings)}B (${String.format(Locale.US, "%.2f", ownerEarningsYield)}% Yield)", valueColor = FintechBlue)
    StockticsMetricRow(label = "Free Cash Flow Yield (FCF/Cap)", value = "${String.format(Locale.US, "%.2f", fcfYield)}% Yield", valueColor = if (isAttractiveYield) GainGreen else if (fcfYield >= 3.0f) WarningAmber else LossRed)
    StockticsMetricRow(label = "Price / Free Cash Flow", value = "${if (fcf > 0f) String.format(Locale.US, "%.1f", priceToFcf) + "x" else "N/A (Negative FCF)"}", valueColor = if (priceToFcf in 1f..22f) GainGreen else FintechBlue)
    StockticsMetricRow(label = "Cash Conversion (FCF / Net Income)", value = "${String.format(Locale.US, "%.1f", cashConversionPct)}% Conversion", valueColor = if (cashConversionPct >= 90f) GainGreen else WarningAmber)
    StockticsMetricRow(label = "Cash Spread vs 10Y US Treasury", value = "${if (cashSpreadVsTreasury >= 0) "+" else ""}${String.format(Locale.US, "%.2f", cashSpreadVsTreasury)}% vs 10Y (${String.format(Locale.US, "%.2f", simTreasuryYieldPct)}%)", valueColor = if (cashSpreadVsTreasury >= 0) GainGreen else LossRed)
    StockticsMetricRow(label = "Cash Generation Classification", value = if (isCapitalConsumer) "CAPITAL TREADMILL / CASH CONSUMER" else if (isCashMachine) "TIER-1 DISCRETIONARY CASH MACHINE" else "MODERATE CASH GENERATOR", valueColor = if (isCashMachine) GainGreen else if (isCapitalConsumer) LossRed else FintechBlue)

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Cash Funnel Sliders Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("1. DISCRETIONARY CASH FUNNEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("CFO minus CapEx and maintenance split", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "${if (fcf >= 0) "+" else ""}\$${String.format(Locale.US, "%.0f", fcf)}B FCF",
                    variant = if (fcf >= 0) BadgeVariant.GAIN else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Operating Cash Flow Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Operating Cash Flow CFO (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simOcf.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechPrimary)
            }
            Slider(
                value = simOcf,
                onValueChange = onOcfChange,
                valueRange = 10.0f..150.0f,
                steps = 70,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Total CapEx Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Capital Expenditures CapEx (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${String.format(Locale.US, "%.1f", simTotalCapex)}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (simTotalCapex > simOcf * 0.5f) LossRed else WarningAmber)
            }
            Slider(
                value = simTotalCapex,
                onValueChange = onTotalCapexChange,
                valueRange = 2.0f..60.0f,
                steps = 58,
                colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Maintenance CapEx % Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Maintenance CapEx (% of Total)", fontSize = 10.sp, color = TextSecondary)
                Text("${simMaintCapexPct.toInt()}% (\$${String.format(Locale.US, "%.1f", maintCapex)}B)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simMaintCapexPct,
                onValueChange = onMaintCapexPctChange,
                valueRange = 20.0f..95.0f,
                steps = 75,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Valuation Yield & Treasury Hurdle Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FintechBackground)
            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2. FCF YIELD & VALUATION HURDLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Benchmarked against 10Y US Treasury Yield", fontSize = 9.sp, color = TextSecondary)
                }
                StockticsBadge(
                    text = "${String.format(Locale.US, "%.2f", fcfYield)}% Yield",
                    variant = if (cashSpreadVsTreasury >= 0) BadgeVariant.GAIN else BadgeVariant.LOSS
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Market Cap Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Market Capitalization (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simMarketCap.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Slider(
                value = simMarketCap,
                onValueChange = onMarketCapChange,
                valueRange = 50.0f..3500.0f,
                steps = 69,
                colors = SliderDefaults.colors(thumbColor = FintechPrimary, activeTrackColor = FintechPrimary, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Net Income Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Reported GAAP Net Income (\$B)", fontSize = 10.sp, color = TextSecondary)
                Text("\$${simNetIncome.toInt()}B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechBlue)
            }
            Slider(
                value = simNetIncome,
                onValueChange = onNetIncomeChange,
                valueRange = 5.0f..120.0f,
                steps = 23,
                colors = SliderDefaults.colors(thumbColor = FintechBlue, activeTrackColor = FintechBlue, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 10Y Treasury Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("10-Year US Treasury Yield Benchmark (%)", fontSize = 10.sp, color = TextSecondary)
                Text("${String.format(Locale.US, "%.2f", simTreasuryYieldPct)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
            }
            Slider(
                value = simTreasuryYieldPct,
                onValueChange = onTreasuryYieldChange,
                valueRange = 1.0f..7.0f,
                steps = 60,
                colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber, inactiveTrackColor = FintechBorder),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isCashMachine)
                    "Elite Cash Machine: Free Cash Flow conversion exceeds 90% of accounting profit and yields a positive excess return over risk-free bonds."
                else if (isCapitalConsumer)
                    "Capital Consumer Alert: Business requires excessive CapEx to maintain operations, leaving negative or minimal cash for shareholders."
                else
                    "Compounding Cash Flow: Sustainable cash yields support organic buybacks and dividend distributions.",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    onStockSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val userId = remember(context) { UserSessionManager.getLoggedInUserId(context) }

    // --- State: Active Lesson & Completion Tracking ---
    var activeLessonNumber by remember { mutableStateOf<Int?>(null) }
    val currentLesson = remember(activeLessonNumber) {
        ALL_40_LESSONS.firstOrNull { it.lessonNumber == activeLessonNumber } ?: ALL_40_LESSONS[0]
    }
    val lessonListState = rememberLazyListState()

    // Ensure scrolling to top when active lesson changes
    LaunchedEffect(activeLessonNumber) {
        if (activeLessonNumber != null) {
            lessonListState.scrollToItem(0)
        }
    }

    // Intercept back button to return to catalog when viewing a lesson
    BackHandler(enabled = activeLessonNumber != null) {
        activeLessonNumber = null
    }

    var completedLessons by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load completed lessons from database
    LaunchedEffect(userId) {
        completedLessons = withContext(Dispatchers.IO) {
            StockticsDatabase.getCompletedLessons(context, userId)
        }
    }

    // --- State: Stock Selection & Live Data ---
    var selectedSymbol by remember { mutableStateOf("AAPL") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<StockSearchItem>>(emptyList()) }

    var researchData by remember(selectedSymbol) {
        mutableStateOf<DeepStockResearchPayload?>(YahooFinanceClient.getInstantPayload(selectedSymbol))
    }
    var isLoadingData by remember(selectedSymbol) {
        mutableStateOf(researchData == null)
    }

    // Interactive Slippage Simulator state (Lesson 1)
    var simulatedOrderShares by remember { mutableFloatStateOf(100f) }

    // Interactive 3-Statement & FCF Simulator state (Lesson 2)
    var simRevenueB by remember { mutableFloatStateOf(245f) }
    var simNetMarginPct by remember { mutableFloatStateOf(36f) }
    var simCfoRatioPct by remember { mutableFloatStateOf(135f) }
    var simCapexB by remember { mutableFloatStateOf(44f) }
    var statementTabIndex by remember { mutableIntStateOf(0) }

    // Interactive Growth Decomposition state (Lesson 3)
    var simOrganicGrowthPct by remember { mutableFloatStateOf(14f) }
    var simAcquiredGrowthPct by remember { mutableFloatStateOf(6f) }
    var simChurnPct by remember { mutableFloatStateOf(4f) }

    // Interactive Operating Leverage Simulator state (Lesson 4)
    var simDolRevChangePct by remember { mutableFloatStateOf(10f) }
    var simFixedCostRatioPct by remember { mutableFloatStateOf(55f) }

    // Interactive SBC Dilution vs Buyback Simulator state (Lesson 5)
    var simNetIncomeGrowthPct by remember { mutableFloatStateOf(15f) }
    var simSbcDilutionPct by remember { mutableFloatStateOf(3f) }
    var simBuybackPct by remember { mutableFloatStateOf(4f) }

    // Interactive Capital Allocation state (Lesson 6)
    var simCapExAllocPct by remember { mutableFloatStateOf(40f) }
    var simBuybackAllocPct by remember { mutableFloatStateOf(30f) }
    var simDividendAllocPct by remember { mutableFloatStateOf(15f) }
    var simDebtAllocPct by remember { mutableFloatStateOf(15f) }

    // Interactive Balance Sheet & Solvency Stress Test (Lesson 7)
    var simRateHikeBps by remember { mutableFloatStateOf(150f) }
    var simDebtPaydownB by remember { mutableFloatStateOf(10f) }

    // Interactive 3-Stage DuPont & ROIC Simulator state (Lesson 8)
    var simNetMarginDuPont by remember { mutableFloatStateOf(24f) }
    var simAssetTurnover by remember { mutableFloatStateOf(1.07f) }
    var simEquityMultiplier by remember { mutableFloatStateOf(2.5f) }
    var simWaccHurdle by remember { mutableFloatStateOf(8.5f) }

    // Interactive Economic Moats & Pricing Power Simulator state (Lesson 9)
    var simPricingPowerPct by remember { mutableFloatStateOf(8.0f) }
    var simChurnRatePct by remember { mutableFloatStateOf(1.2f) }
    var simScaleAdvantagePct by remember { mutableFloatStateOf(12.0f) }
    var simCustomerConcentrationPct by remember { mutableFloatStateOf(6.0f) }

    // Interactive Management Quality & Capital Allocation state (Lesson 10)
    var simAnnualBuybackBillions by remember { mutableFloatStateOf(80f) }
    var simL10SbcDilutionPct by remember { mutableFloatStateOf(1.5f) }
    var simBuybackPriceToFcf by remember { mutableFloatStateOf(22.0f) }
    var simInsiderOwnershipPct by remember { mutableFloatStateOf(12.0f) }

    // Interactive Industry & Sector Dynamics state (Lesson 11)
    var simL11CyclePhase by remember { mutableIntStateOf(2) }
    var simL11IndustryHhi by remember { mutableFloatStateOf(2650f) }
    var simL11RelativeMarginSpreadPct by remember { mutableFloatStateOf(16.5f) }
    var simL11InputCostShockPct by remember { mutableFloatStateOf(0.0f) }

    // Interactive P/E Ratio & Earnings Yield state (Lesson 12)
    var simL12StockPrice by remember { mutableFloatStateOf(192.20f) }
    var simL12TtmEps by remember { mutableFloatStateOf(8.04f) }
    var simL12FwdEps by remember { mutableFloatStateOf(9.80f) }
    var simL12GrowthRatePct by remember { mutableFloatStateOf(22.0f) }
    var simL12TreasuryYieldPct by remember { mutableFloatStateOf(4.25f) }

    // Interactive EV Multiples & Enterprise Value state (Lesson 13)
    var simL13MarketCap by remember { mutableFloatStateOf(295.0f) }
    var simL13TotalDebt by remember { mutableFloatStateOf(12.5f) }
    var simL13Cash by remember { mutableFloatStateOf(14.2f) }
    var simL13Revenue by remember { mutableFloatStateOf(35.0f) }
    var simL13Ebitda by remember { mutableFloatStateOf(8.9f) }
    var simL13Ebit by remember { mutableFloatStateOf(6.2f) }

    // Interactive Free Cash Flow & Owner Earnings state (Lesson 14)
    var simL14Ocf by remember { mutableFloatStateOf(118.0f) }
    var simL14TotalCapex by remember { mutableFloatStateOf(9.5f) }
    var simL14MaintCapexPct by remember { mutableFloatStateOf(65.0f) }
    var simL14MarketCap by remember { mutableFloatStateOf(3400.0f) }
    var simL14NetIncome by remember { mutableFloatStateOf(100.0f) }
    var simL14TreasuryYieldPct by remember { mutableFloatStateOf(4.25f) }

    // Infographic Full-Screen Preview Dialog state
    var isViewingFullInfographic by remember { mutableStateOf(false) }

    // Fetch live deep research data when symbol changes
    LaunchedEffect(selectedSymbol) {
        val cached = YahooFinanceClient.getInstantPayload(selectedSymbol)
        if (cached != null) {
            researchData = cached
            isLoadingData = false
        } else {
            isLoadingData = true
        }

        try {
            val live = YahooFinanceClient.fetchDeepStockResearch(selectedSymbol)
            researchData = live
        } catch (_: Exception) {
            // retain cached
        } finally {
            isLoadingData = false
        }
    }

    // Live Search effect
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            isSearching = true
            try {
                val local = StockticsDatabase.searchLocalStocks(context, q, isIndian = null)
                val online = withContext(Dispatchers.IO) {
                    YahooFinanceClient.searchStocksOnline(q, isIndian = null)
                }
                searchResults = if (online.isNotEmpty()) online else local
            } catch (_: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
    ) {
        if (activeLessonNumber == null) {
            CurriculumCatalogView(
                completedLessons = completedLessons,
                onSelectLesson = { lessonNum ->
                    activeLessonNumber = lessonNum
                }
            )
        } else {
            LazyColumn(
                state = lessonListState,
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
            // --- 1. TOP BAR WITH BACK BUTTON, TITLE & MENU ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Top-Left Back Button to return to Catalog
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FintechSurface)
                                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                                    .clickable { activeLessonNumber = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back to All Lessons",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LESSON ${currentLesson.lessonNumber} • STAGE ${currentLesson.stageNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = FintechPrimary
                                )
                                Text(
                                    text = currentLesson.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Drawer Menu Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechSurface)
                                .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                                .clickable { isDrawerOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Open Curriculum Menu",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val currentProgressRatio = (completedLessons.size.toFloat() / 40f).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechSurface)
                            .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Curriculum Mastery Progress",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(currentProgressRatio * 100).toInt()}% Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { currentProgressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = FintechPrimary,
                            trackColor = FintechSurfaceElevated
                        )
                    }
                }
            }

            // --- 4. ACTIVE LESSON HEADER & STAGE CARD ---
            item {
                val isLessonDone = completedLessons.contains(currentLesson.lessonNumber)
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LESSON ${currentLesson.lessonNumber} OF 40",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechPrimary,
                                letterSpacing = 1.sp
                            )
                            StockticsBadge(
                                text = if (isLessonDone) "Completed ✓" else "In Progress",
                                variant = if (isLessonDone) BadgeVariant.GAIN else BadgeVariant.MUTED
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentLesson.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = currentLesson.stageTitle,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Core Question Callout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechSurfaceElevated)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "THE CORE RESEARCH QUESTION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLesson.coreQuestion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Neat Mathematical Formulations & Worked Examples
                        val lessonFormulas = remember(currentLesson.lessonNumber) {
                            getLessonFormulas(currentLesson.lessonNumber, currentLesson.primaryFormula)
                        }

                        if (lessonFormulas.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "MATHEMATICAL FORMULATIONS & WORKED EXAMPLES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                lessonFormulas.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(FintechBackground)
                                            .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                StockticsBadge(
                                                    text = "Formula",
                                                    variant = BadgeVariant.MUTED
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.formula,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FintechPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.example,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. VISUAL PROCESS FLOW OR WATERFALL (ONLY WHEN APPLICABLE) ---
            val visualFlow = getLessonVisualFlow(currentLesson.lessonNumber)
            if (visualFlow != null) {
                item {
                    StockticsVisualFlowCard(visualFlow = visualFlow)
                }
            }

            // --- 6. DETAILED NOTES & CONCEPT DEEP DIVE ---
            item {
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = "COMPREHENSIVE CONCEPT BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentLesson.fullNotesOverview,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Key concepts table
                        currentLesson.conceptsSummary.forEach { (concept, desc) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• $concept",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                )
                            }
                        }


                    }
                }
            }

            // --- 7. LIVE STOCK METRICS LAB (SEARCH & SIMULATOR) ---
            item {
                val payload = researchData
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE DATA LAB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechPrimary,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Search & analyze live stocks for this lesson",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            StockticsBadge(
                                text = payload?.symbol ?: selectedSymbol,
                                variant = BadgeVariant.INFO
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar inside Live Data Lab
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechSurfaceElevated)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(FintechPrimary),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search any stock to analyze (e.g. AAPL, NVDA)...",
                                                color = TextTertiary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            focusManager.clearFocus()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Autocomplete Search Results
                        AnimatedVisibility(
                            visible = searchResults.isNotEmpty() && searchQuery.isNotBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            StockticsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            ) {
                                Column {
                                    searchResults.take(4).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSymbol = item.symbol
                                                    searchQuery = ""
                                                    focusManager.clearFocus()
                                                    keyboardController?.hide()
                                                }
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StockCompanyLogo(
                                                    symbol = item.symbol,
                                                    initials = item.symbol.take(2),
                                                    logoUrl = item.logoUrl,
                                                    size = 24.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = item.symbol,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = item.name,
                                                        fontSize = 10.sp,
                                                        color = TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.price,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Ticker Chips inside Live Data Lab
                        val quickChips = listOf("AAPL", "NVDA", "MSFT", "GOOGL", "AMZN", "TSLA", "BRK-B", "PLTR")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickChips) { sym ->
                                val isSelected = selectedSymbol.equals(sym, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                                        .border(1.dp, if (isSelected) FintechPrimary else FintechBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedSymbol = sym }
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = sym,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Active Stock Hero Strip inside Live Data Lab
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FintechBackground)
                                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    StockCompanyLogo(
                                        symbol = selectedSymbol,
                                        initials = selectedSymbol.take(2),
                                        logoUrl = payload?.logoUrl ?: "",
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = payload?.symbol ?: selectedSymbol,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StockticsBadge(
                                                text = payload?.exchange ?: "US",
                                                variant = BadgeVariant.MUTED
                                            )
                                        }
                                        Text(
                                            text = payload?.name ?: "Loading...",
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (isLoadingData) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = FintechPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        val priceVal = payload?.price ?: 0.0
                                        val chgPct = payload?.changePct ?: 0.0
                                        val isPos = chgPct >= 0
                                        Text(
                                            text = "$${String.format(Locale.US, "%,.2f", priceVal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isPos) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isPos) GainGreen else LossRed,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", chgPct)}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPos) GainGreen else LossRed
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val priceVal = payload?.price ?: 228.0

                        when (currentLesson.lessonNumber) {
                            1 -> Lesson1LiveLab(
                                payload = payload,
                                priceVal = priceVal,
                                simulatedOrderShares = simulatedOrderShares,
                                onSharesChange = { simulatedOrderShares = it }
                            )
                            2 -> Lesson2LiveLab(
                                payload = payload,
                                selectedSymbol = selectedSymbol,
                                simRevenueB = simRevenueB,
                                simNetMarginPct = simNetMarginPct,
                                simCfoRatioPct = simCfoRatioPct,
                                simCapexB = simCapexB,
                                statementTabIndex = statementTabIndex,
                                onTabChange = { statementTabIndex = it },
                                onRevenueChange = { simRevenueB = it },
                                onNetMarginChange = { simNetMarginPct = it },
                                onCfoRatioChange = { simCfoRatioPct = it },
                                onCapexChange = { simCapexB = it }
                            )
                            3 -> Lesson3LiveLab(
                                payload = payload,
                                simOrganicGrowthPct = simOrganicGrowthPct,
                                simAcquiredGrowthPct = simAcquiredGrowthPct,
                                simChurnPct = simChurnPct,
                                onOrganicChange = { simOrganicGrowthPct = it },
                                onAcquiredChange = { simAcquiredGrowthPct = it },
                                onChurnChange = { simChurnPct = it }
                            )
                            4 -> Lesson4LiveLab(
                                payload = payload,
                                simDolRevChangePct = simDolRevChangePct,
                                simFixedCostRatioPct = simFixedCostRatioPct,
                                onDolRevChange = { simDolRevChangePct = it },
                                onFixedCostRatioChange = { simFixedCostRatioPct = it }
                            )
                            5 -> Lesson5LiveLab(
                                payload = payload,
                                simNetIncomeGrowthPct = simNetIncomeGrowthPct,
                                simSbcDilutionPct = simSbcDilutionPct,
                                simBuybackPct = simBuybackPct,
                                onNetIncomeChange = { simNetIncomeGrowthPct = it },
                                onSbcChange = { simSbcDilutionPct = it },
                                onBuybackChange = { simBuybackPct = it }
                            )
                            6 -> Lesson6LiveLab(
                                payload = payload,
                                simCapExAllocPct = simCapExAllocPct,
                                simBuybackAllocPct = simBuybackAllocPct,
                                simDividendAllocPct = simDividendAllocPct,
                                simDebtAllocPct = simDebtAllocPct,
                                onCapExChange = { simCapExAllocPct = it },
                                onBuybackChange = { simBuybackAllocPct = it },
                                onDividendChange = { simDividendAllocPct = it },
                                onDebtChange = { simDebtAllocPct = it }
                            )
                            7 -> Lesson7LiveLab(
                                payload = payload,
                                simRateHikeBps = simRateHikeBps,
                                simDebtPaydownB = simDebtPaydownB,
                                onRateHikeChange = { simRateHikeBps = it },
                                onDebtPaydownChange = { simDebtPaydownB = it }
                            )
                            8 -> Lesson8LiveLab(
                                payload = payload,
                                simNetMarginDuPont = simNetMarginDuPont,
                                simAssetTurnover = simAssetTurnover,
                                simEquityMultiplier = simEquityMultiplier,
                                simWaccHurdle = simWaccHurdle,
                                onNetMarginChange = { simNetMarginDuPont = it },
                                onAssetTurnoverChange = { simAssetTurnover = it },
                                onEquityMultiplierChange = { simEquityMultiplier = it },
                                onWaccHurdleChange = { simWaccHurdle = it }
                            )
                            9 -> Lesson9LiveLab(
                                payload = payload,
                                simPricingPowerPct = simPricingPowerPct,
                                simChurnRatePct = simChurnRatePct,
                                simScaleAdvantagePct = simScaleAdvantagePct,
                                simCustomerConcentrationPct = simCustomerConcentrationPct,
                                onPricingPowerChange = { simPricingPowerPct = it },
                                onChurnRateChange = { simChurnRatePct = it },
                                onScaleAdvantageChange = { simScaleAdvantagePct = it },
                                onCustomerConcentrationChange = { simCustomerConcentrationPct = it }
                            )
                            10 -> Lesson10LiveLab(
                                payload = payload,
                                simAnnualBuybackBillions = simAnnualBuybackBillions,
                                simSbcDilutionPct = simL10SbcDilutionPct,
                                simBuybackPriceToFcf = simBuybackPriceToFcf,
                                simInsiderOwnershipPct = simInsiderOwnershipPct,
                                onAnnualBuybackChange = { simAnnualBuybackBillions = it },
                                onSbcDilutionChange = { simL10SbcDilutionPct = it },
                                onBuybackPriceChange = { simBuybackPriceToFcf = it },
                                onInsiderOwnershipChange = { simInsiderOwnershipPct = it }
                            )
                            11 -> Lesson11LiveLab(
                                payload = payload,
                                simSectorCyclePhase = simL11CyclePhase,
                                simIndustryHhi = simL11IndustryHhi,
                                simRelativeMarginSpreadPct = simL11RelativeMarginSpreadPct,
                                simInputCostShockPct = simL11InputCostShockPct,
                                onCyclePhaseChange = { simL11CyclePhase = it },
                                onIndustryHhiChange = { simL11IndustryHhi = it },
                                onRelativeMarginSpreadChange = { simL11RelativeMarginSpreadPct = it },
                                onInputCostShockChange = { simL11InputCostShockPct = it }
                            )
                            12 -> Lesson12LiveLab(
                                payload = payload,
                                simStockPrice = simL12StockPrice,
                                simTtmEps = simL12TtmEps,
                                simFwdEps = simL12FwdEps,
                                simGrowthRatePct = simL12GrowthRatePct,
                                simTreasuryYieldPct = simL12TreasuryYieldPct,
                                onStockPriceChange = { simL12StockPrice = it },
                                onTtmEpsChange = { simL12TtmEps = it },
                                onFwdEpsChange = { simL12FwdEps = it },
                                onGrowthRateChange = { simL12GrowthRatePct = it },
                                onTreasuryYieldChange = { simL12TreasuryYieldPct = it }
                            )
                            13 -> Lesson13LiveLab(
                                payload = payload,
                                simMarketCap = simL13MarketCap,
                                simTotalDebt = simL13TotalDebt,
                                simCash = simL13Cash,
                                simRevenue = simL13Revenue,
                                simEbitda = simL13Ebitda,
                                simEbit = simL13Ebit,
                                onMarketCapChange = { simL13MarketCap = it },
                                onTotalDebtChange = { simL13TotalDebt = it },
                                onCashChange = { simL13Cash = it },
                                onRevenueChange = { simL13Revenue = it },
                                onEbitdaChange = { simL13Ebitda = it },
                                onEbitChange = { simL13Ebit = it }
                            )
                            14 -> Lesson14LiveLab(
                                payload = payload,
                                simOcf = simL14Ocf,
                                simTotalCapex = simL14TotalCapex,
                                simMaintCapexPct = simL14MaintCapexPct,
                                simMarketCap = simL14MarketCap,
                                simNetIncome = simL14NetIncome,
                                simTreasuryYieldPct = simL14TreasuryYieldPct,
                                onOcfChange = { simL14Ocf = it },
                                onTotalCapexChange = { simL14TotalCapex = it },
                                onMaintCapexPctChange = { simL14MaintCapexPct = it },
                                onMarketCapChange = { simL14MarketCap = it },
                                onNetIncomeChange = { simL14NetIncome = it },
                                onTreasuryYieldChange = { simL14TreasuryYieldPct = it }
                            )
                            else -> Lesson1LiveLab(
                                payload = payload,
                                priceVal = priceVal,
                                simulatedOrderShares = simulatedOrderShares,
                                onSharesChange = { simulatedOrderShares = it }
                            )
                        }
                    }
                }
            }

            // --- 8. REAL & FICTIONAL CASE STUDIES ---
            if (currentLesson.fictionalExample != null || currentLesson.realStockExample != null) {
                item {
                    StockticsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Column {
                            if (currentLesson.fictionalExample != null) {
                                Text(
                                    text = "FICTIONAL CASE STUDY: COUNTERFACTUAL SCENARIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLesson.fictionalExample!!,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }

                            if (currentLesson.realStockExample != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "REAL-WORLD SEC AUDITED CASE STUDY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLesson.realStockExample!!,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 9. THINK LIKE A RESEARCHER ---
            item {
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "THINK LIKE A RESEARCHER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Researcher Rule:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = currentLesson.researcherRule,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "What This Data Does NOT Prove:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                        Text(
                            text = currentLesson.whatDataDoesNotProve,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // --- 10. COMMON MISTAKES & GOLDEN RULES ---
            item {
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = "COMMON MISTAKES TO AVOID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LossRed,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        currentLesson.commonMistakes.forEach { (mistake, why) ->
                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = "✕ $mistake",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = why,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "GOLDEN RULES TO FOLLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechPrimary,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        currentLesson.goldenRules.forEach { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = FintechPrimary,
                                    modifier = Modifier
                                        .size(13.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rule,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 8. LESSON INFOGRAPHIC CHEAT SHEET (END OF LESSON) ---
            if (currentLesson.assetInfographicPath != null) {
                item {
                    StockticsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = FintechPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LESSON INFOGRAPHIC CHEAT SHEET",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechPrimary,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                IconButton(
                                    onClick = { isViewingFullInfographic = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Fullscreen,
                                        contentDescription = "Full Screen",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Framed Infographic Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FintechBackground)
                                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                                    .clickable { isViewingFullInfographic = true }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentLesson.assetInfographicPath)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Lesson Infographic",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.85f),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FintechSurface.copy(alpha = 0.9f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Fullscreen,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Tap to Expand",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 9. END ACTION BUTTONS (SAME ROW, EQUAL WIDTH) ---
            item {
                val isDone = completedLessons.contains(currentLesson.lessonNumber)
                val hasNextLesson = currentLesson.lessonNumber < 40

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Mark Complete
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDone) GainGreen else FintechPrimary)
                            .clickable {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        StockticsDatabase.toggleLessonCompletion(context, currentLesson.lessonNumber, userId)
                                        completedLessons = StockticsDatabase.getCompletedLessons(context, userId)
                                    }
                                }
                            }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDone) "Completed" else "Mark Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1
                            )
                        }
                    }

                    // Button 2: Deep Research
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FintechSurfaceElevated)
                            .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onStockSelected(researchData?.symbol ?: selectedSymbol)
                            }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deep Research",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    // Button 3: Next Lesson
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasNextLesson) FintechSurfaceElevated else FintechSurface)
                            .border(1.dp, if (hasNextLesson) FintechBorder else FintechBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(enabled = hasNextLesson) {
                                val nextNum = currentLesson.lessonNumber + 1
                                activeLessonNumber = nextNum
                                scope.launch {
                                    lessonListState.scrollToItem(0)
                                }
                            }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (hasNextLesson) "Next Lesson" else "End",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasNextLesson) TextPrimary else TextTertiary,
                                maxLines = 1
                            )
                            if (hasNextLesson) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MODAL DRAWER / BOTTOM SHEET: 40 LESSONS DIRECTORY ---
        if (isDrawerOpen) {
            ModalBottomSheet(
                onDismissRequest = { isDrawerOpen = false },
                sheetState = sheetState,
                containerColor = FintechSurface,
                dragHandle = null
            ) {
                CurriculumDrawerContent(
                    selectedLessonNumber = currentLesson.lessonNumber,
                    completedLessons = completedLessons,
                    onLessonSelect = { lessonNum ->
                        activeLessonNumber = lessonNum
                        scope.launch {
                            sheetState.hide()
                            isDrawerOpen = false
                            lessonListState.scrollToItem(0)
                        }
                    },
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                            isDrawerOpen = false
                        }
                    }
                )
            }
        }

        // --- FULL-SCREEN INFOGRAPHIC PREVIEW DIALOG ---
        if (isViewingFullInfographic && currentLesson.assetInfographicPath != null) {
            Dialog(
                onDismissRequest = { isViewingFullInfographic = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentLesson.assetInfographicPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full Screen Infographic",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { isViewingFullInfographic = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 24.dp, end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FintechSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        } // end of else (activeLessonNumber != null)
    }
}

/**
 * Slide-out Curriculum Directory Drawer displaying all 40 lessons and 8 stages.
 */
@Composable
fun CurriculumDrawerContent(
    selectedLessonNumber: Int,
    completedLessons: Set<Int>,
    onLessonSelect: (Int) -> Unit,
    onClose: () -> Unit
) {
    var selectedStageFilter by remember { mutableIntStateOf(0) } // 0 = All stages
    var filterQuery by remember { mutableStateOf("") }

    val filteredLessons = remember(selectedStageFilter, filterQuery) {
        ALL_40_LESSONS.filter { lesson ->
            val stageMatch = selectedStageFilter == 0 || lesson.stageNumber == selectedStageFilter
            val queryMatch = filterQuery.isBlank() ||
                lesson.title.contains(filterQuery, ignoreCase = true) ||
                lesson.coreQuestion.contains(filterQuery, ignoreCase = true) ||
                "Lesson ${lesson.lessonNumber}".contains(filterQuery, ignoreCase = true)
            stageMatch && queryMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .background(FintechSurface)
            .padding(top = 16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CURRICULUM DIRECTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = FintechPrimary
                )
                Text(
                    text = "40 Lessons Across 8 Stages",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Drawer",
                    tint = TextSecondary
                )
            }
        }

        // Progress Pill
        val doneCount = completedLessons.size
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress: $doneCount of 40 completed",
                fontSize = 12.sp,
                color = TextSecondary
            )
            StockticsBadge(
                text = "${(doneCount * 100) / 40}% Mastered",
                variant = if (doneCount > 0) BadgeVariant.GAIN else BadgeVariant.MUTED
            )
        }

        // Search in Curriculum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FintechSurfaceElevated)
                .border(1.dp, FintechBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                    cursorBrush = SolidColor(FintechPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (filterQuery.isEmpty()) {
                            Text("Filter lessons by topic...", color = TextTertiary, fontSize = 12.sp)
                        }
                        inner()
                    }
                )
            }
        }

        // Stage Tabs (Horizontal scroll)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isSelected = selectedStageFilter == 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                        .clickable { selectedStageFilter = 0 }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "All Stages",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else TextPrimary
                    )
                }
            }
            items(CURRICULUM_STAGES) { stage ->
                val isSelected = selectedStageFilter == stage.stageNumber
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                        .clickable { selectedStageFilter = stage.stageNumber }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Stage ${stage.stageNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 40 Lessons List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredLessons) { lesson ->
                val isCurrent = lesson.lessonNumber == selectedLessonNumber
                val isCompleted = completedLessons.contains(lesson.lessonNumber)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) FintechSurfaceElevated else FintechBackground)
                        .border(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) FintechPrimary else FintechBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onLessonSelect(lesson.lessonNumber) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Completion Checkbox Icon
                            Icon(
                                imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isCompleted) GainGreen else TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Lesson ${lesson.lessonNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) FintechPrimary else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Stage ${lesson.stageNumber}",
                                        fontSize = 10.sp,
                                        color = TextTertiary
                                    )
                                }
                                Text(
                                    text = lesson.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Select",
                            tint = if (isCurrent) FintechPrimary else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


/**
 * Standalone Curriculum Catalog / Library View.
 * Displays all 40 quantitative lessons grouped with search, stage filters, and progress tracking.
 */
@Composable
fun CurriculumCatalogView(
    completedLessons: Set<Int>,
    onSelectLesson: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStageFilter by remember { mutableIntStateOf(0) } // 0 = All stages
    var filterQuery by remember { mutableStateOf("") }

    val filteredLessons = remember(selectedStageFilter, filterQuery) {
        ALL_40_LESSONS.filter { lesson ->
            val stageMatch = selectedStageFilter == 0 || lesson.stageNumber == selectedStageFilter
            val queryMatch = filterQuery.isBlank() ||
                lesson.title.contains(filterQuery, ignoreCase = true) ||
                lesson.coreQuestion.contains(filterQuery, ignoreCase = true) ||
                lesson.stageTitle.contains(filterQuery, ignoreCase = true) ||
                lesson.primaryFormula.contains(filterQuery, ignoreCase = true) ||
                "Lesson ${lesson.lessonNumber}".contains(filterQuery, ignoreCase = true)
            stageMatch && queryMatch
        }
    }

    val nextLessonToLearn = remember(completedLessons) {
        (1..40).firstOrNull { !completedLessons.contains(it) } ?: 1
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUANTITATIVE ACADEMY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = FintechPrimary
                        )
                        Text(
                            text = "Curriculum Library",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    StockticsBadge(
                        text = "${completedLessons.size}/40 Mastered",
                        variant = if (completedLessons.isNotEmpty()) BadgeVariant.GAIN else BadgeVariant.INFO
                    )
                }
            }
        }

        // 1B. QUANT TRADING ACADEMY HERO BANNER
        item {
            val isDark = IsDarkTheme
            val bannerBorderColor = if (isDark) FintechBorder else Color(0xFFCBD5E1)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(175.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF0B0E14) else FintechSurface)
                    .border(1.dp, bannerBorderColor, RoundedCornerShape(16.dp))
            ) {
                val academyHeroRes = if (isDark) com.stocktics.R.drawable.hero_academy_trading else com.stocktics.R.drawable.hero_academy_trading_light
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = academyHeroRes),
                    contentDescription = "Quant Academy Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Theme-adaptive vertical scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    Color(0x33070A0F),
                                    Color(0x660B0E14),
                                    Color(0xF2070A0F)
                                ) else listOf(
                                    Color(0x40FFFFFF),
                                    Color(0x80FFFFFF),
                                    Color(0xF5F8FAFC)
                                )
                            )
                        )
                )

                // Top-Right: Institutional Quant Mastery badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xCC141923) else Color(0xEEFFFFFF))
                        .border(1.dp, if (isDark) FintechPrimary.copy(alpha = 0.5f) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FintechPrimary)
                        )
                        Text(
                            text = "QUANT MASTERY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isDark) Color.White else TextPrimary
                        )
                    }
                }

                // Bottom Content with Next Lesson Quick Action
                val nextLessonObj = remember(nextLessonToLearn) {
                    ALL_40_LESSONS.firstOrNull { it.lessonNumber == nextLessonToLearn } ?: ALL_40_LESSONS[0]
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "QUANTITATIVE RESEARCH ACADEMY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.3.sp,
                        color = if (isDark) FintechPrimary else Color(0xFF0F47D6)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wall Street & Dalal Street Alpha Curriculum",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Glassmorphic Quick Continue Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xCC0E131F) else Color(0xF5FFFFFF))
                            .border(1.dp, if (isDark) Color(0x33475569) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .clickable { onSelectLesson(nextLessonToLearn) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Lesson $nextLessonToLearn: ${nextLessonObj.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color(0xFFE2E8F0) else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (completedLessons.isEmpty()) "START" else "CONTINUE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechPrimary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Progress & Resume Learning Card
        item {
            val progressRatio = (completedLessons.size.toFloat() / 40f).coerceIn(0f, 1f)
            StockticsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FintechPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = FintechPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Curriculum Mastery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${completedLessons.size} of 40 lessons completed",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = FintechPrimary,
                        trackColor = FintechSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resume / Start CTA Button
                    StockticsPrimaryButton(
                        text = if (completedLessons.isEmpty()) "Start Learning — Lesson 1" else "Resume Lesson $nextLessonToLearn →",
                        onClick = { onSelectLesson(nextLessonToLearn) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(FintechPrimary),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (filterQuery.isEmpty()) {
                                Text(
                                    text = "Search 40 lessons by title, formula, or concept...",
                                    color = TextTertiary,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    )
                    if (filterQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { filterQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Horizontal Stage Filters
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    val isSelected = selectedStageFilter == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FintechPrimary else FintechBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedStageFilter = 0 }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "All Lessons (40)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }

                items(CURRICULUM_STAGES) { stage ->
                    val isSelected = selectedStageFilter == stage.stageNumber
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FintechPrimary else FintechSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FintechPrimary else FintechBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedStageFilter = stage.stageNumber }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Stage ${stage.stageNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }

        // 5. Lessons Count Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedStageFilter == 0) "All Modules (${filteredLessons.size})" else CURRICULUM_STAGES.firstOrNull { it.stageNumber == selectedStageFilter }?.title ?: "Modules",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "Tap to open",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }

        // 6. Lessons List
        if (filteredLessons.isEmpty()) {
            item {
                StockticsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Lessons Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "No modules matched your search query '$filterQuery'",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StockticsSecondaryButton(
                            text = "Clear Search",
                            onClick = {
                                filterQuery = ""
                                selectedStageFilter = 0
                            }
                        )
                    }
                }
            }
        } else {
            items(filteredLessons) { lesson ->
                val isCompleted = completedLessons.contains(lesson.lessonNumber)
                CurriculumLessonCard(
                    lesson = lesson,
                    isCompleted = isCompleted,
                    onClick = { onSelectLesson(lesson.lessonNumber) }
                )
            }
        }
    }
}

/**
 * Individual Card in the Curriculum Catalog representing one module.
 */
@Composable
fun CurriculumLessonCard(
    lesson: FullLessonDefinition,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StockticsCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Lesson badge, Stage tag, Completed status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FintechPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LESSON ${lesson.lessonNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = FintechPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stage ${lesson.stageNumber}",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                if (isCompleted) {
                    StockticsBadge(
                        text = "Completed ✓",
                        variant = BadgeVariant.GAIN
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "10 min read",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lesson Title
            Text(
                text = lesson.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Core Question / Subtitle
            Text(
                text = lesson.coreQuestion,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            // Primary Formula Snippet (if non-empty)
            if (lesson.primaryFormula.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechSurfaceElevated)
                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = lesson.primaryFormula,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = FintechPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom CTA row
            HorizontalDivider(color = FintechBorder, thickness = 0.8.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lesson.keyCheckpoints.size} Core Concepts • Practical Checklist",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCompleted) "Review Lesson" else "Start Lesson",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = FintechPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
