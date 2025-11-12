# Run NIO Log Service
Write-Host "Starting NIO Log Service..." -ForegroundColor Cyan
Set-Location $PSScriptRoot

# Check if JAR exists
if (-not (Test-Path "target\nio-log-service-1.0-SNAPSHOT.jar")) {
    Write-Host "JAR not found. Building first..." -ForegroundColor Yellow
    .\build.ps1
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "    NIO LOG SERVICE                              " -ForegroundColor Green
Write-Host "    Port: 9091                                   " -ForegroundColor Green
Write-Host "    Core: Java NIO with Selector                 " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""

java -jar target\nio-log-service-1.0-SNAPSHOT.jar
