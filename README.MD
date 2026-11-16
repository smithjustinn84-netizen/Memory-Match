<p align="center">
  <img src="screenshots/hero.jpg" width="100%" alt="Memory Match Hero Banner">
</p>

# 🧩 Memory-Match: Blackjack Edition

[![Kotlin 2.4.21](https://img.shields.io/badge/Kotlin-2.4.21-purple?logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform 1.12.2](https://img.shields.io/badge/Compose_Multiplatform-1.12.2-blue?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

**A high-performance, cross-platform memory game built with Kotlin Multiplatform, showcasing modern 2026 development standards.**

Memory-Match is not just a game; it's a reference implementation for **Clean Architecture**, **Modular Design**, and **AI-Agentic Development** in the Kotlin ecosystem. It features a unique **Casino/Poker theme**, combining classic memory mechanics with high-stakes "Heat" systems and dynamic dealer commentary.

---

## 🚀 Platform Matrix

| Platform       | Tier   | Implementation                          | Stability |
| :------------- | :----- | :-------------------------------------- | :-------- |
| **Android**    | Tier 1 | Native via Jetpack Compose & Material 3 | Stable    |
| **iOS**        | Tier 1 | Compose Multiplatform (UIKit Interop)   | Beta      |
| **Desktop**    | Tier 2 | Compose for Desktop (JVM)               | Stable    |
| **Web (Wasm)** | Tier 3 | Compose for Web (Experimental)          | Planned   |

---

## 📸 Experience the UI

The "Card Room" look: deep felt, brass gold and ivory card stock, set in Fraunces, Inter and JetBrains Mono.

<p align="center">
  <img src="screenshots/start.png" width="180" alt="Start screen with difficulty chips and game mode selector">
  <img src="screenshots/game.png" width="180" alt="Game table with face-down cards and the time, pot and score bar">
  <img src="screenshots/match.png" width="180" alt="Game table mid-round with a card face up and matched pairs cleared">
  <img src="screenshots/results.png" width="180" alt="Results shown as a paper receipt with the total payout">
</p>
<p align="center">
  <img src="screenshots/settings.png" width="180" alt="Settings with card back and symbol style pickers">
  <img src="screenshots/high-scores.png" width="180" alt="High scores with difficulty tabs">
  <img src="screenshots/shop.png" width="180" alt="The Shop with card backs and skins">
</p>

---

## 🎮 Gameplay & Features

Memory-Match goes beyond a simple card game by introducing high-stakes mechanics and a dynamic "Heat" system that rewards precision and speed.

### 🕹️ Game Modes
*   **Time Attack**: Race against the clock! Every match rewards you with extra seconds, while mismatches penalize your remaining time. How long can you survive?
*   **Daily Challenge**: A unique, fixed puzzle generated every 24 hours. Compete with others on the same board and see who can achieve the best score.

### 🔥 Combo-Driven "Heat" System
Success breeds intensity. By matching multiple pairs in quick succession, you build a **Combo Multiplier**.
*   **Activation**: Reach a `Combo x4` or higher to trigger **Heat Mode**.
*   **Visual Transformation**: The felt cross-fades from green to ember red, with amber accents and coal-black card backs.
*   **Tactile Feedback**: Haptics become significantly more pronounced, making every match feel explosive.
*   **Rewards**: High combos in Time Attack grant massive "Mega Bonus" time gains, essential for high-score runs.
*   **All-In Double Down**: Activate specific "Doubling" mode to gamble your entire game.
    *   **The Risk**: Instant Game Over on any mismatch.
    *   **The Reward**: 2x Score Multiplier if you clear the board.
    *   **Constraint**: Requires 3+ pairs to activate.

### ✨ Key Features
*   **The Shop**: Spend your hard-earned chips on exclusive Card Backs and Skins to customize your deck, then switch between the ones you own in Settings.
*   **Onboarding**: Interactive walkthrough to guide new players through the mechanics.
*   **Peek Mode**: Get a quick glimpse of the cards before the game starts (if enabled).
*   **Poker-Style Commentary**: A dynamic Dealer personality providing context-aware commentary ("River Magic!", "Pot Odds!", "All In!") based on your gameplay.
*   **High-Fidelity Audio**: Custom **Polyphonic Sound Engine** supporting simultaneous SFX traversal (iOS/Android) and rich environmental audio.
*   **Aurora Ambient Effects**: Beautiful, procedural "Northern Lights" background effects that react to game state and "Heat" levels.
*   **Tactile Haptics**: Professional grade, platform-optimized haptic feedback (CoreHaptics on iOS, VibratorManager on Android) for every interaction.
*   **State Persistence**: Your progress is automatically saved locally using **Room KMP**, allowing you to resume any game exactly where you left off.
*   **Deep-Linked Replays**: Share specific game configurations via deep links (`memorymatch://game?seed=...`). Every game generates a unique seed for sharing exact card layouts.
*   **Rewarded Ads**: Integrated "Panic Button" in Time Attack mode to gain bonus time by watching a rewarded ad (Android/iOS). See **[REWARDED_ADS_GUIDE.md](docs/REWARDED_ADS_GUIDE.md)**.
*   **Comprehensive Stats**: Track your best scores, fastest times, and total moves across all difficulty levels and game modes.

---

## 🏗 Modular Architecture

The project follows a **Local-First, Modular-Core** strategy, ensuring that 95% of business logic remains shared while maintaining platform-specific performance.

```mermaid
graph TD
    subgraph UI_Layer [sharedUI]
        direction TB
        UI[Compose Multiplatform UI]
        C[Decompose Components]
    end

    subgraph Core_Layer [shared:core]
        GSM[GameStateMachine]
        Domain[Domain Models & Logic]
    end

    subgraph Data_Layer [shared:data]
        DB[Room KMP Database]
        API[Ktor Network Client]
        Repo[Repositories]
    end

    subgraph Platforms [App Modules]
        Android[androidApp]
        iOS[iosApp]
        Desktop[desktopApp]
    end

    UI --> C
    C --> GSM
    GSM --> Domain
    C --> Repo
    Repo --> DB
    Repo --> API

    Android --> UI_Layer
    iOS --> UI_Layer
    Desktop --> UI_Layer
```

### 🧠 Modern 2026 Idioms
- **Context Parameters**: Leveraging `-Xcontext-parameters` for clean, boilerplate-free dependency injection.
- **Strong Skipping**: Optimized for Compose Multiplatform performance using `kotlinx.collections.immutable`.
- **K2 Compiler Flow**: Fully optimized for the K2 compiler, ensuring sub-second incremental build times.
- **Local-First Data**: Powered by **Room KMP**, providing a seamless offline-first experience.
- **AI-Native**: Designed to be navigated and extended by both humans and AI agents (see `AGENTS.md`).

---

## 🏛 Architecture (MVI + Component-Based)

We use a **Component-Based MVI** pattern powered by **Decompose**, ensuring strict state management and lifecycle awareness.

| Layer          | Responsibility             | Implementation                               |
| :------------- | :------------------------- | :------------------------------------------- |
| **Presentation**| UI & Platform Interop      | Compose Multiplatform (`@Composable`)        |
| **Navigation**  | Lifecycle & Backstack      | Decompose Components (e.g., `GameComponent`) |
| **Logic**       | State Machine & Rules      | `GameStateMachine` + Domain Services (`MatchEvaluator`, etc.) |
| **Data**        | Persistence & Networking   | Room KMP, Ktor, Repositories                 |

### 🔄 Unidirectional Data Flow (UDF)

1.  **State Exposure**: Components expose a single, immutable `StateFlow<UIState>` that the UI observes.
2.  **User Intent**: The UI communicates user actions (e.g., `onFlipCard`) by calling methods on the Component, which are dispatched to the `GameStateMachine`.
3.  **Side Effects**: One-time ephemeral events (sounds, haptics) are broadcast via a `SharedFlow` to be collected by the UI.
4.  **Game Engine**: Deep dive into the mechanics in **[GAME_ENGINE.md](docs/GAME_ENGINE.md)**.

---

## 📦 Module Responsibilities

| Module           | Responsibility                                                                 |
| :--------------- | :----------------------------------------------------------------------------- |
| `:shared:core`   | The "Brain". Contains domain models, `GameStateMachine`, and common utilities. |
| `:shared:data`   | The "Memory". Manages `Room` database, `Ktor` network client, and repositories.|
| `:sharedUI`      | The "Body". All `Compose` UI, `Decompose` components, and platform abstractions.|
| `:androidApp`    | Android entry point, `AndroidManifest`, and specific platform initialization. |
| `:iosApp`        | iOS Swift entry point (UIKit wrapper) and Xcode project configuration.         |
| `:desktopApp`    | Desktop JVM entry point and JavaFX-powered audio support.                      |

---

## 🛠 Tech Stack (2026 Standard)

| Layer           | Technology                | Version     | Key Benefit                                         |
| :-------------- | :------------------------ | :---------- | :-------------------------------------------------- |
| **Language**    | **Kotlin**                | 2.4.21      | K2 Mode, Context Parameters, Multi-dollar strings   |
| **UI**          | **Compose Multiplatform** | 1.12.2      | Adaptive layouts, Shared Resources, Skiko Rendering |
| **Collections** | **Immutable Collections** | 0.5.2       | Optimized Strong Skipping support for Compose       |
| **DI**          | **Metro**                 | 1.5.2       | Compile-time dependency graph, KMP-native           |
| **Navigation**  | **Decompose**             | 3.6.2       | Lifecycle-aware, platform-agnostic navigation       |
| **Persistence** | **Room (KMP)**            | 2.8.5       | Type-safe SQLite, Bundled Drivers                   |
| **Animations**  | **Compottie**             | 2.3.3       | Lottie for KMP                                      |
| **Testing**     | **Turbine + Mokkery**     | 1.2.1 / 3.x | Flow-first testing, KSP-based mocking               |

---

## 🛠 Built With

Memory-Match is developed using modern AI-first tooling and professional IDEs:
- **Google Antigravity**: For advanced agentic coding and architectural orchestration.
- **Android Studio**: The primary development environment for Kotlin Multiplatform.
- **Gemini**: Powering AI-assisted development, brainstorming, and code generation.
- **Google AI Studio**: For rapid prototyping and testing of generative AI models.
- **Warp Terminal**: The intelligent terminal for modern development workflows.


---

## 🧪 Testing Strategy

Quality is baked into the foundation. We use a multi-layered testing approach to ensure stability across platforms.

### 🛠 Test Suite
- **Shared Domain & Data**: Logic is tested in `commonTest` using **Mokkery** for mocking and **Turbine** for asynchronous Flow validation.
- **Component Tests**: Decompose components are tested in isolation, validating state transitions and side effects.
- **Platform Unit Tests**: Specific implementations (e.g., Room migrations, Android-specific logic) have dedicated tests in their respective modules.

### 🚀 Running Tests
You can run all tests across the entire project with a single command:
```bash
./mm.py test
```

Alternatively, run specific module tests:
- **Shared Module**: `./gradlew :sharedUI:allTests`
- **Android App**: `./gradlew :androidApp:testDebugUnitTest`
- **Desktop App**: `./gradlew :desktopApp:test`

---

## 🔍 Static Analysis (Detekt)

We use **Detekt** to maintain high code quality and enforce Kotlin-specific best practices across all KMP modules. Detekt helps identify code smells, potential bugs, and complexity issues early in the development cycle.

### 🚀 Usage Commands

| Command                           | Description                                                       |
| :-------------------------------- | :---------------------------------------------------------------- |
| `./gradlew detekt`                | Runs analysis on all source sets and generates reports.           |
| `./gradlew detekt --auto-correct` | Automatically fixes minor linting and formatting issues.          |
| `./gradlew detektBaseline`        | Generates or updates the baseline file to ignore existing issues. |
| `./gradlew detektMain`            | Runs analysis specifically on the main source sets.               |

### 📊 Configuration & Reports
- **Config File**: Located at `config/detekt/detekt.yml`. This file defines the rules and severity levels.
- **Reports**: Generated in `build/reports/detekt/` in HTML, XML, SARIF, and Markdown formats.
- **Baselines**: Issues recorded in `config/detekt/baseline.xml` are suppressed, allowing you to focus on new technical debt.

---

## 📊 Code Coverage (Kover)

We use **Kotlinx Kover** to measure and enforce test coverage across our KMP modules. Our current goal is to maintain a minimum of **80% line coverage** for all business logic in the domain and data layers.

### 📈 Current Status
- **Overall Line Coverage**: **90.9%** (Exceeding the 80% target) ✅
- **Domain Layer Coverage**: **100%** 🎯

### 🚀 Usage Commands

| Command                     | Description                                                  |
| :-------------------------- | :----------------------------------------------------------- |
| `./gradlew koverHtmlReport` | Generates a comprehensive HTML coverage report.              |
| `./gradlew koverVerify`     | Verifies that the code coverage meets the defined threshold. |
| `./gradlew koverXmlReport`  | Generates an XML report (useful for CI/CD integration).      |

### 📚 Detailed Documentation
For more information on our coverage strategy, metrics, and best practices, please refer to:
- **[KOVER_COVERAGE_SUMMARY.md](docs/KOVER_COVERAGE_SUMMARY.md)**: A high-level overview of our recent coverage improvements.
- **[KOVER_COVERAGE_RESEARCH.md](docs/KOVER_COVERAGE_RESEARCH.md)**: In-depth research on KMP coverage, Kover limitations, and our 80%+ strategy.

---

## 🤖 AI-Agentic Development

This repository is **Agent-Ready**, designed to be seamlessly navigated and extended by AI coding assistants.

- **[AGENTS.md](AGENTS.md)**: Serves as the **Immutable Source of Truth** for the project. It outlines the architectural foundation, 2026 coding idioms (like Context Parameters), and platform-specific constraints that agents must follow.
- **[.Agent/rules](.Agent/rules)**: Contains granular, context-specific instructions and linting rules. These rules ensure that AI-generated code adheres to the project's strict standards for Compose Multiplatform, Decompose, and Clean Architecture.

If you are using an AI coding assistant (like Cursor, Windsurf, or Antigravity), ensure it indexes these files to maintain high-quality, idiomatic contributions.

---

## ⚙️ Setup & Developer Experience (DX)

### Prerequisites
- **Android Studio Ladybug** or **IntelliJ IDEA 2024.3+**
- **JDK 21** (Azul Zulu recommended)
- **Xcode 15.0+** (for iOS builds)

### Quick Start
```bash
# Clone the repository
git clone https://github.com/smithjustinn/Memory-Match.git

# Validate environment
./gradlew check

# Run the Android app
./gradlew :androidApp:installDebug

# Run the Desktop app
./gradlew :desktopApp:run

# Run the Desktop app with Compose Hot Reload (UI edits apply live)
./mm.py build desktop --hot

# Build for Android or the iOS simulator
./mm.py build android
./mm.py build ios
```

### Fonts
The app bundles [Fraunces](https://fonts.google.com/specimen/Fraunces), [Inter](https://fonts.google.com/specimen/Inter) and [JetBrains Mono](https://fonts.google.com/specimen/JetBrains+Mono), each under the SIL Open Font License 1.1. The licence texts ship with the app in [`sharedUI/src/commonMain/composeResources/files/licenses/`](sharedUI/src/commonMain/composeResources/files/licenses).

---

## 🤝 Contribution Guidelines

We adhere to the **Kotlin 2.4+ Clean Architecture** guidelines.
1. **Consistency**: Check `AGENTS.md` for the latest coding standards.
2. **UI First**: All UI components must reside in `sharedUI/commonMain` unless platform-specific interop (e.g., Apple HealthKit) is required.
3. **State Management**: Maintain Unidirectional Data Flow using Decompose Components.
4. **Commits**: Follow [Conventional Commits](https://www.conventionalcommits.org/).

---
*Developed with ❤️ and Kotlin Multiplatform.*