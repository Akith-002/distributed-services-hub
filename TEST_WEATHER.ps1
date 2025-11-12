# Start Hub Server and API Gateway for weather test

Write-Host "=== PHASE 3: WEATHER FETCH TEST ===" -ForegroundColor Cyan
Write-Host ""

# Kill any existing Java processes
Write-Host "Cleaning up old processes..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start Hub Server
Write-Host "Starting Hub Server on port 7070/7071..." -ForegroundColor Green
$hubProcess = Start-Process -PassThru -FilePath "java" -ArgumentList `
    "-jar", "d:\Projects\network programming - assignment\services\hub-server\target\hub-server-1.0-SNAPSHOT.jar"

Write-Host "Waiting for Hub Server to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Start API Gateway Service
Write-Host "Starting API Gateway Service..." -ForegroundColor Green
$apiProcess = Start-Process -PassThru -FilePath "java" -ArgumentList `
    "-jar", "d:\Projects\network programming - assignment\services\api-gateway-service\target\api-gateway-service-1.0-SNAPSHOT.jar"

Write-Host "Waiting for API Gateway to register..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Display process IDs
Write-Host ""
Write-Host "Hub Server PID: $($hubProcess.Id)" -ForegroundColor Cyan
Write-Host "API Gateway PID: $($apiProcess.Id)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services are now running. Check Hub Server logs above for command routing." -ForegroundColor Yellow
Write-Host "When you click 'Fetch Weather' in the Dashboard, you should see:" -ForegroundColor Yellow
Write-Host "  [COMMAND_ROUTER] DEBUG - Looking for service: API_GATEWAY" -ForegroundColor White
Write-Host "  [COMMAND_ROUTER] DEBUG - Registered services: [ApiGateway]" -ForegroundColor White
Write-Host "  [COMMAND_ROUTER] DEBUG - Comparing 'ApiGateway' with 'API_GATEWAY' (case-insensitive): true" -ForegroundColor White
Write-Host "  [COMMAND_ROUTER] DEBUG - ✓ Found match!" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop both services" -ForegroundColor Yellow

# Keep processes alive
$hubProcess.WaitForExit()
