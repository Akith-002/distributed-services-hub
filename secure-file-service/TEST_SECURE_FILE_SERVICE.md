# 🔐 Secure File Service Testing Guide (Phase 3 - Member 3)

**Date:** November 11, 2025  
**Service:** Secure File Service with JSSE/SSL  
**Port:** 9090 (SSL/TLS)

---

## 📋 WHAT TO TEST

The Secure File Service (Member 3's contribution) implements:

1. ✅ **SSL/TLS Security** - JSSE (Java Secure Socket Extension)
2. ✅ **Secure File Upload** - Encrypted file transfers
3. ✅ **Secure File Download** - Encrypted file retrieval
4. ✅ **File Listing** - View uploaded files
5. ✅ **Hub Registration** - Service discovery
6. ✅ **Heartbeat Monitoring** - Keep-alive mechanism

---

## 🧪 TEST 1: Verify Service is Running

### **Check Port Listening:**
```powershell
Get-NetTCPConnection -LocalPort 9090 -ErrorAction SilentlyContinue | Select-Object LocalAddress, LocalPort, State
```

**Expected Output:**
```
LocalAddress LocalPort  State
------------ ---------  -----
::                9090 Listen
```

✅ **PASS** if you see `Listen` state on port 9090

---

## 🧪 TEST 2: Verify SSL/TLS Certificate

### **Check SSL Configuration:**
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\secure-file-service"
Get-Content src\main\java\com\example\fileservice\SecureFileService.java | Select-String -Pattern "SSLContext|KeyStore|TrustManager"
```

**Expected Output:**
Should show lines containing SSL setup code:
- `SSLContext`
- `KeyManagerFactory`
- `TrustManagerFactory`

✅ **PASS** if SSL context initialization code is present

---

## 🧪 TEST 3: Verify Hub Registration

### **Check Service Registration:**
```powershell
curl http://localhost:7071/services | ConvertFrom-Json | ConvertTo-Json
```

**Expected Output:**
```json
{
    "services": [
        {
            "name": "SecureFileService",
            "host": "localhost",
            "port": 9090,
            "status": "online",
            "registered": "HH:MM:SS"
        },
        {
            "name": "ApiGateway",
            "host": "localhost",
            "port": 9001,
            "status": "online",
            "registered": "HH:MM:SS"
        }
    ]
}
```

✅ **PASS** if `SecureFileService` appears in the list with `status: "online"`

---

## 🧪 TEST 4: Test Secure File Upload

### **Method 1: Using PowerShell (HTTPS)**

```powershell
# Create a test file
"Hello from Member 3 - Secure File Service Test!" > test-upload.txt

# Upload the file using SSL (Note: May need to skip certificate validation for self-signed cert)
$uri = "https://localhost:9090/upload"
$file = "test-upload.txt"

# For self-signed certificates, you may need to add certificate exception
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}

# Upload file
Invoke-RestMethod -Uri $uri -Method Post -InFile $file -ContentType "application/octet-stream"
```

**Expected Output:**
```json
{
    "status": "success",
    "filename": "test-upload.txt",
    "size": 52,
    "uploaded": "2025-11-11T16:30:00"
}
```

✅ **PASS** if file uploads successfully

---

### **Method 2: Using Java SSL Client**

Create a test client in `secure-file-service/` directory:

```java
// TestSecureClient.java
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;

public class TestSecureClient {
    public static void main(String[] args) throws Exception {
        // Trust all certificates (for testing only!)
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            }
        };
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 9090);
        
        System.out.println("✅ Connected to Secure File Service via SSL!");
        System.out.println("Cipher Suite: " + socket.getSession().getCipherSuite());
        System.out.println("Protocol: " + socket.getSession().getProtocol());
        
        socket.close();
    }
}
```

Compile and run:
```powershell
cd secure-file-service
javac TestSecureClient.java
java TestSecureClient
```

**Expected Output:**
```
✅ Connected to Secure File Service via SSL!
Cipher Suite: TLS_AES_256_GCM_SHA384
Protocol: TLSv1.3
```

✅ **PASS** if SSL connection succeeds and shows cipher suite

---

## 🧪 TEST 5: Test File Listing

### **List Uploaded Files:**
```powershell
# For self-signed certificates
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}

# List files
Invoke-RestMethod -Uri "https://localhost:9090/files" -Method Get
```

**Expected Output:**
```json
{
    "files": [
        {
            "name": "test-upload.txt",
            "size": 52,
            "modified": "2025-11-11T16:30:00"
        }
    ]
}
```

✅ **PASS** if files list is returned

---

## 🧪 TEST 6: Test Secure File Download

### **Download a File:**
```powershell
# Download file
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
Invoke-RestMethod -Uri "https://localhost:9090/download/test-upload.txt" -Method Get -OutFile "downloaded-file.txt"

# Verify content
Get-Content downloaded-file.txt
```

**Expected Output:**
```
Hello from Member 3 - Secure File Service Test!
```

✅ **PASS** if file downloads correctly and content matches

---

## 🧪 TEST 7: Verify Heartbeat to Hub

### **Monitor Heartbeat:**
Check the Secure File Service console/terminal window. You should see periodic heartbeat messages:

**Expected Output in Console:**
```
[HEARTBEAT] Sent heartbeat to Hub Server
[INFO] Service status: online
```

Every 30 seconds, the service sends a heartbeat to the Hub.

✅ **PASS** if heartbeat messages appear regularly

---

## 🧪 TEST 8: Test SSL Certificate Verification

### **Check Certificate Details:**
Look at the Secure File Service console when it starts. You should see:

**Expected Output:**
```
[SSL] Loading keystore from: security/keystore.jks
[SSL] Initializing SSL context with TLSv1.3
[SSL] Certificate loaded successfully
[SSL] Server certificate: CN=SecureFileService, O=DistributedServicesHub
```

✅ **PASS** if SSL initialization succeeds without errors

---

## 🧪 TEST 9: Test Frontend Integration

### **Open Dashboard:**
1. Start the React frontend:
   ```powershell
   cd multi-client-chat-frontend
   npm run dev
   ```

2. Open browser: `http://localhost:5173`

3. Navigate to **File Service Interface** section

4. Try to:
   - Upload a file
   - View file list
   - Download a file

**Expected Behavior:**
- ✅ File upload shows progress bar
- ✅ Files appear in the list
- ✅ Download triggers file download
- ✅ All operations use HTTPS

---

## 🧪 TEST 10: Verify Security Features

### **Test 1: SSL Encryption**
```powershell
# Try to connect without SSL (should fail)
curl http://localhost:9090/files
```

**Expected Output:**
```
Connection refused or SSL handshake error
```

✅ **PASS** if non-SSL connections are rejected

---

### **Test 2: Certificate Validation**
```powershell
# Try with strict certificate validation
Invoke-RestMethod -Uri "https://localhost:9090/files" -Method Get
```

**Expected Output:**
```
Error: The remote certificate is invalid according to the validation procedure.
```

This is expected for self-signed certificates!

✅ **PASS** - Shows certificate validation is working

---

## 📊 COMPLETE TEST CHECKLIST

Run all tests and mark them:

- [ ] **Test 1:** Service listening on port 9090
- [ ] **Test 2:** SSL/TLS code present in source
- [ ] **Test 3:** Service registered with Hub
- [ ] **Test 4:** File upload works (HTTPS)
- [ ] **Test 5:** File listing works
- [ ] **Test 6:** File download works
- [ ] **Test 7:** Heartbeat sending to Hub
- [ ] **Test 8:** SSL certificate loaded
- [ ] **Test 9:** Frontend integration works
- [ ] **Test 10:** Security enforcement (SSL only)

---

## 🔍 AUTOMATED TEST SCRIPT

Save this as `test-secure-file-service.ps1`:

```powershell
# Secure File Service Test Suite
Write-Host "`n🔐 SECURE FILE SERVICE TEST SUITE`n" -ForegroundColor Cyan

# Test 1: Check if service is running
Write-Host "Test 1: Checking if service is listening on port 9090..." -ForegroundColor Yellow
$port = Get-NetTCPConnection -LocalPort 9090 -ErrorAction SilentlyContinue
if ($port) {
    Write-Host "✅ PASS - Service is listening on port 9090" -ForegroundColor Green
} else {
    Write-Host "❌ FAIL - Service is NOT running" -ForegroundColor Red
}

# Test 2: Check Hub registration
Write-Host "`nTest 2: Checking Hub registration..." -ForegroundColor Yellow
try {
    $services = curl http://localhost:7071/services 2>$null | ConvertFrom-Json
    $secureService = $services.services | Where-Object { $_.name -eq "SecureFileService" }
    if ($secureService) {
        Write-Host "✅ PASS - Service registered with Hub" -ForegroundColor Green
        Write-Host "   Name: $($secureService.name)" -ForegroundColor Gray
        Write-Host "   Port: $($secureService.port)" -ForegroundColor Gray
        Write-Host "   Status: $($secureService.status)" -ForegroundColor Gray
    } else {
        Write-Host "❌ FAIL - Service NOT registered with Hub" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ FAIL - Could not connect to Hub" -ForegroundColor Red
}

# Test 3: Test SSL connection
Write-Host "`nTest 3: Testing SSL connection..." -ForegroundColor Yellow
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
try {
    $response = Invoke-RestMethod -Uri "https://localhost:9090/files" -Method Get -ErrorAction Stop
    Write-Host "✅ PASS - SSL connection successful" -ForegroundColor Green
} catch {
    Write-Host "❌ FAIL - SSL connection failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Test file upload
Write-Host "`nTest 4: Testing file upload..." -ForegroundColor Yellow
try {
    "Test file content - Member 3" | Out-File -FilePath "test-file.txt" -Encoding utf8
    $uploadUri = "https://localhost:9090/upload"
    # Note: Actual upload implementation depends on your API
    Write-Host "✅ Test file created: test-file.txt" -ForegroundColor Green
    Write-Host "   (Upload test requires API endpoint implementation)" -ForegroundColor Gray
} catch {
    Write-Host "❌ FAIL - File upload test failed" -ForegroundColor Red
}

# Summary
Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST SUITE COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
```

Run it:
```powershell
cd secure-file-service
.\test-secure-file-service.ps1
```

---

## 🎯 EXPECTED RESULTS SUMMARY

If everything is working correctly, you should see:

✅ **Service Status:**
- Port 9090 listening
- SSL/TLS enabled
- Registered with Hub Server

✅ **Security Features:**
- JSSE implementation active
- SSL context initialized
- Certificate loaded
- Only HTTPS connections accepted

✅ **File Operations:**
- Upload works (encrypted)
- Download works (encrypted)
- Listing works
- All data encrypted in transit

✅ **Integration:**
- Communicates with Hub Server
- Sends heartbeats every 30s
- Responds to service queries
- Frontend can interact with it

---

## 🐛 TROUBLESHOOTING

### Issue: Service not registered with Hub
**Solution:**
```powershell
# Check if Hub Server is running
curl http://localhost:7071/hub-status

# Restart Secure File Service
# Check console for registration messages
```

### Issue: SSL connection fails
**Solution:**
```powershell
# For testing, disable certificate validation:
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}

# Check if keystore exists:
Test-Path security\keystore.jks
```

### Issue: Port 9090 already in use
**Solution:**
```powershell
# Find what's using the port
Get-NetTCPConnection -LocalPort 9090 | Select-Object OwningProcess
# Kill the process or change port in SecureFileService.java
```

---

## 📝 MEMBER 3 VERIFICATION CHECKLIST

To verify Member 3's work is complete and functional:

- [ ] Code implements JSSE (Java Secure Socket Extension)
- [ ] SSL/TLS certificate configured
- [ ] Secure file upload endpoint
- [ ] Secure file download endpoint
- [ ] File listing endpoint
- [ ] Hub registration working
- [ ] Heartbeat mechanism active
- [ ] Error handling implemented
- [ ] Logging configured
- [ ] Frontend integration working
- [ ] Documentation complete
- [ ] Code pushed to GitHub

---

**Member 3's Secure File Service is ready for demonstration!** 🚀🔐
