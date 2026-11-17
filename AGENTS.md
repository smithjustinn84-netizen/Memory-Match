# 🤖 AGENTS.md: Project Master Router

This is your **Primary Entry Point**. 
Memory-Match follows **Progressive Disclosure** to keep context lean.

---

## 🧭 1. Context Map

1.  **[Architecture & DSLs (GEMINI.md)](GEMINI.md)**: The "Brain". Deep dive into logic and structure.
2.  **[Rules Index (.agent/rules/INDEX.md)](.agent/rules/INDEX.md)**: Full directory of coding standards.
3.  **[Agent Best Practices (.agent/BEST_PRACTICES.md)](.agent/BEST_PRACTICES.md)**: Behaving like a 2026 expert.

---

## 🛠️ 2. Memory-Match CLI (`mm.py`)

**Always prefer these commands over raw Gradle.**

-   **`./mm.py task new <type> <name>`**: Start a new task.
-   **`./mm.py context`**: Generate high-signal summary of changes.
-   **`./mm.py done`**: Verify, format, and prepare commit message.
-   **`./mm.py build [android|ios|desktop|metadata]`**: Efficiently check compilation (iOS builds the arm64 simulator app).
-   **`./mm.py build desktop --hot`**: Run desktop with Compose Hot Reload (UI edits apply live).
-   **`./mm.py test [all|shared|android|desktop]`**: Run specialized test suites.

---

## 📏 3. Quick Reference

-   **DI**: Metro 1.5+ (`@Inject`, `@Provides`, `@SingleIn(AppScope::class)`); dependencies reach UI through `AppGraph`.
-   **UI**: Compose Multiplatform + Decompose (persistent collections).
-   **Checklist**: **[Feature Creation (.agent/rules/feature-creation.md)](.agent/rules/feature-creation.md)**.

---

## 🎨 4. Design System ("Card Room")

The look is defined in `sharedUI/src/commonMain/kotlin/io/github/smithjustinn/theme/`. Read tokens from `PokerTheme`; do not hardcode them.

-   **Colours**: `PokerTheme.colors` — felt greens, `goldenYellow` (brass gold), `ivory`, `sage`, `ink`, `cardStock`, `raisedSurface`, `hudBackground`. Heat mode swaps the palette, so tokens give the ember look for free; a hardcoded hex will not.
-   **Type**: `PokerTheme.fonts.display` (Fraunces, titles), `.body` (Inter), `.mono` (JetBrains Mono, numerals and scores). Never use `FontFamily.Serif` or `FontFamily.Monospace`.
-   **Shapes**: `PokerTheme.shapes.small/medium/large` = 6/14/20dp.
-   **Components**: reuse `PokerButton` (primary = gold with a brass lip, otherwise outlined), `AppCard` (raised panel), `PillSegmentedControl`, `PokerChip`.
-   **Copy**: sentence case for buttons and labels; uppercase only for small section labels and HUD captions.
-   **Fonts**: bundled under `sharedUI/src/commonMain/composeResources/font/`; their OFL licence texts live in `composeResources/files/licenses/` and must ship with them.

---

## ✅ 5. Verifying UI Changes

-   **Lint and tests**: `./gradlew spotlessApply detekt :sharedUI:jvmTest` must pass (`./mm.py done` runs the first two).
-   **Live preview**: `./mm.py build desktop --hot` runs the desktop app with Compose Hot Reload.
-   **Driving the app**: while it runs, `./gradlew :desktopApp:hotMcpServer` exposes an MCP server (semantic tree, click, screenshot) for checking screens without the mouse.
-   **Phone widths**: check HUD and tile layouts at about 390dp wide (iOS simulator or a narrow window); the desktop window hides overflow problems.
-   **Strings**: Compose resources do not unescape Android-style values, so write `%` and `'` literally (no `%%`, no surrounding quotes).

---

## 🌿 6. Version Control (Jujutsu)

This is a colocated **jj** repo. Use `jj` for version control, not `git` commands that change state (`commit`, `switch`, `branch`, `rebase`, `stash`). Read-only git commands are fine.

-   **Survey first**: `jj log` and `jj bookmark list -a`. Anonymous jj changes do not appear in `git branch`, so a git-only survey misses work.
-   **Commit**: `jj describe -m "type: summary"` names the working-copy change (`@`); `jj new` starts the next one. `jj commit -m` does both.
-   **Bookmarks**: branches are bookmarks (`jj bookmark set|delete <name>`). They do not follow new changes, so move one before pushing: `jj bookmark set main -r @-`.
-   **Push**: `jj git fetch`, then `jj git push --bookmark <name>`.
-   **Drop work**: `jj abandon <change>`; `jj undo` reverses the last operation.
-   **Detached HEAD**: git reporting a detached HEAD is normal here. Do not `git switch` to fix it.
-   **`mm.py`**: built on jj. `task new` creates a jj workspace in `../worktrees/<name>` with a `<type>/<name>` bookmark, `task remove` forgets it (its changes stay in `jj log`), and `done` suggests a `jj commit` message.

---
*Immutable Source of Truth | Last Updated: November 2026*
