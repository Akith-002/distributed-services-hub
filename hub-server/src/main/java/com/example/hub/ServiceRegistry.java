package com.example.hub;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe service registry using ConcurrentHashMap.
 * Manages registration, deregistration, and status of all services.
 * 
 * Core Concept: Thread-safe data structures (Lesson 6 - Concurrency)
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class ServiceRegistry {
    // Thread-safe map: Service name -> ServiceInfo
    private final ConcurrentHashMap<String, ServiceInfo> registry = new ConcurrentHashMap<>();
    
    // Listeners for registry changes
    private final List<RegistryChangeListener> listeners = new ArrayList<>();

    /**
     * Register a new service
     */
    public synchronized boolean register(String serviceName, String host, int port) {
        if (registry.containsKey(serviceName)) {
            System.out.println("[REGISTRY] Service '" + serviceName + "' already registered");
            return false;
        }

        ServiceInfo serviceInfo = new ServiceInfo(
                serviceName,
                host,
                port,
                "online",
                System.currentTimeMillis());

        registry.put(serviceName, serviceInfo);
        System.out.println("[REGISTRY] ✓ Service registered: " + serviceInfo);
        notifyListeners(RegistryChangeEvent.REGISTERED, serviceName, serviceInfo);
        return true;
    }

    /**
     * Update heartbeat for a service
     */
    public void heartbeat(String serviceName) {
        ServiceInfo current = registry.get(serviceName);
        if (current != null) {
            ServiceInfo updated = current.withHeartbeat(System.currentTimeMillis());
            registry.put(serviceName, updated);
            // Heartbeat logging is minimal to avoid spam
        }
    }

    /**
     * Deregister a service
     */
    public synchronized boolean deregister(String serviceName) {
        ServiceInfo removed = registry.remove(serviceName);
        if (removed != null) {
            System.out.println("[REGISTRY] ✗ Service deregistered: " + removed.getName());
            notifyListeners(RegistryChangeEvent.DEREGISTERED, serviceName, removed);
            return true;
        }
        return false;
    }

    /**
     * Mark service as offline due to timeout
     */
    public synchronized boolean timeout(String serviceName) {
        ServiceInfo current = registry.get(serviceName);
        if (current != null && current.isAlive(System.currentTimeMillis())) {
            ServiceInfo offlineInfo = current.offline();
            registry.put(serviceName, offlineInfo);
            System.out.println("[REGISTRY] ⏱ Service timeout: " + serviceName + " (no heartbeat for " +
                    current.getSecondsSinceHeartbeat(System.currentTimeMillis()) + "s)");
            notifyListeners(RegistryChangeEvent.TIMEOUT, serviceName, offlineInfo);
            return true;
        }
        return false;
    }

    /**
     * Get all registered services (snapshot)
     */
    public Collection<ServiceInfo> getAllServices() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Get all online services
     */
    public Collection<ServiceInfo> getOnlineServices() {
        return registry.values().stream()
                .filter(s -> "online".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific service by name
     */
    public ServiceInfo getService(String serviceName) {
        return registry.get(serviceName);
    }

    /**
     * Check if service exists
     */
    public boolean contains(String serviceName) {
        return registry.containsKey(serviceName);
    }

    /**
     * Get total number of registered services
     */
    public int getServiceCount() {
        return registry.size();
    }

    /**
     * Get count of online services
     */
    public int getOnlineServiceCount() {
        return (int) registry.values().stream()
                .filter(s -> "online".equals(s.getStatus()))
                .count();
    }

    /**
     * Get all services that have not sent heartbeat in more than 30 seconds
     */
    public List<String> getDeadServices() {
        long currentTime = System.currentTimeMillis();
        return registry.values().stream()
                .filter(s -> !s.isAlive(currentTime))
                .map(ServiceInfo::getName)
                .collect(Collectors.toList());
    }

    /**
     * Clear all services (for testing/shutdown)
     */
    public synchronized void clear() {
        registry.clear();
    }

    /**
     * Add a listener for registry changes
     */
    public void addListener(RegistryChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(RegistryChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of registry change
     */
    private void notifyListeners(RegistryChangeEvent event, String serviceName, ServiceInfo info) {
        for (RegistryChangeListener listener : listeners) {
            try {
                listener.onRegistryChange(event, serviceName, info);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    /**
     * Print registry status
     */
    public void printStatus() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SERVICE REGISTRY STATUS");
        System.out.println("=".repeat(60));
        System.out.println("Total Services: " + getServiceCount());
        System.out.println("Online Services: " + getOnlineServiceCount());
        System.out.println();

        if (registry.isEmpty()) {
            System.out.println("No services registered.");
        } else {
            registry.forEach((name, info) -> {
                System.out.println("  " + info);
            });
        }

        System.out.println("=".repeat(60));
        System.out.println();
    }

    /**
     * Get registry as JSON array string for WebSocket broadcast
     */
    public String toJsonArray() {
        List<String> jsonServices = registry.values().stream()
                .map(ServiceInfo::toJson)
                .collect(Collectors.toList());
        return "[" + String.join(",", jsonServices) + "]";
    }

    /**
     * Listener interface for registry changes
     */
    @FunctionalInterface
    public interface RegistryChangeListener {
        void onRegistryChange(RegistryChangeEvent event, String serviceName, ServiceInfo info);
    }

    /**
     * Registry change event types
     */
    public enum RegistryChangeEvent {
        REGISTERED("Service registered"),
        DEREGISTERED("Service deregistered"),
        TIMEOUT("Service timeout");

        private final String description;

        RegistryChangeEvent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
