---
name: spotless-setup
description: Sets up Spotless and ktlint in a Kotlin Multiplatform project. Use when a project lacks code formatting or needs a standardized ktlint configuration.
---

# Spotless Setup Skill

This skill provides a step-by-step guide to integrating Spotless and ktlint into a project for automated code formatting.

## Workflow

### 1. Add Plugin
In your root `build.gradle.kts`, add the Spotless plugin to the plugins block:
```kotlin
plugins {
    alias(libs.plugins.spotless)
}
```

### 2. Configure Plugin
Add the `spotless` configuration block to your `build.gradle.kts`. It's recommended to apply this to all subprojects in a multi-module project.

```kotlin
subprojects {
    apply(plugin = "com.diffplug.spotless")
    
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            
            // Enable ktlint with specific version
            ktlint(libs.versions.ktlint.get())
                .setEditorConfigPath(rootProject.file(".editorconfig"))
                // Add Compose-specific rules for ktlint
                .customRuleSets(
                    listOf(libs.compose.rules.get().toString())
                )

            trimTrailingWhitespace()
            endWithNewline()
        }
        
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }
}
```

### 3. Create .editorconfig
Create a `.editorconfig` file in the root of your project to define formatting rules. You can use the template provided in the resources of this skill:
[.editorconfig](file:///Users/justinsmith/.gemini/antigravity/skills/spotless-setup/resources/.editorconfig)

### 4. Verify Setup
Run the following commands to ensure Spotless is working:
```bash
./gradlew spotlessCheck  # Verifies formatting
./gradlew spotlessApply  # Applies fixes automatically
```

## Best Practices
- **Standard rules**: Disable `no-wildcard-imports` and `filename` rules if they conflict with project conventions.
- **Compose Support**: Use `customRuleSets` to include `compose-rules` for ktlint.
- **Git Integration**: Consider adding `spotlessCheck` to your CI/CD pipeline or a pre-commit hook.

## Resources
- [Standard .editorconfig Template](file:///Users/justinsmith/.gemini/antigravity/skills/spotless-setup/resources/.editorconfig)
