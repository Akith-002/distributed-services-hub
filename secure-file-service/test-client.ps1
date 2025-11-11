# Test SSL File Client
# This script runs the SSL client to test the Secure File Service

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SSL FILE CLIENT - PHASE 4 TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Run the SSL client
java -cp target\secure-file-service-1.0-SNAPSHOT.jar com.example.fileservice.SSLFileClient
