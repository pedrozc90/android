# Android — Learning Repo

A personal learning repository for Android development using Kotlin and Jetpack Compose.

## About

This repo is a playground to learn and experiment with modern Android tooling and libraries.

It’s intended as a reference for small sample apps and notes while learning.

## Status

Initial commit — no app code yet. Planning and experiments to follow.

## Tech (planned)

- Kotlin
- Jetpack Compose
- Android SDK (minSdk: TBD)
- Android Studio (recommended)
- SDK 33+ (Android 13+)

## Quick start

1. Clone:

```bash
   git clone https://github.com/pedrozc90/android.git
```

2. Open the project in Android Studio when project files exist.
3. Let Gradle sync, then run on an emulator or device.

Command-line (when project exists):
- Build debug APK: `./gradlew assembleDebug`

## Project structure (planned)

```
├── app\                   # main application module
├── docs\                  # learning notes and extra files
│
└── (other files)
```

## Roadmap

-  Compose basics (layouts, state, theming)
-  Navigation (Navigation Compose)
-  ViewModel + StateFlow
-  ~~Hilt for DI~~ (Don´t want it)
-  Manual Dependency Injection (DI)
-  Room for persistence
-  DataStore for local persistence
-  Retrofit for http requests
-  CI for lint/tests

## License

Please, read [LICENSE](./LICENSE) file.
