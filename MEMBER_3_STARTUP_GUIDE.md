# Member 3 - Secure File Service Startup & Demonstration Guide

**Your Component:** Secure File Service (JSSE/SSL Implementation)  
**Your Core Networking Concepts:** SSLServerSocket, SSLSocket, JSSE, KeyStore, TLS/SSL  
**Port:** 9090 (SSL/TLS only)

---

## 📚 Java Network Programming Concepts You're Demonstrating

### 1. **JSSE (Java Secure Socket Extension)** - Lesson 8
- SSL/TLS encryption for secure communication
- **SSLServerSocket** instead of regular ServerSocket
- **SSLSocket** for client connections
- Industry-standard encryption (TLS 1.2/1.3)

### 2. **KeyStore & Certificate Management**
- Self-signed certificates for development
- KeyStore file management
- Certificate-based authentication
- Public/Private key cryptography

### 3. **TLS Handshake Process**
- Client-server SSL handshake
- Certificate validation
- Cipher suite negotiation
- Secure channel establishment

### 4. **Security Enforcement**
- **Regular sockets REJECTED** - only SSL allowed
- Certificate validation on both sides
- Encrypted data transmission
- Secure file storage and retrieval

### 5. **File Operations Over Secure Channel**
- STORE - Upload files securely
- RETRIEVE - Download files securely
- LIST - Show stored files
- DELETE - Remove files

---

## 🚀 How to Start Your Service

### Step 1: Generate SSL KeyStore (First Time Only)

#### Navigate to Your Service Directory
```powershell
cd distributed-services-hub\secure-file-service
```

#### Run KeyStore Generation Script
```powershell
.\generate-keystore.ps1
```

**Expected Output:**
```
Generating self-signed certificate for Secure File Service...
Keystore created: keystore\fileservice.keystore
Certificate details:
  Owner: CN=SecureFileService, OU=NetworkProgramming, O=UOM, L=Colombo, ST=Western, C=LK
  Validity: 365 days
  Algorithm: RSA 2048-bit
  TLS: 1.2/1.3 compatible

✓ KeyStore generated successfully!
```

**What this creates:**
- **File:** `keystore/fileservice.keystore`
- **Password:** `changeit` (default)
- **Contains:** Self-signed certificate + private key

### Step 2: Build Your Service

```powershell
.\build.ps1
```
Or manually:
```powershell
mvn clean package
```

**Expected Output:**
```
[INFO] Building jar: target\secure-file-service-1.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

### Step 3: Start the Hub Server First

**IMPORTANT:** Hub must be running before your service starts!
```powershell
cd ..\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

### Step 4: Run Your Secure File Service

```powershell
cd ..\secure-file-service
java -jar target\secure-file-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
==============================================================================
  SECURE FILE SERVICE - MEMBER 3
==============================================================================

[SSLUtils] Loading KeyStore from: keystore/fileservice.keystore
[SSLUtils] KeyStore loaded successfully
[SSLUtils] Initializing SSLContext with TLS protocol...
[SSLUtils] SSLContext initialized successfully

[HubClient] Connecting to Hub at localhost:7070...
[HubClient] Connected to Hub successfully
[HubClient] Sent registration: REGISTER::SecureFileService::localhost::9090

[SSLFileServer] Creating SSLServerSocket on port 9090...
[SSLFileServer] ✓ SSL Server Socket created successfully
[SSLFileServer] Enabled protocols: [TLSv1.2, TLSv1.3]
[SSLFileServer] Cipher suites: 15 available

✓ SECURE FILE SERVICE STARTED SUCCESSFULLY

✓ Hub Registration: SUCCESS
✓ SSL Server: RUNNING on port 9090
✓ Heartbeat: ACTIVE (every 10 seconds)
✓ TLS Protocols: 1.2, 1.3 enabled
✓ Secure connections ONLY (regular sockets rejected)

Waiting for secure connections...
```

---

## 🎯 How to Demonstrate Your Work

### Method 1: Automated Security Test (Recommended First)

This is the **BEST way** to prove SSL enforcement!

#### Run the Security Test Script
```powershell
.\test-command.ps1
```

**What this does:**
1. **Test 1:** Tries to connect with REGULAR Socket (should FAIL)
2. **Test 2:** Connects with SSLSocket (should SUCCEED)

**Expected Output:**
```
================================================================================
  SECURE FILE SERVICE - SECURITY TEST
================================================================================

Test 1: Regular Socket (Should FAIL)
--------------------------------------
Attempting connection with regular Socket to localhost:9090...
❌ FAILED: Connection rejected (as expected)
   Error: Connection reset / SSL handshake failed
   ✓ This proves the server ONLY accepts SSL connections!

Test 2: SSL Socket (Should SUCCEED)
------------------------------------
Attempting connection with SSLSocket to localhost:9090...
✓ SUCCESS: SSL handshake completed
✓ TLS Protocol: TLSv1.3
✓ Cipher Suite: TLS_AES_256_GCM_SHA384
✓ Secure connection established

✓✓✓ SECURITY TEST PASSED ✓✓✓
Regular sockets rejected, SSL sockets accepted!
```

### Method 2: Interactive Client Testing

#### Start the SSL File Client
```powershell
# In a NEW terminal (keep service running)
cd distributed-services-hub\secure-file-service
java -cp target\secure-file-service-1.0-SNAPSHOT.jar com.example.fileservice.SSLFileClient
```

**You'll see:**
```
================================================================================
  SSL FILE CLIENT
================================================================================
Connecting to Secure File Service at localhost:9090...
✓ SSL handshake successful
✓ Protocol: TLSv1.3
✓ Cipher: TLS_AES_256_GCM_SHA384
✓ Secure connection established

Commands: STORE, RETRIEVE, LIST, DELETE, EXIT
SSL File Client>
```

#### Test Case 1: Store a File
```
SSL File Client> STORE test.txt
Enter file content (type 'END' on new line to finish):
This is a secret message!
It's encrypted over SSL/TLS.
Nobody can read this in transit.
END

✓ Success:
File stored: test.txt (95 bytes)
Encrypted transmission: YES
```

#### Test Case 2: List Files
```
SSL File Client> LIST

✓ Success:
Files in secure storage:
1. test.txt (95 bytes) - Modified: 2025-11-12 14:30:45
```

#### Test Case 3: Retrieve File
```
SSL File Client> RETRIEVE test.txt

✓ Success:
File contents:
--------------------------------------------------------------------------------
This is a secret message!
It's encrypted over SSL/TLS.
Nobody can read this in transit.
--------------------------------------------------------------------------------
```

#### Test Case 4: Delete File
```
SSL File Client> DELETE test.txt

✓ Success:
File deleted: test.txt
```

### Method 3: UI Demonstration (Tab 3: Secure File Service)

This is the **VISUAL** way to show your work!

#### Start the Dashboard
```powershell
cd ..\..\multi-client-chat-frontend
npm run dev
```

Open browser: **http://localhost:5173**

#### Navigate to Tab 3: Secure File Service

1. Click on **"Secure File Service"** tab (Tab 3)
2. You'll see a file management interface with:
   - Upload section
   - File list
   - Download/delete options

#### Demonstrate File Upload (Encrypted)

**Test Case 1: Upload a Text File**
1. Click "Choose File" button
2. Select a text file from your computer (e.g., `test.txt`)
3. Click "Upload File"
4. Watch the progress indicator
5. See success message: "File 'test.txt' uploaded successfully"
6. File appears in the list below

**What's Happening Behind the Scenes:**
- Dashboard sends file to API Gateway via WebSocket
- API Gateway forwards to Secure File Service
- **Secure File Service uses SSLSocket connection**
- File is transmitted over **encrypted TLS 1.3 channel**
- File stored securely on server
- Success response sent back through Hub

#### Demonstrate File List (Shows SSL is Working)

**After uploading files, you'll see:**
```
Files in Secure Storage:
------------------------
📄 test.txt (245 bytes)
📄 document.pdf (2.4 MB)
📄 config.json (1.2 KB)

[Download] [Delete] buttons for each file
```

**The file list proves:**
- SSL connection is active
- Server is responding over encrypted channel
- Multiple files stored securely

#### Demonstrate File Download (Encrypted)

**Test Case 2: Download a File**
1. Click "Download" button next to any file
2. File downloads to your browser
3. See success message: "File downloaded successfully"

**What's Happening:**
- Request sent to Secure File Service via API Gateway
- Server retrieves file over SSLSocket
- File transmitted back through encrypted channel
- Browser receives and saves file

#### Demonstrate SSL Security

**Show the Encryption in Action:**

1. Open browser DevTools (F12)
2. Go to Network tab
3. Upload a file
4. Show WebSocket messages are encrypted
5. Point out: "All communication uses SSL/TLS encryption"

**In your service logs, you'll see:**
```
[SSLFileServer] New SSL connection accepted
[SSLFileServer] TLS handshake completed: TLSv1.3
[SSLFileServer] Cipher: TLS_AES_256_GCM_SHA384
[SSLFileServer] File upload request: test.txt
[SSLFileServer] File stored: 245 bytes
[SSLFileServer] SSL session closed gracefully
```

---

## 🎓 Explaining Your Networking Concepts

### For Your Presentation/Demo, Explain:

#### 1. SSLServerSocket vs ServerSocket

**Code Reference in `SSLFileServer.java`:**
```java
// Regular ServerSocket (what we DON'T use)
// ServerSocket server = new ServerSocket(9090); // ❌ NOT SECURE

// SSLServerSocket (what we DO use)
SSLContext sslContext = SSLUtils.createSSLContext();
SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
SSLServerSocket sslServerSocket = (SSLServerSocket) factory.createServerSocket(9090);
```

**Explain:**
- "I use `SSLServerSocket` instead of regular `ServerSocket`"
- "SSLServerSocket automatically handles TLS handshake"
- "All data is encrypted with industry-standard algorithms"
- "Regular sockets are rejected - SSL-only connections"

#### 2. KeyStore & Certificate Management

**Code Reference in `SSLUtils.java`:**
```java
// Load KeyStore
KeyStore keyStore = KeyStore.getInstance("JKS");
FileInputStream fis = new FileInputStream("keystore/fileservice.keystore");
keyStore.load(fis, "changeit".toCharArray());

// Initialize KeyManager
KeyManagerFactory kmf = KeyManagerFactory.getInstance(
    KeyManagerFactory.getDefaultAlgorithm()
);
kmf.init(keyStore, "changeit".toCharArray());

// Create SSLContext
SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(kmf.getKeyManagers(), null, null);
```

**Explain:**
- "KeyStore contains the server's certificate and private key"
- "Certificate identifies the server to clients"
- "Private key is used to decrypt data encrypted with public key"
- "KeyManagerFactory manages the keys for SSL connections"
- "SSLContext is the main entry point for SSL/TLS"

#### 3. TLS Handshake Process

**Explain the handshake:**
```
1. Client connects to SSLServerSocket
2. Server sends its certificate to client
3. Client validates certificate
4. Client and server negotiate cipher suite
5. Client and server exchange keys
6. Secure channel established
7. All data encrypted/decrypted automatically
```

**In code:**
```java
SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
// Handshake happens automatically here!

String protocol = sslSocket.getSession().getProtocol(); // "TLSv1.3"
String cipher = sslSocket.getSession().getCipherSuite(); // "TLS_AES_256_GCM_SHA384"
```

#### 4. Security Enforcement

**Code Reference:**
```java
// Enable only TLS 1.2 and 1.3 (no older protocols)
sslServerSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

// When regular socket tries to connect:
// The TLS handshake FAILS because client doesn't send certificate
// Connection is rejected automatically
```

**Explain:**
- "If a regular Socket tries to connect, the TLS handshake fails"
- "The server automatically rejects non-SSL connections"
- "This proves security enforcement at the protocol level"

#### 5. Encrypted File Operations

**Explain:**
```java
// All data sent through SSLSocket is encrypted
PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
out.println("STORE::test.txt::content"); // Encrypted automatically

// All data received is decrypted
BufferedReader in = new BufferedReader(
    new InputStreamReader(sslSocket.getInputStream())
);
String response = in.readLine(); // Decrypted automatically
```

**Explain:**
- "File contents are encrypted during transmission"
- "Even if someone intercepts the network traffic, they can't read it"
- "SSLSocket handles encryption/decryption transparently"

---

## 📊 Demonstration Checklist

Use this checklist during your demonstration:

### Command Line Demo
- [ ] Generate KeyStore: `.\generate-keystore.ps1`
- [ ] Show KeyStore file created: `keystore\fileservice.keystore`
- [ ] Build service successfully
- [ ] Start Hub Server
- [ ] Start Secure File Service
- [ ] Show SSL initialization in logs
- [ ] Show enabled TLS protocols (1.2, 1.3)
- [ ] Optional: Run security test: `.\test-command.ps1`

### Interactive Client Demo
- [ ] Start SSL File Client
- [ ] Show successful SSL handshake
- [ ] Show TLS protocol version (1.3)
- [ ] Show cipher suite
- [ ] STORE a test file
- [ ] LIST stored files
- [ ] RETRIEVE the file
- [ ] DELETE the file
- [ ] Show all operations encrypted

### UI Demo (Tab 3: Secure File Service) - PRIMARY DEMO
- [ ] Open Dashboard at http://localhost:5173
- [ ] Navigate to Tab 3: "Secure File Service"
- [ ] Click "Choose File" and select a test file
- [ ] Click "Upload File"
- [ ] Show upload success message
- [ ] Show file appears in file list
- [ ] Upload 2-3 more files
- [ ] Show all files listed with sizes
- [ ] Click "Download" on a file
- [ ] Verify file downloads
- [ ] Optional: Delete a file
- [ ] Show file list updates in real-time
- [ ] Explain all transfers use SSL/TLS encryption

### Technical Explanation Points
- [ ] Explain SSLServerSocket vs ServerSocket
- [ ] Explain KeyStore purpose
- [ ] Explain certificate and private key
- [ ] Explain TLS handshake process
- [ ] Explain cipher suite negotiation
- [ ] Explain why regular sockets fail
- [ ] Show enabled protocols (1.2, 1.3)
- [ ] Show automatic encryption/decryption

---

## 🔧 Troubleshooting

### Issue: KeyStore not found
```powershell
# Regenerate KeyStore
.\generate-keystore.ps1
```

### Issue: SSL handshake failed
```
Error: javax.net.ssl.SSLHandshakeException
```
**Solution:**
- Check KeyStore password is correct ("changeit")
- Verify KeyStore file exists
- Make sure client is using SSLSocket, not Socket

### Issue: Port 9090 already in use
```powershell
# Find what's using the port
netstat -ano | findstr :9090

# Kill the process
taskkill /PID <PID> /F
```

### Issue: Certificate validation error
```
Error: sun.security.validator.ValidatorException
```
**Solution:**
- This is normal for self-signed certificates
- Client needs to trust the certificate
- In production, use CA-signed certificates

---

## 📝 Key Points for Your Report

Include these in your written documentation:

### Architecture
- Secure File Service uses SSLServerSocket for encrypted communication
- Only SSL/TLS connections accepted (regular sockets rejected)
- Self-signed certificate for development (would use CA cert in production)
- File operations (STORE, RETRIEVE, LIST, DELETE) all encrypted

### Networking Concepts

1. **SSLServerSocket & SSLSocket** (Lesson 8)
   - Encrypted server sockets
   - TLS 1.2/1.3 protocol support
   - Automatic encryption/decryption

2. **JSSE (Java Secure Socket Extension)**
   - SSLContext for SSL configuration
   - KeyStore for certificate management
   - KeyManagerFactory for key handling

3. **TLS/SSL Protocol**
   - Handshake process
   - Certificate validation
   - Cipher suite negotiation
   - Symmetric key exchange

4. **Security Enforcement**
   - Protocol-level security
   - Certificate-based authentication
   - Encrypted data transmission

### Why These Concepts Matter
- **Data Security:** Protect sensitive information in transit
- **Authentication:** Verify server identity with certificates
- **Industry Standard:** TLS is used by HTTPS, banking, etc.
- **Compliance:** Required for handling sensitive data

---

## 🎬 Presentation Script Example

**1. Introduction (30 seconds)**
"I implemented the Secure File Service using JSSE - Java Secure Socket Extension. It demonstrates SSL/TLS encryption with SSLServerSocket, KeyStore management, and security enforcement."

**2. Show Architecture (30 seconds)**
"Instead of regular ServerSocket, I use SSLServerSocket which automatically encrypts all data. The service stores and retrieves files, but all communication is encrypted using TLS 1.2 or 1.3."

**3. Code Walkthrough (1 minute)**
"Here's how I create SSLServerSocket: I load the KeyStore with our certificate and private key, initialize SSLContext, and create the server socket. Notice I enable only TLS 1.2 and 1.3 - no older, less secure protocols."

**4. Live Demo - Security Test (2 minutes)**
"Let me run the automated security test. Test 1 uses a regular Socket - watch it fail. The server rejects it because it's not SSL. Test 2 uses SSLSocket - it succeeds with TLS 1.3 handshake. This proves the server enforces SSL."

**5. Live Demo - File Operations (1 minute)**
"Now let me use the Dashboard to upload a file securely. Notice in the logs the SSL handshake completed with TLS 1.3. I'll upload a file... See it appears in the list. Now I'll download it... All encrypted via SSL. Let me upload another file to show multiple files work."

**6. Show Server Logs (30 seconds)**
"Look at the server logs - you can see the SSL handshake, TLS 1.3 protocol, 256-bit cipher, and file operations. Every connection is encrypted."

---

## 📚 Study References

Review these lessons before your demo:

- **Lesson 8:** JSSE, SSLServerSocket, SSLSocket, TLS/SSL
- **Lesson 3:** ServerSocket basics (for comparison)

### Key JSSE Classes to Know:
- `SSLServerSocket` - Secure server socket
- `SSLSocket` - Secure client socket
- `SSLContext` - SSL configuration
- `KeyStore` - Certificate storage
- `KeyManagerFactory` - Key management
- `SSLSession` - Connection session info

### Key Concepts:
- **TLS Handshake:** Multi-step process to establish secure connection
- **Certificate:** Digital ID proving server identity
- **Private Key:** Secret key for decryption
- **Cipher Suite:** Algorithms used for encryption
- **TLS 1.2/1.3:** Modern, secure protocol versions

---

## ✅ Pre-Demo Checklist

Before your demonstration:

- [ ] KeyStore generated: `keystore\fileservice.keystore`
- [ ] Service builds successfully (`.\build.ps1`)
- [ ] Hub Server is running
- [ ] Can start Secure File Service without errors
- [ ] Service registers with Hub successfully
- [ ] Port 9090 is available
- [ ] Security test script works: `.\test-command.ps1` (optional)
- [ ] SSL File Client works
- [ ] Can STORE, LIST, RETRIEVE, DELETE files via CLI
- [ ] Dashboard Tab 3 loads correctly
- [ ] Can upload files via UI
- [ ] File list displays uploaded files
- [ ] Can download files via UI
- [ ] Multiple file uploads work
- [ ] You understand TLS handshake process
- [ ] You can explain SSLServerSocket vs ServerSocket
- [ ] You can explain KeyStore purpose
- [ ] You can explain why regular sockets are rejected
- [ ] You practiced the demo at least once

---

## 🌟 Extra Credit Demonstrations

### Show Certificate Details
```powershell
# View certificate info
keytool -list -v -keystore keystore\fileservice.keystore -storepass changeit
```

### Show Network Traffic is Encrypted
```powershell
# Use Wireshark to capture traffic
# Show that data is NOT readable (encrypted)
# Compare with regular Socket traffic (readable plaintext)
```

### Explain Cipher Suites
```
TLS_AES_256_GCM_SHA384 means:
- Protocol: TLS
- Encryption: AES 256-bit
- Mode: GCM (Galois/Counter Mode)
- Hash: SHA-384
```

---

## 📊 What Makes Your Demo Stand Out

1. **Proof of Security:** Automated test shows regular sockets FAIL
2. **Visual Demonstration:** UI shows security test results
3. **Real Encryption:** TLS 1.3 with 256-bit AES
4. **Industry Standard:** Same technology used by HTTPS
5. **Both CLI and UI:** Show it working in two ways

---

**Good luck with your demonstration! Show them why SSL/TLS is essential for secure communication!**
