# Build Script for Log Service (PowerShell)
# Member 4 - High-Performance Log Service

Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        Building Log Service (Member 4)                         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Check if Maven is installed
$mavenVersion = & mvn -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven from https://maven.apache.org/" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Maven detected" -ForegroundColor Green
Write-Host ""

# Clean and compile
Write-Host "🔨 Compiling Java sources..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "To run the Log Service:" -ForegroundColor Cyan
    Write-Host "  .\run-log-service.ps1" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "`n❌ Build failed!" -ForegroundColor Red
    exit 1
}
