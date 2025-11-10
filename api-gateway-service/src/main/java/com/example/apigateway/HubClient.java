package com.example.apigateway;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HubClient - Handles communication with the Hub Server
 * 
 * Responsibilities:
 * - Register API Gateway Service with Hub
 * - Send periodic heartbeat messages
 * - Gracefully deregister on shutdown
 * 
 * Protocol:
 * - REGISTER::ApiGateway::localhost::9001
 * - HEARTBEAT::ApiGateway
 * - DEREGISTER::ApiGateway
 */
public class HubClient {
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "ApiGateway";
    private static final String SERVICE_HOST = "localhost";
    private static final int SERVICE_PORT = 9001;
    private static final long HEARTBEAT_INTERVAL = 10; // seconds
    
    private Socket socket;
    private PrintWriter out;
    private ScheduledExecutorService scheduler;
    private volatile boolean connected = false;
    
    /**
     * Initialize HubClient (creates scheduler but doesn't connect yet)
     */
    public HubClient() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Connect to Hub and register service
     */
    public boolean connect() {
        try {
            System.out.println("[HubClient] Attempting to connect to Hub at " + HUB_HOST + ":" + HUB_PORT);
            socket = new Socket(HUB_HOST, HUB_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            System.out.println("[HubClient] Connected to Hub successfully");
            
            // Register service
            register();
            
            // Start heartbeat scheduler
            startHeartbeat();
            
            return true;
        } catch (IOException e) {
            System.err.println("[HubClient] Failed to connect to Hub: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    /**
     * Send registration message to Hub
     */
    private void register() {
        if (!connected || out == null) {
            System.err.println("[HubClient] Not connected to Hub, cannot register");
            return;
        }
        
        String registerMsg = String.format("REGISTER::%s::%s::%d", 
            SERVICE_NAME, SERVICE_HOST, SERVICE_PORT);
        
        System.out.println("[HubClient] Sending: " + registerMsg);
        out.println(registerMsg);
        out.flush();
        
        System.out.println("[HubClient] ✓ Service registered with Hub");
    }
    
    /**
     * Start periodic heartbeat to Hub
     */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (connected && out != null) {
                String heartbeatMsg = String.format("HEARTBEAT::%s", SERVICE_NAME);
                try {
                    out.println(heartbeatMsg);
                    out.flush();
                    System.out.println("[HubClient] Heartbeat sent to Hub");
                } catch (Exception e) {
                    System.err.println("[HubClient] Heartbeat failed: " + e.getMessage());
                    connected = false;
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Deregister service from Hub and close connection
     */
    public void disconnect() {
        if (!connected) {
            System.out.println("[HubClient] Already disconnected");
            return;
        }
        
        try {
            String deregisterMsg = String.format("DEREGISTER::%s", SERVICE_NAME);
            System.out.println("[HubClient] Sending: " + deregisterMsg);
            out.println(deregisterMsg);
            out.flush();
            
            Thread.sleep(500); // Give Hub time to process deregistration
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            connected = false;
            System.out.println("[HubClient] ✓ Deregistered and disconnected from Hub");
            
        } catch (Exception e) {
            System.err.println("[HubClient] Error during disconnect: " + e.getMessage());
        } finally {
            scheduler.shutdown();
        }
    }
    
    /**
     * Check if client is connected to Hub
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Shutdown gracefully
     */
    public void shutdown() {
        disconnect();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
}
