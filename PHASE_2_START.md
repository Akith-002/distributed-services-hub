# 🚀 PHASE 2: API Gateway Service - Quick Start Guide

**Date:** November 10, 2025  
**Phase:** 2 of 7  
**Member:** Member 2 (Network Programming - HttpURLConnection)  
**Status:** 🟢 Ready to Implement

---

## 📋 WHAT IS PHASE 2?

Phase 2 has **TWO PARTS**:

### Part B: API Gateway Service (NEW Microservice)

- ✅ **Currently being implemented**
- Connects to Hub Server
- Makes external API calls using **HttpURLConnection** (Lesson 5)
- Provides WebSocket endpoint for React Dashboard

### Part A: React Dashboard Refactoring

- Will implement after Part B
- Refactor existing Chat UI to Service Dashboard
- Connect to Hub via WebSocket
- Display registered services in real-time

---

## 🏗️ WHAT YOU'RE BUILDING

```
┌──────────────────────────────────────────┐
│    API GATEWAY SERVICE (Port 9001)       │
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ HubClient (TCP to Hub:7070)        │  │
│  │ - Register service                 │  │
│  │ - Send heartbeats (every 10s)      │  │
│  │ - Deregister on shutdown           │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ WebSocketServer (port 9001)        │  │
│  │ - Accept dashboard connections     │  │
│  │ - Handle fetchWeather command      │  │
│  │ - Send results back to dashboard   │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │ ExternalApiClient                  │  │
│  │ - HttpURLConnection to APIs        │  │
│  │ - Parse JSON responses             │  │
│  │ - Return weather data              │  │
│  └────────────────────────────────────┘  │
│                                          │
└──────────────────────────────────────────┘
         │                  │
         │                  │
         ▼                  ▼
    Hub Server      React Dashboard
   (Port 7070)      (WebSocket Client)
```

---

## 📦 PROJECT STRUCTURE

```
services/api-gateway-service/
├── pom.xml                          ← Maven configuration
├── README.md                         ← Service documentation
├── build.bat                         ← Windows build script
├── build.ps1                         ← PowerShell build script
│
└── src/main/java/com/example/apigateway/
    ├── ApiGatewayService.java        ← MAIN entry point (89 lines)
    ├── HubClient.java                ← TCP connection to Hub (142 lines)
    ├── ExternalApiClient.java        ← HttpURLConnection wrapper (304 lines)
    ├── WebSocketServer.java          ← Javalin WebSocket server (208 lines)
    └── security/                     ← (Optional SSL utilities)
```

**Total Starter Code:** ~750 lines ready for you

---

## ✅ PREREQUISITES

Before starting, ensure:

1. **Java 17+ installed**

   ```powershell
   java -version
   ```

2. **Maven 3.8.9+ installed**

   ```powershell
   mvn -version
   ```

3. **Hub Server running** (from Phase 1)

   ```powershell
   cd services/hub-server
   java -jar target/hub-server-1.0-SNAPSHOT.jar
   ```

4. **Internet connectivity** (for external API calls)

---

## 🔧 BUILD INSTRUCTIONS

### Option 1: Using PowerShell Script (Recommended for Windows)

```powershell
cd services/api-gateway-service
./build.ps1
```

### Option 2: Using Maven

```powershell
cd services/api-gateway-service
mvn clean package
```

### Option 3: Using Batch Script

```powershell
cd services/api-gateway-service
./build.bat
```

---

## 🚀 RUN THE SERVICE

### Start Hub Server First (if not already running)

**Terminal 1:**

```powershell
cd services/hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

Expected output:

```
==============================================================================
  DISTRIBUTED SERVICES HUB - CENTRAL REGISTRY
  Member 1 - Multithreading & Concurrency Implementation
==============================================================================

Listening Ports:
  • TCP Service Registry: localhost:7070
  • WebSocket (Dashboard): ws://localhost:7070/registry
  • Status API: http://localhost:7070/hub-status

==============================================================================
[HUB] Ready to accept service connections
```

### Start API Gateway Service

**Terminal 2:**

```powershell
cd services/api-gateway-service
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar
```

Expected output:

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
[STARTUP] ✓ Connected to Hub successfully

[STARTUP] Step 3: Starting WebSocket Server...
[STARTUP] ✓ WebSocket Server started successfully

==============================================================================
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
==============================================================================

✓ Hub Registration: SUCCESS
✓ WebSocket Server: RUNNING on port 9001
✓ External API Client: READY

Features Available:
  • fetchWeather <city>: Get weather data from external API
  • getServiceStatus: Get API Gateway service status
  • ping: Keep-alive check

Connected to Hub - heartbeats sent every 10 seconds
Waiting for dashboard connections on ws://localhost:9001/api
```

---

## 🧪 TESTING THE SERVICE

### Test 1: Check Health Endpoint

**Terminal 3:**

```powershell
curl http://localhost:9001/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "ApiGateway",
  "port": 9001
}
```

### Test 2: Check Service Status

```powershell
curl http://localhost:9001/status
```

Expected response:

```json
{
  "service": "ApiGateway",
  "status": "Running",
  "connectedDashboards": 0,
  "timestamp": 1699600000000
}
```

### Test 3: Verify Registration on Hub

```powershell
curl http://localhost:7070/services
```

Expected response (should include ApiGateway):

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

### Test 4: Test WebSocket Connection

**Using wscat (Node.js tool):**

```powershell
npm install -g wscat
wscat -c ws://localhost:9001/api
```

Then send commands:

```json
{ "command": "ping" }
```

Expected response:

```json
{ "type": "PONG", "timestamp": 1699600000000 }
```

### Test 5: Test Fetch Weather Command

Send via WebSocket:

```json
{ "command": "fetchWeather", "city": "Colombo" }
```

Expected response:

```json
{
  "type": "WEATHER_RESPONSE",
  "location": "Colombo, Sri Lanka",
  "temperature": 28.5,
  "condition": "Sunny",
  "timestamp": 1699600000000,
  "status": "success"
}
```

---

## 📚 UNDERSTANDING THE CODE

### 1. HubClient.java - TCP Socket Connection

```java
// Register with Hub
String registerMsg = "REGISTER::ApiGateway::localhost::9001";
out.println(registerMsg);

// Send heartbeat every 10 seconds
scheduler.scheduleAtFixedRate(() -> {
    out.println("HEARTBEAT::ApiGateway");
}, 10, 10, TimeUnit.SECONDS);

// Deregister on shutdown
out.println("DEREGISTER::ApiGateway");
```

**Key Concepts:**

- TCP Socket for service registration (Lesson 3)
- ScheduledExecutorService for periodic heartbeats (Lesson 6)
- Try-with-resources for proper resource cleanup

### 2. ExternalApiClient.java - HttpURLConnection

```java
// Create HTTP connection (Lesson 5)
URL url = new URL(urlString);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();

// Configure connection
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
conn.setRequestProperty("User-Agent", "...");

// Get response
int responseCode = conn.getResponseCode(); // HTTP_OK = 200

// Read response
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream()));
String jsonResponse = reader.lines()
    .collect(Collectors.joining("\n"));

// Parse JSON
JsonObject json = JsonParser.parseString(jsonResponse)
    .getAsJsonObject();
double temperature = json.get("current")
    .getAsJsonObject()
    .get("temperature_2m")
    .getAsDouble();
```

**Key Concepts:**

- HttpURLConnection for HTTP requests (Lesson 5)
- Timeout handling
- JSON parsing with Gson
- Proper resource cleanup with try-finally

### 3. WebSocketServer.java - Real-time Communication

```java
// WebSocket endpoint
app.ws("/api", ws -> {
    ws.onConnect(ctx -> {
        // Track connected dashboards
        sessions.add(ctx);
    });

    ws.onMessage(ctx -> {
        // Handle incoming command
        JsonObject json = gson.fromJson(ctx.message(), JsonObject.class);
        String command = json.get("command").getAsString();

        if (command.equals("fetchWeather")) {
            ExternalApiClient.WeatherData data =
                apiClient.fetchWeatherByCity(city);
            ctx.send(createResponse(data));
        }
    });
});
```

**Key Concepts:**

- WebSocket for real-time bidirectional communication
- Thread-safe session management (CopyOnWriteArraySet)
- JSON serialization/deserialization

---

## 🎯 SUPPORTED COMMANDS

### Fetch Weather

**Request:**

```json
{
  "command": "fetchWeather",
  "city": "Colombo"
}
```

**Supported Cities:**

- Colombo (Sri Lanka)
- New York (USA)
- London (UK)
- Tokyo (Japan)
- Sydney (Australia)
- Paris (France)
- Dubai (UAE)
- Singapore

**Response:**

```json
{
  "type": "WEATHER_RESPONSE",
  "location": "Colombo, Sri Lanka",
  "temperature": 28.5,
  "condition": "Sunny",
  "timestamp": 1699600000000,
  "status": "success"
}
```

### Get Service Status

**Request:**

```json
{ "command": "getServiceStatus" }
```

**Response:**

```json
{
  "type": "SERVICE_STATUS",
  "service": "ApiGateway",
  "status": "online",
  "port": 9001,
  "connectedDashboards": 1,
  "timestamp": 1699600000000
}
```

### Ping

**Request:**

```json
{ "command": "ping" }
```

**Response:**

```json
{
  "type": "PONG",
  "timestamp": 1699600000000
}
```

---

## 🔌 INTEGRATION POINTS

### Hub Server Integration

- **Host:** localhost
- **Port:** 7070
- **Protocol:** TCP

**Messages:**

- `REGISTER::ApiGateway::localhost::9001` (on startup)
- `HEARTBEAT::ApiGateway` (every 10 seconds)
- `DEREGISTER::ApiGateway` (on shutdown)

### React Dashboard Integration

- **WebSocket Endpoint:** ws://localhost:9001/api
- **Protocol:** JSON messages

**Expected flow:**

1. Dashboard connects to ws://localhost:9001/api
2. Dashboard sends: `{"command": "fetchWeather", "city": "Colombo"}`
3. ApiGateway calls external API via HttpURLConnection
4. Dashboard receives: `{"type": "WEATHER_RESPONSE", "temperature": 28.5, ...}`

---

## 📊 EXTERNAL API DETAILS

### Open-Meteo Weather API

- **URL:** https://api.open-meteo.com/v1/forecast
- **Method:** GET
- **Authentication:** None (free API)
- **Response Format:** JSON

**Example Request:**

```
GET https://api.open-meteo.com/v1/forecast?latitude=6.9271&longitude=80.7789&current=temperature_2m,weather_code
```

**Example Response:**

```json
{
  "current": {
    "temperature_2m": 28.5,
    "weather_code": 0
  }
}
```

---

## 🐛 TROUBLESHOOTING

### Issue: "Connection refused" to Hub

**Solution:**

- Make sure Hub Server is running on port 7070
- Check: `netstat -ano | findstr :7070`
- Start Hub: `cd services/hub-server && java -jar target/hub-server-1.0-SNAPSHOT.jar`

### Issue: WebSocket not connecting

**Solution:**

- Verify API Gateway is running on port 9001
- Check: `curl http://localhost:9001/health`
- Ensure no firewall blocking port 9001

### Issue: Weather API returns error

**Solution:**

- Check internet connectivity
- Verify city name is in supported list (see above)
- Check Open-Meteo API status: https://status.open-meteo.com/

### Issue: Port already in use

**Solution:**

- Kill process on port 9001:

  ```powershell
  # Windows
  netstat -ano | findstr :9001
  taskkill /PID <PID> /F

  # Or find and kill
  Get-Process -Id (Get-NetTCPConnection -LocalPort 9001).OwningProcess | Stop-Process
  ```

### Issue: Maven build fails

**Solution:**

- Clear Maven cache: `mvn clean`
- Rebuild: `mvn clean package`
- Ensure Java 17+: `java -version`
- Ensure Maven 3.8.9+: `mvn -version`

---

## 📈 NEXT STEPS

1. ✅ Build API Gateway Service
2. ✅ Start Hub Server (if not running)
3. ✅ Start API Gateway Service
4. ✅ Verify connections with curl/wscat
5. 🔜 Implement Part A: React Dashboard Refactoring
6. 🔜 Integration testing

---

## 🎓 LEARNING OUTCOMES

After completing Phase 2 Part B, you'll understand:

✅ **HttpURLConnection (Lesson 5)**

- HTTP request/response handling
- JSON parsing and serialization
- Timeout configuration
- Proper resource cleanup

✅ **WebSocket Communication**

- Real-time bidirectional data transfer
- Connection lifecycle management
- Broadcast to multiple clients

✅ **Service Architecture**

- How services communicate with central registry
- Health monitoring via heartbeats
- Graceful startup/shutdown

✅ **External API Integration**

- Calling real-world APIs
- Parsing real data
- Error handling for network calls

---

## 📝 DELIVERABLES

By end of Phase 2 Part B:

- ✅ Standalone API Gateway microservice
- ✅ HTTP endpoint for health checks
- ✅ WebSocket endpoint for dashboard communication
- ✅ HttpURLConnection implementation for external APIs
- ✅ Hub registration and heartbeat mechanism
- ✅ Comprehensive logging and error handling
- ✅ Working build with Maven

---

## 🚀 START NOW!

```powershell
# 1. Navigate to service directory
cd services/api-gateway-service

# 2. Build
mvn clean package

# 3. Run (ensure Hub is running)
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar

# 4. Test in another terminal
curl http://localhost:9001/health
```

---

## 📞 SUPPORT

- Check **README.md** in service directory for detailed documentation
- Review **IMPLEMENTATION_PLAN.md** for overall architecture
- Check **PHASE_1_COMPLETE.md** for Hub integration details

---

**Status: READY TO IMPLEMENT PHASE 2 PART B** ✅

Good luck! Happy coding! 🎉
