# Quick Start Guide - Phase 4 UI Integration Testing

## Overview

This guide shows you how to test the new Phase 4 Secure File Service UI integration with the complete distributed system.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Dashboard                          │
│                  (http://localhost:5173)                   │
│                  ✅ UI INTEGRATION COMPLETE                │
│                  • File Upload Interface                    │
│                  • File Download Interface                  │
│                  • File Listing                             │
│                  • Security Indicators                      │
└────────────────┬──────────────────────────────────────────┘
                 │ WebSocket (ws://localhost:9001/api)
┌────────────────▼──────────────────────────────────────────┐
│          API Gateway Service (Port 9001)                   │
│            🆕 NEW FILE SERVICE COMMANDS                    │
│          • uploadFile → Secure File Service                │
│          • listFiles → Secure File Service                │
│          • downloadFile → Secure File Service              │
└────────────────┬──────────────────────────────────────────┘
                 │ TCP (Hub Registry)         │ SSL (File Service)
         ┌───────▼────────────┐      ┌──────▼──────────┐
         │  Hub Server        │      │  Secure File    │
         │  (Port 7070)       │      │  Service        │
         │  Service Registry  │      │  (Port 9090)    │
         │                    │      │  🔒 SSL/TLS     │
         └────────────────────┘      └─────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.9.9
- All services compiled and built
- Ports 5173, 7070, 9001, 9090 available

## Step-by-Step Startup

### Step 1: Start Hub Server (Terminal 1)

```powershell
cd "d:\Projects\network programming - assignment\services\hub-server"
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

Expected output:

```
==============================================================================
  HUB SERVER - CORE REGISTRY SERVICE
==============================================================================

[HUB] Starting Service Registry TCP Server on port 7070...
[HUB] ✓ Service Registry listening on port 7070
[HUB] Starting WebSocket Broadcaster...
[HUB] ✓ WebSocket broadcaster ready on port 7071
[HUB] Heartbeat monitor started...

✓ HUB SERVER STARTED SUCCESSFULLY
```

**Status**: ✅ Ready

- TCP Service Registry: `localhost:7070`
- WebSocket Broadcaster: `ws://localhost:7071`

---

### Step 2: Start Secure File Service (Terminal 2)

```powershell
cd "d:\Projects\network programming - assignment\services\secure-file-service"
java -jar target/secure-file-service-1.0-SNAPSHOT.jar
```

Expected output:

```
==============================================================================
  SECURE FILE SERVICE - MEMBER 3
==============================================================================

[HubClient] Connecting to Hub Server at localhost:7070...
[HubClient] ✓ Service registered with Hub
[HubClient] Starting heartbeat thread (interval: 10 seconds)...
[SSLFileServer] Creating SSL server socket on port 9090...
[SSL] SSLContext created successfully
[SSL] Protocol: TLSv1.2
[SSLFileServer] ✓ SSL Server Socket created on port 9090
✓ SECURE FILE SERVICE STARTED SUCCESSFULLY

✓ Hub Registration: SUCCESS
✓ SSL Server: RUNNING on port 9090
✓ Heartbeat: ACTIVE (every 10 seconds)
```

**Status**: ✅ Ready

- SSL File Server: `localhost:9090` (🔒 Encrypted)
- Registered with Hub: `SecureFileService`

---

### Step 3: Start API Gateway Service (Terminal 3)

```powershell
cd "d:\Projects\network programming - assignment\services\api-gateway-service"
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar
```

Expected output:

```
==============================================================================
  API GATEWAY SERVICE - PHASE 2, MEMBER 2
==============================================================================

[STARTUP] Step 1: Initializing External API Client...
[STARTUP] ✓ External API Client initialized

[STARTUP] Step 2: Connecting to Hub Server...
[HubClient] ✓ Service registered with Hub

[STARTUP] Step 3: Starting WebSocket Server...
[WebSocketServer] ✓ WebSocket server started successfully
[WebSocketServer] Dashboard WebSocket: ws://localhost:9001/api

==============================================================================
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
==============================================================================

✓ Hub Registration: SUCCESS
✓ WebSocket Server: RUNNING on port 9001
✓ External API Client: READY

Features Available:
  ✓ fetchWeather: Get weather data
  ✓ listFiles: 🆕 List secure files
  ✓ uploadFile: 🆕 Upload secure files
  ✓ downloadFile: 🆕 Download secure files
```

**Status**: ✅ Ready

- WebSocket API: `ws://localhost:9001/api`
- New File Commands: `uploadFile`, `listFiles`, `downloadFile`

---

### Step 4: Open React Dashboard (Browser)

Navigate to:

```
http://localhost:5173
```

Expected behavior:

- Dashboard loads with login screen
- Shows "Distributed Services Hub" header
- Ready to connect to Hub

---

## Testing the File Service UI

### Test Flow:

#### 1. Connect to Hub Server

```
Username: admin
SSL: Off (for demo)
```

Click **"Join"** button

Expected:

- ✅ Status shows "Connected"
- ✅ Service list loads automatically
- ✅ Shows 2 services: `SecureFileService`, `ApiGateway`
- ✅ Green indicator for online status

---

#### 2. Select Secure File Service

Click on **"SecureFileService"** in the left sidebar

Expected:

- ✅ Service details panel opens
- ✅ Shows "Online" status
- ✅ Displays connection info: `localhost:9090`
- ✅ Shows security features section (🔒 SSL/TLS)
- ✅ **NEW**: File Service Operations interface appears

---

#### 3. Upload a File

In the File Service Operations panel:

1. Click **"Choose File"** button
2. Select a text file from your computer
3. File appears in the upload area with name and size
4. Click **"Upload File"** button

Expected:

- ✅ Upload progress spinner appears
- ✅ Success message: "✅ [filename] uploaded successfully"
- ✅ File automatically appears in "Stored Files" list
- ✅ No errors in console

---

#### 4. View Stored Files

Click **"Refresh"** button in Stored Files section

Expected:

- ✅ All uploaded files listed with filenames
- ✅ Each file has a "Download" button
- ✅ File count updated
- ✅ Shows uploaded file in the list

---

#### 5. Download a File

In the Stored Files list:

1. Click **"Download"** button next to a file
2. Browser download dialog appears

Expected:

- ✅ File downloads to your Downloads folder
- ✅ Downloaded file content matches uploaded file
- ✅ Success message: "✅ [filename] downloaded successfully"
- ✅ No errors

---

## Verification Checklist

### UI Components ✅

- [ ] FileServiceInterface component renders
- [ ] File upload interface displays
- [ ] File listing displays
- [ ] Download buttons appear
- [ ] Security indicators show

### File Operations ✅

- [ ] Can select and upload files
- [ ] File list updates after upload
- [ ] Can download uploaded files
- [ ] Downloaded file content is correct
- [ ] Error messages display for invalid operations

### Networking ✅

- [ ] WebSocket connects to API Gateway (port 9001)
- [ ] API Gateway proxies requests to Secure File Service (port 9090)
- [ ] SSL connections work for file service
- [ ] All commands execute without timeout

### Security ✅

- [ ] Security badges display on service details
- [ ] SSL/TLS encryption indicators present
- [ ] Secure connection messages shown
- [ ] File operations over encrypted connections

---

## Troubleshooting

### Issue: Dashboard won't connect to Hub

**Solution:**

```powershell
# Check Hub is running
netstat -ano | findstr ":7070"

# Verify port is listening
# Should see a listening socket on 7070
```

### Issue: SecureFileService not appearing in service list

**Solution:**

```powershell
# Check service is registered
# Look for registration message in Hub output:
# [HEARTBEAT] ♥ Acknowledged: SecureFileService

# Restart service:
# 1. Stop Secure File Service
# 2. Restart it
# 3. Refresh dashboard
```

### Issue: File upload fails with error

**Solution:**

```
Check:
1. API Gateway service is running (port 9001)
2. Secure File Service is running (port 9090)
3. Both are registered with Hub
4. WebSocket connection is active
```

### Issue: Downloaded file is empty or corrupted

**Solution:**

```
This is a known limitation with text handling.
Currently only text files transfer perfectly.
Binary files need additional encoding (Base64/hex).
```

---

## Service Port Map

| Service             | Port | Protocol       | Status                   |
| ------------------- | ---- | -------------- | ------------------------ |
| Hub Server          | 7070 | TCP            | ✅ Active                |
| Hub WebSocket       | 7071 | WebSocket      | ✅ Active                |
| API Gateway         | 9001 | HTTP/WebSocket | ✅ Active                |
| Secure File Service | 9090 | SSL/TLS        | ✅ Active (🔒 Encrypted) |
| React Dashboard     | 5173 | HTTP           | ✅ Active                |

---

## Next Steps

### After Testing

1. Document any issues found
2. Test with different file types
3. Verify error handling works
4. Test service recovery scenarios

### Future Enhancements

- Binary file support (Base64 encoding)
- File size limits
- File deletion via UI
- Bulk operations
- File search/filter

---

## Demo Script

For demonstrating the system to others:

### 1. Start All Services

- Show Hub starting with service registry
- Show Secure File Service with SSL/TLS setup
- Show API Gateway with new file commands

### 2. Open Dashboard

- Show connection to Hub
- Highlight service list updating in real-time
- Select SecureFileService

### 3. Demonstrate File Operations

- Upload a sample file
- Show file appearing in list
- Download the file
- Open it to verify content

### 4. Highlight Security

- Show SSL/TLS indicators
- Explain certificate-based encryption
- Show secure connection message

### 5. Explain Architecture

- Draw/show WebSocket flow from UI to API Gateway
- Show SSL connection to File Service
- Explain Hub's role in service discovery

---

## Support

### Documentation

- `PHASE_4_COMPLETE.md` - Implementation details
- `PHASE_4_UI_INTEGRATION.md` - UI integration specifics
- `README.md` (in each service folder) - Service-specific docs

### Logs

Check service startup logs for:

- `✓` symbols for successful startup
- `✗` symbols for errors
- Connection and registration messages

---

**Version:** Phase 4 UI Integration v1.0  
**Date:** November 11, 2025  
**Status:** ✅ Ready for Testing
