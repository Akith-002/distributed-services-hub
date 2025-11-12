package com.example.taskservice;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;

/**
 * TaskService Implementation
 * CORE CONCEPT: UnicastRemoteObject for RMI
 * 
 * Extends UnicastRemoteObject to enable remote method invocation.
 * This class implements the actual task execution logic.
 */
public class TaskServiceImpl extends UnicastRemoteObject implements TaskService {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor - must throw RemoteException
     * Calls super() to export this object to make it available for remote calls
     */
    public TaskServiceImpl() throws RemoteException {
        super();
        System.out.println("[RMI_SERVICE] TaskServiceImpl initialized");
    }
    
    @Override
    public String executeTask(String taskName) throws RemoteException {
        System.out.println("[RMI_SERVICE] Remote method invoked: executeTask(\"" + taskName + "\")");
        
        try {
            switch (taskName.toLowerCase()) {
                case "calculate-pi":
                    return calculatePi();
                    
                case "fibonacci-10":
                    return calculateFibonacci(10);
                    
                case "fibonacci-20":
                    return calculateFibonacci(20);
                    
                case "matrix-multiply":
                    return performMatrixMultiply();
                    
                case "prime-check-1000":
                    return checkPrime(1000);
                    
                case "prime-check-10007":
                    return checkPrime(10007);
                    
                case "factorial-10":
                    return calculateFactorial(10);
                    
                case "factorial-20":
                    return calculateFactorial(20);
                    
                default:
                    return "Unknown task: " + taskName + ". Available tasks: " + 
                           String.join(", ", getAvailableTasks());
            }
        } catch (Exception e) {
            String error = "Task execution failed: " + e.getMessage();
            System.err.println("[RMI_SERVICE] " + error);
            return error;
        }
    }
    
    @Override
    public int getCpuLoad() throws RemoteException {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double load = osBean.getSystemLoadAverage();
            int cpuCount = osBean.getAvailableProcessors();
            
            if (load < 0) {
                return 0; // Not available on this system
            }
            
            int percentage = (int) ((load / cpuCount) * 100);
            return Math.min(100, Math.max(0, percentage));
        } catch (Exception e) {
            System.err.println("[RMI_SERVICE] Error getting CPU load: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public String getStatus() throws RemoteException {
        return "RMI Task Service: RUNNING - Ready to execute remote tasks";
    }
    
    @Override
    public List<String> getAvailableTasks() throws RemoteException {
        return Arrays.asList(
            "calculate-pi",
            "fibonacci-10",
            "fibonacci-20",
            "matrix-multiply",
            "prime-check-1000",
            "prime-check-10007",
            "factorial-10",
            "factorial-20"
        );
    }
    
    // ===== Task Implementation Methods =====
    
    private String calculatePi() {
        System.out.println("[RMI_SERVICE] Calculating Pi using Leibniz formula...");
        long start = System.currentTimeMillis();
        
        double pi = 0.0;
        int iterations = 1000000;
        
        for (int i = 0; i < iterations; i++) {
            pi += Math.pow(-1, i) / (2 * i + 1);
        }
        pi *= 4;
        
        long duration = System.currentTimeMillis() - start;
        
        String result = String.format("Pi ≈ %.10f (calculated in %dms using %d iterations)", 
                                     pi, duration, iterations);
        System.out.println("[RMI_SERVICE] " + result);
        return result;
    }
    
    private String calculateFibonacci(int n) {
        System.out.println("[RMI_SERVICE] Calculating Fibonacci(" + n + ")...");
        long start = System.currentTimeMillis();
        
        long result = fibonacci(n);
        
        long duration = System.currentTimeMillis() - start;
        
        String output = String.format("Fibonacci(%d) = %d (calculated in %dms)", 
                                     n, result, duration);
        System.out.println("[RMI_SERVICE] " + output);
        return output;
    }
    
    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
    
    private String performMatrixMultiply() {
        System.out.println("[RMI_SERVICE] Performing 100x100 matrix multiplication...");
        long start = System.currentTimeMillis();
        
        int size = 100;
        int[][] a = new int[size][size];
        int[][] b = new int[size][size];
        int[][] c = new int[size][size];
        
        // Initialize matrices
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                a[i][j] = i + j;
                b[i][j] = i - j;
            }
        }
        
        // Multiply
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        
        long duration = System.currentTimeMillis() - start;
        
        String result = String.format("Matrix multiplication complete: %dx%d matrices (calculated in %dms)", 
                                     size, size, duration);
        System.out.println("[RMI_SERVICE] " + result);
        return result;
    }
    
    private String checkPrime(int number) {
        System.out.println("[RMI_SERVICE] Checking if " + number + " is prime...");
        long start = System.currentTimeMillis();
        
        boolean isPrime = isPrimeNumber(number);
        
        long duration = System.currentTimeMillis() - start;
        
        String result = String.format("%d is %s (checked in %dms)", 
                                     number, 
                                     isPrime ? "PRIME" : "NOT PRIME", 
                                     duration);
        System.out.println("[RMI_SERVICE] " + result);
        return result;
    }
    
    private boolean isPrimeNumber(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
    
    private String calculateFactorial(int n) {
        System.out.println("[RMI_SERVICE] Calculating " + n + "!...");
        long start = System.currentTimeMillis();
        
        long result = factorial(n);
        
        long duration = System.currentTimeMillis() - start;
        
        String output = String.format("%d! = %d (calculated in %dms)", 
                                     n, result, duration);
        System.out.println("[RMI_SERVICE] " + output);
        return output;
    }
    
    private long factorial(int n) {
        if (n <= 1) return 1;
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
