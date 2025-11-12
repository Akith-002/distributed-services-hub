# 🔗 Integration Guide - Connecting Other Services to Log Service

This guide shows how Members 1, 3, and 5 can send log messages to your Log Service.

## 📍 Log Service Details

- **Host**: `localhost`
- **Port**: `9091`
- **Protocol**: TCP Socket
- **Message Format**: Plain text, any format (recommend: `[ServiceName] message`)

## 📤 How to Send Log Messages

### Option 1: Simple Socket Connection (Recommended)

The easiest way - just open a socket, send a message, and close:

```java
import java.io.*;
import java.net.Socket;

public class LogServiceClient {
    private static final String LOG_HOST = "localhost";
    private static final int LOG_PORT = 9091;

    /**
     * Send a log message to the Log Service
     *
     * @param serviceName Name of your service (e.g., "HubServer")
     * @param message The log message
     */
    public static void sendLog(String serviceName, String message) {
        try (Socket socket = new Socket(LOG_HOST, LOG_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send the formatted message
            String logMessage = String.format("[%s] %s", serviceName, message);
            out.println(logMessage);

        } catch (IOException e) {
            // Fail silently - don't crash if Log Service is down
            System.err.println("Could not send log to Log Service: " + e.getMessage());
        }
    }
}
```

### Usage Examples

```java
// When your service starts
LogServiceClient.sendLog("HubServer", "Service started successfully");

// When a client connects
LogServiceClient.sendLog("HubServer", "New client connected from " + clientAddress);

// When processing requests
LogServiceClient.sendLog("FileService", "File uploaded: report.pdf (2.5 MB)");

// Errors
LogServiceClient.sendLog("APIGateway", "ERROR: Database connection timeout");

// Success messages
LogServiceClient.sendLog("TaskService", "SUCCESS: Task #1234 completed");
```

## 🎨 Message Types (Auto-Detected by Log Service)

Your Log Service automatically color-codes messages based on keywords:

| Keyword                 | Icon | Color   | Example                                |
| ----------------------- | ---- | ------- | -------------------------------------- |
| **ERROR**, **FAIL**     | ❌   | Red     | `[Service] ERROR: Connection failed`   |
| **WARN**                | ⚠️   | Yellow  | `[Service] WARN: High memory usage`    |
| **SUCCESS**, **ONLINE** | ✅   | Green   | `[Service] SUCCESS: Task completed`    |
| **INFO**, **REGISTER**  | ℹ️   | Blue    | `[Service] INFO: Configuration loaded` |
| Other                   | 📝   | Default | `[Service] Processing request`         |

## 📋 Integration Checklist for Each Service

### Member 1 - Hub Server

```java
// Add to HubServer.java
import com.example.hub.LogServiceClient; // After creating the utility class

// In main method after server starts:
LogServiceClient.sendLog("HubServer", "ONLINE: Hub Server started on port 8000");

// When a service registers:
LogServiceClient.sendLog("HubServer", "Service registered: " + serviceName);

// When heartbeat fails:
LogServiceClient.sendLog("HubServer", "WARN: Service " + serviceName + " heartbeat timeout");
```

### Member 3 - File Service

```java
// Add to SecureFileService.java
import com.example.fileservice.LogServiceClient;

// When service starts:
LogServiceClient.sendLog("FileService", "ONLINE: Secure File Service started");

// When file is uploaded:
LogServiceClient.sendLog("FileService", "File uploaded: " + filename + " (" + size + " bytes)");

// When file is downloaded:
LogServiceClient.sendLog("FileService", "File downloaded: " + filename + " by " + clientId);

// Errors:
LogServiceClient.sendLog("FileService", "ERROR: File not found: " + filename);
```

### Member 5 - API Gateway

```java
// Add to APIGatewayService.java
import com.example.apigateway.LogServiceClient;

// When service starts:
LogServiceClient.sendLog("APIGateway", "ONLINE: API Gateway started on port 8080");

// When request received:
LogServiceClient.sendLog("APIGateway", "Request received: " + method + " " + endpoint);

// When forwarding request:
LogServiceClient.sendLog("APIGateway", "Forwarding request to: " + targetService);

// Success:
LogServiceClient.sendLog("APIGateway", "SUCCESS: Request completed in " + duration + "ms");
```

## 🚀 Quick Setup for Other Team Members

### Step 1: Copy the Utility Class

Create this file in your service:

**File**: `src/main/java/com/example/[yourpackage]/LogServiceClient.java`

```java
package com.example.[yourpackage];  // Change to your package!

import java.io.*;
import java.net.Socket;

public class LogServiceClient {
    private static final String LOG_HOST = "localhost";
    private static final int LOG_PORT = 9091;
    private static final int TIMEOUT = 1000; // 1 second timeout

    public static void sendLog(String serviceName, String message) {
        try (Socket socket = new Socket(LOG_HOST, LOG_PORT)) {
            socket.setSoTimeout(TIMEOUT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String logMessage = String.format("[%s] %s", serviceName, message);
            out.println(logMessage);

        } catch (IOException e) {
            // Fail silently - logging should never crash your service!
            System.err.println("Log Service unavailable: " + e.getMessage());
        }
    }

    // Convenience methods for different log levels

    public static void info(String serviceName, String message) {
        sendLog(serviceName, "INFO: " + message);
    }

    public static void success(String serviceName, String message) {
        sendLog(serviceName, "SUCCESS: " + message);
    }

    public static void warn(String serviceName, String message) {
        sendLog(serviceName, "WARN: " + message);
    }

    public static void error(String serviceName, String message) {
        sendLog(serviceName, "ERROR: " + message);
    }
}
```

### Step 2: Use Throughout Your Code

```java
// Import the class
import com.example.yourpackage.LogServiceClient;

// Use it anywhere!
LogServiceClient.sendLog("MyService", "Something happened");
LogServiceClient.info("MyService", "Configuration loaded");
LogServiceClient.success("MyService", "Operation completed");
LogServiceClient.warn("MyService", "Low disk space");
LogServiceClient.error("MyService", "Connection failed");
```

## 🎬 Demo Sequence

For a smooth demo, start services in this order:

1. **Hub Server** (Member 1) → Port 8000
2. **Log Service** (You!) → Port 9091
3. **Other Services** (Members 3, 5) → Various ports

When each service starts, it should:

1. Connect to Hub Server (register)
2. Send a log to Log Service: `"[ServiceName] ONLINE"`

## 🧪 Testing Individual Integration

Each service can test their logging independently:

```java
public static void main(String[] args) {
    // Test log connection before starting main service
    try {
        LogServiceClient.sendLog("TestService", "Testing log connection...");
        System.out.println("✅ Log Service connection successful");
    } catch (Exception e) {
        System.out.println("⚠️  Log Service not available (non-critical)");
    }

    // Continue with main service startup...
}
```

## 📊 What You'll See

When everything is connected, your Log Service console will look like:

```
╔════════════════════════════════════════════════════════════════╗
║        HIGH-PERFORMANCE LOG SERVICE (Member 4)                 ║
╚════════════════════════════════════════════════════════════════╝

🖥️  SERVER  | Log Server started on port 9091
🖥️  SERVER  | Waiting for log messages from other services...

======================================================================

📝 LOG     | New connection from: /127.0.0.1:54321 (Total active: 1)
✅ SUCCESS | [HubServer] ONLINE: Hub Server started on port 8000
📝 LOG     | New connection from: /127.0.0.1:54322 (Total active: 2)
✅ SUCCESS | [FileService] ONLINE: Secure File Service started
📝 LOG     | New connection from: /127.0.0.1:54323 (Total active: 3)
✅ SUCCESS | [APIGateway] ONLINE: API Gateway started on port 8080
ℹ️  INFO    | [HubServer] Service registered: FileService
ℹ️  INFO    | [HubServer] Service registered: APIGateway
📝 LOG     | [FileService] Listening for file transfers on port 9443
📝 LOG     | [APIGateway] Ready to accept requests
```

## 🐛 Troubleshooting

### "Connection refused" when sending logs

**Cause**: Log Service is not running
**Solution**: Start Log Service first, or add try-catch to fail gracefully

```java
try {
    LogServiceClient.sendLog("MyService", "Message");
} catch (Exception e) {
    // Service continues even if logging fails
    System.err.println("Logging unavailable");
}
```

### Messages not appearing

1. Check Log Service is running: `Test-NetConnection localhost -Port 9091`
2. Verify message format is correct
3. Check `logs/system.log` file - messages are persisted even if console doesn't show

### Slow service startup

**Cause**: Socket timeout when Log Service is unavailable
**Solution**: Set a short timeout (see utility class above)

## 💡 Pro Tips

1. **Always log service startup**: `"ONLINE: ServiceName started"`
2. **Log important operations**: File transfers, database queries, API calls
3. **Use consistent format**: `[ServiceName] Action`
4. **Don't over-log**: Avoid logging every tiny operation
5. **Fail gracefully**: Never let logging crash your service

## 🎯 Minimum Integration

At minimum, each service should send just **2 log messages**:

```java
// 1. When service starts
LogServiceClient.sendLog("MyService", "ONLINE: Service started");

// 2. When service stops
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    LogServiceClient.sendLog("MyService", "Service shutting down");
}));
```

Even these two messages will demonstrate your Log Service is working!

## 📞 Questions?

If other team members have integration issues, they can:

1. Run the test client: `.\test-log-service.ps1`
2. Check if port 9091 is accessible
3. Verify the utility class is in their project
4. Test with a simple standalone Java program first

---

**Built for seamless integration | Member 4 Log Service** 📝
