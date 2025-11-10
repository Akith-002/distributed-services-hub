package com.example.apigateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocketServer - Handles WebSocket connections from React Dashboard
 * 
 * Responsibilities:
 * - Accept WebSocket connections from dashboard
 * - Receive commands (e.g., "fetchWeather")
 * - Call external APIs based on commands
 * - Send results back to dashboard via WebSocket
 * - Broadcast service status updates
 * 
 * Protocol:
 * Client → Server:
 * {
 *   "command": "fetchWeather",
 *   "city": "Colombo"
 * }
 * 
 * Server → Client:
 * {
 *   "type": "WEATHER_RESPONSE",
 *   "payload": {
 *     "location": "Colombo, Sri Lanka",
 *     "temperature": 28.5,
 *     "condition": "Sunny"
 *   }
 * }
 */
public class WebSocketServer {
    private final Javalin app;
    private final ExternalApiClient apiClient;
    private final CopyOnWriteArraySet<WsContext> sessions = new CopyOnWriteArraySet<>();
    private final Gson gson = new Gson();
    
    /**
     * Initialize WebSocket server
     */
    public WebSocketServer(ExternalApiClient apiClient) {
        this.app = Javalin.create();
        this.apiClient = apiClient;
        setupRoutes();
    }
    
    /**
     * Setup Javalin routes and WebSocket endpoint
     */
    private void setupRoutes() {
        // Health check endpoint
        app.get("/health", ctx -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "UP");
            response.put("service", "ApiGateway");
            response.put("port", 9001);
            ctx.json(response);
        });
        
        // Status endpoint
        app.get("/status", ctx -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("service", "ApiGateway");
            response.put("status", "Running");
            response.put("connectedDashboards", sessions.size());
            response.put("timestamp", System.currentTimeMillis());
            ctx.json(response);
        });
        
        // WebSocket endpoint for dashboard
        app.ws("/api", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("[WebSocket] Dashboard connected");
                sessions.add(ctx);
                broadcastStatusUpdate();
            });
            
            ws.onMessage(ctx -> {
                try {
                    handleWebSocketMessage(ctx.message(), ctx);
                } catch (Exception e) {
                    System.err.println("[WebSocket] Error processing message: " + e.getMessage());
                    ctx.send(createErrorResponse("Failed to process command: " + e.getMessage()));
                }
            });
            
            ws.onClose(ctx -> {
                System.out.println("[WebSocket] Dashboard disconnected");
                sessions.remove(ctx);
                broadcastStatusUpdate();
            });
            
            ws.onError(ctx -> {
                System.err.println("[WebSocket] Error: " + ctx.error());
            });
        });
    }
    
    /**
     * Handle incoming WebSocket message from dashboard
     */
    private void handleWebSocketMessage(String message, WsContext ctx) {
        System.out.println("[WebSocket] Received message: " + message);
        
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String command = json.get("command").getAsString();
            
            switch (command.toLowerCase()) {
                case "fetchweather":
                    handleFetchWeatherCommand(ctx, json);
                    break;
                    
                case "getservicestatus":
                    handleGetServiceStatusCommand(ctx);
                    break;
                    
                case "ping":
                    ctx.send(createPongResponse());
                    break;
                    
                default:
                    ctx.send(createErrorResponse("Unknown command: " + command));
            }
        } catch (Exception e) {
            System.err.println("[WebSocket] Error parsing message: " + e.getMessage());
            ctx.send(createErrorResponse("Invalid message format: " + e.getMessage()));
        }
    }
    
    /**
     * Handle fetchWeather command
     */
    private void handleFetchWeatherCommand(WsContext ctx, JsonObject json) {
        try {
            String city = json.get("city").getAsString();
            System.out.println("[ApiGateway] Fetching weather for: " + city);
            
            // Call external API
            ExternalApiClient.WeatherData weatherData = apiClient.fetchWeatherByCity(city);
            
            // Send response to dashboard using string (already formatted JSON from Gson)
            String response = gson.toJson(new Object() {
                public String type = "WEATHER_RESPONSE";
                public String location = weatherData.location;
                public double temperature = weatherData.temperature;
                public String condition = weatherData.condition;
                public long timestamp = weatherData.timestamp;
                public String status = "success";
            });
            
            ctx.send(response);
            System.out.println("[ApiGateway] ✓ Weather response sent to dashboard");
            
        } catch (Exception e) {
            System.err.println("[ApiGateway] Error fetching weather: " + e.getMessage());
            ctx.send(createErrorResponse("Failed to fetch weather: " + e.getMessage()));
        }
    }
    
    /**
     * Handle getServiceStatus command
     */
    private void handleGetServiceStatusCommand(WsContext ctx) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "SERVICE_STATUS");
        response.put("service", "ApiGateway");
        response.put("status", "online");
        response.put("port", 9001);
        response.put("connectedDashboards", sessions.size());
        response.put("timestamp", System.currentTimeMillis());
        
        ctx.send(gson.toJson(response));
    }
    
    /**
     * Broadcast service status to all connected dashboards
     */
    private void broadcastStatusUpdate() {
        Map<String, Object> statusUpdate = new LinkedHashMap<>();
        statusUpdate.put("type", "SERVICE_STATUS_UPDATE");
        statusUpdate.put("service", "ApiGateway");
        statusUpdate.put("status", "online");
        statusUpdate.put("connectedDashboards", sessions.size());
        statusUpdate.put("timestamp", System.currentTimeMillis());
        
        String message = gson.toJson(statusUpdate);
        sessions.forEach(session -> {
            try {
                session.send(message);
            } catch (Exception e) {
                System.err.println("[WebSocket] Error sending broadcast: " + e.getMessage());
            }
        });
    }
    
    /**
     * Create error response
     */
    private String createErrorResponse(String error) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "ERROR");
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }
    
    /**
     * Create pong response
     */
    private String createPongResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "PONG");
        response.put("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }
    
    /**
     * Start WebSocket server on port 9001
     */
    public void start() {
        try {
            System.out.println("[WebSocketServer] Starting on port 9001...");
            app.start(9001);
            System.out.println("[WebSocketServer] ✓ WebSocket server started successfully");
            System.out.println("[WebSocketServer] Dashboard WebSocket: ws://localhost:9001/api");
            System.out.println("[WebSocketServer] Health endpoint: http://localhost:9001/health");
        } catch (Exception e) {
            System.err.println("[WebSocketServer] Failed to start: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Stop WebSocket server
     */
    public void stop() {
        System.out.println("[WebSocketServer] Stopping WebSocket server...");
        app.stop();
        System.out.println("[WebSocketServer] ✓ WebSocket server stopped");
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return app != null;
    }
    
    /**
     * Get number of connected dashboards
     */
    public int getConnectedDashboardCount() {
        return sessions.size();
    }
}
