# 🔧 FIX: Secure File Service Not Registered

**Issue:** Only API Gateway appears in service registry  
**Cause:** Secure File Service TCP connected but registration failed  
**Solution:** Restart the Secure File Service

---

## 🔍 DIAGNOSIS

**Current Status:**
- ✅ Hub Server: Running (port 7070, 7071)
- ✅ API Gateway: Registered and online
- ⚠️ Secure File Service: Running but NOT registered
  - Port 9090: ✅ Listening
  - TCP to Hub: ✅ Connected (127.0.0.1:12038 → 127.0.0.1:7070)
  - Registration: ❌ FAILED

**The Problem:**
The service connected to Hub via TCP but the REGISTER message either:
1. Wasn't sent properly
2. Was sent in wrong format
3. Wasn't processed by Hub

---

## ✅ SOLUTION: Restart Secure File Service

### **Step 1: Stop the Current Service**

Find the Secure File Service PowerShell window and:
1. Press `Ctrl+C` to stop it
2. Or close the window entirely

### **Step 2: Restart the Service**

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\secure-file-service"
java -jar target/secure-file-service-1.0-SNAPSHOT.jar
```

### **Step 3: Watch for Registration Messages**

Look for these messages in the console:

**✅ Success Messages:**
```
[HubClient] Attempting to connect to Hub at localhost:7070
[HubClient] Connected to Hub successfully
[HubClient] Sending: REGISTER::SecureFileService::localhost::9090
[HubClient] ✓ Service registered with Hub
[HubClient] Heartbeat thread started
```

**❌ Error Messages:**
```
[HubClient] ✗ Registration failed: ...
[HubClient] Connection refused
[HubClient] Socket timeout
```

### **Step 4: Verify Registration**

Wait 5 seconds after startup, then check:

```powershell
curl http://localhost:7071/services | ConvertFrom-Json | ConvertTo-Json
```

**Expected Output:**
```json
{
    "services": [
        {
            "name": "ApiGateway",
            "host": "localhost",
            "port": 9001,
            "status": "online"
        },
        {
            "name": "SecureFileService",
            "host": "localhost",
            "port": 9090,
            "status": "online"
        }
    ]
}
```

---

## 🚀 QUICK FIX COMMANDS

**Complete restart sequence:**

```powershell
# 1. Stop Secure File Service (press Ctrl+C in its window or close it)

# 2. Restart it
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\secure-file-service"
java -jar target/secure-file-service-1.0-SNAPSHOT.jar

# 3. Wait 5 seconds, then verify
Start-Sleep -Seconds 5
curl http://localhost:7071/services | ConvertFrom-Json | Select-Object -ExpandProperty services | Format-Table
```

---

## 🔍 ALTERNATIVE: Check Hub Server Logs

If restarting doesn't work, check the Hub Server console for errors:

**Look for:**
- Connection from Secure File Service
- REGISTER message received
- Any error messages
- Registration confirmation

---

## 🐛 IF IT STILL DOESN'T WORK

### **Option 1: Rebuild and Restart**

```powershell
cd secure-file-service
mvn clean package
java -jar target/secure-file-service-1.0-SNAPSHOT.jar
```

### **Option 2: Check HubClient Code**

The registration might be commented out or disabled. Check:
```powershell
Get-Content src\main\java\com\example\fileservice\HubClient.java | Select-String -Pattern "REGISTER|register"
```

### **Option 3: Manual TCP Test**

Test if Hub accepts REGISTER commands:
```powershell
# This requires telnet or a TCP client
# The REGISTER format should be:
# REGISTER::SecureFileService::localhost::9090
```

---

## ✅ SUCCESS CRITERIA

After restart, you should see:

1. ✅ **In Secure File Service Console:**
   ```
   [HubClient] ✓ Service registered with Hub
   [HubClient] Heartbeat sent to Hub
   ```

2. ✅ **In Hub Status:**
   ```powershell
   curl http://localhost:7071/hub-status
   # Should show: "totalServices": 2, "onlineServices": 2
   ```

3. ✅ **In Services List:**
   ```powershell
   curl http://localhost:7071/services
   # Should show both ApiGateway AND SecureFileService
   ```

4. ✅ **In Frontend:**
   - Open http://localhost:5173
   - Should see 2 services listed
   - Both showing "online" status

---

## 📊 WHAT YOU SHOULD SEE AFTER FIX

**Terminal Output:**
```
[STARTUP] Step 1: Connecting to Hub Server...
[HubClient] Attempting to connect to Hub at localhost:7070
[HubClient] Connected to Hub successfully
[HubClient] Sending: REGISTER::SecureFileService::localhost::9090
[HubClient] ✓ Service registered with Hub    ← THIS IS KEY!
[STARTUP] ✓ Connected to Hub successfully

[STARTUP] Step 2: Starting SSL File Server...
[SSL] Initializing SSL context...
[SSL] Server ready on port 9090
```

**Services Query:**
```json
{
  "services": [
    {"name": "ApiGateway", "status": "online"},
    {"name": "SecureFileService", "status": "online"}  ← THIS SHOULD APPEAR!
  ]
}
```

---

## 🎯 QUICK CHECKLIST

- [ ] Stop Secure File Service (Ctrl+C or close window)
- [ ] Restart: `java -jar target/secure-file-service-1.0-SNAPSHOT.jar`
- [ ] Watch console for "✓ Service registered with Hub"
- [ ] Verify: `curl http://localhost:7071/services`
- [ ] Check: Should see 2 services
- [ ] Test: Open http://localhost:5173 - should see both services

---

**Most likely this is just a registration timing issue or connection hiccup. A simple restart should fix it!** 🔧✅
