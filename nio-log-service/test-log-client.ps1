# Test NIO Log Service by sending log messages
Write-Host "Testing NIO Log Service..." -ForegroundColor Cyan

$logServerHost = "localhost"
$logServerPort = 9091

try {
    $client = New-Object System.Net.Sockets.TcpClient($logServerHost, $logServerPort)
    $stream = $client.GetStream()
    $writer = New-Object System.IO.StreamWriter($stream)
    $writer.AutoFlush = $true

    Write-Host "Connected to NIO Log Service at ${logServerHost}:${logServerPort}" -ForegroundColor Green
    Write-Host ""

    # Send test log messages
    $testLogs = @(
        "API_GATEWAY: Weather fetched for Colombo (28.5°C)",
        "SECURE_FILE_SERVICE: File test.txt stored (1024 bytes)",
        "HUB: Service 'API_GATEWAY' registered",
        "API_GATEWAY: HttpURLConnection successful",
        "SECURE_FILE_SERVICE: SSL handshake completed"
    )

    foreach ($log in $testLogs) {
        Write-Host "Sending: $log" -ForegroundColor Yellow
        $writer.WriteLine($log)
        Start-Sleep -Milliseconds 500
    }

    Write-Host ""
    Write-Host "Test logs sent successfully!" -ForegroundColor Green
    Write-Host "Check the logs/ directory for output file" -ForegroundColor Cyan
    Write-Host "Check the Hub Dashboard for real-time log display" -ForegroundColor Cyan

    $writer.Close()
    $stream.Close()
    $client.Close()

} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "Make sure NIO Log Service is running on port 9091" -ForegroundColor Yellow
}
