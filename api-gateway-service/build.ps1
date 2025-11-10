# Build script for API Gateway Service (PowerShell)
# Usage: ./build.ps1

Write-Host ""
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  Building API Gateway Service" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ""

# Check if pom.xml exists
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERROR: pom.xml not found. Make sure you run this from the service directory." -ForegroundColor Red
    exit 1
}

# Clean
Write-Host "[BUILD] Cleaning previous build..." -ForegroundColor Yellow
mvn clean

Write-Host ""
Write-Host "[BUILD] Building with Maven..." -ForegroundColor Yellow
mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "================================================================================" -ForegroundColor Green
Write-Host "  BUILD SUCCESSFUL" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "JAR file created: target\api-gateway-service-1.0-SNAPSHOT.jar" -ForegroundColor Green
Write-Host ""
Write-Host "To run the service:" -ForegroundColor Cyan
Write-Host "  java -jar target\api-gateway-service-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
Write-Host "Make sure Hub Server is running first:" -ForegroundColor Cyan
Write-Host "  cd ..\hub-server" -ForegroundColor White
Write-Host "  java -jar target\hub-server-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
