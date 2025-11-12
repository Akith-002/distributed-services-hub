@echo off
echo Building RMI Task Service...
cd /d "%~dp0"
call mvn clean package -DskipTests
if %errorlevel% equ 0 (
    echo Build successful!
    echo JAR location: target\rmi-task-service-1.0-SNAPSHOT.jar
) else (
    echo Build failed!
)
pause
