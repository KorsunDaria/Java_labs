package org.example;

public class ExecutionResult {

    private final String command;
    private final String error;

    public ExecutionResult(String command, String error) {
        this.command = command;
        this.error = error;
    }

    public String getCommand() {
        return command;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
