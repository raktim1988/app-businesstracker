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

## Functional QA: every button and input rule

Build a table of every interactive element per screen (use `qa.py dump` to list clickable nodes) and exercise each one. For each: action, expected destination/effect, actual, PASS/FAIL.

**Buttons (must verify destination and data)**
- Home "More lines…": opens the editor with the quick-form values (client, description, qty, rate, BTW) carried over, not blank.
- Home "Create invoice": valid input creates the invoice, shows it under Recent, resets the form; invalid input stays on Home with errors.
- Home "See all": opens the Invoices tab with the tab selected. Invoices "New"/FAB opens the editor; Cancel returns without creating anything.
- Editor "Add line", remove line, BTW chips, "Save draft": each changes only what it says; totals update live.
- Every button: single tap triggers once (no double navigation or duplicate invoices on double tap), and back from the destination returns to the origin.

**Client-side validation (see `FieldValidation`)**
- Client: empty, 1 char, 81 chars. Description: empty, 121 chars.
- Qty: 0, negative, letters, 4 decimals, 100000. Rate: 0, 3 decimals, 1000001. Both comma and dot.
- Required errors appear only after a save attempt; number errors appear while typing; errors clear when fixed; entered values are kept.
- Rotation and process death keep the draft; nothing is saved when invalid.

**State**
- Typed Home draft survives tab switches. After a successful save no stale draft or `saved` flag remains.

## Rules
- Prefer `qa.py tap "<label>"` over coordinates. Use `adb shell input` only for gestures.
- Always attach a screenshot to each FAIL. Report device model, API level and app version.
- Never change app data on a real device; use the emulator.
