---
name: skill-creator
description: Provides instructions and tools for creating new Antigravity skills. Use when you want to extend the AI's capabilities with a new specialized workflow.
---

# Skill Creator Skill

This meta-skill defines the standard structure for all Antigravity skills and provides tools for scaffolding them.

## Skill Structure

Every skill must reside in `/Users/justinsmith/.gemini/antigravity/skills/<skill-name>/` and contain:

1.  **SKILL.md** (Required)
    - Frontmatter with `name` and `description`.
    - Detailed markdown instructions for the AI on how to perform the skill.
2.  **scripts/** (Optional)
    - Executable helper scripts (bash, python, etc.) that the AI can run.
3.  **examples/** (Optional)
    - Reference files or code snippets showing the skill in action.
4.  **resources/** (Optional)
    - Static configuration files, templates, or assets.

## Workflow for Creating a Skill

### 1. Planning
- Identify a repetitive task or a complex workflow that would benefit from specialized instructions.
- Define the `name` (kebab-case) and a concise `description`.

### 2. Scaffolding
Use the provided `create_skill.sh` script to create the directory structure:
```bash
/Users/justinsmith/.gemini/antigravity/skills/skill-creator/scripts/create_skill.sh <your-skill-name>
```

### 3. Writing Instructions (SKILL.md)
- Write clear, step-by-step instructions.
- Use placeholders or templates if applicable.
- Include common error cases and how to resolve them.
- Reference internal scripts or resources using absolute paths.

### 4. Verification
- Verify the skill folder exists in the search path.
- Test the skill by asking for a task that triggers it.

## Tips
- Keep instructions actionable and concise.
- Automate repetitive CLI commands via scripts in `scripts/`.
- Use the `examples/` directory to provide "Few-Shot" context to the AI.
