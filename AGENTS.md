# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Agent rules

1. Do not EVER push or interact with git remotes in any way.
2. Always work on feature branches, never directly on the `main` branch.

## Commands

```bash
# Build
./gradlew :app-android:assembleDebug       # Android APK
./gradlew :app-android:installDebug        # Install on connected device
./gradlew :app-desktop:run                 # Run desktop app
# iOS: cd app-ios && tuist generate, then build in Xcode

# Test
./gradlew :shared:testDebugUnitTest                          # All shared unit tests
./gradlew :shared:testDebugUnitTest --tests "fr.outadoc.justchatting.feature.chat.presentation.ChatStateReducerTest"  # Single class
./gradlew :konsist-checks:test                               # Architecture layer checks
./gradlew :shared-internal:verifySqlDelightMigration         # Validate DB migrations

# Formatting — always run before committing, CI enforces it
./gradlew ktlintApply    # Auto-format (ktlint)
./gradlew ktlintCheck    # Check only

# ABI — run after any public API change in shared modules
./gradlew updateKotlinAbi  # Regenerate ABI dump files
./gradlew checkKotlinAbi   # Verify no unintended API changes
```

## Architecture

Kotlin Multiplatform app targeting Android, iOS, and JVM desktop.

### Gradle modules

| Module            | Purpose                                                                                                       |
|-------------------|---------------------------------------------------------------------------------------------------------------|
| `shared-internal` | SQLDelight schema + migrations, Compose string/plural resources                                               |
| `shared`          | All business logic, ViewModels, data layer, Ktor clients, common Compose UI                                   |
| `shared-ui`       | Android/iOS Compose UI additions (notifications, shortcuts, `KoinInitializer`)                                |
| `app-android`     | Android app shell                                                                                             |
| `app-desktop`     | JVM desktop app shell                                                                                         |
| `app-ios`         | iOS Compose Multiplatform app (consumes `:shared-ui` as the `JCSharedUI` framework)                           |
| `app-ios-native`  | Alternative iOS SwiftUI app (consumes `:shared` as the `JCShared` framework); Tuist-only, not a Gradle module |
| `konsist-checks`  | JUnit 5 tests that enforce clean architecture layer boundaries                                                |

The two iOS apps (`app-ios`, `app-ios-native`) are not in `settings.gradle.kts`; they are
Xcode/Tuist projects built separately with `tuist generate`.

### Source sets (shared module)

- `commonMain` — vast majority of logic and UI
- `androidMain`, `iosMain`, `desktopMain` — platform-specific implementations
- `skiaMain` — shared between iOS and Desktop (Skia renderer)
- `appleMain` — shared Apple platform code (iOS + macOS)

### Clean architecture layers (enforced by Konsist)

Code lives under `fr.outadoc.justchatting.feature.<name>.<layer>`:

- **domain** — interfaces, use cases, models. No dependencies on other layers.
- **presentation** — ViewModels, UI state. Depends only on domain.
- **data** — repositories, API clients, DB queries. Depends only on domain.

Konsist tests in `:konsist-checks` will fail the build if these boundaries are violated.

### Dependency injection (Koin)

Entry point: `startSharedKoin()` in `shared/src/commonMain/.../di/SharedKoin.kt`.

Two modules compose the DI graph:

- `sharedModule` (`SharedModule.kt`) — all platform-agnostic singletons, `viewModel { }` bindings,
  factories. Uses `single<Interface> { Impl(...) }`, `viewModel { }`, `factory { }`, and `named()`
  qualifiers.
- `platformModule` — `expect val platformModule: Module` with `actual` implementations per platform,
  providing SQLite drivers, HTTP clients, DataStore paths, and platform-specific service
  implementations.

Platform apps wire in via:

- **Android**: `KoinInitializer` (Jetpack Startup), also adds `androidUiModule` from `shared-ui`
- **iOS**: `SharedKoinKt.startSharedKoin { _ in }` called in the Swift app delegate
- **Desktop**: `startSharedKoin()` called in `main()`

### Database

SQLDelight schema lives in `shared-internal/src/commonMain/sqldelight/`. The generated `AppDatabase`
is bound in `sharedModule`; individual `*Queries` are separate singletons. Migration files are in
`shared-internal/migrations/` — always verify with `:shared-internal:verifySqlDelightMigration`
after schema changes.

### ABI files

Shared modules expose ABI dump files in their `abi/` directories. Run `./gradlew updateKotlinAbi`
after any public API change and commit the updated dumps alongside the code change.

## Live testing on desktop

`./gradlew :app-desktop:run` is the fastest way to see a change working or investigate a bug live —
it launches a JVM window on the host without needing an emulator/device, and logs are printed
directly to stdout in the terminal running Gradle.

Desktop has no OS-level deeplink handling, so instead of real `justchatting://` deeplinks,
`KtorLocalCallbackWebServer` (
`shared/src/desktopMain/.../feature/auth/data/KtorLocalCallbackWebServer.kt`) exposes local HTTP
routes on port 45563 that mirror the app's deeplinks and feed them through the same
`DeeplinkParser`/`MainRouterViewModel` path as a real deeplink would. To open a specific channel
while the app is running:

```bash
curl "http://localhost:45563/user/<login>"
```
