package com.example.taskservice.client;

import com.example.taskservice.TaskService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

/**
 * RMI Task Client
 * Used to test remote method invocation on TaskService
 */
public class TaskClient {
    
    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "TaskService";
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    RMI TASK CLIENT                              ");
        System.out.println("    Connecting to: rmi://" + RMI_HOST + ":" + RMI_PORT + "/" + SERVICE_NAME);
        System.out.println("=================================================\n");
        
        try {
            // Step 1: Locate the RMI registry
            System.out.println("[CLIENT] Looking up RMI registry at " + RMI_HOST + ":" + RMI_PORT + "...");
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            
            // Step 2: Lookup the remote service
            System.out.println("[CLIENT] Looking up TaskService...");
            TaskService taskService = (TaskService) registry.lookup(SERVICE_NAME);
            System.out.println("[CLIENT] TaskService found!\n");
            
            // Step 3: Test remote method calls
            System.out.println("========== TESTING REMOTE METHOD INVOCATION ==========\n");
            
            // Test 1: Get status
            System.out.println("1. Testing getStatus():");
            String status = taskService.getStatus();
            System.out.println("   Result: " + status + "\n");
            
            // Test 2: Get available tasks
            System.out.println("2. Testing getAvailableTasks():");
            List<String> tasks = taskService.getAvailableTasks();
            System.out.println("   Available tasks:");
            for (String task : tasks) {
                System.out.println("   - " + task);
            }
            System.out.println();
            
            // Test 3: Get CPU load
            System.out.println("3. Testing getCpuLoad():");
            int cpuLoad = taskService.getCpuLoad();
            System.out.println("   CPU Load: " + cpuLoad + "%\n");
            
            // Test 4: Execute tasks
            System.out.println("4. Testing executeTask() with different tasks:\n");
            
            String[] testTasks = {
                "calculate-pi",
                "fibonacci-10",
                "prime-check-1000"
            };
            
            for (String task : testTasks) {
                System.out.println("   Executing: " + task);
                String result = taskService.executeTask(task);
                System.out.println("   Result: " + result);
                System.out.println();
            }
            
            System.out.println("=================================================");
            System.out.println("    ALL REMOTE METHOD CALLS SUCCESSFUL!          ");
            System.out.println("=================================================\n");
            
            // Interactive mode
            interactiveMode(taskService);
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nMake sure:");
            System.err.println("1. RMI Task Service Server is running");
            System.err.println("2. RMI registry is available on port " + RMI_PORT);
            System.err.println("3. TaskService is bound in the registry");
        }
    }
    
    /**
     * Interactive mode - allows user to execute tasks manually
     */
    private static void interactiveMode(TaskService taskService) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n========== INTERACTIVE MODE ==========");
        System.out.println("Enter task name to execute (or 'quit' to exit):");
        System.out.println("Commands: list, status, cpu, quit, or any task name\n");
        
        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                if (input.equalsIgnoreCase("list")) {
                    List<String> tasks = taskService.getAvailableTasks();
                    System.out.println("Available tasks:");
                    for (String task : tasks) {
                        System.out.println("  - " + task);
                    }
                    continue;
                }
                
                if (input.equalsIgnoreCase("status")) {
                    String status = taskService.getStatus();
                    System.out.println(status);
                    continue;
                }
                
                if (input.equalsIgnoreCase("cpu")) {
                    int cpu = taskService.getCpuLoad();
                    System.out.println("CPU Load: " + cpu + "%");
                    continue;
                }
                
                if (!input.isEmpty()) {
                    String result = taskService.executeTask(input);
                    System.out.println("Result: " + result);
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}
