---
description: Create a new Decompose component with state machine and UI
---

# 🧩 Create New Component

Follow these steps to create a new UI component using the project's standard architecture.

## 1. Domain Layer
1. Create `domain/model/X.kt` (if needed).
2. Create `domain/state/XStateMachine.kt` to handle logic.

## 2. UI Layer
1. Create `ui/x/XModels.kt` (State, Action, Event).
2. Create `ui/x/XComponent.kt` (Interface).
3. Create `ui/x/DefaultXComponent.kt` (Implementation).
4. Create `ui/x/XContent.kt` (@Composable).

## 3. Integration
1. Make new dependencies injectable with Metro (`@Inject` / `@Provides`) and expose them on `AppGraph`.
2. Add to `RootComponent.Child` and `RootComponent.kt` navigation.

## 4. Testing
1. Create `XStateMachineTest.kt`.
2. Create `XComponentTest.kt`.

## 5. Cleanup
// turbo
1. Run `./gradlew spotlessApply`
