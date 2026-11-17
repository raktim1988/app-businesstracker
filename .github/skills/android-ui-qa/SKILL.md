---
name: android-ui-qa
description: Run UI/UX QA on the Invoicer Android app in the emulator using adb. Use when asked to test, QA, validate UI, check tabs/navigation, padding, insets, look and feel, or regression-test a build. Produces screenshots and a findings report.
---

# Android UI QA (Invoicer)

Tools: `scripts/qa/install-and-launch.sh` (build+install+launch) and `scripts/qa/qa.py` (shot, dump, tap, type, back, insets, edges). Run from the repo root. Needs a running emulator (`adb devices`); boot one with `~/Library/Android/sdk/emulator/emulator -avd Pixel_8`. If more than one device is listed, `export ANDROID_SERIAL=<serial>` (e.g. `emulator-5554`) before running anything.

## Procedure

1. `scripts/qa/install-and-launch.sh`, then `adb logcat -d -s AndroidRuntime:E` must show no crash.
2. For every screen below: `qa.py shot <name>`, view the screenshot, run `qa.py insets` and `qa.py edges`.
3. Execute the checklist. Record each result as PASS/FAIL with the screenshot path.
4. Repeat on a second configuration: font scale 1.3 (`adb shell settings put system font_scale 1.3`), dark mode (`adb shell cmd uimode night yes`), and landscape (`adb shell settings put system accelerometer_rotation 0` then `user_rotation 1`). Reset afterwards (`font_scale 1.0`, `night no`, `user_rotation 0`).
5. Write findings to `docs/qa/report-<YYYY-MM-DD>.md` (table: id, screen, severity, expected, actual, screenshot). Do not fix code unless asked; report only.

## Checklist

**Navigation / tabs**
- Bottom bar shows Home, Invoices, Clients, Settings; each tap selects the right tab and screen.
- Selected tab is visually highlighted; state survives switching tabs; back from a tab root exits the app.
- Bottom bar is hidden in the invoice editor; back/Cancel returns to the previous tab.

**Layout / padding**
- Content is not under the status bar or navigation bar (`qa.py insets` = OK).
- Horizontal margins are at least 16dp and consistent between screens (`qa.py edges`).
- Spacing between cards/fields is consistent (12dp cards, 8dp inside rows); nothing is clipped or overlapping.
- FAB does not hide list content or the bottom bar; text is not truncated at font scale 1.3.

**Look and feel**
- Consistent typography (titles, labels), primary colour #0B5FFF, Material 3 components.
- Touch targets at least 48dp; sufficient contrast in light and dark mode.
- Keyboard does not cover the focused field in the editor; fields scroll into view.

**Functional smoke**
- New invoice: client "Acme", line "Consulting", qty 7,5, rate 85, BTW 21% shows total `€ 771,38`; Save draft returns to the list and shows the invoice.
- Validation: empty client, empty lines, qty 0 show an error and keep the entered values.
- Decimal comma and dot both accepted; rotation keeps the entered data.

## Rules
- Prefer `qa.py tap "<label>"` over coordinates. Use `adb shell input` only for gestures.
- Always attach a screenshot to each FAIL. Report device model, API level and app version.
- Never change app data on a real device; use the emulator.
