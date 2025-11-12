#!/usr/bin/env powershell

# Test script to directly send "run-test" command to JSSE service via Hub TCP connection

param(
    [string]$HubHost = "localhost",
    [int]$HubPort = 7070,
    [int]$DelaySeconds = 3
)

Write-Host "JSSE Service Security Test" -ForegroundColor Green
Write-Host "===========================" -ForegroundColor Green
Write-Host ""

try {
    Write-Host "[Test] Connecting to Hub at $HubHost`:$HubPort..." -ForegroundColor Yellow
    $socket = New-Object System.Net.Sockets.TcpClient($HubHost, $HubPort)
    $stream = $socket.GetStream()
    
    # Create reader and writer
    $encoding = [System.Text.Encoding]::UTF8
    $writer = New-Object System.IO.StreamWriter($stream, $encoding)
    $reader = New-Object System.IO.StreamReader($stream, $encoding)
    
    Write-Host "[Test] ✓ Connected to Hub" -ForegroundColor Green
    
    # Send run-test command
    $command = "run-test"
    Write-Host "[Test] Sending command to service: '$command'" -ForegroundColor Yellow
    $writer.WriteLine($command)
    $writer.Flush()
    
    Write-Host "[Test] Waiting $DelaySeconds seconds for response..." -ForegroundColor Yellow
    Start-Sleep -Seconds $DelaySeconds
    
    # Try to read response (non-blocking)
    $responseLines = @()
    while ($true) {
        try {
            if ($stream.DataAvailable) {
                $line = $reader.ReadLine()
                if ($null -ne $line) {
                    $responseLines += $line
                    Write-Host "[Response] $line" -ForegroundColor Cyan
                } else {
                    break
                }
            } else {
                break
            }
        } catch {
            break
        }
    }
    
    if ($responseLines.Count -eq 0) {
        Write-Host "[Test] No response received (service may be processing)" -ForegroundColor Yellow
    }
    
    Write-Host "[Test] Test completed" -ForegroundColor Green
    $socket.Close()
    
} catch {
    Write-Host "[Error] $($_)" -ForegroundColor Red
}
