package com.example.logservice;

import java.io.*;
import java.net.Socket;

/**
 * Client to register the Log Service with the Hub Server
 * 
 * This sends a simple REGISTER message to the Hub so that:
 * 1. The Hub knows the Log Service is online
 * 2. The React Dashboard can display it as "Online"
 * 3. Other services can discover the log service port
 */
public class HubClient {

    private static final String HUB_HOST = "localhost";
    private static final int HUB_PORT = 8000;
    private static final int TIMEOUT = 5000; // 5 seconds

    /**
     * Register the Log Service with the Hub
     * 
     * @param serviceName Name of this service (e.g., "LogService")
     * @param servicePort Port where this service is listening (e.g., 9091)
     * @throws IOException if registration fails
     */
    public void register(String serviceName, int servicePort) throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            // Connect to Hub Server
            socket = new Socket(HUB_HOST, HUB_PORT);
            socket.setSoTimeout(TIMEOUT);

            // Setup I/O streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send registration message
            // Format: REGISTER::ServiceName::Port
            String registerMessage = String.format("REGISTER::%s::%d", serviceName, servicePort);
            out.println(registerMessage);

            // Wait for acknowledgment
            String response = in.readLine();

            if (response != null && response.contains("SUCCESS")) {
                System.out.println("✅ Registered with Hub Server: " + serviceName + " on port " + servicePort);
            } else {
                System.out.println("⚠️  Hub registration response: " + response);
            }

        } catch (IOException e) {
            throw new IOException("Failed to register with Hub Server at " +
                    HUB_HOST + ":" + HUB_PORT + " - " + e.getMessage());
        } finally {
            // Clean up resources
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        }
    }

    /**
     * Test method to verify Hub connection
     */
    public static void main(String[] args) {
        try {
            HubClient client = new HubClient();
            client.register("LogService", 9091);
            System.out.println("Registration test successful!");
        } catch (Exception e) {
            System.err.println("Registration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
