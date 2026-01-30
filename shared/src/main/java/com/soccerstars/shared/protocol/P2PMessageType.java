package com.soccerstars.shared.protocol;

/**
 * Defines all message types for P2P (UDP) communication between clients during gameplay.
 */
public enum P2PMessageType {
    // Connection
    HANDSHAKE,              // Initial connection verification
    HANDSHAKE_ACK,          // Handshake acknowledged
    HEARTBEAT,              // Keep-alive ping
    HEARTBEAT_ACK,          // Keep-alive response

    // Game Actions
    SHOT,                   // Player performed a shot
    SHOT_ACK,               // Shot received acknowledgment

    // Synchronization
    STATE_SYNC,             // Full game state for synchronization
    STATE_HASH,             // Hash of current state for comparison
    STATE_REQUEST,          // Request full state sync

    // Turn Management
    TURN_END,               // Current player's turn ended (all pieces stopped)
    TURN_END_ACK,           // Turn end acknowledged

    // Game Events
    GOAL_SCORED,            // A goal was scored
    GAME_OVER,              // Game finished
    GAME_OVER_ACK,          // Game over acknowledged

    // Rematch
    REMATCH_REQUEST,        // Request to play again
    REMATCH_RESPONSE,       // Accept/reject rematch
    RETURN_TO_LOBBY         // Player returning to lobby
}
