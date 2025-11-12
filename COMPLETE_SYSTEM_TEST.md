# 🚀 COMPLETE SYSTEM TEST GUIDE
## Distributed Services Hub - Full Stack Verification

**Date:** November 11, 2025  
**Purpose:** Verify ALL components are working together  
**Components:** Hub Server, API Gateway, Secure File Service, React Frontend

---

## 📋 QUICK TEST CHECKLIST

Run through this checklist to verify everything is working:

- [ ] Hub Server running (ports 7070, 7071)
- [ ] API Gateway running and registered (port 9001)
- [ ] Secure File Service running and registered (port 9090)
- [ ] React Frontend running (port 5173)
- [ ] All services show in Hub registry
- [ ] WebSocket connections established
- [ ] Frontend displays services
- [ ] Real-time updates working
- [ ] API Gateway can fetch weather data
- [ ] File service accessible

---

## 🧪 TEST 1: VERIFY ALL SERVICES ARE RUNNING

### **Test 1A: Check All Ports**

```powershell
# Check all required ports
Write-Host "`n=== PORT STATUS CHECK ===" -ForegroundColor Cyan
Write-Host "`nChecking Hub Server (7070, 7071)..." -ForegroundColor Yellow
netstat -ano | findstr ":7070 :7071"

Write-Host "`nChecking API Gateway (9001)..." -ForegroundColor Yellow
netstat -ano | findstr :9001

Write-Host "`nChecking Secure File Service (9090)..." -ForegroundColor Yellow
netstat -ano | findstr :9090

Write-Host "`nChecking React Frontend (5173)..." -ForegroundColor Yellow
netstat -ano | findstr :5173
```

**Expected Output:**
```
Hub Server:
  TCP    0.0.0.0:7070           LISTENING
  TCP    0.0.0.0:7071           LISTENING

API Gateway:
  TCP    0.0.0.0:9001           LISTENING

Secure File Service:
  TCP    0.0.0.0:9090           LISTENING

React Frontend:
  TCP    0.0.0.0:5173           LISTENING
```

✅ **PASS** if all ports are listening

---

### **Test 1B: Check Hub Server Status**

```powershell
# Query Hub Server status endpoint
curl http://localhost:7071/hub-status | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**Expected Output:**
```json
{
    "server": "Distributed Services Hub",
    "status": "Running",
    "tcpPort": 7070,
    "httpPort": 7071,
    "totalServices": 2,
    "onlineServices": 2,
    "connectedDashboards": 1,
    "uptime": "..."
}
```

✅ **PASS** if:
- Status = "Running"
- totalServices = 2 (or more)
- onlineServices = 2 (or more)

---

## 🧪 TEST 2: VERIFY SERVICE REGISTRATION

### **Test 2A: List All Registered Services**

```powershell
# Get all services from Hub
Write-Host "`n=== REGISTERED SERVICES ===" -ForegroundColor Cyan
curl http://localhost:7071/services | ConvertFrom-Json | Select-Object -ExpandProperty services | Format-Table -AutoSize
```

**Expected Output:**
```
name               host      port status registered
----               ----      ---- ------ ----------
ApiGateway         localhost 9001 online 17:XX:XX
SecureFileService  localhost 9090 online 17:XX:XX
```

✅ **PASS** if both services appear with status "online"

---

### **Test 2B: Detailed Service Check**

```powershell
# Detailed service information
$services = (curl http://localhost:7071/services | ConvertFrom-Json).services

Write-Host "`n=== DETAILED SERVICE INFO ===" -ForegroundColor Cyan

foreach ($service in $services) {
    Write-Host "`nService: $($service.name)" -ForegroundColor Green
    Write-Host "  Host: $($service.host)" -ForegroundColor Gray
    Write-Host "  Port: $($service.port)" -ForegroundColor Gray
    Write-Host "  Status: $($service.status)" -ForegroundColor Gray
    Write-Host "  Registered: $($service.registered)" -ForegroundColor Gray
}
```

✅ **PASS** if all expected services are listed

---

## 🧪 TEST 3: VERIFY API GATEWAY

### **Test 3A: Check API Gateway WebSocket**

```powershell
# Try to access API Gateway
Write-Host "`n=== API GATEWAY TEST ===" -ForegroundColor Cyan
Write-Host "Testing API Gateway on port 9001..." -ForegroundColor Yellow

# Check if it's listening
netstat -ano | findstr :9001
```

**Expected:** Port 9001 should be LISTENING

---

### **Test 3B: Test Weather API (if implemented)**

```powershell
# Note: This test depends on API Gateway implementation
# Check the API Gateway console for endpoint information
Write-Host "`nAPI Gateway Endpoints:" -ForegroundColor Yellow
Write-Host "  WebSocket: ws://localhost:9001/api" -ForegroundColor Gray
Write-Host "  Check console logs for available features" -ForegroundColor Gray
```

✅ **PASS** if API Gateway is registered and accessible

---

## 🧪 TEST 4: VERIFY SECURE FILE SERVICE

### **Test 4A: Check SSL Service**

```powershell
Write-Host "`n=== SECURE FILE SERVICE TEST ===" -ForegroundColor Cyan

# Check if service is listening
Write-Host "Checking SSL port 9090..." -ForegroundColor Yellow
netstat -ano | findstr :9090

# Check registration
Write-Host "`nChecking Hub registration..." -ForegroundColor Yellow
$services = (curl http://localhost:7071/services | ConvertFrom-Json).services
$secureService = $services | Where-Object { $_.name -eq "SecureFileService" }

if ($secureService) {
    Write-Host "✅ Secure File Service is registered" -ForegroundColor Green
    Write-Host "   Status: $($secureService.status)" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Secure File Service NOT registered" -ForegroundColor Yellow
    Write-Host "   Check service console for errors" -ForegroundColor Gray
}
```

✅ **PASS** if service is listening on 9090 and registered with Hub

---

### **Test 4B: Test SSL Connection (Optional)**

```powershell
# For self-signed certificates, bypass validation
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}

# Try to connect (if REST endpoints implemented)
Write-Host "`nTesting SSL connection..." -ForegroundColor Yellow
Write-Host "Note: This requires REST API implementation" -ForegroundColor Gray
```

---

## 🧪 TEST 5: VERIFY REACT FRONTEND

### **Test 5A: Check Frontend Server**

```powershell
Write-Host "`n=== REACT FRONTEND TEST ===" -ForegroundColor Cyan

# Check if Vite dev server is running
Write-Host "Checking frontend port 5173..." -ForegroundColor Yellow
netstat -ano | findstr :5173
```

**Expected:** Port 5173 should be LISTENING

---

### **Test 5B: Open Frontend in Browser**

```powershell
# Open the dashboard
Write-Host "`nOpening frontend in browser..." -ForegroundColor Yellow
Start-Process "http://localhost:5173"
```

**Manual Verification Steps:**
1. Browser should open to the dashboard
2. Press **F12** to open Developer Console
3. Check **Console** tab for messages:
   ```
   Connecting to Hub Server at ws://localhost:7071/registry...
   Connected to Hub Server
   Received message from Hub: SERVICE_REGISTRY_UPDATE
   ```
4. Check **Network** tab → **WS** for WebSocket connections

✅ **PASS** if:
- Dashboard loads without errors
- Console shows "Connected to Hub Server"
- Services appear in the dashboard

---

## 🧪 TEST 6: VERIFY WEBSOCKET CONNECTIONS

### **Test 6A: Hub WebSocket**

**Frontend → Hub Connection:**
- URL: `ws://localhost:7071/registry`
- Purpose: Real-time service registry updates

**How to Verify:**
1. Open browser console (F12 → Console)
2. Look for: `"Connected to Hub Server"`
3. Look for: `"Received message from Hub: SERVICE_REGISTRY_UPDATE"`

---

### **Test 6B: API Gateway WebSocket**

**Frontend → API Gateway Connection:**
- URL: `ws://localhost:9001/api`
- Purpose: Weather data and external API calls

**How to Verify:**
1. In the dashboard, find "External Data Fetcher" section
2. Try to fetch weather data
3. Check console for connection messages

---

### **Test 6C: Chat WebSocket (if implemented)**

**Frontend → Chat Server Connection:**
- URL: `ws://localhost:7070/chat`
- Purpose: Multi-client chat

**How to Verify:**
1. In the dashboard, find "Chat Room" section
2. Try to send a message
3. Check if messages appear

---

## 🧪 TEST 7: VERIFY REAL-TIME UPDATES

### **Test 7A: Service Registration Updates**

**Steps:**
1. Open the dashboard (http://localhost:5173)
2. Note the current number of services
3. **Start a new service** (or restart one)
4. **Watch the dashboard** - it should update automatically
5. Check console for: `"Received message from Hub: SERVICE_ONLINE"`

✅ **PASS** if dashboard updates without refreshing

---

### **Test 7B: Service Offline Detection**

**Steps:**
1. Open the dashboard
2. **Stop one service** (close its terminal window)
3. **Wait 30-45 seconds** (heartbeat timeout)
4. Dashboard should show service as "offline"
5. Check console for: `"Received message from Hub: SERVICE_OFFLINE"`

✅ **PASS** if dashboard detects offline service

---

## 🧪 TEST 8: VERIFY HEARTBEAT MECHANISM

### **Test 8A: Hub Server Heartbeat Monitoring**

**Check Hub Server Console:**
Look for heartbeat messages every 30 seconds:
```
[HEARTBEAT] Checking service health...
[HEARTBEAT] ApiGateway: OK
[HEARTBEAT] SecureFileService: OK
```

---

### **Test 8B: Service Heartbeat Sending**

**Check Each Service Console:**

**API Gateway Console:**
```
[HEARTBEAT] Sent heartbeat to Hub
```

**Secure File Service Console:**
```
[HubClient] Heartbeat sent to Hub
```

✅ **PASS** if all services send heartbeats regularly

---

## 🧪 TEST 9: VERIFY BROWSER CONSOLE

### **What to Look For:**

Open browser at http://localhost:5173, press **F12**, check **Console** tab:

**✅ Good Messages:**
```javascript
Connecting to Hub Server at ws://localhost:7071/registry...
Connected to Hub Server
Received message from Hub: SERVICE_REGISTRY_UPDATE 2
Services updated: 2
```

**❌ Error Messages to Watch For:**
```javascript
WebSocket connection failed
Failed to connect to Hub Server
Error: Connection refused
```

✅ **PASS** if no WebSocket errors appear

---

## 🧪 TEST 10: END-TO-END INTEGRATION TEST

### **Complete Workflow Test:**

**Step 1: Start All Services** ✅
```powershell
# In separate terminals:
# Terminal 1: Hub Server
cd hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar

# Terminal 2: API Gateway
cd api-gateway-service
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar

# Terminal 3: Secure File Service
cd secure-file-service
java -jar target/secure-file-service-1.0-SNAPSHOT.jar

# Terminal 4: React Frontend
cd multi-client-chat-frontend
npm run dev
```

**Step 2: Verify All Running** ✅
```powershell
netstat -ano | findstr ":7070 :7071 :9001 :9090 :5173"
```

**Step 3: Check Hub Registry** ✅
```powershell
curl http://localhost:7071/services
```

**Step 4: Open Dashboard** ✅
```
http://localhost:5173
```

**Step 5: Verify Dashboard Shows Services** ✅
- Should see 2 services listed
- Both should show "online" status

**Step 6: Test Real-Time Updates** ✅
- Stop one service
- Watch dashboard update (within 30-45 seconds)

**Step 7: Restart Service** ✅
- Start the stopped service
- Watch dashboard update to show it online

✅ **COMPLETE SUCCESS** if all steps pass!

---

## 📊 COMPREHENSIVE TEST SCRIPT

Save this as `test-everything.ps1`:

```powershell
# COMPLETE SYSTEM TEST SCRIPT
Write-Host "`n" -NoNewline
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  DISTRIBUTED SERVICES HUB - SYSTEM TEST" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$passCount = 0
$failCount = 0
$warnCount = 0

# TEST 1: Port Status
Write-Host "TEST 1: Checking All Ports..." -ForegroundColor Yellow
$ports = @(7070, 7071, 9001, 9090, 5173)
$portNames = @("Hub TCP", "Hub HTTP", "API Gateway", "Secure File", "Frontend")

for ($i = 0; $i -lt $ports.Count; $i++) {
    $port = $ports[$i]
    $name = $portNames[$i]
    $result = netstat -ano | findstr ":$port"
    
    if ($result -match "LISTENING") {
        Write-Host "  ✅ $name (Port $port) - LISTENING" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "  ❌ $name (Port $port) - NOT RUNNING" -ForegroundColor Red
        $failCount++
    }
}

# TEST 2: Hub Server Status
Write-Host "`nTEST 2: Hub Server Status..." -ForegroundColor Yellow
try {
    $hubStatus = Invoke-RestMethod -Uri "http://localhost:7071/hub-status" -ErrorAction Stop
    Write-Host "  ✅ Hub Server: $($hubStatus.status)" -ForegroundColor Green
    Write-Host "     Total Services: $($hubStatus.totalServices)" -ForegroundColor Gray
    Write-Host "     Online Services: $($hubStatus.onlineServices)" -ForegroundColor Gray
    $passCount++
} catch {
    Write-Host "  ❌ Hub Server: NOT ACCESSIBLE" -ForegroundColor Red
    $failCount++
}

# TEST 3: Service Registration
Write-Host "`nTEST 3: Service Registration..." -ForegroundColor Yellow
try {
    $servicesResponse = Invoke-RestMethod -Uri "http://localhost:7071/services" -ErrorAction Stop
    $services = $servicesResponse.services
    
    Write-Host "  Registered Services: $($services.Count)" -ForegroundColor Gray
    
    foreach ($service in $services) {
        Write-Host "  ✅ $($service.name) - $($service.status) (port $($service.port))" -ForegroundColor Green
        $passCount++
    }
    
    # Check for expected services
    $apiGateway = $services | Where-Object { $_.name -eq "ApiGateway" }
    $secureFile = $services | Where-Object { $_.name -eq "SecureFileService" }
    
    if (-not $apiGateway) {
        Write-Host "  ⚠️  ApiGateway NOT registered" -ForegroundColor Yellow
        $warnCount++
    }
    
    if (-not $secureFile) {
        Write-Host "  ⚠️  SecureFileService NOT registered" -ForegroundColor Yellow
        $warnCount++
    }
    
} catch {
    Write-Host "  ❌ Cannot query services" -ForegroundColor Red
    $failCount++
}

# TEST 4: Frontend
Write-Host "`nTEST 4: Frontend Accessibility..." -ForegroundColor Yellow
try {
    $frontendTest = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 5 -ErrorAction Stop
    if ($frontendTest.StatusCode -eq 200) {
        Write-Host "  ✅ Frontend accessible at http://localhost:5173" -ForegroundColor Green
        $passCount++
    }
} catch {
    Write-Host "  ❌ Frontend NOT accessible" -ForegroundColor Red
    Write-Host "     Error: $($_.Exception.Message)" -ForegroundColor Gray
    $failCount++
}

# SUMMARY
Write-Host "`n" -NoNewline
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  ✅ Passed: $passCount" -ForegroundColor Green
Write-Host "  ❌ Failed: $failCount" -ForegroundColor Red
Write-Host "  ⚠️  Warnings: $warnCount" -ForegroundColor Yellow
Write-Host ""

if ($failCount -eq 0 -and $warnCount -eq 0) {
    Write-Host "🎉 ALL SYSTEMS OPERATIONAL!" -ForegroundColor Green
} elseif ($failCount -eq 0) {
    Write-Host "⚠️  System mostly operational with warnings" -ForegroundColor Yellow
} else {
    Write-Host "❌ System has failures - check services" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Next Steps
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Open browser: http://localhost:5173" -ForegroundColor White
Write-Host "2. Press F12 to open Developer Console" -ForegroundColor White
Write-Host "3. Check Console tab for WebSocket connections" -ForegroundColor White
Write-Host "4. Verify services appear in dashboard" -ForegroundColor White
Write-Host ""
```

---

## 🎯 EXPECTED FINAL STATE

When everything is working correctly:

### **Services Running:**
- ✅ Hub Server (ports 7070, 7071)
- ✅ API Gateway (port 9001) - REGISTERED
- ✅ Secure File Service (port 9090) - REGISTERED
- ✅ React Frontend (port 5173)

### **Hub Server Status:**
```json
{
  "status": "Running",
  "totalServices": 2,
  "onlineServices": 2,
  "connectedDashboards": 1
}
```

### **Browser Console:**
```
✅ Connected to Hub Server
✅ Received message: SERVICE_REGISTRY_UPDATE
✅ Services displayed: 2
```

### **Dashboard Display:**
```
Service Registry
├─ ApiGateway (port 9001) - online ✅
└─ SecureFileService (port 9090) - online ✅
```

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: Service Not Registered
**Solution:** Check service console for connection errors, restart service

### Issue 2: Frontend Can't Connect
**Solution:** Verify Hub Server is running on port 7071, check CORS settings

### Issue 3: WebSocket Disconnects
**Solution:** Check firewall, verify all ports are accessible

### Issue 4: Services Show Offline
**Solution:** Check heartbeat mechanism, verify TCP connection to Hub

---

## ✅ SUCCESS CRITERIA

Your system is **fully operational** if:

1. ✅ All 5 ports are listening (7070, 7071, 9001, 9090, 5173)
2. ✅ Hub Server returns status 200
3. ✅ 2+ services registered and online
4. ✅ Frontend loads without errors
5. ✅ Browser console shows WebSocket connected
6. ✅ Dashboard displays all services
7. ✅ Real-time updates work (services appear/disappear)
8. ✅ Heartbeats sending every 10-30 seconds

---

## 🚀 QUICK START - TEST EVERYTHING NOW!

```powershell
# Run the comprehensive test
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub"
.\test-everything.ps1

# Then open the dashboard
Start-Process "http://localhost:5173"
```

**Check browser console (F12 → Console) for WebSocket messages!**

---

**Your complete distributed services hub should now be fully operational!** 🎉
