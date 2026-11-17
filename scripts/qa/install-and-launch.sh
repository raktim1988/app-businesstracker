#!/usr/bin/env bash
# Build, install and launch the debug app on the connected emulator/device.
set -euo pipefail
cd "$(dirname "$0")/../.."
./gradlew :androidApp:assembleDebug --console=plain -q
adb get-state >/dev/null
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am force-stop app.invoicer
adb logcat -c
adb shell am start -n app.invoicer/app.invoicer.android.MainActivity
sleep 3
