---
trigger: glob
globs: ["**/*Database.kt", "**/*Dao.kt", "**/*Entity.kt", "**/*Schema*"]
description: Metro DI & Room Integration
---

# Metro DI & Room Integration
- **Database instance:** Each platform builds `AppDatabase` in its UI module (`AndroidUiModule`, `IosUiModule`, `JvmUiModule`) with an `@SingleIn(AppScope::class) @Provides fun provideAppDatabase()`, because the builder needs platform inputs (Android `Context`, file paths).
  - Keep the three builders in step: the same migrations and the same fallback (`fallbackToDestructiveMigrationOnDowngrade`) on every platform.
- **DAOs:** Provide each DAO from `DataProviders` (`shared/data/.../di/DataProviders.kt`) as `@SingleIn(AppScope::class) @Provides fun provideXDao(database: AppDatabase): XDao = database.xDao()`.
- **Repositories:** Inject DAOs through the constructor of an `@Inject` class bound with `@ContributesBinding(AppScope::class)`.
- **Versions:** Bump `AppDatabase.DATABASE_VERSION` for every schema change and add a `Migration`; never lower or reuse a version number.
- **Agent Instruction:** Use Metro for all dependency injection. Register new DAOs in `DataProviders`.
