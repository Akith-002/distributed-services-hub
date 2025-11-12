# Member 4 - NIO Log Service Startup & Demonstration Guide

**Your Component:** NIO Log Service (Non-blocking I/O with Selector)  
**Your Core Networking Concepts:** Java NIO, ServerSocketChannel, Selector, ByteBuffer  
**Port:** 9091 (Non-blocking TCP)

---

## 📚 Java Network Programming Concepts You're Demonstrating

### 1. **Java NIO (New I/O)** - Lesson 7
- **Non-blocking I/O** vs traditional blocking I/O
- Single-threaded event loop handles multiple connections
- Much more scalable than thread-per-client model

### 2. **ServerSocketChannel** (NIO version of ServerSocket)
- Channel-based I/O instead of stream-based
- Can be configured as non-blocking
- Registered with Selector for event notifications

### 3. **Selector** (Event Multiplexing)
- Single thread monitors multiple channels
- Uses `select()` to wait for events
- Event types: `OP_ACCEPT`, `OP_READ`, `OP_WRITE`, `OP_CONNECT`

### 4. **SelectionKey**
- Represents registration of channel with selector
- Contains attachment (custom data)
- Indicates which operations are ready

### 5. **ByteBuffer** (Efficient Data Transfer)
- Direct memory buffer for network I/O
- More efficient than byte arrays
- Flip, clear, compact operations

### 6. **Event-Driven Architecture**
```
Single Thread Event Loop:
while (running) {
    selector.select() → Block until events
    for each ready key:
        if (acceptable) → accept connection
        if (readable) → read data
        if (writable) → write data
}
```

---

## 🚀 How to Start Your Service

### Step 1: Navigate to Your Service Directory
```powershell
cd distributed-services-hub\nio-log-service
```

### Step 2: Build Your Service (First Time Only)
```powershell
.\build.ps1
```
Or manually:
```powershell
mvn clean package
```

**Expected Output:**
```
[INFO] Building jar: target\nio-log-service-1.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

### Step 3: Start the Hub Server First

**IMPORTANT:** Hub must be running before your service starts!
```powershell
cd ..\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

### Step 4: Run Your NIO Log Service

```powershell
cd ..\nio-log-service
java -jar target\nio-log-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
================================================================================
  NIO LOG SERVICE - MEMBER 4
================================================================================

[HubClient] Connecting to Hub at localhost:7070...
[HubClient] Connected to Hub successfully
[HubClient] Sent registration: REGISTER::NioLogService::localhost::9091

[LogServer] Creating ServerSocketChannel on port 9091...
[LogServer] ServerSocketChannel configured as NON-BLOCKING
[LogServer] Selector created
[LogServer] ServerSocketChannel registered with Selector (OP_ACCEPT)

✓ NIO LOG SERVICE STARTED SUCCESSFULLY

✓ Hub Registration: SUCCESS
✓ NIO Server: RUNNING on port 9091
✓ Selector: ACTIVE (event loop running)
✓ Blocking Mode: NON-BLOCKING
✓ Thread Model: SINGLE THREAD handles all connections
✓ Heartbeat: ACTIVE (every 10 seconds)

Selector event loop started...
Waiting for log connections...
```

---

## 🎯 How to Demonstrate Your Work

### Method 1: Test with Simple Client

#### Run the Test Client Script
```powershell
.\test-log-client.ps1
```

**What this does:**
- Connects to your NIO server on port 9091
- Sends multiple log messages
- Shows non-blocking behavior

**Expected Output:**
```
================================================================================
  NIO LOG CLIENT - TEST
================================================================================

Connecting to NIO Log Service at localhost:9091...
✓ Connected

Sending log messages...
→ Sent: TEST_CLIENT: Log message 1
→ Sent: TEST_CLIENT: Log message 2
→ Sent: TEST_CLIENT: Log message 3
→ Sent: TEST_CLIENT: Log message 4
→ Sent: TEST_CLIENT: Log message 5

✓ All messages sent successfully
Connection closed.
```

**In your service logs, you'll see:**
```
[LogServer] Selector: OP_ACCEPT event (new connection)
[LogServer] Accepted connection: /127.0.0.1:xxxxx
[LogServer] New client registered with Selector (OP_READ)
[LogServer] Selector: OP_READ event (data ready)
[LogServer] Read 30 bytes: "TEST_CLIENT: Log message 1"
[LogWriter] Writing to file: logs/service-2025-11-12.log
[HubForwarder] Forwarding to Dashboard: TEST_CLIENT: Log message 1
```

### Method 2: Multiple Concurrent Connections (Shows NIO Power)

This demonstrates the **key advantage** of NIO - handling multiple connections with ONE thread!

#### Open 3 terminals and run clients simultaneously:

**Terminal 1:**
```powershell
cd distributed-services-hub\nio-log-service
.\test-log-client.ps1
```

**Terminal 2:**
```powershell
cd distributed-services-hub\nio-log-service
.\test-log-client.ps1
```

**Terminal 3:**
```powershell
cd distributed-services-hub\nio-log-service
.\test-log-client.ps1
```

**Watch your NIO service handle all 3 with ONE thread:**
```
[LogServer] ✓ SINGLE THREAD handling 3 concurrent connections
[LogServer] Connection 1: Reading data...
[LogServer] Connection 2: Reading data...
[LogServer] Connection 3: Reading data...
[LogServer] All handled by same thread (non-blocking I/O)
```

### Method 3: Check Log Files

#### View the log file created:
```powershell
cat logs\service-2025-11-12.log
```

**Expected Content:**
```
[2025-11-12 14:30:45] TEST_CLIENT: Log message 1
[2025-11-12 14:30:45] TEST_CLIENT: Log message 2
[2025-11-12 14:30:45] TEST_CLIENT: Log message 3
[2025-11-12 14:30:46] API_GATEWAY: Weather fetched for Colombo
[2025-11-12 14:30:47] SECURE_FILE_SERVICE: File stored: test.txt
```

### Method 4: UI Demonstration (Tab 4: NIO Log Stream)

This is the **VISUAL** way to show your work!

#### Start the Dashboard
```powershell
cd ..\..\multi-client-chat-frontend
npm run dev
```

Open browser: **http://localhost:5173**

#### Navigate to Tab 4: NIO Log Stream

1. Click on **"NIO Log Stream"** tab (Tab 4)
2. You'll see a real-time log viewer
3. Start sending logs from test clients
4. **Watch logs appear in real-time!**

**What the UI Shows:**
```
Real-Time Log Stream
--------------------
[14:30:45] TEST_CLIENT: Log message 1
[14:30:45] TEST_CLIENT: Log message 2
[14:30:46] API_GATEWAY: Weather fetched
[14:30:47] SECURE_FILE_SERVICE: File stored
[14:30:48] RMI_TASK_SERVICE: Task executed
```

**UI Features:**
- **Real-time updates** - Logs appear as they arrive
- **Auto-scroll** - Automatically scrolls to latest log
- **Download logs** - Export log file
- **Clear logs** - Clear the display

#### Demonstrate Real-Time Streaming

1. Open Tab 4 in Dashboard
2. Run test client: `.\test-log-client.ps1`
3. **Watch logs appear INSTANTLY in UI**
4. Run multiple clients simultaneously
5. Show all logs streaming in real-time

---

## 🎓 Explaining Your Networking Concepts

### For Your Presentation/Demo, Explain:

#### 1. ServerSocketChannel vs ServerSocket

**Traditional Blocking I/O (What we DON'T use):**
```java
ServerSocket server = new ServerSocket(9091);
while (true) {
    Socket client = server.accept(); // BLOCKS until connection
    new Thread(() -> handleClient(client)).start(); // New thread per client
}
// Problem: 1000 clients = 1000 threads!
```

**NIO Non-Blocking I/O (What we DO use):**
```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false); // NON-BLOCKING!
serverChannel.bind(new InetSocketAddress(9091));

Selector selector = Selector.open();
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    selector.select(); // Wait for events
    // Handle all ready operations
}
// Advantage: 1000 clients = 1 thread!
```

**Explain:**
- "Traditional I/O blocks - one thread per client"
- "NIO is non-blocking - one thread handles many clients"
- "Selector monitors multiple channels, notifies when events occur"
- "Much more scalable for high-concurrency scenarios"

#### 2. Selector and Event Loop

**Code Reference in `LogServer.java`:**
```java
Selector selector = Selector.open();

// Register server channel for ACCEPT events
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

// Event loop
while (running) {
    selector.select(); // Block until events ready
    
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    for (SelectionKey key : selectedKeys) {
        if (key.isAcceptable()) {
            // New connection
            acceptConnection(key);
        }
        if (key.isReadable()) {
            // Data ready to read
            readData(key);
        }
    }
    selectedKeys.clear();
}
```

**Explain:**
- "Selector is like a traffic controller"
- "It monitors multiple channels and tells us when events occur"
- "`select()` blocks until at least one event is ready"
- "We then process all ready events in a loop"
- "One thread efficiently handles everything"

#### 3. Handling New Connections (OP_ACCEPT)

**Code Reference:**
```java
private void acceptConnection(SelectionKey key) throws IOException {
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    
    SocketChannel clientChannel = serverChannel.accept(); // Non-blocking
    if (clientChannel != null) {
        clientChannel.configureBlocking(false); // Make client non-blocking
        clientChannel.register(selector, SelectionKey.OP_READ); // Monitor for reads
        
        System.out.println("New connection: " + clientChannel.getRemoteAddress());
    }
}
```

**Explain:**
- "When `isAcceptable()` is true, a client wants to connect"
- "We accept the connection - returns `SocketChannel`"
- "Configure client channel as non-blocking"
- "Register it with selector to monitor for incoming data (OP_READ)"

#### 4. Reading Data (OP_READ)

**Code Reference:**
```java
private void readData(SelectionKey key) throws IOException {
    SocketChannel clientChannel = (SocketChannel) key.channel();
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    
    int bytesRead = clientChannel.read(buffer); // Non-blocking read
    
    if (bytesRead == -1) {
        // Client disconnected
        clientChannel.close();
        key.cancel();
        return;
    }
    
    if (bytesRead > 0) {
        buffer.flip(); // Switch from write mode to read mode
        
        String logMessage = new String(buffer.array(), 0, buffer.limit());
        processLog(logMessage);
        
        buffer.clear(); // Prepare for next read
    }
}
```

**Explain:**
- "When `isReadable()` is true, data is available"
- "We read into a ByteBuffer - more efficient than byte array"
- "`flip()` switches buffer from write mode to read mode"
- "We extract the data, process it, then `clear()` for next read"
- "If read returns -1, client disconnected"

#### 5. ByteBuffer Operations

**Explain:**
```java
ByteBuffer buffer = ByteBuffer.allocate(1024); // Create buffer

// Writing phase
channel.read(buffer); // Writes data into buffer
// position advances, limit stays at capacity

buffer.flip(); // Switch to reading phase
// limit = current position, position = 0

// Reading phase
byte[] data = new byte[buffer.remaining()];
buffer.get(data); // Read data from buffer

buffer.clear(); // Prepare for next write
// position = 0, limit = capacity
```

**Key Methods:**
- `allocate(size)` - Create buffer
- `flip()` - Switch write→read mode
- `clear()` - Reset for new data
- `remaining()` - How much data left

#### 6. Why NIO is Better for This Use Case

**Comparison:**

| Aspect | Traditional I/O | NIO (Our Implementation) |
|--------|----------------|-------------------------|
| **Threads** | 1000 clients = 1000 threads | 1000 clients = 1 thread |
| **Blocking** | `accept()` and `read()` block | Non-blocking operations |
| **Scalability** | Limited by thread count | Can handle 10,000+ connections |
| **Memory** | High (thread stack × count) | Low (single thread) |
| **CPU** | High (context switching) | Low (event-driven) |

**Explain:**
- "For a logging service, we expect many connections sending small amounts of data"
- "Thread-per-client would waste resources"
- "NIO lets one thread efficiently handle all connections"
- "Perfect for high-concurrency, I/O-bound applications"

---

## 📊 Demonstration Checklist

Use this checklist during your demonstration:

### Command Line Demo
- [ ] Start Hub Server
- [ ] Start NIO Log Service
- [ ] Show non-blocking configuration in logs
- [ ] Show Selector event loop started
- [ ] Run single test client: `.\test-log-client.ps1`
- [ ] Show OP_ACCEPT event in logs
- [ ] Show OP_READ events in logs
- [ ] Show log written to file
- [ ] Run 3 concurrent clients
- [ ] Show single thread handling all 3
- [ ] Check log file: `cat logs\service-2025-11-12.log`

### UI Demo (Tab 4: NIO Log Stream)
- [ ] Open Dashboard at http://localhost:5173
- [ ] Navigate to Tab 4: "NIO Log Stream"
- [ ] Show empty log viewer initially
- [ ] Run test client
- [ ] Show logs appear in real-time
- [ ] Run multiple clients simultaneously
- [ ] Show all logs streaming
- [ ] Use "Download Logs" button
- [ ] Use "Clear Logs" button

### Technical Explanation Points
- [ ] Explain ServerSocketChannel vs ServerSocket
- [ ] Explain blocking vs non-blocking I/O
- [ ] Explain Selector purpose
- [ ] Explain event loop concept
- [ ] Explain OP_ACCEPT event
- [ ] Explain OP_READ event
- [ ] Explain SelectionKey
- [ ] Explain ByteBuffer flip/clear operations
- [ ] Show why NIO is better for this use case
- [ ] Show single thread handling multiple connections

---

## 🔧 Troubleshooting

### Issue: Port 9091 already in use
```powershell
# Find what's using the port
netstat -ano | findstr :9091

# Kill the process
taskkill /PID <PID> /F
```

### Issue: Selector not detecting events
- Check channels are configured as non-blocking
- Check correct operations registered (OP_ACCEPT, OP_READ)
- Check `selectedKeys.clear()` is called after processing

### Issue: ByteBuffer errors
```
BufferUnderflowException or BufferOverflowException
```
**Solution:**
- Always call `flip()` before reading
- Always call `clear()` or `compact()` before writing
- Check `remaining()` before `get()`

### Issue: Logs not appearing in UI
- Check NIO service is running
- Check Hub Server is running
- Check HubForwarder is sending logs
- Check WebSocket connection in browser console

---

## 📝 Key Points for Your Report

Include these in your written documentation:

### Architecture
- NIO Log Service uses ServerSocketChannel for non-blocking I/O
- Single-threaded event loop with Selector
- Handles multiple concurrent connections efficiently
- Logs written to file and forwarded to Dashboard

### Networking Concepts

1. **Java NIO** (Lesson 7)
   - Non-blocking I/O model
   - Channel-based instead of stream-based
   - Event-driven architecture

2. **ServerSocketChannel**
   - NIO version of ServerSocket
   - Configured as non-blocking
   - Registered with Selector

3. **Selector**
   - Multiplexes I/O events
   - Single thread monitors multiple channels
   - `select()` blocks until events ready

4. **SelectionKey**
   - Represents channel registration
   - Indicates ready operations
   - Contains attachments for state

5. **ByteBuffer**
   - Efficient data transfer
   - Direct memory allocation
   - Flip/clear operations

### Why These Concepts Matter
- **Scalability:** Handle thousands of connections with one thread
- **Efficiency:** Low memory and CPU usage
- **Performance:** No blocking, no context switching
- **Real-world:** Used in high-performance servers (Netty, Jetty)

---

## 🎬 Presentation Script Example

**1. Introduction (30 seconds)**
"I implemented the NIO Log Service using Java NIO - Non-blocking I/O. It demonstrates ServerSocketChannel, Selector, and event-driven architecture. One thread handles multiple concurrent log streams."

**2. Show Architecture (30 seconds)**
"Unlike traditional I/O that uses one thread per client, NIO uses a Selector to monitor multiple channels. When data is ready, the Selector notifies us, and we process it - all in one thread."

**3. Code Walkthrough (1 minute)**
"Here's the ServerSocketChannel configured as non-blocking. I register it with a Selector for ACCEPT events. In the event loop, `select()` waits for events. When a client connects, I accept and register for READ events. When data arrives, I read it into a ByteBuffer, process the log, and write to file."

**4. Live Demo - Single Client (1 minute)**
"Let me start the service... See it's non-blocking and using a Selector. Now I'll send logs from a test client... Watch the Selector detect ACCEPT event, then READ events. Logs are written to file and forwarded to Dashboard."

**5. Live Demo - Multiple Clients (1 minute)**
"Now the cool part - I'll start 3 clients simultaneously... Notice the service says 'SINGLE THREAD handling 3 concurrent connections'. No blocking, no thread-per-client. This is the power of NIO!"

**6. UI Demo (1 minute)**
"In the Dashboard Tab 4, you see real-time log streaming. As I send logs from clients, they appear instantly in the UI. The service efficiently handles all connections with one thread and forwards logs via WebSocket."

---

## 📚 Study References

Review these lessons before your demo:

- **Lesson 7:** Java NIO, ServerSocketChannel, Selector, ByteBuffer
- **Lesson 3:** ServerSocket (for comparison)

### Key NIO Classes to Know:
- `ServerSocketChannel` - Non-blocking server
- `SocketChannel` - Non-blocking client connection
- `Selector` - I/O event multiplexer
- `SelectionKey` - Channel registration
- `ByteBuffer` - Efficient data buffer

### Key Operations:
- `OP_ACCEPT` - Accept new connection
- `OP_READ` - Read data
- `OP_WRITE` - Write data
- `OP_CONNECT` - Connect (client-side)

### Key Concepts:
- **Non-blocking:** Operations return immediately
- **Event-driven:** React to events as they occur
- **Multiplexing:** One thread, many channels
- **Scalability:** Handle high concurrency

---

## ✅ Pre-Demo Checklist

Before your demonstration:

- [ ] Service builds successfully (`.\build.ps1`)
- [ ] Hub Server is running
- [ ] Can start NIO Log Service without errors
- [ ] Service registers with Hub successfully
- [ ] Port 9091 is available
- [ ] Test client script works: `.\test-log-client.ps1`
- [ ] Logs written to `logs/` directory
- [ ] Can run 3 concurrent clients
- [ ] Single thread handles all connections
- [ ] Dashboard Tab 4 loads and works
- [ ] Logs appear in real-time in UI
- [ ] You understand Selector concept
- [ ] You can explain non-blocking I/O
- [ ] You can explain ByteBuffer operations
- [ ] You can explain why NIO is better than traditional I/O
- [ ] You practiced the demo at least once

---

## 🌟 Extra Credit Demonstrations

### Show Thread Count

```powershell
# In another terminal while service is running
# Show only ONE thread handling all connections

# Windows: Task Manager → Details → Find java.exe → Thread count
```

### Compare with Traditional I/O

Create a simple blocking server and show:
- Blocking server: 100 clients = 100+ threads
- NIO server: 100 clients = 1 thread

### Explain Real-World Usage

"Netflix uses Netty (NIO-based) to handle millions of concurrent connections with minimal resources."

---

## 📊 What Makes Your Demo Stand Out

1. **Visual Proof:** UI shows real-time log streaming
2. **Concurrency:** Multiple clients with one thread
3. **Efficiency:** Low resource usage, high throughput
4. **Event-Driven:** Modern architecture pattern
5. **Real-Time:** Logs appear instantly in Dashboard

---

**Good luck with your demonstration! Show them the power of non-blocking I/O and event-driven architecture!**
