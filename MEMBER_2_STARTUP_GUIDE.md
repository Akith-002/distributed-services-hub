# Member 2 - API Gateway Service Startup & Demonstration Guide

**Your Component:** API Gateway Service + React Dashboard  
**Your Core Networking Concepts:** HttpURLConnection, HTTP GET requests, JSON parsing, React + WebSocket  
**Port:** 9001 (WebSocket), External API calls

---

## 📚 Java Network Programming Concepts You're Demonstrating

### 1. **HttpURLConnection** (Lesson 5) - CRITICAL: NO Third-Party Libraries
- Creating HTTP connections to external APIs using **ONLY** `java.net.HttpURLConnection`
- **NOT using** Apache HttpClient, OkHttp, or any third-party HTTP libraries
- This is the core requirement from Lesson 5

### 2. **HTTP Protocol**
- HTTP GET requests
- Request headers (User-Agent, Accept)
- Response codes (200 OK, 404 Not Found, etc.)
- Reading response streams

### 3. **URL & URI Handling**
- Constructing URLs with query parameters
- Encoding special characters
- Protocol handling (http:// vs https://)

### 4. **JSON Parsing**
- Manual JSON parsing (no Jackson/Gson libraries)
- String manipulation for JSON data
- Extracting nested JSON fields

### 5. **WebSocket Client** (Frontend)
- Real-time communication with Hub Server
- Sending commands and receiving results
- React integration

---

## 🚀 How to Start Your Services

You have **TWO parts** to demonstrate:
1. **Java Backend:** API Gateway Service (HttpURLConnection)
2. **React Frontend:** Dashboard UI (WebSocket)

### Part A: Start the Hub Server First
```powershell
cd distributed-services-hub\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```
**IMPORTANT:** Hub must be running first!

### Part B: Start Your API Gateway Service

#### Step 1: Navigate to Your Service Directory
```powershell
cd distributed-services-hub\api-gateway-service
```

#### Step 2: Build Your Service (First Time Only)
```powershell
.\build.ps1
```
Or manually:
```powershell
mvn clean package
```

#### Step 3: Run Your Service
```powershell
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
================================================================================
  API GATEWAY SERVICE - MEMBER 2
================================================================================

[HubClient] Connecting to Hub at localhost:7070...
[HubClient] Connected to Hub successfully
[HubClient] Sent registration: REGISTER::ApiGateway::localhost::9001

✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
✓ Hub Registration: SUCCESS
✓ WebSocket Server: RUNNING on port 9001
✓ Heartbeat: ACTIVE (every 10 seconds)
✓ External API Client: READY

Waiting for commands from Dashboard...
```

### Part C: Start Your React Dashboard

#### Step 1: Navigate to Dashboard Directory
```powershell
cd ..\..\multi-client-chat-frontend
```

#### Step 2: Install Dependencies (First Time Only)
```powershell
npm install
```

#### Step 3: Run Development Server
```powershell
npm run dev
```

**Expected Output:**
```
  VITE v5.0.0  ready in 500 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

#### Step 4: Open in Browser
Navigate to: **http://localhost:5173**

---

## 🎯 How to Demonstrate Your Work

### Method 1: Command Line Demonstration

#### Show HttpURLConnection Making Real API Calls

**Test your service from command line:**
```powershell
.\test-endpoints.ps1
```

**Or manually send a command:**
```powershell
# Connect to API Gateway WebSocket and send command
# This will be easier to show via the Dashboard UI
```

**Watch the API Gateway logs:**
```
[ApiGatewayService] Received command: get-weather, city: Colombo
[ExternalApiClient] Fetching weather for: Colombo
[ExternalApiClient] Connecting to: https://api.open-meteo.com/v1/forecast...
[ExternalApiClient] Response code: 200 OK
[ExternalApiClient] Weather data retrieved successfully
[ExternalApiClient] Temperature: 28.5°C
[ExternalApiClient] Wind Speed: 12.3 km/h
[ApiGatewayService] Sending result to Hub...
```

### Method 2: UI Demonstration (Recommended for Presentation)

This is the **BEST way** to demonstrate your work visually!

#### Navigate to Tab 2: API Gateway

1. **Open Dashboard:** http://localhost:5173
2. **Click on "API Gateway" tab** (Tab 2)
3. **You'll see a weather fetcher interface**

#### Demonstrate Weather API Calls

**Test Case 1: Colombo**
1. Type "Colombo" in the city input
2. Click "Fetch Weather"
3. **Show what happens:**
   - Command sent to Hub via WebSocket
   - Hub forwards to your API Gateway service
   - Your service uses HttpURLConnection to call Open-Meteo API
   - Weather data returned and displayed in UI
   - **POINT OUT:** No third-party HTTP libraries used!

**Test Case 2: London**
1. Type "London" in the city input
2. Click "Fetch Weather"
3. Show different weather data

**Test Case 3: New York**
1. Type "New York" in the city input
2. Click "Fetch Weather"
3. Show it works for any city

**What the UI Displays:**
```
Weather in Colombo:
Temperature: 28.5°C
Humidity: 75%
Wind Speed: 12.3 km/h
Conditions: Partly Cloudy
Fetched at: 2:30 PM
```

#### Show the Dashboard WebSocket Connection

Open browser console (F12) to show:
```javascript
WebSocket connection established: ws://localhost:7071/registry
Sending command: {"command_for": "API_GATEWAY", "payload": "get-weather:Colombo"}
Received result: {"result_from": "API_GATEWAY", "data": {...}}
```

---

## 🎓 Explaining Your Networking Concepts

### For Your Presentation/Demo, Explain:

#### 1. HttpURLConnection (The Main Concept)

**Code Reference in `ExternalApiClient.java`:**
```java
URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=6.9271&longitude=79.8612");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();

// Configure connection
conn.setRequestMethod("GET");
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
conn.setRequestProperty("User-Agent", "API-Gateway-Service/1.0");

// Make request
int responseCode = conn.getResponseCode(); // 200 = OK

// Read response
BufferedReader reader = new BufferedReader(
    new InputStreamReader(conn.getInputStream())
);
String response = reader.lines().collect(Collectors.joining("\n"));

// Clean up
conn.disconnect();
```

**Explain:**
- "I'm using `HttpURLConnection` from `java.net` package - NO third-party libraries"
- "This is the standard Java way to make HTTP requests, as taught in Lesson 5"
- "I create a URL object, open a connection, set headers, and read the response"
- "The response is a stream that I read line by line"

#### 2. HTTP GET Request

**Explain:**
- "HTTP GET is used to retrieve data from a server"
- "I set the request method to 'GET' using `setRequestMethod('GET')`"
- "The server responds with a status code (200 means success)"
- "I read the response body which contains JSON weather data"

#### 3. Request Headers

**Code Reference:**
```java
conn.setRequestProperty("User-Agent", "API-Gateway-Service/1.0");
conn.setRequestProperty("Accept", "application/json");
```

**Explain:**
- "User-Agent identifies my application to the server"
- "Accept tells the server I want JSON format response"
- "Some APIs require these headers to work properly"

#### 4. Response Handling

**Explain:**
- "First, I check the response code - 200 means success"
- "If 404, the resource wasn't found"
- "If 500, the server had an error"
- "I read the response using an InputStream and BufferedReader"
- "The response is JSON text that I parse manually"

#### 5. JSON Parsing (Manual)

**Code Reference:**
```java
private double parseTemperature(String json) {
    // Find "temperature_2m" in JSON
    int tempIndex = json.indexOf("\"temperature_2m\"");
    int colonIndex = json.indexOf(":", tempIndex);
    int commaIndex = json.indexOf(",", colonIndex);
    
    String tempValue = json.substring(colonIndex + 1, commaIndex).trim();
    return Double.parseDouble(tempValue);
}
```

**Explain:**
- "I parse JSON manually without Jackson or Gson libraries"
- "I search for specific field names in the JSON string"
- "Extract the value between colons and commas"
- "Convert string values to appropriate types (double, int, etc.)"

#### 6. WebSocket Communication (Dashboard)

**Explain:**
- "The React Dashboard connects to Hub Server via WebSocket"
- "WebSocket provides real-time bidirectional communication"
- "When user clicks 'Fetch Weather', Dashboard sends command to Hub"
- "Hub forwards command to my API Gateway service"
- "My service fetches weather and sends result back through Hub"
- "Dashboard receives result and updates UI in real-time"

---

## 📊 Demonstration Checklist

Use this checklist during your demonstration:

### Command Line Demo
- [ ] Start Hub Server successfully
- [ ] Start API Gateway Service successfully
- [ ] Show registration with Hub in logs
- [ ] Show heartbeat messages every 10 seconds
- [ ] Run test script: `.\test-endpoints.ps1`
- [ ] Show HttpURLConnection making API call in logs
- [ ] Show HTTP response code (200 OK)
- [ ] Show weather data in logs

### UI Demo (Tab 2: API Gateway)
- [ ] Open Dashboard at http://localhost:5173
- [ ] Navigate to Tab 2: "API Gateway"
- [ ] Show weather form interface
- [ ] Enter city: "Colombo"
- [ ] Click "Fetch Weather"
- [ ] Show result displayed in UI
- [ ] Test with "London"
- [ ] Test with "New York"
- [ ] Show multiple requests work
- [ ] Open browser console (F12)
- [ ] Show WebSocket messages being sent/received

### Technical Explanation Points
- [ ] Explain HttpURLConnection (Lesson 5)
- [ ] Emphasize NO third-party HTTP libraries
- [ ] Explain HTTP GET method
- [ ] Explain request headers (User-Agent, Accept)
- [ ] Explain response codes (200, 404, 500)
- [ ] Explain InputStream and BufferedReader
- [ ] Show manual JSON parsing
- [ ] Explain WebSocket communication flow
- [ ] Show Dashboard → Hub → Service → API → Service → Hub → Dashboard flow

---

## 🔧 Troubleshooting

### Issue: Port 9001 already in use
```powershell
# Find what's using the port
netstat -ano | findstr :9001

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Issue: Cannot connect to external API
- Check internet connection
- Check if Open-Meteo API is accessible: https://api.open-meteo.com/v1/forecast?latitude=6.9271&longitude=79.8612
- Check firewall settings
- Try pinging api.open-meteo.com

### Issue: Dashboard not showing weather
- Check API Gateway service is running
- Check Hub Server is running
- Check browser console for errors (F12)
- Verify WebSocket connection in console

### Issue: npm install fails
```powershell
# Clear cache and reinstall
npm cache clean --force
rm -r node_modules
npm install
```

---

## 📝 Key Points for Your Report

Include these in your written documentation:

### Architecture
- API Gateway Service acts as bridge between Dashboard and external APIs
- Uses HttpURLConnection for HTTP requests (Lesson 5 requirement)
- React Dashboard provides visual interface
- WebSocket enables real-time communication

### Networking Concepts

#### Java Backend (API Gateway Service)
1. **HttpURLConnection**: Standard Java HTTP client (no libraries)
2. **URL & URI**: Constructing API endpoints
3. **HTTP GET**: Retrieving data from REST APIs
4. **Request Headers**: User-Agent, Accept, etc.
5. **Response Handling**: Status codes, input streams
6. **JSON Parsing**: Manual string manipulation
7. **Socket Communication**: TCP connection to Hub

#### Frontend (React Dashboard)
1. **WebSocket**: Real-time bidirectional communication
2. **Event-driven UI**: React components responding to WebSocket messages
3. **Command Pattern**: Sending structured commands to services
4. **State Management**: React hooks for UI updates

### Why These Concepts Matter
- **Standard Library**: HttpURLConnection is part of Java SE (no dependencies)
- **Protocol Understanding**: Learn HTTP at a low level
- **Real-world Integration**: Connecting to external APIs
- **User Interface**: Visual demonstration of network concepts

---

## 🎬 Presentation Script Example

**1. Introduction (30 seconds)**
"I implemented the API Gateway Service and React Dashboard. The service demonstrates HttpURLConnection for HTTP requests, and the Dashboard provides a visual interface using WebSocket."

**2. Show Architecture (30 seconds)**
"The Dashboard sends commands via WebSocket to the Hub Server. The Hub forwards commands to my API Gateway Service. My service uses HttpURLConnection to call the Open-Meteo Weather API, then returns results through the Hub back to the Dashboard."

**3. Code Walkthrough (1 minute)**
"Here's the HttpURLConnection code. Notice I'm NOT using any third-party libraries - just java.net.HttpURLConnection as required in Lesson 5. I create a URL, open connection, set headers, read the response stream, and parse JSON manually."

**4. Live Demo (2 minutes)**
"Let me open the Dashboard... Navigate to the API Gateway tab... I'll fetch weather for Colombo... See the real-time result? The service made an HTTP GET request to Open-Meteo API using HttpURLConnection. Now let me try London... Different data. Let me show the browser console - you can see WebSocket messages being exchanged."

**5. Command Line Demo (1 minute)**
"In the service logs, you can see the HttpURLConnection making the API call, receiving HTTP 200 OK, and parsing the JSON response."

---

## 📚 Study References

Review these lessons before your demo:

- **Lesson 5:** HttpURLConnection, URL, HTTP Protocol, GET/POST requests
- **Lesson 3:** Socket, TCP communication (for Hub connection)
- **Lesson 11:** WebSocket basics (for Dashboard)

### Key HttpURLConnection Methods to Know:
- `url.openConnection()` - Creates connection
- `setRequestMethod("GET")` - Sets HTTP method
- `setRequestProperty(key, value)` - Sets headers
- `getResponseCode()` - Gets HTTP status code
- `getInputStream()` - Reads response body
- `disconnect()` - Closes connection

---

## ✅ Pre-Demo Checklist

Before your demonstration:

- [ ] Hub Server is running (port 7070/7071)
- [ ] API Gateway Service builds successfully (`.\build.ps1`)
- [ ] Can start API Gateway Service without errors
- [ ] Service registers with Hub
- [ ] Port 9001 is available
- [ ] Dashboard npm install completed
- [ ] Dashboard runs: `npm run dev`
- [ ] Can access Dashboard: http://localhost:5173
- [ ] Tab 2 (API Gateway) loads correctly
- [ ] Weather fetch works for at least 3 cities
- [ ] Internet connection is working
- [ ] Open-Meteo API is accessible
- [ ] You understand HttpURLConnection API
- [ ] You can explain why NO third-party HTTP libraries
- [ ] You can explain the entire flow: Dashboard → Hub → Service → API
- [ ] You practiced the demo at least once

---

## 🌟 Extra Credit Demonstrations

### Show Error Handling
```java
try {
    int code = conn.getResponseCode();
    if (code == 404) {
        return "Error: API endpoint not found";
    } else if (code == 500) {
        return "Error: Server error";
    }
} catch (IOException e) {
    return "Error: Network timeout - " + e.getMessage();
}
```

### Show Connection Timeouts
```java
conn.setConnectTimeout(5000);  // 5 second connection timeout
conn.setReadTimeout(5000);     // 5 second read timeout
```

### Show Multiple API Endpoints
If you added more API integrations, demonstrate them:
- Weather API (Open-Meteo)
- Any other public APIs you integrated

---

## 📊 What Makes Your Demo Stand Out

1. **Visual Proof:** Dashboard shows HttpURLConnection working in real-time
2. **No Libraries:** Pure Java HttpURLConnection (Lesson 5 requirement met)
3. **Real API:** Actually fetching live weather data from internet
4. **Two Parts:** Both backend (Java) and frontend (React) working together
5. **Complete Flow:** Dashboard → WebSocket → Hub → Service → HTTP API → Back

---

**Good luck with your demonstration! Show them how HttpURLConnection works without any third-party magic!**
