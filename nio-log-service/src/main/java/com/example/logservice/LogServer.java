package com.example.logservice;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO-based Log Server using Selector for non-blocking I/O
 * CORE CONCEPT: Java NIO (Lesson 7) - ServerSocketChannel + Selector
 */
public class LogServer implements Runnable {
    private static final int LOG_PORT = 9091;
    private static final int BUFFER_SIZE = 4096;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final LogWriter logWriter;
    private final HubForwarder hubForwarder;
    private volatile boolean running = true;

    public LogServer(LogWriter logWriter, HubForwarder hubForwarder) {
        this.logWriter = logWriter;
        this.hubForwarder = hubForwarder;
    }

    /**
     * Initialize NIO server with ServerSocketChannel and Selector
     */
    public void start() throws IOException {
        // Create ServerSocketChannel (NIO version of ServerSocket)
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false); // Non-blocking mode
        serverSocketChannel.bind(new InetSocketAddress(LOG_PORT));

        // Create Selector for managing multiple channels
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("[NIO_LOG_SERVICE] Started on port " + LOG_PORT);
        System.out.println("[NIO_LOG_SERVICE] Using non-blocking I/O with Selector");
        
        logMessage("SYSTEM", "NIO Log Service started on port " + LOG_PORT);
    }

    @Override
    public void run() {
        try {
            // Single-threaded event loop with Selector
            while (running) {
                // Block until at least one channel is ready
                int readyChannels = selector.select(1000); // 1 second timeout
                
                if (readyChannels == 0) {
                    continue; // No channels ready, loop again
                }

                // Get set of keys with events
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        System.err.println("[NIO_LOG_SERVICE] Error handling key: " + e.getMessage());
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }

                    keyIterator.remove(); // Remove processed key
                }
            }
        } catch (IOException e) {
            System.err.println("[NIO_LOG_SERVICE] Error in event loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle new client connection (OP_ACCEPT)
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false); // Non-blocking mode
            clientChannel.register(selector, SelectionKey.OP_READ);
            
            String clientAddress = clientChannel.getRemoteAddress().toString();
            System.out.println("[NIO_LOG_SERVICE] New client connected: " + clientAddress);
            logMessage("SYSTEM", "New client connected: " + clientAddress);
        }
    }

    /**
     * Handle data from client (OP_READ)
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            // Client closed connection
            String clientAddress = clientChannel.getRemoteAddress().toString();
            System.out.println("[NIO_LOG_SERVICE] Client disconnected: " + clientAddress);
            logMessage("SYSTEM", "Client disconnected: " + clientAddress);
            
            clientChannel.close();
            key.cancel();
            return;
        }

        if (bytesRead > 0) {
            buffer.flip(); // Switch to read mode
            String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();

            if (!message.isEmpty()) {
                processLogMessage(message);
            }
        }
    }

    /**
     * Process received log message
     * - Write to log file
     * - Forward to Hub for Dashboard display
     */
    private void processLogMessage(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;

        // 1. Write to log file
        logWriter.writeLog(logEntry);

        // 2. Forward to Hub for Dashboard display
        hubForwarder.forwardLog(message);

        // Console output for demo
        System.out.println("[LOG] " + message);
    }

    /**
     * Helper method to log system messages
     */
    private void logMessage(String source, String message) {
        String logEntry = source + ": " + message;
        processLogMessage(logEntry);
    }

    /**
     * Shutdown the log server gracefully
     */
    public void shutdown() {
        running = false;
        try {
            if (selector != null && selector.isOpen()) {
                selector.wakeup();
                selector.close();
            }
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            System.out.println("[NIO_LOG_SERVICE] Shutdown complete");
        } catch (IOException e) {
            System.err.println("[NIO_LOG_SERVICE] Error during shutdown: " + e.getMessage());
        }
    }
}
