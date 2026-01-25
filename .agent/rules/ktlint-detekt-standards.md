# 🛠 Ktlint & Detekt Standards

Expert instructions for maintaining code quality and static analysis standards in the Memory Match project.

## 📋 General Principles
- **Clean Code First**: Prioritize readability and maintainability.
- **Combined Strictness**: Follow both `ktlint` (formatting) and `detekt` (static analysis). If a conflict arises, prefer the stricter rule (usually `detekt`).
- **Nomenclature**: Use standard Kotlin/Compose naming conventions as defined in project configurations.

## 🎨 Ktlint Standards (Formatting)
Based on [.editorconfig](file:///Users/justinsmith/IdeaProjects/Memory-Match/.editorconfig):
- **Line Length**: Maximum 120 characters per line.
- **Indentation**: Use 4 spaces for indentation.
- **Wildcard Imports**: **PROHIBITED** (even if disabled in ktlint, `detekt` enforces this). Always use explicit imports.
- **Naming Exceptions**: 
    - **PascalCase** is allowed for `@Composable` functions.
    - **Backing Properties** (e.g., `_myProperty`) are allowed for private state backing.
- **Final Newline**: Always ensure files end with a final newline.

## 🔍 Detekt Standards (Static Analysis)
Based on [detekt.yml](file:///Users/justinsmith/IdeaProjects/Memory-Match/config/detekt/detekt.yml):
- **Complexity**:
    - **Cyclomatic Complexity**: Max 14 per method.
    - **Nested Block Depth**: Max 4 levels deep.
    - **Method Length**: Max 60 lines.
- **Functions & Parameters**:
    - **Function Parameters**: Max 5 parameters.
    - **Constructor Parameters**: Max 6 parameters.
    - **Function Naming**: Use `camelCase` (except `@Composable` functions).
- **Potential Bugs**:
    - **Magic Numbers**: Avoid hardcoded numbers. **Allowed values**: -1, 0, 1, 2. (Ignored in tests and `.kts` scripts).
    - **Exception Handling**: 
        - Never catch generic `Exception` or `Throwable` without a very specific reason.
        - Never swallow exceptions (provide at least a log or a TODO).
        - Use `_` or `ignore` for genuinely ignorable exceptions.
    - **Return Count**: Max 2 return statements per function (excluding guards and lambdas).

## 🚀 Correction Protocol
When making changes:
1. **Self-Correction**: If you see a magic number or a long parameter list, refactor it immediately.
2. **Verification**: After completing code changes, you SHOULD run:
   ```bash
   ./gradlew detekt
   ```
3. **Formatting**: Use explicit imports and maintain the 120-character line limit.
