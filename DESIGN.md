---
name: Hisaab
description: Pakistan's first AI-native financial intelligence app — refined dark intelligence.
colors:
  primary: "#7B61FF"
  primary-dim: "#5A45CC"
  accent-teal: "#00D4AA"
  accent-teal-dim: "#00A882"
  accent-amber: "#FFB830"
  accent-amber-dim: "#CC8E1A"
  accent-red: "#FF4B6E"
  neutral-bg-base: "#0D0D14"
  neutral-bg-secondary: "#13131F"
  neutral-surface: "#1C1C2E"
  neutral-surface-elevated: "#252538"
  neutral-surface-overlay: "#2E2E45"
  text-primary: "#F5F5FF"
  text-secondary: "#A0A0C0"
  text-muted: "#60607A"
  text-disabled: "#3C3C52"
  border-subtle: "#252538"
  hbl: "#006B3C"
  jazzcash: "#D4002A"
  easypaisa: "#43B02A"
  nayapay: "#5B2D8E"
  sadapay: "#0066FF"
  meezan: "#006747"
  alfalah: "#E4002B"
  mcb: "#009B77"
  upaisa: "#FF6B00"
  zindigi: "#6C2BD9"
typography:
  display:
    fontFamily: "Clash Display, sans-serif"
    fontWeight: 700
    fontSize: "32sp"
    lineHeight: 1.1
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Clash Display, sans-serif"
    fontWeight: 600
    fontSize: "20sp"
    lineHeight: 1.2
    letterSpacing: "-0.01em"
  body:
    fontFamily: "DM Sans, sans-serif"
    fontWeight: 400
    fontSize: "15sp"
    lineHeight: 1.5
  label:
    fontFamily: "JetBrains Mono, monospace"
    fontWeight: 400
    fontSize: "12sp"
    lineHeight: 1.4
    letterSpacing: "0.02em"
  urdu:
    fontFamily: "Noto Nastaliq Urdu, serif"
    fontWeight: 400
rounded:
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "24px"
  pill: "100px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  xxl: "48px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "14px 24px"
  button-secondary:
    backgroundColor: "{colors.neutral-surface-elevated}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "14px 24px"
  insight-card-warning:
    backgroundColor: "{colors.neutral-surface}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.lg}"
    padding: "20px"
  insight-card-positive:
    backgroundColor: "{colors.neutral-surface}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.lg}"
    padding: "20px"
  balance-card:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.xl}"
    padding: "24px"
  conflict-banner:
    backgroundColor: "{colors.accent-amber}"
    textColor: "{colors.neutral-bg-base}"
    rounded: "{rounded.md}"
    padding: "16px"
  institution-chip:
    backgroundColor: "{colors.neutral-surface-elevated}"
    textColor: "{colors.text-secondary}"
    rounded: "{rounded.pill}"
    padding: "6px 12px"
  agent-trace-row:
    backgroundColor: "{colors.neutral-bg-secondary}"
    textColor: "{colors.accent-teal}"
    rounded: "{rounded.sm}"
    padding: "12px 16px"
---

# Design System: Hisaab

## 1. Overview

**Creative North Star: "The Intelligence Terminal"**

Hisaab presents financial data the way a trading terminal presents markets — with total authority, density, and zero decoration. The darkness isn't aesthetic; it's functional: a dim room, a glowing screen, numbers that matter. The user is not a consumer scrolling a feed. They are a decision-maker scanning for signals.

This system is built on three resolved tensions: density without clutter, warmth without pastels, intelligence without jargon. Every purple CTA is a decision prompt. Every amber glow is a warning that deserves attention. Every JetBrains Mono line is the machine speaking plainly.

Anti-references: no neon crypto dashboards, no white-card fintech clones, no "friendly AI" interfaces with gradients and rounded blobs. This is not Nubank or Chime. This is a financial intelligence instrument.

**Key Characteristics:**
- Deep navy-black canvas: `#0D0D14` base, surfaces lift through `#1C1C2E` to `#252538`
- Accent purple (#7B61FF) is the single voice of action — used at most 10% of any screen
- Teal (#00D4AA) signals income and success. Amber (#FFB830) signals caution. Red (#FF4B6E) signals danger. Never swap these roles.
- Agent reasoning always rendered in JetBrains Mono, tree-formatted, visible — never hidden
- Clash Display numerals for balance amounts; DM Sans prose for everything else
- Institution chips carry their true brand colors — the UI is a map of Pakistan's financial ecosystem

## 2. Colors

A dark-field palette where lightness signals elevation and hue signals intent. The four accent colors each own a semantic role and are never decorative.

### Primary
- **Hisaab Purple** (#7B61FF): Primary brand color. CTAs, active nav states, focus indicators, primary buttons. Appears on at most 10% of any given screen. Its rarity is the point.
- **Purple Dim** (#5A45CC): Pressed/active state of the primary. Never used as a background.

### Secondary
- **Intelligence Teal** (#00D4AA): Income received, positive balance deltas, successful forecasts, resolved conflicts. Always signals "good news."
- **Teal Dim** (#00A882): Teal pressed state, teal text on dark surfaces for secondary emphasis.
- **Caution Amber** (#FFB830): Budget warnings, contradiction banners, spending alerts, "approaching limit" states. Always signals "attention required."
- **Amber Dim** (#CC8E1A): Amber text on dark surfaces, pressed amber states.
- **Danger Red** (#FF4B6E): Overspend, balance deficit, unresolved critical conflicts. Always signals "action required immediately."

### Neutral
- **Base** (#0D0D14): The app's bedrock. No content lives here — only the canvas behind everything.
- **Secondary BG** (#13131F): Screen backgrounds, list backgrounds, content areas.
- **Surface** (#1C1C2E): Card backgrounds, bottom sheets at rest, input fills.
- **Surface Elevated** (#252538): Focused cards, hover states, elevated modals, pressed chips.
- **Surface Overlay** (#2E2E45): The topmost layer — dialogs, drawers at their header.
- **Text Primary** (#F5F5FF): All high-emphasis text. Slightly blue-tinted white to harmonize with the palette.
- **Text Secondary** (#A0A0C0): Supporting text, metadata, labels.
- **Text Muted** (#60607A): Timestamps, disabled labels, tertiary info.
- **Text Disabled** (#3C3C52): Inactive states.
- **Border Subtle** (#252538): Dividers, card outlines when used.

### Institution Colors
Used exclusively when rendering institution-specific chips, transaction rows, or account cards:
`HBL #006B3C` · `JazzCash #D4002A` · `Easypaisa #43B02A` · `NayaPay #5B2D8E` · `SadaPay #0066FF` · `Meezan #006747` · `Alfalah #E4002B` · `MCB #009B77` · `UPaisa #FF6B00` · `Zindigi #6C2BD9`

### Named Rules
**The Single Voice Rule.** Purple (#7B61FF) is the only action color. It appears once per screen, on the primary CTA. A screen with two purple buttons has failed.

**The Semantic Lock Rule.** Teal means income/success. Amber means warning. Red means danger. These are not interchangeable for aesthetic variety. If you need a color and none of the three fit the meaning, use a neutral.

**The Institution Truth Rule.** Institution chips and transaction source badges must use the institution's actual brand color — not a Hisaab palette approximation. A JazzCash transaction is always red. An HBL deposit is always green.

## 3. Typography

**Display Font:** Clash Display (700 weight)
**Body Font:** DM Sans (400/500 weight)
**Mono Font:** JetBrains Mono (400 weight)
**Urdu Font:** Noto Nastaliq Urdu (400 weight, right-to-left)

**Character:** Clash Display brings authority to numbers; DM Sans carries conversational warmth in reasoning text; JetBrains Mono signals machine output without decoration.

### Hierarchy

- **Display** (Clash Display 700, 32sp, -0.02em tracking): Total balance, net worth figure. One per screen maximum.
- **Headline** (Clash Display 600, 20sp, -0.01em tracking): Card titles, section headers, insight amounts.
- **Title** (DM Sans 500, 17sp): Screen titles, modal headers, named categories.
- **Body** (DM Sans 400, 15sp, 1.5 line-height): Insight reasoning text, transaction descriptions, action explanations. Max line length: 72 characters.
- **Caption** (DM Sans 400, 13sp, text-secondary): Timestamps, metadata, institution names in transaction rows.
- **Label/Trace** (JetBrains Mono 400, 12sp, 0.02em tracking): Agent trace output, transaction IDs, confidence scores, tool call results.
- **Urdu Body** (Noto Nastaliq Urdu, 16sp): Any Urdu text content; right-to-left direction enforced.

### Named Rules
**The Transparent Trace Rule.** All agent reasoning output — workplans, task logs, tool call results, confidence scores — renders in JetBrains Mono with tree prefixes (`├─`, `└─`, `✅`, `⚠️`). This typography choice is the visual proof that "the machine is thinking, not guessing."

**The One Display Rule.** Clash Display is used for exactly one figure per screen — the primary number. Secondary amounts (e.g., category totals, institution balances) use DM Sans 500, not Clash Display.

## 4. Elevation

This system uses tonal elevation — not shadows. In a dark theme, shadows create visual noise; surface lightness creates hierarchy. The five surface levels form a Z-axis of lightness:

| Level | Token | Hex | Use |
|---|---|---|---|
| 0 — Base | `neutral-bg-base` | #0D0D14 | Canvas |
| 1 — Screen | `neutral-bg-secondary` | #13131F | Screen backgrounds |
| 2 — Surface | `neutral-surface` | #1C1C2E | Cards, inputs |
| 3 — Elevated | `neutral-surface-elevated` | #252538 | Active cards, chips |
| 4 — Overlay | `neutral-surface-overlay` | #2E2E45 | Dialogs, drawers |

**Accent glows** are the only exception: insight cards use a 1px border in their semantic accent color at 60% opacity, paired with a subtle radial glow (`spread: 0, blur: 24px, opacity: 0.15`) at the card's top edge. This is purposeful — it draws the eye to the card's emotional signal without competing with the content.

### Named Rules
**The No-Shadow Rule.** `elevation` in Material 3 Compose is set to 0dp on all surfaces. Depth is achieved exclusively through surface color progression. Any `@Composable` using `.shadow()` or `CardDefaults.cardElevation()` with a nonzero value has failed.

**The Glow-as-Signal Rule.** Accent glow borders are used only on insight cards and forecast cards, to communicate the card's semantic meaning (amber = warning, teal = positive). They are never used decoratively.

## 5. Components

### Balance Card (Flagship Component)
The full-width hero card on the Home screen. Its visual weight anchors the entire screen.
- **Background:** Purple (#7B61FF) fill, full-width, 24px corner radius.
- **Balance figure:** Clash Display 700, 36sp, text-primary white.
- **Delta indicator:** DM Sans 500, 14sp, teal for positive / red for negative.
- **Institution strip:** Row of institution chips (logo + abbreviated name) at the card's base.
- **Privacy toggle:** Eye icon (👁) in top-right. Taps blur/unblur the balance with a 200ms fade.

### Insight Card
Communicates the core value of the app. Must feel important but not alarming.
- **Corner Style:** 16px radius.
- **Background:** `neutral-surface` (#1C1C2E).
- **Accent edge:** 1px border in semantic accent color at 60% opacity + radial glow at 15% opacity.
- **Structure:** Icon + headline (DM Sans 500) + reasoning text (DM Sans 400) + single CTA button.
- **CTA:** "Take Action →" in primary purple text (not a filled button — a text link).

### Transaction Row
Dense, scannable, information-rich. No card wrapping — bare rows with subtle dividers.
- **Layout:** Institution color dot (8px circle) + Merchant name (DM Sans 500) + amount (DM Sans 500, right-aligned) + category + institution chip + timestamp.
- **Amount:** Teal for credit (+), Red for debit (-).
- **Divider:** 1px `border-subtle` (#252538).

### Institution Chip
Used inside balance cards and transaction rows to identify the source institution.
- **Shape:** Pill (100px radius), 28px height.
- **Background:** `neutral-surface-elevated`.
- **Content:** Institution's brand-color dot (10px) + abbreviated name in text-secondary.

### Conflict Banner
High-urgency, full-width. Must stop the user.
- **Background:** `accent-amber` (#FFB830) fill.
- **Text:** `neutral-bg-base` (#0D0D14) — maximum contrast.
- **Icon:** ⚠️ at 20sp.
- **Placement:** Pinned below the app bar, above all other content.

### Agent Trace View
The judge screen. Must feel like a terminal window, not a settings page.
- **Background:** `neutral-bg-secondary` (#13131F).
- **Tree lines:** JetBrains Mono, text-muted for `├─` `└─` prefixes, text-secondary for task names.
- **Status icons:** `✅` (teal) = complete, `⚠️` (amber) = conflict found, `⏳` (text-muted) = running.
- **Confidence scores:** JetBrains Mono, text-muted.
- **LIVE pill:** 6px radius, accent-teal fill, white text, pulsing opacity animation (1s loop, 0.4s ease).

### Primary Button
- **Background:** #7B61FF. **Text:** white, DM Sans 600, 15sp.
- **Pressed state:** #5A45CC, scale 0.97.
- **Disabled:** `neutral-surface-elevated`, text-disabled.
- **Corner radius:** 12px. **Height:** 52dp. **Full-width in modals**, content-width in rows.

### Bottom Navigation
5-tab bar: Home, Transactions, [+ FAB], Insights, Agent.
- **Background:** `neutral-bg-secondary` with a top border in `border-subtle`.
- **Active icon + label:** primary purple (#7B61FF).
- **FAB center button:** Purple circle (56dp), shadow-less, `+` icon, triggers quick-add flow.

## 6. Do's and Don'ts

### Do:
- **Do** use Clash Display exclusively for the one primary financial figure per screen.
- **Do** use JetBrains Mono for every line of agent output, trace, tool call, and confidence score.
- **Do** render institution chips with the institution's real brand color — always.
- **Do** elevate surfaces using the tonal lightness progression (#0D0D14 → #252538). Never use Material elevation shadows.
- **Do** use the semantic accent as a 1px border glow on insight cards — not as a fill.
- **Do** keep the purple CTA to one per screen. Two purple buttons means one is wrong.
- **Do** show the agent trace for every meaningful action. Visibility of reasoning is a feature, not a debug view.
- **Do** use the "LIVE" pulsing teal pill on the Agent Trace screen when the pipeline is running.

### Don't:
- **Don't** use bright, playful, noisy SaaS styles. Hisaab is an intelligence instrument, not a consumer app.
- **Don't** use overstimulating dashboards with excessive, identical cards. Each card type must be visually distinct and semantically unique.
- **Don't** hide agent reasoning. No "black box" loading states — show the workplan and task log at all times.
- **Don't** use gradient text (`background-clip: text`). Balance amounts are solid white. This is non-negotiable.
- **Don't** use glassmorphism. Blurred-glass cards are decorative noise on a dark background.
- **Don't** use `CardDefaults.cardElevation()` with nonzero values. All elevation is tonal.
- **Don't** swap the semantic accent colors. Amber is never used for income. Teal is never used for warnings.
- **Don't** use generic fintech templates (navy + gold, white + teal, big gradient metric cards). The impeccable AI slop test: if someone can guess the palette from the category "fintech," this system has failed.
- **Don't** use the hero-metric template (giant number, small label, gradient accent). The Balance Card is differentiated through institution context and the privacy toggle, not through decoration.
