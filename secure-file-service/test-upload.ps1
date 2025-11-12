# Test script to upload a file to the Secure File Service

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Testing File Upload to Secure File Service" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Test commands to send to the SSL client
$commands = @"
STORE test-file.txt
This is a test file uploaded via SSL/TLS
This file demonstrates the secure file transfer capability
Member 3 - Phase 3 Implementation
END
LIST
EXIT
"@

# Run the SSL File Client
$commands | java -cp "target/classes;target/lib/*" com.example.fileservice.SSLFileClient
