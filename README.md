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

## Project Structure (planned)

```text
├── app/                   # main application module
├── docs/                  # learning notes and extra files
│
└── (other files)
```

## Application Structure (planned)

```text
src/
└── src/main/java/com/pedrozc90/prototype/
├── core/
│   ├── di/                         # dependency injection (DI) providers (AppContainer, ViewModelFactory, etc.)
│   │   ├── AppContainer.kt
│   │   └── AppViewModelProvider.kt
│   ├── network/                    # network helpers (interceptors, Result wrappers)
│   └── utils/                      # extensions, general utilities
├── domain/                         # pure business logic (ports)
│   ├── models/                     # domain entities / value objects
│   │   └── User.kt
│   ├── repositories/               # repository interfaces (ports)
│   │   └── UserRepository.kt
│   └── usecase/                   # interactors / use-cases
│       └── GetUserUseCase.kt
├── data/                          # adapters of ports to concrete infra
│   ├── local/                     # local data sources (DataStore, SharedPrefs)
│   │   └── PreferencesDataSource.kt
│   ├── db/                        # Room: entities (if data-specific), DAOs, DB
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   └── UserDao.kt
│   │   └── entity/                # Room entities if they differ from domain model
│   │       └── UserEntity.kt
│   ├── remote/                    # Retrofit APIs and DTOs
│   │   ├── ApiService.kt
│   │   └── dto/
│   │       └── UserDto.kt
│   ├── repository/                # repository implementations (adapters)
│   │   └── UserRepositoryImpl.kt  # implements domain.repository.UserRepository
│   └── mapper/                    # mappers between DTO/Entity <-> Domain model
│       └── UserMapper.kt
├── ui/
│   ├── navigation/
│   ├── screens/
│   │   └── home/
│   │       ├── HomeScreen.kt
│   │       └── HomeViewModel.kt
│   ├── theme/
│   └── PrototypeActivity.kt
├── Constants.kt
├── PrototypeActivity.kt            # main activity
├── PrototypeApp.kt                 # root composable
└── PrototypeApplication.kt         # android application (instantiate AppContainer here)
```

## Roadmap

-  Compose basics (layouts, state, theming)
-  Navigation (Navigation Compose)
-  ViewModel + StateFlow
-  ~~Hilt for DI~~ (skip it for now)
-  Manual Dependency Injection (DI)
-  Room for persistence
-  DataStore for local persistence
-  Retrofit for http requests
-  CI for lint/tests

## License

Please, read [LICENSE](./LICENSE) file.
