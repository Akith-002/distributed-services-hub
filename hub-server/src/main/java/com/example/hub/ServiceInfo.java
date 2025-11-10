package com.example.hub;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents information about a registered service.
 * This is a thread-safe, immutable data class.
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class ServiceInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String name;
    private final String host;
    private final int port;
    private final String status;
    private final long lastHeartbeat;
    private final String registrationTime;

    /**
     * Create a new service info
     * 
     * @param name Service name
     * @param host Service host
     * @param port Service port
     * @param status Service status (online/offline)
     * @param lastHeartbeat Timestamp of last heartbeat
     */
    public ServiceInfo(String name, String host, int port, String status, long lastHeartbeat) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
        this.registrationTime = LocalDateTime.now().format(formatter);
    }

    /**
     * Create a copy with updated status and heartbeat
     */
    public ServiceInfo withHeartbeat(long newHeartbeat) {
        return new ServiceInfo(this.name, this.host, this.port, "online", newHeartbeat);
    }

    /**
     * Create a copy with offline status
     */
    public ServiceInfo offline() {
        return new ServiceInfo(this.name, this.host, this.port, "offline", this.lastHeartbeat);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public String getEndpoint() {
        return host + ":" + port;
    }

    /**
     * Check if service is responsive (heartbeat within 30 seconds)
     */
    public boolean isAlive(long currentTime) {
        return (currentTime - lastHeartbeat) <= 30000; // 30 seconds
    }

    /**
     * Time since last heartbeat in seconds
     */
    public long getSecondsSinceHeartbeat(long currentTime) {
        return (currentTime - lastHeartbeat) / 1000;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s @ %s | Status: %s | Last HB: %ds ago",
                name, getEndpoint(), registrationTime, status,
                getSecondsSinceHeartbeat(System.currentTimeMillis()));
    }

    /**
     * Convert to JSON representation for WebSocket broadcast
     */
    public String toJson() {
        return String.format(
                "{\"name\":\"%s\",\"host\":\"%s\",\"port\":%d,\"status\":\"%s\",\"registered\":\"%s\"}",
                name, host, port, status, registrationTime);
    }
}
