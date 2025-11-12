package com.example.fileservice;

import java.io.*;
import java.net.Socket;

/**
 * Client to connect to Hub Server for service registration
 * Sends registration and heartbeat messages
 * Member 3 - Secure File Service
 */
public class HubClient {
    
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "SecureFileService";
    private static final String SERVICE_HOST = "localhost";
    private static final int SERVICE_PORT = 9090;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread heartbeatThread;
    private volatile boolean running = false;
    
    /**
     * Connect to Hub Server and register this service
     */
    public void connect() throws IOException {
        System.out.println("[HubClient] Attempting to connect to Hub at " + HUB_HOST + ":" + HUB_PORT);
        
        socket = new Socket(HUB_HOST, HUB_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        System.out.println("[HubClient] Connected to Hub successfully");
        
        // Send registration message
        String registerMsg = String.format("REGISTER::%s::%s::%d", 
            SERVICE_NAME, SERVICE_HOST, SERVICE_PORT);
        out.println(registerMsg);
        System.out.println("[HubClient] Sending: " + registerMsg);
        
        // Read response
        String response = in.readLine();
        if (response != null && response.contains("registered")) {
            System.out.println("[HubClient] ✓ Service registered with Hub");
            running = true;
            startHeartbeat();
        } else {
            System.err.println("[HubClient] ✗ Registration failed: " + response);
        }
    }
    
    /**
     * Start heartbeat thread - sends heartbeat every 10 seconds
     */
    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            System.out.println("[HubClient] Heartbeat thread started");
            
            while (running) {
                try {
                    Thread.sleep(10000); // 10 seconds
                    
                    if (running && out != null) {
                        String heartbeat = "HEARTBEAT::" + SERVICE_NAME;
                        out.println(heartbeat);
                        System.out.println("[HubClient] Heartbeat sent to Hub");
                    }
                } catch (InterruptedException e) {
                    System.out.println("[HubClient] Heartbeat thread interrupted");
                    break;
                }
            }
            
            System.out.println("[HubClient] Heartbeat thread stopped");
        });
        
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }
    
    /**
     * Disconnect from Hub and deregister service
     */
    public void disconnect() {
        running = false;
        
        try {
            if (out != null) {
                String deregister = "DEREGISTER::" + SERVICE_NAME;
                out.println(deregister);
                System.out.println("[HubClient] Sending: " + deregister);
            }
            
            if (heartbeatThread != null) {
                heartbeatThread.interrupt();
            }
            
            if (socket != null) {
                socket.close();
            }
            
            System.out.println("[HubClient] ✓ Deregistered and disconnected from Hub");
            
        } catch (IOException e) {
            System.err.println("[HubClient] Error during disconnect: " + e.getMessage());
        }
    }
    
    /**
     * Check if connected to Hub
     */
    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }
}
