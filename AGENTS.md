# KMP Project Instructions (AGENTS.md)

This document serves as the **Source of Truth** for all AI agents and developers. Follow these rules strictly to maintain architectural integrity, consistency, and cross-platform compatibility.

---

## 📋 Table of Contents
1. [Project Overview & Stack](#1-project-overview--stack)
2. [Architectural Principles](#2-architectural-principles)
3. [Coding Standards](#3-coding-standards)
4. [Library Implementation Rules](#4-library-implementation-rules)
5. [Testing Strategy](#5-testing-strategy)
6. [Prohibited Patterns](#6-prohibited-patterns)
7. [Feature Generation Workflow](#7-feature-generation-workflow)

---

## 1. Project Overview & Stack

- **Project Type**: Kotlin Multiplatform (KMP)
- **Targets**:
    - **Android**: SDK 23+ (Jetpack Compose)
    - **iOS**: iOS 14+ (Compose Multiplatform)
    - **JVM**: Desktop (Compose Multiplatform)
- **Core Stack**:
    - **Language**: Kotlin 2.x (K2 Compiler). Use modern idioms and context receivers.
    - **UI**: Compose Multiplatform (Shared UI).
    - **DI**: **Metro** (Compile-time Dependency Injection).
    - **Async**: Coroutines & Flow.
    - **Network**: **Ktor** (ContentNegotiation & Serialization).
    - **Persistence**: **Room (KMP)**.
    - **Navigation**: **Voyager** (ScreenModel-based).
    - **Logging**: **Kermit**.
    - **Images**: **Coil3**.
    - **Resources**: Compose Multiplatform Resources.
    - **Date/Time**: Kotlin Standard Library (`kotlin.time`) + `kotlinx-datetime`.

---

## 2. Architectural Principles (MANDATORY)

### Clean Architecture
- **Domain Layer**: Pure Kotlin. Models, UseCases, and Repository Interfaces. **No UI or Data dependencies.**
- **Data Layer**: Repository implementations, DTOs, Mappers, and Data Sources (Room/Ktor).
- **UI Layer**: Composables and Voyager ScreenModels.

### Unidirectional Data Flow (UDF)
- UI observes a single `State` object via `StateFlow`.
- UI communicates with `ScreenModel` via `Intent` or `Event` functions.
- **No side-effects in Composables**: Use `LaunchedEffect` only for UI-bound logic.

### Multiplatform Strategy
- **95%+ Code in `commonMain`**.
- **Abstraction over Expect/Actual**: Prefer interfaces in `commonMain` injected via Metro. Only use `expect/actual` for simple values or when interfaces are overkill.

---

## 3. Coding Standards

### Kotlin Best Practices
- **No Java**: Never use `java.*` in `commonMain`.
- **Null Safety**: Avoid `!!`. Use `requireNotNull()` or safe calls.
- **Immutability**: Use `val` and `data class` by default. Use `.copy()` for state updates.

### Compose UI Guidelines
- **Modifiers**: Every public Composable must accept `modifier: Modifier = Modifier` as the first optional parameter.
- **Statelessness**: Hoist state. Pass values and lambdas, not `ScreenModel` instances.
- **Previews**: Provide `@Preview` in `androidMain` or `desktopMain`.

---

## 4. Library Implementation Rules

### Metro (Dependency Injection)
- **Dependency Graphs**: Use `@DependencyGraph` for primary entry points.
- **Constructor Injection**: Classes intended for the graph should be annotated with `@Inject` on the constructor (Note: Metro uses `@Inject` similar to Dagger/Hilt).
- **Factories**: Use `@DependencyGraph.Factory` for runtime inputs. Use `@Provides` on factory parameters.
    ```kotlin
    @DependencyGraph.Factory
    interface AppGraphFactory {
        fun create(@Provides baseUrl: String): AppGraph
    }
    ```
- **Binding Containers**: Use `@BindingContainer` (analogous to Dagger Modules).
- **Providers**: Use `@Provides` (NOT `@Provider`) for methods that return types that cannot be constructor-injected.
    ```kotlin
    @Provides 
    fun provideHttpClient(): HttpClient = HttpClient()
    ```
- **Intrinsics**: Use `createGraph<T>()` or `createGraphFactory<T>()` to instantiate.
- **Scoping**: Use `@SingleIn(Scope::class)` for scoped dependencies.

### Room (KMP)
- Database and DAOs must reside in `commonMain`.
- Schema files are located in `sharedUI/schemas`.
- Use `RoomDatabase.Builder` with platform-specific drivers.

### Voyager Navigation
- Use `Screen` for UI and `ScreenModel` for logic.
- Integrate Metro to provide `ScreenModel` instances.

---

## 5. Testing Strategy
- **Logic Tests**: All in `commonTest`.
- **Mocks**: Use **Mokkery** or manual Fakes.
- **Coroutines**: Use `runTest` and `StandardTestDispatcher`.
- **Flows**: Use **Turbine** for assertion.

---

## 6. Prohibited Patterns (The "Never" List)
- ❌ **No `viewModelScope`**: Use `screenModelScope` from Voyager.
- ❌ **No Hardcoded Strings**: Use `Res.string.key_name`.
- ❌ **No Logic in Composables**: Business rules belong in UseCases or ScreenModels.
- ❌ **No Platform Imports in Common**: Never import `android.*`, `UIKit.*`, or `java.*` in `commonMain`.
- ❌ **No SQLDelight/Koin**: Use Room and Metro as specified.

---

## 7. Feature Generation Workflow
When creating a new feature (e.g., `FeatureX`), generate the following components:

1.  [ ] `domain/model/FeatureX.kt`
2.  [ ] `domain/repository/FeatureXRepository.kt`
3.  [ ] `domain/usecase/GetFeatureXUseCase.kt`
4.  [ ] `data/repository/FeatureXRepositoryImpl.kt`
5.  [ ] `ui/feature_x/FeatureXScreen.kt`
6.  [ ] `ui/feature_x/FeatureXScreenModel.kt`
7.  [ ] Update Metro `@BindingContainer` or `@Module` to include new dependencies.
