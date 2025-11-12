package com.example.logservice;

import java.io.IOException;

/**
 * NIO Log Service - High-performance logging using Java NIO
 * Member 4's Core Concept: Selector-based non-blocking I/O
 */
public class NioLogService {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    NIO LOG SERVICE - Starting...               ");
        System.out.println("    Port: 9091                                   ");
        System.out.println("    Core Concept: Java NIO (Selector-based I/O)  ");
        System.out.println("=================================================");
        
        // Create components
        LogWriter logWriter = new LogWriter();
        HubForwarder hubForwarder = new HubForwarder();
        HubClient hubClient = new HubClient();
        
        // Connect to Hub for log forwarding
        hubForwarder.connect();
        
        // Register with Hub
        hubClient.register();
        
        // Create and start NIO Log Server
        LogServer logServer = new LogServer(logWriter, hubForwarder);
        
        try {
            logServer.start();
            
            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[NIO_LOG_SERVICE] Shutting down...");
                logServer.shutdown();
                hubClient.deregister();
                hubForwarder.close();
                logWriter.close();
                System.out.println("[NIO_LOG_SERVICE] Shutdown complete");
            }));
            
            // Start event loop in new thread
            Thread serverThread = new Thread(logServer, "NIO-LogServer-Thread");
            serverThread.setDaemon(false); // Ensure it's not a daemon thread
            serverThread.start();
            
            System.out.println("[NIO_LOG_SERVICE] Service ready and waiting for log messages...");
            System.out.println("[NIO_LOG_SERVICE] Using single-threaded event loop with Selector");
            System.out.println("[NIO_LOG_SERVICE] Press Ctrl+C to stop");
            
            // Keep main thread alive indefinitely
            while (serverThread.isAlive()) {
                try {
                    serverThread.join(5000); // Check every 5 seconds
                    if (!serverThread.isAlive()) {
                        System.err.println("[NIO_LOG_SERVICE] Server thread died unexpectedly!");
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("[NIO_LOG_SERVICE] Main thread interrupted");
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("[NIO_LOG_SERVICE] Failed to start: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[NIO_LOG_SERVICE] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
