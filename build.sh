#!/bin/bash
# Quick build and run script for Panic Plugin

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "🎭 Panic Plugin Build & Run Script"
echo "===================================="
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or later."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep 'version' | head -1)
echo "✅ Found Java: $JAVA_VERSION"
echo ""

# Try to build
echo "🔨 Building Panic Plugin..."
echo ""

# We'll need to use a gradle wrapper or system gradle
if command -v gradle &> /dev/null; then
    echo "Using system gradle..."
    gradle build -x test
elif [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    chmod +x ./gradlew
    ./gradlew build -x test
else
    echo "⚠️  Neither gradle nor ./gradlew found."
    echo ""
    echo "To build this plugin manually:"
    echo ""
    echo "1. Install Gradle 8.5+ from https://gradle.org/releases/"
    echo "2. Run: gradle build"
    echo ""
    echo "Or use Android Studio > Build > Build Bundle(s)/APK(s)"
    exit 1
fi

echo ""
echo "✅ Build complete!"
echo ""
echo "🎉 Plugin built successfully!"
echo ""
echo "To run in a sandbox IDE:"
echo "  gradle runIde"
echo ""
echo "To install in your IDE:"
echo "  1. File → Settings → Plugins"
echo "  2. Click ⚙️  icon → Install Plugin from Disk"
echo "  3. Select: build/distributions/panic-plugin-1.0.0.jar"
