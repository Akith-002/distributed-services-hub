# NIO Log Service

**Member 4's Core Concept:** Java NIO (Non-blocking I/O) with Selector (Lesson 7)

## Overview

High-performance logging service using Java NIO's `ServerSocketChannel` and `Selector` for handling multiple concurrent log streams with a single-threaded event loop.

## Core Networking Concepts Demonstrated

### 1. **ServerSocketChannel** (NIO version of ServerSocket)
- Non-blocking server socket
- Configured with `configureBlocking(false)`
- Registered with Selector for event-driven I/O

### 2. **Selector** (Event multiplexing)
- Single-threaded event loop using `selector.select()`
- Handles multiple channels concurrently
- Event types: `OP_ACCEPT` (new connections), `OP_READ` (incoming data)

### 3. **SocketChannel** (NIO version of Socket)
- Non-blocking client connections
- Uses `ByteBuffer` for efficient data transfer
- Registered with Selector for read events

### 4. **Non-blocking I/O Pattern**
```
ServerSocketChannel (port 9091)
    ↓
Selector.select() → Wait for events
    ↓
if (key.isAcceptable()) → Accept new connection
if (key.isReadable()) → Read log data
    ↓
Process log → Write to file + Forward to Hub
```

## Architecture

```
┌─────────────────────────────────────────────┐
│         NIO LOG SERVICE (Port 9091)         │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │   ServerSocketChannel + Selector    │   │
│  │   (Single-threaded event loop)      │   │
│  └──────────┬──────────────────────────┘   │
│             │                               │
│             ├─► LogWriter                   │
│             │   (Write to file)             │
│             │                               │
│             └─► HubForwarder                │
│                 (Forward to Dashboard)      │
└─────────────────────────────────────────────┘
         ▲                      │
         │                      │
    Log Messages            Forwarded to
    (from services)         Hub/Dashboard
```

## Features

✅ **Non-blocking I/O:** Single thread handles multiple concurrent connections  
✅ **Selector-based:** Efficient event-driven architecture  
✅ **File Logging:** Persistent logs with daily rotation  
✅ **Real-time Forwarding:** Logs sent to Hub for Dashboard display  
✅ **Hub Integration:** Automatic registration and heartbeat  

## Port Configuration

- **NIO Server:** 9091 (accepts log messages)
- **Hub Connection:** 7070 (registration + log forwarding)

## Build & Run

### Build
```powershell
.\build.ps1
# or
mvn clean package
```

### Run
```powershell
.\run.ps1
# or
java -jar target\nio-log-service-1.0-SNAPSHOT.jar
```

## Testing

### Test with simple client
```powershell
.\test-log-client.ps1
```

### Send logs from other services
All services should connect to port 9091 and send log messages:

```java
Socket logSocket = new Socket("localhost", 9091);
PrintWriter logWriter = new PrintWriter(logSocket.getOutputStream(), true);
logWriter.println("SERVICE_NAME: Log message here");
```

## Log Format

**Input from services:**
```
API_GATEWAY: Weather fetched for Colombo (28.5°C)
SECURE_FILE_SERVICE: File test.txt stored (1024 bytes)
HUB: Service 'API_GATEWAY' registered
```

**Output to file:**
```
[2025-11-12 14:30:45] API_GATEWAY: Weather fetched for Colombo (28.5°C)
[2025-11-12 14:30:46] SECURE_FILE_SERVICE: File test.txt stored (1024 bytes)
[2025-11-12 14:30:47] HUB: Service 'API_GATEWAY' registered
```

**Forwarded to Hub (JSON):**
```json
{
  "result_from": "NIO_SERVICE",
  "data": "LOG: API_GATEWAY: Weather fetched for Colombo (28.5°C)"
}
```

## File Structure

```
nio-log-service/
├── pom.xml
├── build.ps1 / build.bat
├── run.ps1
├── test-log-client.ps1
├── logs/
│   └── service-2025-11-12.log
└── src/main/java/com/example/logservice/
    ├── NioLogService.java       (Main - starts service)
    ├── LogServer.java            (NIO server with Selector)
    ├── LogWriter.java            (File writer with rotation)
    ├── HubForwarder.java         (Forwards logs to Hub)
    └── HubClient.java            (Registration & heartbeat)
```

## UI Demonstration (Dashboard Tab 4)

**Your UI Demo:** The "NIO Log Stream" tab shows real-time logs from all services.

**Live Demo Steps:**
1. Navigate to NIO Log Stream tab on Dashboard
2. As services run, logs appear in real-time:
   ```
   [Hub] Service 'API_GATEWAY' registered
   [ApiGateway] Weather fetched for Colombo (28.5°C)
   [JSSE_SERVICE] SSL handshake completed
   [RMI_SERVICE] Task 'calculate-pi' completed
   ```
3. Auto-scrolling keeps latest logs visible
4. This proves single-threaded NIO Selector handles multiple concurrent streams

## Key Implementation Details

### Selector Event Loop (LogServer.java)
```java
while (running) {
    selector.select(); // Block until events ready
    
    Set<SelectionKey> keys = selector.selectedKeys();
    for (SelectionKey key : keys) {
        if (key.isAcceptable()) {
            handleAccept(key);  // New connection
        } else if (key.isReadable()) {
            handleRead(key);    // Data available
        }
    }
}
```

### Non-blocking Accept
```java
SocketChannel client = serverChannel.accept();
client.configureBlocking(false);
client.register(selector, SelectionKey.OP_READ);
```

### Non-blocking Read
```java
ByteBuffer buffer = ByteBuffer.allocate(4096);
int bytesRead = channel.read(buffer);
if (bytesRead > 0) {
    buffer.flip();
    String message = StandardCharsets.UTF_8.decode(buffer).toString();
    processLogMessage(message);
}
```

## Troubleshooting

**Issue:** Service won't start
- Check if port 9091 is already in use
- Run: `netstat -ano | findstr :9091`

**Issue:** Not connecting to Hub
- Make sure Hub is running on port 7070
- Check Hub console for registration message

**Issue:** Logs not appearing in file
- Check `logs/` directory exists
- Verify write permissions

**Issue:** Dashboard not showing logs
- Check HubForwarder connection
- Verify Hub is broadcasting results to Dashboard

## Why NIO?

**Traditional I/O (One thread per connection):**
- 100 connections = 100 threads
- High memory overhead
- Context switching overhead

**Java NIO with Selector (One thread for all connections):**
- 100 connections = 1 thread
- Low memory footprint
- No context switching
- Better scalability

## Success Criteria

✅ Uses `ServerSocketChannel` (NOT `ServerSocket`)  
✅ Uses `Selector` for event multiplexing  
✅ Single-threaded event loop with `selector.select()`  
✅ Handles multiple concurrent connections non-blocking  
✅ Logs written to file with rotation  
✅ Logs forwarded to Hub for Dashboard display  
✅ Service appears in Hub's Service Registry  
✅ Real-time logs visible in Dashboard Tab 4  

## Next Steps

1. Build the service: `.\build.ps1`
2. Start Hub Server (if not running)
3. Start NIO Log Service: `.\run.ps1`
4. Update other services to send logs to port 9091
5. Test with test client: `.\test-log-client.ps1`
6. Check Dashboard Tab 4 for real-time log display
