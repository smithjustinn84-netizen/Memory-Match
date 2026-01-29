---
name: kover-manager
description: Helps increase and maintain code coverage using Kover. Use when you need to identify test gaps or verify coverage requirements.
---

# Kover Manager Skill

This skill provides a systematic approach to identifying untested code and increasing code coverage.

## Workflow

### 1. Generate Coverage Report
Run the Kover report task to see current metrics:
```bash
./gradlew koverHtmlReport
```
### 1b. (Optional) Use Helper Script
Maintain coverage easily with the helper script:
```bash
/Users/justinsmith/.gemini/antigravity/skills/kover-manager/scripts/manage_coverage.sh
```
This generates a detailed HTML report at `build/reports/kover/html/index.html`.

### 2. Identify Gaps
Open the HTML report in a browser (or use the tool to read it) to identify:
- **Red lines**: Untested code paths.
- **Uncovered branches**: `if`/`when` conditions where only one side is tested.
- **Untested files**: Entire classes or functions with 0% coverage.

### 3. Strategy for Improvement

#### Domain Logic First
Prioritize coverage for `shared:core` (domain logic). Use `Turbine` for flows and `Mokkery` for mocking dependencies.
- **Focus**: `MemoryGameLogic`, `ScoringConfig`, etc.

#### Edge Case Testing
Look for `if` statements and `error` conditions. Write tests specifically for:
- Invalid inputs (negative scores, empty lists).
- Boundary conditions (state transitions at exact thresholds).
- Exception handling.

#### Verification
After adding tests, verify the coverage has increased:
```bash
./gradlew koverVerify
```
This checks if the coverage meets the minimum threshold defined in `build.gradle.kts`.

### 4. Continuous Maintenance
If coverage drops significantly, investigate why:
- Did you add a large new class without tests?
- Is there unreachable code that should be deleted?

## Tips
- Use `@Composable` exclusions if specified in the build config (UI code is often excluded from Kover).
- Mock complex dependencies to isolate the logic you are testing.
- Use `Turbine` to verify StateFlow emissions in your components.
