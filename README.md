# Just Chatting

An app focused on a great Twitch chat experience.

- Available on **desktop, Android, and iOS** (soon).
- Vibrant Material interface with **dynamic colors** that varies per-stream.
- **Quick emote access**. Your recently-used emotes are always one tap away.
- Multi-chat. Open multiple **chat bubbles**, and switch between them (only on Android.)
- **Tablet- and foldable-optimized** interface.
- **Custom emotes**. If the default emotes aren't enough for you, we support third-party emote sets!
- **Slide to reply** to any message, and see the context of the conversation.
- See your favorite channels' **future schedule**.

<p align="center">
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./docs/assets/screenshots/landing/dynamic-colors-yarrow-dark.png">
  <img src="./docs/assets/screenshots/landing/dynamic-colors-yarrow-light.png" alt="Yarrow's chat, themed with the colors of the channel" width="32%">
</picture>
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./docs/assets/screenshots/landing/dynamic-colors-solanum-dark.png">
  <img src="./docs/assets/screenshots/landing/dynamic-colors-solanum-light.png" alt="Solanum's chat, themed with the colors of the channel" width="32%">
</picture>
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./docs/assets/screenshots/landing/dynamic-colors-poke-dark.png">
  <img src="./docs/assets/screenshots/landing/dynamic-colors-poke-light.png" alt="Poke's chat, themed with the colors of the channel" width="32%">
</picture>
</p>

<p align="center">
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./docs/assets/screenshots/landing/tablet-dark.png">
  <img src="./docs/assets/screenshots/landing/tablet-light.png" alt="The list of live channels next to an open chat, on a tablet" width="100%">
</picture>
</p>

The screenshots are generated from the app; see [docs/README.md](./docs/README.md) to update them.

## Download

| Platform | Download |
| --- | --- |
| Android | [<img src="./docs/assets/badges/google-play-badge.png" alt="Get it on Google Play" height="45">](https://play.google.com/store/apps/details?id=fr.outadoc.justchatting) [<img src="./docs/assets/badges/obtainium-badge.png" alt="Get it on Obtainium" height="45">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/outadoc/just-chatting) [<img src="./docs/assets/badges/github-badge.svg" alt="Get it on GitHub" height="45">](https://github.com/outadoc/just-chatting/releases/latest/download/JustChatting-android.apk) |
| Windows | [![Windows (amd64, .msi)](https://img.shields.io/badge/Windows%20%28amd64%2C%20.msi%29-0078D6?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHJlY3QgeD0iMSIgeT0iMSIgd2lkdGg9IjEwIiBoZWlnaHQ9IjEwIiBmaWxsPSJ3aGl0ZSIvPjxyZWN0IHg9IjEzIiB5PSIxIiB3aWR0aD0iMTAiIGhlaWdodD0iMTAiIGZpbGw9IndoaXRlIi8+PHJlY3QgeD0iMSIgeT0iMTMiIHdpZHRoPSIxMCIgaGVpZ2h0PSIxMCIgZmlsbD0id2hpdGUiLz48cmVjdCB4PSIxMyIgeT0iMTMiIHdpZHRoPSIxMCIgaGVpZ2h0PSIxMCIgZmlsbD0id2hpdGUiLz48L3N2Zz4=)](https://github.com/outadoc/just-chatting/releases/latest/download/JustChatting-windows-amd64.msi) |
| macOS | [![macOS (aarch64, .dmg)](https://img.shields.io/badge/macOS%20%28aarch64%2C%20.dmg%29-000000?logo=apple&logoColor=white)](https://github.com/outadoc/just-chatting/releases/latest/download/JustChatting-macos-aarch64.dmg) |
| Linux | [![Linux (amd64, .AppImage)](https://img.shields.io/badge/Linux%20%28amd64%2C%20.AppImage%29-FCC624?logo=linux&logoColor=black)](https://github.com/outadoc/just-chatting/releases/latest/download/JustChatting-linux-amd64.AppImage) |
| iOS | [![iOS (private TestFlight beta)](https://img.shields.io/badge/iOS-private%20TestFlight%20beta-999999?logo=apple&logoColor=white)](https://testflight.apple.com/join/4AAnUWVX) |

## Project setup

### Android

1. Install [Android Studio](https://developer.android.com/studio/install);
2. Open the project in the IDE.

### iOS

1. Install Xcode 16;
2. Install [Tuist](https://docs.tuist.io/guides/quick-start/install-tuist);
3. Move to the `app-ios` directory, and run `tuist generate` to generate and open the Xcode project.

## Build the project locally

```bash
# Android
./gradlew :app-android:assembleDebug       # Build debug APK
./gradlew :app-android:installDebug        # Install on connected device

# Desktop (JVM)
./gradlew :app-desktop:run                 # Run desktop app

# iOS (requires macOS + Xcode + Tuist)
cd app-ios && tuist generate               # Generate Xcode project, then build in Xcode
```

## ABI

Shared modules contain ABI files to precisely control what's exposed publicly to their consumers.
They can be found in the modules' `api/` directory.

```bash
./gradlew updateKotlinAbi        # Generate ABI files
./gradlew checkKotlinAbi         # Check ABI compatibility
```

## Testing

```bash
./gradlew :shared:testAndroidHostTest                # Run shared module unit tests (Android)
./gradlew :konsist-checks:test                       # Run architecture validation tests
./gradlew :shared-internal:verifySqlDelightMigration # Validate SQLDelight migrations
```

Unit tests are located in `shared/src/commonTest/kotlin/` and `shared/src/desktopTest/kotlin/`.

## Code Formatting

Use ktlint:

```bash
./gradlew ktlintCheck    # Check formatting
./gradlew ktlintFormat   # Auto-format
```

Always run `ktlintFormat` before committing. CI enforces formatting on PRs.

## Architecture

This project is a Kotlin Multiplatform app targeting Android, iOS, and JVM desktop.
The common business logic is included in the `shared` module, and common UI code in `shared-ui`.

Then, by platform:

- `app-android` contains the Android Compose-based application;
- `app-desktop` contains the JVM desktop Compose-based application;
- `app-ios` contains the iOS Compose-based application.
- `app-ios-native` contains the iOS SwiftUI application.

Platform-specific code is kept to an absolute minimum.

### Layers (Clean Architecture, enforced by Konsist)

Code is organized by feature under `fr.outadoc.justchatting.feature.*`, with each feature split
into:

- **domain**: Business logic, interfaces, use cases. No dependencies on other layers.
- **presentation**: ViewModels, UI state. Depends only on domain.
- **data**: Repositories, API clients, database. Depends only on domain.

### Source Sets (shared module)

- `commonMain`: Cross-platform code (vast majority of logic and UI)
- `androidMain`, `iosMain`, `desktopMain`: Platform-specific implementations
- `skiaMain`: Shared between iOS and Desktop (Skia renderer)
- `appleMain`: Shared Apple platform code

## Main dependencies

- **Compose Multiplatform**: Shared UI across all platforms
- **Koin**: Dependency injection. DI modules in `fr.outadoc.justchatting.di`
- **SQLDelight**: Database. Schema files in `shared/src/commonMain/sqldelight/`
- **Ktor**: HTTP client and WebSocket connections
- **kotlinx-serialization**: JSON serialization
