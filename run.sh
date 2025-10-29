#!/bin/bash

# Tetris Game Launcher for macOS/Linux
# Double-click to run or execute from terminal: ./run.sh

cd "$(dirname "$0")"

# Check if Java is installed
if ! command -v java &> /dev/null
then
    echo "❌ Java is not installed!"
    echo "Please install Java 17 or higher from:"
    echo "   https://adoptium.net/"
    read -p "Press Enter to exit..."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' | head -1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java version is too old: $JAVA_VERSION"
    echo "Please install Java 17 or higher from:"
    echo "   https://adoptium.net/"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "🎮 Starting Tetris Game..."
echo "Java version: $(java -version 2>&1 | head -1)"
echo ""

# Run the game
java -jar app.jar

# Wait for user input before closing (only if there was an error)
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Game exited with an error"
    read -p "Press Enter to exit..."
fi
