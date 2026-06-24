package com.novalang.model;

public class ExecuteResponse {
    private boolean success;
    private String output;
    private String error;
    private long executionTimeMs;

    public ExecuteResponse() {}

    public ExecuteResponse(boolean success, String output, String error, long executionTimeMs) {
        this.success = success;
        this.output = output;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
