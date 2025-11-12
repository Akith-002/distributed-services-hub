# Simple WebSocket file upload test
Write-Host "Testing WebSocket file upload to API Gateway..." -ForegroundColor Cyan

# Create test file
$testContent = "Hello from PowerShell WebSocket test!"
$testFileName = "powershell-test.txt"
Set-Content -Path $testFileName -Value $testContent
Write-Host "Created test file: $testFileName"

# Read and encode file
$fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $testFileName))
$base64Content = [Convert]::ToBase64String($fileBytes)

Write-Host "File size: $($fileBytes.Length) bytes"

# Create WebSocket client
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$uri = "ws://localhost:9001/api"
$ct = New-Object System.Threading.CancellationToken

Write-Host "Connecting to $uri..."

try {
    $connectTask = $ws.ConnectAsync($uri, $ct)
    $connectTask.Wait()
    
    if ($ws.State -eq 'Open') {
        Write-Host "Connected!" -ForegroundColor Green
        
        Start-Sleep -Milliseconds 500
        
        $uploadCommand = @{
            command = "uploadFile"
            fileName = $testFileName
            fileData = $base64Content
        } | ConvertTo-Json -Compress
        
        Write-Host "Sending upload command..."
        
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($uploadCommand)
        $segment = New-Object System.ArraySegment[byte] -ArgumentList @(,$bytes)
        $sendTask = $ws.SendAsync($segment, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $ct)
        $sendTask.Wait()
        
        Write-Host "Command sent! Waiting for response (15 seconds max)..."
        
        $buffer = New-Object byte[] 8192
        $segment = New-Object System.ArraySegment[byte] -ArgumentList @(,$buffer)
        $receiveTask = $ws.ReceiveAsync($segment, $ct)
        
        if ($receiveTask.Wait(15000)) {
            $result = $receiveTask.Result
            $response = [System.Text.Encoding]::UTF8.GetString($buffer, 0, $result.Count)
            
            Write-Host "=== RESPONSE ===" -ForegroundColor Green
            Write-Host $response
            Write-Host "================" -ForegroundColor Green
            
            $responseObj = $response | ConvertFrom-Json
            
            if ($responseObj.type -eq "FILE_UPLOAD_SUCCESS") {
                Write-Host "SUCCESS!" -ForegroundColor Green
            } else {
                Write-Host "ERROR: $($responseObj.error)" -ForegroundColor Red
            }
        } else {
            Write-Host "TIMEOUT! No response after 15 seconds" -ForegroundColor Red
        }
    } else {
        Write-Host "Failed to connect" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    if ($ws.State -eq 'Open') {
        $closeTask = $ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "Done", $ct)
        $closeTask.Wait()
    }
    $ws.Dispose()
    Remove-Item $testFileName -ErrorAction SilentlyContinue
}

Write-Host "Test complete"
