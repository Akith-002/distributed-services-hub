# Build script for Secure File Service
# Member 3 - JSSE Implementation

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Secure File Service - Build Script" -ForegroundColor Cyan
Write-Host "Member 3 - JSSE (Java Secure Socket Extension)" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

# Check if pom.xml exists
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERROR: pom.xml not found in current directory" -ForegroundColor Red
    Write-Host "Please run this script from the secure-file-service directory" -ForegroundColor Red
    exit 1
}

Write-Host "Building Secure File Service..." -ForegroundColor Yellow
Write-Host ""

# Use the Maven path from the system
$mvnPath = "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd"

if (Test-Path $mvnPath) {
    & $mvnPath clean package
} else {
    # Try system maven
    mvn clean package
}

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "====================================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "====================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Generated JAR: target\secure-file-service-1.0-SNAPSHOT.jar" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Generate SSL keystore (if not done):" -ForegroundColor White
    Write-Host "     .\generate-keystore.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  2. Start Hub Server (in another terminal)" -ForegroundColor White
    Write-Host ""
    Write-Host "  3. Run the Secure File Service:" -ForegroundColor White
    Write-Host "     java -jar target\secure-file-service-1.0-SNAPSHOT.jar" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  4. Test with SSL client (in another terminal):" -ForegroundColor White
    Write-Host "     java -cp target\secure-file-service-1.0-SNAPSHOT.jar com.example.fileservice.SSLFileClient" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "====================================================" -ForegroundColor Red
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host "====================================================" -ForegroundColor Red
    Write-Host ""
    exit 1
}
