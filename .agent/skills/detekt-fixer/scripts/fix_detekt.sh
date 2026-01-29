#!/bin/bash

# Script to run Detekt check or baseline
# Usage: ./fix_detekt.sh [--baseline]

BASELINE=false
if [[ "$1" == "--baseline" ]]; then
    BASELINE=true
fi

if [ "$BASELINE" = true ]; then
    echo "📋 Generating Detekt Baseline..."
    ./gradlew detektBaseline
    echo "✅ Baseline updated."
else
    echo "🔍 Running Detekt Analysis..."
    if ./gradlew detekt --continue; then
        echo "✅ No issues found!"
        exit 0
    else
        echo "❌ Detekt analysis failed."
        echo "📝 Check the report for details: build/reports/detekt/detekt.html"
        exit 1
    fi
fi
