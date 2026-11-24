---
trigger: always_on
---

# Stocktics UI/UX Design System & Engineering Rules
*Standardized for Tier-1 Financial Fintech (Modeled after Groww & INDmoney)*

---

## 1. Core Visual Principles

### 1.1 Institutional Fintech vs. "Vibe Code"
- **No Exaggerated Neon/Purple Glow**: Avoid large diffuse radial glow gradients, hyper-saturated neon borders, or gummy rounded aesthetics.
- **Matte & High Contrast**: Dark mode surfaces use deep obsidian black (`#0B0E14`), slate surface containers (`#141923`), and elevated interactive cards (`#1C2333`).
- **Hairline Precision**: Card borders and divider lines must be sharp, 1dp hairline strokes (`#222A3B`), giving an authoritative, institutional feel.
- **Restrained Purposeful Color**: Color is strictly functional. Green indicates profit/gain/positive consensus; Red indicates loss/risk/sell actions; Blue/Emerald indicates primary interactions.

---

## 2. Color Tokens

| Token Name | Hex Code | Description & Usage |
| :--- | :--- | :--- |
| `FintechBackground` | `#0B0E14` | True matte dark app root background. |
| `FintechSurface` | `#141923` | Primary container surface for cards and grouped modules. |
| `FintechSurfaceElevated` | `#1C2333` | Interactive elevated elements, input fields, pill backgrounds. |
| `FintechBorder` | `#222A3B` | Standard 1dp hairline border for cards and inputs. |
| `FintechBorderFocused` | `#00D09C` | Active/focused border indicator (Emerald). |
| `FintechPrimary` | `#00D09C` | Iconic Groww-grade Mint Emerald for primary CTAs and active states. |
| `FintechPrimaryDark` | `#00B386` | Pressed / hover state for primary action buttons. |
| `FintechBlue` | `#1455FE` | INDmoney-grade Tech Blue for exchange badges, secondary highlights. |
| `GainGreen` | `#00D09C` | Financial profit / bull metrics / high buy scores. |
| `GainGreenBg` | `#1A00D09C` | 10–12% opacity tinted background for profit pills. |
| `LossRed` | `#F04438` | Financial loss / bear metrics / sell triggers. |
| `LossRedBg` | `#1AF04438` | 10–12% opacity tinted background for loss pills. |
| `WarningAmber` | `#F59E0B` | Watchlist caution / neutral ratings. |
| `WarningAmberBg` | `#1AF59E0B` | 10–12% opacity tinted background for caution pills. |
| `TextPrimary` | `#F8FAFC` | 100% white-slate for headings, symbols, and core figures. |
| `TextSecondary` | `#94A3B8` | 70% slate for subtitles, company names, and field labels. |
| `TextTertiary` | `#64748B` | 50% muted slate for timestamps, exchange codes, footnotes. |

---

## 3. Typography & Financial Number Hierarchy

1. **Header / Big Currency**: 24sp–28sp `FontWeight.Bold`, letter spacing `-0.5sp`.
2. **Stock Symbol & Ticker**: 16sp–18sp `FontWeight.Bold`, uppercase.
3. **Card Section Titles**: 14sp–15sp `FontWeight.SemiBold`.
4. **Body & Company Description**: 12sp–13sp `FontWeight.Normal`, `TextSecondary`.
5. **Micro Metrics & Badges**: 10sp–11sp `FontWeight.Bold` or `FontWeight.SemiBold`.
6. **Tabular Figures**: All currency, percentage changes, and scores should render with monospace/tabular numeral alignment so numbers do not jump during real-time updates.

---

## 4. Standard Button Rules (`StockticsButton`)

- **Primary Action (Buy / Submit / Continue)**:
  - Height: Fixed **50dp** touch target.
  - Corner Radius: **12dp** (clean rounded rectangle, never a circle pill).
  - Background: `FintechPrimary` (`#00D09C`).
  - Text: `Color.Black` or bold `#0B0E14` for maximum contrast readability.
  - Loading State: Circular progress indicator centered inside without resizing or displacing the button container.
- **Secondary Action (Add to Watchlist / Outline)**:
  - Height: **46dp–50dp**.
  - Background: `FintechSurfaceElevated` with 1dp `FintechBorder`.
  - Text: `TextPrimary` with subtle icon.
- **Danger Action (Sell / Exit / Delete)**:
  - Background: `LossRed` (`#F04438`), Text: `Color.White`.
- **Text / Ghost Button**:
  - No border or background. Transparent with 8dp padding and `FintechPrimary` or `TextSecondary`.

---

## 5. Input Field Rules (`StockticsTextField`)

- **Container**:
  - Height: **52dp** touch target.
  - Corner Radius: **12dp**.
  - Background: `FintechSurface`.
  - Border: 1dp `FintechBorder` unselected; 1.5dp `FintechPrimary` when focused.
- **Labels & Icons**:
  - Clear floating or distinct upper label in `TextSecondary` (12sp).
  - Leading icon tinted `TextTertiary` (or `FintechPrimary` on focus).
  - Trailing icon for password visibility toggle or clear text action.
- **Error Feedback**:
  - Border turns `LossRed` (`#F04438`).
  - Red assistive error message rendered 4dp below the field in 11sp.

---

## 6. Card & List Module Rules (`StockticsCard`)

- **Corner Radius**: **14dp** to **16dp**.
- **Border**: 1dp `FintechBorder` (`#222A3B`).
- **Gutter Padding**: Internal card padding of **16dp** (horizontal) and **14dp** (vertical).
- **Inter-card Spacing**: **12dp** to **16dp** vertical separation.
- **Screen Margin Gutters**: **16dp** on small screens, **20dp** on standard screens.

---

## 7. Segmented Controls & Pills (`StockticsBadge`)

- **Financial Pill**:
  - Height: **22dp–24dp**.
  - Padding: `horizontal = 8.dp, vertical = 2.dp`.
  - Corner Radius: **6dp–8dp**.
  - Style: Tinted translucent background (e.g. `GainGreenBg`) with crisp solid foreground text (`GainGreen`).
  - Directional indicator: `Icons.AutoMirrored.Rounded.TrendingUp` or `TrendingDown` (12dp size).

---

## 8. Screen Layout & Navigation Bar Standard

- **Top Insets**: Every top-level screen MUST apply `Modifier.statusBarsPadding()`.
- **Bottom Navigation**: Floating pill navigation bar must use `Modifier.navigationBarsPadding()` and symmetrical `Modifier.weight(1f)` tab distribution.
- **Empty States**: Never show empty blank screens or random mock placeholders. Render a centralized `StockticsCard` explaining why the list is empty with an actionable `StockticsPrimaryButton` (e.g., *"Explore & Add Stocks"*).
