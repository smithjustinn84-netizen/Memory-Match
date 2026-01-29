#!/bin/bash

# Script to run Kover reports and verification
# Usage: ./manage_coverage.sh [--verify]

VERIFY=false
if [[ "$1" == "--verify" ]]; then
    VERIFY=true
fi

if [ "$VERIFY" = true ]; then
    echo "✅ Verifying Coverage Thresholds..."
    if ./gradlew koverVerify; then
        echo "🎉 Coverage meets requirements!"
        exit 0
    else
        echo "❌ Coverage verification failed. See report for details."
        exit 1
    fi
else
    echo "📊 Generating Kover HTML Report..."
    if ./gradlew koverHtmlReport; then
        echo "✅ Report generated."
        echo "📍 Path: build/reports/kover/html/index.html"
        exit 0
    else
        echo "❌ Failed to generate report."
        exit 1
    fi
fi
