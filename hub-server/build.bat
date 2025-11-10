@echo off
REM Build script for Hub Server
REM Usage: build.bat

echo.
echo ====================================================
echo Hub Server - Build Script
echo ====================================================
echo.

cd /d "%~dp0"

if not exist pom.xml (
    echo ERROR: pom.xml not found in current directory
    echo Please run this script from the hub-server directory
    exit /b 1
)

echo Building Hub Server...
echo.

call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================
    echo BUILD SUCCESSFUL!
    echo ====================================================
    echo.
    echo Generated JAR: target\hub-server-1.0-SNAPSHOT.jar
    echo.
    echo To run the Hub Server:
    echo   java -jar target\hub-server-1.0-SNAPSHOT.jar
    echo.
    echo To run with mock services for testing:
    echo   Terminal 1: java -jar target\hub-server-1.0-SNAPSHOT.jar
    echo   Terminal 2: java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service1 9001
    echo   Terminal 3: java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service2 9002
    echo.
) else (
    echo.
    echo ====================================================
    echo BUILD FAILED!
    echo ====================================================
    echo.
    exit /b 1
)
