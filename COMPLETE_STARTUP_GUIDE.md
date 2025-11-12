# Complete Team Startup & Demonstration Guide

**Distributed Services Hub - Network Programming Project**  
**Last Updated:** November 12, 2025

---

## 📚 QUICK NAVIGATION

### Individual Member Guides
- **[Member 1 Guide](MEMBER_1_STARTUP_GUIDE.md)** - Hub Server (Multithreading)
- **[Member 2 Guide](MEMBER_2_STARTUP_GUIDE.md)** - API Gateway + Dashboard (HttpURLConnection)
- **[Member 3 Guide](MEMBER_3_STARTUP_GUIDE.md)** - Secure File Service (JSSE/SSL)
- **[Member 4 Guide](MEMBER_4_STARTUP_GUIDE.md)** - NIO Log Service (Non-blocking I/O)
- **[Member 5 Guide](MEMBER_5_STARTUP_GUIDE.md)** - RMI Task Service (Remote Method Invocation)

### Project Documentation
- **[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)** - Detailed project plan and architecture
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Complete implementation summary

---

## 🎯 PROJECT OVERVIEW

### What We Built

A **Distributed Services Hub** - a microservices architecture demonstrating 5 advanced Java networking concepts:

1. **Hub Server** - Central service registry with multithreading and concurrency
2. **API Gateway** - External API integration using HttpURLConnection
3. **Secure File Service** - SSL/TLS encrypted file operations with JSSE
4. **NIO Log Service** - High-performance logging with non-blocking I/O
5. **RMI Task Service** - Distributed computing with remote method invocation

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REACT DASHBOARD                          │
│        http://localhost:5173                                │
│  ┌──────┬──────┬──────┬──────┬──────┐                      │
│  │ Tab1 │ Tab2 │ Tab3 │ Tab4 │ Tab5 │                      │
│  └──┬───┴──┬───┴──┬───┴──┬───┴──┬───┘                      │
└─────┼──────┼──────┼──────┼──────┼──────────────────────────┘
      │      │      │      │      │
      └──────┴──────┴──────┴──────┘
              WebSocket (7071)
                    │
      ┌─────────────▼─────────────┐
      │      HUB SERVER            │
      │   Port 7070 (TCP)          │
      │   Port 7071 (WebSocket)    │
      └───┬────┬────┬────┬─────────┘
          │    │    │    │
    ┌─────┘    │    │    └─────┐
    │          │    │          │
    ▼          ▼    ▼          ▼
API Gateway  JSSE  NIO       RMI
Port 9001    9090  9091      1099
```

---

## 🚀 COMPLETE STARTUP PROCEDURE

### Prerequisites

Before starting, ensure you have:

- ✅ Java 17 or higher
- ✅ Maven 3.6+
- ✅ Node.js 16+ and npm
- ✅ 6 terminal windows available

### Build All Services (One-Time Setup)

**Run these commands ONCE before first startup:**

```powershell
# Build Hub Server
cd distributed-services-hub\hub-server
.\build.ps1

# Build API Gateway
cd ..\api-gateway-service
.\build.ps1

# Build Secure File Service (includes keystore generation)
cd ..\secure-file-service
.\generate-keystore.ps1
.\build.ps1

# Build NIO Log Service
cd ..\nio-log-service
.\build.ps1

# Build RMI Task Service
cd ..\rmi-task-service
.\build.ps1

# Install Dashboard dependencies
cd ..\..\multi-client-chat-frontend
npm install
```

### Startup Order (CRITICAL!)

**⚠️ IMPORTANT:** Services MUST be started in this exact order!

#### Terminal 1: Hub Server (START FIRST!)
```powershell
cd distributed-services-hub\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

**Wait for:**
```
✓ HUB SERVER STARTED SUCCESSFULLY
✓ TCP Server: RUNNING on port 7070
✓ WebSocket Server: RUNNING on port 7071
```

#### Terminal 2: API Gateway Service
```powershell
cd distributed-services-hub\api-gateway-service
java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
```

**Wait for:**
```
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
✓ Hub Registration: SUCCESS
```

#### Terminal 3: Secure File Service
```powershell
cd distributed-services-hub\secure-file-service
java -jar target\secure-file-service-1.0-SNAPSHOT.jar
```

**Wait for:**
```
✓ SECURE FILE SERVICE STARTED SUCCESSFULLY
✓ SSL Server: RUNNING on port 9090
```

#### Terminal 4: NIO Log Service
```powershell
cd distributed-services-hub\nio-log-service
java -jar target\nio-log-service-1.0-SNAPSHOT.jar
```

**Wait for:**
```
✓ NIO LOG SERVICE STARTED SUCCESSFULLY
✓ NIO Server: RUNNING on port 9091
```

#### Terminal 5: RMI Task Service
```powershell
cd distributed-services-hub\rmi-task-service
java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
```

**Wait for:**
```
✓ RMI TASK SERVICE STARTED SUCCESSFULLY
✓ RMI Registry: RUNNING on port 1099
```

#### Terminal 6: React Dashboard
```powershell
cd multi-client-chat-frontend
npm run dev
```

**Wait for:**
```
  ➜  Local:   http://localhost:5173/
```

**Open browser:** http://localhost:5173

### Verify System is Running

**In Hub Server terminal, you should see:**
```
[ServiceRegistryHandler] Service registered: ApiGateway (localhost:9001)
[ServiceRegistryHandler] Service registered: SecureFileService (localhost:9090)
[ServiceRegistryHandler] Service registered: NioLogService (localhost:9091)
[ServiceRegistryHandler] Service registered: RmiTaskService (localhost:1099)
```

**In Dashboard (Tab 1), you should see:**
- ✅ 4 services listed and healthy
- ✅ Green status indicators
- ✅ Heartbeat timestamps updating

---

## 🎯 DEMONSTRATION GUIDE

### Dashboard Tab Overview

| Tab | Service | Member | Demo Focus |
|-----|---------|--------|-----------|
| **Tab 1** | Service Registry | Member 1 | Real-time service monitoring |
| **Tab 2** | API Gateway | Member 2 | External API calls via HttpURLConnection |
| **Tab 3** | Secure File Service | Member 3 | SSL/TLS encrypted file upload/download |
| **Tab 4** | NIO Log Stream | Member 4 | Non-blocking I/O log streaming |
| **Tab 5** | RMI Task Runner | Member 5 | Remote method invocation |

### Demo Flow (25-30 minutes)

#### Introduction (2 minutes)
- **Presenter:** Any team member or designated leader
- Show architecture diagram
- Explain microservices pattern
- Point out message broker (Hub)

#### Member 1: Hub Server (5 minutes)

**Dashboard Tab 1 Demo:**
1. Show all 4 services registered
2. Explain real-time heartbeat monitoring
3. Stop API Gateway service (Ctrl+C in Terminal 2)
4. Wait 30 seconds - show service disappears
5. Restart API Gateway
6. Show it re-registers automatically

**Code Highlight:**
```java
ConcurrentHashMap<String, ServiceInfo> services;
ScheduledExecutorService heartbeatMonitor;
```

**Explain:**
- ServerSocket on port 7070
- Thread pool with 20 threads
- ConcurrentHashMap for thread-safe registry
- Heartbeat timeout detection (30 seconds)

#### Member 2: API Gateway + Dashboard (5 minutes)

**Dashboard Tab 2 Demo:**
1. Enter city: "Colombo"
2. Click "Fetch Weather"
3. Show weather data appears
4. Try "London", "New York", "Tokyo"
5. Show each city returns different data

**Code Highlight:**
```java
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
int responseCode = conn.getResponseCode(); // 200 OK
```

**Explain:**
- HttpURLConnection (NO third-party libraries!)
- HTTP GET to Open-Meteo API
- Manual JSON parsing
- React Dashboard with WebSocket

#### Member 3: Secure File Service (5 minutes)

**Dashboard Tab 3 Demo:**
1. Show file management interface
2. Click "Choose File" and select a text file
3. Click "Upload File"
4. Show success message and file appears in list
5. Upload 2-3 more files (different sizes)
6. Show file list with all uploaded files
7. Click "Download" on a file
8. Verify file downloads to browser
9. Optional: Delete a file

**Code Highlight:**
```java
SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
SSLServerSocket sslServer = (SSLServerSocket) factory.createServerSocket(9090);
```

**Explain:**
- SSLServerSocket (NOT regular ServerSocket)
- KeyStore with self-signed certificate
- TLS handshake process
- All file uploads/downloads encrypted with TLS 1.3
- Show in logs: SSL handshake, cipher suite, file operations
- Every connection is encrypted end-to-end

#### Member 4: NIO Log Service (5 minutes)

**Dashboard Tab 4 Demo:**
1. Show empty log viewer
2. Open new terminal, run:
   ```powershell
   cd distributed-services-hub\nio-log-service
   .\test-log-client.ps1
   ```
3. Watch logs appear in real-time
4. Run 2-3 test clients simultaneously
5. Show all logs streaming from SINGLE thread

**Code Highlight:**
```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false); // Non-blocking!
Selector selector = Selector.open();
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (running) {
    selector.select(); // Wait for events
    // Handle all ready channels
}
```

**Explain:**
- Java NIO (Non-blocking I/O)
- Selector monitors multiple channels
- Single thread handles all connections
- Event-driven architecture
- Much more scalable than thread-per-client

#### Member 5: RMI Task Service (5 minutes)

**Dashboard Tab 5 Demo:**
1. Select "Calculate Pi" from dropdown
2. Click "Execute Task"
3. Show result: Pi ≈ 3.141...
4. Try other tasks:
   - Fibonacci-20
   - Prime Check (10007)
   - Factorial-10

**Optional CLI Demo:**
```powershell
# In new terminal
cd distributed-services-hub\rmi-task-service
.\run-client.ps1
```
Interactive commands:
- `calculate-pi`
- `fibonacci-20`
- `prime-check-10007`

**Code Highlight:**
```java
// Remote interface
public interface TaskService extends Remote {
    String executeTask(String taskName) throws RemoteException;
}

// Server: Bind to registry
Registry registry = LocateRegistry.createRegistry(1099);
Naming.rebind("rmi://localhost:1099/TaskService", service);

// Client: Lookup and call
TaskService service = (TaskService) registry.lookup("TaskService");
String result = service.executeTask("calculate-pi");
```

**Explain:**
- Java RMI (Remote Method Invocation)
- Remote interface extends Remote
- RMI Registry on port 1099
- Transparent remote calls
- Method executes on server JVM, result returns to client

#### Integration Demo (3 minutes)

**Show all services working together:**

1. Use Tab 2 - Fetch weather for "Paris"
2. Switch to Tab 4 - See log: "API_GATEWAY: Weather fetched"
3. Use Tab 3 - Upload a file
4. Switch to Tab 4 - See log: "SECURE_FILE_SERVICE: File uploaded via SSL"
5. Use Tab 5 - Execute RMI task
6. Switch to Tab 4 - See log: "RMI_TASK_SERVICE: Task executed"
7. Check Tab 1 - All services still healthy with green status

**Explain:**
- Hub Server routes all commands
- Services don't talk directly to each other
- Message broker pattern
- NIO Log Service receives logs from all services
- WebSocket provides real-time updates
- Secure File Service encrypts all file transfers with TLS 1.3

---

## 🎓 NETWORKING CONCEPTS SUMMARY

### What Each Member Demonstrates

| Member | Primary Concept | Key Classes | Lesson Reference |
|--------|----------------|-------------|-----------------|
| **1** | Multithreading & Concurrency | ServerSocket, ExecutorService, ConcurrentHashMap | Lesson 6 |
| **2** | HTTP Communication | HttpURLConnection, URL | Lesson 5 |
| **3** | Secure Sockets | SSLServerSocket, SSLSocket, KeyStore | Lesson 8 |
| **4** | Non-blocking I/O | ServerSocketChannel, Selector, ByteBuffer | Lesson 7 |
| **5** | Remote Method Invocation | Remote, UnicastRemoteObject, Registry | Lesson 9 |

### Additional Concepts Covered

- ✅ TCP client-server communication
- ✅ Thread pools and thread management
- ✅ Thread-safe data structures
- ✅ Event-driven architecture
- ✅ Service discovery and registration
- ✅ Health monitoring with heartbeats
- ✅ Message broker pattern
- ✅ WebSocket bidirectional communication
- ✅ JSON data parsing
- ✅ TLS/SSL encryption
- ✅ Certificate management
- ✅ Distributed computing
- ✅ Microservices architecture

---

## 🔧 TROUBLESHOOTING

### Common Issues

#### Port Already in Use
```powershell
# Find process using port (replace 7070 with your port)
netstat -ano | findstr :7070

# Kill process (replace PID)
taskkill /PID <PID> /F
```

#### Service Not Registering with Hub
- Check Hub Server is running FIRST
- Check service logs for connection errors
- Verify port 7070 is accessible
- Check firewall settings

#### Dashboard Not Connecting
- Check WebSocket URL: `ws://localhost:7071/registry`
- Open browser console (F12) for errors
- Verify Hub Server WebSocket is running
- Try refreshing the page

#### npm install Fails
```powershell
npm cache clean --force
rm -r node_modules
npm install
```

### Quick Restart

If something goes wrong, restart everything:

1. Stop all services (Ctrl+C in each terminal)
2. Wait 5 seconds
3. Restart in order: Hub → API Gateway → Secure File → NIO Log → RMI → Dashboard

---

## ✅ PRE-DEMONSTRATION CHECKLIST

### One Day Before

**Technical:**
- [ ] All services build successfully
- [ ] Hub Server starts without errors
- [ ] All 4 services register with Hub
- [ ] Dashboard loads correctly
- [ ] All 5 tabs work
- [ ] Internet connection available (for API Gateway)
- [ ] Test on presentation machine if different from development machine

**Team:**
- [ ] Each member has reviewed their startup guide
- [ ] Each member has practiced their demo (5 min each)
- [ ] Everyone knows the startup order
- [ ] Everyone can explain their networking concept
- [ ] Roles assigned (who demos what)

**Backup:**
- [ ] Full project on USB drive
- [ ] Screenshots/screen recording of working system
- [ ] Alternative demo plan if internet fails

### 30 Minutes Before Presentation

- [ ] Open 6 terminals
- [ ] Navigate each terminal to correct directory
- [ ] Test complete startup sequence
- [ ] Verify all services register
- [ ] Open Dashboard in browser
- [ ] Test each tab quickly
- [ ] Prepare any test data/files
- [ ] Keep terminals visible on screen

### 5 Minutes Before Presentation

- [ ] All services running
- [ ] Dashboard open
- [ ] Tab 1 shows all 4 services healthy
- [ ] Screen sharing ready (if virtual presentation)
- [ ] Calm and confident!

---

## 🏆 PRESENTATION TIPS

### General Tips

1. **Speak Clearly** - Explain concepts in simple terms
2. **Show, Don't Just Tell** - Use live demos, not just code
3. **Highlight Key Code** - Point out networking-specific code
4. **Connect to Theory** - Reference course lessons
5. **Be Enthusiastic** - Show pride in your work!

### Time Management

- **Introduction:** 2 min
- **Each Member:** 5 min (25 min total)
- **Integration Demo:** 3 min
- **Q&A Buffer:** 5 min
- **Total:** 35 min (safe with 30 min limit)

### If Something Breaks During Demo

1. **Stay Calm** - Technical issues happen
2. **Show Logs** - Demonstrate you understand the problem
3. **Have Screenshots** - Backup proof it worked
4. **Explain Anyway** - Can still explain concepts
5. **Move On** - Don't spend too long troubleshooting live

---

## 📚 ADDITIONAL RESOURCES

### For Deeper Understanding

- **Java NIO Tutorial:** https://docs.oracle.com/javase/tutorial/essential/io/
- **Java RMI Guide:** https://docs.oracle.com/javase/tutorial/rmi/
- **JSSE Reference:** https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html

### Project Documentation

- Each service has its own `README.md` with detailed information
- Individual member startup guides cover all concepts in depth
- `IMPLEMENTATION_PLAN.md` has complete architectural details
- `IMPLEMENTATION_SUMMARY.md` summarizes all achievements

---

## 🎯 LEARNING OUTCOMES

### What You've Mastered

By completing this project, your team has demonstrated:

1. ✅ **Advanced Java Programming** - Complex multi-service architecture
2. ✅ **Network Programming** - 5 distinct networking concepts
3. ✅ **Concurrent Programming** - Multithreading, thread safety
4. ✅ **Security** - SSL/TLS implementation
5. ✅ **Performance** - Non-blocking I/O optimization
6. ✅ **Distributed Systems** - RMI and microservices
7. ✅ **Full-Stack Development** - Java backend + React frontend
8. ✅ **Software Architecture** - Message broker, service registry patterns
9. ✅ **Testing & Documentation** - Comprehensive guides and tests
10. ✅ **Teamwork & Collaboration** - Coordinated 5-member team effort

---

## 🌟 FINAL WORDS

**Congratulations!** You've built a production-quality distributed system demonstrating advanced Java networking concepts. This project showcases:

- **Technical Excellence** - All core networking concepts properly implemented
- **Professional Quality** - Complete with testing, documentation, and UI
- **Real-World Relevance** - Microservices architecture used in industry
- **Team Collaboration** - Coordinated effort across 5 components

**You're ready to demonstrate!** Trust your preparation, follow the guides, and show confidence in your work.

**Good luck with your presentation! 🚀**

---

**Project:** Distributed Services Hub  
**Team Size:** 5 Members  
**Services:** 5 Microservices  
**Lines of Code:** 3,000+  
**Documentation:** Complete  
**Status:** ✅ Production Ready

**Last Updated:** November 12, 2025
