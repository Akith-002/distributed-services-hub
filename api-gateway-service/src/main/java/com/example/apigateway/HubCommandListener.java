package com.example.apigateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HubCommandListener - Listens for commands from Hub Server
 * 
 * PHASE 3 NEW: Message Broker Integration
 * 
 * This component implements the message broker pattern for API Gateway.
 * It listens on a TCP port for commands routed from the Hub Server and
 * sends results back through the Hub to the Dashboard.
 * 
 * Architecture:
 * Dashboard → WebSocket → Hub → TCP → HubCommandListener → API Gateway
 * API Gateway → Result → HubClient → TCP → Hub → WebSocket → Dashboard
 * 
 * Command Format from Hub:
 * {
 *   "command": "fetchWeather",
 *   "payload": {"city": "Colombo"}
 * }
 * 
 * Result Format to Hub:
 * {
 *   "result_from": "API_GATEWAY",
 *   "data": "{temperature: 28.5, ...}"
 * }
 * 
 * @author Member 2 - API Gateway
 * @version 1.0 - Phase 3 Enhancement
 */
public class HubCommandListener {
    private static final int LISTEN_PORT = 9011; // Hub commands listener port
    private static final int THREAD_POOL_SIZE = 5;
    
    private final HubClient hubClient;
    private final ExternalApiClient apiClient;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final Gson gson = new Gson();
    
    /**
     * Create a Hub command listener
     * 
     * @param hubClient Reference to Hub client for sending results back
     * @param apiClient Reference to API client for executing commands
     */
    public HubCommandListener(HubClient hubClient, ExternalApiClient apiClient) {
        this.hubClient = hubClient;
        this.apiClient = apiClient;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    /**
     * Start listening for commands from Hub
     */
    public void start() throws IOException {
        System.out.println("[HubCommandListener] Starting command listener on port " + LISTEN_PORT);
        
        serverSocket = new ServerSocket(LISTEN_PORT);
        running = true;
        
        System.out.println("[HubCommandListener] ✓ Command listener started on port " + LISTEN_PORT);
        System.out.println("[HubCommandListener] Waiting for commands from Hub...");
        
        // Accept connections in a separate thread
        new Thread(() -> acceptConnections()).start();
    }
    
    /**
     * Accept incoming command connections from Hub
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[HubCommandListener] New command connection from Hub");
                
                // Handle each command in a thread pool
                executorService.submit(() -> handleCommandConnection(clientSocket));
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("[HubCommandListener] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle a single command connection from Hub
     */
    private void handleCommandConnection(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String commandLine;
            while ((commandLine = in.readLine()) != null) {
                System.out.println("[HubCommandListener] Received command: " + commandLine);
                
                try {
                    // Parse command JSON
                    JsonObject commandJson = gson.fromJson(commandLine, JsonObject.class);
                    String command = commandJson.get("command").getAsString();
                    
                    // Execute command and get result
                    String result = executeCommand(command, commandJson);
                    
                    // Send result back to Hub
                    System.out.println("[HubCommandListener] Sending result back to Hub: " + result);
                    out.println(result);
                    out.flush();
                    
                } catch (Exception e) {
                    System.err.println("[HubCommandListener] Error processing command: " + e.getMessage());
                    String errorResult = createErrorResult("Command processing failed: " + e.getMessage());
                    out.println(errorResult);
                    out.flush();
                }
            }
            
        } catch (Exception e) {
            System.err.println("[HubCommandListener] Error handling command connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[HubCommandListener] Error closing socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Execute a command and return the result
     * 
     * @param command Command name
     * @param commandJson Full command JSON
     * @return Result as JSON string ready to send back to Hub
     */
    private String executeCommand(String command, JsonObject commandJson) throws Exception {
        System.out.println("[HubCommandListener] Executing command: " + command);
        
        switch (command.toLowerCase()) {
            case "fetchweather":
                return executeFetchWeatherCommand(commandJson);
                
            case "getservicestatus":
                return executeGetServiceStatusCommand();
                
            default:
                return createErrorResult("Unknown command: " + command);
        }
    }
    
    /**
     * Execute fetchWeather command from Hub
     * 
     * Expected command format:
     * {
     *   "command": "fetchWeather",
     *   "payload": {"city": "Colombo"}
     * }
     */
    private String executeFetchWeatherCommand(JsonObject commandJson) throws Exception {
        try {
            String city = commandJson.getAsJsonObject("payload").get("city").getAsString();
            System.out.println("[HubCommandListener] Fetching weather for: " + city);
            
            // Call external API using HttpURLConnection
            ExternalApiClient.WeatherData weatherData = apiClient.fetchWeatherByCity(city);
            
            // Create result JSON
            Map<String, Object> resultData = new LinkedHashMap<>();
            resultData.put("location", weatherData.location);
            resultData.put("temperature", weatherData.temperature);
            resultData.put("condition", weatherData.condition);
            resultData.put("timestamp", weatherData.timestamp);
            
            String dataJson = gson.toJson(resultData);
            System.out.println("[HubCommandListener] Weather data fetched: " + dataJson);
            
            // Create response format for Hub
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("result_from", "ApiGateway");
            response.put("data", dataJson);
            
            String resultJson = gson.toJson(response);
            System.out.println("[HubCommandListener] ✓ Fetch weather command completed");
            
            return resultJson;
            
        } catch (Exception e) {
            System.err.println("[HubCommandListener] Error fetching weather: " + e.getMessage());
            return createErrorResult("Weather fetch failed: " + e.getMessage());
        }
    }
    
    /**
     * Execute getServiceStatus command from Hub
     */
    private String executeGetServiceStatusCommand() {
        Map<String, Object> statusData = new LinkedHashMap<>();
        statusData.put("service", "ApiGateway");
        statusData.put("status", "online");
        statusData.put("port", 9001);
        statusData.put("commandListenerPort", LISTEN_PORT);
        statusData.put("timestamp", System.currentTimeMillis());
        
        String dataJson = gson.toJson(statusData);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result_from", "ApiGateway");
        response.put("data", dataJson);
        
        return gson.toJson(response);
    }
    
    /**
     * Create an error result message for sending back to Hub
     */
    private String createErrorResult(String errorMessage) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result_from", "ApiGateway");
        response.put("error", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        
        return gson.toJson(response);
    }
    
    /**
     * Check if listener is running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gracefully shutdown the listener
     */
    public void shutdown() {
        if (!running) {
            return;
        }
        
        running = false;
        
        System.out.println("[HubCommandListener] Shutting down command listener...");
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[HubCommandListener] Error closing server socket: " + e.getMessage());
        }
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        System.out.println("[HubCommandListener] ✓ Command listener shut down");
    }
}
