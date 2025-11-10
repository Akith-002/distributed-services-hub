# 📖 PHASE 3 - VISUAL REFERENCE GUIDE

**A Quick Reference for All Phase 3 Components and Features**

---

## 🎨 COMPONENT HIERARCHY

```
┌─────────────────────────────────────────────────────────────┐
│                     App.jsx                                 │
│        (Entry point - renders ServiceDashboard)             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ↓
         ┌─────────────────────────────────────┐
         │   ServiceDashboard.jsx              │
         │   (Main Dashboard Component)        │
         │   • WebSocket to Hub (7070)         │
         │   • State management                │
         │   • Message routing                 │
         └─────────┬──────────┬────────────────┘
                   │          │
        ┌──────────┴──────┐   │
        │                 │   │
        ↓                 ↓   ↓
    ┌─────────────┐  ┌──────────────────┐  ┌──────────────┐
    │ ServiceReg. │  │ ServiceDetailsPan│  │ ExternalData │
    │ .jsx        │  │ el.jsx           │  │ Fetcher.jsx  │
    │             │  │                  │  │              │
    │ • Sidebar   │  │ • Details panel  │  │ • Weather WS │
    │ • Services  │  │ • Metadata       │  │ • City input │
    │ • Status    │  │ • Action buttons │  │ • Weather    │
    │ • Selection │  │                  │  │   display    │
    └─────────────┘  └──────────────────┘  └──────────────┘
```

---

## 📂 FILE LOCATIONS

```
frontend/src/
├── App.jsx                                    ← Entry point
├── components/
│   ├── ServiceDashboard.jsx                  ← NEW Main component
│   ├── ServiceRegistry.jsx                   ← NEW Sidebar
│   ├── ServiceDetailsPanel.jsx               ← NEW Details
│   ├── ExternalDataFetcher.jsx               ← NEW Weather
│   ├── Login.jsx                             ← Updated for dashboard
│   └── [Legacy components - kept for reference]
│       ├── ChatRoom.jsx
│       ├── MessageInput.jsx
│       ├── MessagesPanel.jsx
│       └── UploadModal.jsx
└── main.jsx                                  ← React entry point
```

---

## 🔌 CONNECTIVITY DIAGRAM

```
BROWSER (http://localhost:5173)
    │
    │ React App loads
    │
    ↓
┌─────────────────────────┐
│  Login Screen (React)   │
│                         │
│ [Dashboard name input]  │
│ [Connect to Hub button] │
└───────────┬─────────────┘
            │ User enters name & clicks button
            ↓
┌─────────────────────────────────────────────────────────────┐
│  ServiceDashboard.jsx                                       │
│  • Creates WebSocket connection                             │
│  • ws://localhost:7070/dashboard                            │
└──────┬──────────────────────────────────────────────────────┘
       │
       │ WebSocket opened
       │
       ↓ (Connected to Hub)
┌─────────────────────────┐
│   Hub Server            │
│   (Java - Port 7070)    │
│   (Phase 1 - Complete)  │
└──┬──────────────────────┘
   │
   ├─ Sends: SERVICE_REGISTRY_UPDATE
   │
   ↓ (Message received by Dashboard)
┌─────────────────────────────────────────────────────────────┐
│  ServiceRegistry.jsx                                        │
│  • Displays list of services                                │
│  • Shows status badges                                      │
│  • Allows service selection                                 │
└─────────────────────────────────────────────────────────────┘
   │
   └─ User clicks on "ApiGateway" service
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  ServiceDetailsPanel.jsx                                    │
│  • Shows selected service details                           │
│  • Host: localhost, Port: 9001, Status: Online             │
└─────────────────────────────────────────────────────────────┘
   │
   └─ User enters "Colombo" in weather input & clicks Fetch
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  ExternalDataFetcher.jsx                                    │
│  • Creates WebSocket to ws://localhost:9001/api             │
└──┬───────────────────────────────────────────────────────────┘
   │
   │ WebSocket opened to API Gateway
   │
   ↓ (Connected to API Gateway)
┌─────────────────────────┐
│   API Gateway           │
│   (Java - Port 9001)    │
│   (Phase 2 - Complete)  │
└──┬──────────────────────┘
   │
   │ fetchWeather("Colombo")
   │
   ├─ Makes HttpURLConnection call
   │  to external weather API
   │
   ↓ (Gets temperature, condition, etc.)
│
├─ Sends weather back to Dashboard
│  {temperature: 28.5, condition: "Sunny", ...}
│
↓
Display weather on screen
```

---

## 🎨 UI LAYOUT DIAGRAM

```
┌───────────────────────────────────────────────────────────────┐
│  Header: Distributed Services Hub  [Monitor 1] [SSL] [Logout] │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐  ┌──────────────────────────────────┐  │
│  │  SERVICE         │  │                                  │  │
│  │  REGISTRY        │  │   SERVICE DETAILS PANEL          │  │
│  │  (Sidebar)       │  │                                  │  │
│  │                  │  │                                  │  │
│  │ ✓ ApiGateway     │  │   ApiGateway                     │  │
│  │   Online         │  │   ─────────────────────          │  │
│  │   Port: 9001     │  │   Status: Online ✓               │  │
│  │   5s ago         │  │                                  │  │
│  │                  │  │   Connection Information         │  │
│  │ ✓ FileService    │  │   Host: localhost               │  │
│  │   Online         │  │   Port: 9001                    │  │
│  │   Port: 9090     │  │                                  │  │
│  │   5s ago         │  │   Heartbeat Information         │  │
│  │                  │  │   Last: 12:34:56 PM            │  │
│  │ 2 services      │  │                                  │  │
│  │ connected        │  │   Service Metadata              │  │
│  │                  │  │   Type: HTTP_API                │  │
│  │                  │  │   CPU: 25%                      │  │
│  │                  │  │                                  │  │
│  │                  │  │   [View Logs] [Details] [Refresh]  │  │
│  └──────────────────┘  └──────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  EXTERNAL DATA FETCHER - WEATHER                         │  │
│  │                                                          │  │
│  │  [City: Colombo] [Fetch]                                │  │
│  │                                                          │  │
│  │  Temperature: 28.5°C    Condition: Sunny                │  │
│  │  Humidity: 72%          Wind Speed: 15 km/h             │  │
│  │                                                          │  │
│  │  Data Source: API Gateway (HttpURLConnection)           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                               │
├───────────────────────────────────────────────────────────────┤
│  ● Connected | 2 services registered | Port: 7070/7443      │
└───────────────────────────────────────────────────────────────┘
```

---

## 📊 STATE MANAGEMENT

```
ServiceDashboard.jsx (State Container)
│
├─ services: []                    ← All registered services
│  [{name: "ApiGateway", host: "localhost", port: 9001, ...}]
│
├─ selectedService: null           ← Currently selected service
│  {name: "ApiGateway", status: "online", ...}
│
├─ isConnected: false              ← Hub connection status
│  true/false
│
├─ username: ""                    ← Dashboard name
│  "Monitor 1"
│
├─ useSSL: true                    ← SSL/TLS enabled?
│  true/false
│
├─ error: null                     ← Error message
│  "Connection timeout..."
│
└─ Callbacks:
   ├─ connectWebSocket(name)       ← Connect to Hub
   ├─ handleDisconnect()           ← Disconnect
   ├─ handleServiceAction()        ← Service actions
   └─ WebSocket handlers
      ├─ onopen
      ├─ onmessage
      ├─ onclose
      └─ onerror
```

---

## 🔄 MESSAGE FLOW SEQUENCE

```
TIME    ACTOR               ACTION
────────────────────────────────────────────────────────────

T0      User               Enters dashboard name "Monitor 1"
T0      User               Clicks "Connect to Hub"
T1      ServiceDashboard   Creates WebSocket connection
        (React)
T2      WebSocket          Connection opens to Hub:7070
T3      ServiceDashboard   Sends DASHBOARD_CONNECT message
        (React)
T4      Hub Server         Receives connection
        (Java)
T5      Hub Server         Sends SERVICE_REGISTRY_UPDATE
        (Java)             (all registered services)
T6      ServiceDashboard   Receives services list
        (React)
T7      ServiceRegistry    Updates UI with services
        (React)
T8      User               Sees services in sidebar
        (Browser)
T9      User               Clicks "ApiGateway" service
        (Browser)
T10     ServiceDetailsPanel Shows service details
        (React)
T11     User               Enters "Colombo" in weather input
        (Browser)
T12     User               Clicks "Fetch" button
        (Browser)
T13     ExternalDataFetcher Creates WebSocket to API Gateway:9001
        (React)
T14     WebSocket          Connection opens to API Gateway
T15     ExternalDataFetcher Sends weather command
        (React)
T16     API Gateway        Receives command
        (Java)
T17     API Gateway        Makes HttpURLConnection call
        (Java)             to external weather API
T18     External API       Returns weather data
        (Internet)
T19     API Gateway        Sends weather back via WebSocket
        (Java)
T20     ExternalDataFetcher Receives weather data
        (React)
T21     Weather Widget     Displays temperature, condition
        (React)
T22     User               Sees weather on screen
        (Browser)
```

---

## 🎯 KEY FEATURES MAP

```
FEATURE                    COMPONENT              TECHNOLOGY
───────────────────────────────────────────────────────────────
Real-time service list     ServiceRegistry        WebSocket → Hub
Service selection          ServiceRegistry        React onClick
Service details            ServiceDetailsPanel    Props passing
Status badges              ServiceDetailsPanel    Tailwind CSS
Weather display            ExternalDataFetcher    JSON parsing
City input                 ExternalDataFetcher    React input
Auto-reconnection          ServiceDashboard       setTimeout loop
Error messages             ServiceDashboard       Error state
Connection indicator       Footer                 isConnected state
Responsive layout          All                    Tailwind CSS
Loading animations         ExternalDataFetcher    Lucide icons
```

---

## 🔐 PORTS REFERENCE

```
SERVICE              PORT    PROTOCOL    USE CASE
──────────────────────────────────────────────────────
React Dashboard      5173    HTTP        Vite dev server
Hub Server           7070    WebSocket   Service registry
Hub Server SSL       7443    WebSocket   Secure registry
API Gateway          9001    WebSocket   Weather commands
Secure File Svc      9090    SSL         File transfer (Phase 4)
NIO Log Service      9091    NIO         Logging (Phase 5)
RMI Task Service     1099    RMI         Remote tasks (Phase 6)
```

---

## 🧩 COMPONENT PROPS

```
ServiceDashboard.jsx
  └─ No props (top-level component)

ServiceRegistry.jsx
  ├─ services: Array<ServiceInfo>           ← Service objects
  ├─ selectedService: ServiceInfo | null    ← Selected service
  ├─ onSelectService: Function              ← Selection callback
  └─ isConnected: Boolean                   ← Connection status

ServiceDetailsPanel.jsx
  ├─ service: ServiceInfo | null            ← Service to display
  └─ onAction: Function                     ← Action callback

ExternalDataFetcher.jsx
  └─ No props (self-contained)

Login.jsx
  ├─ username: String
  ├─ setUsername: Function
  ├─ useSSL: Boolean
  ├─ setUseSSL: Function
  ├─ error: String | null
  ├─ onJoin: Function
  └─ isDashboard: Boolean (new)
```

---

## 📈 EVENT FLOW DIAGRAM

```
User Action           Component            State Update         UI Update
─────────────────────────────────────────────────────────────────────────
Enter name            Login                username = "Monitor"  Text appears
Click Connect         Login                isConnected = true    Page changes
Message from Hub      ServiceDashboard     services = [...]      List appears
Click service         ServiceRegistry      selectedService = X   Details show
Enter city            ExternalDataFetcher  city = "Colombo"      Text appears
Click Fetch           ExternalDataFetcher  loading = true        Spinner shows
Weather received      ExternalDataFetcher  weatherData = {...}   Data shows
Connection lost       ServiceDashboard     isConnected = false   Error shows
Reconnect attempt     ServiceDashboard     error = "Reconnecting" Message shows
Reconnect success     ServiceDashboard     isConnected = true    Clears error
```

---

## 🎓 COMPONENT RESPONSIBILITIES

```
ServiceDashboard.jsx
├─ WebSocket connection management
├─ Message routing and handling
├─ State management (services, selectedService, etc.)
├─ Reconnection logic
├─ Error handling
└─ Orchestration of sub-components

ServiceRegistry.jsx
├─ Display service list
├─ Show service status
├─ Handle service selection
├─ Format heartbeat times
└─ Show connection indicator

ServiceDetailsPanel.jsx
├─ Display service information
├─ Format metadata
├─ Show status badges
├─ Provide action buttons
└─ Handle action callbacks

ExternalDataFetcher.jsx
├─ WebSocket to API Gateway
├─ Send weather requests
├─ Parse weather response
├─ Display weather data
├─ Handle loading state
└─ Handle error state
```

---

## 🚀 EXECUTION FLOW

```
1. User opens browser
   ↓
2. React loads, App.jsx renders ServiceDashboard
   ↓
3. ServiceDashboard mounts, shows Login component
   ↓
4. User enters dashboard name and clicks "Connect to Hub"
   ↓
5. ServiceDashboard.connectWebSocket() called
   ↓
6. WebSocket created to ws://localhost:7070/dashboard
   ↓
7. WebSocket onopen event fires
   ↓
8. DASHBOARD_CONNECT message sent to Hub
   ↓
9. Hub responds with SERVICE_REGISTRY_UPDATE
   ↓
10. ServiceDashboard.onmessage() handles update
    ↓
11. setServices() called, re-renders ServiceRegistry
    ↓
12. Services appear in sidebar
    ↓
13. User clicks service, setSelectedService() called
    ↓
14. ServiceDetailsPanel re-renders with new service
    ↓
15. User enters city and clicks Fetch Weather
    ↓
16. ExternalDataFetcher.handleFetchWeather() called
    ↓
17. New WebSocket to ws://localhost:9001/api
    ↓
18. Weather command sent to API Gateway
    ↓
19. API Gateway fetches weather, sends response
    ↓
20. ExternalDataFetcher receives data
    ↓
21. setWeatherData() called
    ↓
22. Weather displays on screen
```

---

## 💾 Data Structures

```typescript
// ServiceInfo
{
  name: string;              // "ApiGateway"
  host: string;              // "localhost"
  port: number;              // 9001
  status: string;            // "online" | "offline" | "timeout"
  lastHeartbeat: string;     // ISO timestamp
  metadata?: {
    type?: string;           // "HTTP_API"
    cpuLoad?: number;        // 25 (percent)
    [key: string]: any;
  };
}

// Message Types
{
  type: "SERVICE_REGISTRY_UPDATE";
  payload: {
    services: ServiceInfo[];
  };
}

// Weather Response
{
  success: boolean;
  data?: {
    temperature: number;
    condition: string;
    humidity?: number;
    windSpeed?: number;
  };
  error?: string;
}
```

---

## 📋 TESTING SCENARIOS

```
Scenario 1: Dashboard Connects
- Start Hub Server
- Open dashboard
- Click Connect
- Verify: No errors, connected status shows

Scenario 2: Services Appear
- Hub Server running
- API Gateway running
- Open dashboard
- Verify: ApiGateway appears in list after ~10s

Scenario 3: Service Details Display
- Services showing in list
- Click on ApiGateway
- Verify: Details panel shows information

Scenario 4: Weather Fetching
- Service selected
- Enter "Colombo" in city input
- Click Fetch
- Verify: Weather data appears

Scenario 5: Error Handling
- Stop Hub Server
- Dashboard shows disconnect error
- Start Hub Server again
- Dashboard auto-reconnects

Scenario 6: Responsive Design
- Resize browser window
- Verify: Layout adapts (flex/grid)
- Check: All elements visible
```

---

This visual reference guide covers all aspects of Phase 3 implementation.  
Use this as a quick lookup while testing and development! 🚀
