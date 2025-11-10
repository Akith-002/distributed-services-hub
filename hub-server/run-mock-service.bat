@echo off
REM Set Java 17 for this session
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.12
set Path=%JAVA_HOME%\bin;%Path%

echo.
echo Starting Mock Service Client...
echo Service Name: %1
echo Service Port: %2
echo.

java -cp target/hub-server-1.0-SNAPSHOT.jar com.example.hub.MockServiceClient %1 %2

pause
