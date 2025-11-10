# 🚀 PHASE 3 KICKOFF - React Dashboard Refactoring

**Status:** 🟡 READY TO START  
**Date:** November 10, 2025  
**Objective:** Transform Chat UI → Service Registry Dashboard

---

## 📋 PHASE 3 OVERVIEW

### Current State (Chat Application)

- Login screen → ChatRoom with messages
- User list on sidebar → Shows connected chat users
- Message panel → Displays chat messages
- Upload modal → File uploads

### Target State (Service Registry Dashboard)

- Login screen → Service Dashboard with services
- Service list on sidebar → Shows registered microservices
- Service panel → Displays real-time service status
- Action panel → View logs, execute tasks, upload files

---

## 🎯 PHASE 3 TASKS

### Task 1: Create New Components (High Priority)

#### 1.1 ServiceDashboard.jsx (Replaces ChatRoom.jsx)

```
Purpose: Main dashboard showing all services
Displays:
  - Service name, host, port, status
  - Last heartbeat time
  - Action buttons (view details, upload file, execute task)
  - CPU load (from RMI service)
```

#### 1.2 ServiceRegistry.jsx (Sidebar replacement)

```
Purpose: Displays list of registered services
Shows:
  - Service name
  - Status badge (Online/Offline/Timeout)
  - Port number
  - Select/highlight functionality
```

#### 1.3 ExternalDataFetcher.jsx (New component)

```
Purpose: Fetch and display external API data
Features:
  - City input field
  - Button to fetch weather
  - Display temperature, conditions
  - Loading/error states
  - Uses API Gateway service
```

#### 1.4 ServiceDetailsPanel.jsx (New component)

```
Purpose: Show detailed info for selected service
Displays:
  - Service metadata
  - Connection status
  - Real-time metrics
  - Available actions
```

### Task 2: Update App.jsx

```
Changes:
  1. Replace chat WebSocket URL with Hub Server URL (port 7070)
  2. Connect to Hub instead of chat server
  3. Replace message handlers with service registry handlers
  4. Update state management:
     - Replace 'messages' → 'services'
     - Replace 'users' → 'registeredServices'
     - Add 'selectedService', 'metrics'
  5. Handle SERVICE_REGISTRY_UPDATE message
```

### Task 3: Update Styling (CSS)

```
Changes:
  1. Rename ChatRoom.css → ServiceDashboard.css
  2. Adjust panel layouts for service display
  3. Update colors/badges for service status
  4. Create table-style layout for services
```

### Task 4: WebSocket Message Handlers

```
Old Messages (Remove):
  - JOIN
  - MESSAGE
  - USER_LIST_UPDATE
  - TYPING
  - FILE_UPLOAD

New Messages (Add):
  - SERVICE_REGISTRY_UPDATE: Receives list of all services
  - SERVICE_ONLINE: Service came online
  - SERVICE_OFFLINE: Service went offline
  - HEARTBEAT_UPDATE: Service heartbeat received
```

---

## 📐 ARCHITECTURE

### WebSocket Connection Flow

```
React Dashboard
      ↓
Connects to: ws://localhost:7070/dashboard
      ↓
Hub Server receives connection
      ↓
Sends SERVICE_REGISTRY_UPDATE with all registered services
      ↓
Dashboard displays services
      ↓
When service joins/leaves/times out:
  Hub broadcasts update to all dashboards
```

### Service Display Format

```json
{
  "type": "SERVICE_REGISTRY_UPDATE",
  "payload": {
    "services": [
      {
        "name": "ApiGateway",
        "host": "localhost",
        "port": 9001,
        "status": "online",
        "lastHeartbeat": "2025-11-10T12:34:56Z",
        "metadata": {
          "type": "HTTP_API",
          "cpuLoad": 25
        }
      },
      {
        "name": "SecureFileService",
        "host": "localhost",
        "port": 9090,
        "status": "online",
        "lastHeartbeat": "2025-11-10T12:34:56Z",
        "metadata": {
          "type": "SSL_SERVER",
          "storageUsed": "1.2 GB"
        }
      }
    ]
  }
}
```

---

## 🔧 IMPLEMENTATION CHECKLIST

### Component Creation

- [ ] Create `ServiceDashboard.jsx`
- [ ] Create `ServiceRegistry.jsx`
- [ ] Create `ExternalDataFetcher.jsx`
- [ ] Create `ServiceDetailsPanel.jsx`
- [ ] Rename `ChatRoom.css` → `ServiceDashboard.css`

### App.jsx Refactoring

- [ ] Update WebSocket URL to Hub port (7070)
- [ ] Rename state variables (messages → services)
- [ ] Update message handlers
- [ ] Remove chat-specific logic
- [ ] Add service-specific logic

### Styling

- [ ] Update component styling for services
- [ ] Create status badge styles (online/offline/timeout)
- [ ] Add table layout CSS
- [ ] Responsive design checks

### Testing

- [ ] Test connection to Hub Server
- [ ] Test receiving SERVICE_REGISTRY_UPDATE
- [ ] Test real-time updates when services join/leave
- [ ] Test error states and disconnection
- [ ] Test UI responsiveness

---

## 📦 FILE STRUCTURE (After Phase 3)

```
frontend/src/
├── App.jsx                          (UPDATED - new message handlers)
├── index.css                        (unchanged)
├── main.jsx                         (unchanged)
├── components/
│   ├── ServiceDashboard.jsx         (NEW - replaces ChatRoom)
│   ├── ServiceDashboard.css         (NEW - styling)
│   ├── ServiceRegistry.jsx          (NEW - service list sidebar)
│   ├── ServiceDetailsPanel.jsx      (NEW - selected service details)
│   ├── ExternalDataFetcher.jsx      (NEW - weather data fetcher)
│   ├── Login.jsx                    (UPDATED - new login title)
│   ├── MessageInput.jsx             (DELETE or repurpose)
│   ├── MessagesPanel.jsx            (DELETE or repurpose)
│   ├── UploadModal.jsx              (KEEP - for file uploads)
│   └── ChatRoom.css                 (DELETE - replaced)
└── assets/
    └── ...
```

---

## 🔌 INTEGRATION POINTS

### 1. Hub Server Integration

- Hub must send WebSocket updates to all connected dashboards
- Dashboard connects to: `ws://localhost:7070/dashboard` (new endpoint)
- Receives `SERVICE_REGISTRY_UPDATE` messages

### 2. API Gateway Integration

- Dashboard can send command: `{"command": "fetchWeather", "city": "Colombo"}`
- To WebSocket: `ws://localhost:9001/api`
- Receives weather data back

### 3. File Upload (Optional Enhancement)

- Keep upload modal
- Upload to: `http://localhost:9090/upload` (Secure File Service)

---

## 🎨 UI MOCKUP

```
┌─────────────────────────────────────────────────────────┐
│         SERVICE REGISTRY DASHBOARD                      │
├─────────────────────────────────────────────────────────┤
│ Header: "Distributed Services Hub"                      │
│ Status: Connected | Disconnected                        │
├────────────────────┬──────────────────────────────────┤
│                    │                                  │
│   SERVICE LIST     │   SERVICE DETAILS PANEL          │
│   ──────────────   │   ──────────────────────────     │
│ ✓ ApiGateway       │ Service: ApiGateway              │
│ ✓ FileService      │ Host: localhost:9001             │
│ ✓ LogService       │ Status: Online ●                 │
│ ⚠ TaskService      │ CPU Load: 25%                    │
│   (timeout)        │ Last Heartbeat: 5 seconds ago    │
│                    │                                  │
│ [Refresh] [Clear]  │ Actions:                         │
│                    │ [View Logs] [Execute Task]       │
│                    │ [Upload File] [Details]          │
│                    │                                  │
├────────────────────┴──────────────────────────────────┤
│ EXTERNAL DATA: Weather for Colombo                    │
│ Temperature: 28.5°C | Condition: Sunny               │
│ [Fetch Weather] [City Input]                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 QUICK START (After Implementation)

### To Start Dashboard:

```powershell
# Terminal 1: Start Hub Server
cd services/hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar

# Terminal 2: Start API Gateway
cd services/api-gateway-service
java -jar target/api-gateway-service-1.0-SNAPSHOT.jar

# Terminal 3: Start Dashboard
cd frontend
npm run dev
# Open http://localhost:5173
```

### Expected Behavior:

1. Dashboard connects to Hub (shows "Connected")
2. ApiGateway appears in service list as "Online"
3. Click on service to view details
4. Enter city name and click "Fetch Weather"
5. Weather appears below service list

---

## 📝 COMPONENT DETAILS

### ServiceDashboard.jsx - Main Component

```jsx
export default function ServiceDashboard() {
  // State: services, selectedService, metrics
  // Connect to Hub WebSocket
  // Handle SERVICE_REGISTRY_UPDATE
  // Render: ServiceRegistry + ServiceDetailsPanel + ExternalDataFetcher
}
```

### ServiceRegistry.jsx - Service List

```jsx
export default function ServiceRegistry({
  services,
  selectedService,
  onSelect,
}) {
  // Display list of services
  // Show status badges (online/offline/timeout)
  // Handle selection
  // Show last heartbeat
}
```

### ExternalDataFetcher.jsx - Weather Widget

```jsx
export default function ExternalDataFetcher() {
  // Input: city name
  // Button: "Fetch Weather"
  // Connect to API Gateway WebSocket (port 9001)
  // Display: temperature, condition, humidity
}
```

### ServiceDetailsPanel.jsx - Service Info

```jsx
export default function ServiceDetailsPanel({ service }) {
  // Display selected service details
  // Show: name, host, port, status, metrics
  // Action buttons: view logs, execute task, upload file
}
```

---

## 🔌 HUB SERVER REQUIREMENT

Before testing Phase 3, **Hub Server MUST support**:

1. **New WebSocket Endpoint**: `/dashboard`

   - Accepts dashboard connections
   - Sends `SERVICE_REGISTRY_UPDATE` on connect
   - Broadcasts updates when services change

2. **Message Format**:

   ```json
   {
     "type": "SERVICE_REGISTRY_UPDATE",
     "payload": {
       "services": [...]
     }
   }
   ```

3. **Broadcasting Logic**:
   - When service registers: broadcast to all dashboards
   - When service deregisters: broadcast to all dashboards
   - When service times out: broadcast to all dashboards

---

## ⚠️ DEPENDENCIES & PREREQUISITES

### What's Ready (Phase 1 & 2 Complete)

- ✅ Hub Server running
- ✅ API Gateway Service running
- ✅ WebSocket support in React

### What's Needed for Phase 3

- ⚠️ Hub Server WebSocket `/dashboard` endpoint (Member 1 must add)
- ✅ Frontend React setup (already done)
- ✅ npm packages installed

### If Hub Endpoint Not Ready

- Temporarily mock the WebSocket response
- Use hardcoded service list for UI testing
- Complete integration once Hub endpoint is ready

---

## 📊 TIMELINE

| Task                   | Duration       | Owner        |
| ---------------------- | -------------- | ------------ |
| Create new components  | 2 hours        | Frontend dev |
| Update App.jsx         | 1.5 hours      | Frontend dev |
| Update styling         | 1 hour         | Frontend dev |
| Test with Hub Server   | 1 hour         | Frontend dev |
| Bug fixes & refinement | 1 hour         | Frontend dev |
| **Total**              | **~6-7 hours** |              |

---

## ✅ SUCCESS CRITERIA

Phase 3 is complete when:

- ✅ Dashboard connects to Hub WebSocket successfully
- ✅ Service list displays all registered services
- ✅ Status badges show correct online/offline status
- ✅ Real-time updates work when services join/leave
- ✅ Clicking service shows detailed information
- ✅ Weather fetcher component works
- ✅ No console errors
- ✅ Responsive on desktop and tablet
- ✅ WebSocket reconnection works on disconnect

---

## 🎓 LEARNING OUTCOMES

After Phase 3, you'll understand:

1. **React State Management**: Multiple interconnected state updates
2. **WebSocket Clients**: Handling real-time updates
3. **Component Architecture**: Separating concerns into reusable components
4. **JSON Message Handling**: Parsing and responding to different message types
5. **UI/UX Design**: Building dashboards with real data
6. **Responsive Design**: Tailwind CSS for different screen sizes

---

## 📚 NEXT STEPS

1. **Immediate**: Read this document carefully
2. **Hour 1-2**: Create new component structure
3. **Hour 3-4**: Update App.jsx with new logic
4. **Hour 5**: Style and test UI
5. **Hour 6-7**: Integration testing with Hub Server

---

**Ready to start?** Let's build the Service Registry Dashboard! 🚀

Let me know when you're ready to begin the implementation.
