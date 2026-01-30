package com.soccerstars.server.model;

/**
 * Represents an active user session (logged-in user).
 */
public class UserSession {

    public enum Status {
        FREE,       // Online and available for games
        IN_GAME,    // Currently playing a game
        BUSY        // In matchmaking / waiting for response
    }

    private String sessionId;
    private String username;
    private Status status;
    private String ipAddress;
    private long loginTime;
    private long lastActivityTime;

    public UserSession(String sessionId, String username, String ipAddress) {
        this.sessionId = sessionId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.status = Status.FREE;
        this.loginTime = System.currentTimeMillis();
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }

    public String getUsername() { return username; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }

    public long getLoginTime() { return loginTime; }

    public long getLastActivityTime() { return lastActivityTime; }
    public void updateActivity() { this.lastActivityTime = System.currentTimeMillis(); }

    public String getStatusString() {
        return switch (status) {
            case FREE -> "free";
            case IN_GAME -> "in_game";
            case BUSY -> "busy";
        };
    }
}
