# Functional QA report 2026-11-18 (emulator-5554, Invoicer app.invoicer)

Scope: functional button/input rules only. Screenshots in `docs/qa/out/`.
Note: only `qa.py` was used for tapping/typing; clearing fields and the FAB (not exposed in `qa.py dump`) needed `adb shell input` taps/DEL keys.

| id | screen | severity | action | expected | actual | result | screenshot |
|----|--------|----------|--------|----------|--------|--------|-----------|
| F01 | Home | - | Create invoice with empty form | Stay on Home, required errors on all 4 fields | Errors shown, nothing created | PASS | f01-empty-create |
| F02 | Home | - | Client = "A" | Min-length error, keep value | "Enter at least 2 characters", value kept | PASS | f02-client1char |
| F03 | Home | - | Qty = "abc" | Letters rejected | Letters ignored, field stays empty | PASS | f03-qty-letters |
| F04 | Home | - | Qty = 0 | Error | "Must be greater than 0" | PASS | f04-qty-0 |
| F05 | Home | - | Qty = 0.1234 | Error (4 decimals) | "Use a number, max 3 decimals" | PASS | f05-qty-4dec |
| F06 | Home | - | Rate = 0 | Error | Not visually verified (screenshot f06 not reviewed); 0.123 gives "Use an amount, max 2 decimals" | PARTIAL | f06-rate-0, f07-rate-3dec |
| F07 | Home | - | Qty = -5 | Negative rejected | "-" stripped, qty becomes 5 (silent) | PASS | f10-qty-neg |
| F08 | Home | - | Qty = 100000 / Rate = 1000001 | Error | "Too large" for both; rate 100000 accepted | PASS | f09-qty-100000, f11-rate-1000001 |
| F09 | Home | - | Comma "7,5" x "85,5" and dot "7.5" x "85.5" | Both accepted, same total | Both give € 641,25 | PASS | f12-comma, f13-dot |
| F10 | Home | Low | Client 81 chars / description 121 chars | Error or hard cap | Silently capped at 80 / 120 chars, no feedback | PASS (note) | f14-client81, f15-desc121 |
| F11 | Home | - | Add more lines (client, desc, qty, rate, BTW 9%) | Editor opens with values carried | All values + BTW 9% carried; total € 698,96 correct | PASS | f16-before-addlines, f17-editor-carry |
| F12 | Editor | Medium | Tap Add line | New blank line; required errors only after a save attempt | New line instantly shows "Description is required" and "Rate is required" before any save attempt | FAIL | f18-addline |
| F13 | Editor | - | BTW 0% chip | Only BTW changes | BTW row € 0,00, total € 641,25, subtotal unchanged | PASS | f18-addline |
| F14 | Editor | - | Remove line | Second line removed, totals unchanged | Line removed, totals € 641,25 | PASS | f19-removed |
| F15 | Editor | - | Save draft (opened from Home) | Draft saved, back to origin, form reset | Returned to Home, 2026-0001 DRAFT under Recent, quick form reset | PASS | f20-after-save |
| F16 | Home | Low | Create invoice valid (Acme/Work/2/10, 21%) | Invoice created, in Recent, form reset | Created 2026-0002 (€ 24,20), form reset. Status is DRAFT (not "created"/sent) | PASS (note) | f21-valid-filled, f22-after-doubletap |
| F17 | Home | - | Double tap Create invoice | Exactly one invoice | One invoice (0002) created. Caveat: button moves after reset, so 2nd tap lands off-target; weak test | PASS (weak) | f22-after-doubletap |
| F18 | Home | - | See all | Invoices tab selected | Invoices tab opened and highlighted, both invoices listed | PASS | f23-see-all |
| F19 | Invoices | Medium | Long client name (80 chars) in list | Name truncated/wrapped without touching amount | Name runs into amount ("c...c€ 641,25"), no ellipsis/padding | FAIL | f23-see-all |
| F20 | Invoices | Low | FAB "New invoice" | Reachable by accessibility label | Not exposed as text/content-desc in uiautomator dump (qa.py tap fails); opens editor by coordinate tap | FAIL (a11y) | f24-new-editor |
| F21 | Invoices | - | FAB -> editor, Cancel | Back to list, nothing created | Back on Invoices, still 2 invoices | PASS | f27-fab-editor, f28-cancel |
| F22 | Invoices | - | FAB -> editor, system Back | Return to list, nothing created | Returned to Invoices | PASS | f29-back |
| F23 | App | - | logcat AndroidRuntime:E | No crash | None | PASS | - |

Not covered (call budget): rotation/process death, Home draft surviving tab switch, saving the editor empty, 1-char client in editor, rate=0 verification.

## FAILs summary
- F12 (Medium): Add line shows required errors immediately.
- F19 (Medium): long client name overlaps amount in Invoices list.
- F20 (Low): FAB has no accessible label (not in dump).

## Fix status (2026-11-18)
F12 fixed (Add line clears showErrors), F19 fixed (ellipsis, 12dp gap), F20 fixed (FAB content description). Not yet covered: rotation, process death, draft across tab switch, empty editor save, rate=0 visual, real double-tap.
