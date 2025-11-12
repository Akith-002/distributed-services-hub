# Member 5 - RMI Task Service Startup & Demonstration Guide

**Your Component:** RMI Task Service (Remote Method Invocation)  
**Your Core Networking Concepts:** Java RMI, Remote Interfaces, RMI Registry, Distributed Computing  
**Port:** 1099 (RMI Registry)

---

## 📚 Java Network Programming Concepts You're Demonstrating

### 1. **Java RMI (Remote Method Invocation)**
- Call methods on objects in different JVMs
- Distributed computing across network boundaries
- Transparent remote calls (looks like local calls)

### 2. **Remote Interface** (extends `java.rmi.Remote`)
- Defines methods that can be invoked remotely
- All methods must throw `RemoteException`
- Acts as contract between client and server

### 3. **Remote Object** (extends `UnicastRemoteObject`)
- Implementation of the remote interface
- Automatically exported to accept remote calls
- Serializable for network transmission

### 4. **RMI Registry** (Naming Service)
- Directory service for RMI objects
- Binds remote objects to human-readable names
- Clients lookup services by name
- Default port: 1099

### 5. **Stub & Skeleton** (Behind the Scenes)
- **Stub:** Client-side proxy for remote object
- **Skeleton:** Server-side receiver (auto-generated in modern Java)
- Handles marshalling/unmarshalling of parameters and results

### 6. **Distributed Computing Pattern**
```
Client JVM                    Network                    Server JVM
-----------                  --------                   -----------
1. Lookup("TaskService")  ─────────►  RMI Registry
2. Get Stub               ◄─────────  Return reference
3. stub.executeTask(...)  ─────────►  Unmarshal → Execute method
4. Wait for result        ◄─────────  Marshal → Return result
```

---

## 🚀 How to Start Your Service

### Step 1: Navigate to Your Service Directory
```powershell
cd distributed-services-hub\rmi-task-service
```

### Step 2: Build Your Service (First Time Only)
```powershell
.\build.ps1
```
Or manually:
```powershell
mvn clean package
```

**Expected Output:**
```
[INFO] Building jar: target\rmi-task-service-1.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

### Step 3: Start the Hub Server First

**IMPORTANT:** Hub must be running before your service starts!
```powershell
cd ..\hub-server
java -jar target\hub-server-1.0-SNAPSHOT.jar
```

### Step 4: Run Your RMI Task Service

```powershell
cd ..\rmi-task-service
.\run-server.ps1
```

Or manually:
```powershell
java -jar target\rmi-task-service-1.0-SNAPSHOT.jar
```

**Expected Output:**
```
================================================================================
  RMI TASK SERVICE - MEMBER 5
================================================================================

[RMI Server] Starting RMI Registry on port 1099...
[RMI Server] RMI Registry created successfully

[TaskServiceImpl] Creating TaskService remote object...
[TaskServiceImpl] Remote object created and exported

[RMI Server] Binding TaskService to registry...
[RMI Server] Service bound to: rmi://localhost:1099/TaskService

[HubClient] Connecting to Hub at localhost:7070...
[HubClient] Connected to Hub successfully
[HubClient] Sent registration: REGISTER::RmiTaskService::localhost::1099

✓ RMI TASK SERVICE STARTED SUCCESSFULLY

✓ Hub Registration: SUCCESS
✓ RMI Registry: RUNNING on port 1099
✓ Service Binding: rmi://localhost:1099/TaskService
✓ Remote Object: EXPORTED and ready for calls
✓ Heartbeat: ACTIVE (every 10 seconds)
✓ Available Tasks: 8 tasks ready

Available Remote Methods:
  - executeTask(String taskName)
  - getAvailableTasks()
  - getStatus()
  - getCpuLoad()

Waiting for remote method invocations...
```

---

## 🎯 How to Demonstrate Your Work

### Method 1: Interactive RMI Client (Best for Learning)

This is the **BEST way** to understand RMI!

#### Start the RMI Client
```powershell
# In a NEW terminal (keep server running)
cd distributed-services-hub\rmi-task-service
.\run-client.ps1
```

Or manually:
```powershell
java -cp target\rmi-task-service-1.0-SNAPSHOT.jar com.example.taskservice.client.TaskClient
```

**You'll see:**
```
================================================================================
  RMI TASK CLIENT
================================================================================

Looking up TaskService in RMI Registry...
✓ Connected to: rmi://localhost:1099/TaskService
✓ Remote reference obtained

Running automated tests...

Test 1: getStatus()
-------------------
✓ Result: "RMI Task Service: RUNNING"

Test 2: getAvailableTasks()
---------------------------
✓ Available tasks (8):
  1. calculate-pi
  2. fibonacci-10
  3. fibonacci-20
  4. matrix-multiply
  5. prime-check-1000
  6. prime-check-10007
  7. factorial-10
  8. factorial-20

Test 3: getCpuLoad()
--------------------
✓ Current CPU load: 5%

Test 4: executeTask("calculate-pi")
------------------------------------
✓ Result: Pi ≈ 3.1415916536 (calculated in 28ms)

Test 5: executeTask("fibonacci-10")
------------------------------------
✓ Result: Fibonacci(10) = 55 (calculated in 0ms)

Test 6: executeTask("factorial-10")
------------------------------------
✓ Result: 10! = 3628800 (calculated in 0ms)

✓✓✓ ALL REMOTE METHOD CALLS SUCCESSFUL ✓✓✓

Entering interactive mode...
Type 'help' for available commands

RMI Client>
```

#### Interactive Mode Commands

**Available Commands:**
- `list` - Show all available tasks
- `status` - Get service status
- `cpu` - Get CPU load
- `calculate-pi` - Execute Pi calculation
- `fibonacci-10` - Calculate 10th Fibonacci number
- `fibonacci-20` - Calculate 20th Fibonacci number
- `matrix-multiply` - Multiply matrices
- `prime-check-1000` - Check if 1000 is prime
- `prime-check-10007` - Check if 10007 is prime
- `factorial-10` - Calculate 10!
- `factorial-20` - Calculate 20!
- `help` - Show help
- `quit` - Exit

**Example Session:**
```
RMI Client> calculate-pi
Calling remote method: executeTask("calculate-pi")...
✓ Remote execution completed
Result: Pi ≈ 3.1415916536 (28ms)

RMI Client> fibonacci-20
Calling remote method: executeTask("fibonacci-20")...
✓ Remote execution completed
Result: Fibonacci(20) = 6765 (1ms)

RMI Client> prime-check-10007
Calling remote method: executeTask("prime-check-10007")...
✓ Remote execution completed
Result: 10007 is PRIME (5ms)

RMI Client> cpu
Calling remote method: getCpuLoad()...
✓ Remote execution completed
Result: CPU Load: 12%

RMI Client> quit
Goodbye!
```

**Watch your server logs:**
```
[TaskServiceImpl] Remote call: executeTask("calculate-pi")
[TaskServiceImpl] Executing Pi calculation...
[TaskServiceImpl] Pi calculation completed: 3.1415916536 (28ms)

[TaskServiceImpl] Remote call: executeTask("fibonacci-20")
[TaskServiceImpl] Executing Fibonacci(20)...
[TaskServiceImpl] Fibonacci(20) = 6765 (1ms)

[TaskServiceImpl] Remote call: getCpuLoad()
[TaskServiceImpl] CPU load requested: 12%
```

### Method 2: UI Demonstration (Tab 5: RMI Task Runner)

This is the **VISUAL** way to show your work!

#### Start the Dashboard
```powershell
cd ..\..\multi-client-chat-frontend
npm run dev
```

Open browser: **http://localhost:5173**

#### Navigate to Tab 5: RMI Task Runner

1. Click on **"RMI Task Runner"** tab (Tab 5)
2. You'll see a task selection interface
3. Select a task from dropdown
4. Click "Execute Task"
5. **Watch the result appear!**

**UI Workflow:**
```
1. User selects task: "calculate-pi"
2. Dashboard sends command to Hub via WebSocket
3. Hub forwards to RMI Task Service
4. RMI service calls remote method executeTask("calculate-pi")
5. Method executes on server JVM
6. Result returned to Hub
7. Hub forwards to Dashboard
8. UI displays result
```

**What the UI Shows:**
```
RMI Task Runner
---------------

Select Task: [Calculate Pi ▼]

[Execute Task]

Result:
-------
Task: calculate-pi
Result: Pi ≈ 3.1415916536
Execution Time: 28ms
Status: ✓ Success
```

### Method 3: Demonstrate All 8 Tasks

**Show the variety of remote computations:**

#### 1. Calculate Pi (Monte Carlo Method)
```
RMI Client> calculate-pi
Result: Pi ≈ 3.1415916536 (28ms)
```

#### 2. Fibonacci Sequence
```
RMI Client> fibonacci-10
Result: Fibonacci(10) = 55 (0ms)

RMI Client> fibonacci-20
Result: Fibonacci(20) = 6765 (1ms)
```

#### 3. Matrix Multiplication
```
RMI Client> matrix-multiply
Result: Matrix multiplication (100x100) completed (15ms)
```

#### 4. Prime Number Checking
```
RMI Client> prime-check-1000
Result: 1000 is NOT PRIME (0ms)

RMI Client> prime-check-10007
Result: 10007 is PRIME (5ms)
```

#### 5. Factorial Calculation
```
RMI Client> factorial-10
Result: 10! = 3628800 (0ms)

RMI Client> factorial-20
Result: 20! = 2432902008176640000 (0ms)
```

---

## 🎓 Explaining Your Networking Concepts

### For Your Presentation/Demo, Explain:

#### 1. Remote Interface Definition

**Code Reference in `TaskService.java`:**
```java
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TaskService extends Remote {
    // All methods must throw RemoteException
    String executeTask(String taskName) throws RemoteException;
    String[] getAvailableTasks() throws RemoteException;
    String getStatus() throws RemoteException;
    double getCpuLoad() throws RemoteException;
}
```

**Explain:**
- "Remote interface defines methods that can be called from another JVM"
- "Must extend `java.rmi.Remote` to mark it as remotely accessible"
- "All methods must throw `RemoteException` for network failures"
- "This is the contract between client and server"

#### 2. Remote Object Implementation

**Code Reference in `TaskServiceImpl.java`:**
```java
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TaskServiceImpl extends UnicastRemoteObject implements TaskService {
    
    public TaskServiceImpl() throws RemoteException {
        super(); // Export this object to accept remote calls
    }
    
    @Override
    public String executeTask(String taskName) throws RemoteException {
        // Method body executes on SERVER JVM
        switch (taskName) {
            case "calculate-pi":
                return calculatePi();
            case "fibonacci-10":
                return fibonacci(10);
            // ... other tasks
        }
    }
}
```

**Explain:**
- "Implementation extends `UnicastRemoteObject`"
- "This automatically exports the object to accept remote calls"
- "Constructor must call `super()` to set up RMI infrastructure"
- "When client calls method, it executes HERE on server JVM"

#### 3. RMI Registry and Binding

**Code Reference in `RmiTaskServer.java`:**
```java
// Create RMI Registry on port 1099
Registry registry = LocateRegistry.createRegistry(1099);

// Create remote object
TaskService service = new TaskServiceImpl();

// Bind to registry with a name
Naming.rebind("rmi://localhost:1099/TaskService", service);

System.out.println("Service bound to: rmi://localhost:1099/TaskService");
```

**Explain:**
- "RMI Registry is a naming service - like a phone book"
- "We create a registry on port 1099 (default RMI port)"
- "We bind our remote object to a name: 'TaskService'"
- "Clients can look up the service by this name"

#### 4. Client Lookup and Remote Calls

**Code Reference in `TaskClient.java`:**
```java
// Look up the remote service
Registry registry = LocateRegistry.getRegistry("localhost", 1099);
TaskService service = (TaskService) registry.lookup("TaskService");

// Now we can call methods as if they were local!
String result = service.executeTask("calculate-pi");
System.out.println("Result: " + result);
```

**Explain:**
- "Client connects to RMI Registry on server's host and port"
- "Looks up service by name: 'TaskService'"
- "Gets back a **stub** - a proxy object"
- "When we call methods on the stub, RMI sends request to server"
- "Server executes method and returns result"
- "Client receives result - looks like local method call!"

#### 5. How RMI Works Behind the Scenes

**The Complete Flow:**

```
Client Side:                         Network:                    Server Side:
-----------                          --------                   ------------

1. registry.lookup("TaskService")
                                ─────────────►
                                              RMI Registry returns stub
                                ◄─────────────

2. service.executeTask("pi")    
                                ─────────────►
   Stub marshals:                            Skeleton unmarshals:
   - Method name                             - Method name
   - Parameters                              - Parameters
                                              
                                              3. Execute method
                                                 calculatePi()
                                              
                                              4. Marshal result
                                ◄─────────────
   5. Unmarshal result
   
6. Return to caller
```

**Explain:**
- "Marshalling: Converting objects to byte stream for network transmission"
- "Unmarshalling: Converting byte stream back to objects"
- "Stub: Client-side proxy that handles communication"
- "Skeleton: Server-side receiver (auto-generated in modern Java)"

#### 6. Why RMI is Powerful

**Traditional Socket Approach:**
```java
// Client sends request manually
Socket socket = new Socket("localhost", 1099);
PrintWriter out = new PrintWriter(socket.getOutputStream());
out.println("EXECUTE:calculate-pi");

// Read response manually
BufferedReader in = new BufferedReader(
    new InputStreamReader(socket.getInputStream())
);
String result = in.readLine();
```

**RMI Approach:**
```java
// Just call the method!
String result = service.executeTask("calculate-pi");
```

**Explain:**
- "RMI abstracts away all the networking complexity"
- "No manual socket handling, protocol design, or data parsing"
- "Method calls look local but execute remotely"
- "RMI handles serialization, network communication, and error handling"

---

## 📊 Demonstration Checklist

Use this checklist during your demonstration:

### Command Line Demo - Server
- [ ] Start Hub Server
- [ ] Start RMI Task Service: `.\run-server.ps1`
- [ ] Show RMI Registry created on port 1099
- [ ] Show service bound to "TaskService"
- [ ] Show service registered with Hub
- [ ] Show 8 available tasks listed
- [ ] Show remote object exported

### Command Line Demo - Client
- [ ] Start RMI Client: `.\run-client.ps1`
- [ ] Show successful lookup in registry
- [ ] Show automated test results (6 tests)
- [ ] Show all remote method calls succeed
- [ ] Enter interactive mode
- [ ] Execute: `list`
- [ ] Execute: `status`
- [ ] Execute: `cpu`
- [ ] Execute: `calculate-pi`
- [ ] Execute: `fibonacci-20`
- [ ] Execute: `prime-check-10007`
- [ ] Execute: `factorial-10`
- [ ] Show server logs for each remote call

### UI Demo (Tab 5: RMI Task Runner)
- [ ] Open Dashboard at http://localhost:5173
- [ ] Navigate to Tab 5: "RMI Task Runner"
- [ ] Select task: "Calculate Pi"
- [ ] Click "Execute Task"
- [ ] Show result appears
- [ ] Try different tasks:
  - [ ] Fibonacci-10
  - [ ] Prime check
  - [ ] Factorial
- [ ] Show execution times
- [ ] Show multiple executions work

### Technical Explanation Points
- [ ] Explain Remote interface (extends Remote)
- [ ] Explain RemoteException requirement
- [ ] Explain UnicastRemoteObject
- [ ] Explain RMI Registry purpose
- [ ] Explain binding and lookup process
- [ ] Explain stub and skeleton
- [ ] Explain marshalling/unmarshalling
- [ ] Show method executes on server JVM
- [ ] Compare with traditional socket approach
- [ ] Explain why RMI is powerful

---

## 🔧 Troubleshooting

### Issue: Port 1099 already in use
```powershell
# Find what's using the port
netstat -ano | findstr :1099

# Kill the process
taskkill /PID <PID> /F
```

### Issue: RemoteException - Connection refused
```
java.rmi.ConnectException: Connection refused to host: localhost
```
**Solution:**
- Make sure RMI server is running
- Check port 1099 is accessible
- Check firewall settings

### Issue: NotBoundException
```
java.rmi.NotBoundException: TaskService
```
**Solution:**
- Server didn't bind service to registry
- Check service name matches exactly: "TaskService"
- Restart server

### Issue: ClassNotFoundException
```
java.rmi.UnmarshalException: error unmarshalling return
```
**Solution:**
- Client and server must have same class definitions
- Make sure using same JAR file or compiled classes

---

## 📝 Key Points for Your Report

Include these in your written documentation:

### Architecture
- RMI Task Service provides remote computational tasks
- Uses RMI Registry for service discovery
- Clients invoke methods across JVM boundaries
- 8 different computational tasks available

### Networking Concepts

1. **Remote Interface** (extends `Remote`)
   - Defines remotely accessible methods
   - All methods throw `RemoteException`
   - Acts as service contract

2. **Remote Object** (extends `UnicastRemoteObject`)
   - Implementation of remote interface
   - Automatically exported for remote access
   - Serializable for network transmission

3. **RMI Registry**
   - Naming service on port 1099
   - Binds remote objects to names
   - Clients lookup services by name

4. **Remote Method Invocation**
   - Transparent remote calls
   - Automatic marshalling/unmarshalling
   - Exception handling for network failures

5. **Distributed Computing**
   - Client and server in different JVMs
   - Computation happens on server
   - Results returned to client

### Why These Concepts Matter
- **Abstraction:** No manual socket/protocol handling
- **Transparency:** Remote calls look like local calls
- **Distributed Computing:** Offload computation to powerful servers
- **Scalability:** Multiple clients can use same service
- **Real-World:** Used in enterprise systems, microservices

---

## 🎬 Presentation Script Example

**1. Introduction (30 seconds)**
"I implemented the RMI Task Service using Java RMI - Remote Method Invocation. It demonstrates distributed computing, where clients can call methods on objects running in different JVMs across the network."

**2. Show Architecture (30 seconds)**
"The server creates an RMI Registry on port 1099 and binds a TaskService remote object. Clients look up the service by name, get a stub (proxy), and can call methods as if they were local - but they execute on the server."

**3. Code Walkthrough (1 minute)**
"Here's the remote interface - it extends `Remote` and all methods throw `RemoteException`. The implementation extends `UnicastRemoteObject` which exports it for remote access. On the server, we bind it to the registry. On the client, we look it up and call methods - RMI handles all the networking."

**4. Live Demo - Interactive Client (2 minutes)**
"Let me start the RMI server... It creates the registry and binds the service. Now I'll run the client... It looks up the service and runs automated tests. See - all remote method calls succeed! Now in interactive mode, I'll execute Pi calculation... The method runs on the SERVER JVM, result returns to client. Let me try Fibonacci... Prime check... All transparent!"

**5. Server Logs (30 seconds)**
"Look at the server logs - you can see each remote method invocation being logged. The server is executing these methods, not the client."

**6. UI Demo (1 minute)**
"In the Dashboard Tab 5, I can execute tasks visually. Select Calculate Pi... Click Execute... Result appears. The flow is: Dashboard → Hub → RMI Service → Remote Method → Result back through Hub → Dashboard."

---

## 📚 Study References

Review these concepts before your demo:

- **Lesson 9 (or RMI topic):** Java RMI, Remote interfaces, RMI Registry
- **Distributed Computing:** Client-server architecture across JVMs

### Key RMI Classes to Know:
- `java.rmi.Remote` - Marker interface for remote interfaces
- `java.rmi.RemoteException` - Exception for network failures
- `java.rmi.server.UnicastRemoteObject` - Base for remote objects
- `java.rmi.registry.LocateRegistry` - Access to RMI registry
- `java.rmi.Naming` - Bind/lookup remote objects

### Key Concepts:
- **Remote Interface:** Contract defining remotely callable methods
- **Remote Object:** Implementation that accepts remote calls
- **RMI Registry:** Naming service for remote objects
- **Stub:** Client-side proxy
- **Skeleton:** Server-side receiver (auto in modern Java)
- **Marshalling:** Serializing objects for network transmission
- **Unmarshalling:** Deserializing received objects

---

## ✅ Pre-Demo Checklist

Before your demonstration:

- [ ] Service builds successfully (`.\build.ps1`)
- [ ] Hub Server is running
- [ ] Can start RMI server: `.\run-server.ps1`
- [ ] RMI Registry created on port 1099
- [ ] Service bound to "TaskService"
- [ ] Port 1099 is available
- [ ] Can start RMI client: `.\run-client.ps1`
- [ ] Client successfully looks up service
- [ ] All 6 automated tests pass
- [ ] Interactive mode works
- [ ] All 8 tasks execute successfully
- [ ] Dashboard Tab 5 loads and works
- [ ] Can execute tasks from UI
- [ ] You understand Remote interface concept
- [ ] You understand UnicastRemoteObject
- [ ] You understand RMI Registry purpose
- [ ] You can explain stub/skeleton
- [ ] You can explain marshalling
- [ ] You practiced the demo at least once

---

## 🌟 Extra Credit Demonstrations

### Show Remote Exception Handling

Stop the server while client is running:
```
RMI Client> calculate-pi
❌ Error: java.rmi.NoSuchObjectException: no such object in table
Explanation: Server is no longer available
```

### Show Multiple Clients

Start 2 clients simultaneously:
```
Both clients can call the same remote service concurrently!
```

### Explain Serialization

```java
// Parameters and return values must be Serializable
public String executeTask(String taskName) // String is Serializable
```

### Compare Performance

```
Local method call: < 1 microsecond
Remote method call: 1-5 milliseconds (network overhead)
```

---

## 📊 What Makes Your Demo Stand Out

1. **Transparent Remote Calls:** Methods look local but execute remotely
2. **Automated Testing:** 6 tests prove all remote methods work
3. **Interactive Mode:** Live demonstration of RMI calls
4. **Visual UI:** Dashboard shows distributed computing in action
5. **Multiple Tasks:** 8 different computations show versatility
6. **Server Logs:** Proof that methods execute on server JVM

---

## 🎓 Deep Dive: How RMI Actually Works

**For advanced understanding:**

1. **Stub Generation:**
   - In old Java: `rmic` tool generated stubs
   - Modern Java: Dynamic proxy creates stubs at runtime

2. **Marshalling Process:**
   - Method name → Serialized
   - Parameters → Serialized
   - Sent over network as byte stream

3. **Remote Reference Layer:**
   - Manages connections to remote objects
   - Handles retries and failures

4. **Transport Layer:**
   - Uses TCP sockets underneath
   - Port 1099 for registry
   - Random high ports for actual RMI calls

---

**Good luck with your demonstration! Show them the magic of transparent distributed computing with RMI!**
