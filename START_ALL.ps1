# ============================================
#  Distributed Services Hub - Startup Script
# ============================================

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " Starting Distributed Services Hub" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Get the script directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path

# Start Hub Server in new window
Write-Host "[1/2] Starting Hub Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptPath\hub-server'; Write-Host 'Hub Server Starting...' -ForegroundColor Green; java -jar target\hub-server-1.0-SNAPSHOT.jar"

# Wait for Hub Server to start
Write-Host "      Waiting for Hub Server to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# Start API Gateway in new window
Write-Host "[2/2] Starting API Gateway Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptPath\api-gateway-service'; Write-Host 'API Gateway Starting...' -ForegroundColor Green; java -jar target\api-gateway-service-1.0-SNAPSHOT.jar"

# Wait a moment
Start-Sleep -Seconds 3

Write-Host "`n============================================" -ForegroundColor Green
Write-Host " Services Started!" -ForegroundColor Green
Write-Host "============================================`n" -ForegroundColor Green

Write-Host "  Hub Server:     http://localhost:7071" -ForegroundColor White
Write-Host "  API Gateway:    http://localhost:9001`n" -ForegroundColor White

Write-Host "  Checking status...`n" -ForegroundColor Gray

# Check status
try {
    $response = Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing -ErrorAction Stop
    $services = $response.Content | ConvertFrom-Json
    
    Write-Host "=== Registered Services ===" -ForegroundColor Green
    $services.services | Format-Table -AutoSize
    
    Write-Host "`n✅ All services are running successfully!`n" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Services are still starting up. Please wait a moment...`n" -ForegroundColor Yellow
    Write-Host "   You can manually check status with:" -ForegroundColor Gray
    Write-Host "   Invoke-WebRequest -Uri 'http://localhost:7071/services' -UseBasicParsing`n" -ForegroundColor Gray
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Available Endpoints:" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Hub Status:     http://localhost:7071/hub-status" -ForegroundColor White
Write-Host "  Services List:  http://localhost:7071/services" -ForegroundColor White
Write-Host "  Gateway Health: http://localhost:9001/health" -ForegroundColor White
Write-Host "  Gateway Status: http://localhost:9001/status" -ForegroundColor White
Write-Host "`n  WebSocket:" -ForegroundColor Cyan
Write-Host "  Hub Registry:   ws://localhost:7071/registry" -ForegroundColor White
Write-Host "  Gateway API:    ws://localhost:9001/api" -ForegroundColor White
Write-Host "`n============================================`n" -ForegroundColor Cyan

Write-Host "To stop services: Close the Hub Server and API Gateway windows" -ForegroundColor Yellow
Write-Host "`nPress any key to exit this window..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
