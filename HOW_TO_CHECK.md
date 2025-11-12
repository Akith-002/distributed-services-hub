# 🔍 How to Check Your Services

## Quick Status Checks

### ✅ Method 1: Quick Commands (Fastest)

Open PowerShell and run these commands:

```powershell
# Check Hub Server
Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing

# Check Registered Services
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing

# Check API Gateway
Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing
```

---

### 📊 Method 2: Formatted Status (Recommended)

```powershell
# Hub Server Status
Write-Host "`n=== HUB SERVER ===" -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing | ConvertFrom-Json | Format-List

# Registered Services
Write-Host "`n=== REGISTERED SERVICES ===" -ForegroundColor Green
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing | ConvertFrom-Json | Select-Object -ExpandProperty services | Format-Table

# API Gateway Status
Write-Host "`n=== API GATEWAY ===" -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing | ConvertFrom-Json | Format-List
```

---

### 🔧 Method 3: Health Check Script

**Option A:** Run the automated script:
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub"
powershell -ExecutionPolicy Bypass -File .\CHECK_STATUS.ps1
```

**Option B:** Direct browser check - Open these URLs:
- Hub Status: http://localhost:7071/hub-status
- Services List: http://localhost:7071/services
- Gateway Status: http://localhost:9001/status

---

## 🎯 What to Look For

### ✅ Hub Server is Running if you see:
```json
{
  "server": "Distributed Services Hub",
  "status": "Running",
  "tcpPort": 7070,
  "httpPort": 7071,
  "totalServices": 1,
  "onlineServices": 1
}
```

### ✅ API Gateway is Registered if you see:
```json
{
  "services": [
    {
      "name": "ApiGateway",
      "host": "localhost",
      "port": 9001,
      "status": "online"
    }
  ]
}
```

### ✅ API Gateway is Running if you see:
```json
{
  "service": "ApiGateway",
  "status": "Running",
  "connectedDashboards": 0
}
```

---

## 🔍 Check Running Processes

```powershell
# See Java processes
Get-Process | Where-Object { $_.ProcessName -like "*java*" } | Select-Object Id, ProcessName, @{Name='Memory(MB)';Expression={[math]::Round($_.WS/1MB,2)}}

# Check specific ports
netstat -ano | findstr ":7070 :7071 :9001"
```

---

## 🌐 Browser Testing

Open these URLs in your browser:

| Service | URL | Expected Result |
|---------|-----|-----------------|
| Hub Status | http://localhost:7071/hub-status | JSON with server info |
| Services List | http://localhost:7071/services | JSON array of services |
| Gateway Health | http://localhost:9001/health | "OK" or health status |
| Gateway Status | http://localhost:9001/status | JSON with service info |

---

## 📋 Current Status (Your System)

Based on the latest check:

### ✅ Hub Server
- **Status**: 🟢 ONLINE
- **TCP Port**: 7070
- **HTTP Port**: 7071
- **Uptime**: ~16 minutes
- **Total Services**: 1
- **Online Services**: 1

### ✅ API Gateway
- **Status**: 🟢 ONLINE
- **Port**: 9001
- **Registered**: 23:52:14
- **Hub Connection**: ✅ Connected

### 📊 Ports in Use
```
TCP    0.0.0.0:7070    LISTENING    (Hub Server - TCP)
TCP    0.0.0.0:7071    LISTENING    (Hub Server - HTTP)
TCP    0.0.0.0:9001    LISTENING    (API Gateway)
```

---

## ❌ Troubleshooting

### If Hub Server shows OFFLINE:

```powershell
# Start Hub Server
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

### If API Gateway shows OFFLINE:

```powershell
# Start API Gateway
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\api-gateway-service"
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

### If Port is Already in Use:

```powershell
# Find what's using the port
netstat -ano | findstr :7071

# Kill the process (replace PID with actual number)
taskkill /PID <PID> /F
```

---

## 🎯 Quick Reference

### All-in-One Status Check
```powershell
Write-Host "`n=== SYSTEM STATUS ===" -ForegroundColor Cyan
try {
    $hub = Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ Hub Server: ONLINE" -ForegroundColor Green
} catch {
    Write-Host "❌ Hub Server: OFFLINE" -ForegroundColor Red
}

try {
    $gateway = Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ API Gateway: ONLINE" -ForegroundColor Green
} catch {
    Write-Host "❌ API Gateway: OFFLINE" -ForegroundColor Red
}

Write-Host "`n=== REGISTERED SERVICES ===" -ForegroundColor Cyan
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing | ConvertFrom-Json | Select-Object -ExpandProperty services | Format-Table
```

---

## 📞 Available Endpoints Reference

### Hub Server (Port 7071)
- `GET /hub-status` - Server status and statistics
- `GET /services` - List all registered services
- `WebSocket /registry` - Real-time service updates

### API Gateway (Port 9001)
- `GET /health` - Health check
- `GET /status` - Service status
- `WebSocket /api` - Send commands (fetchWeather, etc)

---

**Your services are currently running and healthy!** ✅
