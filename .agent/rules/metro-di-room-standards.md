---
trigger: model_decision
description: Metro DI & Room Integration
---

# Metro DI & Room Integration
- **Platform Modules:** - Define an `expect fun platformModule()` in `commonMain`.
  - In `androidMain`, the actual module should provide `RoomDatabase.Builder<AppDatabase>` using the Android Context.
  - In `iosMain`, provide the builder directly.
- **Dependency Injection:** - The `AppDatabase` instance should be a `@Singleton` in the `commonMain` Metro component.
  - DAOs should be provided by calling `db.daoName()` within the Metro module.
- **Agent Instruction:** "Do not use Koin. If the project uses Metro, ensure that new DAOs are exposed via the central `Component` interface so they can be injected into ScreenModels."