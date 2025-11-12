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
    private static final String SERVICE_NAME = "JSSE_SERVICE";
    private static final String SERVICE_HOST = "localhost";
    private static final int SERVICE_PORT = 9090;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread heartbeatThread;
    private Thread commandListenerThread;
    private volatile boolean running = false;
    private CommandListener commandListener;
    
    /**
     * Set a command listener for incoming commands
     */
    public void setCommandListener(CommandListener listener) {
        this.commandListener = listener;
    }
    
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
            startCommandListener();  // Start listening for commands from Hub
        } else {
            System.err.println("[HubClient] ✗ Registration failed: " + response);
        }
    }
    
    /**
     * Start command listener thread - listens for commands from Hub
     */
    private void startCommandListener() {
        commandListenerThread = new Thread(() -> {
            System.out.println("[HubClient] Command listener thread started");
            
            while (running) {
                try {
                    String message = in.readLine();
                    if (message != null) {
                        // Ignore responses (OK:: or ERROR::)
                        if (message.startsWith("OK::") || message.startsWith("ERROR::")) {
                            System.out.println("[HubClient] Received response from Hub: " + message);
                            continue;
                        }
                        
                        // Process actual commands
                        System.out.println("[HubClient] Received command from Hub: " + message);
                        
                        // Notify listener if one is registered
                        if (commandListener != null) {
                            commandListener.onCommand(message);
                        }
                    } else {
                        // Connection closed
                        System.out.println("[HubClient] Hub connection closed");
                        running = false;
                        break;
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[HubClient] Error reading command: " + e.getMessage());
                    }
                    break;
                }
            }
            
            System.out.println("[HubClient] Command listener thread stopped");
        });
        
        commandListenerThread.setDaemon(true);
        commandListenerThread.start();
    }
    
    /**
     * Send a result message back to the Hub
     */
    public void sendResult(String resultJson) {
        if (out != null && running) {
            out.println(resultJson);
            out.flush();
            System.out.println("[HubClient] Sent result to Hub: " + resultJson);
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
            
            if (commandListenerThread != null) {
                commandListenerThread.interrupt();
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
