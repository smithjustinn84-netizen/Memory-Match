# KMP Project Instructions (AGENTS.md)

## 1. Project Overview
- **Type:** Kotlin Multiplatform (KMP) Mobile Application.
- **Targets:** Android (Jetpack Compose), iOS (Compose Multiplatform / SwiftUI via UIKitInterop), JVM (Desktop).
- **Core Stack:**
  - **UI:** Compose Multiplatform (shared UI).
  - **Language:** Kotlin 2.x (Use K2 compiler features).
  - **DI:** Metro.
  - **Async:** Kotlin Coroutines & Flow.
  - **Network:** Ktor Client.
  - **Persistence:** Room (KMP).
  - **Navigation:** Voyager for Compose (KMP).

## 2. Architectural Principles
- **Clean Architecture:** Strictly separate `data`, `domain`, and `ui` (presentation) layers.
- **Unidirectional Data Flow (UDF):** Use MVI or MVVM patterns. UI observes `StateFlow` from ScreenModels.
- **Single Source of Truth:** The UI should never modify state directly; it must send `Intents` or `Events` to the ViewModel.
- **Platform Agnostic:** 99% of code should live in `commonMain`.
  - **Avoid** logic in `androidMain` or `iosMain` unless strictly necessary (e.g., Camera, Bluetooth, FileSystem).
  - Use `expect`/`actual` mechanisms sparingly. Prefer interface abstraction (Dependency Inversion) over `expect`/`actual` classes.

## 3. Coding Standards & Style
- **Kotlin First:** Never use Java. Use Kotlin idioms (extension functions, scoped functions `let`/`apply`/`also`).
- **Compose UI:**
  - Break complex screens into small, reusable Composables.
  - All Composables must accept a `Modifier` as the first optional parameter.
  - hoist state: Pass data *down* and events *up*. Do not pass ViewModels into child components; pass lambdas instead.
- **Concurrency:**
  - Use `suspend` functions for one-shot operations.
  - Use `Flow` for streams of data.
  - **NEVER** block the main thread. Always dispatch IO work to `Dispatchers.IO`.
- **Error Handling:**
  - Use `Result<T>` or a sealed `DataState` class wrapper for repository responses.
  - Do not throw exceptions in domain logic; return failure states.

## 4. Specific Library Guidelines

### Koin (Dependency Injection)
- Define modules in `commonMain`.
- Use `platformModule()` in `androidMain` and `iosMain` for platform-specific dependencies (e.g., Context, NSUserDefaults).
- Use constructor injection for all classes.

### Room / SQLite (Persistence)
- Use the KMP compatible version of Room.
- DAOs must be interfaces defined in `commonMain`.
- Database instantiation logic belongs in platform-specific source sets, injected via DI.

### Ktor (Networking)
- Use `ContentNegotiation` with `kotlinx.serialization`.
- Define a single `HttpClient` instance in `commonMain` DI module.
- Handle `ClientRequestException` and `ServerResponseException` globally or in a safe-call wrapper.

## 5. Testing Guidelines
- **Unit Tests:** Write in `commonTest`. Logic coverage should be near 100%.
- **Mocks:** Use a multiplatform mocking library (e.g., Mokkery) or manual fakes.
- **UI Tests:** Use Compose Multiplatform testing APIs for shared UI behavior.

## 6. Forbidden Patterns 
- ❌ **NO** `AsyncTask` or `Thread` usage. Use Coroutines.
- ❌ **NO** `synthetics` (deprecated Android).
- ❌ **NO** putting business logic in Composables.
- ❌ **NO** hardcoded strings. Use resources (Moko Resources or Compose Multiplatform Resources).
- ❌ **NO** Force unwrapping (`!!`). Use `?.` or `?:`.

## 7. Generator Instructions
- When creating new features, generate the following file structure:
  - `domain/models/` (Data classes)
  - `domain/usecases/` (Business logic)
  - `domain/repositories/` (Interfaces)
  - `data/repositories/` (Implementations)
  - `data/remote/` (DTOs & API services)
  - `ui/screens/` (Composables & ViewModels)
- Always add KDoc for public interfaces.
