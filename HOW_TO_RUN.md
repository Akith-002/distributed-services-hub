# 🚀 How to Run This Project

## Quick Overview

This is a **Distributed Services Hub** with two main backend services:
1. **Hub Server** - Central service registry (ports 7070, 7071)
2. **API Gateway** - External API bridge (port 9001)

---

## ✅ Prerequisites

Before running, make sure you have:

- ✅ **Java 17 or higher** installed
- ✅ **Maven** installed (for building)
- ✅ Windows PowerShell or Command Prompt

### Check Your Installation

```powershell
# Check Java version
java -version
# Should show: java version "17" or higher

# Check Maven (if needed for building)
mvn --version
# Or use the full path if not in PATH
& "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd" --version
```

---

## 🏃 Quick Start (Services Already Built)

If the JAR files are already built (which they are), you can skip building and run directly:

### Step 1: Start Hub Server (Terminal 1)

```powershell
# Navigate to hub-server directory
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"

# Run the Hub Server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[TCP_SERVER] Started on port 7070
[HTTP_SERVER] WebSocket server started on port 7071
[HUB] Ready to accept service connections
```

✅ Hub Server is now running on:
- TCP: `localhost:7070`
- HTTP/WebSocket: `localhost:7071`

---

### Step 2: Start API Gateway (Terminal 2)

**Open a NEW terminal window** and run:

```powershell
# Navigate to api-gateway-service directory
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\api-gateway-service"

# Run the API Gateway
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
[HubClient] Connected to Hub successfully
[HubClient] ✓ Service registered with Hub
[WebSocketServer] ✓ WebSocket server started successfully
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
```

✅ API Gateway is now running on:
- HTTP/WebSocket: `localhost:9001`

---

### Step 3: Verify Services (Terminal 3)

**Open another terminal** and test:

```powershell
# Check Hub Server status
Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing

# Check registered services
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing

# Check API Gateway status
Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing
```

---

## 🔨 Building from Source

If you need to rebuild the services:

### Build Hub Server

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"

# Using Maven (full path)
& "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd" clean package

# Or use the build script
.\build.ps1
```

### Build API Gateway

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\api-gateway-service"

# Using Maven (full path)
& "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd" clean package

# Or use the build script
.\build.ps1
```

---

## 🎯 One-Command Start (Recommended)

You can start both services in new windows with one command:

```powershell
# Start Hub Server in new window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server'; java -jar target\hub-server-1.0-SNAPSHOT.jar"

# Wait 3 seconds for Hub to start
Start-Sleep -Seconds 3

# Start API Gateway in new window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\api-gateway-service'; java -jar target\api-gateway-service-1.0-SNAPSHOT.jar"

# Check status
Start-Sleep -Seconds 3
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing | ConvertFrom-Json | ConvertTo-Json
```

---

## 📊 Service Architecture

```
┌─────────────────────────────────────────┐
│    Hub Server (Central Registry)       │
│    - TCP: localhost:7070                │
│    - HTTP: localhost:7071               │
│    - WebSocket: ws://localhost:7071     │
└──────────────┬──────────────────────────┘
               │ Registration & Heartbeat
               │
┌──────────────▼──────────────────────────┐
│    API Gateway Service                  │
│    - HTTP: localhost:9001               │
│    - WebSocket: ws://localhost:9001/api │
│    - External API Calls (Weather, etc)  │
└─────────────────────────────────────────┘
```

---

## 🔗 Available Endpoints

### Hub Server (Port 7071)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `http://localhost:7071/hub-status` | Server status and stats |
| GET | `http://localhost:7071/services` | List all registered services |
| WebSocket | `ws://localhost:7071/registry` | Real-time service updates |

### API Gateway (Port 9001)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `http://localhost:9001/health` | Health check |
| GET | `http://localhost:9001/status` | Service status |
| WebSocket | `ws://localhost:9001/api` | Send commands (fetchWeather, etc) |

---

## 🧪 Testing the Services

### Test 1: Check Hub Status

```powershell
Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing | ConvertFrom-Json | Format-List
```

### Test 2: View Registered Services

```powershell
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing | ConvertFrom-Json | ConvertTo-Json
```

### Test 3: Test API Gateway

```powershell
Invoke-WebRequest -Uri "http://localhost:9001/health" -UseBasicParsing
```

### Test 4: Weather API (WebSocket Required)

The API Gateway can fetch weather data. You'll need a WebSocket client to test this feature.

---

## 🛑 Stopping the Services

To stop the services:

1. Press `Ctrl+C` in each terminal window
2. Or close the terminal windows

The services will gracefully shutdown and deregister from the Hub.

---

## ❗ Troubleshooting

### Port Already in Use

If you get "Address already in use" error:

```powershell
# Check what's using the port
netstat -ano | findstr :7071
netstat -ano | findstr :9001

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Maven Not Found

Use the full path to Maven:

```powershell
& "C:\Users\Mandrini Yashodha\maven\apache-maven-3.9.9\bin\mvn.cmd" clean package
```

### Java Not Found

Make sure Java is in your PATH or use full path:

```powershell
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar target\hub-server-1.0-SNAPSHOT.jar
```

---

## 📝 Project Structure

```
distributed-services-hub/
├── hub-server/                    # Central service registry
│   ├── src/main/java/            # Java source code
│   ├── target/                   # Compiled JAR files
│   │   └── hub-server-1.0-SNAPSHOT.jar
│   ├── pom.xml                   # Maven configuration
│   └── build.ps1                 # Build script
│
├── api-gateway-service/          # API Gateway service
│   ├── src/main/java/            # Java source code
│   ├── target/                   # Compiled JAR files
│   │   └── api-gateway-service-1.0-SNAPSHOT.jar
│   ├── pom.xml                   # Maven configuration
│   └── build.ps1                 # Build script
│
└── Documentation files (MD)
```

---

## ✨ Features

- ✅ **Multithreading** - Concurrent service handling
- ✅ **Service Registry** - Automatic service discovery
- ✅ **Heartbeat Monitoring** - Health checks every 5 seconds
- ✅ **WebSocket Communication** - Real-time updates
- ✅ **HTTP REST APIs** - Standard endpoints
- ✅ **External API Integration** - Weather data fetching

---

## 🎓 What This Project Demonstrates

1. **Network Programming Concepts:**
   - TCP Socket Communication
   - HTTP/REST APIs
   - WebSocket Real-time Communication
   - HttpURLConnection for external APIs

2. **Concurrency & Multithreading:**
   - ExecutorService thread pools
   - ConcurrentHashMap for thread-safe storage
   - ScheduledExecutorService for periodic tasks

3. **Distributed Systems:**
   - Service registry pattern
   - Health monitoring
   - Service discovery

---

## 🚀 Current Status

**Your project is CURRENTLY RUNNING!** ✅

Both services are active and operational. You can verify by running:

```powershell
Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing
```

---

## 📞 Need Help?

- Check the individual README files in `hub-server/` and `api-gateway-service/`
- Review the phase documentation: `PHASE_1_COMPLETE.md`, `PHASE_2_BUILD_RUN.md`
- Check the implementation plan: `IMPLEMENTATION_PLAN.md`

---

**Happy Coding!** 🎉
