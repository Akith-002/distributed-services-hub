# Quick Start - Phase 1: Hub Server

## ⚡ 5-Minute Setup

### Prerequisites

- Java 17 (JDK) installed and `JAVA_HOME` set to the JDK 17 installation (e.g. `C:\Program Files\Java\jdk-17.0.12`).
- Maven installed (or use the Maven wrapper).
- Recommended: You have run the permanent JAVA_HOME update (or set it for your session).

### 1. Build the Hub Server

```bash
cd services/hub-server
mvn clean package -DskipTests
```

### 2. Run the Hub Server

**Option A: Using Batch Script (Easiest)**

```bash
cd services/hub-server
run-hub-server.bat
```

**Option B: Using PowerShell (if JAVA_HOME already set)**

```powershell
cd services/hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

Note: If you haven't set `JAVA_HOME` permanently, you can set it for the current session before running:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.12"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**

```
[TCP_SERVER] Started on port 7070 with thread pool size 20
[HEARTBEAT] Monitor started - checking every 5 seconds (timeout: 30s)
[main] INFO io.javalin.Javalin - Listening on http://localhost:7071/
[HTTP_SERVER] WebSocket server started on port 7071
[HTTP_SERVER] WebSocket endpoint: ws://localhost:7071/registry
[HTTP_SERVER] Status endpoint: http://localhost:7071/hub-status
[HTTP_SERVER] Services endpoint: http://localhost:7071/services
======================================================================
  DISTRIBUTED SERVICES HUB - CENTRAL REGISTRY
  Member 1 - Multithreading & Concurrency Implementation
======================================================================
[HUB] Ready to accept service connections
```

### 3. Test with Mock Services (in separate terminals)

**Terminal 2: Start API Gateway Service**

Using Batch Script (Easiest):

```bash
cd services/hub-server
run-mock-service.bat ApiGateway 9001
```

Using PowerShell (assuming `JAVA_HOME` is set):

```powershell
cd services/hub-server
java -cp target/hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient ApiGateway 9001
```

**Terminal 3: Start File Service**

Using Batch Script (Easiest):

```bash
cd services/hub-server
run-mock-service.bat FileService 9090
```

Using PowerShell (assuming `JAVA_HOME` is set):

```powershell
cd services/hub-server
java -cp target/hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient FileService 9090
```

### 4. Verify Services (in Terminal 4)

**Using PowerShell or Command Prompt:**

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.12"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
curl http://localhost:7071/services
```

**Or using Invoke-WebRequest in PowerShell:**

```powershell
Invoke-WebRequest -Uri http://localhost:7071/services | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json
```

**Expected Response:**

```json
{
  "services": [
    {
      "name": "ApiGateway",
      "host": "localhost",
      "port": 9001,
      "status": "online",
      "registered": "14:30:00"
    },
    {
      "name": "FileService",
      "host": "localhost",
      "port": 9090,
      "status": "online",
      "registered": "14:31:00"
    }
  ]
}
```

## 📊 What to Observe

### Terminal 1 (Hub Server)

Shows:

- ✅ Service registrations
- ✅ Heartbeat acknowledgments
- ✅ WebSocket broadcasts
- ✅ Timeout detection (after 30s)

### Terminal 2 & 3 (Mock Services)

Show:

- ✅ Registration confirmation
- ✅ Heartbeat sent every 10 seconds
- ✅ Graceful disconnect on Ctrl+C

### Terminal 4 (Verification)

Shows:

- ✅ Current service list
- ✅ Service status and endpoints
- ✅ Registration timestamps

## 🎯 Test Scenarios

### Scenario 1: Service Registration

1. Keep Hub running
2. Start Service1 - see "✓ Service registered"
3. Start Service2 - see both services in API response

### Scenario 2: Service Timeout

1. Start Service1 and Service2
2. Stop Service1 (Ctrl+C)
3. Wait 30+ seconds
4. See "⏱ Service timeout" in Hub console
5. Service1 no longer appears in API response

### Scenario 3: Heartbeat Monitoring

1. Start Service
2. Watch Hub console for "Heartbeat acknowledged" every 10 seconds
3. Service stays "online" as long as heartbeats arrive

## 🔍 REST API Endpoints

```bash
# Get hub status
curl http://localhost:7071/hub-status

# Get all services
curl http://localhost:7071/services
```

## 🔌 WebSocket Endpoint

Connect any WebSocket client to:

```
ws://localhost:7071/registry
```

You'll receive real-time updates:

```json
{
  "type": "SERVICE_REGISTRY_UPDATE",
  "payload": {
    "services": [...],
    "totalServices": 2,
    "onlineServices": 2
  }
}
```

## 🆘 Troubleshooting

### "Error: could not open `jvm.cfg`" (jre/JDK mismatch)

**Problem:** A broken or older Java (e.g. Java 8 runtime) is being picked up instead of your JDK 17.

**If you've already updated JAVA_HOME permanently:**

- Close all open terminals (including VS Code). Open a _new_ terminal and verify:

```powershell
java -version
echo $env:JAVA_HOME
```

You should see `java version "17.x.x"` and `JAVA_HOME` pointing to your JDK 17 path.

**If you still see the error:**

- Use the batch helpers shipped with the project:

```bash
cd services/hub-server
run-hub-server.bat          # For Hub Server
run-mock-service.bat ApiGateway 9001   # For mock services
```

**If you prefer to set for the current session (temporary):**

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.12"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### "Address already in use"

**Problem:** Port 7070 or 7071 is occupied

**Solution (PowerShell):**

```powershell
# Kill process on port 7070 (TCP Registry)
$process = Get-NetTCPConnection -LocalPort 7070 -ErrorAction SilentlyContinue
if ($process) { Stop-Process -Id $process.OwningProcess -Force }

# Kill process on port 7071 (WebSocket/HTTP)
$process = Get-NetTCPConnection -LocalPort 7071 -ErrorAction SilentlyContinue
if ($process) { Stop-Process -Id $process.OwningProcess -Force }
```

**Solution (Command Prompt):**

```bash
netstat -ano | findstr :7070
taskkill /PID <PID> /F

netstat -ano | findstr :7071
taskkill /PID <PID> /F
```

### "Maven not found"

```bash
# Ensure Maven is installed and in PATH
mvn --version

# Or use Maven wrapper if available
./mvnw clean package
```

### Services not showing up

- Verify services are sending heartbeats (check Terminal 2/3 output)
- Check Hub console for registration messages
- Ensure ports 7070 and 7071 are not blocked by firewall
- Check that mock services connected successfully

## 📚 Next Steps

1. ✅ **Phase 1 (Current):** Hub Server - COMPLETE
2. ⏳ **Phase 2:** API Gateway Service (HttpURLConnection)
3. ⏳ **Phase 3:** React Dashboard Update
4. ⏳ **Phase 4:** Secure File Service (JSSE)
5. ⏳ **Phase 5:** NIO Log Service
6. ⏳ **Phase 6:** RMI Task Service

## 💡 Key Concepts Demonstrated

- **ConcurrentHashMap** - Thread-safe service registry
- **ExecutorService** - Thread pool for connections
- **ScheduledExecutorService** - Periodic health checks
- **Heartbeat Pattern** - Service liveliness detection
- **WebSocket Broadcasting** - Real-time updates
- **Protocol Design** - Simple text-based messaging

## 📖 Documentation

For more details, see:

- `services/hub-server/README.md` - Full documentation
- `IMPLEMENTATION_PLAN.md` - Architecture overview
- `PHASE_1_COMPLETE.md` - Complete Phase 1 summary

---

**Need Help?** Check the FAQ in services/hub-server/README.md
