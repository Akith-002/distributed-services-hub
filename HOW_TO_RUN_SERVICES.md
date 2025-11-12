# ✅ HOW TO RUN HUB SERVER (Java Project)

**Issue:** Trying to use `npm run dev` on a Java/Maven project  
**Solution:** Use Java to run the compiled JAR file

---

## 🎯 CORRECT COMMAND

### **Hub Server is a JAVA project, not Node.js!**

**❌ WRONG:**
```powershell
npm run dev  # This is for Node.js projects!
```

**✅ CORRECT:**
```powershell
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

---

## 🚀 HOW TO RUN HUB SERVER

### **Option 1: Run the JAR file directly** (Recommended)

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### **Option 2: Build and run** (if JAR doesn't exist)

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
mvn clean package
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### **Option 3: Run in new window** (keeps terminal free)

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/hub-server-1.0-SNAPSHOT.jar"
```

---

## 📊 PROJECT TYPES IN YOUR WORKSPACE

| Service | Technology | Run Command |
|---------|-----------|-------------|
| **Hub Server** | Java/Maven | `java -jar target/hub-server-1.0-SNAPSHOT.jar` |
| **API Gateway** | Java/Maven | `java -jar target/api-gateway-service-1.0-SNAPSHOT.jar` |
| **Secure File Service** | Java/Maven | `java -jar target/secure-file-service-1.0-SNAPSHOT.jar` |
| **React Frontend** | Node.js/Vite | `npm run dev` ← **ONLY this one uses npm!** |

---

## ✅ COMPLETE STARTUP SEQUENCE

### **Start All Services (Correct Order):**

#### **1. Hub Server** (Java)
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/hub-server-1.0-SNAPSHOT.jar"
```

#### **2. API Gateway** (Java)
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\api-gateway-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/api-gateway-service-1.0-SNAPSHOT.jar"
```

#### **3. Secure File Service** (Java)
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\secure-file-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/secure-file-service-1.0-SNAPSHOT.jar"
```

#### **4. React Frontend** (Node.js) ← **ONLY ONE using npm!**
```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\multi-client-chat-frontend"
npm run dev
```

---

## 🔍 WHY THE ERROR HAPPENED

### **Understanding the Error:**

```
npm ERR! enoent Could not read package.json
```

**Explanation:**
- `npm` is for **Node.js projects** (JavaScript/TypeScript)
- `package.json` is a **Node.js** configuration file
- **Hub Server** is a **Java project** (uses `pom.xml`, not `package.json`)
- Java projects use **Maven** (`mvn`) or **Gradle**, not npm

**File Structure:**
```
hub-server/
├── pom.xml          ← Maven config (Java)
├── src/
│   └── main/java/   ← Java source code
└── target/
    └── hub-server-1.0-SNAPSHOT.jar  ← Compiled Java application

multi-client-chat-frontend/
├── package.json     ← npm config (Node.js)
├── src/
│   └── *.jsx        ← React source code
└── node_modules/    ← npm dependencies
```

---

## ⚠️ COMMON MISTAKES

### **Mistake 1: Using npm on Java projects**
```powershell
# ❌ WRONG
cd hub-server
npm run dev  # Error! No package.json

# ✅ CORRECT
cd hub-server
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

### **Mistake 2: Using java on Node.js projects**
```powershell
# ❌ WRONG
cd multi-client-chat-frontend
java -jar ...  # Error! Not a Java project

# ✅ CORRECT
cd multi-client-chat-frontend
npm run dev
```

---

## 🎯 QUICK REFERENCE

### **Run Hub Server Now:**

```powershell
cd "c:\Users\Mandrini Yashodha\Desktop\distributed-services-hub\hub-server"
java -jar target/hub-server-1.0-SNAPSHOT.jar
```

**You should see:**
```
[main] INFO io.javalin.Javalin - Starting Javalin ...
[main] INFO io.javalin.Javalin - Javalin started in XXXms
[main] INFO - Hub Server started on port 7070 (TCP) and 7071 (HTTP)
```

---

## ✅ VERIFICATION

After starting Hub Server, verify it's running:

```powershell
# Check ports
netstat -ano | findstr ":7070 :7071"

# Check HTTP endpoint
curl http://localhost:7071/hub-status
```

**Expected Output:**
```json
{
  "server": "Distributed Services Hub",
  "status": "Running",
  "tcpPort": 7070,
  "httpPort": 7071
}
```

---

## 📚 SUMMARY

**Remember:**
- 🔵 **Java/Maven Projects** → `java -jar target/*.jar`
- 🟢 **Node.js Projects** → `npm run dev`

**In your workspace:**
- 🔵 Hub Server = Java
- 🔵 API Gateway = Java
- 🔵 Secure File Service = Java
- 🟢 React Frontend = Node.js

---

**Now you can run the Hub Server correctly!** 🚀
