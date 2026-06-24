#!/bin/bash
set -e

# Test script for continuous build setup

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Testing Continuous Build Setup ==="

echo "1. Testing workflow file syntax..."
if [ -f "$SCRIPT_DIR/.github/workflows/debug_build.yml" ]; then
    echo "✓ Workflow file exists"
    # Check if the file has the required sections
    if grep -q "on:" "$SCRIPT_DIR/.github/workflows/debug_build.yml" && grep -q "workflow_dispatch" "$SCRIPT_DIR/.github/workflows/debug_build.yml"; then
        echo "✓ Workflow has proper triggers"
    else
        echo "✗ Workflow missing proper triggers"
        exit 1
    fi
else
    echo "✗ Workflow file not found"
    exit 1
fi

echo "2. Testing build script..."
if [ -f "$SCRIPT_DIR/continuous_build.sh" ] && [ -x "$SCRIPT_DIR/continuous_build.sh" ]; then
    echo "✓ Build script exists and is executable"
else
    echo "✗ Build script missing or not executable"
    exit 1
fi

echo "3. Testing Gradle wrapper..."
if [ -f "$SCRIPT_DIR/gradlew" ] && [ -x "$SCRIPT_DIR/gradlew" ]; then
    echo "✓ Gradle wrapper exists and is executable"
else
    echo "✗ Gradle wrapper missing or not executable"
    exit 1
fi

echo "4. Testing build_termux_package script..."
if [ -f "$SCRIPT_DIR/build_termux_package" ] && [ -x "$SCRIPT_DIR/build_termux_package" ]; then
    echo "✓ build_termux_package script exists and is executable"
else
    echo "✗ build_termux_package script missing or not executable"
    exit 1
fi

echo "5. Checking project structure..."
if [ -d "$SCRIPT_DIR/app" ] && [ -d "$SCRIPT_DIR/shell-loader" ]; then
    echo "✓ Project structure is correct"
else
    echo "✗ Project structure is incorrect"
    exit 1
fi

echo "=== All tests passed! ==="
