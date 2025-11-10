@echo off
REM Set Java 17 for this session
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.12
set Path=%JAVA_HOME%\bin;%Path%

echo.
echo Starting Hub Server...
echo.

java -jar target/hub-server-1.0-SNAPSHOT.jar

pause
