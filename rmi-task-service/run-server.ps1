# Run RMI Task Service Server
Write-Host "Starting RMI Task Service Server..." -ForegroundColor Cyan
Set-Location $PSScriptRoot

# Check if JAR exists
if (-not (Test-Path "target\rmi-task-service-1.0-SNAPSHOT.jar")) {
    Write-Host "JAR not found. Building first..." -ForegroundColor Yellow
    .\build.ps1
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "    RMI TASK SERVICE SERVER                      " -ForegroundColor Green
Write-Host "    Port: 1099 (RMI Registry)                    " -ForegroundColor Green
Write-Host "    Core: Java RMI                               " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""

java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
