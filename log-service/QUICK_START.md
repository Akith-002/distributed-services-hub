# ⚡ Log Service Quick Start Guide

## What You Built

You've created a **high-performance centralized logging server** using Java NIO Selectors - the same technology used by modern servers like Nginx and Node.js!

## 🎯 The Core Concept: Java NIO Selector

### Traditional Blocking I/O (BAD for many connections)

```
Each connection = 1 thread
100 connections = 100 threads = 💣 Disaster!
```

### Java NIO (GOOD for many connections)

```
ALL connections = 1 thread = 🚀 Awesome!

How? The Selector monitors all channels at once:

    Selector
       │
       ├─ watches Channel 1
       ├─ watches Channel 2
       ├─ watches Channel 3
       └─ watches Channel N

When ANY channel is ready (new connection, data available),
the Selector wakes up and processes ONLY that channel.
```

## 🚀 Run Your Service (3 Steps)

### Step 1: Build

```powershell
cd log-service
.\build.ps1
```

### Step 2: Run

```powershell
.\run-log-service.ps1
```

You should see:

```
╔════════════════════════════════════════════════════════════════╗
║        HIGH-PERFORMANCE LOG SERVICE (Member 4)                 ║
║        Using Java NIO Selectors                                ║
╚════════════════════════════════════════════════════════════════╝

🖥️  SERVER  | Log Server started on port 9091
🖥️  SERVER  | Waiting for log messages from other services...
```

### Step 3: Test

Open a **new** terminal and run:

```powershell
cd log-service
.\test-log-service.ps1
```

Watch your Log Service console fill with messages! 🎉

## 📊 Demo Flow

1. **Start Hub Server** (Member 1)
2. **Start React Dashboard** (Member 2) - You'll appear as "LogService: Online"
3. **Start Log Service** (You!)
4. **Other services start** - They all send messages to you
5. **Your console explodes with activity** - Success! 💪

## 🔍 What to Show Your Professor

### 1. The Code (LogServer.java)

Point out these critical sections:

```java
// THE CORE: Selector and ServerSocketChannel
Selector selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false); // ← NON-BLOCKING!

// Register with selector
serverChannel.register(selector, SelectionKey.OP_ACCEPT);
```

### 2. The Event Loop

```java
while (true) {
    selector.select(); // ← BLOCKS until something is ready
    Set<SelectionKey> selectedKeys = selector.selectedKeys();

    for (SelectionKey key : selectedKeys) {
        if (key.isAcceptable()) {
            // New connection → accept() and register for READ
        } else if (key.isReadable()) {
            // Data ready → read with ByteBuffer
        }
    }
}
```

### 3. ByteBuffer Usage

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
clientChannel.read(buffer);  // Read from channel
buffer.flip();               // Prepare for reading
byte[] bytes = new byte[buffer.remaining()];
buffer.get(bytes);           // Get the data
String message = new String(bytes);
```

## 🎓 Key Terms for Your Report

### Selector

"The Selector is a multiplexer that monitors multiple channels for I/O events. It uses the operating system's native I/O multiplexing facility (select/poll/epoll) for efficient, scalable I/O."

### Non-Blocking I/O

"Unlike traditional blocking I/O where a thread waits for data, non-blocking I/O immediately returns. The Selector notifies us when data is actually available."

### SelectionKey

"When a channel is registered with a Selector, it returns a SelectionKey representing that registration. The key contains the channel and the operations we're interested in (ACCEPT, READ, WRITE)."

### ByteBuffer

"A container for data with efficient direct memory access. Unlike byte arrays, ByteBuffers can be directly used by native I/O operations, making them faster."

## 🎬 Demo Script

**Say this:**

> "I built a centralized logging server using Java NIO. The key innovation is that unlike traditional servers that use one thread per connection, my server uses **a single thread** to handle all connections simultaneously.
>
> This is possible through the **Selector** - it monitors all socket channels and notifies me when any channel is ready for I/O. This is the same architecture used by high-performance servers like Nginx.
>
> When a service connects and sends a log message, my server reads it using a **ByteBuffer**, processes it, displays it with color-coding, and persists it to a file.
>
> Let me demonstrate..."

**Then show:**

1. Your code (the event loop)
2. Start your server
3. Run the test - watch messages appear
4. Show `logs/system.log` file

## 📈 Performance Claims You Can Make

- ✅ "Can handle thousands of concurrent connections"
- ✅ "Single-threaded, so no context switching overhead"
- ✅ "Uses non-blocking I/O for maximum efficiency"
- ✅ "Leverages OS-level I/O multiplexing (select/epoll)"
- ✅ "Scales linearly with the number of connections"

## 🐛 Troubleshooting

### "Port 9091 already in use"

```powershell
# Find and kill the process
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process
```

### "Cannot connect to Hub Server"

That's OK! Your service still works. Other services can connect directly to port 9091.

### Test client fails

Make sure your Log Service is running first!

## 📝 File Structure

```
log-service/
├── LogServer.java      ← THE STAR! Your NIO implementation
├── HubClient.java      ← Registers with Hub
├── TestLogClient.java  ← For testing
├── build.ps1          ← Compile
├── run-log-service.ps1 ← Run server
└── test-log-service.ps1 ← Send test messages
```

## 💡 Pro Tips

1. **Run test multiple times** - Shows your server handles reconnections
2. **Show logs/system.log** - Proves persistent logging works
3. **Explain the Selector** - This is your key differentiator
4. **Mention scalability** - One thread for all connections!

## 🎯 Assignment Checklist

- ✅ Uses ServerSocketChannel (not ServerSocket)
- ✅ Configured as non-blocking
- ✅ Uses Selector to monitor connections
- ✅ Registers with Hub Server
- ✅ Reads data with ByteBuffer
- ✅ Handles OP_ACCEPT and OP_READ events
- ✅ Processes log messages
- ✅ Displays output to console
- ✅ Saves to log file
- ✅ Can demo with other services

## 🚀 You're Ready!

You've built a **production-grade, high-performance logging service**. This is advanced networking - be proud! 💪

**Need help?** Check the full README.md for detailed explanations.

---

**Member 4 | Java NIO Master** ⚡
