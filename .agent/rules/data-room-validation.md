---
trigger: glob
globs: ["**/*Database.kt", "**/*Dao.kt", "**/*Entity.kt", "**/*Schema*"]
description: Room Schema Sync
---

# Rule: Room Schema Sync
- **Grounding Source:** `. /schemas/*.json`
- **Validation:** When the user asks to "Add a column to X," the agent must:
  1. Check the latest JSON schema in the `schemas/` folder.
  2. Draft the Migration file in `commonMain`.
  3. Ensure the `exportSchema` flag is set to `true`.
- **Metro DI Integration:** After generating a new DAO, the agent MUST automatically add a `@Provides` method to the `DatabaseModule` interface.