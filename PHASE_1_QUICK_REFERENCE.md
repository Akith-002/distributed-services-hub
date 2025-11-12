# Phase 1 Complete - Hub Server Message Broker Ready

**Status:** ✅ READY FOR PHASE 2  
**Date:** November 12, 2025  
**Build:** ✅ SUCCESS  
**Tests:** ✅ 10/10 PASSED

---

## Quick Start: Running the Hub Server

### Build

```bash
cd services/hub-server
mvn clean package
```

### Run

```bash
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### Verify

```
Expected output:
[TCP_SERVER] Started on port 7070
[HTTP_SERVER] WebSocket server started on port 7071
[HUB] Ready to accept service connections
```

---

## What Phase 1 Provides for Phase 2+

### 1. Service Registration

Services connect to Hub on TCP port 7070 and send:

```
REGISTER::ServiceName::Host::Port
```

Hub responds with status and maintains active connection.

### 2. Message Broker - Commands

Dashboard sends commands via WebSocket on port 7071:

```json
{
  "command_for": "SERVICE_NAME",
  "payload": "command_data"
}
```

Hub routes to service via active TCP connection.

### 3. Message Broker - Results

Service sends results via TCP:

```json
{
  "result_from": "SERVICE_NAME",
  "data": "result_data"
}
```

Hub broadcasts to Dashboard via WebSocket.

### 4. Service Registry API

Dashboard can query services via REST:

```
GET http://localhost:7071/services
GET http://localhost:7071/hub-status
```

---

## For Phase 2: Dashboard Development

### WebSocket Connection

```javascript
const ws = new WebSocket("ws://localhost:7071/registry");

ws.addEventListener("message", (event) => {
  const message = JSON.parse(event.data);

  if (message.type === "SERVICE_REGISTRY_UPDATE") {
    // Update service list
  } else if (message.type === "SERVICE_RESULT") {
    // Update with result from service
  }
});
```

### Sending Commands

```javascript
ws.send(
  JSON.stringify({
    command_for: "API_GATEWAY",
    payload: "get-weather",
  })
);
```

---

## For Phase 3-6: Service Development

### Service Registration (on startup)

```java
Socket hubConnection = new Socket("localhost", 7070);
PrintWriter out = new PrintWriter(hubConnection.getOutputStream(), true);
out.println("REGISTER::MyService::localhost::9001");
// Keep connection open for heartbeat
```

### Sending Heartbeat

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    out.println("HEARTBEAT::MyService");
}, 0, 10, TimeUnit.SECONDS);
```

### Receiving Commands

```java
BufferedReader in = new BufferedReader(
    new InputStreamReader(hubConnection.getInputStream()));
String command = in.readLine(); // e.g., "get-weather"
// Process command
```

### Sending Results

```java
// Send via same connection used for REGISTER/HEARTBEAT
out.println("{\"result_from\": \"MyService\", \"data\": \"result data\"}");
```

---

## Architecture Overview

```
┌─────────────────────────────────────────┐
│      React Dashboard (Port 7071 WS)     │
├─────────────────────────────────────────┤
│ - Tab 1: Service Registry (Member 1)    │
│ - Tab 2: API Gateway (Member 2)         │
│ - Tab 3: Security Test (Member 3)       │
│ - Tab 4: NIO Logs (Member 4)            │
│ - Tab 5: RMI Tasks (Member 5)           │
└───────────────────┬─────────────────────┘
                    │ WebSocket
                    │ Commands & Results
                    ▼
┌─────────────────────────────────────────┐
│    HUB SERVER (Port 7070 TCP)           │
│  Message Broker Pattern                 │
│  ┌─────────────────────────────────────┐│
│  │ CommandRouter (Dashboard → Services)││
│  │ ResultAggregator (Services → Dash)  ││
│  │ ServiceRegistry (ConcurrentHashMap) ││
│  │ HeartbeatMonitor (5s checks)        ││
│  └─────────────────────────────────────┘│
└───────────────────┬─────────────────────┘
                    │ TCP
                    │ Service Protocol
                    ▼
    ┌───────────────┼───────────────┐
    ▼               ▼               ▼
API_GATEWAY    JSSE_SERVICE    NIO_LOG_SERVICE
(Port 9001)    (Port 9090)     (Port 9091)
```

---

## Key Classes - Phase 1

### CommandRouter

- Routes commands from Dashboard to Services
- Thread-safe with ConcurrentHashMap
- Location: `hub-server/src/main/java/com/example/hub/CommandRouter.java`

### ResultAggregator

- Aggregates results from Services for Dashboard
- Broadcasts via WebSocketBroadcaster
- Location: `hub-server/src/main/java/com/example/hub/ResultAggregator.java`

### ServiceRegistry

- Manages service metadata (name, host, port, status)
- Uses ConcurrentHashMap for thread safety
- Location: `hub-server/src/main/java/com/example/hub/ServiceRegistry.java`

### HeartbeatMonitor

- Checks service health every 5 seconds
- Removes services after 30 seconds of no heartbeat
- Uses ScheduledExecutorService
- Location: `hub-server/src/main/java/com/example/hub/HeartbeatMonitor.java`

---

## Testing Phase 1 Implementation

### Run Integration Tests

```bash
cd services/hub-server
mvn test -Dtest=HubServerMessageBrokerTest
```

### Test Results

```
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
✓ Basic Command Routing
✓ Service Not Found Handling
✓ Result Message Creation
✓ Multiple Service Routing
✓ Service Deregistration
✓ JSON Command Parsing
✓ Invalid JSON Handling
✓ Concurrent Service Access
✓ Service Availability Check
✓ Result Aggregation
```

---

## Common Tasks for Phase 2-6

### Creating a Microservice Template

```java
public class MyService {
    private Socket hubConnection;
    private PrintWriter hubWriter;
    private BufferedReader hubReader;

    public void connectToHub() throws IOException {
        hubConnection = new Socket("localhost", 7070);
        hubWriter = new PrintWriter(hubConnection.getOutputStream(), true);
        hubReader = new BufferedReader(
            new InputStreamReader(hubConnection.getInputStream()));

        // Register
        hubWriter.println("REGISTER::MyService::localhost::9001");
        String response = hubReader.readLine();
        System.out.println("Hub response: " + response);

        // Start heartbeat
        startHeartbeat();

        // Start command listener
        startCommandListener();
    }

    private void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            hubWriter.println("HEARTBEAT::MyService");
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void startCommandListener() {
        new Thread(() -> {
            try {
                String command;
                while ((command = hubReader.readLine()) != null) {
                    processCommand(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processCommand(String command) {
        // Your service logic here
        String result = "processed: " + command;
        sendResult(result);
    }

    private void sendResult(String result) {
        String json = "{\"result_from\": \"MyService\", \"data\": \"" + result + "\"}";
        hubWriter.println(json);
    }
}
```

---

## Debugging Tips

### Check Hub Status

```bash
curl http://localhost:7071/hub-status
```

### Get Services List

```bash
curl http://localhost:7071/services
```

### Monitor Hub Output

```bash
# In new terminal while hub is running
tail -f hub-server.log
```

### Test WebSocket Manually

```bash
# Using wscat or similar tool
wscat -c ws://localhost:7071/registry
# Then send: {"command_for": "API_GATEWAY", "payload": "test"}
```

---

## Performance Notes

- **Thread Pool:** 20 concurrent service handlers
- **Heartbeat Check:** Every 5 seconds, O(n) scan
- **Service Lookup:** O(1) with ConcurrentHashMap
- **Command Routing:** O(1) lookup + TCP send
- **Result Broadcast:** O(m) where m = connected dashboards

### Scalability Limits

- Services: Unlimited (limited by thread pool)
- Dashboards: Unlimited (tested with many concurrent clients)
- Concurrent Operations: Safely handles 20+ simultaneous handlers

---

## Troubleshooting

### Port Already in Use

```bash
# Find and kill process on port 7070
netstat -ano | findstr :7070
taskkill /PID <PID> /F
```

### Service Not Registering

1. Check Hub is running: `curl http://localhost:7071/hub-status`
2. Verify service connects to `localhost:7070`
3. Check firewall rules
4. Verify REGISTER message format

### Commands Not Routing

1. Ensure service is registered: `curl http://localhost:7071/services`
2. Check WebSocket connection is open
3. Verify JSON format: `{"command_for": "NAME", "payload": "..."}`
4. Check Hub logs for routing errors

### Results Not Appearing

1. Verify service sends: `{"result_from": "NAME", "data": "..."}`
2. Check Dashboard WebSocket connected
3. Verify result JSON is valid
4. Check broadcast is reaching Dashboard

---

## Documentation

- **Full Report:** See `PHASE_1_MESSAGE_BROKER_REPORT.md`
- **Implementation Plan:** See `IMPLEMENTATION_PLAN.md`
- **Test Code:** See `HubServerMessageBrokerTest.java`
- **Source Code:** See `hub-server/src/main/java/com/example/hub/`

---

**Phase 1 Complete ✅**

Ready for Phase 2 Dashboard Development!
