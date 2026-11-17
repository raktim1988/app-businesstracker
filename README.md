# Invoicer (app-businesstracker)

Offline-first invoice & hours app for Dutch freelancers (ZZP). Android first, iOS-ready via Kotlin Multiplatform.
See `docs/APP_BUILD_BLUEPRINT.md` for the product blueprint.

## Structure

```
androidApp/        Compose UI, ViewModels, navigation (Android only)
shared/domain/     KMP, pure Kotlin: models, Money, VAT calc, repository interfaces, use cases
shared/data/       KMP: repository implementations (in-memory for v0.1) + AppContainer
iosApp/            Placeholder for the future SwiftUI/Compose-MP host app
docs/              Blueprint and notes
```

Dependency direction: `androidApp -> shared:data -> shared:domain` (ui -> data -> domain; domain has no platform deps).

## Run

Open the folder in Android Studio, pick an emulator, run `androidApp`. Or:

```bash
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
./gradlew :shared:domain:jvmTest
```

Requires JDK 17+ and the Android SDK (`local.properties` is git-ignored; Android Studio creates it).

## Status: v0.1

Dashboard, invoice list, invoice editor with live BTW totals, in-memory storage. Clients/Settings are placeholders.
Next: Room/SQLDelight persistence, PDF export, clients, hours.
