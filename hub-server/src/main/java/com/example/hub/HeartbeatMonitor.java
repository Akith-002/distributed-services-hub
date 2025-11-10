package com.example.hub;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors service heartbeats and removes dead services.
 * Uses ScheduledExecutorService for periodic health checks.
 * 
 * Core Concept: ScheduledExecutorService for periodic tasks (Lesson 6)
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class HeartbeatMonitor {
    private static final long CHECK_INTERVAL_SECONDS = 5;  // Check every 5 seconds
    private static final long TIMEOUT_SECONDS = 30;        // Timeout after 30 seconds

    private final ServiceRegistry registry;
    private final ScheduledExecutorService scheduler;

    /**
     * Create a heartbeat monitor for the service registry
     */
    public HeartbeatMonitor(ServiceRegistry registry) {
        this.registry = registry;
        this.scheduler = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "HeartbeatMonitor");
            thread.setDaemon(false);
            return thread;
        });
    }

    /**
     * Start the heartbeat monitoring
     */
    public void start() {
        scheduler.scheduleAtFixedRate(
                this::checkHeartbeats,
                CHECK_INTERVAL_SECONDS,  // Initial delay
                CHECK_INTERVAL_SECONDS,  // Repeat interval
                TimeUnit.SECONDS
        );
        System.out.println("[HEARTBEAT] Monitor started - checking every " + 
                CHECK_INTERVAL_SECONDS + " seconds (timeout: " + TIMEOUT_SECONDS + "s)");
    }

    /**
     * Stop the heartbeat monitoring
     */
    public void stop() {
        System.out.println("[HEARTBEAT] Monitor stopping...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[HEARTBEAT] Monitor stopped");
    }

    /**
     * Check for dead services and remove them
     */
    private void checkHeartbeats() {
        try {
            // Get list of dead services
            List<String> deadServices = registry.getDeadServices();

            if (!deadServices.isEmpty()) {
                System.out.println("[HEARTBEAT] Detected " + deadServices.size() + " dead service(s)");

                for (String serviceName : deadServices) {
                    if (registry.timeout(serviceName)) {
                        // Service was removed due to timeout
                        // Registry will notify WebSocket broadcaster
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[HEARTBEAT] Error during heartbeat check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the scheduler (for testing)
     */
    protected ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
