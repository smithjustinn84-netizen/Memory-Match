---
name: spotless-fixer
description: Fixes Spotless and ktlint violations in Kotlin Multiplatform projects. Use when there are formatting errors or lint warnings.
---

# Spotless Fixer Skill

This skill provides a systematic approach to identifying and fixing Spotless (ktlint) violations.

## Workflow

### 1. Identify Violations
Run the check task to see what needs fixing:
```bash
./gradlew spotlessCheck --continue
```
### 1b. (Optional) Use Helper Script
You can also use the included helper script to automate the process:
```bash
/Users/justinsmith/.gemini/antigravity/skills/spotless-fixer/scripts/fix_spotless.sh --apply
```
Pay close attention to the output for files that cannot be automatically fixed.

### 2. Apply Automatic Fixes
Most formatting issues (spacing, newlines, imports) can be fixed automatically:
```bash
./gradlew spotlessApply
```

### 3. Handle Manual Lint Issues
If `spotlessApply` fails or `spotlessCheck` still reports issues, you must fix them manually. Common issues include:

#### `ktlint(compose:lambda-param-in-effect)`
A lambda parameter in a `@Composable` is referenced directly inside a restarting effect (`LaunchedEffect`, `DisposableEffect`, etc.).
- **Fix**: Use `rememberUpdatedState`.
```kotlin
@Composable
fun MyComponent(onAction: () -> Unit) {
    val currentOnAction by rememberUpdatedState(onAction)
    LaunchedEffect(Unit) {
        // use currentOnAction() instead of onAction()
    }
}
```

#### `ktlint(standard:no-unused-imports)`
`spotlessApply` usually handles this, but sometimes manual removal is needed if there are complex import chains.

### 4. Verify Fixes
Always run a final check and compilation to ensure no regressions:
```bash
./gradlew spotlessCheck :shared:core:compileCommonMainKotlinMetadata :sharedUI:compileCommonMainKotlinMetadata
```

## Tips
- If `spotlessApply` fails without a clear error in the terminal, check the Gradle output cache or run with `--info`.
- When fixing Compose-specific rules, refer to the [official Compose lint documentation](https://slack-lints.github.io/slack-lints/composable-functions/).
