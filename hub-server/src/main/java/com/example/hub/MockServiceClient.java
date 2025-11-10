package com.example.hub;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Mock Service Client for testing the Hub Server.
 * Simulates a microservice registering with the Hub and sending heartbeats.
 * 
 * Usage:
 *   java MockServiceClient <ServiceName> <Host> <Port>
 *   Example: java MockServiceClient "TestService" "localhost" "9001"
 * 
 * @author Member 1 - Hub Server Implementation (Testing)
 * @version 1.0
 */
public class MockServiceClient {
    private final String serviceName;
    private final String hubHost;
    private final int hubPort;
    private final int servicePort;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ScheduledExecutorService heartbeatScheduler;

    public MockServiceClient(String serviceName, String hubHost, int hubPort, int servicePort) {
        this.serviceName = serviceName;
        this.hubHost = hubHost;
        this.hubPort = hubPort;
        this.servicePort = servicePort;
    }

    /**
     * Connect to Hub and register
     */
    public void connect() throws IOException {
        socket = new Socket(hubHost, hubPort);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("[" + serviceName + "] Connected to Hub at " + hubHost + ":" + hubPort);

        // Send REGISTER message
        String registerMsg = "REGISTER::" + serviceName + "::" + hubHost + "::" + servicePort;
        writer.println(registerMsg);
        System.out.println("[" + serviceName + "] Sent: " + registerMsg);

        // Read response
        String response = reader.readLine();
        if (response != null) {
            System.out.println("[" + serviceName + "] Response: " + response);
        }

        // Start heartbeat scheduler
        startHeartbeat();
    }

    /**
     * Start sending heartbeats every 10 seconds
     */
    private void startHeartbeat() {
        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeat,
                10,  // Initial delay
                10,  // Repeat interval
                TimeUnit.SECONDS
        );
        System.out.println("[" + serviceName + "] Heartbeat scheduler started");
    }

    /**
     * Send heartbeat to Hub
     */
    private void sendHeartbeat() {
        try {
            String heartbeatMsg = "HEARTBEAT::" + serviceName;
            writer.println(heartbeatMsg);
            System.out.println("[" + serviceName + "] Heartbeat sent");

            String response = reader.readLine();
            if (response != null && response.startsWith("OK")) {
                System.out.println("[" + serviceName + "] Heartbeat acknowledged");
            }
        } catch (Exception e) {
            System.err.println("[" + serviceName + "] Error sending heartbeat: " + e.getMessage());
        }
    }

    /**
     * Deregister and disconnect (graceful)
     */
    public void disconnect() {
        try {
            // Stop heartbeat
            if (heartbeatScheduler != null) {
                heartbeatScheduler.shutdown();
                heartbeatScheduler.awaitTermination(2, TimeUnit.SECONDS);
            }

            // Send DEREGISTER
            String deregisterMsg = "DEREGISTER::" + serviceName;
            writer.println(deregisterMsg);
            System.out.println("[" + serviceName + "] Sent: " + deregisterMsg);

            String response = reader.readLine();
            if (response != null) {
                System.out.println("[" + serviceName + "] Response: " + response);
            }

            // Close connection
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("[" + serviceName + "] Disconnected");
        } catch (Exception e) {
            System.err.println("[" + serviceName + "] Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Crash without sending DEREGISTER (for testing timeout behavior)
     * Useful for Scenario 2: Service Timeout test
     */
    public void crash() {
        try {
            System.out.println("[" + serviceName + "] CRASH MODE: Simulating service crash (no graceful shutdown)");
            
            // Stop heartbeat
            if (heartbeatScheduler != null) {
                heartbeatScheduler.shutdown();
            }

            // Force close socket without sending DEREGISTER
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[" + serviceName + "] Socket forcefully closed - Hub will detect timeout after 30s");
            }
        } catch (Exception e) {
            System.err.println("[" + serviceName + "] Error during crash: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java MockServiceClient <ServiceName> <ServicePort> [HubHost] [HubPort] [--crash]");
            System.err.println("Example: java MockServiceClient TestService 9001 localhost 7070");
            System.err.println("Example (Crash Mode): java MockServiceClient TestService 9001 localhost 7070 --crash");
            System.exit(1);
        }

        String serviceName = args[0];
        int servicePort = Integer.parseInt(args[1]);
        String hubHost = args.length > 2 ? args[2] : "localhost";
        int hubPort = args.length > 3 ? Integer.parseInt(args[3]) : 7070;
        boolean crashMode = args.length > 4 && "--crash".equals(args[4]);

        MockServiceClient client = new MockServiceClient(serviceName, hubHost, hubPort, servicePort);

        try {
            client.connect();

            if (crashMode) {
                System.out.println("[" + serviceName + "] Running in CRASH MODE... Service will crash in 15 seconds");
                System.out.println("[" + serviceName + "] Hub will detect timeout after 30 seconds total");
                
                // Run for 15 seconds then crash
                Thread.sleep(15000);
                client.crash();
                System.out.println("[" + serviceName + "] Crashed - process still running to keep JVM alive");
                // Keep JVM alive so Hub can detect timeout
                Thread.currentThread().join();
            } else {
                System.out.println("[" + serviceName + "] Running... Press Ctrl+C to stop");

                // Keep running
                Thread.currentThread().join();
            }

        } catch (Exception e) {
            System.err.println("[" + serviceName + "] Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!crashMode) {
                client.disconnect();
            }
        }
    }
}
