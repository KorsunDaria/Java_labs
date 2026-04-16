package org.example;

import java.io.*;
import java.util.Date;

/**
 * Provides logging functionality to record game history into a file.
 */
public class GameLogger {
    private String fileName;

    /**
     * Initializes the logger with a specific file path.
     * @param fileName Path to the log file.
     */
    public GameLogger(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Appends a text message to the log file.
     * @param message String message to log.
     */
    public void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, true))) {
            out.println("[" + new Date() + "] " + message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file.");
        }
    }

    /**
     * Logs details of a single move.
     * @param step Move number.
     * @param guess Player's input string.
     * @param result Result of the check.
     */
    public void logMove(int step, String guess, GameResult result) {
        log("Step #" + step + " | Input: " + guess +
                " | Bulls: " + result.getBulls() + " Cows: " + result.getCows());
    }
}