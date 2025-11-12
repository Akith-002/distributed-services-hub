# 🎯 PHASE 3 FINAL DEBUG RUN - Execute This Now

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  PHASE 3 - CRITICAL DEBUG TEST" -ForegroundColor Cyan
Write-Host "  String Comparison Mystery Solver" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Step 1: Kill all Java
Write-Host "Step 1: Killing any existing Java processes..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Write-Host "✓ Cleaned up" -ForegroundColor Green
Write-Host ""

# Step 2: Start Hub
Write-Host "Step 2: Starting Hub Server..." -ForegroundColor Yellow
$hubPath = "d:\Projects\network programming - assignment\services\hub-server\target\hub-server-1.0-SNAPSHOT.jar"
if (Test-Path $hubPath) {
    $hubProcess = Start-Process -PassThru -FilePath "java" -ArgumentList "-jar", $hubPath -NoNewWindow
    Write-Host "✓ Hub started (PID: $($hubProcess.Id))" -ForegroundColor Green
} else {
    Write-Host "✗ Hub JAR not found!" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 3
Write-Host ""

# Step 3: Start API Gateway
Write-Host "Step 3: Starting API Gateway Service..." -ForegroundColor Yellow
$apiPath = "d:\Projects\network programming - assignment\services\api-gateway-service\target\api-gateway-service-1.0-SNAPSHOT.jar"
if (Test-Path $apiPath) {
    $apiProcess = Start-Process -PassThru -FilePath "java" -ArgumentList "-jar", $apiPath -NoNewWindow
    Write-Host "✓ API Gateway started (PID: $($apiProcess.Id))" -ForegroundColor Green
} else {
    Write-Host "✗ API Gateway JAR not found!" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 3
Write-Host ""

# Step 4: Start Dashboard
Write-Host "Step 4: Starting React Dashboard..." -ForegroundColor Yellow
$frontendPath = "d:\Projects\network programming - assignment\frontend"
if (Test-Path $frontendPath) {
    Push-Location $frontendPath
    $devProcess = Start-Process -PassThru -FilePath "npm" -ArgumentList "run", "dev" -NoNewWindow
    Pop-Location
    Write-Host "✓ Dashboard started (PID: $($devProcess.Id))" -ForegroundColor Green
    Write-Host "   Access at: http://localhost:5173" -ForegroundColor Cyan
} else {
    Write-Host "✗ Frontend directory not found!" -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5
Write-Host ""

# Step 5: Instructions
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ALL SERVICES STARTED" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 WHAT TO DO NOW:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Open browser → http://localhost:5173" -ForegroundColor White
Write-Host "2. Enter username → 'TestUser'" -ForegroundColor White
Write-Host "3. Click 'Connect to Hub'" -ForegroundColor White
Write-Host "4. Navigate to 'API Gateway' tab" -ForegroundColor White
Write-Host "5. Click 'Fetch Weather' button" -ForegroundColor White
Write-Host ""
Write-Host "📊 WHAT TO WATCH:" -ForegroundColor Cyan
Write-Host ""
Write-Host "When you click 'Fetch Weather', look for this in Hub console:" -ForegroundColor White
Write-Host ""
Write-Host "  [HANDLER] DEBUG - incomingServiceName bytes: [...]" -ForegroundColor Gray
Write-Host "  [COMMAND_ROUTER] DEBUG - registeredName bytes: [...]" -ForegroundColor Gray
Write-Host "  [COMMAND_ROUTER] DEBUG - serviceName bytes: [...]" -ForegroundColor Gray
Write-Host "  [COMMAND_ROUTER] DEBUG - equalsIgnoreCase: ?" -ForegroundColor Gray
Write-Host "  [COMMAND_ROUTER] DEBUG - toUpperCase: ?" -ForegroundColor Gray
Write-Host "  [COMMAND_ROUTER] DEBUG - trim+equalsIgnoreCase: ?" -ForegroundColor Gray
Write-Host ""
Write-Host "✅ If ANY of those return true → Message will be routed! 🎉" -ForegroundColor Green
Write-Host "❌ If ALL return false → We have found the bug!" -ForegroundColor Red
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Process IDs:" -ForegroundColor Cyan
Write-Host "  Hub:        $($hubProcess.Id)" -ForegroundColor White
Write-Host "  API GW:     $($apiProcess.Id)" -ForegroundColor White
Write-Host "  Dashboard:  $($devProcess.Id)" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 TIP: Open 3 terminals side-by-side to see all logs at once!" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Enter when done testing..." -ForegroundColor Yellow
Read-Host

# Cleanup
Write-Host ""
Write-Host "Cleaning up..." -ForegroundColor Yellow
$hubProcess.Kill()
$apiProcess.Kill()
$devProcess.Kill()
Write-Host "✓ All processes terminated" -ForegroundColor Green

