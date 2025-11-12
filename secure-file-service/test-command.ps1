#!/usr/bin/env powershell

# Test script to send "run-test" command to Secure File Service via Hub

$hubHost = "localhost"
$hubPort = 7070

Write-Host "Connecting to Hub at $hubHost`:$hubPort..."

try {
    $socket = New-Object System.Net.Sockets.TcpClient($hubHost, $hubPort)
    $stream = $socket.GetStream()
    $writer = New-Object System.IO.StreamWriter($stream)
    $reader = New-Object System.IO.StreamReader($stream)

    # Send run-test command to the service
    $command = "run-test"
    Write-Host "Sending command: $command"
    $writer.WriteLine($command)
    $writer.Flush()

    # Wait for response
    Write-Host "Waiting for response..."
    Start-Sleep -Seconds 5

    # Read any response
    if ($stream.DataAvailable) {
        $response = $reader.ReadLine()
        Write-Host "Response: $response"
    } else {
        Write-Host "No response received"
    }

    $socket.Close()
    Write-Host "Test completed"
} catch {
    Write-Host "Error: $_"
}
