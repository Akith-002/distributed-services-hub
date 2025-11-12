package com.example.taskservice;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Task Service Server
 * Member 5's Core Concept: Java RMI (Remote Method Invocation)
 * 
 * This server:
 * 1. Starts an RMI registry on port 1099
 * 2. Creates and binds the TaskService remote object
 * 3. Registers with Hub for service discovery
 * 4. Keeps running to serve remote method calls
 */
public class TaskServiceServer {
    
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "TaskService";
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    RMI TASK SERVICE - Starting...              ");
        System.out.println("    Port: 1099 (RMI Registry)                    ");
        System.out.println("    Core Concept: Java RMI                       ");
        System.out.println("=================================================");
        
        HubClient hubClient = new HubClient();
        
        try {
            // Step 1: Create RMI registry on port 1099
            System.out.println("[RMI_SERVICE] Creating RMI registry on port " + RMI_PORT + "...");
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[RMI_SERVICE] RMI registry created successfully");
            
            // Step 2: Create and export the remote object
            System.out.println("[RMI_SERVICE] Creating TaskService remote object...");
            TaskService taskService = new TaskServiceImpl();
            System.out.println("[RMI_SERVICE] TaskService object created and exported");
            
            // Step 3: Bind the remote object in the registry
            System.out.println("[RMI_SERVICE] Binding TaskService to registry...");
            registry.rebind(SERVICE_NAME, taskService);
            System.out.println("[RMI_SERVICE] TaskService bound successfully");
            System.out.println("[RMI_SERVICE] Service available at: rmi://localhost:" + RMI_PORT + "/" + SERVICE_NAME);
            
            // Step 4: Register with Hub
            System.out.println("[RMI_SERVICE] Registering with Hub...");
            hubClient.register();
            
            // Step 5: Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[RMI_SERVICE] Shutting down...");
                hubClient.deregister();
                System.out.println("[RMI_SERVICE] Shutdown complete");
            }));
            
            System.out.println("\n=================================================");
            System.out.println("    RMI TASK SERVICE - READY                     ");
            System.out.println("    Waiting for remote method invocations...     ");
            System.out.println("    Press Ctrl+C to stop                         ");
            System.out.println("=================================================\n");
            
            // Keep the server running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("[RMI_SERVICE] Server error: " + e.getMessage());
            e.printStackTrace();
            hubClient.deregister();
        }
    }
}
