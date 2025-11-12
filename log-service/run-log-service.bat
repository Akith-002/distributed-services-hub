@echo off
REM Run Script for Log Service (Batch)
REM Member 4 - High-Performance Log Service

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║        Starting Log Service (Member 4)                         ║
echo ║        Java NIO Selector-based Server                          ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Check if compiled
if not exist "target\classes\com\example\logservice\LogServer.class" (
    echo ❌ Log Service not compiled!
    echo Run build.bat first
    exit /b 1
)

echo 🚀 Launching Log Service on port 9091...
echo 📝 Log file: logs\system.log
echo Press Ctrl+C to stop
echo.

REM Run the log server
mvn exec:java -Dexec.mainClass="com.example.logservice.LogServer" -q
