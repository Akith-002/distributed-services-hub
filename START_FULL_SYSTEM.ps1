# 🚀 START ENTIRE PROJECT - DISTRIBUTED SERVICES HUB
# This script starts all backend services and the React frontend

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  DISTRIBUTED SERVICES HUB - FULL SYSTEM STARTUP" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is available
$mavenPath = "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $mavenPath)) {
    Write-Host "❌ Maven not found at: $mavenPath" -ForegroundColor Red
    exit 1
}

Write-Host "📋 Starting Services in Order..." -ForegroundColor Yellow
Write-Host ""

# 1. Start Hub Server
Write-Host "1️⃣  Starting Hub Server (Ports 7070/7071)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\hub-server'; Write-Host '🌐 HUB SERVER' -ForegroundColor Cyan; java -jar target\hub-server-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# 2. Start API Gateway
Write-Host "2️⃣  Starting API Gateway (Port 9001)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\api-gateway-service'; Write-Host '🌉 API GATEWAY' -ForegroundColor Cyan; java -jar target\api-gateway-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 3. Start Secure File Service
Write-Host "3️⃣  Starting Secure File Service (Port 9090 - SSL)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\secure-file-service'; Write-Host '🔐 SECURE FILE SERVICE' -ForegroundColor Cyan; java -jar target\secure-file-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# 4. Start React Frontend
Write-Host "4️⃣  Starting React Frontend (Port 5173)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\multi-client-chat-frontend'; Write-Host '⚛️  REACT FRONTEND' -ForegroundColor Cyan; npm run dev"
Start-Sleep -Seconds 3

Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  ✅ ALL SERVICES STARTED!" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 SERVICES RUNNING:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  📡 Hub Server:          http://localhost:7071" -ForegroundColor White
Write-Host "  🌉 API Gateway:         http://localhost:9001" -ForegroundColor White
Write-Host "  🔐 Secure File Service: https://localhost:9090 (SSL)" -ForegroundColor White
Write-Host "  ⚛️  React Frontend:      http://localhost:5173" -ForegroundColor White
Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 CHECK STATUS:" -ForegroundColor Yellow
Write-Host "  Hub Status:    http://localhost:7071/hub-status" -ForegroundColor White
Write-Host "  Services List: http://localhost:7071/services" -ForegroundColor White
Write-Host ""
Write-Host "🎯 OPEN IN BROWSER:" -ForegroundColor Yellow
Write-Host "  Dashboard: http://localhost:5173" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  To stop all services, close each PowerShell window" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press any key to check service status..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Check services
Write-Host ""
Write-Host "Checking services..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

try {
    $response = Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing | ConvertFrom-Json
    Write-Host "✅ Hub Server is running!" -ForegroundColor Green
    Write-Host "   Total Services: $($response.totalServices)" -ForegroundColor White
    Write-Host "   Online Services: $($response.onlineServices)" -ForegroundColor White
} catch {
    Write-Host "⚠️  Could not connect to Hub Server" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 System is ready! Open http://localhost:5173 in your browser" -ForegroundColor Green
