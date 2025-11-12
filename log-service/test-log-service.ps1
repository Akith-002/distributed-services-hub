# Test Script for Log Service
# Sends sample log messages to test the NIO server

Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║        Log Service Test Client                                 ║" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Magenta

# Check if Log Service is running
Write-Host "🔍 Checking if Log Service is running on port 9091..." -ForegroundColor Cyan

$testConnection = Test-NetConnection -ComputerName localhost -Port 9091 -WarningAction SilentlyContinue

if (-Not $testConnection.TcpTestSucceeded) {
    Write-Host "❌ Log Service is not running!" -ForegroundColor Red
    Write-Host "Start the Log Service first with: .\run-log-service.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Log Service is online!`n" -ForegroundColor Green

# Compile test client if needed
if (-Not (Test-Path "target\classes\com\example\logservice\TestLogClient.class")) {
    Write-Host "📦 Compiling test client..." -ForegroundColor Yellow
    mvn compile -q
}

Write-Host "📤 Sending test log messages...`n" -ForegroundColor Cyan

# Run the test client
mvn exec:java -Dexec.mainClass="com.example.logservice.TestLogClient" -q

Write-Host "`n✅ Test complete! Check the Log Service console for messages." -ForegroundColor Green
