package com.soccerstars.shared.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Contains all P2P message types used for UDP communication during gameplay.
 */
public class P2PMessages {
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Base class for all P2P messages.
     */
    public static class P2PMessage {
        private P2PMessageType type;
        private long timestamp;
        private int sequenceNumber;

        public P2PMessage() {}

        public P2PMessage(P2PMessageType type) {
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public P2PMessageType getType() { return type; }
        public void setType(P2PMessageType type) { this.type = type; }
        public long getTimestamp() { return timestamp; }
        public int getSequenceNumber() { return sequenceNumber; }
        public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

        public String toJson() { return gson.toJson(this); }

        public static P2PMessageType parseType(String json) {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return P2PMessageType.valueOf(obj.get("type").getAsString());
        }

        public static <T extends P2PMessage> T fromJson(String json, Class<T> clazz) {
            return gson.fromJson(json, clazz);
        }
    }

    // ==================== Connection Messages ====================

    public static class HandshakeMessage extends P2PMessage {
        private String gameSessionId;
        private String username;

        public HandshakeMessage() { super(P2PMessageType.HANDSHAKE); }

        public HandshakeMessage(String gameSessionId, String username) {
            super(P2PMessageType.HANDSHAKE);
            this.gameSessionId = gameSessionId;
            this.username = username;
        }

        public String getGameSessionId() { return gameSessionId; }
        public String getUsername() { return username; }
    }

    public static class HandshakeAckMessage extends P2PMessage {
        private String gameSessionId;
        private String username;
        private boolean ready;

        public HandshakeAckMessage() { super(P2PMessageType.HANDSHAKE_ACK); }

        public HandshakeAckMessage(String gameSessionId, String username, boolean ready) {
            super(P2PMessageType.HANDSHAKE_ACK);
            this.gameSessionId = gameSessionId;
            this.username = username;
            this.ready = ready;
        }

        public String getGameSessionId() { return gameSessionId; }
        public String getUsername() { return username; }
        public boolean isReady() { return ready; }
    }

    public static class HeartbeatMessage extends P2PMessage {
        public HeartbeatMessage() { super(P2PMessageType.HEARTBEAT); }
    }

    public static class HeartbeatAckMessage extends P2PMessage {
        public HeartbeatAckMessage() { super(P2PMessageType.HEARTBEAT_ACK); }
    }

    // ==================== Game Action Messages ====================

    public static class ShotMessage extends P2PMessage {
        private int pieceId;       // Which piece was shot (0-4 for each player)
        private double angle;      // Direction in radians
        private double power;      // Shot power (can be fixed or variable)
        private int turnNumber;    // For ordering and validation

        public ShotMessage() { super(P2PMessageType.SHOT); }

        public ShotMessage(int pieceId, double angle, double power, int turnNumber) {
            super(P2PMessageType.SHOT);
            this.pieceId = pieceId;
            this.angle = angle;
            this.power = power;
            this.turnNumber = turnNumber;
        }

        public int getPieceId() { return pieceId; }
        public double getAngle() { return angle; }
        public double getPower() { return power; }
        public int getTurnNumber() { return turnNumber; }
    }

    public static class ShotAckMessage extends P2PMessage {
        private int turnNumber;
        private boolean received;

        public ShotAckMessage() { super(P2PMessageType.SHOT_ACK); }

        public ShotAckMessage(int turnNumber, boolean received) {
            super(P2PMessageType.SHOT_ACK);
            this.turnNumber = turnNumber;
            this.received = received;
        }

        public int getTurnNumber() { return turnNumber; }
        public boolean isReceived() { return received; }
    }

    // ==================== Synchronization Messages ====================

    /**
     * Represents position of a single game object (piece or ball).
     */
    public static class ObjectState {
        private double x;
        private double y;
        private double vx;  // velocity x
        private double vy;  // velocity y

        public ObjectState() {}

        public ObjectState(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getVx() { return vx; }
        public double getVy() { return vy; }
    }

    public static class StateSyncMessage extends P2PMessage {
        private ObjectState[] bluePieces;   // 5 pieces for blue player
        private ObjectState[] redPieces;    // 5 pieces for red player
        private ObjectState ball;
        private int blueScore;
        private int redScore;
        private int currentTurn;
        private boolean isBluesTurn;

        public StateSyncMessage() { super(P2PMessageType.STATE_SYNC); }

        public StateSyncMessage(ObjectState[] bluePieces, ObjectState[] redPieces,
                                ObjectState ball, int blueScore, int redScore,
                                int currentTurn, boolean isBluesTurn) {
            super(P2PMessageType.STATE_SYNC);
            this.bluePieces = bluePieces;
            this.redPieces = redPieces;
            this.ball = ball;
            this.blueScore = blueScore;
            this.redScore = redScore;
            this.currentTurn = currentTurn;
            this.isBluesTurn = isBluesTurn;
        }

        public ObjectState[] getBluePieces() { return bluePieces; }
        public ObjectState[] getRedPieces() { return redPieces; }
        public ObjectState getBall() { return ball; }
        public int getBlueScore() { return blueScore; }
        public int getRedScore() { return redScore; }
        public int getCurrentTurn() { return currentTurn; }
        public boolean isBluesTurn() { return isBluesTurn; }
    }

    public static class StateHashMessage extends P2PMessage {
        private String stateHash;  // Hash of current positions for comparison
        private int turnNumber;

        public StateHashMessage() { super(P2PMessageType.STATE_HASH); }

        public StateHashMessage(String stateHash, int turnNumber) {
            super(P2PMessageType.STATE_HASH);
            this.stateHash = stateHash;
            this.turnNumber = turnNumber;
        }

        public String getStateHash() { return stateHash; }
        public int getTurnNumber() { return turnNumber; }
    }

    public static class StateRequestMessage extends P2PMessage {
        public StateRequestMessage() { super(P2PMessageType.STATE_REQUEST); }
    }

    // ==================== Turn Management Messages ====================

    public static class TurnEndMessage extends P2PMessage {
        private int turnNumber;
        private String stateHash;  // Hash after pieces stopped

        public TurnEndMessage() { super(P2PMessageType.TURN_END); }

        public TurnEndMessage(int turnNumber, String stateHash) {
            super(P2PMessageType.TURN_END);
            this.turnNumber = turnNumber;
            this.stateHash = stateHash;
        }

        public int getTurnNumber() { return turnNumber; }
        public String getStateHash() { return stateHash; }
    }

    public static class TurnEndAckMessage extends P2PMessage {
        private int turnNumber;
        private boolean hashMatch;

        public TurnEndAckMessage() { super(P2PMessageType.TURN_END_ACK); }

        public TurnEndAckMessage(int turnNumber, boolean hashMatch) {
            super(P2PMessageType.TURN_END_ACK);
            this.turnNumber = turnNumber;
            this.hashMatch = hashMatch;
        }

        public int getTurnNumber() { return turnNumber; }
        public boolean isHashMatch() { return hashMatch; }
    }

    // ==================== Game Events Messages ====================

    public static class GoalScoredMessage extends P2PMessage {
        private boolean blueScored;  // true if blue team scored
        private int blueScore;
        private int redScore;

        public GoalScoredMessage() { super(P2PMessageType.GOAL_SCORED); }

        public GoalScoredMessage(boolean blueScored, int blueScore, int redScore) {
            super(P2PMessageType.GOAL_SCORED);
            this.blueScored = blueScored;
            this.blueScore = blueScore;
            this.redScore = redScore;
        }

        public boolean isBlueScored() { return blueScored; }
        public int getBlueScore() { return blueScore; }
        public int getRedScore() { return redScore; }
    }

    public static class GameOverMessage extends P2PMessage {
        private String winnerUsername;
        private int winnerScore;
        private int loserScore;

        public GameOverMessage() { super(P2PMessageType.GAME_OVER); }

        public GameOverMessage(String winnerUsername, int winnerScore, int loserScore) {
            super(P2PMessageType.GAME_OVER);
            this.winnerUsername = winnerUsername;
            this.winnerScore = winnerScore;
            this.loserScore = loserScore;
        }

        public String getWinnerUsername() { return winnerUsername; }
        public int getWinnerScore() { return winnerScore; }
        public int getLoserScore() { return loserScore; }
    }

    public static class GameOverAckMessage extends P2PMessage {
        public GameOverAckMessage() { super(P2PMessageType.GAME_OVER_ACK); }
    }

    // ==================== Rematch Messages ====================

    public static class RematchRequestMessage extends P2PMessage {
        public RematchRequestMessage() { super(P2PMessageType.REMATCH_REQUEST); }
    }

    public static class RematchResponseMessage extends P2PMessage {
        private boolean accepted;

        public RematchResponseMessage() { super(P2PMessageType.REMATCH_RESPONSE); }

        public RematchResponseMessage(boolean accepted) {
            super(P2PMessageType.REMATCH_RESPONSE);
            this.accepted = accepted;
        }

        public boolean isAccepted() { return accepted; }
    }

    public static class ReturnToLobbyMessage extends P2PMessage {
        public ReturnToLobbyMessage() { super(P2PMessageType.RETURN_TO_LOBBY); }
    }
}
