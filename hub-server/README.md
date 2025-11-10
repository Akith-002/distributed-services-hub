# Hub Server - Phase 1 Implementation

**Member 1 - Multithreading & Concurrency**

## Overview

The Hub Server is the central registry for all microservices in the Distributed Services Hub system. It manages service registration, monitors service health through heartbeats, and provides real-time updates to the React Dashboard.

## Architecture

### Components

1. **ServiceRegistry** (ConcurrentHashMap)

   - Thread-safe storage of all registered services
   - Implements concurrent access patterns (Lesson 6)

2. **ServiceRegistryServer** (Thread Pool)

   - TCP server accepting connections on port 7070
   - Uses ExecutorService with fixed thread pool (20 threads)
   - Each service connection handled by ServiceRegistryHandler

3. **ServiceRegistryHandler** (Runnable)

   - Handles individual service connections
   - Processes REGISTER, HEARTBEAT, DEREGISTER, FETCH_SERVICES messages
   - Runs in thread pool threads

4. **HeartbeatMonitor** (ScheduledExecutorService)

   - Runs periodic health checks every 5 seconds
   - Detects and removes dead services (timeout > 30 seconds)
   - Notifies WebSocket broadcaster of changes

5. **WebSocketBroadcaster**

   - Manages WebSocket connections from React Dashboard
   - Sends real-time service registry updates

6. **HubServer** (Main)
   - Orchestrates all components
   - Provides REST API endpoints for status and services

## Service Protocol

Services communicate with the Hub via TCP on port 7070 using a simple text protocol:

```
REGISTER::ServiceName::Host::Port
  - Register a new service
  - Response: OK::Service registered successfully

HEARTBEAT::ServiceName
  - Send a heartbeat to keep service alive
  - Response: OK::Heartbeat received

DEREGISTER::ServiceName
  - Unregister service
  - Response: OK::Service deregistered successfully

FETCH_SERVICES
  - Get list of all registered services
  - Response: OK::[{...json array of services...}]
```

## API Endpoints

### WebSocket

- **ws://localhost:7070/registry**
  - Real-time service registry updates
  - Sends SERVICE_REGISTRY_UPDATE messages

### REST API

- **GET /hub-status**

  - Server status, metrics, and statistics

- **GET /services**
  - List of all registered services

## Building & Running

### Build

```bash
cd services/hub-server
mvn clean package
```

### Run

```bash
# Default mode (non-SSL)
java -jar target/hub-server-1.0-SNAPSHOT.jar

# With SSL enabled (requires keystore)
java -Dssl.enabled=true -jar target/hub-server-1.0-SNAPSHOT.jar
```

### Run with Maven

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.hub.HubServer"
```

## Testing

### Test with Mock Service Client

```bash
# Terminal 1: Start Hub Server
cd services/hub-server
mvn clean package
java -jar target/hub-server-1.0-SNAPSHOT.jar

# Terminal 2: Connect first mock service
cd services/hub-server
java -cp target/hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient TestService1 9001

# Terminal 3: Connect second mock service (while first is running)
cd services/hub-server
java -cp target/hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient TestService2 9002

# Terminal 4: Check status via REST API
curl http://localhost:7070/hub-status

# Terminal 5: Get services list
curl http://localhost:7070/services
```

### Expected Behavior

1. **Service Registration**

   ```
   [HUB] ServiceRegistry Status
   [REGISTRY] ✓ Service registered: [TestService1] localhost:9001
   [REGISTRY] ✓ Service registered: [TestService2] localhost:9002
   ```

2. **Heartbeat Monitoring**

   ```
   [HEARTBEAT] Detected alive service: TestService1
   [HEARTBEAT] Detected alive service: TestService2
   ```

3. **Service Timeout (after 30 seconds without heartbeat)**

   ```
   [REGISTRY] ⏱ Service timeout: TestService1 (no heartbeat for 35s)
   ```

4. **WebSocket Broadcast**
   ```
   [WEBSOCKET] Broadcasted registry update to 1 dashboard(s)
   ```

## Concurrency Concepts Demonstrated

### 1. **ConcurrentHashMap** (Lesson 6)

- Thread-safe service registry
- No explicit synchronization needed for individual operations
- Used in ServiceRegistry class

```java
private final ConcurrentHashMap<String, ServiceInfo> registry = new ConcurrentHashMap<>();
```

### 2. **ExecutorService** (Lesson 6)

- Thread pool for managing service connections
- Fixed thread pool size of 20 threads
- Efficient handling of multiple concurrent connections

```java
threadPool = Executors.newFixedThreadPool(threadPoolSize);
threadPool.execute(new ServiceRegistryHandler(...));
```

### 3. **ScheduledExecutorService** (Lesson 6)

- Periodic heartbeat monitoring
- Runs health check every 5 seconds
- Single-threaded scheduler for reliability

```java
scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
```

### 4. **Thread-per-Client Model** (Lesson 3 & 6)

- Each service connection runs in its own thread
- Managed by ExecutorService for efficiency
- ServiceRegistryHandler.run() executes in thread pool

### 5. **Listener Pattern for Registry Changes**

- RegistryChangeListener interface for subscribers
- Observers notified when services register/deregister/timeout
- Enables loose coupling between components

## Performance Characteristics

- **Concurrent Connections**: Up to 20 simultaneous service connections
- **Heartbeat Check**: Every 5 seconds (configurable)
- **Service Timeout**: 30 seconds without heartbeat (configurable)
- **WebSocket Broadcasts**: Instant updates to dashboards

## Key Files

- `HubServer.java` - Main entry point
- `ServiceRegistry.java` - ConcurrentHashMap-based registry
- `ServiceRegistryServer.java` - TCP server with thread pool
- `ServiceRegistryHandler.java` - Connection handler (thread-per-client)
- `HeartbeatMonitor.java` - ScheduledExecutorService for health checks
- `WebSocketBroadcaster.java` - WebSocket dashboard updates
- `ServiceInfo.java` - Service data model
- `MockServiceClient.java` - Test client

## Troubleshooting

### Port Already in Use

```
Address already in use
```

Kill the process on port 7070:

```bash
# Windows
netstat -ano | findstr :7070
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :7070
kill -9 <PID>
```

### Services Not Appearing on Dashboard

- Ensure WebSocket is connected to `ws://localhost:7070/registry`
- Check browser console for WebSocket connection errors
- Verify services are sending heartbeats

### Services Timing Out

- Ensure MockServiceClient is running with correct heartbeat interval
- Check network connectivity between service and Hub
- Verify heartbeat timeout threshold (default 30 seconds)

## Next Steps

- Phase 2: API Gateway Service (HttpURLConnection)
- Phase 3: React Dashboard (Service registry visualization)
- Phase 4: Secure File Service (JSSE/SSLServerSocket)
- Phase 5: NIO Log Service (Selector-based logging)
- Phase 6: RMI Task Service (Remote Method Invocation)

## References

- Lesson 3: Basic Networking with ServerSocket
- Lesson 6: Multithreading & Concurrency (ExecutorService, ConcurrentHashMap)
- Lesson 7: Java NIO (for future Log Service)

## Author

Member 1 - Multithreading & Concurrency Implementation
