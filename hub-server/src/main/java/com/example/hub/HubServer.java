package com.example.hub;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * Central Hub Server for Distributed Services.
 * Acts as a Service Registry accepting connections from microservices.
 * Provides WebSocket endpoint for React Dashboard to visualize services.
 * 
 * Architecture:
 * - TCP Server (port 7070): Accepts service REGISTER/HEARTBEAT/DEREGISTER messages
 * - WebSocket Server (Javalin 5070): Streams service updates to React Dashboard
 * - Service Registry: ConcurrentHashMap for thread-safe service management
 * - Heartbeat Monitor: ScheduledExecutorService checking for dead services
 * 
 * Core Concepts Demonstrated:
 * - Multithreading: Thread-per-client model with ExecutorService
 * - Concurrency: ConcurrentHashMap for thread-safe registry
 * - Scheduled Tasks: HeartbeatMonitor using ScheduledExecutorService
 * - WebSocket: Javalin for real-time dashboard updates
 * - SSL/TLS: Optional HTTPS support
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class HubServer {
    private static final int TCP_PORT = 7070;              // Service registry TCP port
    private static final int HTTP_PORT = 7071;             // HTTP/WebSocket port
    private static final int HTTPS_PORT = 7443;            // HTTPS/WebSocket port
    private static final int THREAD_POOL_SIZE = 20;        // Max concurrent service connections
    private static final boolean SSL_ENABLED = Boolean.parseBoolean(
            System.getProperty("ssl.enabled", "false"));

    private static ServiceRegistry registry;
    private static ServiceRegistryServer tcpServer;
    private static HeartbeatMonitor heartbeatMonitor;
    private static WebSocketBroadcaster broadcaster;
    private static Javalin app;

    public static void main(String[] args) {
        try {
            // Initialize components
            registry = new ServiceRegistry();
            broadcaster = new WebSocketBroadcaster();
            tcpServer = new ServiceRegistryServer(TCP_PORT, registry, broadcaster, THREAD_POOL_SIZE);
            heartbeatMonitor = new HeartbeatMonitor(registry);

            // Listen to registry changes and broadcast them
            registry.addListener((event, serviceName, info) -> {
                broadcaster.broadcastRegistry(registry);
            });

            // Start TCP server for service connections
            tcpServer.start();

            // Start heartbeat monitor
            heartbeatMonitor.start();

            // Start HTTP/WebSocket server for dashboard
            startWebSocketServer();

            // Print startup info
            printStartupInfo();

            // Add graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(HubServer::shutdown, "ShutdownHook"));

            // Keep running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start Hub Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Start the WebSocket/HTTP server using Javalin
     */
    private static void startWebSocketServer() {
        app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
            config.jsonMapper(new JavalinJackson());
        }).start(HTTP_PORT);

        // WebSocket endpoint for dashboard
        app.ws("/registry", ws -> {
            ws.onConnect(ctx -> {
                broadcaster.onConnect(ctx);
                // Send current registry state immediately
                broadcaster.broadcastRegistry(registry);
            });
            ws.onMessage(ctx -> broadcaster.onMessage(ctx, ctx.message()));
            ws.onClose(broadcaster::onClose);
            ws.onError(ctx -> broadcaster.onError(ctx, ctx.error()));
        });

        // REST endpoint for server status
        app.get("/hub-status", ctx -> {
            java.util.Map<String, Object> statusMap = new java.util.LinkedHashMap<>();
            statusMap.put("server", "Distributed Services Hub");
            statusMap.put("status", "Running");
            statusMap.put("tcpPort", TCP_PORT);
            statusMap.put("httpPort", HTTP_PORT);
            statusMap.put("sslEnabled", SSL_ENABLED);
            statusMap.put("totalServices", registry.getServiceCount());
            statusMap.put("onlineServices", registry.getOnlineServiceCount());
            statusMap.put("connectedDashboards", broadcaster.getConnectedDashboardCount());
            statusMap.put("activeConnections", tcpServer.getActiveThreadCount());
            statusMap.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            statusMap.put("timestamp", System.currentTimeMillis());
            ctx.json(statusMap);
        });

        // REST endpoint to get services list
        app.get("/services", ctx -> {
            java.util.Map<String, Object> servicesMap = new java.util.LinkedHashMap<>();
            servicesMap.put("services", registry.getAllServices().stream()
                    .map(s -> {
                        java.util.Map<String, Object> serviceMap = new java.util.LinkedHashMap<>();
                        serviceMap.put("name", s.getName());
                        serviceMap.put("host", s.getHost());
                        serviceMap.put("port", s.getPort());
                        serviceMap.put("status", s.getStatus());
                        serviceMap.put("registered", s.getRegistrationTime());
                        return serviceMap;
                    })
                    .toList());
            ctx.json(servicesMap);
        });

        System.out.println("[HTTP_SERVER] WebSocket server started on port " + HTTP_PORT);
        System.out.println("[HTTP_SERVER] WebSocket endpoint: ws://localhost:" + HTTP_PORT + "/registry");
        System.out.println("[HTTP_SERVER] Status endpoint: http://localhost:" + HTTP_PORT + "/hub-status");
        System.out.println("[HTTP_SERVER] Services endpoint: http://localhost:" + HTTP_PORT + "/services");
    }

    /**
     * Graceful shutdown
     */
    private static void shutdown() {
        System.out.println("\n[HUB] Shutdown initiated...");

        // Stop accepting new connections
        if (tcpServer != null) {
            tcpServer.stop();
        }

        // Stop heartbeat monitor
        if (heartbeatMonitor != null) {
            heartbeatMonitor.stop();
        }

        // Stop HTTP/WebSocket server
        if (app != null) {
            app.stop();
        }

        // Print final registry status
        if (registry != null) {
            registry.printStatus();
        }

        System.out.println("[HUB] Shutdown complete");
    }

    /**
     * Print startup information
     */
    private static void printStartupInfo() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  DISTRIBUTED SERVICES HUB - CENTRAL REGISTRY");
        System.out.println("  Member 1 - Multithreading & Concurrency Implementation");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("Core Concepts:");
        System.out.println("  ✓ Multithreading: Thread-per-client model with ExecutorService");
        System.out.println("  ✓ Concurrency: ConcurrentHashMap for thread-safe service registry");
        System.out.println("  ✓ Scheduled Tasks: ScheduledExecutorService for heartbeat monitoring");
        System.out.println("  ✓ WebSocket: Real-time updates to React Dashboard");
        System.out.println();
        System.out.println("Listening Ports:");
        System.out.println("  • TCP Service Registry: localhost:" + TCP_PORT);
        System.out.println("  • WebSocket (Dashboard): ws://localhost:" + HTTP_PORT + "/registry");
        System.out.println("  • Status API: http://localhost:" + HTTP_PORT + "/hub-status");
        System.out.println("  • Services API: http://localhost:" + HTTP_PORT + "/services");
        System.out.println();
        System.out.println("Service Protocol:");
        System.out.println("  REGISTER::ServiceName::Host::Port");
        System.out.println("  HEARTBEAT::ServiceName");
        System.out.println("  DEREGISTER::ServiceName");
        System.out.println("  FETCH_SERVICES");
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  • Thread Pool Size: " + THREAD_POOL_SIZE);
        System.out.println("  • Heartbeat Check Interval: 5 seconds");
        System.out.println("  • Service Timeout: 30 seconds");
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("[HUB] Ready to accept service connections");
        System.out.println("[HUB] Press Ctrl+C to shutdown");
        System.out.println("=".repeat(70));
        System.out.println();
    }
}
