# 📱 Task: Android Auth (Login/Signup) & Floating Pill Navigation Bar

> **Status**: 🟢 Completed / In Active Development
> **Scope**: Implement modern, sleek email/password authentication (Login & Signup), user session state, and the floating pill style Bottom Navigation Bar connecting the 5 primary application screens (Home, Buy, Sell, Explore, Settings).

---

## 📋 Task Checklist

- [x] **1. UI Theme & Financial Design System**
  - [x] Define modern dark theme palette (Deep OLED `#0B0E14`, Glassmorphic Card `#161B26`, Neon Bullish `#00E676`, Neon Bearish `#FF5252`, Accent Cyan `#00E5FF`, Purple Gradient `#7C4DFF`).
  - [x] Configure typography and smooth component shape tokens.

- [x] **2. Authentication Screens (Login & Signup)**
  - [x] Create modern Glassmorphism Auth Card with Stocktics branding & glow effects.
  - [x] Implement Email and Password TextFields with validation and visibility toggle.
  - [x] Implement tab toggle between "Sign In" and "Create Account".
  - [x] Support email/password verification and transition directly to the Home dashboard.
  - [x] Provide test accounts / instant fill for frictionless developer testing.

- [x] **3. Floating Pill Bottom Navigation Bar**
  - [x] Design floating pill navigation container with rounded pill shape (`CircleShape` / `RoundedCornerShape(32.dp)`), glassmorphic border, and elevation.
  - [x] Implement 5 core navigation items:
    1. 🏠 **Home** (`HomeTab`)
    2. 🟢 **Buy** (`BuyTab`)
    3. 🔴 **Sell** (`SellTab`)
    4. 🔎 **Explore** (`ExploreTab`)
    5. ⚙️ **Settings** (`SettingsTab`)
  - [x] Add smooth animated pill indicator sliding to the active tab with vibrant icon & label highlight.

- [x] **4. Screen Placeholders & Modern Content Hubs**
  - [x] 🏠 **Home Screen**: Portfolio value card, urgent risk alerts, top buy picks, market indices.
  - [x] 🟢 **Buy Screen**: Watchlist pipeline cards with 0–100 Buy Scores and 5-factor breakdown.
  - [x] 🔴 **Sell Screen**: Active positions with P&L %, "Time to Leave" scores, and stop-loss proximity.
  - [x] 🔎 **Explore Screen**: Search bar with debounced query & strategy screener chips.
  - [x] ⚙️ **Settings Screen**: User profile info, scoring engine weight sliders, and Logout button.

---

## 🎯 Acceptance Criteria
- [x] User can switch between Login and Signup seamlessly.
- [x] Successful login transitions directly into the Home dashboard.
- [x] Floating pill navbar floats elegantly above screen content with padding.
- [x] Tapping navbar items switches tabs instantly with smooth animated transitions.
- [x] User can log out from Settings and return to the Login screen.
