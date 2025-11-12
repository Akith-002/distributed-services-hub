package com.example.taskservice;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;

/**
 * Manages registration, heartbeat, and command listening with Hub
 */
public class HubClient {
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "RMI_SERVICE";
    private static final String SERVICE_HOST = "localhost";
    private static final int SERVICE_PORT = 1099;
    
    private ScheduledExecutorService heartbeatScheduler;
    private Socket registrationSocket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean listening = false;

    /**
     * Register service with Hub
     * Format: REGISTER::ServiceName::Host::Port
     */
    public void register() {
        try {
            registrationSocket = new Socket(HUB_HOST, HUB_PORT);
            out = new PrintWriter(registrationSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(registrationSocket.getInputStream()));
            
            // Correct format: REGISTER::ServiceName::Host::Port
            String registerMsg = "REGISTER::" + SERVICE_NAME + "::" + SERVICE_HOST + "::" + SERVICE_PORT;
            out.println(registerMsg);
            
            System.out.println("[HUB_CLIENT] Registered with Hub: " + registerMsg);
            
            // Start heartbeat
            startHeartbeat();
            
            // Start command listener on same socket
            startCommandListener();
            
        } catch (IOException e) {
            System.err.println("[HUB_CLIENT] Failed to register with Hub: " + e.getMessage());
            System.err.println("[HUB_CLIENT] Make sure Hub is running on port " + HUB_PORT);
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
     * Start listening for commands from Hub on the same socket
     */
    private void startCommandListener() {
        listening = true;
        Thread listenerThread = new Thread(() -> {
            System.out.println("[HUB_CLIENT] Command listener started on registration socket");
            try {
                String message;
                while (listening && (message = in.readLine()) != null) {
                    System.out.println("[HUB_CLIENT] Received from Hub: " + message);
                    handleCommand(message);
                }
            } catch (IOException e) {
                if (listening) {
                    System.err.println("[HUB_CLIENT] Error reading from Hub: " + e.getMessage());
                }
            }
        }, "HubCommandListener");
        listenerThread.setDaemon(false);
        listenerThread.start();
    }

    /**
     * Handle command from Hub by invoking RMI method
     */
    private void handleCommand(String message) {
        // Filter out non-command messages (heartbeats, OK responses, etc.)
        if (message.startsWith("OK::") || message.startsWith("HEARTBEAT::") || 
            message.startsWith("REGISTER::") || message.startsWith("DEREGISTER::")) {
            // Ignore these protocol messages
            return;
        }
        
        // Only process JSON commands
        if (!message.trim().startsWith("{")) {
            // Not a JSON command, ignore
            return;
        }
        
        try {
            JSONObject command = new JSONObject(message);
            String taskName = command.optString("command", "");
            
            if (taskName.isEmpty()) {
                taskName = command.optString("payload", "");
            }
            
            System.out.println("[HUB_CLIENT] Executing task: " + taskName);
            
            // Look up RMI service and invoke method
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            TaskService service = (TaskService) registry.lookup("TaskService");
            
            // Execute the task remotely
            String result = service.executeTask(taskName);
            
            System.out.println("[HUB_CLIENT] Task completed: " + result);
            
            // Send result back to Hub
            sendResultToHub(taskName, result);
            
        } catch (Exception e) {
            System.err.println("[HUB_CLIENT] Error handling command: " + e.getMessage());
            e.printStackTrace();
            sendErrorToHub(e.getMessage());
        }
    }

    /**
     * Send successful result back to Hub for Dashboard display
     */
    private void sendResultToHub(String taskName, String result) {
        try {
            JSONObject response = new JSONObject();
            response.put("result_from", SERVICE_NAME);
            
            JSONObject data = new JSONObject();
            data.put("task", taskName);
            data.put("result", result);
            data.put("status", "success");
            
            response.put("data", data.toString());
            
            out.println(response.toString());
            System.out.println("[HUB_CLIENT] Sent result to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[HUB_CLIENT] Error sending result: " + e.getMessage());
        }
    }

    /**
     * Send error result back to Hub
     */
    private void sendErrorToHub(String errorMessage) {
        try {
            JSONObject response = new JSONObject();
            response.put("result_from", SERVICE_NAME);
            
            JSONObject data = new JSONObject();
            data.put("status", "error");
            data.put("error", errorMessage);
            
            response.put("data", data.toString());
            
            out.println(response.toString());
            System.out.println("[HUB_CLIENT] Sent error to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[HUB_CLIENT] Error sending error message: " + e.getMessage());
        }
    }

    /**
     * Deregister from Hub and cleanup
     */
    public void deregister() {
        try {
            listening = false;
            
            if (heartbeatScheduler != null) {
                heartbeatScheduler.shutdown();
            }
            
            if (out != null) {
                String deregisterMsg = "DEREGISTER::" + SERVICE_NAME;
                out.println(deregisterMsg);
                System.out.println("[HUB_CLIENT] Deregistered from Hub");
                out.close();
            }
            
            if (in != null) {
                in.close();
            }
            
            if (registrationSocket != null && !registrationSocket.isClosed()) {
                registrationSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[HUB_CLIENT] Error during deregistration: " + e.getMessage());
        }
    }
}
