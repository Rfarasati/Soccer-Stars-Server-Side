package com.soccerstars.server.model;

/**
 * Represents an active game session between two players.
 */
public class GameSession {
    private String gameSessionId;
    private String player1Username; // Initiator - blue pieces
    private String player2Username; // Accepter - red pieces
    private long startTime;
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

    // Getters and Setters
    public String getGameSessionId() { return gameSessionId; }

    public String getPlayer1Username() { return player1Username; }
    public String getPlayer2Username() { return player2Username; }

    public long getStartTime() { return startTime; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }

    public boolean hasPlayer(String username) {
        return player1Username.equals(username) || player2Username.equals(username);
    }
}
