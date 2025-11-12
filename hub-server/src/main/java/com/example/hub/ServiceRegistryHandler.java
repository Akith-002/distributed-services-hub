package com.example.hub;

import java.io.*;
import java.net.Socket;

/**
 * Handles individual TCP connections from services.
 * Processes REGISTER, HEARTBEAT, DEREGISTER messages.
 * Also routes commands TO services and receives results FROM services.
 * 
 * Core Concept: Thread-per-client model (Lesson 3 & 6)
 * Each service connection runs in its own thread via ExecutorService.
 * 
 * Bidirectional Communication:
 * 1. Receive: REGISTER, HEARTBEAT, DEREGISTER (service protocol messages)
 * 2. Send: Commands routed from Dashboard (command router)
 * 3. Receive: Results from services (result aggregator)
 * 4. Send: Responses/Confirmations
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class ServiceRegistryHandler implements Runnable {
    private final Socket socket;
    private final ServiceRegistry registry;
    private final WebSocketBroadcaster broadcaster;
    private final CommandRouter commandRouter;
    private final ResultAggregator resultAggregator;
    private String serviceName;
    private PrintWriter writer;

    /**
     * Create a new service connection handler
     * 
     * @param socket Connected socket from service
     * @param registry Service registry
     * @param broadcaster WebSocket broadcaster for dashboard updates
     * @param commandRouter Command router for routing commands to services
     * @param resultAggregator Result aggregator for broadcasting service results
     */
    public ServiceRegistryHandler(Socket socket, ServiceRegistry registry, 
                                 WebSocketBroadcaster broadcaster,
                                 CommandRouter commandRouter,
                                 ResultAggregator resultAggregator) {
        this.socket = socket;
        this.registry = registry;
        this.broadcaster = broadcaster;
        this.commandRouter = commandRouter;
        this.resultAggregator = resultAggregator;
    }

    /**
     * Handle the service connection
     */
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = reader.readLine()) != null) {
                handleMessage(message, writer);
            }

        } catch (IOException e) {
            if (serviceName != null) {
                System.err.println("[HANDLER] Connection error for " + serviceName + ": " + e.getMessage());
            }
        } finally {
            // Cleanup on disconnect
            if (serviceName != null) {
                if (registry.contains(serviceName)) {
                    registry.deregister(serviceName);
                    broadcaster.broadcastRegistry(registry);
                }
                // Deregister command route
                commandRouter.deregisterServiceConnection(serviceName);
            }

            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Process incoming message
     * Protocol: TYPE::ServiceName::Host::Port or TYPE::ServiceName
     */
    private void handleMessage(String message, PrintWriter writer) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String[] parts = message.split("::");
        if (parts.length < 2) {
            sendError(writer, "Invalid message format");
            return;
        }

        String messageType = parts[0].trim();
        String incomingServiceName = parts[1].trim();

        try {
            switch (messageType) {
                case "REGISTER":
                    handleRegister(incomingServiceName, parts, writer);
                    break;

                case "HEARTBEAT":
                    handleHeartbeat(incomingServiceName, writer);
                    break;

                case "DEREGISTER":
                    handleDeregister(incomingServiceName, writer);
                    break;

                case "FETCH_SERVICES":
                    handleFetchServices(writer);
                    break;

                default:
                    sendError(writer, "Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            System.err.println("[HANDLER] Error processing message: " + e.getMessage());
            sendError(writer, "Server error: " + e.getMessage());
        }
    }

    /**
     * Handle REGISTER message
     * Format: REGISTER::ServiceName::Host::Port
     */
    private void handleRegister(String incomingServiceName, String[] parts, PrintWriter writer) {
        if (parts.length < 4) {
            sendError(writer, "REGISTER requires: REGISTER::ServiceName::Host::Port");
            return;
        }

        String host = parts[2].trim();
        int port;

        try {
            port = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            sendError(writer, "Invalid port number: " + parts[3]);
            return;
        }

        // Register the service
        boolean success = registry.register(incomingServiceName, host, port);

        if (success) {
            this.serviceName = incomingServiceName;
            
            // Register command route for this service
            commandRouter.registerServiceConnection(incomingServiceName, writer);
            
            sendOk(writer, "Service registered successfully");

            // Broadcast updated registry to all dashboards
            broadcaster.broadcastRegistry(registry);
        } else {
            sendError(writer, "Service name already registered");
        }
    }

    /**
     * Handle HEARTBEAT message
     * Format: HEARTBEAT::ServiceName
     */
    private void handleHeartbeat(String incomingServiceName, PrintWriter writer) {
        if (!registry.contains(incomingServiceName)) {
            sendError(writer, "Service not registered: " + incomingServiceName);
            return;
        }

        registry.heartbeat(incomingServiceName);
        ServiceInfo service = registry.getService(incomingServiceName);
        if (service != null) {
            System.out.println("[HEARTBEAT] ♥ Acknowledged: " + incomingServiceName + " @ " + service.getLastHeartbeat());
        }
        sendOk(writer, "Heartbeat received");
    }

    /**
     * Handle DEREGISTER message
     * Format: DEREGISTER::ServiceName
     */
    private void handleDeregister(String incomingServiceName, PrintWriter writer) {
        boolean success = registry.deregister(incomingServiceName);

        if (success) {
            this.serviceName = null;  // Clear to prevent double deregister
            sendOk(writer, "Service deregistered successfully");

            // Broadcast updated registry
            broadcaster.broadcastRegistry(registry);
        } else {
            sendError(writer, "Service not found: " + incomingServiceName);
        }
    }

    /**
     * Handle FETCH_SERVICES message
     * Returns all registered services as JSON
     */
    private void handleFetchServices(PrintWriter writer) {
        String jsonArray = registry.toJsonArray();
        writer.println("OK::" + jsonArray);
    }

    /**
     * Send OK response
     */
    private void sendOk(PrintWriter writer, String message) {
        writer.println("OK::" + message);
    }

    /**
     * Send error response
     */
    private void sendError(PrintWriter writer, String errorMessage) {
        writer.println("ERROR::" + errorMessage);
    }
}
