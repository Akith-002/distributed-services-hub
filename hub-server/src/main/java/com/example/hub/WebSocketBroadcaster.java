package com.example.hub;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles WebSocket connections from React Dashboard.
 * Broadcasts service registry updates to all connected dashboards.
 * Also receives and routes commands from dashboard to services.
 * 
 * Message Flow:
 * 1. Dashboard → Hub (Commands): {"command_for": "SERVICE_NAME", "payload": "..."}
 * 2. Hub → Services (Routed Command)
 * 3. Service → Hub (Results): {"result_from": "SERVICE_NAME", "data": "..."}
 * 4. Hub → Dashboard (Broadcasted Result)
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class WebSocketBroadcaster {
    private static final List<WsContext> connectedDashboards = new CopyOnWriteArrayList<>();
    private static final Gson gson = new Gson();
    private CommandRouter commandRouter;
    private ResultAggregator resultAggregator;

    /**
     * Called when a dashboard connects
     */
    public void onConnect(WsContext ctx) {
        connectedDashboards.add(ctx);
        System.out.println("[WEBSOCKET] Dashboard connected. Total dashboards: " + connectedDashboards.size());
    }

    /**
     * Called when a dashboard disconnects
     */
    public void onClose(WsContext ctx) {
        connectedDashboards.remove(ctx);
        System.out.println("[WEBSOCKET] Dashboard disconnected. Total dashboards: " + connectedDashboards.size());
    }

    /**
     * Called when a dashboard sends a message
     */
    public void onMessage(WsContext ctx, String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = gson.fromJson(message, Map.class);
            String type = (String) msg.get("type");

            if ("COMMAND".equals(type)) {
                // Dashboard is sending a command to a service
                if (commandRouter != null) {
                    commandRouter.routeCommand(message);
                } else {
                    System.err.println("[WEBSOCKET] CommandRouter not initialized");
                }
            } else if ("FETCH_SERVICES".equals(type)) {
                // Dashboard requesting current service list
                System.out.println("[WEBSOCKET] Dashboard requested service list");
            } else {
                System.out.println("[WEBSOCKET] Received message from dashboard: " + message);
            }
        } catch (Exception e) {
            System.err.println("[WEBSOCKET] Error parsing dashboard message: " + e.getMessage());
        }
    }

    /**
     * Broadcast the current registry state to all connected dashboards
     */
    public void broadcastRegistry(ServiceRegistry registry) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "SERVICE_REGISTRY_UPDATE");

        Map<String, Object> payload = new LinkedHashMap<>();
        List<Map<String, Object>> services = new ArrayList<>();

        for (ServiceInfo service : registry.getAllServices()) {
            Map<String, Object> serviceMap = new LinkedHashMap<>();
            serviceMap.put("name", service.getName());
            serviceMap.put("host", service.getHost());
            serviceMap.put("port", service.getPort());
            serviceMap.put("status", service.getStatus());
            serviceMap.put("registered", service.getRegistrationTime());
            serviceMap.put("endpoint", service.getEndpoint());
            services.add(serviceMap);
        }

        payload.put("services", services);
        payload.put("totalServices", registry.getServiceCount());
        payload.put("onlineServices", registry.getOnlineServiceCount());
        payload.put("timestamp", System.currentTimeMillis());

        message.put("payload", payload);

        String json = gson.toJson(message);

        // Broadcast to all connected dashboards
        for (WsContext ctx : connectedDashboards) {
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(json);
                }
            } catch (Exception e) {
                System.err.println("[WEBSOCKET] Error sending to dashboard: " + e.getMessage());
            }
        }

        if (!connectedDashboards.isEmpty()) {
            System.out.println("[WEBSOCKET] Broadcasted registry update to " + 
                    connectedDashboards.size() + " dashboard(s)");
        }
    }

    /**
     * Send a custom message to all dashboards
     */
    public void broadcast(String message) {
        for (WsContext ctx : connectedDashboards) {
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(message);
                }
            } catch (Exception e) {
                System.err.println("[WEBSOCKET] Error broadcasting: " + e.getMessage());
            }
        }
    }

    /**
     * Get count of connected dashboards
     */
    public int getConnectedDashboardCount() {
        return connectedDashboards.size();
    }

    /**
     * Called on WebSocket error
     */
    public void onError(WsContext ctx, Throwable error) {
        System.err.println("[WEBSOCKET] Error: " + error.getMessage());
        error.printStackTrace();
    }

    /**
     * Set the command router for handling dashboard commands
     */
    public void setCommandRouter(CommandRouter router) {
        this.commandRouter = router;
    }

    /**
     * Set the result aggregator for broadcasting service results
     */
    public void setResultAggregator(ResultAggregator aggregator) {
        this.resultAggregator = aggregator;
    }
}
