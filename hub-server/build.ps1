# Build script for Hub Server
# Usage: .\build.ps1

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Hub Server - Build Script (PowerShell)" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

# Check if pom.xml exists
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERROR: pom.xml not found in current directory" -ForegroundColor Red
    Write-Host "Please run this script from the hub-server directory" -ForegroundColor Red
    exit 1
}

Write-Host "Building Hub Server..." -ForegroundColor Yellow
Write-Host ""

# Run Maven build
& mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "====================================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "====================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Generated JAR: target\hub-server-1.0-SNAPSHOT.jar" -ForegroundColor Green
    Write-Host ""
    Write-Host "To run the Hub Server:"
    Write-Host "  java -jar target\hub-server-1.0-SNAPSHOT.jar"
    Write-Host ""
    Write-Host "To run with mock services for testing:"
    Write-Host "  Terminal 1: java -jar target\hub-server-1.0-SNAPSHOT.jar"
    Write-Host "  Terminal 2: java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service1 9001"
    Write-Host "  Terminal 3: java -cp target\hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient Service2 9002"
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "====================================================" -ForegroundColor Red
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host "====================================================" -ForegroundColor Red
    Write-Host ""
    exit 1
}
