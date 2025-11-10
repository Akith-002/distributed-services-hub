# Secure File Service - Member 3

**Network Programming Group Assignment**  
**Core Concept:** JSSE (Java Secure Socket Extension) - SSL/TLS Security

---

## 📋 Overview

The **Secure File Service** is a microservice that provides secure file storage and retrieval using SSL/TLS encryption. It demonstrates the use of `SSLServerSocket` and `SSLSocket` with self-signed certificates.

### Key Features

- ✅ **SSLServerSocket** - Secure server using JSSE
- ✅ **Self-signed certificates** - KeyStore management
- ✅ **TLS 1.2/1.3 encryption** - Industry-standard security
- ✅ **File operations** - STORE, RETRIEVE, LIST, DELETE
- ✅ **Hub registration** - Automatic service discovery
- ✅ **Heartbeat monitoring** - Health tracking
- ✅ **Regular sockets rejected** - SSL-only connections

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│   Secure File Service (Port 9090)      │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐  │
│  │   SSLFileServer                  │  │
│  │   (SSLServerSocket on :9090)     │  │
│  └─────────────┬────────────────────┘  │
│                │                        │
│  ┌─────────────▼────────────────────┐  │
│  │   FileServiceHandler             │  │
│  │   (Processes file commands)      │  │
│  │   - STORE, RETRIEVE, LIST, DELETE│  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │   HubClient                      │  │
│  │   (TCP to Hub :7070)             │  │
│  │   - Register, Heartbeat          │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │   SSLUtils                       │  │
│  │   (KeyStore, SSLContext)         │  │
│  └──────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Hub Server running on port 7070

### Step 1: Generate SSL Keystore

```powershell
cd secure-file-service
.\generate-keystore.ps1
```

This creates `keystore/fileservice.keystore` with a self-signed certificate.

### Step 2: Build the Service

```powershell
.\build.ps1
```

Or manually:
```powershell
mvn clean package
```

### Step 3: Run the Service

```powershell
java -jar target\secure-file-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
==============================================================================
  SECURE FILE SERVICE - MEMBER 3
==============================================================================

[HubClient] Connected to Hub successfully
[SSLFileServer] ✓ SSL Server Socket created on port 9090
✓ SECURE FILE SERVICE STARTED SUCCESSFULLY

✓ Hub Registration: SUCCESS
✓ SSL Server: RUNNING on port 9090
✓ Heartbeat: ACTIVE (every 10 seconds)
```

---

## 🧪 Testing

### Test with SSL Client

```powershell
# In a new terminal
java -cp target\secure-file-service-1.0-SNAPSHOT.jar com.example.fileservice.SSLFileClient
```

### Example Commands

```
SSL File Client> STORE test.txt
Enter file content (type 'END' on new line to finish):
Hello, this is a secure file!
This content is encrypted over SSL/TLS.
END

✓ Success:
File stored: test.txt (68 bytes)

SSL File Client> LIST

✓ Success:
test.txt (68 bytes)

SSL File Client> RETRIEVE test.txt

✓ Success:
Hello, this is a secure file!
This content is encrypted over SSL/TLS.

SSL File Client> DELETE test.txt

✓ Success:
File deleted: test.txt

SSL File Client> EXIT
```

---

## 📡 File Protocol

### Commands

| Command | Format | Description |
|---------|--------|-------------|
| STORE | `STORE::<filename>::<size>` | Store a file |
| RETRIEVE | `RETRIEVE::<filename>` | Retrieve a file |
| LIST | `LIST` | List all files |
| DELETE | `DELETE::<filename>` | Delete a file |
| EXIT | `EXIT` | Close connection |

### Response Format

```
SUCCESS::<result data>
ERROR::<error message>
```

---

## 🔐 Security Features

### SSL/TLS Encryption

- **Protocol**: TLS 1.2 / TLS 1.3
- **Algorithm**: RSA 2048-bit
- **Certificate**: Self-signed (for demo)
- **Keystore**: JKS format

### Security Demonstration

**Test 1: Regular Socket (Fails)**
```java
Socket socket = new Socket("localhost", 9090); // ❌ Connection rejected
```

**Test 2: SSL Socket (Succeeds)**
```java
SSLSocket socket = (SSLSocket) SSLUtils.getClientSocketFactory()
    .createSocket("localhost", 9090); // ✅ Connection accepted
```

---

## 📂 Project Structure

```
secure-file-service/
├── pom.xml                           # Maven configuration
├── build.ps1                         # Build script
├── generate-keystore.ps1             # Keystore generation script
├── README.md                         # This file
│
├── keystore/                         # SSL certificates
│   └── fileservice.keystore          # Self-signed certificate
│
├── files/                            # File storage directory
│
└── src/main/java/com/example/fileservice/
    ├── SecureFileService.java        # Main entry point
    ├── SSLFileServer.java            # SSL server (SSLServerSocket)
    ├── FileServiceHandler.java       # File operations handler
    ├── HubClient.java                # Hub registration client
    ├── SSLFileClient.java            # Test client
    └── security/
        └── SSLUtils.java             # SSL utilities
```

---

## 🔧 Configuration

### Keystore Configuration

Located in `SSLUtils.java`:

```java
private static final String KEYSTORE_PATH = "keystore/fileservice.keystore";
private static final String KEYSTORE_PASSWORD = "password";
private static final String KEY_PASSWORD = "password";
private static final String PROTOCOL = "TLSv1.2";
```

### Service Configuration

Located in `SSLFileServer.java` and `HubClient.java`:

```java
// SSL Server Port
private static final int PORT = 9090;

// Hub Connection
private static final String HUB_HOST = "localhost";
private static final int HUB_PORT = 7070;
```

---

## 🎯 Learning Objectives (Member 3)

### 1. JSSE (Java Secure Socket Extension)
- ✅ Creating `SSLContext` from KeyStore
- ✅ Using `SSLServerSocket` instead of `ServerSocket`
- ✅ Handling `SSLSocket` connections
- ✅ Configuring TLS protocols and cipher suites

### 2. Certificate Management
- ✅ Generating self-signed certificates with `keytool`
- ✅ Loading KeyStore in Java
- ✅ KeyManagerFactory configuration
- ✅ TrustManager setup (for clients)

### 3. Secure Network Programming
- ✅ SSL/TLS handshake process
- ✅ Encrypted data transmission
- ✅ Rejecting non-SSL connections
- ✅ SSL session information

### 4. Service Integration
- ✅ Hub registration protocol
- ✅ Heartbeat mechanism
- ✅ Service discovery
- ✅ Graceful shutdown

---

## 📊 Demonstration Points

### For Presentation/Demo

1. **Show SSL Setup**
   - Display keystore generation
   - Show certificate details
   - Explain KeyStore loading

2. **Show Regular Socket Rejection**
   - Try connecting with regular `Socket` → Fails
   - Show SSL handshake failure

3. **Show SSL Socket Success**
   - Connect with `SSLSocket` → Succeeds
   - Display SSL session information
   - Show encrypted connection

4. **Show File Operations**
   - Store files securely
   - Retrieve files
   - List and delete files

5. **Show Hub Integration**
   - Service appears on dashboard
   - Heartbeat updates
   - Automatic registration/deregistration

---

## ❗ Troubleshooting

### "Keystore not found"

```powershell
# Generate keystore first
.\generate-keystore.ps1
```

### "Connection refused on port 9090"

```bash
# Check if service is running
netstat -ano | findstr :9090

# Make sure Hub Server is running first
# Then start this service
```

### "Hub connection failed"

```
# Make sure Hub Server is running on port 7070
cd ..\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

### "SSL handshake failed"

```
# Regenerate keystore
Remove-Item keystore\fileservice.keystore
.\generate-keystore.ps1
```

---

## 🔗 Integration with Other Services

### Hub Server (Member 1)
- Registers as "SecureFileService"
- Sends heartbeat every 10 seconds
- Appears on service registry

### API Gateway (Member 2)
- Can be called for file operations
- Dashboard can display file service status

### Log Service (Member 4)
- Logs file operations
- Logs SSL connection events

### Task Service (Member 5)
- Can trigger file operations via RMI

---

## 📚 Resources

- [Java SSL/TLS Documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html)
- [keytool Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html)
- [SSLSocket Javadoc](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLSocket.html)

---

## ✅ Acceptance Criteria

- [x] Uses SSLServerSocket (NOT regular ServerSocket)
- [x] Self-signed certificate and KeyStore properly configured
- [x] Regular Socket client fails to connect
- [x] SSLSocket client can store and retrieve files securely
- [x] Service appears on Hub dashboard
- [x] Heartbeat mechanism working
- [x] File operations (STORE, RETRIEVE, LIST, DELETE) functional
- [x] Graceful shutdown with Hub deregistration

---

**Member 3 - JSSE Implementation Complete!** 🔐

For questions or issues, refer to the main project documentation.
