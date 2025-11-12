# 🚀 PHASE 3 QUICK START - 5 MINUTES

**Status:** ✅ Ready to Run  
**Time Required:** ~5 minutes  
**What You'll See:** Live service registry with real-time updates

---

## 🎯 QUICK SETUP

### Terminal 1: Start Hub Server

```powershell
cd "d:\Projects\network programming - assignment\services\hub-server"
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

**Expected Output:**

```
[HUB] Ready to accept service connections
```

### Terminal 2: Start API Gateway

```powershell
cd "d:\Projects\network programming - assignment\services\api-gateway-service"
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar
```

**Expected Output:**

```
✓ API GATEWAY SERVICE STARTED SUCCESSFULLY
```

### Terminal 3: Start Dashboard

```powershell
cd "d:\Projects\network programming - assignment\frontend"
npm run dev
```

**Expected Output:**

```
  VITE v7.1.7  ready in 234 ms
  ➜  Local:   http://localhost:5173/
```

### Terminal 4 (Optional): View Logs

```powershell
# Watch for connection messages
# You should see Dashboard → Hub connections
# And ApiGateway → Hub heartbeats
```

---

## 💻 BROWSER STEPS

1. Open `http://localhost:5173`
2. Enter dashboard name: `Monitor 1`
3. Click "Connect to Hub"
4. See "ApiGateway" appear in service list
5. Click on ApiGateway to see details
6. Scroll down to "Weather Information"
7. Enter city: `Colombo`
8. Click "Fetch" button
9. See weather data appear: Temperature, Condition, etc.

---

## ✅ WHAT SHOULD HAPPEN

| Step               | Expected Result                |
| ------------------ | ------------------------------ |
| 1. Dashboard loads | Login screen appears           |
| 2. Click Connect   | Dashboard shows service list   |
| 3. After 5s        | ApiGateway appears as "Online" |
| 4. Click service   | Details panel shows info       |
| 5. Enter city      | Input field works              |
| 6. Click Fetch     | Weather data appears           |
| 7. Disconnect      | Dashboard returns to login     |

---

## 🔍 TROUBLESHOOTING

### Dashboard won't load

```
Check: Is the dashboard on port 5173?
Try: Refresh the page (Ctrl+R)
```

### "Disconnected from Hub Server"

```
Check: Is Hub Server running on port 7070?
Try: Turn off SSL checkbox before connecting
```

### Services don't appear

```
Check: Is API Gateway running?
Wait: Services take ~10 seconds to register
Try: Refresh page
```

### Weather won't fetch

```
Check: Is API Gateway running on port 9001?
Try: Different city name
Check: Browser console for errors
```

---

## 📱 UI WALKTHROUGH

### Login Screen

```
┌─────────────────────────────────┐
│  Distributed Services Hub       │
│                                 │
│  [Dashboard name input]         │
│  [☑] SSL/TLS Enabled            │
│                                 │
│  [Connect to Hub]               │
│                                 │
│  Port: 7070                     │
└─────────────────────────────────┘
```

### Dashboard Screen

```
Left Panel (Services):
  ✓ ApiGateway
    Port: 9001
    Heartbeat: 5s ago

Main Panel (Details):
  ApiGateway
  Status: Online ✓
  Host: localhost
  Port: 9001
  Last Heartbeat: 12:34:56

Bottom Panel (Weather):
  City: [Colombo] [Fetch]
  Temperature: 28.5°C
  Condition: Sunny
```

---

## 🎓 WHAT YOU'RE TESTING

1. **WebSocket to Hub** - Real-time service discovery
2. **Service Registry** - Live service list updates
3. **Service Details** - Metadata display
4. **External API** - Weather from API Gateway
5. **Auto-Reconnection** - Connection recovery
6. **UI Responsiveness** - Real-time updates

---

## 📊 COMPONENTS IN ACTION

```
Dashboard (React)
    ↓ WebSocket
Hub Server (Java)
    ↓ Broadcasts services
Dashboard (React)
    ↓ Shows services
    ↓ User clicks weather
Dashboard (React)
    ↓ WebSocket
API Gateway (Java)
    ↓ HttpURLConnection
External Weather API
    ↓ Returns data
API Gateway (Java)
    ↓ Sends to dashboard
Dashboard (React)
    ↓ Displays weather
```

---

## 🎯 SUCCESS INDICATORS

You know it's working when you see:

- ✅ Dashboard connects (no errors)
- ✅ Service appears within 10 seconds
- ✅ Service shows "Online" status
- ✅ Click service shows details
- ✅ Weather fetches successfully
- ✅ Console shows no WebSocket errors
- ✅ All updates are real-time

---

## 📚 NEXT STEPS (After Verification)

1. ✅ Phase 3 UI is working correctly
2. 🔜 Phase 4: Secure File Service
3. 🔜 Phase 5: NIO Log Service
4. 🔜 Phase 6: RMI Task Service

---

**Ready? Start the services above and open the browser!** 🚀

---

## 🔧 PORTS REFERENCE

| Service      | Port | Type          |
| ------------ | ---- | ------------- |
| Hub Server   | 7070 | TCP/WebSocket |
| API Gateway  | 9001 | WebSocket     |
| Dashboard    | 5173 | HTTP (Vite)   |
| Optional SSL | 7443 | TLS           |

---

## 💡 TIPS

- Keep browser console open (F12) to see connection logs
- All three services (Hub, API Gateway, Dashboard) must be running
- Services take 10 seconds to register due to heartbeat timing
- Click refresh if dashboard seems stuck
- Check firewall if connection fails

---

**Questions?** Check the full PHASE_3_COMPLETE.md for detailed documentation.
