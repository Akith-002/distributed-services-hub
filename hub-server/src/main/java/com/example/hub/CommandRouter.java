package com.example.hub;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.PrintWriter;
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
            JsonObject jsonObject = JsonParser.parseString(messageJson).getAsJsonObject();
            
            String commandFor = jsonObject.has("command_for") 
                    ? jsonObject.get("command_for").getAsString() 
                    : null;
            String payload = jsonObject.has("payload") 
                    ? jsonObject.get("payload").getAsString() 
                    : null;

            if (commandFor == null || payload == null) {
                System.err.println("[COMMAND_ROUTER] Invalid command format: missing 'command_for' or 'payload'");
                return false;
            }

            return routeCommandToService(commandFor, payload);

        } catch (Exception e) {
            System.err.println("[COMMAND_ROUTER] Error parsing command: " + e.getMessage());
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
        // Check if service exists in registry
        if (!registry.contains(serviceName)) {
            System.err.println("[COMMAND_ROUTER] Service not found: " + serviceName);
            return false;
        }

        // Get the service connection
        PrintWriter writer = serviceConnections.get(serviceName);
        if (writer == null) {
            System.err.println("[COMMAND_ROUTER] No connection to service: " + serviceName);
            return false;
        }

        try {
            // Send payload to service
            writer.println(payload);
            writer.flush();
            
            System.out.println("[COMMAND_ROUTER] ✓ Routed command to " + serviceName + 
                    ": " + payload);
            return true;

        } catch (Exception e) {
            System.err.println("[COMMAND_ROUTER] Error sending command to " + serviceName + 
                    ": " + e.getMessage());
            deregisterServiceConnection(serviceName);
            return false;
        }
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
