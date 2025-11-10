# Test script for API Gateway Service endpoints
# Run this after starting both Hub Server and API Gateway Service

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "API GATEWAY SERVICE - ENDPOINT TESTS"
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: API Gateway Health
Write-Host "Test 1: API Gateway Health Check" -ForegroundColor Yellow
Write-Host "URL: http://localhost:9001/health"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9001/health" -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) OK" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: API Gateway Status
Write-Host "Test 2: API Gateway Status" -ForegroundColor Yellow
Write-Host "URL: http://localhost:9001/status"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9001/status" -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) OK" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Hub Server Status
Write-Host "Test 3: Hub Server Status" -ForegroundColor Yellow
Write-Host "URL: http://localhost:7071/hub-status"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:7071/hub-status" -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) OK" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Hub Services List
Write-Host "Test 4: Hub Services Registry" -ForegroundColor Yellow
Write-Host "URL: http://localhost:7071/services"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:7071/services" -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) OK" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $services = $response.Content | ConvertFrom-Json
    $services.services | ForEach-Object {
        Write-Host "  • Name: $($_.name)" -ForegroundColor Green
        Write-Host "    Host: $($_.host):$($_.port)" -ForegroundColor Green
        Write-Host "    Status: $($_.status)" -ForegroundColor Green
        Write-Host "    Registered: $($_.registered)" -ForegroundColor Green
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: WebSocket Connection Test
Write-Host "Test 5: WebSocket Connection Test" -ForegroundColor Yellow
Write-Host "URL: ws://localhost:9001/api" -ForegroundColor Yellow
try {
    $ws = New-Object System.Net.WebSockets.ClientWebSocket
    $uri = [uri]"ws://localhost:9001/api"
    $cancellation = New-Object System.Threading.CancellationToken
    
    # Connect with timeout
    $task = $ws.ConnectAsync($uri, $cancellation)
    $task.Wait([System.TimeSpan]::FromSeconds(5))
    
    if ($ws.State -eq 'Open') {
        Write-Host "✓ WebSocket Connected Successfully" -ForegroundColor Green
        
        # Send ping
        $pingMessage = '{"command": "ping"}'
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($pingMessage)
        $ws.SendAsync([System.ArraySegment[byte]]$bytes, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $cancellation).Wait()
        
        # Receive response
        $buffer = New-Object System.ArraySegment[byte] -ArgumentList (New-Object byte[] 1024)
        $receiveTask = $ws.ReceiveAsync($buffer, $cancellation)
        $receiveTask.Wait([System.TimeSpan]::FromSeconds(5))
        
        $response = [System.Text.Encoding]::UTF8.GetString($buffer.Array, 0, $receiveTask.Result.Count)
        Write-Host "Sent: $pingMessage" -ForegroundColor Cyan
        Write-Host "Received: $response" -ForegroundColor Green
        
        # Close connection
        $ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::Normal, "Closed", $cancellation).Wait()
        Write-Host "✓ WebSocket Closed Successfully" -ForegroundColor Green
    } else {
        Write-Host "✗ WebSocket Connection Failed - State: $($ws.State)" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "TESTS COMPLETED"
Write-Host "======================================================================" -ForegroundColor Cyan
