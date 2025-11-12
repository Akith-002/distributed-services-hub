package com.example.apigateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
 * - Listen for commands from Hub on registration socket
 * - Gracefully deregister on shutdown
 * 
 * Protocol:
 * - REGISTER::ApiGateway::localhost::9001
 * - HEARTBEAT::ApiGateway
 * - DEREGISTER::ApiGateway
 */
public class HubClient {
    private static final String HUB_HOST = "127.0.0.1";
    private static final int HUB_PORT = 7070;
    private static final String SERVICE_NAME = "ApiGateway";
    private static final String SERVICE_HOST = "127.0.0.1";
    private static final int SERVICE_PORT = 9001;
    private static final long HEARTBEAT_INTERVAL = 10; // seconds
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ScheduledExecutorService scheduler;
    private volatile boolean connected = false;
    private volatile boolean listening = false;
    private ExternalApiClient apiClient;
    
    /**
     * Initialize HubClient (creates scheduler but doesn't connect yet)
     */
    public HubClient() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Set the API client for executing commands
     */
    public void setApiClient(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Connect to Hub and register service
     */
    public boolean connect() {
        try {
            System.out.println("[HubClient] Attempting to connect to Hub at " + HUB_HOST + ":" + HUB_PORT);
            socket = new Socket(HUB_HOST, HUB_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            
            System.out.println("[HubClient] Connected to Hub successfully");
            
            // Register service
            register();
            
            // Start heartbeat scheduler
            startHeartbeat();
            
            // Start command listener on registration socket
            startCommandListener();
            
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
        
        // Simplified registration - just service name, host, and port
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
     * Start listening for commands from Hub on the same socket
     */
    private void startCommandListener() {
        listening = true;
        Thread listenerThread = new Thread(() -> {
            System.out.println("[HubClient] Command listener started on registration socket");
            try {
                String message;
                while (listening && (message = in.readLine()) != null) {
                    System.out.println("[HubClient] Received from Hub: " + message);
                    handleCommand(message);
                }
            } catch (IOException e) {
                if (listening) {
                    System.err.println("[HubClient] Error reading from Hub: " + e.getMessage());
                }
            }
        }, "HubCommandListener");
        listenerThread.setDaemon(false);
        listenerThread.start();
    }
    
    /**
     * Handle command from Hub
     */
    private void handleCommand(String message) {
        // Filter out non-command messages (heartbeats, OK responses, etc.)
        if (message.startsWith("OK::") || message.startsWith("HEARTBEAT::") || 
            message.startsWith("REGISTER::") || message.startsWith("DEREGISTER::")) {
            return;
        }
        
        // Only process JSON commands
        if (!message.trim().startsWith("{")) {
            return;
        }
        
        try {
            Gson gson = new Gson();
            JsonObject command = gson.fromJson(message, JsonObject.class);
            
            String commandType = command.has("command") ? command.get("command").getAsString() : "";
            
            System.out.println("[HubClient] Executing command: " + commandType);
            
            if (apiClient != null) {
                // Execute command based on type
                if (commandType.equals("fetchWeather") || commandType.equals("get-weather")) {
                    String city = "Colombo"; // Default city
                    if (command.has("payload") && command.get("payload").isJsonObject()) {
                        JsonObject payload = command.getAsJsonObject("payload");
                        if (payload.has("city")) {
                            city = payload.get("city").getAsString();
                        }
                    }
                    ExternalApiClient.WeatherData weatherData = apiClient.fetchWeatherByCity(city);
                    
                    // Send weather data as JSON object
                    sendWeatherResultToHub(weatherData);
                } else {
                    sendErrorToHub("Unknown command: " + commandType);
                }
                
                System.out.println("[HubClient] Command completed");
            } else {
                System.err.println("[HubClient] API client not set");
                sendErrorToHub("API client not initialized");
            }
            
        } catch (Exception e) {
            System.err.println("[HubClient] Error handling command: " + e.getMessage());
            e.printStackTrace();
            sendErrorToHub(e.getMessage());
        }
    }
    
    /**
     * Send weather result back to Hub for Dashboard display
     */
    private void sendWeatherResultToHub(ExternalApiClient.WeatherData weatherData) {
        try {
            // Create the outer response object
            JsonObject response = new JsonObject();
            response.addProperty("result_from", SERVICE_NAME);
            
            // Create the weather data object matching Open-Meteo API format for Dashboard
            JsonObject dataObj = new JsonObject();
            
            // Add current weather nested object (Dashboard expects this structure)
            JsonObject current = new JsonObject();
            current.addProperty("temperature_2m", weatherData.temperature);
            current.addProperty("weather_code", weatherData.weatherCode);
            
            dataObj.add("current", current);
            dataObj.addProperty("location", weatherData.location);
            dataObj.addProperty("timestamp", weatherData.timestamp);
            
            // Set data as JSON string (Dashboard will parse it)
            response.addProperty("data", dataObj.toString());
            
            out.println(response.toString());
            System.out.println("[HubClient] Sent weather result to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[HubClient] Error sending result: " + e.getMessage());
        }
    }
    
    /**
     * Send error result back to Hub for Dashboard display
     */
    private void sendErrorToHub(String errorMessage) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("result_from", SERVICE_NAME);
            
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("error", errorMessage);
            
            response.addProperty("data", errorObj.toString());
            
            out.println(response.toString());
            System.out.println("[HubClient] Sent error to Hub: " + response.toString());
            
        } catch (Exception e) {
            System.err.println("[HubClient] Error sending error message: " + e.getMessage());
        }
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
            listening = false;
            
            String deregisterMsg = String.format("DEREGISTER::%s", SERVICE_NAME);
            System.out.println("[HubClient] Sending: " + deregisterMsg);
            out.println(deregisterMsg);
            out.flush();
            
            Thread.sleep(500); // Give Hub time to process deregistration
            
            if (in != null) {
                in.close();
            }
            
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
