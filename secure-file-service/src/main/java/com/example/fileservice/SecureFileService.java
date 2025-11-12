package com.example.fileservice;

import java.io.IOException;

/**
 * Secure File Service - Main Entry Point
 * Member 3 - JSSE (Java Secure Socket Extension) Implementation
 * 
 * This service demonstrates:
 * - SSLServerSocket for secure connections
 * - Self-signed certificates and KeyStore
 * - File storage over SSL/TLS
 * - Hub registration and heartbeat
 */
public class SecureFileService {
    
    private static HubClient hubClient;
    private static SSLFileServer fileServer;
    
    public static void main(String[] args) {
        printBanner();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown] Received shutdown signal");
            shutdown();
        }));
        
        try {
            // Step 1: Connect to Hub Server
            System.out.println("\n[STARTUP] Step 1: Connecting to Hub Server...");
            hubClient = new HubClient();
            hubClient.connect();
            System.out.println("[STARTUP] ✓ Connected to Hub successfully\n");
            
            // Step 2: Start SSL File Server
            System.out.println("[STARTUP] Step 2: Starting SSL File Server...");
            fileServer = new SSLFileServer();
            
            // Start server in new thread
            Thread serverThread = new Thread(() -> {
                try {
                    fileServer.start();
                } catch (IOException e) {
                    System.err.println("[STARTUP] ✗ Failed to start SSL server: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            });
            
            serverThread.start();
            
            // Wait a moment for server to initialize
            Thread.sleep(1000);
            
            if (fileServer.isRunning()) {
                System.out.println("[STARTUP] ✓ SSL File Server started successfully\n");
                printSuccessBanner();
                
                // Keep main thread alive
                serverThread.join();
            } else {
                System.err.println("[STARTUP] ✗ SSL File Server failed to start");
                System.exit(1);
            }
            
        } catch (IOException e) {
            System.err.println("\n[ERROR] Failed to connect to Hub: " + e.getMessage());
            System.err.println("[ERROR] Make sure Hub Server is running on localhost:7070");
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("\n[INFO] Service interrupted");
            shutdown();
        } catch (Exception e) {
            System.err.println("\n[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Shutdown the service gracefully
     */
    private static void shutdown() {
        System.out.println("\n==============================================================================");
        System.out.println("SHUTTING DOWN");
        System.out.println("==============================================================================\n");
        
        if (fileServer != null) {
            System.out.println("[Shutdown] Stopping SSL File Server...");
            fileServer.stop();
            System.out.println("[Shutdown] ✓ SSL File Server stopped\n");
        }
        
        if (hubClient != null) {
            System.out.println("[Shutdown] Disconnecting from Hub...");
            hubClient.disconnect();
            System.out.println("[Shutdown] ✓ Disconnected from Hub\n");
        }
        
        System.out.println("[Shutdown] ✓ Secure File Service shutdown successfully\n");
    }
    
    /**
     * Print startup banner
     */
    private static void printBanner() {
        System.out.println("\n==============================================================================");
        System.out.println("  SECURE FILE SERVICE - MEMBER 3");
        System.out.println("  Network Programming Group Assignment");
        System.out.println("==============================================================================");
        System.out.println();
        System.out.println("Core Concepts:");
        System.out.println("  🔐 JSSE (Java Secure Socket Extension)");
        System.out.println("  🔐 SSLServerSocket & SSLSocket");
        System.out.println("  🔐 Self-signed certificates & KeyStore");
        System.out.println("  🔐 TLS 1.2/1.3 encryption");
        System.out.println();
        System.out.println("Service Endpoints:");
        System.out.println("  🔒 SSL File Server: localhost:9090");
        System.out.println("  📡 Hub Registration: localhost:7070");
        System.out.println();
        System.out.println("File Commands:");
        System.out.println("  STORE::<filename>::<size>");
        System.out.println("  RETRIEVE::<filename>");
        System.out.println("  LIST");
        System.out.println("  DELETE::<filename>");
        System.out.println();
        System.out.println("==============================================================================");
        System.out.println("STARTING UP");
        System.out.println("==============================================================================");
    }
    
    /**
     * Print success banner
     */
    private static void printSuccessBanner() {
        System.out.println("==============================================================================");
        System.out.println("✓ SECURE FILE SERVICE STARTED SUCCESSFULLY");
        System.out.println("==============================================================================");
        System.out.println();
        System.out.println("✓ Hub Registration: SUCCESS");
        System.out.println("✓ SSL Server: RUNNING on port 9090");
        System.out.println("✓ Heartbeat: ACTIVE (every 10 seconds)");
        System.out.println();
        System.out.println("Security Features:");
        System.out.println("  ✓ SSL/TLS encryption enabled");
        System.out.println("  ✓ Regular sockets REJECTED (SSL only)");
        System.out.println("  ✓ Self-signed certificate active");
        System.out.println("  ✓ Secure file storage");
        System.out.println();
        System.out.println("Files Directory: ./files/");
        System.out.println("Keystore: ./keystore/fileservice.keystore");
        System.out.println();
        System.out.println("Connected to Hub - heartbeats sent every 10 seconds");
        System.out.println("Waiting for SSL client connections on port 9090");
        System.out.println();
        System.out.println("[SecureFileService] Press Ctrl+C to shutdown");
        System.out.println();
    }
}
