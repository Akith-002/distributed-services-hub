# Run Script for Log Service (PowerShell)
# Member 4 - High-Performance Log Service

Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║        Starting Log Service (Member 4)                         ║" -ForegroundColor Green
Write-Host "║        Java NIO Selector-based Server                          ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

# Check if compiled
if (-Not (Test-Path "target\classes\com\example\logservice\LogServer.class")) {
    Write-Host "❌ Log Service not compiled!" -ForegroundColor Red
    Write-Host "Run .\build.ps1 first" -ForegroundColor Yellow
    exit 1
}

Write-Host "🚀 Launching Log Service on port 9091..." -ForegroundColor Cyan
Write-Host "📝 Log file: logs\system.log" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop`n" -ForegroundColor Yellow

# Run the log server
mvn exec:java -q
