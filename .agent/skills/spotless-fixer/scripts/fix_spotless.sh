#!/bin/bash

# Script to run Spotless check and apply
# Usage: ./fix_spotless.sh [--apply]

APPLY=false
if [[ "$1" == "--apply" ]]; then
    APPLY=true
fi

echo "🔍 Running Spotless Check..."
if ./gradlew spotlessCheck --continue; then
    echo "✅ No violations found!"
    exit 0
else
    echo "❌ Violations detected."
    if [ "$APPLY" = true ]; then
        echo "🔧 Applying automatic fixes..."
        if ./gradlew spotlessApply; then
            echo "✅ Automatic fixes applied. Re-checking..."
            if ./gradlew spotlessCheck; then
                echo "✅ All issues resolved!"
                exit 0
            else
                echo "⚠️ Some issues still persist. Please check for manual lint errors (e.g. lambda-param-in-effect)."
                exit 1
            fi
        else
            echo "❌ spotlessApply failed. Check terminal output for errors."
            exit 1
        fi
    else
        echo "💡 Run with --apply to fix automatic violations."
        exit 1
    fi
fi
