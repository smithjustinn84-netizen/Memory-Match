---
name: detekt-fixer
description: Fixes Detekt static analysis issues. Use when there are code smells, complexity issues, or style violations reported by Detekt.
---

# Detekt Fixer Skill

This skill provides a systematic approach to identifying, resolving, or suppressing Detekt violations.

## Workflow

### 1. Identify Violations
Run the detekt task to see the report:
```bash
./gradlew detekt --continue
```
The summary report will list files and specific rules violated (e.g., `MagicNumber`, `LongMethod`).

### 2. Resolve Common Issues

#### `MagicNumber`
Hardcoded numbers found in the code.
- **Fix**: Move the number to a private constant with a descriptive name.
```kotlin
// Before
val speed = 500

// After
private const val DEFAULT_ANIMATION_SPEED_MS = 500
val speed = DEFAULT_ANIMATION_SPEED_MS
```

#### `LongMethod` / `CyclomaticComplexMethod`
Functions that are too long or have too many branching paths.
- **Fix**: Extract smaller, focused functions. If it's a Composable, break it into sub-composables.

#### `ComplexCondition`
`if` or `when` conditions with too many logical operators.
- **Fix**: Extract the condition into a well-named boolean variable or a private helper function.

#### `TooManyFunctions`
Too many functions in a single file.
- **Fix**: Split the file into multiple files or move related functions to a companion object or a singleton if appropriate.

### 3. Suppress Violations
If a violation is intentional or cannot be reasonably fixed (e.g., a complex animation that must be in one block), use `@Suppress`:
```kotlin
@Suppress("MagicNumber")
val obscureMath = 42 * 7
```

### 4. Update Baseline
If you have many existing issues that you want to ignore for now and only track new ones:
```bash
./gradlew detektBaseline
```

### 5. Verify Fixes
Always run detekt again to ensure the issues are gone:
```bash
./gradlew detekt
```

## Tips
- Use the HTML report for a better overview of all issues: `build/reports/detekt/detekt.html`.
- Focus on `Complexity` and `Maintainability` issues first, as they often hide bugs.
