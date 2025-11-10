package com.example.apigateway;

/**
 * ApiGatewayService - Main Entry Point for Phase 2, Member 2
 * 
 * CORE CONCEPTS:
 * - HttpURLConnection (Lesson 5): External API communication
 * - WebSocket: Real-time communication with React Dashboard
 * - TCP Socket: Service registration with Hub
 * - Multithreading: Concurrent request handling
 * 
 * RESPONSIBILITIES:
 * 1. Register with Hub Server (port 7070)
 * 2. Start WebSocket server for React Dashboard (port 9001)
 * 3. Handle HttpURLConnection calls to external APIs
 * 4. Process commands from dashboard and return data
 * 5. Send heartbeat to Hub every 10 seconds
 * 6. Graceful shutdown
 * 
 * SERVICE PORTS:
 * - Hub Registration: TCP to localhost:7070
 * - WebSocket API: ws://localhost:9001/api
 * - HTTP Health Check: http://localhost:9001/health
 * - HTTP Status: http://localhost:9001/status
 */
public class ApiGatewayService {
    
    private static HubClient hubClient;
    private static WebSocketServer wsServer;
    private static ExternalApiClient apiClient;
    private static volatile boolean running = false;
    
    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println("  API GATEWAY SERVICE - PHASE 2, MEMBER 2");
        System.out.println("  Network Programming Group Assignment");
        System.out.println("==============================================================================");
        System.out.println();
        
        System.out.println("Core Concepts:");
        System.out.println("  ✓ HttpURLConnection: External API communication (Lesson 5)");
        System.out.println("  ✓ WebSocket: Real-time dashboard communication");
        System.out.println("  ✓ TCP Socket: Hub service registration");
        System.out.println("  ✓ Multithreading: Concurrent request handling");
        System.out.println();
        
        System.out.println("Service Endpoints:");
        System.out.println("  • WebSocket API: ws://localhost:9001/api");
        System.out.println("  • HTTP Health: http://localhost:9001/health");
        System.out.println("  • HTTP Status: http://localhost:9001/status");
        System.out.println("  • Hub TCP: localhost:7070");
        System.out.println();
        
        try {
            startup();
        } catch (Exception e) {
            System.err.println("[ApiGatewayService] Startup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Startup sequence
     */
    private static void startup() throws Exception {
        System.out.println("==============================================================================");
        System.out.println("STARTING UP");
        System.out.println("==============================================================================");
        System.out.println();
        
        try {
            // Step 1: Initialize External API Client
            System.out.println("[STARTUP] Step 1: Initializing External API Client...");
            apiClient = new ExternalApiClient();
            System.out.println("[STARTUP] ✓ External API Client initialized");
            System.out.println();
            
            // Step 2: Connect to Hub
            System.out.println("[STARTUP] Step 2: Connecting to Hub Server...");
            hubClient = new HubClient();
            if (!hubClient.connect()) {
                System.err.println("[STARTUP] ✗ Failed to connect to Hub. Make sure Hub is running on port 7070");
                System.err.println("[STARTUP] You can run Hub Server with: java -jar ../hub-server/target/hub-server-1.0-SNAPSHOT.jar");
                throw new Exception("Hub connection failed");
            }
            System.out.println("[STARTUP] ✓ Connected to Hub successfully");
            System.out.println();
            
            // Step 3: Start WebSocket Server
            System.out.println("[STARTUP] Step 3: Starting WebSocket Server...");
            wsServer = new WebSocketServer(apiClient);
            wsServer.start();
            System.out.println("[STARTUP] ✓ WebSocket Server started successfully");
            System.out.println();
            
            running = true;
            
            // Print startup complete message
            printStartupComplete();
            
            // Setup graceful shutdown hook
            setupShutdownHook();
            
            // Keep the application running
            System.out.println("[ApiGatewayService] Press Ctrl+C to shutdown");
            System.out.println();
            
            // Block main thread
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("[STARTUP] ✗ Startup failed: " + e.getMessage());
            shutdown();
            throw e;
        }
    }
    
    /**
     * Print startup complete message
     */
    private static void printStartupComplete() {
        System.out.println("==============================================================================");
        System.out.println("✓ API GATEWAY SERVICE STARTED SUCCESSFULLY");
        System.out.println("==============================================================================");
        System.out.println();
        System.out.println("✓ Hub Registration: SUCCESS");
        System.out.println("✓ WebSocket Server: RUNNING on port 9001");
        System.out.println("✓ External API Client: READY");
        System.out.println();
        System.out.println("Features Available:");
        System.out.println("  • fetchWeather <city>: Get weather data from external API using HttpURLConnection");
        System.out.println("  • getServiceStatus: Get API Gateway service status");
        System.out.println("  • ping: Keep-alive check");
        System.out.println();
        System.out.println("Connected to Hub - heartbeats sent every 10 seconds");
        System.out.println("Waiting for dashboard connections on ws://localhost:9001/api");
        System.out.println();
    }
    
    /**
     * Setup graceful shutdown hook
     */
    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("[Shutdown] Received shutdown signal");
            shutdown();
        }));
    }
    
    /**
     * Graceful shutdown
     */
    private static void shutdown() {
        if (!running) {
            return;
        }
        
        running = false;
        
        System.out.println();
        System.out.println("==============================================================================");
        System.out.println("SHUTTING DOWN");
        System.out.println("==============================================================================");
        System.out.println();
        
        try {
            // Close WebSocket server
            if (wsServer != null && wsServer.isRunning()) {
                System.out.println("[Shutdown] Stopping WebSocket server...");
                wsServer.stop();
                System.out.println("[Shutdown] ✓ WebSocket server stopped");
            }
            
            // Disconnect from Hub
            if (hubClient != null && hubClient.isConnected()) {
                System.out.println("[Shutdown] Disconnecting from Hub...");
                hubClient.shutdown();
                System.out.println("[Shutdown] ✓ Disconnected from Hub");
            }
            
            System.out.println();
            System.out.println("[Shutdown] ✓ API Gateway Service shutdown successfully");
            
        } catch (Exception e) {
            System.err.println("[Shutdown] Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Check if service is running
     */
    public static boolean isRunning() {
        return running;
    }
    
    /**
     * Get Hub client instance
     */
    public static HubClient getHubClient() {
        return hubClient;
    }
    
    /**
     * Get WebSocket server instance
     */
    public static WebSocketServer getWebSocketServer() {
        return wsServer;
    }
    
    /**
     * Get External API client instance
     */
    public static ExternalApiClient getApiClient() {
        return apiClient;
    }
}
