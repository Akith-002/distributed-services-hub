package com.example.logservice;

import java.io.*;
import java.net.Socket;

/**
 * Utility Client for Other Services to Send Logs
 * 
 * OTHER TEAM MEMBERS: Copy this file to your project!
 * 
 * Usage:
 * LogServiceClient.sendLog("MyService", "Something happened");
 * LogServiceClient.info("MyService", "Informational message");
 * LogServiceClient.error("MyService", "Error occurred");
 */
public class LogServiceClient {

    private static final String LOG_HOST = "localhost";
    private static final int LOG_PORT = 9091;
    private static final int TIMEOUT = 1000; // 1 second

    /**
     * Send a log message to the Log Service
     * 
     * @param serviceName Name of your service (e.g., "HubServer", "FileService")
     * @param message     The log message
     */
    public static void sendLog(String serviceName, String message) {
        Socket socket = null;
        PrintWriter out = null;

        try {
            // Connect to Log Service
            socket = new Socket(LOG_HOST, LOG_PORT);
            socket.setSoTimeout(TIMEOUT);

            // Send the message
            out = new PrintWriter(socket.getOutputStream(), true);
            String logMessage = String.format("[%s] %s", serviceName, message);
            out.println(logMessage);

        } catch (IOException e) {
            // Fail silently - logging should never crash your service!
            // You can uncomment this line for debugging:
            // System.err.println("Log Service unavailable: " + e.getMessage());
        } finally {
            // Clean up
            try {
                if (out != null)
                    out.close();
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Send an INFO level log message
     * Will display with ℹ️ icon in Log Service
     */
    public static void info(String serviceName, String message) {
        sendLog(serviceName, "INFO: " + message);
    }

    /**
     * Send a SUCCESS level log message
     * Will display with ✅ icon in Log Service
     */
    public static void success(String serviceName, String message) {
        sendLog(serviceName, "SUCCESS: " + message);
    }

    /**
     * Send a WARNING level log message
     * Will display with ⚠️ icon in Log Service
     */
    public static void warn(String serviceName, String message) {
        sendLog(serviceName, "WARN: " + message);
    }

    /**
     * Send an ERROR level log message
     * Will display with ❌ icon in Log Service
     */
    public static void error(String serviceName, String message) {
        sendLog(serviceName, "ERROR: " + message);
    }

    /**
     * Test if Log Service is available
     * 
     * @return true if Log Service is reachable
     */
    public static boolean isAvailable() {
        try (Socket socket = new Socket(LOG_HOST, LOG_PORT)) {
            socket.setSoTimeout(TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Example usage and testing
     */
    public static void main(String[] args) {
        System.out.println("Testing Log Service Client...\n");

        // Check availability
        if (isAvailable()) {
            System.out.println("✅ Log Service is available");
        } else {
            System.out.println("❌ Log Service is not available");
            System.out.println("Make sure the Log Service is running on port 9091");
            return;
        }

        // Send test messages
        System.out.println("\nSending test messages...");

        sendLog("TestService", "This is a basic log message");
        info("TestService", "This is an info message");
        success("TestService", "This is a success message");
        warn("TestService", "This is a warning message");
        error("TestService", "This is an error message");

        System.out.println("\n✅ Test messages sent!");
        System.out.println("Check the Log Service console to see them.");
    }
}
