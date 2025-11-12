# Test WebSocket file upload to API Gateway
# This bypasses the React frontend to test if the backend works

Write-Host "Testing WebSocket file upload to API Gateway..." -ForegroundColor Cyan
Write-Host ""

# Create a test file
$testContent = "Hello from PowerShell WebSocket test!`nThis is a test file uploaded via WebSocket."
$testFileName = "powershell-test.txt"
Set-Content -Path $testFileName -Value $testContent
Write-Host "Created test file: $testFileName" -ForegroundColor Green

# Read file content and encode to base64
$fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $testFileName))
$base64Content = [Convert]::ToBase64String($fileBytes)

Write-Host "File size: $($fileBytes.Length) bytes" -ForegroundColor Yellow
Write-Host ""

# Create WebSocket client
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$uri = "ws://localhost:9001/api"
$ct = New-Object System.Threading.CancellationToken

Write-Host "Connecting to API Gateway at $uri..." -ForegroundColor Cyan

try {
    # Connect
    $connectTask = $ws.ConnectAsync($uri, $ct)
    $connectTask.Wait()
    
    if ($ws.State -eq 'Open') {
        Write-Host "✓ Connected successfully!" -ForegroundColor Green
        Write-Host ""
        
        # Wait a moment for connection to stabilize
        Start-Sleep -Milliseconds 500
        
        # Create upload command
        $uploadCommand = @{
            command = "uploadFile"
            fileName = $testFileName
            fileData = $base64Content
        } | ConvertTo-Json -Compress
        
        Write-Host "Sending upload command..." -ForegroundColor Cyan
        Write-Host "Command: uploadFile" -ForegroundColor Yellow
        Write-Host "FileName: $testFileName" -ForegroundColor Yellow
        Write-Host "Data size: $($base64Content.Length) characters (base64)" -ForegroundColor Yellow
        Write-Host ""
        
        # Send the command
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($uploadCommand)
        $segment = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
        $sendTask = $ws.SendAsync($segment, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $ct)
        $sendTask.Wait()
        
        Write-Host "✓ Upload command sent!" -ForegroundColor Green
        Write-Host "Waiting for response..." -ForegroundColor Cyan
        Write-Host ""
        
        # Receive response
        $buffer = New-Object byte[] 8192
        $segment = New-Object System.ArraySegment[byte] -ArgumentList @(,$buffer)
        
        $receiveTask = $ws.ReceiveAsync($segment, $ct)
        
        # Wait up to 15 seconds for response
        $timeout = 15000
        if ($receiveTask.Wait($timeout)) {
            $result = $receiveTask.Result
            $response = [System.Text.Encoding]::UTF8.GetString($buffer, 0, $result.Count)
            
            Write-Host "=== RESPONSE RECEIVED ===" -ForegroundColor Green
            Write-Host $response -ForegroundColor White
            Write-Host "=========================" -ForegroundColor Green
            Write-Host ""
            
            # Parse response
            $responseObj = $response | ConvertFrom-Json
            
            if ($responseObj.type -eq "FILE_UPLOAD_SUCCESS") {
                Write-Host "✓✓✓ SUCCESS! File uploaded successfully!" -ForegroundColor Green
                Write-Host "Message: $($responseObj.message)" -ForegroundColor Green
            } elseif ($responseObj.type -eq "ERROR") {
                Write-Host "✗✗✗ ERROR: $($responseObj.error)" -ForegroundColor Red
            } else {
                Write-Host "Unexpected response type: $($responseObj.type)" -ForegroundColor Yellow
            }
        } else {
            Write-Host "✗✗✗ TIMEOUT: No response received after $timeout ms" -ForegroundColor Red
            Write-Host "This means the backend is hanging!" -ForegroundColor Red
        }
        
    } else {
        Write-Host "✗ Failed to connect. State: $($ws.State)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗✗✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host $_.Exception.StackTrace -ForegroundColor DarkRed
} finally {
    if ($ws.State -eq 'Open') {
        Write-Host ""
        Write-Host "Closing WebSocket connection..." -ForegroundColor Cyan
        $closeTask = $ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "Test complete", $ct)
        $closeTask.Wait()
    }
    $ws.Dispose()
    
    # Clean up test file
    Remove-Item $testFileName -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Test complete!" -ForegroundColor Cyan
