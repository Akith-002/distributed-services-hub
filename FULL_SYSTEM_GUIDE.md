# 🚀 COMPLETE SYSTEM STARTUP GUIDE

**Distributed Services Hub - Full Stack Application**  
**Date:** November 11, 2025

---

## 📋 SYSTEM ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────┐
│              REACT FRONTEND (Port 5173)                      │
│              Multi-Client Chat Dashboard                     │
└────────────────────┬────────────────────────────────────────┘
                     │ WebSocket + HTTP
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 HUB SERVER (Ports 7070/7071)                 │
│         Central Service Registry & WebSocket Server          │
└──────┬──────────────────────────────────────────────────────┘
       │
       ├─────────────┬─────────────────┬──────────────────────┐
       │             │                 │                      │
   ┌───▼──────┐  ┌──▼──────────┐  ┌───▼──────────────────┐   │
   │ API      │  │ Secure File │  │ Future Services      │   │
   │ Gateway  │  │ Service     │  │ (NIO Log, RMI Task)  │   │
   │ Port     │  │ Port 9090   │  │                      │   │
   │ 9001     │  │ (SSL/TLS)   │  │                      │   │
   └──────────┘  └─────────────┘  └──────────────────────┘   │
```

---

## 🎯 QUICK START (Easiest Method)

### Option 1: One-Click Startup Script

```powershell
# Run this command from the project root:
.\START_FULL_SYSTEM.ps1
```

This will automatically start:
1. ✅ Hub Server (ports 7070/7071)
2. ✅ API Gateway (port 9001)
3. ✅ Secure File Service (port 9090)
4. ✅ React Frontend (port 5173)

---

## 📝 MANUAL STARTUP (Step-by-Step)

### Step 1: Start Hub Server
```powershell
cd hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[HubServer] Starting Distributed Services Hub...
[HubServer] TCP Server started on port 7070
[HubServer] HTTP Server started on port 7071
```

**Verify:**
- Open: http://localhost:7071/hub-status
- Should show: `"status": "Running"`

---

### Step 2: Start API Gateway
```powershell
# In a NEW terminal window
cd api-gateway-service
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[ApiGatewayService] Starting API Gateway...
[HubClient] ✓ Registered with Hub
[WebSocketServer] WebSocket server started on port 9001
```

**Verify:**
- Open: http://localhost:9001/status
- Should show: `"status": "running"`

---

### Step 3: Start Secure File Service
```powershell
# In a NEW terminal window
cd secure-file-service
java -jar target\secure-file-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
==============================================================================
  SECURE FILE SERVICE - MEMBER 3
==============================================================================

[HubClient] ✓ Service registered with Hub
[SSLFileServer] ✓ SSL Server Socket created on port 9090
✓ SECURE FILE SERVICE STARTED SUCCESSFULLY
```

**Verify:**
- Check Hub: http://localhost:7071/services
- Should show: `"SecureFileService"` with status `"online"`

---

### Step 4: Start React Frontend
```powershell
# In a NEW terminal window
cd multi-client-chat-frontend
npm run dev
```

**Expected Output:**
```
VITE v7.1.7  ready in XXX ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

**Open in Browser:**
- Navigate to: http://localhost:5173

---

## 🔍 VERIFY ALL SERVICES

### Check Hub Server Status:
```powershell
curl http://localhost:7071/hub-status
```

**Expected Response:**
```json
{
  "server": "Distributed Services Hub",
  "status": "Running",
  "tcpPort": 7070,
  "httpPort": 7071,
  "totalServices": 2,
  "onlineServices": 2
}
```

### Check Registered Services:
```powershell
curl http://localhost:7071/services
```

**Expected Response:**
```json
{
  "services": [
    {
      "name": "ApiGateway",
      "host": "localhost",
      "port": 9001,
      "status": "online"
    },
    {
      "name": "SecureFileService",
      "host": "localhost",
      "port": 9090,
      "status": "online"
    }
  ]
}
```

### Check Ports:
```powershell
netstat -ano | findstr "7070 7071 9001 9090 5173"
```

**Expected Output:**
```
TCP    0.0.0.0:5173           LISTENING    (Frontend)
TCP    0.0.0.0:7070           LISTENING    (Hub TCP)
TCP    0.0.0.0:7071           LISTENING    (Hub HTTP)
TCP    0.0.0.0:9001           LISTENING    (API Gateway)
TCP    0.0.0.0:9090           LISTENING    (Secure File)
```

---

## 🌐 SERVICE ENDPOINTS

### Hub Server:
- **HTTP Status:** http://localhost:7071/hub-status
- **Services List:** http://localhost:7071/services
- **WebSocket:** ws://localhost:7071/hub-ws

### API Gateway:
- **Status:** http://localhost:9001/status
- **Weather API:** http://localhost:9001/weather?city=London
- **WebSocket:** ws://localhost:9001/ws

### Secure File Service:
- **SSL Server:** localhost:9090 (SSL/TLS only)
- Commands: STORE, RETRIEVE, LIST, DELETE

### React Frontend:
- **Dashboard:** http://localhost:5173
- **Features:**
  - Service registry visualization
  - Real-time service status
  - WebSocket connection to Hub
  - Chat functionality

---

## 🎨 FRONTEND FEATURES

The React dashboard should display:

1. **Service Registry View**
   - List of all registered services
   - Service status (online/offline)
   - Service details (host, port)

2. **Real-time Updates**
   - WebSocket connection to Hub
   - Live service status updates
   - Heartbeat monitoring

3. **Chat Interface**
   - Multi-client chat
   - Message history
   - User list

4. **Service Statistics**
   - Total services count
   - Online services count
   - Uptime information

---

## 📊 SYSTEM STATUS CHECKLIST

- [ ] Hub Server running on ports 7070/7071
- [ ] API Gateway registered and online (port 9001)
- [ ] Secure File Service registered and online (port 9090)
- [ ] React Frontend accessible at http://localhost:5173
- [ ] WebSocket connections established
- [ ] All services showing "online" status
- [ ] Frontend displaying service registry

---

## 🐛 TROUBLESHOOTING

### Issue: Hub Server won't start
**Solution:**
```powershell
# Check if port is already in use
netstat -ano | findstr ":7070"
# Kill the process if needed
taskkill /PID <PID> /F
```

### Issue: Services not registering
**Solution:**
1. Ensure Hub Server is running first
2. Wait 2-3 seconds between starting each service
3. Check Hub logs for connection errors

### Issue: Frontend can't connect
**Solution:**
1. Check Hub Server is running on port 7071
2. Verify WebSocket endpoint in frontend config
3. Check browser console for errors

### Issue: Port already in use
**Solution:**
```powershell
# Check what's using the port
netstat -ano | findstr ":<PORT>"
# Kill the process
taskkill /PID <PID> /F
```

---

## 🔧 BUILD REQUIREMENTS

### Before First Run:

**Backend Services:**
```powershell
# Build Hub Server
cd hub-server
mvn clean package

# Build API Gateway
cd ..\api-gateway-service
mvn clean package

# Build Secure File Service
cd ..\secure-file-service
mvn clean package
```

**Frontend:**
```powershell
cd multi-client-chat-frontend
npm install
```

---

## 🎯 TESTING THE SYSTEM

### 1. Test Hub Server:
```powershell
curl http://localhost:7071/hub-status
```

### 2. Test API Gateway:
```powershell
curl http://localhost:9001/status
curl "http://localhost:9001/weather?city=London"
```

### 3. Test Service Registration:
```powershell
curl http://localhost:7071/services
```

### 4. Test Frontend:
- Open http://localhost:5173
- Check if services are listed
- Verify real-time updates

---

## 🛑 SHUTDOWN

### Graceful Shutdown:
1. Close React Frontend terminal (Ctrl+C)
2. Close Secure File Service terminal (Ctrl+C)
3. Close API Gateway terminal (Ctrl+C)
4. Close Hub Server terminal (Ctrl+C)

### Force Shutdown All:
```powershell
# Kill all Java processes (use with caution!)
Get-Process java | Stop-Process -Force

# Kill Vite dev server
Get-Process node | Where-Object {$_.MainWindowTitle -like "*Vite*"} | Stop-Process -Force
```

---

## 📈 SYSTEM METRICS

| Component | Port | Protocol | Status |
|-----------|------|----------|--------|
| Hub Server TCP | 7070 | TCP | Required |
| Hub Server HTTP | 7071 | HTTP/WebSocket | Required |
| API Gateway | 9001 | HTTP/WebSocket | Optional |
| Secure File Service | 9090 | SSL/TLS | Optional |
| React Frontend | 5173 | HTTP | Required |

**Minimum Required:** Hub Server + Frontend  
**Full System:** All 5 components

---

## 🎉 SUCCESS INDICATORS

✅ **All Systems Operational:**
- 4 PowerShell windows open (3 backend + 1 frontend)
- Hub status shows 2 online services
- Frontend displays service list
- No error messages in any terminal
- All ports listening on netstat

---

## 📚 ADDITIONAL RESOURCES

- **Hub Server Docs:** `hub-server/README.md`
- **API Gateway Docs:** `api-gateway-service/README.md`
- **Secure File Service Docs:** `secure-file-service/README.md`
- **Phase 4 Complete:** `PHASE_4_COMPLETE.md`
- **Implementation Plan:** `IMPLEMENTATION_PLAN.md`

---

## 🚀 QUICK COMMANDS

```powershell
# Full system startup
.\START_FULL_SYSTEM.ps1

# Check all services
curl http://localhost:7071/services

# Check all ports
netstat -ano | findstr "7070 7071 9001 9090 5173"

# Open dashboard
start http://localhost:5173
```

---

**System Ready! Navigate to http://localhost:5173 to see your dashboard!** 🎉
