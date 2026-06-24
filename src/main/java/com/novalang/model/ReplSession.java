package com.novalang.model;

import com.novalang.evaluator.Environment;

public class ReplSession {
    private final String sessionId;
    private final Environment environment;
    private long lastActiveTime;

    public ReplSession(String sessionId, Environment environment) {
        this.sessionId = sessionId;
        this.environment = environment;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
}
