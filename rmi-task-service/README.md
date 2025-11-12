# RMI Task Service

**Member 5's Core Concept:** Java RMI (Remote Method Invocation)

## Overview

Distributed task execution service using Java RMI. Demonstrates remote method invocation across different JVMs with proper remote interfaces, RMI registry, and exception handling.

## Core Networking Concepts Demonstrated

### 1. **Remote Interface** (extends `java.rmi.Remote`)
- Defines methods that can be called remotely
- All methods must throw `RemoteException`
- Acts as contract between client and server

### 2. **Remote Object** (extends `UnicastRemoteObject`)
- Implementation of the remote interface
- Automatically exported to accept remote calls
- Serializable for network transmission

### 3. **RMI Registry** (`LocateRegistry`)
- Naming service for RMI objects
- Binds remote objects to human-readable names
- Allows clients to lookup services by name

### 4. **Remote Method Invocation**
```
Client JVM                                    Server JVM
    |                                             |
    | 1. Lookup service in registry              |
    |-------------------------------------------->|
    |                                             |
    | 2. Get remote reference (stub)              |
    |<--------------------------------------------|
    |                                             |
    | 3. Invoke remote method                     |
    |-------------------------------------------->|
    |                                             |
    | 4. Method executes on server                |
    |                                        [Execute]
    |                                             |
    | 5. Result returned to client                |
    |<--------------------------------------------|
```

## Architecture

```
┌──────────────────────────────────────────────┐
│         RMI TASK SERVICE (Port 1099)         │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │      TaskService (Remote Interface)    │ │
│  │  - executeTask(String)                 │ │
│  │  - getCpuLoad()                        │ │
│  │  - getStatus()                         │ │
│  │  - getAvailableTasks()                 │ │
│  └──────────────┬─────────────────────────┘ │
│                 │                            │
│  ┌──────────────▼─────────────────────────┐ │
│  │   TaskServiceImpl                      │ │
│  │   (extends UnicastRemoteObject)        │ │
│  │                                        │ │
│  │   Implements:                          │ │
│  │   - Pi calculation                     │ │
│  │   - Fibonacci sequence                 │ │
│  │   - Matrix multiplication              │ │
│  │   - Prime number checking              │ │
│  │   - Factorial calculation              │ │
│  └────────────────────────────────────────┘ │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │        RMI Registry (1099)             │ │
│  │  Binds: TaskService                    │ │
│  └────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
         ▲                      │
         │                      │
    Lookup from            Register with
    RMI Client                  Hub
```

## Features

✅ **Remote Method Invocation:** Call methods across JVM boundaries  
✅ **RMI Registry:** Service discovery and binding  
✅ **Multiple Tasks:** Pi, Fibonacci, Matrix, Prime, Factorial  
✅ **Exception Handling:** Proper RemoteException handling  
✅ **Hub Integration:** Automatic registration and heartbeat  

## Available Tasks

1. **calculate-pi** - Calculate Pi using Leibniz formula
2. **fibonacci-10** - Calculate 10th Fibonacci number
3. **fibonacci-20** - Calculate 20th Fibonacci number
4. **matrix-multiply** - Multiply 100x100 matrices
5. **prime-check-1000** - Check if 1000 is prime
6. **prime-check-10007** - Check if 10007 is prime
7. **factorial-10** - Calculate 10!
8. **factorial-20** - Calculate 20!

## Build & Run

### Build
```powershell
.\build.ps1
# or
mvn clean package
```

### Run Server
```powershell
.\run-server.ps1
# or
java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
```

### Run Client (for testing)
```powershell
.\run-client.ps1
# or
java -cp target\rmi-task-service-1.0-SNAPSHOT.jar com.example.taskservice.client.TaskClient
```

## Testing

### Manual Test with Client

1. Start the RMI server:
   ```powershell
   .\run-server.ps1
   ```

2. In a new terminal, run the client:
   ```powershell
   .\run-client.ps1
   ```

3. The client will automatically test all remote methods and then enter interactive mode

### Interactive Mode Commands

- `list` - Show available tasks
- `status` - Get service status
- `cpu` - Get CPU load
- `calculate-pi` - Execute Pi calculation
- `fibonacci-10` - Execute Fibonacci task
- Any task name from the list
- `quit` - Exit

## UI Demonstration (Dashboard Tab 5)

**Your UI Demo:** The "RMI Task Runner" tab shows distributed computing in action.

**How It Works:**
1. Dashboard sends command to Hub: `{"command_for": "RMI_SERVICE", "payload": "calculate-pi"}`
2. Hub calls RMI client code (integrated in Hub)
3. RMI client invokes remote method on RMI server (THIS IS THE REMOTE METHOD INVOCATION!)
4. Server executes task and returns result
5. Hub forwards result to Dashboard
6. Result appears in UI

**Live Demo Steps:**
1. Navigate to RMI Task Runner tab on Dashboard
2. Select task from dropdown (e.g., "calculate-pi")
3. Click "Execute Remote Task"
4. Watch as remote method is invoked
5. Result appears showing task execution time

## Key Implementation Details

### Remote Interface (TaskService.java)
```java
public interface TaskService extends Remote {
    String executeTask(String taskName) throws RemoteException;
    int getCpuLoad() throws RemoteException;
    String getStatus() throws RemoteException;
    List<String> getAvailableTasks() throws RemoteException;
}
```

### Remote Object Implementation (TaskServiceImpl.java)
```java
public class TaskServiceImpl extends UnicastRemoteObject 
                             implements TaskService {
    public TaskServiceImpl() throws RemoteException {
        super(); // Export this object
    }
    
    @Override
    public String executeTask(String taskName) throws RemoteException {
        // Actual task execution
    }
}
```

### Server Startup (TaskServiceServer.java)
```java
// 1. Create RMI registry
Registry registry = LocateRegistry.createRegistry(1099);

// 2. Create remote object
TaskService service = new TaskServiceImpl();

// 3. Bind in registry
registry.rebind("TaskService", service);

// 4. Register with Hub
hubClient.register();
```

### Client Invocation (TaskClient.java)
```java
// 1. Locate registry
Registry registry = LocateRegistry.getRegistry("localhost", 1099);

// 2. Lookup service
TaskService service = (TaskService) registry.lookup("TaskService");

// 3. Invoke remote method
String result = service.executeTask("calculate-pi");
```

## Hub Integration

The RMI service registers with the Hub for service discovery:

**Registration Message:**
```
REGISTER::RMI_SERVICE::rmi://localhost:1099/TaskService
```

**Heartbeat:**
Sent every 10 seconds to keep service active in Hub registry.

**Hub Command Handling:**
When Hub receives a command for RMI_SERVICE, it uses embedded RMI client code to invoke remote methods.

## Troubleshooting

**Issue:** `java.rmi.ConnectException: Connection refused`
- Make sure RMI server is running
- Check port 1099 is not blocked
- Verify RMI registry is started

**Issue:** `java.rmi.NotBoundException: TaskService`
- Service not bound in registry
- Wait a few seconds for server to fully start
- Check server logs for binding confirmation

**Issue:** Hub not receiving registration
- Make sure Hub is running on port 7070
- Check Hub console for registration message
- Verify network connectivity

**Issue:** Remote method calls fail
- Check RemoteException messages
- Verify server is still running
- Check network connectivity between client/server

## Why RMI?

**Advantages:**
- ✅ Pure Java solution for distributed computing
- ✅ Automatic object serialization
- ✅ Built-in exception handling
- ✅ Type-safe remote calls
- ✅ No manual protocol design needed

**Use Cases:**
- Distributed computing tasks
- Load balancing across servers
- Microservices communication (Java-to-Java)
- Remote administration tools

## Success Criteria

✅ Remote interface extends `java.rmi.Remote`  
✅ All remote methods throw `RemoteException`  
✅ Implementation extends `UnicastRemoteObject`  
✅ RMI registry started and service bound  
✅ Client can lookup and invoke remote methods  
✅ Service registered with Hub  
✅ Remote method invocation works across JVMs  
✅ Dashboard displays task execution results  

## Next Steps

1. Build the service: `.\build.ps1`
2. Start Hub Server (if not running)
3. Start RMI Task Service: `.\run-server.ps1`
4. Test with client: `.\run-client.ps1`
5. Check Dashboard Tab 5 for UI-integrated task execution
6. Try different tasks to see remote method invocation

## Architecture Notes

**Why RMI Client is in Hub:**
- RMI is Java-to-Java communication
- Dashboard (JavaScript) can't directly call RMI
- Hub acts as bridge: WebSocket (Dashboard) → RMI Client (Hub) → RMI Server
- The actual remote method invocation still happens (Hub client → RMI server)
- This demonstrates the core concept while providing UI integration
