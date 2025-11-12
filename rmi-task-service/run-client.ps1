# Run RMI Task Client
Write-Host "Starting RMI Task Client..." -ForegroundColor Cyan
Set-Location $PSScriptRoot

# Check if JAR exists
if (-not (Test-Path "target\rmi-task-service-1.0-SNAPSHOT.jar")) {
    Write-Host "JAR not found. Building first..." -ForegroundColor Yellow
    .\build.ps1
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "    RMI TASK CLIENT                              " -ForegroundColor Green
Write-Host "    Connecting to: localhost:1099                " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""

java -cp target\rmi-task-service-1.0-SNAPSHOT.jar com.example.taskservice.client.TaskClient
