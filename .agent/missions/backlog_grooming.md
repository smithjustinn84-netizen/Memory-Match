# Mission: Automated Sprint Planning
**Goal:** Analyze the current project state and generate a structured Sprint backlog using the `sprint-manager` skill.

## Execution
1.  **Initialize**: Call the `sprint-manager` skill to begin context gathering.
2.  **Groom**: Use the skill's instructions to scan `TODO.md` and run quality checks.
3.  **Plan**: Generate a new sprint in `.agent/sprints/` using the skill's provided template.

## Success Criteria
- A valid `sprint_XX.md` file is generated.
- The plan addresses tech debt identified by `detekt` or `spotless`.
