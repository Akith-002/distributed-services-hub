@echo off
REM Build script for API Gateway Service
REM Usage: build.bat

echo.
echo ================================================================================
echo  Building API Gateway Service
echo ================================================================================
echo.

if not exist "pom.xml" (
    echo ERROR: pom.xml not found. Make sure you run this from the service directory.
    exit /b 1
)

echo [BUILD] Cleaning previous build...
call mvn clean

echo.
echo [BUILD] Building with Maven...
call mvn package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Build failed!
    exit /b 1
)

echo.
echo ================================================================================
echo  BUILD SUCCESSFUL
echo ================================================================================
echo.
echo JAR file created: target\api-gateway-service-1.0-SNAPSHOT.jar
echo.
echo To run the service:
echo   java -jar target\api-gateway-service-1.0-SNAPSHOT.jar
echo.
echo Make sure Hub Server is running first:
echo   cd ..\hub-server
echo   java -jar target\hub-server-1.0-SNAPSHOT.jar
echo.
pause
