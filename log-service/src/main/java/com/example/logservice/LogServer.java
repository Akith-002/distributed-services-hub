package com.example.logservice;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Member 4 - High-Performance Log Service
 * 
 * This service uses Java NIO (Non-Blocking I/O) with Selectors to handle
 * thousands of concurrent log message connections efficiently using a single
 * thread.
 * 
 * Key Concepts:
 * - ServerSocketChannel: Non-blocking server socket
 * - Selector: Monitors multiple channels for events (accept, read, write)
 * - SelectionKey: Represents the registration of a channel with a selector
 * - ByteBuffer: Efficient buffer for reading/writing data
 */
public class LogServer {

    private static final int LOG_PORT = 9091;
    private static final int BUFFER_SIZE = 1024;
    private static final String LOG_FILE = "logs/system.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean running = true;
    private PrintWriter fileWriter;
    private int messagesReceived = 0;
    private int activeConnections = 0;

    public LogServer() throws IOException {
        // Create logs directory if it doesn't exist
        new File("logs").mkdirs();

        // Initialize file writer for persistent logging
        fileWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);

        // Log server startup
        logMessage("=== Log Service Starting ===", true);
    }

    /**
     * Initialize the NIO server with Selector and ServerSocketChannel
     */
    public void start() throws IOException {
        // Step 1: Open the Selector - the heart of NIO
        selector = Selector.open();

        // Step 2: Create a ServerSocketChannel (not ServerSocket!)
        serverChannel = ServerSocketChannel.open();

        // Step 3: CRITICAL - Make it non-blocking!
        serverChannel.configureBlocking(false);

        // Step 4: Bind to the log service port
        serverChannel.socket().bind(new InetSocketAddress(LOG_PORT));

        // Step 5: Register the server channel with the selector
        // We're interested in ACCEPT operations (new connections)
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        logMessage("Log Server started on port " + LOG_PORT, true);
        logMessage("Waiting for log messages from other services...", true);
        logMessage("Press Ctrl+C to stop the server", true);
        System.out.println("\n" + "=".repeat(70) + "\n");

        // Register with the Hub Server
        registerWithHub();

        // Start the main event loop
        runEventLoop();
    }

    /**
     * Register this service with the Hub Server
     */
    private void registerWithHub() {
        try {
            HubClient hubClient = new HubClient();
            hubClient.register("LogService", LOG_PORT);
            logMessage("Successfully registered with Hub Server", true);
        } catch (Exception e) {
            logMessage("Warning: Could not register with Hub Server: " + e.getMessage(), true);
            logMessage("Continuing anyway - services can still connect directly", true);
        }
    }

    /**
     * The Main NIO Event Loop
     * 
     * This is where the "magic" happens. A single thread handles all connections!
     */
    private void runEventLoop() throws IOException {
        while (running) {
            // Block until at least one channel is ready for an operation
            // This is efficient - the thread sleeps here until something happens
            int readyChannels = selector.select();

            if (readyChannels == 0) {
                continue; // Nothing ready, loop again
            }

            // Get the set of keys for channels that are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            // Process each ready key
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                // ALWAYS remove the key from the selected set
                // If you forget this, you'll process the same event multiple times!
                keyIterator.remove();

                // Check if the key is still valid
                if (!key.isValid()) {
                    continue;
                }

                try {
                    // Handle different types of events
                    if (key.isAcceptable()) {
                        // A new client wants to connect
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        // A connected client has sent data
                        handleRead(key);
                    }
                } catch (IOException e) {
                    // If there's an error, close the connection gracefully
                    handleError(key, e);
                }
            }
        }

        cleanup();
    }

    /**
     * Handle a new incoming connection (OP_ACCEPT)
     */
    private void handleAccept(SelectionKey key) throws IOException {
        // Get the server channel from the key
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

        // Accept the new connection - this gives us a SocketChannel for the client
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            // Make the client channel non-blocking too!
            clientChannel.configureBlocking(false);

            // Register this new client channel with the selector
            // We're interested in READ operations (when the client sends data)
            clientChannel.register(selector, SelectionKey.OP_READ);

            activeConnections++;

            String clientAddress = clientChannel.getRemoteAddress().toString();
            logMessage("New connection from: " + clientAddress +
                    " (Total active: " + activeConnections + ")", false);
        }
    }

    /**
     * Handle data from a connected client (OP_READ)
     * 
     * This is where we receive and process log messages!
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // Create a ByteBuffer to hold incoming data
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        // Read data from the channel into the buffer
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            // Client closed the connection
            String clientAddress = clientChannel.getRemoteAddress().toString();
            logMessage("Connection closed by: " + clientAddress, false);
            activeConnections--;
            clientChannel.close();
            key.cancel();
            return;
        }

        if (bytesRead > 0) {
            // Flip the buffer: set position to 0, limit to current position
            // Now we can read the data that was just written
            buffer.flip();

            // Convert the ByteBuffer to a String
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String message = new String(bytes, StandardCharsets.UTF_8).trim();

            // Process the log message
            if (!message.isEmpty()) {
                processLogMessage(message, clientChannel);
                messagesReceived++;
            }

            // Clear the buffer for reuse
            buffer.clear();
        }
    }

    /**
     * Process and display a log message
     */
    private void processLogMessage(String message, SocketChannel clientChannel) {
        try {
            String clientAddress = clientChannel.getRemoteAddress().toString();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

            // Format the log entry
            String logEntry = String.format("[%s] %s | %s",
                    timestamp, clientAddress, message);

            // Display to console with color coding
            displayLogMessage(message, logEntry);

            // Write to file
            fileWriter.println(logEntry);
            fileWriter.flush();

        } catch (IOException e) {
            logMessage("Error processing message: " + e.getMessage(), false);
        }
    }

    /**
     * Display log message with visual formatting
     */
    private void displayLogMessage(String message, String fullEntry) {
        // Determine message type for color coding
        String messageUpper = message.toUpperCase();
        String prefix = "";

        if (messageUpper.contains("ERROR") || messageUpper.contains("FAIL")) {
            prefix = "❌ ERROR   | ";
        } else if (messageUpper.contains("WARN")) {
            prefix = "⚠️  WARNING | ";
        } else if (messageUpper.contains("SUCCESS") || messageUpper.contains("ONLINE")) {
            prefix = "✅ SUCCESS | ";
        } else if (messageUpper.contains("INFO") || messageUpper.contains("REGISTER")) {
            prefix = "ℹ️  INFO    | ";
        } else {
            prefix = "📝 LOG     | ";
        }

        System.out.println(prefix + message);

        // Show statistics every 10 messages
        if (messagesReceived % 10 == 0 && messagesReceived > 0) {
            System.out.println("\n" + "─".repeat(70));
            System.out.printf("📊 Statistics: Messages=%d | Active Connections=%d%n",
                    messagesReceived, activeConnections);
            System.out.println("─".repeat(70) + "\n");
        }
    }

    /**
     * Handle errors gracefully
     */
    private void handleError(SelectionKey key, IOException e) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            String address = channel.getRemoteAddress().toString();
            logMessage("Error with connection " + address + ": " + e.getMessage(), false);
            channel.close();
            activeConnections--;
        } catch (IOException ex) {
            // Ignore - channel already closed
        }
        key.cancel();
    }

    /**
     * Log internal server messages
     */
    private void logMessage(String message, boolean isServerMessage) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] [SERVER] %s", timestamp, message);

        if (isServerMessage) {
            System.out.println("🖥️  SERVER  | " + message);
        }

        if (fileWriter != null) {
            fileWriter.println(logEntry);
            fileWriter.flush();
        }
    }

    /**
     * Cleanup resources on shutdown
     */
    private void cleanup() {
        logMessage("Shutting down Log Server...", true);
        logMessage("Total messages received: " + messagesReceived, true);

        try {
            if (selector != null)
                selector.close();
            if (serverChannel != null)
                serverChannel.close();
            if (fileWriter != null)
                fileWriter.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }

        logMessage("Log Server stopped", true);
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        HIGH-PERFORMANCE LOG SERVICE (Member 4)                 ║");
        System.out.println("║        Using Java NIO Selectors                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            LogServer server = new LogServer();

            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.running = false;
                try {
                    server.selector.wakeup(); // Wake up the selector
                } catch (Exception e) {
                    // Ignore
                }
            }));

            server.start();

        } catch (IOException e) {
            System.err.println("Failed to start Log Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
