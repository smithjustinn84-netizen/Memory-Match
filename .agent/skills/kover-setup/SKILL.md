---
name: kover-setup
description: Sets up Kover code coverage in a Kotlin Multiplatform project. Use when a project lacks coverage reporting or needs a standardized Kover configuration.
---

# Kover Setup Skill

This skill provides a step-by-step guide to integrating Kover into a project for code coverage reporting and verification.

## Workflow

### 1. Add Plugin
In your root `build.gradle.kts`, add the Kover plugin:
```kotlin
plugins {
    alias(libs.plugins.kover)
}
```

### 2. Configure Multi-module Dependencies (Optional)
If you have a multi-module project, add the subprojects to the root kover configuration:
```kotlin
dependencies {
    kover(project(":shared:core"))
    kover(project(":sharedUI"))
    // Add other modules
}
```

### 3. Configure Exclusions and Rules
Add the `kover` configuration block to the root `build.gradle.kts`. Use the template provided in `resources/kover-config-template.kts` as a starting point.

Key exclusions to include:
- **UI Code**: `@Composable` functions and pure presentation packages.
- **Generated Code**: DI modules, Room implementations, BuildKonfig, and Resources.
- **Test Utilities**: Fakes, Mocks, and Test Helpers.

### 4. Verify Setup
Run the following commands to ensure Kover is working:
```bash
./gradlew koverHtmlReport  # Generates the report
./gradlew koverVerify      # Verifies coverage thresholds
```

## Best Practices
- **Thresholds**: Start with a reasonable threshold (e.g., 80%) and increase it as coverage improves.
- **Annotation Exclusions**: Use `annotatedBy("androidx.compose.runtime.Composable")` to automatically exclude all Composable functions.
- **Package Filtering**: Use `packages("io.github.user.ui.*")` to exclude entire UI packages.

## Resources
- [Standard Kover Config Template](file:///Users/justinsmith/.gemini/antigravity/skills/kover-setup/resources/kover-config-template.kts)
