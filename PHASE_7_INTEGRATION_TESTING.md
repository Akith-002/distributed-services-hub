# Phase 7: Integration & Testing Guide

**Date:** November 12, 2025  
**Status:** In Progress  
**Duration:** 2-3 days

---

## 📋 OVERVIEW

Phase 7 brings together all 5 microservices and the React Dashboard into a fully integrated distributed system. This phase focuses on:

1. **System Startup Sequence** - Correct order to start all services
2. **UI Testing** - Verify each of the 5 dashboard tabs works correctly
3. **End-to-End Testing** - Test complete message flows through the Hub
4. **Demo Preparation** - Create scripts and walkthroughs for presentation

---

## 🚀 SYSTEM STARTUP SEQUENCE

### Step 1: Start Hub Server (Member 1)

**Terminal 1:**
```powershell
cd distributed-services-hub\hub-server
mvn clean package
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[HUB] Starting Distributed Services Hub on port 7070...
[HUB] WebSocket endpoint: ws://localhost:7070/hub
[HUB] Service Registry initialized
[HUB] Heartbeat Monitor started (check interval: 5s, timeout: 30s)
[HUB] Hub is ready and waiting for services...
```

**Checklist:**
- [ ] Hub starts without errors
- [ ] Port 7070 is listening
- [ ] WebSocket endpoint available
- [ ] Heartbeat monitor thread running

---

### Step 2: Start React Dashboard (Member 2)

**Terminal 2:**
```powershell
cd multi-client-chat-frontend
npm run dev
```

**Expected Output:**
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

**Open Browser:** `http://localhost:5173`

**Checklist:**
- [ ] Dashboard loads without errors
- [ ] 5 tabs visible: Service Registry, API Gateway, Security Test, NIO Log Stream, RMI Task Runner
- [ ] WebSocket connection to Hub established
- [ ] Service Registry tab shows "No services registered yet" or empty list

---

### Step 3: Start NIO Log Service (Member 4)

**Terminal 3:**
```powershell
cd distributed-services-hub\nio-log-service
mvn clean package
java -jar target\nio-log-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[NIO-LOG] Starting NIO Log Service...
[NIO-LOG] Selector-based server listening on port 9091
[NIO-LOG] Registered with Hub: NIO_SERVICE
[NIO-LOG] Heartbeat thread started
[NIO-LOG] NIO Log Service ready to accept connections
```

**Verify in Dashboard:**
- [ ] Service Registry tab shows "NIO_SERVICE" as ONLINE
- [ ] NIO Log Stream tab shows initial log: "NIO Log Service started"

---

### Step 4: Start API Gateway Service (Member 2)

**Terminal 4:**
```powershell
cd distributed-services-hub\api-gateway-service
mvn clean package
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[API-GATEWAY] Starting API Gateway Service...
[API-GATEWAY] Command listener on port 9001
[API-GATEWAY] Registered with Hub: API_GATEWAY
[API-GATEWAY] Heartbeat thread started
[API-GATEWAY] Ready to process commands
```

**Verify in Dashboard:**
- [ ] Service Registry tab shows "API_GATEWAY" as ONLINE
- [ ] NIO Log Stream shows: "API Gateway Service registered"

---

### Step 5: Start Secure File Service (Member 3)

**Terminal 5:**
```powershell
cd distributed-services-hub\secure-file-service
mvn clean package
java -jar target\secure-file-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[SECURE-FILE] Starting Secure File Service...
[SECURE-FILE] SSL/TLS initialized with keystore
[SECURE-FILE] SSLServerSocket listening on port 9090
[SECURE-FILE] Registered with Hub: JSSE_SERVICE
[SECURE-FILE] Heartbeat thread started
[SECURE-FILE] Ready for secure connections
```

**Verify in Dashboard:**
- [ ] Service Registry tab shows "JSSE_SERVICE" as ONLINE
- [ ] NIO Log Stream shows: "Secure File Service registered"

---

### Step 6: Start RMI Task Service (Member 5)

**Terminal 6:**
```powershell
cd distributed-services-hub\rmi-task-service
mvn clean package
java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[RMI-TASK] Starting RMI Task Service...
[RMI-TASK] RMI Registry created on port 1099
[RMI-TASK] TaskService bound: rmi://localhost:1099/TaskService
[RMI-TASK] Registered with Hub: RMI_SERVICE
[RMI-TASK] Heartbeat thread started
[RMI-TASK] Available tasks: [calculate-pi, fibonacci-10, matrix-multiply, prime-check]
```

**Verify in Dashboard:**
- [ ] Service Registry tab shows "RMI_SERVICE" as ONLINE
- [ ] NIO Log Stream shows: "RMI Task Service registered"

---

## ✅ UI TESTING - EACH TAB

### Tab 1: Service Registry (Member 1's Demo)

**Test 1: Service List Display**

**Steps:**
1. Navigate to "Service Registry" tab
2. Verify all 5 services are listed

**Expected Result:**
```
Service Name       | Host:Port           | Status  | Last Heartbeat
-------------------|---------------------|---------|---------------
NIO_SERVICE        | localhost:9091      | ONLINE  | 2s ago
API_GATEWAY        | localhost:9001      | ONLINE  | 1s ago
JSSE_SERVICE       | localhost:9090      | ONLINE  | 3s ago
RMI_SERVICE        | rmi://localhost:1099| ONLINE  | 2s ago
```

**Checklist:**
- [ ] All 4 services visible
- [ ] Status shows "ONLINE" for all
- [ ] Last Heartbeat updates every 10 seconds
- [ ] Host:Port information correct

---

**Test 2: Real-Time Service Join**

**Steps:**
1. Keep Service Registry tab open
2. In Terminal 4, stop API Gateway (Ctrl+C)
3. Wait 30 seconds
4. Restart API Gateway

**Expected Result:**
- [ ] After 30s, API_GATEWAY status changes to "TIMEOUT" or disappears
- [ ] Dashboard log shows: "Service API_GATEWAY timed out"
- [ ] When restarted, API_GATEWAY reappears immediately
- [ ] Dashboard log shows: "Service API_GATEWAY registered"

**This proves:** Member 1's ConcurrentHashMap and heartbeat mechanism work correctly!

---

**Test 3: Concurrent Service Updates**

**Steps:**
1. Stop all services (Ctrl+C in each terminal)
2. Start them all within 10 seconds
3. Watch Service Registry tab

**Expected Result:**
- [ ] All services appear within 2-3 seconds
- [ ] No race conditions or missing services
- [ ] ConcurrentHashMap handles concurrent registrations

**This proves:** Member 1's multithreading and concurrency implementation is thread-safe!

---

### Tab 2: API Gateway (Member 2's Demo)

**Test 1: Fetch Weather Data**

**Steps:**
1. Navigate to "API Gateway" tab
2. Click "Fetch Weather" button
3. Wait 2-3 seconds

**Expected Result:**
- [ ] Button shows "Loading..." during request
- [ ] Weather data appears:
  ```
  Location: Colombo, Sri Lanka
  Temperature: 28.5°C
  Condition: Partly Cloudy
  Humidity: 75%
  Wind Speed: 12 km/h
  ```
- [ ] Data is real (changes daily)
- [ ] NIO Log Stream shows: "API Gateway: Weather fetched for Colombo"

**This proves:** Member 2's HttpURLConnection implementation works!

---

**Test 2: Multiple API Calls**

**Steps:**
1. Click "Fetch Weather" 5 times rapidly
2. Watch results and logs

**Expected Result:**
- [ ] All 5 requests complete successfully
- [ ] No race conditions or errors
- [ ] Each request logged in NIO Log Stream
- [ ] Hub routes all commands correctly

**This proves:** Message broker handles concurrent commands!

---

**Test 3: Command Routing Through Hub**

**Steps:**
1. Open browser developer console (F12)
2. Watch Network tab → WS (WebSocket)
3. Click "Fetch Weather"

**Expected Result:**
- [ ] WebSocket sends: `{"command_for": "API_GATEWAY", "payload": "get-weather"}`
- [ ] WebSocket receives: `{"result_from": "API_GATEWAY", "data": "{...}"}`
- [ ] Dashboard never directly contacts API Gateway (only Hub)

**This proves:** Clean separation - Dashboard only talks to Hub!

---

### Tab 3: Security Test (Member 3's Demo)

**Test 1: Run Security Connection Test**

**Steps:**
1. Navigate to "Security Test" tab
2. Click "Run Security Test" button
3. Wait 2-3 seconds

**Expected Result:**

**Test Results Display:**
```
Test 1 (Insecure Socket):
❌ FAILED - Connection refused or SSL handshake error
Details: Regular Socket cannot connect to SSLServerSocket

Test 2 (Secure SSLSocket):
✅ SUCCESS - SSL/TLS handshake completed
Details: Secure connection established on port 9090
Protocol: TLSv1.2
Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
```

**Checklist:**
- [ ] Test 1 shows FAILED (this is correct!)
- [ ] Test 2 shows SUCCESS
- [ ] Both results appear within 3 seconds
- [ ] NIO Log Stream shows both test executions

**This proves:** Member 3's SSLServerSocket rejects insecure connections!

---

**Test 2: Verify Certificate Details**

**Steps:**
1. Check Test 2 result details
2. Verify TLS protocol and cipher suite shown

**Expected Result:**
- [ ] Protocol: TLSv1.2 or TLSv1.3
- [ ] Cipher Suite shown (proves SSL handshake succeeded)
- [ ] Certificate DN shown: `CN=FileServer`

**This proves:** Member 3's JSSE (Java Secure Socket Extension) configuration is correct!

---

### Tab 4: NIO Log Stream (Member 4's Demo)

**Test 1: Real-Time Log Display**

**Steps:**
1. Navigate to "NIO Log Stream" tab
2. Perform actions in other tabs:
   - Fetch weather (Tab 2)
   - Run security test (Tab 3)
   - Execute RMI task (Tab 5)

**Expected Result:**

**Log Stream Display (auto-scrolling):**
```
[2025-11-12 14:23:15] API_GATEWAY: Weather API called for Colombo
[2025-11-12 14:23:16] API_GATEWAY: HttpURLConnection successful (200 OK)
[2025-11-12 14:23:17] API_GATEWAY: Weather data fetched (temp: 28.5°C)
[2025-11-12 14:23:20] JSSE_SERVICE: Security test initiated
[2025-11-12 14:23:21] JSSE_SERVICE: Test 1 (Insecure) - Connection rejected
[2025-11-12 14:23:22] JSSE_SERVICE: Test 2 (Secure) - SSL handshake success
[2025-11-12 14:23:25] RMI_SERVICE: Remote task 'calculate-pi' started
[2025-11-12 14:23:26] RMI_SERVICE: Task completed (result: 3.14159...)
```

**Checklist:**
- [ ] Logs appear in real-time (< 1 second delay)
- [ ] Auto-scrolls to bottom
- [ ] Timestamp on each log line
- [ ] Clear "Clear Logs" button works
- [ ] All service activities logged

**This proves:** Member 4's NIO Selector handles multiple concurrent log streams!

---

**Test 2: High-Volume Log Stress Test**

**Steps:**
1. Click "Fetch Weather" rapidly 10 times
2. Click "Run Security Test" rapidly 5 times
3. Execute 5 RMI tasks simultaneously
4. Watch NIO Log Stream

**Expected Result:**
- [ ] All ~50 log lines appear
- [ ] No logs lost or dropped
- [ ] Order preserved (FIFO)
- [ ] No performance degradation
- [ ] NIO service CPU usage stays low

**This proves:** Single-threaded NIO Selector is more efficient than multi-threaded blocking I/O!

---

**Test 3: Verify Non-Blocking I/O**

**Steps:**
1. Monitor NIO Log Service terminal
2. Send 100 rapid log messages from multiple services
3. Check CPU and thread count

**Expected Result:**
- [ ] Only 1-2 threads handling all connections
- [ ] CPU usage < 10%
- [ ] No thread pool exhaustion
- [ ] Selector.select() properly multiplexing

**This proves:** Member 4's non-blocking I/O implementation is correct!

---

### Tab 5: RMI Task Runner (Member 5's Demo)

**Test 1: Execute Remote Task**

**Steps:**
1. Navigate to "RMI Task Runner" tab
2. Select task: "calculate-pi"
3. Click "Execute Remote Task" button
4. Wait 1-2 seconds

**Expected Result:**
```
Task: calculate-pi
Status: ✅ Completed
Result: π ≈ 3.141592653589793
Execution Time: 0.8s
Remote Host: localhost:1099
```

**Checklist:**
- [ ] Task result appears within 2 seconds
- [ ] Result is mathematically correct
- [ ] Status shows "Completed"
- [ ] Execution time displayed
- [ ] NIO Log Stream shows: "RMI_SERVICE: Task 'calculate-pi' completed"

**This proves:** Member 5's RMI remote method invocation works!

---

**Test 2: Multiple Task Execution**

**Steps:**
1. Execute each task type:
   - calculate-pi
   - fibonacci-10
   - matrix-multiply
   - prime-check

**Expected Results:**

| Task             | Expected Result                         |
|------------------|-----------------------------------------|
| calculate-pi     | π ≈ 3.141592653589793                   |
| fibonacci-10     | 55                                      |
| matrix-multiply  | [[30, 36, 42], [66, 81, 96], ...]       |
| prime-check      | Prime numbers up to 100: [2, 3, 5, ...] |

**Checklist:**
- [ ] All 4 tasks execute successfully
- [ ] Results are correct
- [ ] Each task logged in NIO stream
- [ ] No remote exceptions

**This proves:** Member 5's TaskService remote interface is fully functional!

---

**Test 3: RMI Integration with Hub**

**Steps:**
1. Open browser console
2. Execute "calculate-pi" task
3. Watch WebSocket traffic

**Expected Flow:**
```
Dashboard → Hub (WebSocket):
  {"command_for": "RMI_SERVICE", "payload": "calculate-pi"}

Hub → RMI Client (Internal):
  Hub calls Member 5's RMI client code
  
RMI Client → RMI Server (RMI):
  Remote method invocation: taskService.executeTask("calculate-pi")
  
RMI Server → RMI Client (RMI):
  Returns: "π ≈ 3.141592653589793"
  
RMI Client → Hub (Internal):
  Returns result to Hub
  
Hub → Dashboard (WebSocket):
  {"result_from": "RMI_SERVICE", "data": "π ≈ 3.141592653589793"}
```

**Checklist:**
- [ ] Command routes through Hub (not direct to RMI)
- [ ] Remote method invocation happens inside Hub
- [ ] Result returns through Hub to Dashboard

**This proves:** RMI integration with message broker architecture works!

---

## 🔗 END-TO-END TESTING

### Test 1: Complete Message Flow

**Goal:** Verify commands flow correctly: Dashboard → Hub → Service → Hub → Dashboard

**Test Case: API Gateway Weather Fetch**

**Steps:**
1. Open 3 browser tabs:
   - Tab A: Dashboard (Service Registry)
   - Tab B: Dashboard (API Gateway)
   - Tab C: Dashboard (NIO Log Stream)
2. Arrange windows side-by-side
3. Click "Fetch Weather" in Tab B
4. Watch all 3 tabs simultaneously

**Expected Sequence:**

| Time | Tab A (Registry)              | Tab B (API Gateway)      | Tab C (Logs)                         |
|------|-------------------------------|--------------------------|--------------------------------------|
| T+0s | API_GATEWAY status: ONLINE    | Button clicked           | -                                    |
| T+1s | Last Heartbeat updates        | "Loading..."             | "API_GATEWAY: Weather API called"    |
| T+2s | -                             | -                        | "API_GATEWAY: HttpURLConnection OK"  |
| T+3s | -                             | Weather data displayed   | "API_GATEWAY: Data fetched"          |

**Checklist:**
- [ ] All 3 tabs update correctly
- [ ] No tab shows errors
- [ ] Timing is consistent (< 3 seconds total)
- [ ] Message broker routes correctly

---

### Test 2: Concurrent Multi-Service Commands

**Goal:** Prove Hub handles multiple simultaneous commands without blocking

**Steps:**
1. Open all 5 tabs in separate browser windows
2. Simultaneously (within 1 second):
   - Click "Fetch Weather" (Tab 2)
   - Click "Run Security Test" (Tab 3)
   - Execute "calculate-pi" (Tab 5)
3. Watch all results

**Expected Result:**
- [ ] All 3 commands complete successfully
- [ ] No command blocks another
- [ ] Results appear in 2-4 seconds
- [ ] NIO Log Stream shows all activities
- [ ] Service Registry remains stable

**This proves:** Hub's message broker handles concurrent operations correctly!

---

### Test 3: Service Failure and Recovery

**Goal:** Test system resilience

**Steps:**
1. All services running normally
2. Stop API Gateway (Ctrl+C in Terminal 4)
3. Wait 30 seconds (heartbeat timeout)
4. Try to "Fetch Weather" from Dashboard
5. Restart API Gateway
6. Try "Fetch Weather" again

**Expected Results:**

| Phase                  | Service Registry         | API Gateway Tab           | NIO Log Stream                   |
|------------------------|--------------------------|---------------------------|----------------------------------|
| Normal operation       | API_GATEWAY: ONLINE      | "Fetch Weather" works     | Normal logs                      |
| After crash (T+30s)    | API_GATEWAY: TIMEOUT     | "Service unavailable"     | "API_GATEWAY: Timeout detected"  |
| Command attempt        | API_GATEWAY: TIMEOUT     | Error message shown       | "Command failed: Service down"   |
| After restart          | API_GATEWAY: ONLINE      | "Fetch Weather" works     | "API_GATEWAY: Re-registered"     |

**Checklist:**
- [ ] Timeout detection works (30 seconds)
- [ ] Service disappears from registry
- [ ] Dashboard shows error gracefully
- [ ] Service re-registers on restart
- [ ] System recovers automatically

**This proves:** Heartbeat mechanism and fault tolerance work correctly!

---

### Test 4: System-Wide Logging

**Goal:** Verify all service activities are logged

**Steps:**
1. Perform one action in each tab:
   - Tab 2: Fetch Weather
   - Tab 3: Run Security Test
   - Tab 5: Execute calculate-pi
2. Check NIO Log Stream

**Expected Log Output:**
```
[API_GATEWAY] Weather API called for Colombo
[API_GATEWAY] HttpURLConnection successful (200 OK)
[API_GATEWAY] Weather data fetched (temp: 28.5°C)

[JSSE_SERVICE] Security test initiated
[JSSE_SERVICE] Test 1 (Insecure Socket): Connection rejected ✅
[JSSE_SERVICE] Test 2 (Secure SSLSocket): SSL handshake success ✅

[RMI_SERVICE] Remote task 'calculate-pi' started
[RMI_SERVICE] Task completed successfully
[RMI_SERVICE] Result: π ≈ 3.141592653589793
```

**Checklist:**
- [ ] Every action logged
- [ ] Logs from all 4 services present
- [ ] Log format consistent
- [ ] Timestamps accurate
- [ ] No missing logs

**This proves:** System-wide logging integration is complete!

---

## 🎬 DEMO PREPARATION

### Create Startup Script

Create: `START_ALL_SERVICES.ps1`

```powershell
# Distributed Services Hub - Startup Script
Write-Host "=== Starting Distributed Services Hub ===" -ForegroundColor Cyan

# Start Hub Server
Write-Host "`n[1/6] Starting Hub Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd distributed-services-hub\hub-server; java -jar target\hub-server-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start NIO Log Service
Write-Host "[2/6] Starting NIO Log Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd distributed-services-hub\nio-log-service; java -jar target\nio-log-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# Start API Gateway
Write-Host "[3/6] Starting API Gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd distributed-services-hub\api-gateway-service; java -jar target\api-gateway-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# Start Secure File Service
Write-Host "[4/6] Starting Secure File Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd distributed-services-hub\secure-file-service; java -jar target\secure-file-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# Start RMI Task Service
Write-Host "[5/6] Starting RMI Task Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd distributed-services-hub\rmi-task-service; java -jar target\rmi-task-service-1.0-SNAPSHOT.jar"
Start-Sleep -Seconds 2

# Start React Dashboard
Write-Host "[6/6] Starting React Dashboard..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd multi-client-chat-frontend; npm run dev"

Write-Host "`n=== All services started! ===" -ForegroundColor Green
Write-Host "Dashboard will open at: http://localhost:5173" -ForegroundColor Cyan
Write-Host "`nPress Ctrl+C in each terminal to stop services" -ForegroundColor Gray
```

---

### Demo Script for Presentation

Create: `DEMO_SCRIPT.md`

```markdown
# Demonstration Script

## Introduction (1 minute)
"We've built a Distributed Services Hub that demonstrates 5 core networking concepts
through a unified React dashboard. Each team member implemented a different service..."

## Demo Flow (8 minutes)

### 1. Service Registry - Multithreading & Concurrency (Member 1)
- Show Tab 1: All services registered
- Stop one service → Show timeout detection
- Restart service → Show automatic re-registration
- "This proves our ConcurrentHashMap handles concurrent service updates"

### 2. API Gateway - HttpURLConnection (Member 2)
- Show Tab 2: Click "Fetch Weather"
- Real external API call using HttpURLConnection
- Real-time weather data displayed
- "This demonstrates HTTP client communication with external APIs"

### 3. Security Test - JSSE & SSL/TLS (Member 3)
- Show Tab 3: Click "Run Security Test"
- Two results appear:
  - Insecure Socket: FAILED ❌
  - Secure SSLSocket: SUCCESS ✅
- "This proves our SSLServerSocket only accepts secure connections"

### 4. NIO Log Stream - Non-Blocking I/O (Member 4)
- Show Tab 4: Real-time logs scrolling
- Perform actions in other tabs → Logs appear instantly
- "Single-threaded NIO Selector handles all log streams concurrently"

### 5. RMI Task Runner - Remote Method Invocation (Member 5)
- Show Tab 5: Execute "calculate-pi"
- Result appears from remote server
- "This demonstrates Java RMI for distributed computing"

## Conclusion (1 minute)
"Our Hub acts as a message broker, routing commands from the dashboard to services.
This architecture is scalable, fault-tolerant, and demonstrates real-world
distributed systems patterns."
```

---

### Pre-Demo Checklist

**24 Hours Before:**
- [ ] All services build without errors
- [ ] All dependencies installed
- [ ] Keystore/certificates valid
- [ ] External API (weather) accessible
- [ ] RMI registry port available (1099)
- [ ] No port conflicts (7070, 9001, 9090, 9091, 1099)

**1 Hour Before:**
- [ ] Run full system test
- [ ] Test all 5 tabs
- [ ] Verify logs appearing
- [ ] Check heartbeat mechanism
- [ ] Test service recovery
- [ ] Clear browser cache
- [ ] Restart computer (fresh environment)

**5 Minutes Before:**
- [ ] Close all unnecessary applications
- [ ] Start services in correct order
- [ ] Open browser to dashboard
- [ ] Verify all tabs load
- [ ] Keep backup video recording ready

---

## 🐛 TROUBLESHOOTING COMMON ISSUES

### Issue 1: Hub Won't Start

**Symptoms:** Port 7070 already in use

**Solution:**
```powershell
# Find process using port 7070
netstat -ano | findstr :7070

# Kill process (replace PID)
taskkill /PID <PID> /F

# Restart Hub
```

---

### Issue 2: Services Not Appearing in Registry

**Symptoms:** Service starts but doesn't show on Dashboard

**Debugging Steps:**
1. Check service logs - is it sending REGISTER message?
2. Check Hub logs - is it receiving registration?
3. Verify port numbers match
4. Check firewall settings
5. Verify WebSocket connection in browser console

**Solution:** Usually timing issue - wait 5 seconds, or restart service

---

### Issue 3: WebSocket Connection Failed

**Symptoms:** Dashboard shows "Disconnected" or "Connecting..."

**Solution:**
```javascript
// Check browser console for errors
// Verify Hub is running on port 7070
// Check WebSocket URL in Dashboard code:
const ws = new WebSocket('ws://localhost:7070/hub');
```

---

### Issue 4: API Gateway - Weather Not Fetching

**Symptoms:** Click "Fetch Weather" → No result or error

**Debugging:**
1. Check API Gateway logs - is command received?
2. Check external API is accessible:
   ```powershell
   curl https://api.open-meteo.com/v1/forecast?latitude=6.9271&longitude=79.8612
   ```
3. Check HttpURLConnection timeout settings
4. Verify Hub routing logic

---

### Issue 5: RMI Service - "NotBoundException"

**Symptoms:** RMI task execution fails

**Solution:**
```powershell
# Verify RMI registry is running
rmiregistry 1099 &

# Or kill existing registry
taskkill /IM rmiregistry.exe /F

# Restart RMI service
```

---

### Issue 6: NIO Logs Not Appearing

**Symptoms:** Actions performed but no logs in Tab 4

**Debugging:**
1. Check NIO service logs - are log messages received?
2. Check Hub - is it forwarding logs to Dashboard?
3. Verify all services are connecting to port 9091
4. Check NIO Selector loop - is it blocked?

**Solution:** Usually services not configured to log - check each service's log client

---

## 📊 SUCCESS CRITERIA

### Minimum Viable Demo (Must Pass)

- [ ] All 5 services start without errors
- [ ] Dashboard loads and connects to Hub
- [ ] Service Registry shows all 4 services
- [ ] At least 1 command works in each tab (API, Security, RMI)
- [ ] NIO Log Stream shows activity

### Complete Demo (Target)

- [ ] All 5 tabs fully functional
- [ ] Heartbeat timeout works
- [ ] Service recovery works
- [ ] Concurrent commands work
- [ ] All logs appear in real-time
- [ ] No errors during 10-minute demo
- [ ] Clean startup/shutdown

### Exceptional Demo (Stretch Goal)

- [ ] Multiple simultaneous commands
- [ ] Service failure recovery demonstrated live
- [ ] Performance metrics shown
- [ ] Code walkthrough prepared
- [ ] Architecture diagram displayed
- [ ] Q&A answers prepared

---

## 📝 DOCUMENTATION REQUIREMENTS

### Code Documentation

Each service needs:
- [ ] README.md with startup instructions
- [ ] Javadoc for key classes
- [ ] Protocol documentation
- [ ] Configuration guide

### System Documentation

- [ ] Architecture diagram (created)
- [ ] API documentation (protocols)
- [ ] Startup guide (this document)
- [ ] Troubleshooting guide (above)
- [ ] Individual member guides (created)

---

## 🎯 NEXT STEPS AFTER PHASE 7

1. **Testing Complete** → Record demo video (backup)
2. **All Tests Pass** → Create presentation slides
3. **Documentation Complete** → Final code review
4. **Pre-Demo Test** → Practice presentation (2-3 times)
5. **Ready for Presentation** → Deliver demo confidently!

---

## ✅ PHASE 7 COMPLETION CHECKLIST

**Integration:**
- [ ] All services connect to Hub successfully
- [ ] Dashboard connects to Hub via WebSocket
- [ ] Message broker routes commands correctly
- [ ] Results return to Dashboard correctly

**UI Testing:**
- [ ] Tab 1 (Service Registry) - All tests pass
- [ ] Tab 2 (API Gateway) - All tests pass
- [ ] Tab 3 (Security Test) - All tests pass
- [ ] Tab 4 (NIO Log Stream) - All tests pass
- [ ] Tab 5 (RMI Task Runner) - All tests pass

**End-to-End Testing:**
- [ ] Complete message flow test passes
- [ ] Concurrent commands test passes
- [ ] Service failure/recovery test passes
- [ ] System-wide logging test passes

**Demo Preparation:**
- [ ] Startup script created and tested
- [ ] Demo script written
- [ ] Pre-demo checklist complete
- [ ] Backup video recorded
- [ ] All documentation complete

**When all checkboxes are ✅, Phase 7 is COMPLETE!**
