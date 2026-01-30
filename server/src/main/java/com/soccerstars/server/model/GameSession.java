package com.soccerstars.server.model;

public class GameSession {
    private final String gameSessionId;
    private final String player1Username; // blue pieces
    private final String player2Username; // red pieces
    private final long startTime;
    private boolean finished;
    private String winnerUsername;
    private int player1Score;
    private int player2Score;

    public GameSession(String gameSessionId, String player1Username, String player2Username) {
        this.gameSessionId = gameSessionId;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.startTime = System.currentTimeMillis();
        this.finished = false;
        this.player1Score = 0;
        this.player2Score = 0;
    }

    public String getGameSessionId() { return gameSessionId; }

    public String getPlayer1Username() { return player1Username; }
    public String getPlayer2Username() { return player2Username; }

}
