package com.example.logservice;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Handles writing logs to files with rotation support
 */
public class LogWriter {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private BufferedWriter currentWriter;
    private String currentLogFile;

    public LogWriter() {
        try {
            // Create logs directory if it doesn't exist
            Files.createDirectories(Paths.get(LOG_DIR));
            openLogFile();
        } catch (IOException e) {
            System.err.println("[LOG_WRITER] Error initializing: " + e.getMessage());
        }
    }

    /**
     * Write log entry to file
     */
    public synchronized void writeLog(String logEntry) {
        try {
            // Check if we need to rotate (new day)
            String expectedFile = getLogFileName();
            if (!expectedFile.equals(currentLogFile)) {
                rotateLogFile();
            }

            if (currentWriter != null) {
                currentWriter.write(logEntry);
                currentWriter.newLine();
                currentWriter.flush(); // Ensure data is written immediately
            }
        } catch (IOException e) {
            System.err.println("[LOG_WRITER] Error writing log: " + e.getMessage());
        }
    }

    /**
     * Get current log file name based on date
     */
    private String getLogFileName() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return LOG_DIR + File.separator + "service-" + date + ".log";
    }

    /**
     * Open a new log file
     */
    private void openLogFile() throws IOException {
        currentLogFile = getLogFileName();
        currentWriter = new BufferedWriter(
            new FileWriter(currentLogFile, true) // Append mode
        );
        System.out.println("[LOG_WRITER] Opened log file: " + currentLogFile);
    }

    /**
     * Rotate log file (close current, open new)
     */
    private void rotateLogFile() throws IOException {
        if (currentWriter != null) {
            currentWriter.close();
        }
        openLogFile();
        System.out.println("[LOG_WRITER] Rotated to new log file: " + currentLogFile);
    }

    /**
     * Close the log writer
     */
    public void close() {
        try {
            if (currentWriter != null) {
                currentWriter.close();
            }
        } catch (IOException e) {
            System.err.println("[LOG_WRITER] Error closing: " + e.getMessage());
        }
    }
}
