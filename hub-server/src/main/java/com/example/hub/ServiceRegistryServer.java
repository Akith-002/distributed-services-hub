package com.example.hub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP Server that accepts connections from microservices.
 * Uses ExecutorService for thread pool management.
 * 
 * Core Concept: Thread pool with ExecutorService (Lesson 6)
 * Handles multiple concurrent service connections efficiently.
 * 
 * @author Member 1 - Hub Server Implementation
 * @version 1.0
 */
public class ServiceRegistryServer implements Runnable {
    private final int port;
    private final ServiceRegistry registry;
    private final WebSocketBroadcaster broadcaster;
    private final CommandRouter commandRouter;
    private final ResultAggregator resultAggregator;
    private final int threadPoolSize;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    /**
     * Create a service registry server
     * 
     * @param port Port to listen on
     * @param registry Service registry
     * @param broadcaster WebSocket broadcaster
     * @param commandRouter Command router for service commands
     * @param resultAggregator Result aggregator for service results
     * @param threadPoolSize Number of threads in thread pool
     */
    public ServiceRegistryServer(int port, ServiceRegistry registry, 
                                WebSocketBroadcaster broadcaster,
                                CommandRouter commandRouter,
                                ResultAggregator resultAggregator,
                                int threadPoolSize) {
        this.port = port;
        this.registry = registry;
        this.broadcaster = broadcaster;
        this.commandRouter = commandRouter;
        this.resultAggregator = resultAggregator;
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Start the TCP server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(threadPoolSize, runnable -> {
            Thread thread = new Thread(runnable, "ServiceHandler-" + Thread.currentThread().getId());
            thread.setDaemon(false);
            return thread;
        });

        running = true;
        System.out.println("[TCP_SERVER] Started on port " + port + 
                " with thread pool size " + threadPoolSize);

        // Start accepting connections in a separate thread
        new Thread(this, "ServiceRegistryServer").start();
    }

    /**
     * Accept incoming service connections
     */
    @Override
    public void run() {
        System.out.println("[TCP_SERVER] Listening for service connections on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();

                System.out.println("[TCP_SERVER] New connection from " + 
                        clientAddress + ":" + clientPort);

                // Handle connection in thread pool
                ServiceRegistryHandler handler = new ServiceRegistryHandler(
                        clientSocket, registry, broadcaster, commandRouter, resultAggregator);
                threadPool.execute(handler);

            } catch (IOException e) {
                if (running) {
                    System.err.println("[TCP_SERVER] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;
        System.out.println("[TCP_SERVER] Stopping...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[TCP_SERVER] Error closing socket: " + e.getMessage());
        }

        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("[TCP_SERVER] Stopped");
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get active thread count
     */
    public int getActiveThreadCount() {
        return threadPool != null ? ((java.util.concurrent.ThreadPoolExecutor) threadPool).getActiveCount() : 0;
    }
}
