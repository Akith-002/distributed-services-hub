package com.example.fileservice;

/**
 * Interface for handling commands received from Hub
 * Member 3 - Secure File Service UI Integration
 */
public interface CommandListener {
    /**
     * Called when a command is received from the Hub
     * 
     * @param command The command string received from Hub
     */
    void onCommand(String command);
}
