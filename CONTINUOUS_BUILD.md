# Continuous Build Setup for Termux:X11

This document describes the continuous build setup for Termux:X11, which ensures that APKs are built regularly and automatically.

## Overview

The continuous build system for Termux:X11 includes:

1. **GitHub Actions Workflow**: Automated builds triggered by various events
2. **Build Script**: Manual build script for local development
3. **Artifact Management**: Organized storage and cleanup of build artifacts

## GitHub Actions Workflow

The workflow (`.github/workflows/debug_build.yml`) builds APKs on:

### Triggers

1. **Manual Trigger**: `workflow_dispatch` - Allows manual build triggering
2. **Push Events**:
   - `master` branch - Production builds
   - `develop` branch - Development builds
   - `feature/*` branches - Feature development builds
   - `bugfix/*` branches - Bug fix builds
3. **Pull Request Events**:
   - `master` branch - CI validation
   - `develop` branch - Integration testing
4. **Scheduled Builds**:
   - Daily at 2 AM UTC
   - Weekly on Sunday at midnight UTC

### Build Process

1. **Clone Repository**: Fetch the latest code with submodules
2. **Validate Gradle Wrapper**: Ensure wrapper is up to date
3. **Setup Java**: Configure Java environment
4. **Restore Cache**: Use cached Gradle dependencies
5. **Build APKs**: Run `./gradlew assembleDebug`
6. **Build Companion Package**: Run `./build_termux_package`
7. **Clean Artifacts**: Remove old build artifacts
8. **Store Artifacts**: Upload APKs and companion packages
9. **Update Nightly Release**: Create nightly release on master/develop

### Artifacts Built

- **APK Variants**:
  - `app-arm64-v8a-debug.apk` - ARM64 architecture
  - `app-armeabi-v7a-debug.apk` - ARMv7 architecture
  - `app-universal-debug.apk` - Universal package
  - `app-x86_64-debug.apk` - x86_64 architecture
  - `app-x86-debug.apk` - x86 architecture

- **Companion Packages**:
  - `termux-x11-nightly-*.all.deb` - Debian package
  - `termux-x11-nightly-*.any.pkg.tar.xz` - Pacman package

## Local Build Script

The `continuous_build.sh` script provides a simple way to build APKs locally:

```bash
./continuous_build.sh
```

This script:

1. Builds all APK variants
2. Builds the companion package
3. Reports build completion

## Artifact Management

### Storage

All build artifacts are stored in:
- `./app/build/outputs/apk/debug/` - APK files
- `./app/build/intermediates/` - Build intermediates

### Cleanup

The workflow automatically cleans up old artifacts before each build to:
- Reduce disk usage
- Ensure clean builds
- Prevent accumulation of outdated files

## Usage

### Manual Build

Trigger a manual build via:
1. Go to the Actions tab in your GitHub repository
2. Select the "Build" workflow
3. Click "Run workflow" and select the branch

### Local Development

For local development:

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/termux/termux-x11
cd termux-x11

# Build locally
./continuous_build.sh
```

## Benefits

1. **Regular Updates**: Scheduled builds ensure fresh APKs are always available
2. **Multi-Architecture**: Supports all major Android architectures
3. **Automated Testing**: Pull request builds validate changes
4. **Clean Builds**: Automatic cleanup prevents artifact accumulation
5. **Easy Access**: Artifacts are easily downloadable from GitHub Actions

## Troubleshooting

### Build Failures

If builds fail:
1. Check the build logs in GitHub Actions
2. Ensure all dependencies are up to date
3. Verify the Gradle wrapper is correct

### Missing Artifacts

If artifacts are missing:
1. Check the build logs for errors
2. Verify the build steps completed successfully
3. Ensure the artifact upload steps ran

### Scheduled Build Issues

If scheduled builds don't run:
1. Check the GitHub Actions schedule settings
2. Verify the repository has proper permissions
3. Check for any rate limiting issues
