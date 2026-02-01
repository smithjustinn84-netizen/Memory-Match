# Mission: Automated Sprint Planning
**Goal:** Analyze the current project state and generate a structured Sprint backlog.

## Context Gathering
1. Scan `.agent/backlog/` (or your GitHub issues via the integrated tool) for open tickets.
2. Review the current state of `shared/src/commonMain` to identify missing logic for card matching or state persistence.

## Execution Steps
1. **Identify Bottlenecks:** Look for areas where the Memory Match logic is tightly coupled to a single platform.
2. **Generate Sprint Tickets:** Create a new file in `.agent/sprints/sprint_0x.md` with:
    - **Feature Requests:** e.g., "Implement Room-backed high score tracking via Koin DI."
    - **Issues:** e.g., "Standardize card flip animations in Compose Multiplatform for Desktop."
    - **Bug Tickets:** e.g., "Fix memory leak when clearing card bitmaps during game reset."

## Success Criteria
- The generated sprint must have a clear "Production Goal."
- Tickets must be formatted for direct conversion into GitHub Issues.

## Pre-flight Check
Run `./gradlew detekt` and `./gradlew spotlessCheck` before suggesting any "Issue" tickets related to code quality. This ensures the PM is grounding its suggestions in the actual project health.
