---
trigger: glob
globs: ["**/*Test.kt"]
---

# 🧱 Decompose Testing Standards

Testing Decompose components requires manual control over the `Lifecycle` and mocking the `ComponentContext`.

## 1. Lifecycle Management
Use `LifecycleRegistry` to drive the component through its states.

```kotlin
private lateinit var lifecycle: LifecycleRegistry

@BeforeTest
fun setUp() {
    lifecycle = LifecycleRegistry()
    lifecycle.onCreate() // Required for component initialization
}

@AfterTest
fun tearDown() {
    lifecycle.onDestroy()
}
```

## 2. Component Initialization
Always initialize the component within the test method or a specialized helper to ensure the `Lifecycle` is ready.

```kotlin
private fun createComponent(): MyComponent {
    return DefaultMyComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        // ... dependencies
    )
}
```

## 3. Navigation Testing
Navigation is driven by the `ChildStack`. Assert against the `active` child instance.

```kotlin
@Test
fun `navigating from Start to Game`() {
    val root = DefaultRootComponent(DefaultComponentContext(lifecycle), appGraph)
    
    val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
    startChild.component.onStartGame()
    
    assertTrue(root.childStack.value.active.instance is RootComponent.Child.Game)
}
```

## 4. StateFlow Assertion (Turbine)
Most components use `StateFlow`. Use Turbine's `test` extension.

```kotlin
component.state.test {
    assertEquals(InitialState, awaitItem())
    component.onAction()
    assertEquals(UpdatedState, awaitItem())
}
```

## 5. Coroutine Dispatchers
Inject `CoroutineDispatchers` and use `StandardTestDispatcher` in tests to ensure deterministic execution, especially when using `testScheduler.advanceTimeBy()`.
