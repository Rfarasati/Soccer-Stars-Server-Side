package com.soccerstars.shared.protocol;

/**
 * Defines all message types for client-server communication.
 * Messages are sent as JSON with a "type" field indicating the message type.
 */
public enum MessageType {
    // Connection
    PING,                    // Client -> Server: Connection test
    PONG,                    // Server -> Client: Connection confirmed with welcome message

    // Authentication
    REGISTER_REQUEST,        // Client -> Server: Register new user
    REGISTER_RESPONSE,       // Server -> Client: Registration result
    LOGIN_REQUEST,           // Client -> Server: Login request
    LOGIN_RESPONSE,          // Server -> Client: Login result
    LOGOUT_REQUEST,          // Client -> Server: Logout request
    LOGOUT_RESPONSE,         // Server -> Client: Logout confirmed

    // Lobby
    GET_ONLINE_USERS,        // Client -> Server: Request online users list
    ONLINE_USERS_LIST,       // Server -> Client: List of online users
    USER_STATUS_UPDATE,      // Server -> Client: A user's status changed (broadcast)

    // Matchmaking
    GAME_INVITE,             // Client -> Server: Invite another user to play
    GAME_INVITE_NOTIFICATION,// Server -> Client: You have been invited
    GAME_INVITE_RESPONSE,    // Client -> Server: Accept/reject invitation
    GAME_START,              // Server -> Both Clients: Game starting, here's P2P info
    GAME_INVITE_CANCELLED,   // Server -> Client: Invitation was rejected/cancelled

    // Game Result
    GAME_RESULT,             // Client -> Server: Report game result
    GAME_RESULT_ACK,         // Server -> Client: Game result received

    // Error
    ERROR                    // Server -> Client: Error message
}
