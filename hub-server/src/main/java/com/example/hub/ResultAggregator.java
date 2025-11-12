package com.example.hub;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Aggregates results from microservices and broadcasts them to the Dashboard.
 * 
 * Message Flow:
 * Service (TCP) → ResultAggregator → Dashboard (WebSocket)
 * 
 * Message Format from Service:
 * {
 *   "result_from": "API_GATEWAY",
 *   "data": "{temperature: 28.5, ...}"
 * }
 * 
 * Core Concept: Message Broker Pattern with Result Aggregation (Lesson 6 - Concurrency)
 * Uses WebSocketBroadcaster to send results to all connected dashboards.
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class ResultAggregator {
    private final WebSocketBroadcaster broadcaster;

    /**
     * Create a result aggregator
     * 
     * @param broadcaster WebSocket broadcaster for sending results to dashboards
     */
    public ResultAggregator(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    /**
     * Handle a result message from a service
     * 
     * Message format:
     * {
     *   "result_from": "SERVICE_NAME",
     *   "data": "result_data"
     * }
     * 
     * @param resultJson JSON message from service
     * @return true if result was processed successfully, false otherwise
     */
    public boolean handleServiceResult(String resultJson) {
        try {
            JsonObject jsonObject = JsonParser.parseString(resultJson).getAsJsonObject();
            
            String resultFrom = jsonObject.has("result_from") 
                    ? jsonObject.get("result_from").getAsString() 
                    : null;
            String data = jsonObject.has("data") 
                    ? jsonObject.get("data").getAsString() 
                    : null;

            if (resultFrom == null || data == null) {
                System.err.println("[RESULT_AGGREGATOR] Invalid result format: missing 'result_from' or 'data'");
                return false;
            }

            return broadcastResult(resultFrom, data);

        } catch (Exception e) {
            System.err.println("[RESULT_AGGREGATOR] Error parsing result: " + e.getMessage());
            return false;
        }
    }

    /**
     * Broadcast a service result to all connected dashboards
     * 
     * @param serviceName Name of the service that produced the result
     * @param data Result data to broadcast
     * @return true if broadcast was successful, false otherwise
     */
    public boolean broadcastResult(String serviceName, String data) {
        try {
            // Create WebSocket message for dashboard
            java.util.Map<String, Object> message = new java.util.LinkedHashMap<>();
            message.put("type", "SERVICE_RESULT");
            message.put("result_from", serviceName);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            // Convert to JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String json = gson.toJson(message);

            // Broadcast to all dashboards
            broadcaster.broadcast(json);
            
            System.out.println("[RESULT_AGGREGATOR] ✓ Broadcasted result from " + serviceName);
            return true;

        } catch (Exception e) {
            System.err.println("[RESULT_AGGREGATOR] Error broadcasting result from " + serviceName + 
                    ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a dashboard-ready message for a service result
     * 
     * @param serviceName Name of the service
     * @param data Result data
     * @return JSON message ready to send to dashboard
     */
    public String createResultMessage(String serviceName, String data) {
        java.util.Map<String, Object> message = new java.util.LinkedHashMap<>();
        message.put("type", "SERVICE_RESULT");
        message.put("result_from", serviceName);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());

        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.toJson(message);
    }
}
