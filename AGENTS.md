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

### Demo mode

"Enter demo mode" on the onboarding screen puts the app in a fully offline, logged-in state backed
entirely by hand-written domain models — no HTTP, no websockets, no SQLDelight. This lets app
reviewers, instrumented tests, and prospective users see the app without a real Twitch login.

All demo code lives under `fr.outadoc.justchatting.feature.demo` in `shared/commonMain`:

- `data/DemoData.kt` — **single source of truth** for every fixture (users, streams, schedule,
  badges, emotes, chat script). Add new fixtures here as named `val`s/`fun`s rather than inlining
  data elsewhere.
- `data/Demo*Repository.kt`, `data/Demo*Api.kt` — fake implementations of each repository/API
  interface (`TwitchRepository`, `ChatRepository`, `AuthRepository`, `PronounsApi`,
  `LocalPronounsApi`, `RecentEmotesApi`), returning `DemoData` fixtures directly.
- `data/DemoAware*.kt` — routers bound to each interface in Koin. Each holds the real
  implementation as `Lazy<T>` so it's never constructed (and never touches disk/network) while
  demo mode is active, and reads `DemoModeRepository.isDemoMode` at call time to pick a side.
- `domain/DemoModeRepository.kt` / `data/InMemoryDemoModeRepository.kt` — the in-memory on/off
  switch (`StateFlow<Boolean>`); flipping it off routes `AuthRepository.currentUser` back to
  `NotLoggedIn`, which is what the existing Settings → Log out button triggers in demo mode.
- `data/DemoChatBus.kt` — lets a message sent while in demo mode (via
  `DemoTwitchRepository.sendChatMessage`) get echoed back into `DemoChatRepository`'s chat event
  flow, so typing in demo mode doesn't look broken.

Gotchas when editing `DemoData.kt`:

- The chat data (`chatOpeningBurst`, `chatScript`) is themed around Nomai characters from *Outer
  Wilds*. Usernames and message text must be verbatim quotes sourced from
  https://outadoc.github.io/nomai-scrolls/en/index.html — never invent new dialogue, even to avoid
  a spoilery quote; pick a different real, spoiler-free line instead.
- `chatScript` must be written in terms of `ChatEvent` (domain), not `ChatViewModel.Action`
  (presentation) — Konsist forbids `feature.demo.data` from importing anything under
  `..presentation..`.
- Non-square emotes need an explicit `Emote.ratio = width / height`; it defaults to `1f` (square),
  which squishes wide emotes like `om`/`o`. See `getEmotePlaceholder()` in
  `ChatInlineTextContent.kt`.
- Chat messages default to `badges = emptyList()`. To have a badge actually render on a scripted
  message, pass `badges = listOf(Badge(id = "<setId>", version = "<version>"))` to `chatMessage(...)`
  matching an entry in `DemoData.globalBadges`/`channelBadges` — resolution is by string-key match
  (`"badge_${setId}_$version"`), so a mismatched id/version silently renders nothing.
- Chat viewers (e.g. `"demo-viewer-ramie"`) are never added to `DemoData.allUsers` — there'd be too
  many to maintain by hand. `DemoTwitchRepository.getUserById` falls back to
  `DemoData.syntheticUser(id)` for any unrecognized id, which derives both `login` and
  `displayName` from the id itself (stripping the `demo-viewer-` prefix and capitalizing), so it
  stays consistent with the name already shown inline in the chat message.

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
