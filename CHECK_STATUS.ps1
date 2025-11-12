# ============================================
#  Service Health Check Script
# ============================================

Write-Host "`n" -NoNewline
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Distributed Services Hub - Health Check" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "`n"

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Icon = "🔗"
    )
    
    Write-Host "Testing: $Icon $Name" -ForegroundColor Yellow
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✅ Status: " -NoNewline -ForegroundColor Green
            Write-Host "ONLINE (200 OK)" -ForegroundColor Green
            
            # Try to parse as JSON
            try {
                $json = $response.Content | ConvertFrom-Json
                Write-Host "  📄 Response:" -ForegroundColor Cyan
                $json | Format-List | Out-String | ForEach-Object { 
                    $_.Split("`n") | ForEach-Object { 
                        if ($_.Trim()) { Write-Host "     $_" -ForegroundColor White }
                    }
                }
            } catch {
                Write-Host "  📄 Response: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor White
            }
            return $true
        }
    } catch {
        Write-Host "  ❌ Status: " -NoNewline -ForegroundColor Red
        Write-Host "OFFLINE or ERROR" -ForegroundColor Red
        Write-Host "  ⚠️  Error: $($_.Exception.Message)" -ForegroundColor Yellow
        return $false
    }
    Write-Host ""
}

# Test Hub Server
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host " 🏢 HUB SERVER (Central Registry)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

$hubStatus = Test-Endpoint -Name "Hub Status" -Url "http://localhost:7071/hub-status" -Icon "📊"
Write-Host ""

$hubServices = Test-Endpoint -Name "Registered Services" -Url "http://localhost:7071/services" -Icon "📋"
Write-Host ""

# Test API Gateway
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host " 🌐 API GATEWAY SERVICE" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

$gatewayHealth = Test-Endpoint -Name "Gateway Health" -Url "http://localhost:9001/health" -Icon "💚"
Write-Host ""

$gatewayStatus = Test-Endpoint -Name "Gateway Status" -Url "http://localhost:9001/status" -Icon "📊"
Write-Host ""

# Summary
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host " 📊 SUMMARY" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

$allOnline = $hubStatus -and $hubServices -and $gatewayHealth -and $gatewayStatus

if ($allOnline) {
    Write-Host "  ✅ All services are ONLINE and responding!" -ForegroundColor Green
    Write-Host "  🎉 Your distributed services hub is working perfectly!`n" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Some services are not responding" -ForegroundColor Yellow
    Write-Host "  💡 Tip: Make sure both Hub Server and API Gateway are running`n" -ForegroundColor Yellow
    
    if (-not $hubStatus -or -not $hubServices) {
        Write-Host "  To start Hub Server:" -ForegroundColor Cyan
        Write-Host "     cd hub-server" -ForegroundColor White
        Write-Host "     java -jar target\hub-server-1.0-SNAPSHOT.jar`n" -ForegroundColor White
    }
    
    if (-not $gatewayHealth -or -not $gatewayStatus) {
        Write-Host "  To start API Gateway:" -ForegroundColor Cyan
        Write-Host "     cd api-gateway-service" -ForegroundColor White
        Write-Host "     java -jar target\api-gateway-service-1.0-SNAPSHOT.jar`n" -ForegroundColor White
    }
}

# Show running processes
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host " 🔍 RUNNING JAVA PROCESSES" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

$javaProcesses = Get-Process | Where-Object { $_.ProcessName -like "*java*" -or $_.MainWindowTitle -like "*Hub*" -or $_.MainWindowTitle -like "*Gateway*" }

if ($javaProcesses) {
    $javaProcesses | Select-Object Id, ProcessName, @{Name='Memory(MB)';Expression={[math]::Round($_.WS/1MB,2)}}, MainWindowTitle | Format-Table -AutoSize
} else {
    Write-Host "  ℹ️  No Java processes found" -ForegroundColor Yellow
    Write-Host "  This might mean services are not running`n" -ForegroundColor Yellow
}

# Show ports in use
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host " 🔌 PORT USAGE" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

Write-Host "  Checking ports 7070, 7071, 9001...`n" -ForegroundColor Gray

$portCheck = netstat -ano | Select-String -Pattern ":(7070|7071|9001)\s"

if ($portCheck) {
    Write-Host "  Active listeners:" -ForegroundColor Green
    $portCheck | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
} else {
    Write-Host "  ⚠️  No services listening on expected ports" -ForegroundColor Yellow
}

Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

# Quick access commands
Write-Host "💡 Quick Access Commands:" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  Check Hub status:" -ForegroundColor Yellow
Write-Host "    Invoke-WebRequest -Uri 'http://localhost:7071/hub-status' -UseBasicParsing`n" -ForegroundColor White

Write-Host "  List registered services:" -ForegroundColor Yellow
Write-Host "    Invoke-WebRequest -Uri 'http://localhost:7071/services' -UseBasicParsing`n" -ForegroundColor White

Write-Host "  Check Gateway status:" -ForegroundColor Yellow
Write-Host "    Invoke-WebRequest -Uri 'http://localhost:9001/status' -UseBasicParsing`n" -ForegroundColor White

Write-Host "  Re-run this health check:" -ForegroundColor Yellow
Write-Host "    .\CHECK_STATUS.ps1`n" -ForegroundColor White

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan
