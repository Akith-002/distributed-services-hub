# COMPLETE SYSTEM TEST SCRIPT
Write-Host "`n" -NoNewline
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  DISTRIBUTED SERVICES HUB - SYSTEM TEST" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$passCount = 0
$failCount = 0
$warnCount = 0

# TEST 1: Port Status
Write-Host "TEST 1: Checking All Ports..." -ForegroundColor Yellow
$ports = @(7070, 7071, 9001, 9090, 5173)
$portNames = @("Hub TCP", "Hub HTTP", "API Gateway", "Secure File", "Frontend")

for ($i = 0; $i -lt $ports.Count; $i++) {
    $port = $ports[$i]
    $name = $portNames[$i]
    $result = netstat -ano | findstr ":$port"
    
    if ($result -match "LISTENING") {
        Write-Host "  ✅ $name (Port $port) - LISTENING" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "  ❌ $name (Port $port) - NOT RUNNING" -ForegroundColor Red
        $failCount++
    }
}

# TEST 2: Hub Server Status
Write-Host "`nTEST 2: Hub Server Status..." -ForegroundColor Yellow
try {
    $hubStatus = Invoke-RestMethod -Uri "http://localhost:7071/hub-status" -ErrorAction Stop
    Write-Host "  ✅ Hub Server: $($hubStatus.status)" -ForegroundColor Green
    Write-Host "     Total Services: $($hubStatus.totalServices)" -ForegroundColor Gray
    Write-Host "     Online Services: $($hubStatus.onlineServices)" -ForegroundColor Gray
    $passCount++
} catch {
    Write-Host "  ❌ Hub Server: NOT ACCESSIBLE" -ForegroundColor Red
    $failCount++
}

# TEST 3: Service Registration
Write-Host "`nTEST 3: Service Registration..." -ForegroundColor Yellow
try {
    $servicesResponse = Invoke-RestMethod -Uri "http://localhost:7071/services" -ErrorAction Stop
    $services = $servicesResponse.services
    
    Write-Host "  Registered Services: $($services.Count)" -ForegroundColor Gray
    
    foreach ($service in $services) {
        Write-Host "  ✅ $($service.name) - $($service.status) (port $($service.port))" -ForegroundColor Green
        $passCount++
    }
    
    # Check for expected services
    $apiGateway = $services | Where-Object { $_.name -eq "ApiGateway" }
    $secureFile = $services | Where-Object { $_.name -eq "SecureFileService" }
    
    if (-not $apiGateway) {
        Write-Host "  ⚠️  ApiGateway NOT registered" -ForegroundColor Yellow
        $warnCount++
    }
    
    if (-not $secureFile) {
        Write-Host "  ⚠️  SecureFileService NOT registered" -ForegroundColor Yellow
        $warnCount++
    }
    
} catch {
    Write-Host "  ❌ Cannot query services" -ForegroundColor Red
    $failCount++
}

# TEST 4: Frontend
Write-Host "`nTEST 4: Frontend Accessibility..." -ForegroundColor Yellow
try {
    $frontendTest = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 5 -ErrorAction Stop
    if ($frontendTest.StatusCode -eq 200) {
        Write-Host "  ✅ Frontend accessible at http://localhost:5173" -ForegroundColor Green
        $passCount++
    }
} catch {
    Write-Host "  ❌ Frontend NOT accessible" -ForegroundColor Red
    Write-Host "     Error: $($_.Exception.Message)" -ForegroundColor Gray
    $failCount++
}

# SUMMARY
Write-Host "`n" -NoNewline
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  ✅ Passed: $passCount" -ForegroundColor Green
Write-Host "  ❌ Failed: $failCount" -ForegroundColor Red
Write-Host "  ⚠️  Warnings: $warnCount" -ForegroundColor Yellow
Write-Host ""

if ($failCount -eq 0 -and $warnCount -eq 0) {
    Write-Host "🎉 ALL SYSTEMS OPERATIONAL!" -ForegroundColor Green
} elseif ($failCount -eq 0) {
    Write-Host "⚠️  System mostly operational with warnings" -ForegroundColor Yellow
} else {
    Write-Host "❌ System has failures - check services" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Next Steps
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Open browser: http://localhost:5173" -ForegroundColor White
Write-Host "2. Press F12 to open Developer Console" -ForegroundColor White
Write-Host "3. Check Console tab for WebSocket connections" -ForegroundColor White
Write-Host "4. Verify services appear in dashboard" -ForegroundColor White
Write-Host ""
