@echo off
REM Comprehensive test script for Hub Server Phase 1
REM Usage: test-hub.bat

setlocal enabledelayedexpansion

echo.
echo ====================================================
echo Phase 1 - Hub Server Testing Suite
echo ====================================================
echo.

cd /d "%~dp0"

REM Check if hub-server folder exists
if not exist "services\hub-server" (
    echo ERROR: services\hub-server folder not found
    exit /b 1
)

cd services\hub-server

REM Check if pom.xml exists
if not exist pom.xml (
    echo ERROR: pom.xml not found
    exit /b 1
)

echo Step 1: Building Hub Server...
echo.
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    exit /b 1
)

echo.
echo ====================================================
echo BUILD SUCCESSFUL!
echo ====================================================
echo.

echo Testing Instructions:
echo.
echo 1. Start Hub Server (Terminal 1):
echo    java -jar target\hub-server-1.0-SNAPSHOT.jar
echo.
echo 2. Start Service 1 (Terminal 2):
echo    java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service1 9001
echo.
echo 3. Start Service 2 (Terminal 3):
echo    java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service2 9002
echo.
echo 4. Check Hub Status (Terminal 4):
echo    curl http://localhost:7070/hub-status
echo.
echo 5. Get Services List (Terminal 4):
echo    curl http://localhost:7070/services
echo.
echo Press any key to continue with automated testing...
pause > nul

echo.
echo ====================================================
echo Starting Hub Server (will run in background)
echo ====================================================
echo.

REM Start Hub Server in new window
start "Hub Server" cmd /k "java -jar target\hub-server-1.0-SNAPSHOT.jar"

REM Wait for Hub to start
timeout /t 2 /nobreak

echo.
echo ====================================================
echo Starting Mock Service 1
echo ====================================================
echo.

REM Start Service 1 in new window
start "Service 1" cmd /k "java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service1 9001"

timeout /t 2 /nobreak

echo.
echo ====================================================
echo Starting Mock Service 2
echo ====================================================
echo.

REM Start Service 2 in new window
start "Service 2" cmd /k "java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service2 9002"

timeout /t 3 /nobreak

echo.
echo ====================================================
echo Testing API Endpoints
echo ====================================================
echo.

echo Testing: curl http://localhost:7070/hub-status
echo.
powershell -Command "Invoke-RestMethod -Uri 'http://localhost:7070/hub-status' -Headers @{'Accept'='application/json'} | ConvertTo-Json"

echo.
echo.
echo Testing: curl http://localhost:7070/services
echo.
powershell -Command "Invoke-RestMethod -Uri 'http://localhost:7070/services' -Headers @{'Accept'='application/json'} | ConvertTo-Json"

echo.
echo ====================================================
echo Test Complete
echo ====================================================
echo.
echo Open windows:
echo - Hub Server
echo - Service 1
echo - Service 2
echo.
echo Observations to make:
echo 1. Services register in Hub console
echo 2. Services send heartbeats every 10 seconds
echo 3. API returns registered services
echo 4. Stop a service and wait 30 seconds - see timeout
echo.
echo Press any key to close test windows...
pause > nul

REM Close all spawned windows
taskkill /FI "WINDOWTITLE eq Hub Server" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Service 1" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Service 2" /T /F >nul 2>&1

echo.
echo Test complete. All windows closed.
echo.
