# Member 1 - Hub Server Startup & Demonstration Guide

**Your Component:** Hub Server (Central Service Registry & Message Broker)  
**Your Core Networking Concepts:** ServerSocket, Multithreading, ConcurrentHashMap, WebSocket  
**Port:** 7070 (TCP), 7071 (WebSocket)

---

## 📚 Java Network Programming Concepts You're Demonstrating

### 1. **ServerSocket & TCP Communication** (Lesson 3)
- Creating a TCP server that listens on port 7070
- Accepting incoming connections from services
- Using `ServerSocket.accept()` to handle multiple clients

### 2. **Multithreading** (Lesson 6)
- **ExecutorService** with fixed thread pool (20 threads)
- **Thread-per-client pattern** - Each service gets its own handler thread
- **ScheduledExecutorService** for periodic heartbeat monitoring

### 3. **Thread Safety & Concurrency** (Lesson 6)
- **ConcurrentHashMap** for thread-safe service registry
- Multiple threads can read/write service registry simultaneously
- No race conditions or deadlocks

### 4. **WebSocket Communication**
- Real-time bidirectional communication with Dashboard
- Broadcasting updates to multiple WebSocket clients
- Message-based protocol for service updates

### 5. **Service Registry Pattern**
- Centralized service discovery
- Registration, heartbeat monitoring, deregistration
- Automatic dead service detection (30-second timeout)

---

## 🚀 How to Start Your Service

### Step 1: Navigate to Your Service Directory
```powershell
cd distributed-services-hub\hub-server
```

### Step 2: Build Your Service (First Time Only)
```powershell
.\build.ps1
```
Or manually:
```powershell
mvn clean package
```

### Step 3: Run Your Service
```powershell
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
================================================================================
  HUB SERVER - MEMBER 1
================================================================================

[ServiceRegistryServer] TCP Server started on port 7070
[HubServer] WebSocket endpoint created at ws://localhost:7071/registry
[HeartbeatMonitor] Heartbeat monitor started (checking every 5 seconds)

✓ HUB SERVER STARTED SUCCESSFULLY
✓ TCP Server: RUNNING on port 7070
✓ WebSocket Server: RUNNING on port 7071
✓ Thread Pool: 20 threads ready
✓ Heartbeat Monitor: ACTIVE

Waiting for services to register...
```

### Step 4: Keep This Running
**IMPORTANT:** Your Hub Server must be running BEFORE any other services start, as all services register with you.

---

## 🎯 How to Demonstrate Your Work

### Method 1: Command Line Demonstration

#### Show TCP Server & Service Registration
1. Start your Hub Server (as shown above)
2. Wait for other members to start their services
3. You should see output like:
```
[ServiceRegistryHandler] New service connection accepted
[ServiceRegistryHandler] Service registered: ApiGateway (localhost:9001)
[ServiceRegistryHandler] Service registered: SecureFileService (localhost:9090)
[ServiceRegistryHandler] Service registered: NioLogService (localhost:9091)
[ServiceRegistryHandler] Service registered: RmiTaskService (localhost:1099)
```

#### Show Heartbeat Monitoring
Watch the periodic heartbeat messages:
```
[HeartbeatMonitor] Checking service health...
[HeartbeatMonitor] ✓ ApiGateway: Healthy (last heartbeat: 2s ago)
[HeartbeatMonitor] ✓ SecureFileService: Healthy (last heartbeat: 1s ago)
[HeartbeatMonitor] ✓ NioLogService: Healthy (last heartbeat: 3s ago)
[HeartbeatMonitor] ✓ RmiTaskService: Healthy (last heartbeat: 2s ago)
```

#### Show Dead Service Detection
1. Stop any service (e.g., API Gateway)
2. Wait 30 seconds
3. Your Hub will detect and remove it:
```
[HeartbeatMonitor] ⚠ Service 'ApiGateway' timeout (35 seconds since last heartbeat)
[HeartbeatMonitor] Removing dead service: ApiGateway
[ServiceRegistry] Service deregistered: ApiGateway
```

### Method 2: UI Demonstration (Recommended for Presentation)

#### Start the Dashboard
```powershell
cd ..\..\multi-client-chat-frontend
npm run dev
```
Open browser: http://localhost:5173

#### Navigate to Tab 1: Service Registry
This is YOUR tab - it visualizes your Hub Server's service registry.

**What to Show:**
1. **Real-time Service List:**
   - See all connected services
   - Service name, host, port, status
   - Last heartbeat timestamp

2. **Live Updates:**
   - Start a service → See it appear immediately
   - Stop a service → See it disappear after 30 seconds

3. **Service Metadata:**
   - Registration time
   - Heartbeat count
   - Connection status

4. **Concurrent Access:**
   - Multiple services registering simultaneously
   - No conflicts or race conditions
   - Thread-safe operations

#### Demonstrate Multithreading
1. **Start all 4 services simultaneously** (in separate terminals):
   ```powershell
   # Terminal 1
   cd distributed-services-hub\api-gateway-service
   java -jar target\api-gateway-service-1.0-SNAPSHOT.jar

   # Terminal 2
   cd distributed-services-hub\secure-file-service
   java -jar target\secure-file-service-1.0-SNAPSHOT.jar

   # Terminal 3
   cd distributed-services-hub\nio-log-service
   java -jar target\nio-log-service-1.0-SNAPSHOT.jar

   # Terminal 4
   cd distributed-services-hub\rmi-task-service
   java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
   ```

2. **Watch your Hub handle all 4 concurrent registrations:**
   ```
   [ServiceRegistryHandler] Thread-1: Handling ApiGateway
   [ServiceRegistryHandler] Thread-2: Handling SecureFileService
   [ServiceRegistryHandler] Thread-3: Handling NioLogService
   [ServiceRegistryHandler] Thread-4: Handling RmiTaskService
   ✓ All 4 services registered successfully (no blocking)
   ```

3. **Show in UI:**
   - All 4 services appear in Tab 1
   - Each service has its own connection thread
   - No delays or blocking

---

## 🎓 Explaining Your Networking Concepts

### For Your Presentation/Demo, Explain:

#### 1. ServerSocket & TCP Server
**Code Reference:**
```java
ServerSocket serverSocket = new ServerSocket(7070);
while (running) {
    Socket clientSocket = serverSocket.accept(); // Blocks until connection
    executor.submit(new ServiceRegistryHandler(clientSocket, registry));
}
```

**Explain:**
- "I created a TCP server using `ServerSocket` on port 7070"
- "When a service connects, `accept()` returns a `Socket` for that client"
- "Each service connection is handled by a separate thread from the pool"

#### 2. ExecutorService & Thread Pool
**Code Reference:**
```java
private final ExecutorService executor = Executors.newFixedThreadPool(20);
```

**Explain:**
- "I use a thread pool with 20 worker threads"
- "When a service connects, I submit a `ServiceRegistryHandler` task to the pool"
- "This is more efficient than creating a new thread for each client"
- "Thread pool reuses threads, reducing overhead"

#### 3. ConcurrentHashMap for Thread Safety
**Code Reference:**
```java
private final ConcurrentHashMap<String, ServiceInfo> services;
```

**Explain:**
- "Multiple threads access the service registry simultaneously"
- "`ConcurrentHashMap` is thread-safe - no locks needed for basic operations"
- "One thread can register a service while another sends heartbeat"
- "Prevents race conditions and data corruption"

#### 4. Heartbeat Monitoring
**Code Reference:**
```java
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
```

**Explain:**
- "Every 5 seconds, I check when each service last sent a heartbeat"
- "If a service hasn't sent heartbeat for 30 seconds, it's considered dead"
- "Dead services are automatically removed from the registry"
- "This ensures the registry only shows active services"

#### 5. WebSocket Broadcasting
**Explain:**
- "When the registry changes, I broadcast updates to all connected Dashboard clients"
- "WebSocket provides real-time bidirectional communication"
- "Dashboard sees service status updates immediately"

---

## 📊 Demonstration Checklist

Use this checklist during your demonstration:

### Command Line Demo
- [ ] Start Hub Server successfully
- [ ] Show TCP server listening on port 7070
- [ ] Show WebSocket server on port 7071
- [ ] Show thread pool initialization
- [ ] Watch services register (all 4)
- [ ] Show concurrent registration (multiple threads)
- [ ] Show heartbeat monitoring output
- [ ] Stop one service and show timeout detection
- [ ] Restart service and show re-registration

### UI Demo (Tab 1: Service Registry)
- [ ] Open Dashboard at http://localhost:5173
- [ ] Navigate to Tab 1
- [ ] Show empty registry initially
- [ ] Start services and watch them appear
- [ ] Show real-time heartbeat updates
- [ ] Stop a service and show it disappear after 30s
- [ ] Restart and show re-registration
- [ ] Show all 4 services running simultaneously
- [ ] Explain the metadata (timestamp, port, host)

### Technical Explanation Points
- [ ] Explain ServerSocket and accept()
- [ ] Explain thread-per-client pattern
- [ ] Explain ExecutorService thread pool
- [ ] Explain ConcurrentHashMap thread safety
- [ ] Explain heartbeat timeout mechanism
- [ ] Explain WebSocket broadcasting
- [ ] Show how services send REGISTER messages
- [ ] Show how services send HEARTBEAT messages

---

## 🔧 Troubleshooting

### Issue: Port 7070 already in use
```powershell
# Find what's using the port
netstat -ano | findstr :7070

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Issue: Services not registering
- Check that Hub Server started successfully
- Check firewall isn't blocking port 7070
- Check service logs for connection errors

### Issue: WebSocket not connecting
- Check Dashboard is using ws://localhost:7071/registry
- Check browser console for errors
- Try refreshing the Dashboard

---

## 📝 Key Points for Your Report

Include these in your written documentation:

### Architecture
- Hub Server acts as central service registry
- Uses message broker pattern
- All services register with Hub on startup

### Networking Concepts
1. **ServerSocket**: Accepts TCP connections on port 7070
2. **Multithreading**: ExecutorService with 20-thread pool
3. **Thread-per-client**: Each service gets dedicated handler thread
4. **Thread Safety**: ConcurrentHashMap prevents race conditions
5. **Heartbeat Monitoring**: ScheduledExecutorService for periodic checks
6. **WebSocket**: Real-time updates to Dashboard

### Why These Concepts Matter
- **Scalability**: Thread pool handles many concurrent services
- **Reliability**: Heartbeat monitoring detects failures
- **Performance**: Non-blocking concurrent operations
- **Real-time**: WebSocket provides instant updates

---

## 🎬 Presentation Script Example

**1. Introduction (30 seconds)**
"I implemented the Hub Server, which is the central registry for all microservices. It demonstrates multithreading, thread safety, and service discovery patterns."

**2. Show Architecture (30 seconds)**
"The Hub runs a TCP server on port 7070. When services start, they connect and register. I use a thread pool to handle multiple services concurrently."

**3. Code Walkthrough (1 minute)**
"Here's the ServerSocket accepting connections. Each connection gets a thread from the pool. The service registry uses ConcurrentHashMap for thread-safe access."

**4. Live Demo (2 minutes)**
"Let me start the Hub Server... Now I'll start all 4 services simultaneously... Notice how they all register concurrently - no blocking. The heartbeat monitor runs every 5 seconds. Now I'll stop one service... After 30 seconds, it's automatically removed."

**5. UI Demo (1 minute)**
"In the Dashboard Tab 1, you can see real-time service status. Watch as I start and stop services - updates are instant via WebSocket."

---

## 📚 Study References

Review these lessons before your demo:

- **Lesson 3:** ServerSocket, Socket, TCP communication
- **Lesson 6:** Multithreading, ExecutorService, Thread pools, ConcurrentHashMap
- **Lesson 11:** WebSocket basics

---

## ✅ Pre-Demo Checklist

Before your demonstration:

- [ ] Hub Server builds successfully (`.\build.ps1`)
- [ ] Can start Hub Server without errors
- [ ] Port 7070 is available
- [ ] Port 7071 is available (WebSocket)
- [ ] All other services are built and ready
- [ ] Dashboard is installed (`npm install`)
- [ ] You understand the protocol (REGISTER, HEARTBEAT, DEREGISTER)
- [ ] You can explain ConcurrentHashMap
- [ ] You can explain thread pool advantages
- [ ] You practiced the demo at least once

---

**Good luck with your demonstration! Your Hub Server is the foundation that makes all other services work together.**
