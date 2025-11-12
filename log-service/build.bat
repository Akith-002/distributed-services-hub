@echo off
REM Build Script for Log Service (Batch)
REM Member 4 - High-Performance Log Service

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║        Building Log Service (Member 4)                         ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    exit /b 1
)

echo ✅ Maven detected
echo.

REM Clean and compile
echo 🔨 Compiling Java sources...
call mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build successful!
    echo.
    echo To run the Log Service:
    echo   run-log-service.bat
    echo.
) else (
    echo.
    echo ❌ Build failed!
    exit /b 1
)
