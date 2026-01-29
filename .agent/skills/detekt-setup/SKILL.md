---
name: detekt-setup
description: Sets up Detekt static analysis in a Kotlin Multiplatform project. Use when a project lacks code analysis or needs a standardized Detekt configuration.
---

# Detekt Setup Skill

This skill provides a systematic approach to integrating Detekt into a project for static code analysis.

## Workflow

### 1. Add Plugin
In your root `build.gradle.kts`, add the Detekt plugin:
```kotlin
plugins {
    alias(libs.plugins.detekt)
}
```

### 2. Configure Plugin
Add the `detekt` configuration block to the root `build.gradle.kts`. This is where you specify the config file, source sets, and other options.

```kotlin
detekt {
    toolVersion = libs.versions.detekt.get()

    // Configure source directories for KMP
    source.setFrom(
        files(
            "shared/core/src/commonMain/kotlin",
            "shared/data/src/commonMain/kotlin",
            "sharedUI/src/commonMain/kotlin",
            "androidApp/src/main/kotlin",
            "desktopApp/src/main/kotlin",
        ),
    )

    config.setFrom("config/detekt/detekt.yml")
    baseline.set(file("config/detekt/baseline.xml"))
    buildUponDefaultConfig = true
    parallel = true
}
```

### 3. Create Configuration File
Create the default configuration at `config/detekt/detekt.yml`. You can copy a base configuration from the resources of this skill:
[detekt.yml](file:///Users/justinsmith/.gemini/antigravity/skills/detekt-setup/resources/detekt.yml)

### 4. Create Baseline (Optional)
If you are adding Detekt to an existing project with many issues, you can create a baseline to ignore current issues and only track new ones:
```bash
./gradlew detektBaseline
```

### 5. Verify Setup
Run the following command to ensure Detekt is working:
```bash
./gradlew detekt --continue
```

## Best Practices
- **KMP Support**: Always explicitly define `source.setFrom` for KMP projects to ensure all modules are analyzed.
- **Fail on Violations**: Keep `buildUponDefaultConfig = true` and ensure the build fails if violations are found in new code.
- **Custom Rules**: Adjust thresholds (like `LongMethod` or `ComplexCondition`) in `detekt.yml` to fit your project's needs.

## Resources
- [Standard detekt.yml Template](file:///Users/justinsmith/.gemini/antigravity/skills/detekt-setup/resources/detekt.yml)
