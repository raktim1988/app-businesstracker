# Android-First App Build Blueprint

**Status:** Discovery + technical blueprint  
**Prepared:** 17 November 2026  
**Decision scope:** Five candidate micro-apps; Android-first delivery  
**Audience:** Product owner, UX/UI designer, senior mobile engineer, backend engineer, QA, and AI coding agents

---

## 0. Executive recommendation

Do not build a generic utility app containing all five ideas. They solve different jobs, have different buyers, and require different product, data, and infrastructure models. Pick one narrowly defined user problem and make the first release excellent at that job.

### Recommended first experiment: a trade-specific calculator / estimator

This is the best *first build* on execution risk, time-to-value, offline capability, and low operating cost. It can be useful without accounts, a server, AI, or recurring cloud spend. The market is not empty: construction and woodworking calculators already exist. The opportunity is therefore not “another calculator”; it is a more focused workflow for a clearly defined user segment, with fewer steps, trustworthy formulas, local units/material presets, saved jobs, and genuinely useful exports.

**Suggested initial wedge to validate:** a woodworking / small-cabinet-shop cut-list and material estimator for metric-first users, with excellent mm/cm/m entry, board/sheet presets, kerf and waste controls, project reuse, and a clean printable cut plan. This is a hypothesis, not a final product decision. Existing apps already offer cut lists and optimization, so user interviews and competitor testing must validate a real gap before committing to an optimizer.

**Alternative if the team has strong access to local service businesses:** a booking companion can have stronger recurring revenue, but it is a much larger product with calendar conflicts, customer-facing booking, notifications, cancellation rules, and support expectations.

### What not to do in v1

- Do not build all five concepts in one app.
- Do not add an LLM merely because the architecture diagram includes AI.
- Do not introduce login/cloud sync until users demonstrate a need for cross-device persistence or sharing.
- Do not build a custom API gateway, microservices, Kubernetes, or a complex admin portal for a calculator MVP.
- Do not make subscriptions the default monetization for a utility that users may only open occasionally. Test a one-time Pro unlock first.

---

## 1. Source architecture diagram: interpretation and corrections

The supplied diagram proposes four layers:

1. **Client/UI:** React Native or Flutter, alternatively Next.js; user input/camera, state management, charts.
2. **API/gateway:** Clerk or Supabase Auth, rate limiting, routing; HTTPS/REST/JSON.
3. **Core backend:** PostgreSQL/Supabase, S3/R2 file storage, cron jobs.
4. **AI/external:** OpenAI/Gemini/Claude APIs and Stripe/RevenueCat payments.

This is a reasonable *general-purpose* architecture map, but it is overbuilt for several of the candidate products. A niche calculator can run almost entirely on-device. A habit tracker can also be local-first. Home inventory needs local database + image storage and may later need sync. Booking needs a real backend from day one because availability and double-booking are shared state. Pomodoro needs very little backend unless community features are introduced.

**Architecture principle:** choose infrastructure from the product's consistency, privacy, offline, and collaboration requirements—not from a generic technology checklist.

---

## 2. Candidate portfolio: market, pain, opportunity, monetization

Scores are directional product judgments (1 = weak, 5 = strong), not market-size estimates. Market examples and pricing/features below were checked against public product pages/search results in November 2026; app-store prices and feature limits can vary by country and change frequently.

| Candidate | Competitive pressure | MVP complexity | Backend need | Monetization fit | Main risk | Directional score |
|---|---:|---:|---:|---:|---|---:|
| Trade/hobby calculator | 3 | 1–2 | Low | One-time Pro / niche ads | Generic feature parity; formula trust | **4.3/5** |
| Micro-journal / habit tracker | 5 | 1–2 | Low initially | Freemium subscription | Crowded, weak differentiation, churn | 2.8/5 |
| Local-business booking | 5 | 4–5 | High | B2B monthly fee | Mature competitors, support and integration burden | 3.0/5 |
| Home inventory | 3–4 | 3 | Medium | Annual / one-time premium | Episodic use, photo storage, trust/privacy | 3.8/5 |
| Niche Pomodoro | 5 | 1–2 | Low initially | IAP/theme packs/subscription | Commodity timer, retention and CAC | 2.5/5 |

### 2.1 Trade / hobby calculator

**Existing landscape:** The Google Play listing for [Concrete Calculator](https://play.google.com/store/apps/details?id=com.pixelsdo.concretecalculator) reports 1M+ downloads and 10.9K reviews, with offline use, metric/imperial units, concrete, blocks, plastering, and rebar estimates. [Construction Calculator](https://play.google.com/store/apps/details?id=com.patchmedia.constructioncalculator) covers multiple material types, editable waste/cost assumptions, saved estimates and exports. In woodworking, [Woodworking Cut List](https://play.google.com/store/apps/details?id=com.patchmedia.woodworkingcutlist), [WoodCalc](https://play.google.com/store/apps/details?id=com.minossoft.woodcalc), and [Kerfo](https://play.google.com/store/apps/details?id=com.cutlist.optimizer) show that users can already find offline cut lists, material calculations, kerf/grain handling, and paid export/pro features.

**Pain points to validate, not assume:**
- Too many calculators or options on one screen; users need the one relevant calculation quickly.
- Input conventions are often trade-specific (fractions, metric dimensions, board sizes, decimal commas, kerf, waste, grain direction).
- Users need to understand *why* a result is what it is, not only see a number.
- Re-entering project dimensions and material prices is tedious.
- A result must be easy to share, print, or take to a supplier/workshop.
- Users need clear warnings when a calculation is an estimate and not an engineering design.

**Easy wins:** recent calculations, favourites, project templates, local material presets, user-defined waste/kerf, one-tap duplicate, share sheet, printable PDF, clear formula explanation, and no forced sign-up.

**Differentiation hypothesis:** “the fastest reliable calculator for [one trade] in metric-first workflows”, not “200 calculators for everyone”. A focused onboarding sample project and task-based home screen can outperform a large tool catalogue for a narrow audience.

**Monetization:**
- Free: core calculations, offline, recent history, basic share/text export.
- Pro one-time unlock: PDF/CSV export, unlimited saved projects/templates, advanced cost summaries, custom material library, no ads.
- Indicative test price: €4.99–€9.99 one-time; validate willingness to pay. Avoid committing to this price before testing.
- Ads: only if traffic is meaningful; never place an ad beside a critical numeric result or in the middle of an active workflow.

### 2.2 Micro-journal / habit tracker

**Existing landscape:** [Loop Habit Tracker](https://loophabits.org/) is a strong Android/offline/free benchmark; 2026 comparison pages also list Habitica, Habitify, HabitNow, and other established options. This is a crowded category with free alternatives and mature streak, reminder, chart, and widget experiences.

**Pain points to validate:** habit apps can become over-configurable, guilt-inducing after a missed day, notification-heavy, or too generic to support a specific routine. A micro-product must own one habit/use case and its workflow.

**Easy wins:** forgiving streak logic, “skip/rest day” semantics, two-tap daily completion, flexible schedules, discreet reminders, local-first storage, home-screen widget, and export that users can actually interpret.

**Monetization:** free single habit/basic history; €2–€4/month or annual plan for multiple routines, sync, advanced history, widgets/themes, and export. Subscription risk is high unless the app delivers ongoing value beyond a checkmark.

**Verdict:** only pursue with a sharply defined audience (e.g., a specific care routine or occupational workflow) and a distribution channel. A generic habit tracker is unlikely to win on features alone.

### 2.3 Local-business appointment / booking companion

**Existing landscape:** scheduling tools already provide booking pages, calendar sync, reminders, staff/resource support, payments, and integrations. A November 2026 comparison lists Setmore, SimplyBook.me, Square Appointments, Zoho Bookings, Acuity, Calendly, and Google Calendar with free/paid tiers. [2026 small-business booking comparison](https://fitsmallbusiness.com/airtable-crm/). Other current comparisons show products such as Goldie, Fresha, Booksy, Vagaro and others with solo-business plans.

**Pain points to validate:** solo operators may need less software, faster setup, WhatsApp-first workflows, deposits/no-show prevention, local-language templates, and a simpler client list. But “simple booking” is not enough if existing tools already provide it cheaply.

**Easy wins:** one booking page, working hours, service duration/buffer, manual booking, customer details, cancellation link, reminders, and a daily agenda. Avoid staff management, marketplace discovery, POS, payroll, and complex CRM in v1.

**Monetization:** B2B flat fee, test €9–€19/month per solo business; potentially a higher tier for multiple staff, reminders, and custom branding. SMS is a variable cost and should be metered or passed through. Do not underprice support and messaging costs.

**Verdict:** strongest recurring-revenue potential, but not the easiest app. Validate with 10–15 real operators and secure 3–5 design partners before coding.

### 2.4 Home inventory / insurance / moving log

**Existing landscape:** [HoardIQ](https://play.google.com/store/apps/details?id=com.hoardiq.app) lists free limits and a €/$4.99/month Premium plan with higher limits, AI analysis, insurance exports and family sharing. [Ownitory](https://play.google.com/store/apps/details?id=com.ownwise.app) positions itself around offline, no-account, no-tracking use and a one-time purchase. [InventoryMe](https://play.google.com/store/apps/details?id=fr.inventoryme.app) lists a free tier and premium from €4.25/month, with PDF/CSV exports and cloud backup. [Itemtopia](https://play.google.com/store/apps/details?id=com.itemtopia.app) offers item records, receipts, reminders, reports, sharing and AI features.

**Pain points to validate:** adding items is tedious; users forget to capture serial numbers/receipts; room/storage location organization matters; exports must be credible and complete; privacy and backup matter because the data is sensitive and difficult to recreate.

**Easy wins:** room-by-room capture, batch entry, camera-first flow, receipt/photo attachments, warranty reminder, offline mode, CSV export, clear PDF report, household sharing later.

**Monetization:** free limited inventory; €9.99–€19.99/year for larger inventories, cloud backup, family sharing and report exports, or a one-time export unlock. Annual utility pricing is plausible, but usage is episodic: test whether people value recurring backup/reminders enough to renew.

**Verdict:** credible second choice, especially if aimed at renters/homeowners in a specific market. Cloud storage, image upload, account deletion, and GDPR handling make it more complex than it first appears.

### 2.5 Niche Pomodoro / focus timer

**Existing landscape:** current 2026 comparisons include Forest, Focus To-Do, Flora, Tide, Study Bunny, Pomi and Pomofocus. [2026 Pomodoro comparison](https://pomifocus.com/blog/best-pomodoro-apps-2026.html) shows free timers, gamification, ambient sounds, group sessions and cross-platform options already exist.

**Pain points to validate:** users may want less friction, a distinct aesthetic, accountability, context-specific focus modes, or a meaningful community. A timer alone is easy to copy.

**Easy wins:** reliable foreground/background timer, session presets, pause rules, notification controls, widgets, soundscapes, and session history. Avoid social features until there is evidence of demand.

**Monetization:** one-time sound/theme packs or €1.99–€3.99/month for analytics and sync. Subscription value is weak without a continuing service.

**Verdict:** technically easy but commercially difficult. A strong brand/community or creator distribution would be required.

---

## 3. Product discovery plan (before committing)

### 3.1 Validate the problem

For the recommended calculator wedge:
1. Pick one trade/hobby and one primary user type (e.g., hobbyist cabinet maker, small joinery shop, DIY renovator).
2. Interview 12–15 people who have completed a real project in the last 60 days.
3. Ask them to demonstrate their current workflow. Do not ask only whether they “like the idea”.
4. Collect 5 real anonymised examples of calculations, input units, material assumptions, and output formats.
5. Observe competitor workflows on actual devices. Record time-to-result, number of taps, errors, ads, paywalls, and export quality.
6. Test a clickable prototype with 5 users before building the calculation engine.

### 3.2 Interview prompts

- Tell me about the last time you calculated quantities/material for a project.
- What did you use (paper, spreadsheet, website, app, supplier)?
- What did you have to calculate manually?
- What happens when the estimate is wrong?
- Which inputs do you reuse between jobs?
- Do you work offline/on a jobsite/workshop?
- Do you need to share, print, or export the result? With whom?
- What would make you trust a new calculator?
- Have you paid for a calculator/tool app? What did you pay for?

### 3.3 Go / no-go gates

Proceed only if:
- At least 8/12 interviewees describe the same frequent or costly friction.
- At least 5 can provide realistic sample jobs/data for validation.
- At least 5 agree to test a prototype, and 3 indicate willingness to pay for the proposed Pro benefit.
- The product can deliver a materially faster or clearer workflow than the top 3 direct competitors.

If these conditions fail, change the niche or stop. Do not use code already written as a reason to continue.

---

## 4. MVP product requirements (recommended calculator wedge)

> Working product definition: an offline-first Android calculator for a single trade/hobby workflow. The exact calculator set must be confirmed by discovery.

### 4.1 Primary user journey

1. Open app directly to the most-used tools (no forced onboarding/account).
2. Choose a calculator or resume a recent project.
3. Enter dimensions using the units and input patterns the trade actually uses.
4. See validation and a live result; understand the formula and assumptions.
5. Adjust waste/kerf/material price if relevant.
6. Save or duplicate the calculation/project.
7. Share a text summary for free; export a clean PDF/CSV as a Pro benefit.

### 4.2 MVP features (P0)

- Android app with responsive phone and tablet layouts.
- One clearly defined niche and 3–5 high-frequency calculators only.
- Metric units first; imperial only if research proves it is needed at launch.
- Correct input validation, sensible defaults, rounding rules, and edge-case handling.
- Formula/assumption disclosure and estimate disclaimer where relevant.
- Offline operation with no account.
- Recent calculations and favourites.
- Save, rename, duplicate, and delete a project locally.
- Share result as plain text using Android Sharesheet.
- Settings: unit preference, decimal separator, theme, privacy/about, feedback.
- Accessibility: scalable text, content descriptions, sufficient contrast, keyboard navigation, touch targets.
- Crash reporting with privacy-conscious configuration and opt-out where appropriate.

### 4.3 Next release (P1)

- PDF export with a polished, printable report.
- CSV export/import where meaningful.
- Custom presets/material rates.
- Project templates and reusable defaults.
- One-time Pro purchase and restore purchases.
- Home-screen widget / quick actions if the core use case benefits.
- Localization based on actual target-market demand.

### 4.4 Explicitly out of scope for MVP

- Login, cloud sync, social/community, user-generated formula marketplace.
- AI assistant or camera-based measurement.
- Multi-user collaboration.
- Web dashboard and iOS release.
- Subscription unless repeat-value evidence supports it.
- Engineering-grade structural/safety calculations without qualified review and validation.

### 4.5 UX principles

- **Task-first:** lead with actions, not a dashboard full of charts.
- **One screen, one job:** inputs and results should be visible together where practical.
- **No surprise math:** show units, formula, assumptions, rounding and waste separately.
- **Forgiving input:** preserve values on validation errors; support locale decimal conventions.
- **Field-ready:** high contrast, large touch targets, readable in bright environments, usable one-handed.
- **Trust:** show when data is stored locally; never imply estimates are guaranteed quantities.
- **Monetization without hostility:** do not interrupt a calculation with a paywall or full-screen ad.

---

## 5. Recommended Android technology stack

### 5.1 Primary recommendation

| Layer | Recommendation | Reason |
|---|---|---|
| Language | Kotlin | Native Android ecosystem and strong type safety |
| UI | Jetpack Compose + Material 3 | Modern declarative UI, accessible components, fast iteration |
| Architecture | Feature-based Clean Architecture / MVVM | Separates UI, domain calculations and persistence without excessive ceremony |
| Async | Kotlin Coroutines + Flow | Native asynchronous and reactive state handling |
| Dependency injection | Hilt or Koin (choose one) | Testable dependency graph; avoid adding both |
| Local DB | Room (SQLite) | Reliable structured offline storage and migrations |
| Preferences | DataStore | Unit/theme/locale preferences; not for project records |
| Background work | WorkManager only when needed | Reliable reminders/export tasks; do not run unnecessary jobs |
| Navigation | Navigation Compose | Typed/structured app navigation |
| Billing | Google Play Billing Library | Digital Pro unlocks sold in Android app |
| Crash reporting | Firebase Crashlytics or Sentry | Crash visibility; configure privacy and consent appropriately |
| Analytics | Firebase Analytics or PostHog (optional) | Minimal funnel/product events; avoid collecting calculation contents |
| CI | GitHub Actions | Build, unit tests, lint, dependency checks |
| Distribution | Google Play internal → closed → production tracks | Controlled release and tester feedback |

**Cross-platform alternative:** Flutter is a valid choice if iOS delivery is an explicit near-term requirement and the team has Flutter expertise. React Native is also viable. Do not select a framework based on fashion; select based on team capability, required native integrations, testability, and future platform plans. For Android-first with no committed iOS date, Kotlin/Compose is the default recommendation.

### 5.2 Architecture pattern

Use a modular monolith inside the mobile app:

- `presentation`: Compose screens, navigation, view models, UI state.
- `domain`: pure Kotlin use cases, entities, validation and calculation engines.
- `data`: Room entities/DAOs, repositories, preferences, import/export.
- `core`: design system, error types, logging, formatters, unit conversion primitives.
- `billing`: Play Billing integration and entitlement state.

Keep the calculation engine pure Kotlin with no Android dependencies. It should be deterministic and testable on the JVM.

---

## 6. Database and data model

### 6.1 MVP: local database only

Do not deploy PostgreSQL just to store a user's local calculations. Room/SQLite is sufficient for an offline calculator. This reduces cloud cost, account friction, privacy exposure, and operational work.

Suggested entities (adjust to the chosen niche):

**`Project`**
- `id` UUID (primary key)
- `name` string
- `projectType` string/enum
- `createdAt`, `updatedAt` epoch millis
- `isArchived` boolean
- `notes` nullable string

**`Calculation`**
- `id` UUID (primary key)
- `projectId` nullable FK → Project
- `calculatorType` string/enum
- `inputJson` versioned JSON or normalized typed columns for frequently queried fields
- `resultJson` versioned JSON
- `formulaVersion` string
- `assumptionsJson` string
- `createdAt`, `updatedAt` epoch millis
- `isFavourite` boolean

**`MaterialPreset`** (only if the selected niche needs material definitions)
- `id` UUID
- `name`, `category`, `unit`
- `defaultPrice` nullable decimal stored as integer minor units or precise decimal representation
- `currencyCode` nullable ISO 4217
- `metadataJson` versioned JSON
- `isBuiltIn` boolean

**`AppPreference`**
- Use DataStore for global preferences rather than a Room table unless queryable preference history is needed.

### 6.2 Data rules

- Store canonical values in a base unit (e.g., millimetres, square millimetres, cubic millimetres) and convert only at input/output boundaries.
- Never use binary floating point for money. Store cents/minor units as integer or use a decimal library.
- Define explicit rounding rules per output; avoid rounding intermediate calculations.
- Store formula version and assumptions so an old saved calculation remains explainable after formula changes.
- Version exported/imported JSON; validate all imported data and set size limits.
- Use foreign keys, indexes on `projectId`, `updatedAt`, and favourite/recent queries.
- Add Room migrations and migration tests from the first schema version.
- Do not store user calculation data in analytics events.

### 6.3 When to add cloud database

Add cloud sync only when users ask for it or usage data demonstrates multi-device/backup demand. Then use:
- Supabase PostgreSQL (EU region where available and appropriate) for user, project and sync metadata.
- Supabase Auth only if account identity is required.
- Row Level Security (RLS) on every user-owned table.
- Explicit sync conflict policy (last-write-wins is simple but may lose edits; use revision/version fields and conflict UI if data is valuable).
- Export and account deletion workflows.
- A documented data retention and backup policy.

Do not put an API key with elevated database privileges in the mobile app. Mobile clients may contain only public/anon keys protected by RLS; privileged operations must run server-side.

---

## 7. Cloud hosting and backend decision

### 7.1 MVP infrastructure

For the recommended calculator: **no custom backend required**.

- App distribution: Google Play.
- Billing: Google Play Billing.
- Crash reports: Crashlytics/Sentry.
- Optional product analytics: minimal, privacy-aware event collection.
- Support: support email/form and a public privacy policy.
- PDF generation: on-device.

Expected infrastructure spend can be close to zero at small scale, excluding developer accounts, analytics/crash plan limits, and any paid services. Verify current provider pricing before launch; do not treat estimates as quotes.

### 7.2 Future backend (only if justified)

| Requirement | Candidate service | Notes |
|---|---|---|
| Auth + PostgreSQL + sync API | Supabase | Start with managed Postgres; enable RLS and backups |
| Object/file storage | Supabase Storage or Cloudflare R2 | Needed for photos/attachments, not calculator MVP |
| Server functions | Supabase Edge Functions or Cloud Run | Use for trusted server-side operations only |
| Scheduled jobs | Supabase scheduled functions / Cloud Scheduler | Only for reminders or recurring workflows |
| Email | Resend / Postmark | Transactional messages; verify GDPR/DPA and deliverability |
| SMS | Twilio or regional provider | Expensive variable cost; booking use case only |
| Monitoring | Sentry + provider logs | Avoid logging personal or sensitive payloads |

### 7.3 Environments

- `local`: developer machine/emulator; test data only.
- `dev`: integration environment; non-production credentials.
- `staging`: release-candidate environment; production-like settings and synthetic data.
- `production`: live app/services, restricted access, backups and monitoring.

For a backend-free MVP, environments still apply to app build variants and billing products. Keep separate package IDs and signing configurations for debug/staging/production where practical.

---

## 8. API and integration contracts

### MVP

No public REST API is necessary. Use Android system capabilities:
- Android Sharesheet for sharing text/files.
- Storage Access Framework for user-selected import/export destinations.
- Play Billing for purchases and entitlement restore.
- Notification APIs only if reminders are a proven use case.

### If a backend is added later

Use versioned REST endpoints or Supabase client APIs behind RLS. Define OpenAPI before implementation. Example resource shape:

- `GET /v1/projects`
- `POST /v1/projects`
- `PATCH /v1/projects/{projectId}`
- `DELETE /v1/projects/{projectId}`
- `GET /v1/projects/{projectId}/calculations`
- `POST /v1/sync` (only if sync design requires a batch endpoint)

Every endpoint must define auth, validation, pagination, idempotency where relevant, error schema, rate limits, and ownership checks. Never trust a client-supplied `userId` as authorization.

---

## 9. Monetization and unit economics

### 9.1 Recommended tests

**Primary:** one-time Pro unlock. This aligns with offline utility behavior and avoids asking occasional users to pay forever.

**Secondary:** optional ad-supported free tier only after organic usage is established. Ads can harm trust and task completion; test them carefully.

### 9.2 Example product tiers

| Tier | Features | Price hypothesis |
|---|---|---:|
| Free | Core calculators, offline, recent history, favourites, text share | €0 |
| Pro lifetime | PDF/CSV, unlimited projects/templates, custom presets, no ads | Test €4.99 / €7.99 / €9.99 |
| Optional later subscription | Cloud sync, multi-device backup, shared projects, ongoing content/service | Test only if recurring costs/value exist |

### 9.3 Metrics and formulas

- Store conversion = installs / store listing visitors.
- Activation = users completing a first useful calculation / first opens.
- Repeat use = users returning in 7 and 30 days.
- Pro conversion = paid users / active users (report by cohort).
- Refund rate = refunded purchases / purchases.
- Revenue per active user = net revenue / monthly active users.
- Ad ARPDAU = ad revenue / daily active users.

Do not forecast revenue from downloads alone. Measure acquisition source, activation, retention, paid conversion, refunds, and support burden.

---

## 10. Security, privacy, legal and compliance

- Publish a clear privacy policy before public release; accurately disclose analytics, crash reporting, ads, and third-party SDKs.
- Follow Google Play Data safety declarations and the applicable Google Play policies.
- Apply GDPR principles for EU users: data minimisation, purpose limitation, retention limits, access/deletion processes, processor agreements, and lawful basis where personal data is processed.
- For the offline calculator, avoid collecting account or project data unless essential.
- Use Android Keystore for secrets if any local secret storage is needed; do not hardcode credentials.
- Use HTTPS for all network traffic; validate certificates through platform defaults and do not disable TLS checks.
- Keep dependencies patched; run dependency vulnerability checks in CI.
- Never log personal data, photos, exact project contents, or purchase tokens.
- Verify purchase tokens server-side if a backend entitlement service is introduced; for a simple one-time Play Billing entitlement, follow Google's current Billing guidance.
- Provide a support contact, terms where needed, and a clear disclaimer for estimates.
- Do not claim a calculator is suitable for structural, electrical, medical, or other safety-critical decisions without qualified validation and regulatory review.

---

## 11. Testing and quality strategy

### 11.1 Calculation correctness (highest risk)

- Unit tests for every formula and unit conversion.
- Golden test vectors reviewed by a domain expert or independently verified against trusted examples.
- Boundary tests: zero, negative, very large, tiny dimensions, missing values, invalid units, rounding edges.
- Property-based tests where useful (e.g., converting A→B→A should return within defined tolerance).
- Regression tests for every reported incorrect result.
- Tests for locale decimal comma/point and fractional input if supported.
- Formula version tests for previously saved calculations.

### 11.2 Android app tests

- ViewModel/use-case unit tests.
- Room DAO and migration tests.
- Compose UI tests for primary workflows, keyboard behavior, accessibility semantics and error states.
- Integration tests for export/share and Play Billing test environment.
- Device matrix: low-end Android, current Android, small phone, large phone, tablet, dark/light mode, font scaling.
- Offline/airplane-mode tests; app must not block core functionality.
- Performance: cold start, screen transition, memory usage, large project history.

### 11.3 Release quality gates

- No P0/P1 defects.
- All critical formula tests pass.
- No crash in the primary end-to-end flows.
- App works offline.
- No secrets or production personal data in logs.
- Store screenshots, listing text, privacy policy, Data safety form and support route ready.

---

## 12. Repository and project structure

Suggested single-repository structure:

```text
app/
  src/main/
    AndroidManifest.xml
    java/com/example/nichecalc/
      MainActivity.kt
      App.kt
      navigation/
      core/
        common/
        designsystem/
        formatting/
        units/
        database/
        billing/
        export/
      feature/
        home/
        calculator/
          presentation/
          domain/
          data/
        projects/
        history/
        settings/
      di/
    res/
  src/test/                 # JVM unit tests
  src/androidTest/          # instrumented/UI tests
build-logic/                # optional convention plugins
config/                     # lint, detekt, dependency rules
docs/
  product-requirements.md
  formula-specifications.md
  architecture-decisions/
  privacy-data-map.md
  release-runbook.md
.github/workflows/
  android-ci.yml
  android-release.yml
```

Package naming and module boundaries should be finalised after the niche and calculator types are selected. Avoid splitting every folder into a Gradle module until build times or team ownership justify it.

### Code conventions

- Compose UI does not contain calculation logic.
- ViewModels expose immutable UI state and receive user actions.
- Domain use cases have no Android framework dependencies.
- Repositories own persistence details.
- Use sealed result/error types; do not silently coerce invalid input to zero.
- Keep calculation functions deterministic and side-effect free.
- Add KDoc/comments for non-obvious formulas and link to the reviewed formula source.

---

## 13. Design system and screen inventory

### Screens

1. Home / tool selector.
2. Calculator input + live result.
3. Result details / assumptions.
4. Project save / rename / duplicate.
5. Recent calculations.
6. Favourites.
7. PDF/CSV export preview (P1).
8. Pro paywall / purchase restore.
9. Settings / units / theme / privacy.
10. About / support / disclaimer.

### Components

- Numeric input with unit suffix and validation.
- Unit selector.
- Increment/decrement controls where appropriate.
- Result card with primary result and breakdown.
- Waste/kerf/material-price control.
- Formula explanation / assumptions disclosure.
- Save/favourite/share action row.
- Empty, loading, error, and offline states.
- Accessible confirmation dialogs and snackbars.

### UX acceptance criteria

- A returning user can reach a core calculator in no more than two taps from home.
- A first-time user can complete the sample calculation without creating an account.
- Invalid input is explained next to the field and does not erase other values.
- Every displayed result shows its unit.
- Users can see the assumptions used in the result.
- No paywall blocks the first successful calculation.

---

## 14. Analytics and product instrumentation

Track only events needed to answer product questions. Example events:

- `app_first_open`
- `calculator_opened` (calculator type only)
- `calculation_completed` (calculator type, not raw inputs/results)
- `calculation_saved`
- `project_duplicated`
- `result_shared`
- `export_started` / `export_completed`
- `pro_paywall_viewed`
- `purchase_started` / `purchase_completed` / `purchase_restored`
- `feedback_submitted`

Do not log raw dimensions, material prices, project names, photos, or notes. Define event names and properties in a tracking plan and review privacy implications before implementation.

---

## 15. CI/CD and Google Play release plan

### Pull request pipeline

1. Checkout and configure JDK/Gradle.
2. Run formatting/lint (e.g., ktlint + detekt if adopted).
3. Run JVM unit tests.
4. Run Room migration tests.
5. Build debug APK/AAB.
6. Run dependency and secret scans.
7. Upload test/build artifacts.

### Release pipeline

- Version code/name managed centrally.
- Signed release bundle built only in protected CI context.
- Signing keys and Play service credentials stored as protected secrets.
- Internal testing track first; closed testing with real devices; production staged rollout.
- Crash-free sessions, reviews, support requests and purchase behavior monitored after rollout.
- Rollback/release halt criteria documented.

Never commit signing keys, service-account JSON, API secrets, or production credentials.

---

## 16. Delivery roadmap and ownership

Indicative schedule for a tightly scoped calculator MVP; actual timeline depends on niche complexity and team availability.

| Phase | Work | Deliverable | Exit criteria |
|---|---|---|---|
| 0. Discovery (1–2 weeks) | Interviews, competitor teardown, formula validation, prototype | PRD + validated workflow | Problem and niche pass go/no-go gates |
| 1. Foundation (3–5 days) | Repo, Compose design system, navigation, CI, Room setup | Running app skeleton | CI green; architecture decisions recorded |
| 2. Core engine (1–2 weeks) | Formula specs, units, validation, golden tests | Pure Kotlin calculation module | Domain tests reviewed and pass |
| 3. MVP UX (1–2 weeks) | Home, calculator, results, history, projects, settings | Feature-complete alpha | Primary flows work offline |
| 4. Polish & release (1 week) | Accessibility, device tests, privacy, store assets, billing if ready | Release candidate | Quality gates pass |
| 5. Beta validation (2–3 weeks) | Closed test, interviews, funnel and crash review | Launch decision | Activation/retention and feedback justify launch |

Suggested roles (one person may cover multiple roles):
- Product owner: scope, user interviews, pricing, release decision.
- UX/UI: prototype, interaction design, accessibility and store visuals.
- Android engineer: app architecture, Compose, Room, billing and release.
- Domain reviewer: validates formulas and assumptions.
- QA: test design, device matrix, regression and release sign-off.

---

## 17. Launch and distribution

- App name should state the niche and primary job; avoid generic “All-in-One Calculator”.
- Google Play listing: exact target user, example tasks, offline promise (only if true), screenshots showing inputs and outputs, clear Pro details.
- ASO: research search terms used by the target trade; include relevant calculator names naturally in title/short description/long description without keyword stuffing.
- Create a lightweight landing page with use cases, screenshots, FAQ, privacy policy and support contact.
- Seed beta users through trade communities, workshops, relevant forums, creators and local groups; do not spam communities.
- Ask for a review after a successful repeated use, not on first launch.
- Build a feedback channel and publish a visible changelog.

---

## 18. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Formula or unit error | High; trust and possible safety implications | Expert review, golden tests, disclaimers, formula versioning |
| Generic feature parity | High; poor installs/conversion | Narrow niche, workflow research, differentiate on speed and local conventions |
| Users do not return | Medium/high | Choose recurring workflow; measure repeat use before adding subscriptions |
| Overbuilding backend | Medium; wasted time/cost | Local-first; add services only for proven requirements |
| Bad ad experience | High for trust | Delay ads; no interstitials during task; test ad-free Pro |
| Billing edge cases | Medium | Play Billing test cases, restore flow, entitlement state tests |
| Data loss on device change | Medium | Explain local-only behavior; export/backup feature; cloud sync only if validated |
| GDPR/store policy gaps | High | Data map, minimal collection, accurate disclosures, deletion/support process |
| AI output errors (if later added) | High | Do not use AI for deterministic arithmetic; constrain AI to optional text assistance and validate output |

---

## 19. Decision log: why the diagram changes for this MVP

| Diagram component | MVP decision | Rationale |
|---|---|---|
| React Native / Flutter / Next.js | Kotlin + Jetpack Compose | Android-first; native UX and billing; pure domain engine remains portable in concept |
| Camera input | Excluded | Not required for initial calculator workflow |
| Charts | Excluded initially | Add only if users need cost/usage trends |
| State management | ViewModel + StateFlow | Native and sufficient |
| API gateway | Removed | No custom backend/API in MVP |
| Clerk/Supabase Auth | Removed | No account in MVP |
| PostgreSQL/Supabase | Replaced by Room/SQLite | Local data, offline, no cloud dependency |
| S3/R2 storage | Removed | No remote files in MVP; local PDF export |
| Cron jobs | Removed | No scheduled server tasks |
| LLM/Vision APIs | Removed | Formula calculations must be deterministic; AI adds cost and risk without proven value |
| Stripe/RevenueCat | Replaced by Google Play Billing | Android digital purchases should use Play Billing subject to current Play policy |

---

## 20. Open decisions required before implementation

These are genuine product decisions; do not silently guess them:

1. Which of the five candidate categories are we committing to for the first release?
2. If calculator: which exact trade/hobby and first 3–5 calculations?
3. Who is the primary user: hobbyist, DIY homeowner, or working professional?
4. Initial geography/language: Netherlands/EU, India, global English, or another segment?
5. Must iOS follow within 6–12 months, or is Android the only planned platform?
6. Is the business goal a small side-income utility or a product intended to scale into a larger SaaS?
7. Is a one-time purchase preferred, or is there a defensible recurring service to support subscription pricing?

### Recommended default decisions if speed is the priority

- One niche calculator, not a bundle of unrelated utilities.
- Metric-first, English-first, Android-first.
- Kotlin + Jetpack Compose + Room; offline and no account.
- Free core + one-time Pro unlock; no ads in initial beta.
- No custom backend, no AI, no cloud storage in v1.
- Validate the exact niche and formulas before implementation.

---

## 21. Reference links and evidence notes

Public product pages and comparisons consulted for the November 2026 landscape scan. Vendor/app-store listings are primary evidence for their own features; third-party comparison articles are directional and may contain affiliate or vendor bias. Pricing and availability must be rechecked in the target country before a commercial decision.

### Calculator / trade tools
- [Concrete Calculator – Google Play](https://play.google.com/store/apps/details?id=com.pixelsdo.concretecalculator)
- [Construction Calculator – Google Play](https://play.google.com/store/apps/details?id=com.patchmedia.constructioncalculator)
- [Woodworking Cut List – Google Play](https://play.google.com/store/apps/details?id=com.patchmedia.woodworkingcutlist)
- [WoodCalc – Google Play](https://play.google.com/store/apps/details?id=com.minossoft.woodcalc)
- [Kerfo – Google Play](https://play.google.com/store/apps/details?id=com.cutlist.optimizer)
- [Lumber Logic – Google Play](https://play.google.com/store/apps/details?id=com.lumberlogic.app)
- [Scaled Recipe – Google Play](https://play.google.com/store/apps/details?id=org.andronotes.recipe)

### Habit / focus
- [Loop Habit Tracker](https://loophabits.org/)
- [2026 habit tracker comparison](https://www.buildmyhabit.com/blog/best-habit-tracker-apps/)
- [2026 Pomodoro comparison](https://pomifocus.com/blog/best-pomodoro-apps-2026.html)

### Booking
- [2026 small-business booking comparison](https://fitsmallbusiness.com/airtable-crm/)
- [2026 appointment software comparison](https://serpastra.com/blog/best-appointment-scheduling-software)

### Home inventory
- [HoardIQ – Google Play](https://play.google.com/store/apps/details?id=com.hoardiq.app)
- [Ownitory – Google Play](https://play.google.com/store/apps/details?id=com.ownwise.app)
- [InventoryMe – Google Play](https://play.google.com/store/apps/details?id=fr.inventoryme.app)
- [Itemtopia – Google Play](https://play.google.com/store/apps/details?id=com.itemtopia.app)
- [2026 Android home inventory comparison](https://unstoreit.com/discover/best-home-inventory-tracker-apps-android/)

### Platform documentation to use during implementation
- [Android app architecture](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/compose)
- [Room persistence library](https://developer.android.com/training/data-storage/room)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Google Play Billing](https://developer.android.com/google/play/billing)
- [Google Play policy centre](https://support.google.com/googleplay/android-developer)
- [Supabase documentation](https://supabase.com/docs)
- [GDPR – European Commission](https://commission.europa.eu/law/law-topic/data-protection_en)

---

## Final recommendation

The highest-probability route is to validate one niche calculator, ship it as a polished offline Android utility, and only add cloud services if user behavior proves they are needed. The architecture should be deliberately boring: native UI, deterministic calculation engine, local database, tested export, Play Billing, CI, crash reporting, and clear privacy disclosures.

The first milestone is **not coding**. It is choosing the niche and collecting real workflows and example calculations. That is what determines whether this becomes a useful paid tool or just another calculator in an already crowded Play Store.

## 13. App-store competition audit — expanded evidence (17 November 2026)

This section adds a first-pass audit of live Google Play listings and available App Store evidence. It is a screening exercise, not a complete store scrape or a statistically representative market-size study. Google Play install bands are global lifetime bands unless explicitly stated otherwise; third-party install estimates are estimates. Apple rating counts are storefront/country-specific and must not be compared directly with Google Play ratings or install bands.

### 13.1 Google Play: concrete competitor signals

| Candidate niche | Example app | Public store / market signal | What it tells us |
|---|---|---|---|
| Construction / concrete | [Concrete Calculator – Pixelsdo](https://play.google.com/store/apps/details?id=com.pixelsdo.concretecalculator) | 1M+ installs, 4.8 stars, about 10.9K reviews; offline, metric/imperial, concrete, blocks, plastering and rebar | Broad construction calculators are already validated and competitive. A generic concrete calculator is not an attractive wedge. |
| Construction / site tools | [Concrete Calculator A1 PRO](https://play.google.com/store/apps/details?id=com.binaryandbricks.concrete_calculator) | 50K+ installs, 4.5 stars; 85 concrete shapes, multiple regional design codes, project totals and PDF site documents | Professional workflows and documentation can justify a specialist tool, but the feature bar is higher than simple volume arithmetic. |
| Woodworking / lumber | [WoodCalc: Lumber Calculator](https://play.google.com/store/apps/details?id=com.mishanoyr.apps.woodcalc) | 50K+ installs; lumber, logs, firewood, decking, sheet materials, kerf-aware cut lists, project saving and CSV/PDF export; updated June 2026 | A broad lumber calculator already covers much of the proposed feature set. Competing only on “more calculators” is weak. |
| Woodworking / cut planning | [Timberlog](https://play.google.com/store/apps/details?id=timber.volume.calculator.timbervolumecalculator) | 500K+ installs, 4.2 stars, 2.1K reviews; offline timber/log calculations and camera-assisted log pile measurement | There is real usage in timber workflows; camera-assisted measurement is an established feature, not a unique differentiator by itself. |
| Woodworking / cut planning | [Cutter – Cut Optimizer](https://play.google.com/store/apps/details?id=com.embarcadero.OptimizaCorte) | 100K+ installs, 3.3 stars, 2.54K reviews; 1D cut optimisation for lumber, bars, tubes and profiles, kerf handling | A relatively low rating alongside meaningful installs may indicate usability or quality gaps worth investigating through review mining. It does not prove what those gaps are. |
| Habit tracking | [Habit tracker market snapshot](https://appimp.app/markets/habit-tracker-app) | One market dataset matched 10 apps with 52.7M combined installs; leaders include Fabulous and Finch; 14 of 16 apps listing in-app pricing charge | Very high demand, strong incumbents and established paid behaviour. A generic habit tracker is a poor first bet. |
| Routine tracking | [Routine tracker market snapshot](https://appimp.app/markets/routine-tracker-and-habit-guide-apps) | One dataset matched 10 apps with 30.6M combined installs; Me+ accounts for a large share | Another crowded adjacent segment. Avoid entering with a generic daily routine tracker. |
| Focus / Pomodoro | [Focus timer market snapshot](https://www.appbrain.com/topics/focus-timer-apps) | 30 apps reportedly received 2.4M installs in the preceding 30 days; Regain reported 940K, YPT 200K, and Focus Friend 55K in that period | Large active demand, but distribution is concentrated in products with strong brands, social mechanics or distinctive presentation. A plain timer is commoditised. |
| Focus / Pomodoro | [Focus To-Do](https://play.google.com/store/apps/details?id=com.superelement.pomodoro) | 10M+ installs and 285K reviews; combines timer, tasks, reminders and sync | Users can already get a broad productivity bundle for free/freemium. A timer-only app needs a sharp audience or aesthetic/community hook. |
| Home inventory | [Magic Home Inventory](https://play.google.com/store/apps/details?id=net.twisterrob.inventory) | 50K+ installs, 4.1 stars, 551 reviews; hierarchical property/room/item structure and detailed categorisation | Android has established specialist apps, but visible scale is smaller than habit/focus categories. There may be room for a simpler onboarding and capture workflow. |
| Home inventory | [Itemtopia](https://play.google.com/store/apps/details?id=com.itemtopia.app) | Third-party tracking reports roughly 50K–73K installs and around 3.4–3.5 stars in Sep–Nov 2026; free tier limited by item count | The low rating is a review-mining lead, not proof of product failure. Inspect recent negative reviews to identify specific pain points before drawing conclusions. |
| Appointment booking | [Setmore](https://play.google.com/store/apps/details?id=com.adaptavant.setmore) | 500K+ installs, 4.5 stars, 6.38K reviews; booking page, reminders, payments and service-business support | The basic booking companion is an established category with mature feature expectations and integrations. A generic competitor is unlikely to win. |

### 13.2 Apple App Store: evidence available in this pass

- [Setmore Appointment Scheduling – US App Store](https://apps.apple.com/us/app/setmore-appointment-scheduling/id665859578) shows 4.7 stars from 9.2K ratings and describes self-booking, payments, team scheduling and reminders. This reinforces the maturity of the booking category on iOS.
- [2026 home inventory comparison](https://www.getitemlist.app/best-home-inventory-apps/) lists iOS products such as Itemlist, Under My Roof and Nest Egg, alongside Android-capable products such as Everspruce and Itemtopia. It reports meaningful but relatively niche rating counts for several dedicated home inventory apps. Treat this as a competitor-discovery source, not independent proof of demand; the publisher also sells a competing product.
- This pass did not retrieve reliable, directly comparable Apple storefront data for all five categories. Do not infer iOS absence from missing search results. A country-specific App Store audit remains a required validation task.

### 13.3 First-pass crowding and opportunity judgement

| Candidate | Crowding judgement | Opportunity judgement | Current decision |
|---|---|---|---|
| Generic habit tracker | Very high | Low unless focused on a sharply underserved job/audience | Do not build generic version |
| Generic Pomodoro timer | Very high | Low; established free products and strong differentiated brands | Do not build generic version |
| Generic appointment scheduler | Very high | Low; incumbents offer booking pages, reminders, payments and team workflows | Reject as first solo micro-app unless a narrow vertical channel is secured |
| Home inventory | Medium / fragmented | Potentially attractive if capture is dramatically faster and insurance/moving output is trustworthy | Keep on shortlist; investigate onboarding and review complaints |
| Generic construction calculator | High | Low; broad calculators already have million-install products | Avoid generic version |
| Woodworking / lumber calculator | Medium–high | Possible only with a specific underserved workflow, regional convention or much better UX | Keep as hypothesis, not decision |
| Highly specific trade calculator | Unknown until the trade is named and audited | Potentially strong where search intent is clear and current tools are clumsy/outdated | Explore additional micro-niches before choosing |

### 13.4 Review-mining plan: find the gap, not just the competitors

For the top 5–8 competitors in each shortlisted niche, collect 1-star, 2-star and recent 3-star reviews from the last 12 months where available. Code each complaint into a consistent taxonomy:

1. **Core task failure:** incorrect calculation, lost booking, missing item, timer/notification failure.
2. **Friction:** too many taps, repetitive data entry, confusing setup, poor defaults.
3. **Trust:** privacy concerns, unexpected paywall, ads interrupting work, unreliable export or sync.
4. **Platform/quality:** crashes, battery use, accessibility, small text, device compatibility.
5. **Missing workflow:** project templates, local units, multi-property support, printable reports, customer reminders, team access.
6. **Monetisation dissatisfaction:** subscription price, feature gating, trial cancellation, ads.

For each theme, record review date, app version, country/store, verbatim short excerpt (within copyright limits), frequency, severity and whether the complaint is still unresolved in the current version. Do not treat one review as a market-wide pain point.

### 13.5 Store listing and discoverability audit

For each competitor, capture:

- Exact title, subtitle/short description, category and developer name.
- Screenshot sequence and first-screen value proposition.
- Video preview, icon quality and visual consistency.
- Rating average, rating count, install band (Google Play), update date and version cadence.
- Monetisation signals: ads, in-app purchases, subscription tiers, lifetime unlock and trial.
- Localisation, supported countries, language coverage and unit conventions.
- Data safety/privacy disclosures, requested permissions and account requirement.
- Review sentiment and recurring feature requests.
- Whether the app is still actively maintained and whether support links work.

Search the exact task phrase as well as broad category terms. Example: “cut list optimizer mm”, “cabinet material estimator”, “home contents insurance inventory”, “photo receipt serial number inventory”, “dog groomer booking reminders”, not only “calculator” or “booking app”.

### 13.6 Market validation gate before product selection

Do not choose the niche from install counts alone. A candidate passes discovery only when all the following are true:

- At least 10–15 target users can be reached for short interviews or workflow observation.
- At least 5 competitor apps have been tested hands-on, including their free tier and onboarding.
- At least 30–50 relevant recent negative/neutral reviews have been coded where enough reviews exist.
- A repeated high-severity problem is visible in user evidence, not merely imagined by the team.
- The proposed MVP solves that problem in a measurable way (e.g. fewer steps, less entry time, fewer mistakes, better report output).
- A plausible acquisition channel exists that does not rely solely on paid ads.
- At least 5 target users indicate credible willingness to pay or join a paid pilot/pre-order test.

### 13.7 Research limitations and next actions

1. Search result visibility is not a complete catalogue of either store. Store search rankings vary by country, device, language, personalisation and query.
2. Google Play install bands are coarse, usually worldwide, and not equivalent to monthly active users or revenue.
3. Third-party analytics estimates should be treated as directional, not ground truth.
4. Apple ratings are storefront-specific; use the Netherlands storefront as the primary European reference and the US storefront as a secondary market if the intended audience is international.
5. Prices, feature limits, screenshots and policies change. Re-check each candidate directly in the relevant store immediately before making a product decision.
6. Next research sprint: choose 2–3 micro-niches (not just broad categories), inspect 5–8 direct competitors each on both stores, mine recent reviews, compare Netherlands vs US localisation, and run 10–15 target-user interviews.

**Interim conclusion:** Store competition confirms that generic habit, focus, booking and broad construction utilities are not attractive first builds. Home inventory and tightly scoped trade calculators deserve deeper validation. The winner should be the niche where we can prove a specific workflow is painful and can be made meaningfully faster—not simply the category with the fewest search results.

### Sources consulted for this update

- [Google Play – Concrete Calculator (Pixelsdo)](https://play.google.com/store/apps/details?id=com.pixelsdo.concretecalculator)
- [Google Play – Concrete Calculator A1 PRO](https://play.google.com/store/apps/details?id=com.binaryandbricks.concrete_calculator)
- [Google Play – WoodCalc: Lumber Calculator](https://play.google.com/store/apps/details?id=com.mishanoyr.apps.woodcalc)
- [Google Play – Timberlog](https://play.google.com/store/apps/details?id=timber.volume.calculator.timbervolumecalculator)
- [Google Play – Cutter: Cut Optimizer](https://play.google.com/store/apps/details?id=com.embarcadero.OptimizaCorte)
- [Google Play – Magic Home Inventory](https://play.google.com/store/apps/details?id=net.twisterrob.inventory)
- [Google Play – Itemtopia](https://play.google.com/store/apps/details?id=com.itemtopia.app)
- [Google Play – Setmore](https://play.google.com/store/apps/details?id=com.adaptavant.setmore)
- [Apple App Store – Setmore](https://apps.apple.com/us/app/setmore-appointment-scheduling/id665859578)
- [Apple App Store competitor overview – Home Inventory](https://www.getitemlist.app/best-home-inventory-apps/)
- [AppBrain – Focus timer apps](https://www.appbrain.com/topics/focus-timer-apps)
- [App Imp – Habit tracker market](https://appimp.app/markets/habit-tracker-app)
- [App Imp – Routine tracker market](https://appimp.app/markets/routine-tracker-and-habit-guide-apps)
