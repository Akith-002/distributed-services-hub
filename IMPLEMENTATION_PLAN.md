# Distributed Services Hub - Implementation Plan

## Evolution from Chat Application to Microservices Architecture

**Date:** November 10, 2025  
**Project:** Network Programming Group Assignment - 5 Person Team  
**Current Status:** Existing chat application (WebSocket + SSL + NIO file transfer) ready for refactoring

---

## 1. PROJECT OVERVIEW & ARCHITECTURE TRANSFORMATION

### Current State

- ✅ **Secure WebSocket Chat Server** (Java, port 7070/7443)
  - Uses Javalin framework with SSL/TLS support
  - Handles multiple concurrent WebSocket connections
  - Basic file upload/download with NIO
  - Message history and user management
- ✅ **React Frontend Dashboard**
  - Modern UI for chat application
  - WebSocket client with SSL support
  - File upload modal functionality
  - Real-time user list updates

### Target State

**Distributed Services Hub** with the following components:

```
┌─────────────────────────────────────────────────────────────┐
│              THE HUB (Central Registry & Message Broker)     │
│  ├─ Service Registry (TCP Server + Concurrency)             │
│  ├─ Heartbeat Monitor (30-second timeout)                   │
│  ├─ WebSocket Broadcaster (Sends service updates)           │
│  ├─ Command Router (Forwards commands to services)          │
│  ├─ Result Aggregator (Receives results from services)      │
│  └─ RESTful API (Service status, statistics)                │
└────┬─────────────────────────────────────────────────────────┘
     │
     ├── Connected to React Dashboard (UI with Service Tabs)
     │   ├─ Service Registry Tab (Live service status)
     │   ├─ API Gateway Tab (Weather fetch demo)
     │   ├─ Security Test Tab (JSSE connection demo)
     │   ├─ NIO Log Stream Tab (Real-time log viewer)
     │   └─ RMI Task Runner Tab (Remote task execution)
     │
     ├── Connected to API Gateway Service (HttpURLConnection)
     │
     ├── Connected to Secure File Service (JSSE/SSLServerSocket)
     │
     ├── Connected to NIO Log Service (Selector-based Logging)
     │
     └── Connected to RMI Task Runner (Remote Method Invocation)
```

**Core Architecture Pattern: Message Broker**

The Hub acts as a central message broker:

1. **Dashboard → Hub → Service**: Dashboard sends commands via WebSocket to Hub, Hub forwards to appropriate service
2. **Service → Hub → Dashboard**: Services send results back to Hub, Hub broadcasts to Dashboard
3. **Clean Separation**: Dashboard only communicates with Hub, never directly with services

---

## 2. UI DEMONSTRATION STRATEGY

### The Core Idea: Hub as Message Broker

**Problem:** Each member needs a visual demonstration of their networking concept on the Dashboard, not just CLI output.

**Solution:** The Hub (Member 1) becomes a **Message Broker** that routes commands and results between the Dashboard and all services.

### Message Flow Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     REACT DASHBOARD                          │
│  ┌──────┬──────┬──────┬──────┬──────┐                      │
│  │ Tab1 │ Tab2 │ Tab3 │ Tab4 │ Tab5 │                      │
│  └──────┴──────┴──────┴──────┴──────┘                      │
└───────────────────┬──────────────────────────────────────────┘
                    │ WebSocket (Commands & Results)
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                   HUB (MESSAGE BROKER)                       │
│  ┌──────────────┐    ┌──────────────┐                      │
│  │ Command      │    │ Result       │                      │
│  │ Router       │◄───┤ Aggregator   │                      │
│  └──────┬───────┘    └──────▲───────┘                      │
└─────────┼──────────────────┼──────────────────────────────┘
          │                  │
          │ TCP Commands     │ TCP Results
          ▼                  │
┌─────────────────────────────┼──────────────────────────────┐
│  API Gateway ───────────────┘                               │
│  JSSE Service ──────────────┐                               │
│  NIO Log Service ────────────┤                               │
│  RMI Task Service ───────────┘                               │
└──────────────────────────────────────────────────────────────┘
```

### Communication Patterns

**Pattern 1: Dashboard → Service (Command)**

1. User clicks button on Dashboard (e.g., "Fetch Weather")
2. Dashboard sends WebSocket message to Hub: `{"command_for": "API_GATEWAY", "payload": "get-weather"}`
3. Hub routes command to API Gateway service via TCP
4. API Gateway executes its core task (HttpURLConnection)
5. API Gateway sends result to Hub: `{"result_from": "API_GATEWAY", "data": "..."}`
6. Hub broadcasts result to all Dashboard clients via WebSocket
7. Dashboard displays result in appropriate tab

**Pattern 2: Service → Dashboard (Proactive Update)**

1. NIO Log Service receives a log message from any service
2. NIO Service forwards to Hub: `{"result_from": "NIO_SERVICE", "data": "LOG: ..."}`
3. Hub broadcasts to Dashboard
4. Dashboard appends to log stream tab

### Why This Architecture Works

✅ **Clean Separation:** Dashboard only talks to Hub, never directly to services  
✅ **Consistent Pattern:** All services use same command/result format  
✅ **Real Networking:** Each member still implements their core concept (HttpURLConnection, SSLServerSocket, NIO, RMI)  
✅ **Visual Demonstration:** Every member gets a dedicated tab showing their work  
✅ **No CLI Required:** Everything visible in browser interface

### Dashboard Tab Mapping

| Tab # | Name             | Owner    | Core Concept Demonstrated         | Button/Trigger             |
| ----- | ---------------- | -------- | --------------------------------- | -------------------------- |
| 1     | Service Registry | Member 1 | Multithreading, ConcurrentHashMap | Auto-updates (no button)   |
| 2     | API Gateway      | Member 2 | HttpURLConnection                 | "Fetch Weather" button     |
| 3     | Security Test    | Member 3 | SSLServerSocket, JSSE             | "Run Security Test" button |
| 4     | NIO Log Stream   | Member 4 | Java NIO, Selector                | Auto-updates (no button)   |
| 5     | RMI Task Runner  | Member 5 | Java RMI                          | "Execute Task" button      |

---

## 3. DETAILED BREAKDOWN BY MEMBER

### MEMBER 1: HUB SERVER (Multithreading & Concurrency)

**Core Concepts:** ServerSocket, Thread-per-Client, ExecutorService, ConcurrentHashMap, Heartbeat Mechanism

#### Current Code Location

- Main: `secure-websocket-chat/src/main/java/com/Itfac/TestNGLab/chat/ChatServer.java`
- Handler: `secure-websocket-chat/src/main/java/com/Itfac/TestNGLab/chat/WebSocketHandler.java`

#### Transformation Tasks

1. **Refactor ChatServer → HubServer**

   - File: Rename to `HubServer.java`
   - Keep: SSL/TLS setup, Javalin WebSocket endpoint
   - Change: Instead of handling chat messages, handle service REGISTER/DEREGISTER/HEARTBEAT messages
   - Port: Remain at 7070/7443

2. **Define Hub Protocol - Extended for Message Broker**

   ```
   Registration Protocol:
   - REGISTER::ApiGateway::localhost::9001
   - DEREGISTER::ApiGateway
   - HEARTBEAT::ApiGateway
   - FETCH_SERVICES (to get all registered services)

   Command/Result Protocol (JSON over WebSocket):

   Dashboard → Hub → Service (Commands):
   {
     "command_for": "API_GATEWAY",
     "payload": "get-weather"
   }

   Service → Hub → Dashboard (Results):
   {
     "result_from": "API_GATEWAY",
     "data": "{...weather_json...}"
   }

   Supported Services:
   - API_GATEWAY
   - JSSE_SERVICE
   - NIO_SERVICE
   - RMI_SERVICE
   ```

3. **Service Registry Data Structure**

   ```java
   public class ServiceRegistry {
       // ConcurrentHashMap<ServiceName, ServiceInfo>
       // ServiceInfo: {name, host, port, status, lastHeartbeat, metadata}
       // MUST use ConcurrentHashMap for thread-safety (Lesson 6)
   }
   ```

4. **Thread Pool for Service Connections**

   - Use `ExecutorService` (Lesson 6) instead of one thread per service
   - Recommended: `Executors.newFixedThreadPool(20)` or `newCachedThreadPool()`
   - Each service connection handler runs in a separate thread
   - Handler listens for HEARTBEAT/DEREGISTER messages

5. **Heartbeat Mechanism**

   - Each service must send HEARTBEAT every 10 seconds
   - Hub tracks `lastHeartbeat` timestamp for each service
   - Separate heartbeat monitor thread: Every 5 seconds, scan registry
   - If `(currentTime - lastHeartbeat) > 30 seconds`, remove service and broadcast update
   - Implementation: Use `ScheduledExecutorService` for periodic heartbeat checks

6. **Message Broker Implementation (NEW CORE FEATURE)**

   a. **Command Router - Dashboard to Services**

   - Listen for WebSocket messages from Dashboard
   - Parse command format: `{"command_for": "SERVICE_NAME", "payload": "..."}`
   - Lookup service in `ConcurrentHashMap<ServiceName, Socket>`
   - Forward payload to the correct service's TCP connection
   - Example:
     ```java
     void routeCommand(JsonObject command) {
         String targetService = command.getString("command_for");
         String payload = command.getString("payload");

         ServiceInfo service = serviceRegistry.get(targetService);
         if (service != null && service.socket != null) {
             PrintWriter out = new PrintWriter(service.socket.getOutputStream());
             out.println(payload);
             out.flush();
         }
     }
     ```

   b. **Result Aggregator - Services to Dashboard**

   - Each service connection handler listens for result messages
   - Parse result format: `{"result_from": "SERVICE_NAME", "data": "..."}`
   - Broadcast result to all connected Dashboard clients via WebSocket
   - Example:
     ```java
     void handleServiceResult(String serviceMessage) {
         JsonObject result = Json.parse(serviceMessage);
         broadcastToAllDashboards(result);
     }
     ```

   c. **WebSocket Broadcaster Enhancement**

   - When service list changes (JOIN/LEAVE/TIMEOUT), broadcast JSON:
     ```json
     {
       "type": "SERVICE_REGISTRY_UPDATE",
       "payload": {
         "services": [
           {
             "name": "ApiGateway",
             "host": "localhost",
             "port": 9001,
             "status": "online"
           }
         ]
       }
     }
     ```
   - When service results arrive, broadcast:
     ```json
     {
       "type": "SERVICE_RESULT",
       "result_from": "API_GATEWAY",
       "data": "{...}"
     }
     ```

7. **Logging & Demo Output**
   - Console should show:
     ```
     [HUB] Starting Service Registry on port 7070...
     [HUB] ApiGateway registered: localhost:9001
     [HUB] SecureFileService registered: localhost:9090
     [HUB] NioLogService registered: localhost:9091
     [HUB] TaskService (RMI) registered: rmi://localhost:1099/TaskService
     [HUB] NioLogService heartbeat timeout - DEREGISTERED
     [HUB] Broadcasting updated service list to dashboards...
     ```

#### UI Demonstration for Member 1

**Your UI Demo:** The "Service Registry" tab on the Dashboard is your primary demonstration. This proves your concurrent `ConcurrentHashMap` and multithreading architecture works.

**Live Demo Steps:**

1. Start Hub Server
2. As other members start their services, the Service Registry tab updates in real-time
3. When services timeout, they disappear from the list automatically
4. Multiple concurrent service registrations are handled without blocking

**Your UI Contribution:** This tab is 100% your work - it demonstrates your core networking concepts (multithreading, concurrency, service registry).

#### Acceptance Criteria

- ✅ Multiple services can connect simultaneously (concurrency tested)
- ✅ Services register, heartbeat, and deregister properly
- ✅ Hub acts as message broker - routes commands from Dashboard to services
- ✅ Hub forwards service results back to Dashboard
- ✅ Dashboard receives and displays service updates live
- ✅ Heartbeat timeout removes dead services automatically
- ✅ Command routing works for all service types (API, JSSE, NIO, RMI)

---

### MEMBER 2: NETWORK DASHBOARD & API GATEWAY SERVICE

**Core Concepts:** HttpURLConnection (Lesson 5), WebSocket Client, REST API

#### Part A: React Dashboard Refactoring

**Current Code Location**

- `frontend/src/App.jsx`
- `frontend/src/components/ChatRoom.jsx`

**Transformation Tasks**

1. **Create Multi-Tab Dashboard Layout**

   - Implement tabbed interface with 5 tabs:
     - **Tab 1: Service Registry** (Member 1's demo)
     - **Tab 2: API Gateway** (Member 2's demo)
     - **Tab 3: Security Test** (Member 3's demo)
     - **Tab 4: NIO Log Stream** (Member 4's demo)
     - **Tab 5: RMI Task Runner** (Member 5's demo)

2. **Tab 1: Service Registry (Member 1's Demo)**

   - Component: `ServiceRegistry.jsx`
   - Display: Real-time list of registered services
   - Columns:
     - Service Name
     - Host:Port
     - Status (Online/Offline/Timeout)
     - Last Heartbeat
   - Updates automatically when services join/leave

3. **Tab 2: API Gateway Demo (Member 2's Demo)**

   - Component: `ExternalDataFetcher.jsx`
   - UI Elements:
     - Button: "Fetch Weather from API"
     - Input field for city/location (optional)
     - Display area for weather results
   - Logic:

     ```jsx
     function fetchWeather() {
       const command = {
         command_for: "API_GATEWAY",
         payload: "get-weather",
       };
       websocket.send(JSON.stringify(command));
     }

     // On result received:
     if (msg.type === "SERVICE_RESULT" && msg.result_from === "API_GATEWAY") {
       setWeatherData(JSON.parse(msg.data));
     }
     ```

4. **Tab 3: Security Test Demo (Member 3's Demo)**

   - Component: `SecurityTestPanel.jsx`
   - UI Elements:
     - Button: "Run Security Connection Test"
     - Display area for test results (2 test outcomes)
   - Logic:

     ```jsx
     function runSecurityTest() {
       const command = {
         command_for: "JSSE_SERVICE",
         payload: "run-test",
       };
       websocket.send(JSON.stringify(command));
     }

     // On result received:
     if (msg.result_from === "JSSE_SERVICE") {
       addTestResult(msg.data); // Shows FAILED or SUCCESS
     }
     ```

5. **Tab 4: NIO Log Stream (Member 4's Demo)**

   - Component: `NioLogStream.jsx`
   - UI Elements:
     - Auto-scrolling text area (like a terminal)
     - Clear button
   - Logic:
     ```jsx
     // On log received:
     if (msg.result_from === "NIO_SERVICE") {
       appendLog(msg.data); // Append to text area
       scrollToBottom();
     }
     ```

6. **Tab 5: RMI Task Runner (Member 5's Demo)**

   - Component: `RmiTaskRunner.jsx`
   - UI Elements:
     - Dropdown: Select task (calculate-pi, fibonacci-10, etc.)
     - Button: "Execute Remote Task"
     - Display area for task result
   - Logic:

     ```jsx
     function executeTask(taskName) {
       const command = {
         command_for: "RMI_SERVICE",
         payload: taskName,
       };
       websocket.send(JSON.stringify(command));
     }

     // On result received:
     if (msg.result_from === "RMI_SERVICE") {
       setTaskResult(msg.data);
     }
     ```

7. **Modify WebSocket Message Handler**

   ```jsx
   switch (msg.type) {
     case "SERVICE_REGISTRY_UPDATE":
       setServices(msg.payload.services);
       break;

     case "SERVICE_RESULT":
       handleServiceResult(msg.result_from, msg.data);
       break;
   }
   ```

#### Part B: Java API Gateway Service

**New Java Service**

1. **Create New Module Structure**

   ```
   services/
   └── api-gateway-service/
       ├── pom.xml
       ├── src/main/java/com/example/apigateway/
       │   ├── ApiGatewayService.java (MAIN - connects to Hub)
       │   ├── HubClient.java (TCP client to Hub)
       │   ├── CommandListener.java (Listens for commands from Hub)
       │   └── ExternalApiClient.java (HttpURLConnection wrapper)
       └── src/main/resources/
           └── application.properties
   ```

2. **Startup: Register with Hub**

   ```java
   // On startup:
   String registerMsg = "REGISTER::API_GATEWAY::localhost::9001";
   // Send to Hub on port 7070
   // Also start heartbeat thread every 10 seconds
   ```

3. **Implement HttpURLConnection Client (CORE CONCEPT)**

   - Endpoint: Weather API (e.g., Open-Meteo free API or similar)
   - Method: `fetchWeatherData(String city) → WeatherData`
   - Use `HttpURLConnection` to:
     - Create URL connection
     - Set request method (GET)
     - Set headers (User-Agent, etc.)
     - Read response as JSON
     - Parse JSON response
   - Example:

     ```java
     URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=...");
     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
     conn.setRequestMethod("GET");
     conn.setConnectTimeout(5000);
     conn.setReadTimeout(5000);

     BufferedReader reader = new BufferedReader(...);
     String response = reader.lines().collect(Collectors.joining());
     JSONObject json = new JSONObject(response);
     ```

4. **Command Listener for Hub Communication**

   - Maintain persistent TCP connection to Hub
   - Listen for commands forwarded by Hub: `"get-weather"`
   - When command received:
     1. Execute `fetchWeatherData()` using HttpURLConnection
     2. Format result as JSON
     3. Send back to Hub: `{"result_from": "API_GATEWAY", "data": "{temperature: 28.5, ...}"}`
   - Example:
     ```java
     class CommandListener implements Runnable {
         private Socket hubConnection;

         public void run() {
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(hubConnection.getInputStream()));
             PrintWriter out = new PrintWriter(hubConnection.getOutputStream());

             while (true) {
                 String command = in.readLine();
                 if ("get-weather".equals(command)) {
                     String weatherData = fetchWeatherData();
                     String result = "{\"result_from\": \"API_GATEWAY\", \"data\": "
                                   + weatherData + "}";
                     out.println(result);
                     out.flush();
                 }
             }
         }
     }
     ```

5. **Logging Integration**
   - Connect to Member 4's Log Service on port 9091
   - Send log messages like:
     ```
     ApiGateway: Weather fetched for Colombo (28.5°C)
     ApiGateway: HttpURLConnection successful
     ```

#### UI Demonstration for Member 2

**Your UI Demo:** The "API Gateway" tab on the Dashboard - this is your showcase.

**Live Demo Steps:**

1. Navigate to API Gateway tab
2. Click "Fetch Weather from API"
3. Dashboard sends command to Hub
4. Hub forwards to your API Gateway service
5. Your service uses HttpURLConnection to fetch real weather data
6. Result appears on Dashboard in 2-3 seconds
7. Repeat with different cities to show it's live

**Your Core Concept Demonstrated:** HttpURLConnection to external APIs (Lesson 5)

#### Acceptance Criteria

- ✅ Dashboard has 5 functional tabs (one for each member's demo)
- ✅ Tab 1 (Service Registry) displays all registered services in real-time
- ✅ Tab 2 (API Gateway) has working "Fetch Weather" button
- ✅ API Gateway service receives commands from Hub
- ✅ HttpURLConnection successfully fetches external API data
- ✅ Real-world data (weather) displayed on dashboard
- ✅ All tabs functional and demonstrate each member's work
- ✅ Logs sent to Log Service

---

### MEMBER 3: SECURE FILE SERVICE (JSSE / Secure Sockets)

**Core Concepts:** SSLServerSocket, KeyStore, JSSE (Java Secure Socket Extension)

#### Current Code Relevant

- SSL setup: `ChatServer.java` (lines: keystore configuration)
- File handling: `ApiController.java` (upload/download endpoints)

#### New Java Service

1. **Create Service Structure**

   ```
   services/
   └── secure-file-service/
       ├── pom.xml
       ├── keystore/
       │   ├── fileservice.keystore
       │   ├── fileservice.cer
       │   └── truststore
       ├── files/ (storage directory)
       ├── src/main/java/com/example/fileset/
       │   ├── SecureFileService.java (MAIN)
       │   ├── SSLFileServer.java (SSLServerSocket listener)
       │   ├── FileServiceHandler.java (Per-connection handler)
       │   ├── HubClient.java (Register with Hub)
       │   └── security/SSLUtils.java (Reuse from ChatServer)
       └── src/main/resources/ssl-config.properties
   ```

2. **Generate Self-Signed Certificate**

   - Use existing: `keystore/server.keystore` (or generate new)
   - Copy from Chat Server setup or regenerate:
     ```bash
     keytool -genkey -alias fileserver -keyalg RSA -keysize 2048 \
       -keystore keystore/fileservice.keystore -validity 365 \
       -storepass password -keypass password -dname "CN=FileServer"
     ```

3. **Implement SSLServerSocket (Not Regular ServerSocket)**

   ```java
   public class SSLFileServer {
       // Create KeyStore from file
       KeyStore keyStore = KeyStore.getInstance("JKS");
       keyStore.load(new FileInputStream("keystore/fileservice.keystore"),
                     "password".toCharArray());

       // Create SSLContext
       KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
       kmf.init(keyStore, "password".toCharArray());

       SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
       sslContext.init(kmf.getKeyManagers(), null, null);

       // Create SSLServerSocket on port 9090
       SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
       SSLServerSocket serverSocket =
           (SSLServerSocket) factory.createServerSocket(9090);

       // Accept connections
       while (true) {
           SSLSocket socket = (SSLSocket) serverSocket.accept();
           new Thread(new FileServiceHandler(socket)).start();
       }
   }
   ```

4. **File Command Protocol**

   ```
   STORE <filename> <content>
   RETRIEVE <filename>
   LIST
   DELETE <filename>
   ```

   - Example: `STORE test.txt Hello World`
   - Example: `RETRIEVE test.txt`

5. **FileServiceHandler Implementation**

   - Read commands from SSLSocket input stream
   - Process file operations
   - Send responses back over SSL

6. **Hub Registration**

   - On startup: Send `REGISTER::SecureFileService::localhost::9090`
   - Heartbeat every 10 seconds

7. **Implement UI-Triggered Security Test**

   - Listen for command from Hub: `"run-test"`
   - When command received, automatically run TWO test clients:

   **Test Client 1: Insecure Socket (Should Fail)**

   ```java
   void runInsecureTest() {
       try {
           Socket insecureSocket = new Socket("localhost", 9090);
           // This should fail because SSLServerSocket rejects non-SSL
       } catch (Exception e) {
           sendResultToHub("Test 1 (Insecure Socket): FAILED - " + e.getMessage());
       }
   }
   ```

   **Test Client 2: Secure SSLSocket (Should Succeed)**

   ```java
   void runSecureTest() {
       try {
           SSLSocketFactory factory = sslContext.getSocketFactory();
           SSLSocket secureSocket = (SSLSocket) factory.createSocket("localhost", 9090);
           secureSocket.startHandshake();
           sendResultToHub("Test 2 (Secure SSLSocket): SUCCESS - Connected securely");
       } catch (Exception e) {
           sendResultToHub("Test 2 (Secure SSLSocket): FAILED - " + e.getMessage());
       }
   }
   ```

   - Send BOTH results back to Hub:
     ```java
     void sendResultToHub(String testResult) {
         String result = "{\"result_from\": \"JSSE_SERVICE\", \"data\": \""
                       + testResult + "\"}";
         hubConnection.println(result);
     }
     ```

8. **Testing & Demo**
   - Demo 1: Show service registered on Hub
   - Demo 2: From Dashboard, click "Run Security Test"
   - Demo 3: Two results appear: One FAILED (insecure), One SUCCESS (secure)
   - Demo 4: This proves your SSLServerSocket is working correctly

#### UI Demonstration for Member 3

**Your UI Demo:** The "Security Test" tab on the Dashboard - this proves your JSSE implementation works.

**Live Demo Steps:**

1. Navigate to Security Test tab
2. Click "Run Security Connection Test"
3. Dashboard sends command to Hub
4. Hub forwards to your Secure File Service
5. Your service runs TWO automated test clients
6. First result appears: "Test 1 (Insecure Socket): FAILED ❌"
7. Second result appears: "Test 2 (Secure SSLSocket): SUCCESS ✅"
8. This proves your SSLServerSocket rejects insecure connections

**Your Core Concept Demonstrated:** JSSE (Java Secure Socket Extension) and SSLServerSocket (Lesson 8)

#### Acceptance Criteria

- ✅ Uses SSLServerSocket (NOT regular ServerSocket)
- ✅ Self-signed certificate and KeyStore properly configured
- ✅ Service listens for "run-test" command from Hub
- ✅ Automated test shows insecure Socket FAILS to connect
- ✅ Automated test shows secure SSLSocket SUCCEEDS
- ✅ Both test results sent back to Hub and displayed on Dashboard
- ✅ Service appears on Hub dashboard
- ✅ Logs sent to Log Service

---

### MEMBER 4: HIGH-PERFORMANCE LOG SERVICE (Java NIO)

**Core Concepts:** ServerSocketChannel, Selector, SocketChannel, Non-Blocking I/O (Lesson 7)

#### New Java Service

1. **Create Service Structure**

   ```
   services/
   └── nio-log-service/
       ├── pom.xml
       ├── logs/ (log output directory)
       ├── src/main/java/com/example/logservice/
       │   ├── NioLogService.java (MAIN)
       │   ├── LogServer.java (Selector-based NIO server)
       │   ├── HubClient.java (Register with Hub)
       │   └── LogWriter.java (Async file writer)
       └── src/main/resources/
   ```

2. **Implement NIO Server (NOT using Socket/ServerSocket)**

   ```java
   public class LogServer {
       private ServerSocketChannel serverSocketChannel;
       private Selector selector;

       public void start(int port) throws IOException {
           // Create ServerSocketChannel
           serverSocketChannel = ServerSocketChannel.open();
           serverSocketChannel.configureBlocking(false);
           serverSocketChannel.bind(new InetSocketAddress(port));

           // Create Selector for managing channels
           selector = Selector.open();
           serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

           // Single-threaded event loop
           while (true) {
               selector.select(); // Block until events ready

               Set<SelectionKey> selectedKeys = selector.selectedKeys();
               Iterator<SelectionKey> it = selectedKeys.iterator();

               while (it.hasNext()) {
                   SelectionKey key = it.next();

                   if (key.isAcceptable()) {
                       handleAccept(key);
                   } else if (key.isReadable()) {
                       handleRead(key);
                   }

                   it.remove();
               }
           }
       }

       private void handleAccept(SelectionKey key) throws IOException {
           SocketChannel clientChannel =
               serverSocketChannel.accept();
           clientChannel.configureBlocking(false);
           clientChannel.register(selector, SelectionKey.OP_READ);
       }

       private void handleRead(SelectionKey key) throws IOException {
           SocketChannel channel = (SocketChannel) key.channel();
           ByteBuffer buffer = ByteBuffer.allocate(1024);

           int bytesRead = channel.read(buffer);
           if (bytesRead == -1) {
               channel.close();
               key.cancel();
           } else {
               String logMessage = new String(buffer.array(), 0, bytesRead);
               writeLogFile(logMessage);
           }
       }
   }
   ```

3. **Hub Registration**

   - On startup: Send `REGISTER::NioLogService::localhost::9091`
   - Heartbeat every 10 seconds

4. **Logging Protocol**

   - Clients connect and send log lines:
     ```
     ApiGateway: Weather fetched for Colombo (28.5°C)
     SecureFileService: File test.txt stored (1024 bytes)
     TaskService: Task calculate-pi completed
     ```

5. **Other Services Integration**

   - Member 1 (Hub): Sends registration/deregistration events
   - Member 2 (API Gateway): Sends API call logs
   - Member 3 (File Service): Sends file operation logs
   - Member 5 (Task Service): Sends task execution logs

6. **Forward Logs to Hub for Dashboard Display**

   - When a log message is received from any service, process it twice:

     1. Write to log file: `logs/service.log`
     2. Forward to Hub for Dashboard display

   - Example:

     ```java
     private void handleRead(SelectionKey key) throws IOException {
         SocketChannel channel = (SocketChannel) key.channel();
         ByteBuffer buffer = ByteBuffer.allocate(1024);

         int bytesRead = channel.read(buffer);
         if (bytesRead > 0) {
             String logMessage = new String(buffer.array(), 0, bytesRead);

             // 1. Write to file
             writeLogFile(logMessage);

             // 2. Forward to Hub for Dashboard
             forwardToHub(logMessage);
         }
     }

     private void forwardToHub(String logMessage) {
         String result = "{\"result_from\": \"NIO_SERVICE\", \"data\": \"LOG: "
                       + logMessage + "\"}";
         hubConnection.println(result);
     }
     ```

7. **Persistent Logging**
   - Write logs to file: `logs/service.log`
   - Rotate logs daily or by size
   - Console output for demo purposes

#### UI Demonstration for Member 4

**Your UI Demo:** The "NIO Log Stream" tab on the Dashboard - this is the most visually impressive demo.

**Live Demo Steps:**

1. Navigate to NIO Log Stream tab
2. As all services run, this tab fills with real-time logs:
   ```
   [Hub] Service 'API_GATEWAY' registered
   [ApiGateway] Weather fetched for Colombo (28.5°C)
   [JSSE_SERVICE] Test 1 (Insecure Socket): FAILED
   [JSSE_SERVICE] Test 2 (Secure SSLSocket): SUCCESS
   [RMI_SERVICE] Task 'calculate-pi' completed
   [Hub] Service 'API_GATEWAY' heartbeat received
   ```
3. All system activity appears here in real-time
4. This proves your single-threaded NIO Selector is handling multiple concurrent log streams

**Your Core Concept Demonstrated:** Java NIO (Non-blocking I/O with Selector) - Lesson 7

#### Acceptance Criteria

- ✅ Uses ServerSocketChannel and Selector (NOT Socket/ServerSocket)
- ✅ Single-threaded event loop with selector.select()
- ✅ Handles multiple concurrent connections non-blocking
- ✅ Service appears on Hub dashboard
- ✅ Logs from all other services appear in real-time
- ✅ Logs forwarded to Hub and displayed on Dashboard
- ✅ Logs persisted to file

---

### MEMBER 5: DISTRIBUTED TASK RUNNER (Java RMI)

**Core Concepts:** Remote Method Invocation (RMI), Remote Interface, RMI Registry, Remote Exceptions

#### New Java Service

1. **Create Service Structure**

   ```
   services/
   └── rmi-task-service/
       ├── pom.xml
       ├── src/main/java/com/example/taskservice/
       │   ├── TaskServiceServer.java (MAIN - starts RMI registry & binds service)
       │   ├── TaskService.java (Remote interface, extends Remote)
       │   ├── TaskServiceImpl.java (Implementation)
       │   ├── HubClient.java (Register with Hub)
       │   └── client/
       │       ├── TaskClient.java (CLI client to invoke remote methods)
       │       └── TaskResult.java (Result object)
       └── src/main/resources/
   ```

2. **Define Remote Interface**

   ```java
   import java.rmi.Remote;
   import java.rmi.RemoteException;

   public interface TaskService extends Remote {
       String executeTask(String taskName) throws RemoteException;
       int getCpuLoad() throws RemoteException;
       String getStatus() throws RemoteException;
       List<String> getAvailableTasks() throws RemoteException;
   }
   ```

3. **Implement Remote Service**

   ```java
   import java.rmi.RemoteException;
   import java.rmi.server.UnicastRemoteObject;

   public class TaskServiceImpl
       extends UnicastRemoteObject
       implements TaskService {

       public TaskServiceImpl() throws RemoteException {
           super();
       }

       @Override
       public String executeTask(String taskName) throws RemoteException {
           System.out.println("Remote call: executeTask(" + taskName + ")");

           if ("calculate-pi".equals(taskName)) {
               return "Pi = 3.14159265358979...";
           } else if ("fibonacci-10".equals(taskName)) {
               return "55";
           }
           return "Unknown task";
       }

       @Override
       public int getCpuLoad() throws RemoteException {
           return (int) (ManagementFactory
               .getOperatingSystemMXBean()
               .getProcessCpuLoad() * 100);
       }

       @Override
       public String getStatus() throws RemoteException {
           return "Task Service: Running";
       }

       @Override
       public List<String> getAvailableTasks() throws RemoteException {
           return Arrays.asList(
               "calculate-pi",
               "fibonacci-10",
               "matrix-multiply",
               "prime-check"
           );
       }
   }
   ```

4. **Start RMI Registry & Bind Service**

   ```java
   public class TaskServiceServer {
       public static void main(String[] args) {
           try {
               // Start RMI registry on port 1099
               Registry registry = LocateRegistry.createRegistry(1099);

               // Create and bind service
               TaskService service = new TaskServiceImpl();
               registry.rebind("TaskService", service);

               System.out.println("TaskService bound and ready on rmi://localhost:1099/TaskService");

               // Register with Hub
               registerWithHub();

               // Keep running
               Thread.currentThread().join();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }

       private static void registerWithHub() throws IOException {
           String registerMsg = "REGISTER::TaskService::rmi://localhost::1099/TaskService";
           Socket socket = new Socket("localhost", 7070);
           PrintWriter out = new PrintWriter(socket.getOutputStream());
           out.println(registerMsg);
           out.flush();
           socket.close();
       }
   }
   ```

5. **Implement RMI Client**

   ```java
   public class TaskClient {
       public static void main(String[] args) {
           try {
               // Look up service in registry
               Registry registry =
                   LocateRegistry.getRegistry("localhost", 1099);
               TaskService service =
                   (TaskService) registry.lookup("TaskService");

               // Invoke remote methods
               System.out.println("Available tasks: "
                   + service.getAvailableTasks());
               System.out.println("CPU Load: "
                   + service.getCpuLoad() + "%");
               System.out.println("Execute calculate-pi: "
                   + service.executeTask("calculate-pi"));

           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }
   ```

6. **Integrate RMI Client with Hub (CRITICAL FOR UI DEMO)**

   - Your RMI client code will run **inside the Hub server** (Member 1's code)
   - When Hub receives command `{"command_for": "RMI_SERVICE", "payload": "calculate-pi"}`:

     1. Hub internally calls your RMI client code
     2. Your RMI client invokes remote method on your RMI server
     3. RMI server returns result
     4. RMI client gives result to Hub
     5. Hub sends to Dashboard: `{"result_from": "RMI_SERVICE", "data": "Task 'calculate-pi' complete. Result: 3.14159"}`

   - Implementation in Hub (Member 1 integrates this):
     ```java
     // In HubServer.java
     void routeRmiCommand(String payload) {
         try {
             Registry registry = LocateRegistry.getRegistry("localhost", 1099);
             TaskService service = (TaskService) registry.lookup("TaskService");

             String result = service.executeTask(payload);

             String response = "{\"result_from\": \"RMI_SERVICE\", \"data\": \""
                             + result + "\"}";
             broadcastToAllDashboards(response);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     ```

7. **Hub Registration**
   - Send: `REGISTER::RMI_SERVICE::rmi://localhost:1099/TaskService`
   - Heartbeat every 10 seconds

#### UI Demonstration for Member 5

**Your UI Demo:** The "RMI Task Runner" tab on the Dashboard - this shows distributed computing in action.

**Live Demo Steps:**

1. Navigate to RMI Task Runner tab
2. Select task from dropdown: "calculate-pi"
3. Click "Execute Remote Task"
4. Dashboard sends command to Hub
5. Hub uses your RMI client code to invoke your RMI server
6. Your RMI server calculates Pi (Remote Method Invocation happens here)
7. Result appears on Dashboard: "Task 'calculate-pi' complete. Result: 3.14159..."
8. Repeat with different tasks to show remote method calls

**Your Core Concept Demonstrated:** Java RMI (Remote Method Invocation) - Distributed computing

**Why This Architecture:** RMI isn't web-friendly, so we run your RMI client alongside the Hub. The remote method invocation (your core concept) still happens between your client and server - the Hub just acts as a trigger.

#### Acceptance Criteria

- ✅ Proper Remote interface extending `java.rmi.Remote`
- ✅ RMI registry started and service bound
- ✅ RMI client can successfully invoke remote methods
- ✅ Remote exceptions properly handled
- ✅ RMI client code integrated with Hub for UI commands
- ✅ Service appears on Hub dashboard
- ✅ Remote method calls work across network
- ✅ Dashboard displays task execution results
- ✅ Logs sent to Log Service

---

## 4. SUMMARY OF UI INTEGRATION REQUIREMENTS

### Quick Reference for Each Member

**Member 1 (Hub Server):**

- ✅ Existing: Service registry, heartbeat monitoring, WebSocket broadcasting
- 🆕 **Add Command Router:** Parse `{"command_for": "...", "payload": "..."}` from Dashboard WebSocket
- 🆕 **Add Result Aggregator:** Listen for `{"result_from": "...", "data": "..."}` from service TCP connections
- 🆕 **Integrate RMI Client:** When command_for = "RMI_SERVICE", call Member 5's RMI client code
- **UI Demo:** Service Registry tab shows real-time service join/leave (proves concurrency)

**Member 2 (Dashboard + API Gateway):**

- 🆕 **Build 5-Tab Dashboard:** One tab for each member's demonstration
- 🆕 **Implement Command Sending:** Buttons send commands to Hub via WebSocket
- 🆕 **Implement Result Display:** Handle results and display in appropriate tabs
- 🆕 **Build API Gateway Service:** Command listener + HttpURLConnection implementation
- **UI Demo:** API Gateway tab with "Fetch Weather" button (proves HttpURLConnection)

**Member 3 (Secure File Service):**

- ✅ Existing: SSLServerSocket file service
- 🆕 **Add Command Listener:** Listen for "run-test" command from Hub
- 🆕 **Implement Automated Test Clients:** Run 2 tests (insecure Socket fails, secure SSLSocket succeeds)
- 🆕 **Send Results to Hub:** Both test outcomes sent back for Dashboard display
- **UI Demo:** Security Test tab shows 2 test results (proves SSLServerSocket rejects insecure connections)

**Member 4 (NIO Log Service):**

- ✅ Existing: NIO Selector-based logging, file writing
- 🆕 **Add Log Forwarding:** When log received, ALSO forward to Hub (in addition to file write)
- 🆕 **Format for Hub:** `{"result_from": "NIO_SERVICE", "data": "LOG: ..."}`
- **UI Demo:** NIO Log Stream tab shows real-time logs from all services (proves NIO Selector handles concurrent streams)

**Member 5 (RMI Task Service):**

- 🆕 **Build RMI Server:** TaskService remote interface, implementation, registry binding
- 🆕 **Build RMI Client:** Separate client that invokes remote methods
- 🆕 **Integrate with Hub:** Work with Member 1 to call RMI client from Hub when command received
- **UI Demo:** RMI Task Runner tab executes remote tasks (proves Java RMI remote method invocation)

---

## 5. IMPLEMENTATION PHASES & TIMELINE

### Phase 1: Hub Server Refactoring (Member 1)

**Duration:** 3-4 days  
**Dependencies:** None (Independent start)

- [ ] Refactor ChatServer → HubServer
- [ ] Implement ConcurrentHashMap service registry
- [ ] Create service registration protocol
- [ ] Implement heartbeat monitor (ScheduledExecutorService)
- [ ] Deploy heartbeat detection and timeout mechanism
- [ ] **NEW: Implement Message Broker - Command Router**
  - [ ] Parse incoming WebSocket commands from Dashboard
  - [ ] Route commands to appropriate service TCP connections
- [ ] **NEW: Implement Message Broker - Result Aggregator**
  - [ ] Listen for results from service connections
  - [ ] Broadcast results to all Dashboard clients
- [ ] Enhance WebSocket broadcaster for service updates
- [ ] Test with mock services
- [ ] Complete logging output

**Deliverable:** Working Hub server with message broker capabilities

### Phase 2: React Dashboard Refactoring (Member 2 - Part A)

**Duration:** 3-4 days  
**Dependencies:** Hub server (Phase 1) with message broker

- [ ] **NEW: Create Multi-Tab Dashboard Layout**
  - [ ] Tab 1: Service Registry (Member 1's demo)
  - [ ] Tab 2: API Gateway (Member 2's demo)
  - [ ] Tab 3: Security Test (Member 3's demo)
  - [ ] Tab 4: NIO Log Stream (Member 4's demo)
  - [ ] Tab 5: RMI Task Runner (Member 5's demo)
- [ ] **Implement Tab 1: Service Registry Component**
- [ ] **Implement Tab 2: API Gateway Component**
  - [ ] Button to fetch weather
  - [ ] Display area for results
  - [ ] Send commands to Hub
- [ ] **Implement Tab 3: Security Test Component**
  - [ ] Button to run security test
  - [ ] Display area for test results (2 outcomes)
- [ ] **Implement Tab 4: NIO Log Stream Component**
  - [ ] Auto-scrolling log text area
  - [ ] Real-time log appending
- [ ] **Implement Tab 5: RMI Task Runner Component**
  - [ ] Task dropdown selector
  - [ ] Execute button
  - [ ] Display area for task results
- [ ] Update WebSocket message handlers for commands and results
- [ ] Connect to Hub for service updates
- [ ] Style dashboard UI
- [ ] Test real-time updates for all tabs

**Deliverable:** Working multi-tab dashboard with all 5 demo interfaces

### Phase 3: API Gateway Service (Member 2 - Part B)

**Duration:** 3-4 days  
**Dependencies:** Hub server (Phase 1), Dashboard (Phase 2)

- [ ] Create API Gateway service module
- [ ] Implement HubClient to register with Hub
- [ ] Implement heartbeat mechanism
- [ ] **Implement Command Listener for Hub communication**
- [ ] **Implement HttpURLConnection to external API (CORE CONCEPT)**
- [ ] **Parse commands and send results back to Hub**
- [ ] Test HTTP calls to external API
- [ ] Test end-to-end: Dashboard → Hub → Service → Hub → Dashboard
- [ ] Integrate with Log Service (once ready)

**Deliverable:** API Gateway service with UI integration

### Phase 4: Secure File Service (Member 3) ✅ COMPLETE + UI INTEGRATION NEEDED

**Duration:** 1 day (Core completed: November 11, 2025) + 1 day UI integration  
**Dependencies:** Hub server ✅, Dashboard ✅

**Completed:**

- [x] Create service module structure
- [x] Generate self-signed certificate and KeyStore
- [x] Implement SSLServerSocket server
- [x] Create FileServiceHandler for protocols
- [x] Implement file storage logic
- [x] Create test SSL client
- [x] Implement HubClient registration
- [x] Integrate logging

**NEW - UI Integration (To Do):**

- [ ] **Implement Command Listener for Hub communication**
- [ ] **Implement automated security test (2 test clients)**
  - [ ] Test Client 1: Insecure Socket (should fail)
  - [ ] Test Client 2: Secure SSLSocket (should succeed)
- [ ] **Send test results back to Hub**
- [ ] **Test end-to-end: Dashboard → Hub → Security Test → Results**

**Deliverable:** ✅ Secure file storage over SSL + UI-integrated security demonstration  
**Documentation:** See `PHASE_4_COMPLETE.md` + UI integration docs

### Phase 5: NIO Log Service (Member 4)

**Duration:** 4-5 days  
**Dependencies:** Hub server (Phase 1), Dashboard (Phase 2)

- [ ] Create service module structure
- [ ] **Implement ServerSocketChannel + Selector (CORE CONCEPT)**
- [ ] **Create non-blocking event loop with selector.select()**
- [ ] Implement HubClient registration
- [ ] Create log file writer
- [ ] **NEW: Implement log forwarding to Hub for Dashboard display**
  - [ ] When log received, write to file AND forward to Hub
  - [ ] Format: `{"result_from": "NIO_SERVICE", "data": "LOG: ..."}`
- [ ] Test with multiple concurrent connections
- [ ] Integrate with all other services
- [ ] **Test end-to-end: Services → NIO → Hub → Dashboard log tab**

**Deliverable:** High-performance logging service with real-time UI display

### Phase 6: RMI Task Service (Member 5)

**Duration:** 3-4 days  
**Dependencies:** Hub server, Dashboard, Log Service

- [ ] Create service module structure
- [ ] **Define TaskService remote interface (extends Remote)**
- [ ] **Implement TaskServiceImpl (extends UnicastRemoteObject)**
- [ ] **Start RMI registry and bind service (CORE CONCEPT)**
- [ ] **Create RMI client**
- [ ] **Integrate RMI client with Hub (Member 1 helps)**
  - [ ] Hub calls RMI client when command received
  - [ ] RMI client invokes remote methods on RMI server
  - [ ] Results sent back through Hub to Dashboard
- [ ] Implement HubClient registration
- [ ] Test remote method invocation
- [ ] **Test end-to-end: Dashboard → Hub → RMI Client → RMI Server → Results**
- [ ] Integrate logging

**Deliverable:** Working RMI service with UI-integrated task execution

### Phase 7: Integration & Testing (All Members)

**Duration:** 2-3 days  
**Dependencies:** All services (Phases 1-6)

**System Startup Sequence:**

- [ ] Start Hub server (Member 1)
- [ ] Start React Dashboard (Member 2)
- [ ] Start all microservices (Members 2-5)
- [ ] Verify all registrations on Hub

**UI Testing - Each Tab:**

- [ ] **Tab 1: Service Registry** (Member 1's demo)
  - [ ] All services appear when started
  - [ ] Services disappear when stopped/timeout
  - [ ] Real-time updates work
- [ ] **Tab 2: API Gateway** (Member 2's demo)
  - [ ] Click "Fetch Weather" button
  - [ ] Command routes through Hub correctly
  - [ ] HttpURLConnection fetches real data
  - [ ] Weather data displays on Dashboard
- [ ] **Tab 3: Security Test** (Member 3's demo)
  - [ ] Click "Run Security Test" button
  - [ ] Two test results appear (FAILED and SUCCESS)
  - [ ] Proves SSLServerSocket rejects insecure connections
- [ ] **Tab 4: NIO Log Stream** (Member 4's demo)
  - [ ] Real-time logs appear from all services
  - [ ] Auto-scrolling works
  - [ ] Proves NIO Selector handles concurrent connections
- [ ] **Tab 5: RMI Task Runner** (Member 5's demo)
  - [ ] Select task and click Execute
  - [ ] Remote method invocation succeeds
  - [ ] Task result displays on Dashboard

**End-to-End Testing:**

- [ ] All 5 tabs functional simultaneously
- [ ] Message broker routes commands correctly
- [ ] Results return through Hub to Dashboard
- [ ] Concurrent operations work without blocking
- [ ] System handles service failures gracefully

**Demo Preparation:**

- [ ] Create demo script for presentation
- [ ] Record video walkthrough (backup)
- [ ] Prepare documentation
- [ ] Test on clean machine

**Deliverable:** Fully integrated system with working UI demonstrations for all 5 members

---

## 6. DIRECTORY STRUCTURE (After Implementation)

```
network programming - assignment/
├── frontend/                          (React Dashboard - Refactored)
│   ├── src/components/
│   │   ├── ServiceRegistry.jsx
│   │   ├── ServiceDashboard.jsx
│   │   ├── ExternalDataFetcher.jsx
│   │   └── ...
│   └── ...
│
├── services/
│   ├── hub-server/                    (Member 1)
│   │   ├── pom.xml
│   │   ├── src/main/java/com/example/hub/
│   │   │   ├── HubServer.java
│   │   │   ├── ServiceRegistry.java
│   │   │   ├── ServiceRegistryHandler.java
│   │   │   ├── HeartbeatMonitor.java
│   │   │   └── security/SSLUtils.java
│   │   └── ...
│   │
│   ├── api-gateway-service/           (Member 2)
│   │   ├── pom.xml
│   │   ├── src/main/java/com/example/apigateway/
│   │   │   ├── ApiGatewayService.java
│   │   │   ├── HubClient.java
│   │   │   ├── WebSocketServer.java
│   │   │   ├── ExternalApiClient.java (HttpURLConnection)
│   │   │   └── ...
│   │   └── ...
│   │
│   ├── secure-file-service/           (Member 3)
│   │   ├── pom.xml
│   │   ├── keystore/
│   │   │   ├── fileservice.keystore
│   │   │   └── fileservice.cer
│   │   ├── files/ (storage)
│   │   ├── src/main/java/com/example/fileservice/
│   │   │   ├── SecureFileService.java
│   │   │   ├── SSLFileServer.java
│   │   │   ├── FileServiceHandler.java
│   │   │   ├── HubClient.java
│   │   │   └── security/SSLUtils.java
│   │   └── ...
│   │
│   ├── nio-log-service/               (Member 4)
│   │   ├── pom.xml
│   │   ├── logs/ (output)
│   │   ├── src/main/java/com/example/logservice/
│   │   │   ├── NioLogService.java
│   │   │   ├── LogServer.java (Selector-based)
│   │   │   ├── HubClient.java
│   │   │   └── LogWriter.java
│   │   └── ...
│   │
│   └── rmi-task-service/              (Member 5)
│       ├── pom.xml
│       ├── src/main/java/com/example/taskservice/
│       │   ├── TaskServiceServer.java
│       │   ├── TaskService.java (Remote interface)
│       │   ├── TaskServiceImpl.java
│       │   ├── HubClient.java
│       │   └── client/
│       │       ├── TaskClient.java
│       │       └── TaskResult.java
│       └── ...
│
├── IMPLEMENTATION_PLAN.md           (This file)
├── START_SERVICES.md                 (Startup guide - to be created)
├── API_DOCUMENTATION.md              (Protocol documentation - to be created)
└── DEMO_GUIDE.md                     (Presentation guide - to be created)
```

---

## 7. KEY IMPLEMENTATION GUIDELINES

### General Requirements for All Services

1. **Hub Registration on Startup**

   ```java
   String registerMessage = "REGISTER::" + serviceName + "::" + host + "::" + port;
   // Send to Hub on port 7070 via TCP Socket
   ```

2. **Heartbeat Every 10 Seconds**

   ```java
   ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   scheduler.scheduleAtFixedRate(() -> {
       String heartbeat = "HEARTBEAT::" + serviceName;
       // Send to Hub on port 7070
   }, 0, 10, TimeUnit.SECONDS);
   ```

3. **Logging to Log Service on port 9091**

   ```java
   // All services should connect to Log Service and send logs
   Socket logSocket = new Socket("localhost", 9091);
   PrintWriter logWriter = new PrintWriter(logSocket.getOutputStream(), true);
   logWriter.println("ServiceName: Log message here");
   ```

4. **Graceful Shutdown**
   ```java
   Runtime.getRuntime().addShutdownHook(new Thread(() -> {
       // Send DEREGISTER message to Hub
       // Close all connections
       // Write final log entry
   }));
   ```

### Member 1 (Hub) Specific

- Use `ConcurrentHashMap` for thread-safety
- Use `ExecutorService` for managing service connections
- Use `ScheduledExecutorService` for heartbeat monitor
- Broadcast service list as JSON via WebSocket
- Handle multiple concurrent service connections

### Member 2 (API Gateway) Specific

- Use `HttpURLConnection` for external API calls (NOT Retrofit, OkHttp, etc.)
- Handle JSON parsing from external API
- Create WebSocket endpoint for React commands
- Implement proper error handling for network calls

### Member 3 (Secure File Service) Specific

- Use `SSLServerSocket` (NOT regular ServerSocket)
- Proper KeyStore and KeyManager configuration
- Verify client certificates if required
- Implement file storage with proper permissions

### Member 4 (Log Service) Specific

- Use `ServerSocketChannel` and `Selector` (NOT ServerSocket)
- Single-threaded event loop with `selector.select()`
- Non-blocking read/write operations
- Persistent log file output

### Member 5 (RMI Service) Specific

- Extend `UnicastRemoteObject` for automatic serialization
- Implement proper `Remote` interface with `RemoteException` throws
- Use `LocateRegistry` for registry operations
- Implement proper shutdown in RMI registry

---

## 8. TESTING STRATEGY

### Unit Testing

- Each service tested independently
- Mock Hub for service testing
- Test protocol parsing and handling

### Integration Testing

- Start Hub first
- Start each service and verify registration
- Verify heartbeat mechanism
- Test service timeout detection
- Test concurrent operations

### End-to-End Testing

- Start all services
- React Dashboard displays all services
- API Gateway fetches external data
- File upload/download via Secure File Service
- All logs appear in Log Service
- RMI client calls remote methods
- Service timeout and recovery

### Demo Scenarios

1. Hub startup and dashboard initialization
2. API Gateway registers and fetches weather
3. Secure File Service stores/retrieves files securely
4. Log Service displays concurrent logs from all services
5. RMI Task Service executes remote tasks
6. Service timeout and automatic deregistration
7. Service recovery and re-registration

---

## 9. DEPLOYMENT CHECKLIST

Before presentation:

- [ ] All services build successfully with Maven
- [ ] No compilation errors or warnings
- [ ] All dependencies properly defined in pom.xml
- [ ] Keystore/SSL certificates generated and valid
- [ ] Logging properly configured in all services
- [ ] Demo scripts prepared and tested
- [ ] Documentation complete
- [ ] GitHub repository updated with all code
- [ ] Each member can independently run their service
- [ ] System works with all services running together

---

## 10. DOCUMENTATION TO CREATE

1. **START_SERVICES.md** - Step-by-step guide to start each service
2. **API_DOCUMENTATION.md** - Protocol specification for service communication
3. **DEMO_GUIDE.md** - Presentation walkthrough
4. **TROUBLESHOOTING.md** - Common issues and solutions
5. **SERVICE_README.md** (each service folder) - Service-specific documentation

---

## 11. MIGRATION FROM EXISTING CODE

### What to Keep

- ✅ SSL/TLS infrastructure (ChatServer)
- ✅ WebSocket foundation (Javalin)
- ✅ File handling with NIO (ApiController)
- ✅ React UI structure
- ✅ Error handling patterns

### What to Refactor

- ⚠️ WebSocketHandler → ServiceRegistryHandler (not chat messages, but service events)
- ⚠️ ChatRoom.jsx → ServiceDashboard.jsx
- ⚠️ Remove chat-specific logic

### What to Create New

- 🆕 HubServer core functionality
- 🆕 4 Independent microservices
- 🆕 Service registration protocol
- 🆕 Heartbeat mechanism
- 🆕 HttpURLConnection wrapper
- 🆕 SSLServerSocket file server
- 🆕 Selector-based NIO log server
- 🆕 RMI service with remote interface

---

## SUMMARY

This Distributed Services Hub architecture transforms a simple chat application into a production-like microservices demonstration system with **full UI integration for each member's work**.

### Core Architecture Innovation: Hub as Message Broker

Instead of CLI-only demonstrations, the Hub acts as a **central message broker** that routes commands from the React Dashboard to services and broadcasts results back. This ensures:

✅ **Every member gets a dedicated UI tab** to showcase their networking concept  
✅ **No CLI required** - Everything visible in the browser  
✅ **Real networking concepts demonstrated** - Not just UI mockups  
✅ **Clean architecture** - Dashboard only talks to Hub, Hub routes to services

### Member Focus & UI Demonstrations

| Member       | Core Concept                       | UI Demo              | What User Sees                                                 |
| ------------ | ---------------------------------- | -------------------- | -------------------------------------------------------------- |
| **Member 1** | Multithreading & Concurrency (Hub) | Service Registry Tab | Real-time service list updates, proves ConcurrentHashMap works |
| **Member 2** | HttpURLConnection (API Gateway)    | API Gateway Tab      | "Fetch Weather" button shows external API call results         |
| **Member 3** | JSSE & SSLServerSocket (Security)  | Security Test Tab    | 2 test results (insecure FAILS, secure SUCCEEDS)               |
| **Member 4** | Java NIO & Selector (Logging)      | NIO Log Stream Tab   | Real-time scrolling logs from all services                     |
| **Member 5** | Java RMI (Task Runner)             | RMI Task Runner Tab  | "Execute Task" button shows remote method results              |

### Message Flow Example

```
User clicks "Fetch Weather" on Dashboard (Member 2's tab)
    ↓
Dashboard sends: {"command_for": "API_GATEWAY", "payload": "get-weather"}
    ↓
Hub routes command to API Gateway service (Member 1's routing logic)
    ↓
API Gateway uses HttpURLConnection to fetch weather (Member 2's core concept)
    ↓
API Gateway sends: {"result_from": "API_GATEWAY", "data": "{temp: 28.5, ...}"}
    ↓
Hub broadcasts result to all Dashboards (Member 1's broadcast logic)
    ↓
Dashboard displays weather data in API Gateway tab (Member 2's UI)
```

### Why This Approach is Superior

❌ **Old Approach:** "Here's my CLI output, trust me it works"  
✅ **New Approach:** "Click this button, see the real-time result on screen"

The system is **scalable, professional, visually impressive, and demonstrates real-world distributed computing patterns** suitable for both academic presentations and portfolio projects.

---

**Next Steps:**

1. Share this updated plan with all team members
2. Member 2 starts Dashboard UI (5 tabs) immediately
3. Member 1 adds message broker capabilities to Hub
4. Members 3-5 add command listeners and result sending
5. Integrate and test all UI demonstrations
