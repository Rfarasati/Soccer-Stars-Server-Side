package com.soccerstars.server.model;

public class GameInvitation {

    private final String inviteId;
    private final String fromUsername;
    private final String toUsername;
    private final String fromIpAddress;
    private final int fromUdpPort;
    private final long createdTime;
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

    public String getInviteId() { return inviteId; }
    public String getFromUsername() { return fromUsername; }
    public String getToUsername() { return toUsername; }
    public String getFromIpAddress() { return fromIpAddress; }
    public int getFromUdpPort() { return fromUdpPort; }
    public boolean isResponded() { return responded; }
    public void setResponded(boolean responded) { this.responded = responded; }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdTime > 30000;
    }
}
