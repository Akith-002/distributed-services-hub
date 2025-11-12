# Troubleshooting File Upload

## Current Status
The file upload is stuck in "Uploading..." state, which means:
1. ✅ WebSocket connects successfully
2. ✅ Frontend sends the upload command
3. ❌ Frontend doesn't receive a response

## Debugging Steps

### Step 1: Check API Gateway Console
Look at the **API Gateway PowerShell window** and see if you see these messages when you try to upload:

```
[ApiGateway] Upload request received: {"command":"uploadFile","fileName":"...","fileData":"..."}
[ApiGateway] Uploading file: filename (XXX bytes)
[ApiGateway] Secure File Service response: SUCCESS::...
[ApiGateway] Sending response to frontend: {...}
[ApiGateway] ✓ File uploaded successfully
```

**If you DON'T see these messages**, the upload handler is not being triggered.

### Step 2: Check Browser Console
Open browser DevTools (F12) → Console tab
Look for these messages:
- "Connected to API Gateway for file operations" ✅
- "Service status update received" ✅
- Any error messages ❌

### Step 3: Test WebSocket Manually

Open browser console (F12) and run this JavaScript code:

```javascript
// Test upload via WebSocket
const ws = new WebSocket("ws://localhost:9001/api");

ws.onopen = () => {
  console.log("WebSocket opened");
  
  // Send upload command
  const command = {
    command: "uploadFile",
    fileName: "test.txt",
    fileData: "Hello from manual test"
  };
  
  console.log("Sending:", command);
  ws.send(JSON.stringify(command));
};

ws.onmessage = (event) => {
  console.log("Response:", event.data);
  const response = JSON.parse(event.data);
  console.log("Parsed:", response);
};

ws.onerror = (error) => {
  console.error("WebSocket error:", error);
};

ws.onclose = () => {
  console.log("WebSocket closed");
};
```

This will show you:
1. If the WebSocket connects
2. If the command is sent correctly
3. What response (if any) comes back

### Step 4: Verify Secure File Service

Check if Secure File Service is running:

```powershell
netstat -ano | findstr :9090
```

Should show:
```
TCP    0.0.0.0:9090    LISTENING    [PID]
```

### Step 5: Check for Errors

**Look at the Secure File Service console** - any error messages?

## Possible Issues

### Issue 1: SecureFileClient Timeout
The SSL connection to port 9090 might be timing out. The SecureFileClient.storeFile() method might be hanging.

**Solution**: Add timeout to SSL socket connection.

### Issue 2: Response Not Sent
The API Gateway might be getting an exception when trying to send the response.

**Solution**: Check for exceptions in API Gateway console.

### Issue 3: Frontend Not Handling Response
The response type might be wrong or the frontend is not parsing it.

**Solution**: Check browser console for the actual response.

### Issue 4: WebSocket Disconnect
The WebSocket might be disconnecting before the upload completes.

**Solution**: Check WebSocket state in browser console.

## Quick Fix Attempt

If the API Gateway console shows no messages at all, try this:

1. **Hard refresh browser**: Ctrl + Shift + R (clears all cache)
2. **Check WebSocket state**: In browser console, check if WebSocket is actually open
3. **Try the manual test above**: To see if the issue is in the FileServiceInterface component

## What to Tell Me

Please check the **API Gateway console window** and tell me:
1. Do you see ANY messages when you click Upload?
2. What is the LAST message you see?
3. Any error messages in red?

This will help me identify exactly where the upload is failing!
