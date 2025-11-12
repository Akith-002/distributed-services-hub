@echo off
echo Building NIO Log Service...
cd /d "%~dp0"
call mvn clean package -DskipTests
if %errorlevel% equ 0 (
    echo Build successful!
    echo JAR location: target\nio-log-service-1.0-SNAPSHOT.jar
) else (
    echo Build failed!
)
pause
