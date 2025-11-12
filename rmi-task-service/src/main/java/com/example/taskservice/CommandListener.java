package com.example.taskservice;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Listens for commands from Hub and executes them via RMI
 */
public class CommandListener implements Runnable {
    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "RMI_SERVICE";
    
    private Socket hubConnection;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    /**
     * Connect to Hub and start listening for commands
     */
    public void connect() {
        try {
            hubConnection = new Socket(HUB_HOST, HUB_PORT);
            in = new BufferedReader(new InputStreamReader(hubConnection.getInputStream()));
            out = new PrintWriter(hubConnection.getOutputStream(), true);
            
            System.out.println("[COMMAND_LISTENER] Connected to Hub for command listening");
        } catch (IOException e) {
            System.err.println("[COMMAND_LISTENER] Failed to connect to Hub: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (hubConnection == null) {
            System.err.println("[COMMAND_LISTENER] Not connected to Hub");
            return;
        }

        System.out.println("[COMMAND_LISTENER] Listening for commands from Hub...");
        
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                System.out.println("[COMMAND_LISTENER] Received command: " + message);
                handleCommand(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[COMMAND_LISTENER] Error reading from Hub: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Handle command from Hub by invoking RMI method
     */
    private void handleCommand(String commandJson) {
        try {
            JSONObject command = new JSONObject(commandJson);
            String taskName = command.optString("command", "");
            
            if (taskName.isEmpty()) {
                taskName = command.optString("payload", "");
            }
            
            System.out.println("[COMMAND_LISTENER] Executing task: " + taskName);
            
            // Look up RMI service and invoke method
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            TaskService service = (TaskService) registry.lookup("TaskService");
            
            // Execute the task remotely
            String result = service.executeTask(taskName);
            
            System.out.println("[COMMAND_LISTENER] Task completed: " + result);
            
            // Send result back to Hub
            sendResultToHub(taskName, result);
            
        } catch (Exception e) {
            System.err.println("[COMMAND_LISTENER] Error handling command: " + e.getMessage());
            e.printStackTrace();
            
            // Send error result to Hub
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
            System.out.println("[COMMAND_LISTENER] Sent result to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[COMMAND_LISTENER] Error sending result: " + e.getMessage());
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
            System.out.println("[COMMAND_LISTENER] Sent error to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[COMMAND_LISTENER] Error sending error message: " + e.getMessage());
        }
    }

    /**
     * Stop listening and cleanup
     */
    public void stop() {
        running = false;
        cleanup();
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (hubConnection != null && !hubConnection.isClosed()) {
                hubConnection.close();
            }
            System.out.println("[COMMAND_LISTENER] Disconnected from Hub");
        } catch (IOException e) {
            System.err.println("[COMMAND_LISTENER] Error during cleanup: " + e.getMessage());
        }
    }
}
