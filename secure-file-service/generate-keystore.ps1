# Generate SSL Keystore for Secure File Service
# Member 3 - JSSE Implementation

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "SSL Keystore Generation" -ForegroundColor Cyan
Write-Host "Member 3 - Secure File Service" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$keystorePath = "keystore\fileservice.keystore"
$keystorePassword = "password"
$keyPassword = "password"
$alias = "fileserver"
$dname = "CN=SecureFileServer, OU=NetworkProgramming, O=University, L=Colombo, ST=Western, C=LK"
$validity = 365

# Check if keystore already exists
if (Test-Path $keystorePath) {
    Write-Host "⚠️  Keystore already exists: $keystorePath" -ForegroundColor Yellow
    $response = Read-Host "Do you want to overwrite it? (yes/no)"
    
    if ($response -ne "yes") {
        Write-Host "Keystore generation cancelled." -ForegroundColor Yellow
        exit 0
    }
    
    Remove-Item $keystorePath
    Write-Host "✓ Existing keystore removed" -ForegroundColor Green
}

# Ensure keystore directory exists
if (-not (Test-Path "keystore")) {
    New-Item -ItemType Directory -Path "keystore" | Out-Null
    Write-Host "✓ Created keystore directory" -ForegroundColor Green
}

Write-Host ""
Write-Host "Generating self-signed certificate..." -ForegroundColor Yellow
Write-Host "  Alias: $alias" -ForegroundColor Gray
Write-Host "  DN: $dname" -ForegroundColor Gray
Write-Host "  Validity: $validity days" -ForegroundColor Gray
Write-Host ""

# Generate keystore with self-signed certificate
$keytoolCmd = "keytool"

try {
    & $keytoolCmd -genkeypair `
        -alias $alias `
        -keyalg RSA `
        -keysize 2048 `
        -keystore $keystorePath `
        -storepass $keystorePassword `
        -keypass $keyPassword `
        -validity $validity `
        -dname $dname `
        -storetype JKS
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "====================================================" -ForegroundColor Green
        Write-Host "✓ KEYSTORE GENERATED SUCCESSFULLY!" -ForegroundColor Green
        Write-Host "====================================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Keystore Details:" -ForegroundColor Cyan
        Write-Host "  Location: $keystorePath" -ForegroundColor White
        Write-Host "  Password: $keystorePassword" -ForegroundColor White
        Write-Host "  Alias: $alias" -ForegroundColor White
        Write-Host "  Algorithm: RSA 2048-bit" -ForegroundColor White
        Write-Host "  Validity: $validity days" -ForegroundColor White
        Write-Host ""
        
        # List keystore contents
        Write-Host "Keystore Contents:" -ForegroundColor Cyan
        & $keytoolCmd -list -v -keystore $keystorePath -storepass $keystorePassword
        
        Write-Host ""
        Write-Host "✓ SSL Certificate ready for use!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Next Step: Build and run the service" -ForegroundColor Cyan
        Write-Host "  .\build.ps1" -ForegroundColor Gray
        Write-Host ""
        
    } else {
        Write-Host ""
        Write-Host "✗ Failed to generate keystore" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure Java is installed and keytool is in PATH" -ForegroundColor Yellow
    exit 1
}
