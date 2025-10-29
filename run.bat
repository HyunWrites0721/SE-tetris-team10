@echo off
REM Tetris Game Launcher for Windows
REM Double-click to run

cd /d "%~dp0"

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java is not installed!
    echo Please install Java 17 or higher from:
    echo    https://adoptium.net/
    pause
    exit /b 1
)

echo 🎮 Starting Tetris Game...
java -version
echo.

REM Run the game
java -jar app.jar

REM Wait for user input if there was an error
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Game exited with an error
    pause
)
