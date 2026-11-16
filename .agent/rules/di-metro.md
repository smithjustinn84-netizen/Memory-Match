---
trigger: glob
globs: ["**/*.kt"]
---

# 💉 Metro DI & AppGraph Pattern

The project uses **Metro (1.5+)**, a compile-time dependency injection framework, with an **AppGraph** interface as the UI layer's view of the graph. There is no Koin in the build; do not add it.

## Guidelines
1. **Scope**: Everything lives in the single application scope, `AppScope` (`shared/core/.../di/AppScope.kt`).
2. **Classes you own** (repositories, use cases, services): annotate the class with `@Inject` and take dependencies through the constructor.
   - To bind an implementation to its interface, add `@ContributesBinding(AppScope::class)`.
   - Add `@SingleIn(AppScope::class)` when one shared instance is required.
3. **Things you cannot annotate** (DAOs, `Json`, the logger, platform objects): add an `@Provides` function to a provider interface marked `@ContributesTo(AppScope::class)`.
   - Shared: `CoreProviders` (`shared/core`) and `DataProviders` (`shared/data`).
   - Per platform: `AndroidUiModule`, `IosUiModule`, `JvmUiModule` in `sharedUI/src/<platform>Main/.../di/`.
4. **Graphs**: Each platform has one `@DependencyGraph(AppScope::class)` interface (`AndroidAppGraph`, `IosAppGraph`, `JvmAppGraph`) that extends `AppGraph` and the provider interfaces. Runtime inputs such as the Android `Context` enter through its `@DependencyGraph.Factory`.
5. **AppGraph**: A plain interface in `sharedUI/src/commonMain/.../di/AppGraph.kt` listing what the UI layer may use.
   - Pass `AppGraph` to Decompose components at construction.
   - In Composables, read it from `LocalAppGraph`.
6. **Errors are compile errors**: a missing binding fails the build of the platform graph, not the app at runtime. Compile a platform target (`./mm.py build desktop`) after changing the graph; the metadata-only build does not validate it.

## Example (injectable class)
```kotlin
@ContributesBinding(AppScope::class)
@Inject
class LeaderboardRepositoryImpl(
    private val dao: LeaderboardDao,
    private val logger: Logger,
) : LeaderboardRepository
```

## Example (AppGraph usage)
```kotlin
class DefaultGameComponent(
    private val appGraph: AppGraph,
    componentContext: ComponentContext,
) : GameComponent, ComponentContext by componentContext {
    private val gameStateRepository = appGraph.gameStateRepository
    // ...
}
```

## Adding a dependency the UI needs
1. Make it injectable (`@Inject`, plus `@ContributesBinding` or an `@Provides` function).
2. Add a property for it to `AppGraph`; Metro implements it in every platform graph.
3. In tests, stub it on the mocked graph in `MokkeryTestContext` (`every { appGraph.myUseCase } returns ...`), or every component test that touches it fails with `CallNotMockedException`.
