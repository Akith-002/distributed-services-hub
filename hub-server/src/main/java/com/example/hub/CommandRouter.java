package com.example.hub;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes commands from React Dashboard to appropriate microservices.
 * 
 * Message Flow:
 * Dashboard (WebSocket) → CommandRouter → Service (TCP)
 * 
 * Message Format from Dashboard:
 * {
 *   "command_for": "API_GATEWAY",
 *   "payload": "get-weather"
 * }
 * 
 * Core Concept: Message Broker Pattern with Command Routing (Lesson 6 - Concurrency)
 * Uses thread-safe ConcurrentHashMap to store service connections.
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class CommandRouter {
    // Service name -> PrintWriter for sending commands
    private final ConcurrentHashMap<String, PrintWriter> serviceConnections = new ConcurrentHashMap<>();
    private final ServiceRegistry registry;

    /**
     * Create a command router
     * 
     * @param registry Service registry
     */
    public CommandRouter(ServiceRegistry registry) {
        this.registry = registry;
    }

    /**
     * Register a service connection for command routing
     * Called when a service successfully registers with the Hub.
     * 
     * @param serviceName Name of the service
     * @param writer PrintWriter for sending commands to the service
     */
    public void registerServiceConnection(String serviceName, PrintWriter writer) {
        serviceConnections.put(serviceName, writer);
        System.out.println("[COMMAND_ROUTER] Registered command route to: " + serviceName);
    }

    /**
     * Deregister a service connection
     * Called when a service disconnects or times out.
     * 
     * @param serviceName Name of the service
     */
    public void deregisterServiceConnection(String serviceName) {
        serviceConnections.remove(serviceName);
        System.out.println("[COMMAND_ROUTER] Deregistered command route from: " + serviceName);
    }

    /**
     * Route a command from the Dashboard to a service
     * 
     * Message format:
     * {
     *   "command_for": "SERVICE_NAME",
     *   "payload": "command_data"
     * }
     * 
     * @param messageJson JSON message from Dashboard
     * @return true if command was routed successfully, false otherwise
     */
    public boolean routeCommand(String messageJson) {
        try {
            System.out.println("[COMMAND_ROUTER] Received raw message: " + messageJson);
            JsonObject jsonObject = JsonParser.parseString(messageJson).getAsJsonObject();
            System.out.println("[COMMAND_ROUTER] DEBUG - Parsed JSON object: " + jsonObject);
            System.out.println("[COMMAND_ROUTER] DEBUG - Has command_for: " + jsonObject.has("command_for"));
            System.out.println("[COMMAND_ROUTER] DEBUG - Has payload: " + jsonObject.has("payload"));
            
            String commandFor = jsonObject.has("command_for") 
                    ? jsonObject.get("command_for").getAsString() 
                    : null;
            String payload = jsonObject.has("payload") 
                    ? jsonObject.get("payload").getAsString() 
                    : null;

            System.out.println("[COMMAND_ROUTER] DEBUG - Extracted commandFor: '" + commandFor + "' (null=" + (commandFor==null) + ")");
            System.out.println("[COMMAND_ROUTER] DEBUG - Extracted payload: '" + payload + "' (null=" + (payload==null) + ")");

            if (commandFor == null || payload == null) {
                System.err.println("[COMMAND_ROUTER] Invalid command format: missing 'command_for' or 'payload'");
                return false;
            }

            return routeCommandToService(commandFor, payload);

        } catch (Exception e) {
            System.err.println("[COMMAND_ROUTER] Error parsing command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Route a command to a specific service
     * 
     * @param serviceName Name of the service
     * @param payload Command payload
     * @return true if routed successfully, false otherwise
     */
    public boolean routeCommandToService(String serviceName, String payload) {
        // Debug: List all registered services
        System.out.println("[COMMAND_ROUTER] DEBUG - Looking for service: " + serviceName);
        System.out.println("[COMMAND_ROUTER] DEBUG - serviceName length: " + serviceName.length() + ", bytes: " + java.util.Arrays.toString(serviceName.getBytes()));
        System.out.println("[COMMAND_ROUTER] DEBUG - Registered services in REGISTRY: " + 
            registry.getAllServices().stream().map(ServiceInfo::getName).toList());
        System.out.println("[COMMAND_ROUTER] DEBUG - Registered services in CONNECTIONS: " + 
            new java.util.ArrayList<>(serviceConnections.keySet()));
        
        // Try to find service with case-insensitive match
        ServiceInfo targetService = null;
        for (ServiceInfo service : registry.getAllServices()) {
            String registeredName = service.getName();
            System.out.println("[COMMAND_ROUTER] DEBUG - registeredName: '" + registeredName + "' (length=" + registeredName.length() + ")");
            System.out.println("[COMMAND_ROUTER] DEBUG - registeredName bytes: " + java.util.Arrays.toString(registeredName.getBytes()));
            System.out.println("[COMMAND_ROUTER] DEBUG - serviceName: '" + serviceName + "' (length=" + serviceName.length() + ")");
            System.out.println("[COMMAND_ROUTER] DEBUG - serviceName bytes: " + java.util.Arrays.toString(serviceName.getBytes()));
            
            // Try multiple comparison methods
            boolean matches1 = registeredName.equalsIgnoreCase(serviceName);
            boolean matches2 = registeredName.equals(serviceName);
            boolean matches3 = registeredName.toUpperCase().equals(serviceName.toUpperCase());
            boolean matches4 = registeredName.trim().equalsIgnoreCase(serviceName.trim());
            
            System.out.println("[COMMAND_ROUTER] DEBUG - equalsIgnoreCase: " + matches1);
            System.out.println("[COMMAND_ROUTER] DEBUG - equals: " + matches2);
            System.out.println("[COMMAND_ROUTER] DEBUG - toUpperCase: " + matches3);
            System.out.println("[COMMAND_ROUTER] DEBUG - trim+equalsIgnoreCase: " + matches4);
            
            if (registeredName.equalsIgnoreCase(serviceName)) {
                targetService = service;
                System.out.println("[COMMAND_ROUTER] DEBUG - ✓ Found match in registry!");
                break;
            }
        }
        
        if (targetService == null) {
            System.err.println("[COMMAND_ROUTER] Service not found: " + serviceName);
            return false;
        }

        // Get the service connection using the actual service name
        PrintWriter writer = serviceConnections.get(targetService.getName());
        if (writer == null) {
            System.err.println("[COMMAND_ROUTER] No connection to service: " + targetService.getName());
            return false;
        }

        try {
            // Format payload for service
            // Convert Dashboard format: "get-weather" → Service format: {"command": "fetchWeather", "payload": {...}}
            String commandToSend = formatCommandForService(targetService.getName(), payload);
            
            // Send formatted command to service
            writer.println(commandToSend);
            writer.flush();
            
            System.out.println("[COMMAND_ROUTER] ✓ Routed command to " + targetService.getName() + 
                    ": " + commandToSend);
            return true;

        } catch (Exception e) {
            System.err.println("[COMMAND_ROUTER] Error sending command to " + targetService.getName() + 
                    ": " + e.getMessage());
            deregisterServiceConnection(targetService.getName());
            return false;
        }
    }

    /**
     * Format a command for the service listener
     * Converts dashboard payload into service-specific command format
     */
    private String formatCommandForService(String serviceName, String payload) {
        Gson gson = new Gson();
        Map<String, Object> command = new LinkedHashMap<>();
        
        // Route command based on service name
        if ("API_GATEWAY".equalsIgnoreCase(serviceName)) {
            // For API Gateway: "get-weather" → {"command": "fetchWeather", "payload": {...}}
            if (payload.contains("weather")) {
                command.put("command", "fetchWeather");
                Map<String, Object> payloadObj = new LinkedHashMap<>();
                payloadObj.put("city", "Colombo"); // Default city - can be enhanced later
                command.put("payload", payloadObj);
            } else if (payload.contains("status")) {
                command.put("command", "getServiceStatus");
                command.put("payload", new LinkedHashMap<>());
            }
        } else {
            // Generic format for other services
            command.put("command", payload);
            command.put("payload", new LinkedHashMap<>());
        }
        
        return gson.toJson(command);
    }

    /**
     * Check if a service connection is available
     * 
     * @param serviceName Name of the service
     * @return true if service is registered and connected, false otherwise
     */
    public boolean isServiceAvailable(String serviceName) {
        return registry.contains(serviceName) && serviceConnections.containsKey(serviceName);
    }

    /**
     * Get count of available service routes
     */
    public int getRouteCount() {
        return serviceConnections.size();
    }

    /**
     * Get all service names that have active connections
     */
    public java.util.List<String> getAvailableServices() {
        return new java.util.ArrayList<>(serviceConnections.keySet());
    }

    /**
     * Clear all service connections (for testing)
     */
    protected void clear() {
        serviceConnections.clear();
    }
}
