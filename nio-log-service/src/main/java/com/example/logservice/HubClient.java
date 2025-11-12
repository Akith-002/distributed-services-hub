package com.example.logservice;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Manages registration and heartbeat with Hub
 */
public class HubClient {
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "NIO_SERVICE";
    private static final int SERVICE_PORT = 9091;
    
    private ScheduledExecutorService heartbeatScheduler;
    private Socket registrationSocket;
    private PrintWriter out;

    /**
     * Register service with Hub
     */
    public void register() {
        try {
            registrationSocket = new Socket(HUB_HOST, HUB_PORT);
            out = new PrintWriter(registrationSocket.getOutputStream(), true);
            
            String registerMsg = "REGISTER::" + SERVICE_NAME + "::localhost::" + SERVICE_PORT;
            out.println(registerMsg);
            
            System.out.println("[HUB_CLIENT] Registered with Hub: " + registerMsg);
            
            // Start heartbeat
            startHeartbeat();
            
        } catch (IOException e) {
            System.err.println("[HUB_CLIENT] Failed to register with Hub: " + e.getMessage());
        }
    }

    /**
     * Start periodic heartbeat to Hub (every 10 seconds)
     */
    private void startHeartbeat() {
        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                if (out != null) {
                    String heartbeat = "HEARTBEAT::" + SERVICE_NAME;
                    out.println(heartbeat);
                    System.out.println("[HUB_CLIENT] Heartbeat sent");
                }
            } catch (Exception e) {
                System.err.println("[HUB_CLIENT] Error sending heartbeat: " + e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Deregister from Hub and cleanup
     */
    public void deregister() {
        try {
            if (heartbeatScheduler != null) {
                heartbeatScheduler.shutdown();
            }
            
            if (out != null) {
                String deregisterMsg = "DEREGISTER::" + SERVICE_NAME;
                out.println(deregisterMsg);
                System.out.println("[HUB_CLIENT] Deregistered from Hub");
                out.close();
            }
            
            if (registrationSocket != null && !registrationSocket.isClosed()) {
                registrationSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[HUB_CLIENT] Error during deregistration: " + e.getMessage());
        }
    }
}
