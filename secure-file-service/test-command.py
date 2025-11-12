#!/usr/bin/env python3

import json
import websocket
import time
import sys

def on_message(ws, message):
    print(f"[WebSocket] Received: {message}")

def on_error(ws, error):
    print(f"[WebSocket] Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("[WebSocket] Closed")

def on_open(ws):
    print("[WebSocket] Connected to Hub")
    
    # Send the run-test command for JSSE service
    command = {
        "command_for": "SecureFileService",
        "payload": "run-test"
    }
    
    print(f"[WebSocket] Sending command: {json.dumps(command)}")
    ws.send(json.dumps(command))
    
    # Wait for responses
    time.sleep(10)
    ws.close()

if __name__ == "__main__":
    # Connect to Hub WebSocket
    ws_url = "ws://localhost:7071/registry"
    print(f"[Main] Connecting to {ws_url}")
    
    ws = websocket.WebSocketApp(ws_url,
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    
    ws.run_forever()
