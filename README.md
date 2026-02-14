# Just Chatting

An app focused on a great Twitch chat experience.

- iOS and Android support.
- Multi-chat. Open multiple chat bubbles, and switch between them (only on Android.)
- Tablet- and foldable-optimized interface.
- Custom emotes. If the default emotes aren't enough for you, we support third-party emote sets!
- Slide to reply to any message, and see the context of the conversation.
- See your favorite channels' schedule -- past, present, and future, in a unified timeline.

<table>
<tr>
<td>

https://github.com/user-attachments/assets/0f107cee-6294-4fbf-aa15-5466b57a548f

</td>
<td>

![](./assets/screenshots/ios/ipad-static.png)

</td>
</tr>
</table>

## Project setup

### Android

1. Install [Android Studio](https://developer.android.com/studio/install);
2. Open the project in the IDE.

### iOS

1. Install Xcode 16;
2. Install [Tuist](https://docs.tuist.io/guides/quick-start/install-tuist);
3. Move to the `app-ios` directory, and run `tuist generate` to generate and open the Xcode project.

## Build the project locally

### Android

Build the Android app:

```shell
./gradlew :app-android:assembleDebug
```

### Desktop

Run the desktop app:

```shell
./gradlew :app-desktop:run
```

### iOS

Build and run the iOS app on a macOS machine using Xcode, once the project has been generated with
the command above.

## Architecture

This project is a Kotlin Multiplatform app targeting Android, iOS, and JVM desktop. 
The common code (both logic and UI) is included in the `shared` module.

- `app-android` contains the Android-specific code;
- `app-desktop` contains the JVM desktop-specific code;
- `app-ios` contains the iOS-specific code.

Platform-specific code is kept to an absolute minimum.

Code is split by feature, under `fr.outadoc.justchatting.feature`, and split in layers following
the "clean architecture" design pattern.

## Main dependencies

- The UI is written with Compose Multiplatform and shared between all targets.
- Koin is used for dependency injection. DI modules are kept in the `fr.outadoc.justchatting.di`
package.
- SQLDelight is used to manage the SQLite database.
- Network operations are handled by ktor-client.
