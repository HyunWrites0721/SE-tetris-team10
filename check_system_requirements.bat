@echo off
echo ================================================
echo Tetris Game - System Requirements Check
echo ================================================
echo.

echo [System Information]
systeminfo | findstr /C:"Processor" /C:"Total Physical Memory" /C:"Available Physical Memory"
echo.

echo [Disk Space Check]
dir "%~dp0" | findstr "bytes free"
echo.

echo [Java Version]
java -version
echo.

echo ================================================
echo Instructions:
echo 1. Run the game (run.bat)
echo 2. Open Task Manager (Ctrl+Shift+Esc)
echo 3. Find "java.exe" or "javaw.exe" process
echo 4. Check CPU and Memory usage columns
echo.
echo Expected Requirements:
echo - CPU: 1.2GHz or higher
echo - RAM: Max 1GB usage
echo - Disk: 500MB+ free space
echo ================================================
pause
