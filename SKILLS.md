# Hisaab — Skill Usage Rules

This file governs **when and how** each installed skill must be invoked during development.
All agents working on this project MUST read this file before taking action.

---

## Installed Skills

| Skill | Source | Install Count | Invoke When |
|---|---|---|---|
| `impeccable` | pbakaus/impeccable | — | Any UI design, layout, component, or theming work |
| `android-jetpack-compose` | thebushidocollective/han | 1.1K | Writing or reviewing any `@Composable` function |
| `migrate-xml-views-to-jetpack-compose` | android/skills | 282 | Converting any legacy XML View to Compose |
| `gemini-api` | google/skills | 3.1K | Implementing any Gemini API call (Tier 3 fallback, InsightAgent, GeminiParserFallback) |
| `kotlin-testing` | affaan-m/everything-claude-code | 3.1K | Writing unit tests or integration tests for any Kotlin class |
| `lottie` | heygen-com/hyperframes | 18.6K | Integrating or configuring any Lottie animation JSON |
| `mobile-design` | sickn33/antigravity-awesome-skills | 2.3K | Designing or auditing any mobile screen layout |
| `grill-me` | mattpocock/skills | — | Before finalizing any major architectural or product decision |
| `find-skills` | vercel-labs/skills | — | When a new capability gap is identified during development |

---

## Mandatory Rules

### Rule 1: DESIGN.md is the source of truth for all UI
Before writing any Jetpack Compose UI code, load `DESIGN.md` and `PRODUCT.md`.
All color values, typography specs, corner radii, spacing, and component behavior
are defined there. **Never invent new design tokens inline.**

Invoke: `impeccable` (runs `load-context.mjs` automatically)

### Rule 2: Gemini API calls go through the skill
Any interaction with the Gemini API — whether in `GeminiParserFallback.kt`,
`GeminiAgentService.kt`, or direct tool calls in `InsightAgent` — must follow
the patterns defined in the `gemini-api` skill. This ensures proper error handling,
exponential backoff, and cost-aware batching.

Invoke: `gemini-api` skill before writing any `generativeModel.generateContent()` call.

### Rule 3: Every parser has a test
Every new parser in `parser-core` requires a corresponding unit test before it is
considered done. Tests must cover: happy path, malformed input, edge amounts,
Unicode/Urdu sender names, and the idempotency of `ParserUtils.deterministicId`.

Invoke: `kotlin-testing` skill when writing any `*ParserTest.kt`.

### Rule 4: Compose components use the design system
Every `@Composable` must use tokens from `HisaabTheme` — never hardcoded color
values or raw `dp` values that bypass the spacing scale.

Invoke: `android-jetpack-compose` skill for component patterns; `impeccable` for design review.

### Rule 5: Lottie animations follow the sourcing plan
The 6 required Lottie JSONs are sourced from LottieFiles.com (free tier).
Color tinting is applied programmatically via `LottieAnimationView.addValueCallback()`
to match `#7B61FF` (primary purple) and `#FFB830` (amber). Never embed custom
After Effects exports without the team's approval.

Invoke: `lottie` skill when integrating any `.json` Lottie file.

### Rule 6: Mobile design audit before demo
Before Day 7 polish, run an `impeccable audit` against all 5 screens.
Any screen that fails the AI slop test (predictable fintech palette, identical card grids,
hero-metric template, glassmorphism) must be revised.

Invoke: `impeccable audit` on `HomeScreen.kt`, `InsightsScreen.kt`, `AgentScreen.kt`,
`ConflictScreen.kt`, `SimulationView.kt`.

### Rule 7: Grill before major decisions
Any decision that changes: agent architecture, privacy model, institution coverage claims,
demo data strategy, or UI screen count — must be grilled first.

Invoke: `grill-me` with the specific decision as context.

---

## Skill Layering (for complex tasks)

Some tasks require multiple skills in sequence:

### Building a new Compose screen:
1. `impeccable shape [screen name]` — UX plan and design brief
2. `android-jetpack-compose` — implementation patterns
3. `mobile-design` — mobile-first layout review
4. `impeccable audit [screen name]` — final quality check

### Implementing a new agent tool:
1. `gemini-api` — API call patterns and error handling
2. `kotlin-testing` — write the test first (TDD)
3. `grill-me` — stress-test the tool's edge cases

### Adding a Lottie animation:
1. `lottie` — sourcing and integration guidance
2. `android-jetpack-compose` — composable wrapper pattern

---

## Design Constraints (from DESIGN.md + impeccable)

These are non-negotiable and apply to every piece of UI code in this project:

- **No `CardDefaults.cardElevation()` with nonzero values.** All elevation is tonal.
- **No gradient text.** Balance amounts are solid `#F5F5FF`.
- **No glassmorphism.** No `Modifier.blur()` on card backgrounds.
- **One purple CTA per screen.** If there are two, one is wrong.
- **Agent trace = JetBrains Mono, always.**
- **Institution chips = institution brand color, always.**
- **Clash Display = one figure per screen only.**
