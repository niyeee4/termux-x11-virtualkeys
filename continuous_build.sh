#!/bin/bash
set -e

cd "$(dirname "$0")"

# Continuous Build Script for Termux:X11
# This script helps automate the build process for continuous integration

echo "=== Termux:X11 Continuous Build ==="

echo "1. Building APKs..."
./gradlew assembleDebug

echo "2. Building companion package..."
./build_termux_package

echo "3. Build completed successfully!"
echo "4. APKs are available in: ./app/build/outputs/apk/debug/"
echo "5. Companion packages are available in: ./app/build/outputs/apk/debug/"

echo "=== Build Complete ==="
