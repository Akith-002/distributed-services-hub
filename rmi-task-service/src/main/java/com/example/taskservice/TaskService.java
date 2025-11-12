package com.example.taskservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * TaskService Remote Interface
 * CORE CONCEPT: Java RMI (Remote Method Invocation)
 * 
 * This interface defines methods that can be invoked remotely from another JVM.
 * All methods must throw RemoteException as required by RMI.
 */
public interface TaskService extends Remote {
    
    /**
     * Execute a task by name
     * @param taskName Name of the task to execute
     * @return Result of the task execution
     * @throws RemoteException if remote communication fails
     */
    String executeTask(String taskName) throws RemoteException;
    
    /**
     * Get current CPU load percentage
     * @return CPU load as integer percentage (0-100)
     * @throws RemoteException if remote communication fails
     */
    int getCpuLoad() throws RemoteException;
    
    /**
     * Get service status
     * @return Status message
     * @throws RemoteException if remote communication fails
     */
    String getStatus() throws RemoteException;
    
    /**
     * Get list of available tasks
     * @return List of task names
     * @throws RemoteException if remote communication fails
     */
    List<String> getAvailableTasks() throws RemoteException;
}
