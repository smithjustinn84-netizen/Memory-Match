#!/bin/bash

# Scaffolds a new Antigravity skill structure
# Usage: ./create_skill.sh <skill-name>

SKILL_NAME=$1
SKILLS_DIR="/Users/justinsmith/.gemini/antigravity/skills"

if [ -z "$SKILL_NAME" ]; then
    echo "❌ Error: Skill name is required."
    echo "Usage: ./create_skill.sh <skill-name>"
    exit 1
fi

SKILL_PATH="$SKILLS_DIR/$SKILL_NAME"

if [ -d "$SKILL_PATH" ]; then
    echo "❌ Error: Skill '$SKILL_NAME' already exists at $SKILL_PATH"
    exit 1
fi

echo "🏗️  Scaffolding skill: $SKILL_NAME..."

mkdir -p "$SKILL_PATH/scripts"
mkdir -p "$SKILL_PATH/examples"
mkdir -p "$SKILL_PATH/resources"

cat <<EOF > "$SKILL_PATH/SKILL.md"
---
name: $SKILL_NAME
description: [Provide a concise description of the skill]
---

# $(echo $SKILL_NAME | tr '-' ' ' | awk '{for(i=1;i<=NF;i++)sub(/./,toupper(substr(\$i,1,1)),\$i)}1') Skill

[Describe the purpose and context of this skill]

## Workflow

### 1. Identify
[How to identify that this skill is needed]

### 2. Prepare
[Prerequisite steps or research]

### 3. Execute
[Details on how to perform the core task]

### 4. Verify
[How to verify that the task was completed correctly]
EOF

echo "✅ Skill '$SKILL_NAME' created at $SKILL_PATH"
echo "📝 Edit $SKILL_PATH/SKILL.md to add instructions."
