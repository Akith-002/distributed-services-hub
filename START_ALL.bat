@echo off
REM ============================================
REM  Distributed Services Hub - Startup Script
REM ============================================

echo.
echo ============================================
echo  Starting Distributed Services Hub
echo ============================================
echo.

REM Start Hub Server in new window
echo [1/2] Starting Hub Server...
start "Hub Server" powershell -NoExit -Command "cd '%~dp0hub-server'; java -jar target\hub-server-1.0-SNAPSHOT.jar"

REM Wait for Hub Server to start
echo      Waiting for Hub Server to initialize...
timeout /t 5 /nobreak >nul

REM Start API Gateway in new window
echo [2/2] Starting API Gateway Service...
start "API Gateway" powershell -NoExit -Command "cd '%~dp0api-gateway-service'; java -jar target\api-gateway-service-1.0-SNAPSHOT.jar"

REM Wait a moment
timeout /t 3 /nobreak >nul

echo.
echo ============================================
echo  Services Started!
echo ============================================
echo.
echo  Hub Server:     http://localhost:7071
echo  API Gateway:    http://localhost:9001
echo.
echo  Checking status...
echo.

REM Check status
powershell -Command "try { Invoke-WebRequest -Uri 'http://localhost:7071/services' -UseBasicParsing | ConvertFrom-Json | ConvertTo-Json } catch { Write-Host 'Services are still starting up...' -ForegroundColor Yellow }"

echo.
echo  Press any key to open the monitoring dashboard...
pause >nul

REM Show status
powershell -Command "Write-Host '`n=== Service Status ===' -ForegroundColor Green; Invoke-WebRequest -Uri 'http://localhost:7071/services' -UseBasicParsing | ConvertFrom-Json | Select-Object -ExpandProperty services | Format-Table -AutoSize"

echo.
echo  To stop services: Close the Hub Server and API Gateway windows
echo.
pause
