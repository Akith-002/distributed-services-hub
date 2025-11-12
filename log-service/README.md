# 🚀 High-Performance Log Service (Member 4)

## Overview

This is a **centralized logging server** built using **Java NIO (Non-Blocking I/O)** with Selectors. It can handle thousands of concurrent connections efficiently using a single thread, demonstrating one of the most powerful concepts in Java networking.

### Key Features

- ✅ **Non-Blocking I/O**: Uses Java NIO Selectors for high performance
- ✅ **Single-Threaded**: Handles multiple connections without creating threads
- ✅ **Persistent Logging**: Saves all messages to `logs/system.log`
- ✅ **Visual Console**: Color-coded output with emojis for easy monitoring
- ✅ **Hub Integration**: Automatically registers with Hub Server
- ✅ **Real-time Statistics**: Shows message count and active connections

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Log Service (Port 9091)                  │
│                                                             │
│  ┌────────────┐    ┌──────────────────────────────────┐   │
│  │  Selector  │◄───│  ServerSocketChannel (Listener)  │   │
│  └─────┬──────┘    └──────────────────────────────────┘   │
│        │                                                    │
│        ├──► SocketChannel (Service 1)                      │
│        ├──► SocketChannel (Service 2)                      │
│        ├──► SocketChannel (Service 3)                      │
│        └──► SocketChannel (Service N)                      │
│                                                             │
│  All monitored by ONE thread using ONE selector!           │
└─────────────────────────────────────────────────────────────┘
```

## How Java NIO Works

### Traditional Blocking I/O vs NIO

**Traditional (One thread per connection):**

```
Client 1 ──► Thread 1
Client 2 ──► Thread 2
Client 3 ──► Thread 3
...
Client 1000 ──► Thread 1000 ❌ Resource intensive!
```

**Java NIO (One thread for all connections):**

```
Client 1 ──┐
Client 2 ──┤
Client 3 ──┼──► Selector ──► Single Thread ✅
...        │
Client 1000─┘
```

### Key Components

1. **ServerSocketChannel**: Non-blocking server socket
2. **Selector**: Monitors multiple channels for I/O events
3. **SelectionKey**: Represents a channel's registration with a selector
4. **ByteBuffer**: Efficient buffer for reading/writing data

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Hub Server running (for registration)

### Build and Run

```powershell
# 1. Build the service
.\build.ps1

# 2. Start the Log Service
.\run-log-service.ps1
```

Or use batch files on Windows:

```cmd
build.bat
run-log-service.bat
```

### Testing

Test the Log Service independently:

```powershell
.\test-log-service.ps1
```

This will send 20 sample log messages to verify everything works.

## Port Configuration

| Service     | Port     | Purpose               |
| ----------- | -------- | --------------------- |
| Log Service | **9091** | Receives log messages |
| Hub Server  | 8000     | Service registration  |

## How Other Services Connect

Other services (Members 1, 2, 3, 5) send log messages like this:

```java
// Simple example of how to send a log message
try (Socket socket = new Socket("localhost", 9091);
     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

    out.println("[MyService] This is a log message");
}
```

## Message Format

Log messages should follow this format:

```
[ServiceName] Message content
```

Examples:

- `[HubServer] Service started successfully`
- `[FileService] File transfer completed`
- `[APIGateway] ERROR: Connection timeout`

## Console Output

The Log Service displays messages with visual indicators:

- ✅ **SUCCESS**: Online, success messages
- ❌ **ERROR**: Error messages
- ⚠️ **WARNING**: Warning messages
- ℹ️ **INFO**: Informational messages
- 📝 **LOG**: General log messages
- 🖥️ **SERVER**: Internal server messages

## File Logging

All messages are saved to:

```
logs/system.log
```

Format:

```
[2025-11-11 14:30:45.123] /127.0.0.1:52341 | [FileService] File uploaded
[2025-11-11 14:30:46.456] [SERVER] Connection closed by: /127.0.0.1:52341
```

## NIO Implementation Details

### The Main Event Loop

```java
while (running) {
    // 1. Wait for events (blocks until something is ready)
    selector.select();

    // 2. Get ready channels
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

    // 3. Process each ready channel
    while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        keyIterator.remove(); // CRITICAL!

        if (key.isAcceptable()) {
            // New connection
            handleAccept(key);
        } else if (key.isReadable()) {
            // Data available to read
            handleRead(key);
        }
    }
}
```

### Accepting New Connections

```java
ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
SocketChannel clientChannel = serverChannel.accept();

// Make non-blocking
clientChannel.configureBlocking(false);

// Register for read operations
clientChannel.register(selector, SelectionKey.OP_READ);
```

### Reading Data with ByteBuffer

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
int bytesRead = clientChannel.read(buffer);

// Flip: prepare for reading
buffer.flip();

// Convert to string
byte[] bytes = new byte[buffer.remaining()];
buffer.get(bytes);
String message = new String(bytes, StandardCharsets.UTF_8);
```

## Demo Checklist

When presenting your work:

1. ✅ Start Hub Server (Member 1)
2. ✅ Start React Dashboard (Member 2) - shows "LogService" as Online
3. ✅ Start your Log Service - console shows "Waiting for messages..."
4. ✅ Other services start and send log messages
5. ✅ Your console fills with color-coded messages in real-time
6. ✅ Show `logs/system.log` file with persistent logs

## Performance Benefits

### Why NIO is Better for This Task

**Traditional Approach (1 thread per connection):**

- 100 services = 100 threads
- Each thread uses ~1MB memory
- Context switching overhead
- Scalability issues

**NIO Approach (1 thread for all):**

- 100 services = 1 thread
- Minimal memory footprint
- No context switching
- Scales to thousands of connections

## Troubleshooting

### Port Already in Use

```
Error: Address already in use: bind
```

**Solution**: Another service is using port 9091. Stop it or change the port in `LogServer.java`.

### Cannot Connect to Hub

```
Warning: Could not register with Hub Server
```

**Solution**: Make sure Hub Server is running first. The Log Service will still work for direct connections.

### No Messages Appearing

1. Check if Log Service is running: `Test-NetConnection localhost -Port 9091`
2. Verify other services are sending to port 9091
3. Check firewall settings

## Code Structure

```
log-service/
├── src/main/java/com/example/logservice/
│   ├── LogServer.java       # Main NIO server (THE CORE!)
│   ├── HubClient.java        # Registers with Hub
│   └── TestLogClient.java    # Test client
├── logs/
│   └── system.log            # Persistent log file
├── build.ps1                 # Build script
├── build.bat                 # Build script (batch)
├── run-log-service.ps1       # Run script
├── run-log-service.bat       # Run script (batch)
├── test-log-service.ps1      # Test script
├── pom.xml                   # Maven configuration
└── README.md                 # This file
```

## Learning Resources

### Java NIO Concepts (Lesson 7)

1. **Channels**: Bi-directional data streams
2. **Buffers**: Container for data
3. **Selectors**: Multiplexing I/O events
4. **Non-blocking mode**: Don't wait for I/O

### Key Methods

- `ServerSocketChannel.open()` - Create server channel
- `configureBlocking(false)` - Enable non-blocking mode
- `Selector.open()` - Create selector
- `channel.register(selector, ops)` - Register channel
- `selector.select()` - Wait for events
- `key.isAcceptable()` - Check for new connections
- `key.isReadable()` - Check for readable data

## Advanced Features (Optional)

### Future Enhancements

1. **Log Levels**: Filter by ERROR, WARN, INFO, DEBUG
2. **Log Rotation**: Rotate files daily or by size
3. **Remote Monitoring**: Web interface to view logs
4. **Search**: Full-text search through logs
5. **Alerts**: Send notifications on errors

## Integration with Other Services

### For Members 1, 3, 5 (Java Services)

Add this utility class to send logs:

```java
public class LogServiceClient {
    private static final String LOG_HOST = "localhost";
    private static final int LOG_PORT = 9091;

    public static void sendLog(String serviceName, String message) {
        try (Socket socket = new Socket(LOG_HOST, LOG_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("[" + serviceName + "] " + message);
        } catch (IOException e) {
            // Log locally if Log Service is unavailable
            System.err.println("Could not send to Log Service: " + e.getMessage());
        }
    }
}
```

Usage:

```java
LogServiceClient.sendLog("HubServer", "Service started successfully");
LogServiceClient.sendLog("FileService", "File uploaded: report.pdf");
```

### For Member 2 (React Dashboard)

Use WebSocket or HTTP to send logs:

```javascript
// Simple HTTP POST example
async function sendLog(serviceName, message) {
  try {
    const response = await fetch('http://localhost:9091/log', {
      method: 'POST',
      body: `[${serviceName}] ${message}`,
    });
  } catch (error) {
    console.error('Could not send log:', error);
  }
}
```

## Conclusion

This Log Service demonstrates:

- ✅ **Java NIO mastery**: Selectors, channels, buffers
- ✅ **High performance**: Single-threaded, thousands of connections
- ✅ **Production-ready**: Error handling, logging, monitoring
- ✅ **Clean architecture**: Modular, maintainable code

You've built the **high-performance engine** of the distributed system! 🚀

## Questions?

Common questions during demo:

**Q: How does one thread handle multiple connections?**
A: The Selector monitors all channels. When data is ready, `select()` returns and we process only the ready channels.

**Q: What if a service sends a lot of data?**
A: ByteBuffer reads in chunks. For very large messages, you'd implement a buffering strategy.

**Q: Is this production-ready?**
A: This is a great foundation! Production systems add authentication, encryption (SSL/TLS), and distributed logging.

---

**Built by Member 4 | Powered by Java NIO** ⚡
