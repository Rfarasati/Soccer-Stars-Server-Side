package com.soccerstars.shared;

/**
 * Game constants shared between server and client.
 */
public final class GameConstants {
    private GameConstants() {} // Prevent instantiation

    // ==================== Network ====================
    public static final int DEFAULT_SERVER_PORT = 5000;
    public static final String DEFAULT_SERVER_HOST = "localhost";

    // ==================== Game Field ====================
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 500;
    public static final int GOAL_WIDTH = 20;
    public static final int GOAL_HEIGHT = 120;

    // Goal positions (Y coordinates for goal posts)
    public static final int GOAL_TOP = (FIELD_HEIGHT - GOAL_HEIGHT) / 2;
    public static final int GOAL_BOTTOM = GOAL_TOP + GOAL_HEIGHT;

    // ==================== Pieces ====================
    public static final int PIECES_PER_PLAYER = 5;
    public static final int PIECE_RADIUS = 25;
    public static final int BALL_RADIUS = 15;

    // ==================== Physics ====================
    public static final double FRICTION = 0.98;        // Velocity multiplier per frame
    public static final double MIN_VELOCITY = 0.1;     // Velocity threshold to stop
    public static final double SHOT_POWER = 15.0;      // Default shot power
    public static final double RESTITUTION = 0.8;      // Bounce coefficient

    // ==================== Game Rules ====================
    public static final int GOALS_TO_WIN = 2;
    public static final int MAX_TURN_TIME_MS = 30000;  // 30 seconds per turn

    // ==================== Initial Positions ====================
    // Blue pieces (left side) - X, Y coordinates
    public static final double[][] BLUE_INITIAL_POSITIONS = {
            {150, 250},  // Goalkeeper
            {250, 150},  // Defender top
            {250, 350},  // Defender bottom
            {350, 200},  // Midfielder top
            {350, 300}   // Midfielder bottom
    };

    // Red pieces (right side) - X, Y coordinates
    public static final double[][] RED_INITIAL_POSITIONS = {
            {650, 250},  // Goalkeeper
            {550, 150},  // Defender top
            {550, 350},  // Defender bottom
            {450, 200},  // Midfielder top
            {450, 300}   // Midfielder bottom
    };

    // Ball position (center)
    public static final double BALL_INITIAL_X = FIELD_WIDTH / 2.0;
    public static final double BALL_INITIAL_Y = FIELD_HEIGHT / 2.0;

    // ==================== Synchronization ====================
    public static final int SYNC_CHECK_INTERVAL_TURNS = 3;  // Check sync every N turns
    public static final double POSITION_TOLERANCE = 5.0;     // Max allowed position difference
    public static final double CORRECTION_SPEED = 0.2;       // Interpolation factor for soft correction

    // ==================== Timeouts ====================
    public static final int HANDSHAKE_TIMEOUT_MS = 10000;
    public static final int HEARTBEAT_INTERVAL_MS = 2000;
    public static final int DISCONNECT_TIMEOUT_MS = 10000;
    public static final int INVITATION_TIMEOUT_MS = 30000;
}
