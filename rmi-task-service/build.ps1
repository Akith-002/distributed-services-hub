# Build RMI Task Service
Write-Host "Building RMI Task Service..." -ForegroundColor Cyan
Set-Location $PSScriptRoot
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host "JAR location: target\rmi-task-service-1.0-SNAPSHOT.jar" -ForegroundColor Yellow
} else {
    Write-Host "Build failed!" -ForegroundColor Red
}
