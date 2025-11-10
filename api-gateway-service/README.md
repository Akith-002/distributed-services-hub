# API Gateway Service - Phase 2, Member 2

## 🎯 Overview

The **API Gateway Service** is a microservice that acts as a bridge between the React Dashboard and external APIs. It demonstrates:

- **HttpURLConnection (Lesson 5):** Making HTTP requests to external services
- **WebSocket:** Real-time communication with the dashboard
- **Service Architecture:** Registration with central Hub, health monitoring
- **JSON Processing:** Parsing and responding with JSON data

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   API GATEWAY SERVICE                       │
│                     (Port 9001)                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────┐   ┌─────────────────────┐         │
│  │   HubClient         │   │  WebSocketServer    │         │
│  │  (TCP to Hub:7070)  │   │  (WS on :9001/api)  │         │
│  └──────────┬──────────┘   └────────────┬────────┘         │
│             │                           │                  │
│  - Register │                           │ - Handle commands
│  - Heartbeat│                           │ - Broadcast
│  - Deregister                          │                  │
│                                         │                  │
│  ┌─────────────────────────────────────▼──────────┐       │
│  │        ExternalApiClient                        │       │
│  │   (HttpURLConnection to APIs)                  │       │
│  │                                                 │       │
│  │  - fetchWeatherByCity(cityName)                │       │
│  │  - parseWeatherResponse(json)                  │       │
│  │  - Handle HTTP requests/responses              │       │
│  └─────────────────────────────────────────────────┘       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
         │                          │
         │                          │
         ▼                          ▼
    Hub Server              React Dashboard
   (Port 7070)              (WebSocket Client)
```

## 📦 Components

### 1. ApiGatewayService.java

**Main Entry Point** (89 lines)

Orchestrates startup and shutdown:

- Initializes ExternalApiClient
- Connects to Hub Server
- Starts WebSocket server
- Manages graceful shutdown

### 2. HubClient.java

**TCP Socket Connection to Hub** (142 lines)

Handles communication with Hub Server:

- Registers service on startup: `REGISTER::ApiGateway::localhost::9001`
- Sends heartbeat every 10 seconds: `HEARTBEAT::ApiGateway`
- Deregisters on shutdown: `DEREGISTER::ApiGateway`
- Uses ScheduledExecutorService for periodic tasks

### 3. ExternalApiClient.java

**HttpURLConnection Wrapper** (304 lines)

**Key Concepts: HttpURLConnection (Lesson 5)**

```java
// Create HTTP connection
URL url = new URL(apiUrl);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();

// Configure
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
conn.setRequestProperty("User-Agent", "...");

// Execute
int responseCode = conn.getResponseCode(); // 200 = OK

// Read response
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream()));
String response = reader.lines().collect(Collectors.joining("\n"));

// Cleanup
conn.disconnect();
```

**Features:**

- `fetchWeather(latitude, longitude)` - Get weather via HttpURLConnection
- `fetchWeatherByCity(cityName)` - Convenient city-based lookup
- JSON parsing with Gson
- Timeout handling (5 seconds)
- Proper resource cleanup

**External API Used:**

- **Open-Meteo:** https://api.open-meteo.com/v1/forecast
- Free weather data API (no authentication required)
- Returns current temperature and weather condition

### 4. WebSocketServer.java

**Javalin WebSocket Server** (208 lines)

Handles real-time communication with React Dashboard:

**Endpoints:**

- `GET /health` - Health check
- `GET /status` - Service status
- `WS /api` - WebSocket endpoint for dashboard

**Supported Commands:**

- `fetchWeather` - Get weather data from external API
- `getServiceStatus` - Get service status
- `ping` - Keep-alive check

**Message Protocol:**

Client → Server:

```json
{
  "command": "fetchWeather",
  "city": "Colombo"
}
```

Server → Client:

```json
{
  "type": "WEATHER_RESPONSE",
  "location": "Colombo, Sri Lanka",
  "temperature": 28.5,
  "condition": "Sunny",
  "status": "success",
  "timestamp": 1699600000000
}
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8.9 or higher
- Hub Server running on port 7070
- Internet connection (for external API calls)

### Build

**Option 1: PowerShell**

```powershell
cd services/api-gateway-service
./build.ps1
```

**Option 2: Batch**

```powershell
cd services/api-gateway-service
build.bat
```

**Option 3: Maven**

```powershell
cd services/api-gateway-service
mvn clean package
```

### Run

**Terminal 1: Start Hub Server**

```powershell
cd services/hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

**Terminal 2: Start API Gateway Service**

```powershell
cd services/api-gateway-service
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar
```

Expected startup sequence:

```
[STARTUP] Step 1: Initializing External API Client...
[STARTUP] ✓ External API Client initialized

[STARTUP] Step 2: Connecting to Hub Server...
[STARTUP] ✓ Connected to Hub successfully

[STARTUP] Step 3: Starting WebSocket Server...
[STARTUP] ✓ WebSocket Server started successfully

✓ Hub Registration: SUCCESS
✓ WebSocket Server: RUNNING on port 9001
✓ External API Client: READY
```

## 🧪 Testing

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

### Test 3: Hub Verification

```powershell
curl http://localhost:7070/services
```

Verify ApiGateway is registered:

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

Using wscat (Node.js):

```powershell
npm install -g wscat
wscat -c ws://localhost:9001/api
```

Send command:

```json
{ "command": "ping" }
```

Response:

```json
{ "type": "PONG", "timestamp": 1699600000000 }
```

### Test 5: Fetch Weather

Send command:

```json
{ "command": "fetchWeather", "city": "Colombo" }
```

Response:

```json
{
  "type": "WEATHER_RESPONSE",
  "location": "Colombo, Sri Lanka",
  "temperature": 28.5,
  "condition": "Sunny",
  "status": "success",
  "timestamp": 1699600000000
}
```

## 📊 API Endpoints

### HTTP Endpoints

| Endpoint  | Method | Purpose        |
| --------- | ------ | -------------- |
| `/health` | GET    | Health check   |
| `/status` | GET    | Service status |

### WebSocket Endpoint

| Endpoint | Protocol  | Purpose                 |
| -------- | --------- | ----------------------- |
| `/api`   | WebSocket | Dashboard communication |

## 🔌 Supported Cities for Weather

- Colombo (Sri Lanka)
- New York (USA)
- London (UK)
- Tokyo (Japan)
- Sydney (Australia)
- Paris (France)
- Dubai (UAE)
- Singapore

(Can be easily extended by adding coordinates to `getCityCoordinates()`)

## 🎓 Key Concepts Demonstrated

### 1. HttpURLConnection (Lesson 5)

```java
// Creating HTTP connection
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);

// Reading response
int code = conn.getResponseCode();
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream()));

// Parsing JSON
JsonObject json = JsonParser.parseString(response).getAsJsonObject();
```

### 2. WebSocket Communication

```java
app.ws("/api", ws -> {
    ws.onConnect(ctx -> { /* Handle new connection */ });
    ws.onMessage(ctx -> { /* Handle message */ });
    ws.onClose(ctx -> { /* Handle close */ });
});
```

### 3. Service Registration (Hub Integration)

```java
// Register with Hub
String registerMsg = "REGISTER::ApiGateway::localhost::9001";
socket.getOutputStream().println(registerMsg);

// Heartbeat
scheduler.scheduleAtFixedRate(() -> {
    out.println("HEARTBEAT::ApiGateway");
}, 10, 10, TimeUnit.SECONDS);
```

### 4. JSON Processing

```java
// Parse response
JsonObject json = JsonParser.parseString(response).getAsJsonObject();
double temp = json.get("current").getAsJsonObject()
    .get("temperature_2m").getAsDouble();

// Send response
JsonObject response = new JsonObject();
response.addProperty("temperature", temp);
ctx.send(response.toString());
```

## 🐛 Troubleshooting

### Connection to Hub Failed

- Verify Hub is running: `curl http://localhost:7070/hub-status`
- Check port 7070 is not blocked by firewall
- Restart both Hub and Gateway

### WebSocket Connection Refused

- Verify service is running: `curl http://localhost:9001/health`
- Check port 9001 is available: `netstat -ano | findstr :9001`
- Try restarting the service

### Weather API Returns Error

- Check internet connectivity
- Verify city name is spelled correctly
- Check Open-Meteo status: https://status.open-meteo.com/

### Port Already in Use

```powershell
# Find process using port
Get-NetTCPConnection -LocalPort 9001 | Select-Object OwningProcess

# Kill process
taskkill /PID <process-id> /F
```

## 📈 Dependencies

| Dependency | Version | Purpose                    |
| ---------- | ------- | -------------------------- |
| Javalin    | 5.6.3   | WebSocket server framework |
| Gson       | 2.10.1  | JSON parsing               |
| SLF4J      | 2.0.9   | Logging                    |
| JUnit      | 4.13.2  | Testing                    |

## 🔗 Integration Points

### Hub Server

- **Host:** localhost
- **Port:** 7070
- **Protocol:** TCP

### React Dashboard

- **Endpoint:** ws://localhost:9001/api
- **Protocol:** WebSocket (JSON)

### External APIs

- **Weather API:** https://api.open-meteo.com/v1/forecast
- **Method:** GET with parameters
- **Response:** JSON

## 📝 Future Enhancements

- [ ] Add caching for weather data
- [ ] Support additional external APIs
- [ ] Add rate limiting
- [ ] Add authentication/authorization
- [ ] Log all API calls to Log Service
- [ ] Add metrics and monitoring
- [ ] Support batch weather requests

## 📚 Related Documentation

- **IMPLEMENTATION_PLAN.md** - Overall project architecture
- **PHASE_1_COMPLETE.md** - Hub Server details
- **PHASE_2_START.md** - Phase 2 quick start guide

## ✅ Acceptance Criteria

- ✅ Service registers with Hub on startup
- ✅ Heartbeat sent every 10 seconds
- ✅ HttpURLConnection works for external APIs
- ✅ WebSocket endpoint operational
- ✅ Receives and processes commands correctly
- ✅ Returns accurate weather data
- ✅ Graceful shutdown and deregistration
- ✅ Comprehensive error handling

## 🎉 Next Steps

1. ✅ Build and run API Gateway Service
2. ✅ Verify hub registration
3. ✅ Test WebSocket endpoints
4. 🔜 Implement React Dashboard (Phase 2 Part A)
5. 🔜 Integrate with other services

---

**Status: READY FOR TESTING** ✅

For issues or questions, refer to **PHASE_2_START.md** for detailed troubleshooting.
