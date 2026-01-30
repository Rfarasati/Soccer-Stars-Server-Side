package com.soccerstars.server.model;

/**
 * Represents a pending game invitation between two users.
 */
public class GameInvitation {
    private String inviteId;
    private String fromUsername;
    private String toUsername;
    private String fromIpAddress;
    private int fromUdpPort;
    private long createdTime;
    private boolean responded;

    public GameInvitation(String inviteId, String fromUsername, String toUsername,
                          String fromIpAddress, int fromUdpPort) {
        this.inviteId = inviteId;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.fromIpAddress = fromIpAddress;
        this.fromUdpPort = fromUdpPort;
        this.createdTime = System.currentTimeMillis();
        this.responded = false;
    }

    // Getters
    public String getInviteId() { return inviteId; }
    public String getFromUsername() { return fromUsername; }
    public String getToUsername() { return toUsername; }
    public String getFromIpAddress() { return fromIpAddress; }
    public int getFromUdpPort() { return fromUdpPort; }
    public long getCreatedTime() { return createdTime; }
    public boolean isResponded() { return responded; }
    public void setResponded(boolean responded) { this.responded = responded; }

    /**
     * Check if invitation has expired (30 seconds timeout)
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createdTime > 30000;
    }
}
