# PHASE 2 BUILD & RUN INSTRUCTIONS

**Quick Reference - Copy & Paste Commands**

---

## 🏗️ ARCHITECTURE OVERVIEW

### Port Map

| Service               | Port | Protocol       | Purpose                                            |
| --------------------- | ---- | -------------- | -------------------------------------------------- |
| **Hub Server (TCP)**  | 7070 | TCP Socket     | Service registration, heartbeat, service discovery |
| **Hub Server (HTTP)** | 7071 | HTTP/WebSocket | REST API endpoints, dashboard WebSocket connection |
| **API Gateway**       | 9001 | HTTP/WebSocket | Health check, status, dashboard control WebSocket  |

**Important:** HTTP endpoints are on different ports than TCP registration!

---

## 🚀 QUICK START (5 MINUTES)

### Terminal 1: Build API Gateway Service

```powershell
cd d:\Projects\network programming - assignment\services\api-gateway-service
./build.ps1
```

Or using Maven directly:

```powershell
cd d:\Projects\network programming - assignment\services\api-gateway-service
mvn clean package
```

### Terminal 2: Start Hub Server (if not running)

```powershell
cd d:\Projects\network programming - assignment\services\hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### Terminal 3: Start API Gateway Service

```powershell
cd d:\Projects\network programming - assignment\services\api-gateway-service
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar
```

### Terminal 4: Test the Service

```powershell
# Make sure both services are running:
# - Hub Server: http://localhost:7070 (TCP) + http://localhost:7071 (HTTP)
# - API Gateway: http://localhost:9001 (HTTP/WebSocket)

# Quick HTTP tests:
Invoke-WebRequest -Uri "http://localhost:9001/health" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing

# OR run the automated test script:
cd d:\Projects\network programming - assignment\services\api-gateway-service
.\test-endpoints.ps1

# For WebSocket testing, see "Option 2" and "Option 4" below
```

---

## 📦 EXPECTED BUILD OUTPUT

```
[INFO] Scanning for projects...
[INFO]
[INFO] -----< com.example:api-gateway-service >-----
[INFO] Building API Gateway Service 1.0-SNAPSHOT
[INFO]
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ api-gateway-service ---
[INFO] Deleting d:\Projects\network programming - assignment\services\api-gateway-service\target
[INFO]
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ api-gateway-service ---
[INFO] Changes detected - recompiling module: api-gateway-service
[INFO] Compiling 4 source files to target\classes
[INFO]
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ api-gateway-service ---
[INFO]
[INFO] --- maven-assembly-plugin:3.6.0:single (default) @ api-gateway-service ---
[INFO]
[INFO] --- maven-shade-plugin:3.5.0:shade (default) @ maven-shade-plugin ---
[INFO] Replacing original artifact with shaded artifact.
[INFO]
[INFO] --- maven-install-plugin:3.1.1:install (default-install) @ api-gateway-service ---
[INFO]
[INFO] BUILD SUCCESS
[INFO]
[INFO] Total time: XX.XXs
[INFO] Artifact: target/api-gateway-service-1.0-SNAPSHOT.jar
```

---

## 🟢 EXPECTED RUNTIME OUTPUT

```
==============================================================================
  API GATEWAY SERVICE - PHASE 2, MEMBER 2
  Network Programming Group Assignment
==============================================================================

Core Concepts:
  ✓ HttpURLConnection: External API communication (Lesson 5)
  ✓ WebSocket: Real-time dashboard communication
  ✓ TCP Socket: Hub service registration
  ✓ Multithreading: Concurrent request handling

Service Endpoints:
  • WebSocket API: ws://localhost:9001/api
  • HTTP Health: http://localhost:9001/health
  • HTTP Status: http://localhost:9001/status
  • Hub TCP: localhost:7070

==============================================================================
[STARTUP] Step 1: Initializing External API Client...
[STARTUP] ✓ External API Client initialized

[STARTUP] Step 2: Connecting to Hub Server...
[HubClient] Attempting to connect to Hub at localhost:7070
[HubClient] Connected to Hub successfully
[HubClient] Sending: REGISTER::ApiGateway::localhost::9001
[HubClient] ✓ Service registered with Hub
[STARTUP] ✓ Connected to Hub successfully

[STARTUP] Step 3: Starting WebSocket Server...
[WebSocketServer] Starting on port 9001...
[WebSocketServer] ✓ WebSocket server started successfully
[WebSocketServer] Dashboard WebSocket: ws://localhost:9001/api
[WebSocketServer] Health endpoint: http://localhost:9001/health
[STARTUP] ✓ WebSocket Server started successfully

==============================================================================
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
==============================================================================

✓ Hub Registration: SUCCESS
✓ WebSocket Server: RUNNING on port 9001
✓ External API Client: READY

Features Available:
  • fetchWeather <city>: Get weather data from external API using HttpURLConnection
  • getServiceStatus: Get API Gateway service status
  • ping: Keep-alive check

Connected to Hub - heartbeats sent every 10 seconds
Waiting for dashboard connections on ws://localhost:9001/api

[ApiGatewayService] Press Ctrl+C to shutdown

[HubClient] Heartbeat sent to Hub
[HubClient] Heartbeat sent to Hub
...
```

---

## 🧪 EXPECTED TEST RESPONSES

### Test 1: Health Check

```powershell
curl http://localhost:9001/health
```

Response:

```json
{
  "status": "UP",
  "service": "ApiGateway",
  "port": 9001
}
```

### Test 2: Service Status

```powershell
curl http://localhost:9001/status
```

Response:

```json
{
  "service": "ApiGateway",
  "status": "Running",
  "connectedDashboards": 0,
  "timestamp": 1699600000000
}
```

### Test 3: Hub Services List

```powershell
# NOTE: Hub HTTP endpoints are on port 7071 (not 7070)
# Port 7070 is for TCP service registration only
curl http://localhost:7071/services
```

Response:

```json
{
  "services": [
    {
      "name": "ApiGateway",
      "host": "localhost",
      "port": 9001,
      "status": "online",
      "registered": "14:32:00"
    }
  ]
}
```

### Test 4: WebSocket Connection

**Option 1: Install wscat (Node.js required)**

```powershell
# Install wscat globally (one-time)
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:9001/api

# Send ping command:
> {"command": "ping"}
# Expected response:
< {"type":"PONG","timestamp":1699600000000}

# Send weather command:
> {"command": "fetchWeather", "city": "Colombo"}
# Expected response:
< {"type":"WEATHER_RESPONSE","location":"Colombo, Sri Lanka","temperature":28.5,"condition":"Sunny","timestamp":1699600000000,"status":"success"}
```

**Option 2: Use PowerShell WebSocket Client (No Installation Needed)**

```powershell
# Create and connect WebSocket
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$uri = [uri]"ws://localhost:9001/api"
$cancellation = New-Object System.Threading.CancellationToken
$ws.ConnectAsync($uri, $cancellation).Wait()

# Send ping command
$message = '{"command": "ping"}'
$bytes = [System.Text.Encoding]::UTF8.GetBytes($message)
$ws.SendAsync([System.ArraySegment[byte]]$bytes, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $cancellation).Wait()

# Receive response
$buffer = New-Object System.ArraySegment[byte] -ArgumentList (New-Object byte[] 1024)
$result = $ws.ReceiveAsync($buffer, $cancellation).Result
$response = [System.Text.Encoding]::UTF8.GetString($buffer.Array, 0, $result.Count)
Write-Host "Response: $response"

# Close connection
$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::Normal, "Closed", $cancellation).Wait()
$ws.Dispose()
```

**Option 3: Use Node.js with WebSocket Library (Quickest)**

```powershell
# Create a test.js file with:
# Copy the following code into test.js

const WebSocket = require('ws');
const ws = new WebSocket('ws://localhost:9001/api');

ws.on('open', () => {
  console.log('Connected to API Gateway');

  // Send ping
  ws.send(JSON.stringify({ command: 'ping' }));

  // Send weather request after 1 second
  setTimeout(() => {
    ws.send(JSON.stringify({ command: 'fetchWeather', city: 'Colombo' }));
  }, 1000);
});

ws.on('message', (data) => {
  console.log('Received:', JSON.parse(data));
});

ws.on('close', () => {
  console.log('Disconnected');
});

# Then run:
npm install ws
node test.js
```

**Option 4: Use Browser Developer Console (Easiest)**

1. Open any website in your browser
2. Press `F12` to open Developer Tools → Console tab
3. Paste this code:

```javascript
const ws = new WebSocket("ws://localhost:9001/api");

ws.onopen = () => {
  console.log("Connected to API Gateway");
  ws.send(JSON.stringify({ command: "ping" }));
};

ws.onmessage = (event) => {
  console.log("Response:", JSON.parse(event.data));
};

ws.onerror = (error) => {
  console.error("Error:", error);
};
```

---

## ✅ SUCCESS INDICATORS

When everything is working:

- ✅ Hub Server console shows: `[HUB] Ready to accept service connections`
- ✅ API Gateway console shows: `✓ API GATEWAY SERVICE STARTED SUCCESSFULLY`
- ✅ Hub console shows: `[REGISTRY] ✓ Service registered: [ApiGateway] localhost:9001`
- ✅ API Gateway console shows heartbeats every 10 seconds: `[HubClient] Heartbeat sent to Hub`
- ✅ curl health check returns HTTP 200
- ✅ Hub services endpoint includes ApiGateway
- ✅ WebSocket connection accepts commands and returns results

---

## 🐛 TROUBLESHOOTING

### Issue: "Protocol violation" error on curl http://localhost:7070/services

**Solution:** The HTTP REST endpoints are on **port 7071**, not 7070. Port 7070 is TCP-only for service registration.

```powershell
# WRONG - This will fail:
curl http://localhost:7070/services

# CORRECT - Use port 7071:
curl http://localhost:7071/services
curl http://localhost:7071/hub-status
```

**Hub Server Port Map:**

- **7070 (TCP)**: Service registry - for REGISTER/HEARTBEAT/DEREGISTER messages (TCP socket protocol)
- **7071 (HTTP)**: REST API & WebSocket - for dashboard and HTTP queries (Javalin server)

### Issue: Build fails with "pom.xml not found"

**Solution:** Make sure you're in the correct directory:

```powershell
cd d:\Projects\network programming - assignment\services\api-gateway-service
```

### Issue: "Connection refused" to Hub

**Solution:** Start Hub Server first:

```powershell
cd d:\Projects\network programming - assignment\services\hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### Issue: Port 9001 already in use

**Solution:** Kill the process:

```powershell
Get-NetTCPConnection -LocalPort 9001 | Select-Object OwningProcess | Foreach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

### Issue: Build hangs or times out

**Solution:** Clear Maven cache:

```powershell
rm -r ~/.m2/repository/com/example
mvn clean package -DskipTests
```

### Issue: Weather API returns error

**Solution:** Check your internet connection and try a simpler city name:

- Valid: "Colombo", "New York", "London"
- Invalid: "Co lo mbo", "NewYork" (with space)

---

## 📊 PROJECT STRUCTURE CREATED

```
services/api-gateway-service/
├── pom.xml                          ✅ Created
├── README.md                         ✅ Created
├── build.bat                         ✅ Created
├── build.ps1                         ✅ Created
├── src/main/java/com/example/apigateway/
│   ├── ApiGatewayService.java        ✅ Created (89 lines)
│   ├── HubClient.java                ✅ Created (142 lines)
│   ├── ExternalApiClient.java        ✅ Created (304 lines)
│   └── WebSocketServer.java          ✅ Created (208 lines)
└── src/test/java/com/example/apigateway/
    └── (Ready for test code)
```

**Total Starter Code: ~750 lines**

---

## 🎯 NEXT STEPS AFTER BUILD

1. ✅ Build successful
2. ✅ Test endpoints working
3. 🔜 **Next: Phase 2 Part A - React Dashboard Refactoring**
   - Modify Chat UI to Service UI
   - Connect dashboard to Hub WebSocket
   - Display registered services
4. 🔜 **Then: Phase 3-6 - Other Services**

---

## 💾 FILES CREATED FOR YOU

| File                   | Purpose                | Status     |
| ---------------------- | ---------------------- | ---------- |
| PHASE_2_START.md       | Quick start guide      | ✅ Created |
| PHASE_2_KICKOFF.md     | Implementation summary | ✅ Created |
| PHASE_2_BUILD_RUN.md   | Build instructions     | ✅ Created |
| pom.xml                | Maven config           | ✅ Created |
| README.md              | Service docs           | ✅ Created |
| build.ps1              | Build script           | ✅ Created |
| build.bat              | Build script           | ✅ Created |
| ApiGatewayService.java | Main class             | ✅ Created |
| HubClient.java         | Hub integration        | ✅ Created |
| ExternalApiClient.java | HttpURLConnection      | ✅ Created |
| WebSocketServer.java   | WebSocket server       | ✅ Created |

---

## 🚀 YOU'RE READY!

All components are ready. Just run the build script and start testing!

```powershell
# One command to build:
cd services/api-gateway-service && ./build.ps1
```

Then follow the test procedures above.

**Happy coding! 🎉**
