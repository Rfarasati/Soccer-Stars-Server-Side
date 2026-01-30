package com.soccerstars.server.service;

import com.soccerstars.server.model.GameInvitation;
import com.soccerstars.server.model.GameSession;
import com.soccerstars.server.model.UserSession;
import com.soccerstars.server.network.ClientHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages active user sessions, game invitations, and active games.
 * Thread-safe implementation for concurrent access.
 */
public class SessionManager {
    // Maps sessionId -> UserSession
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    // Maps username -> sessionId (for quick lookup)
    private final Map<String, String> usernameToSession = new ConcurrentHashMap<>();

    // Maps sessionId -> ClientHandler (for sending messages)
    private final Map<String, ClientHandler> sessionHandlers = new ConcurrentHashMap<>();

    // Maps inviteId -> GameInvitation
    private final Map<String, GameInvitation> pendingInvitations = new ConcurrentHashMap<>();

    // Maps gameSessionId -> GameSession
    private final Map<String, GameSession> activeGames = new ConcurrentHashMap<>();

    /**
     * Create a new session for a logged-in user.
     */
    public UserSession createSession(String username, String ipAddress, ClientHandler handler) {
        // Check if user already has an active session
        if (usernameToSession.containsKey(username)) {
            String oldSessionId = usernameToSession.get(username);
            removeSession(oldSessionId);
        }

        String sessionId = generateSessionId();
        UserSession session = new UserSession(sessionId, username, ipAddress);

        sessions.put(sessionId, session);
        usernameToSession.put(username, sessionId);
        sessionHandlers.put(sessionId, handler);

        System.out.println("[SessionManager] Session created for " + username + " (ID: " + sessionId + ")");
        return session;
    }

    /**
     * Remove a session (logout or disconnect).
     */
    public void removeSession(String sessionId) {
        UserSession session = sessions.remove(sessionId);
        if (session != null) {
            usernameToSession.remove(session.getUsername());
            sessionHandlers.remove(sessionId);
            System.out.println("[SessionManager] Session removed for " + session.getUsername());
        }
    }

    /**
     * Remove session by username.
     */
    public void removeSessionByUsername(String username) {
        String sessionId = usernameToSession.get(username);
        if (sessionId != null) {
            removeSession(sessionId);
        }
    }

    /**
     * Get session by session ID.
     */
    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Get session by username.
     */
    public UserSession getSessionByUsername(String username) {
        String sessionId = usernameToSession.get(username);
        return sessionId != null ? sessions.get(sessionId) : null;
    }

    /**
     * Get client handler for a session.
     */
    public ClientHandler getHandler(String sessionId) {
        return sessionHandlers.get(sessionId);
    }

    /**
     * Get client handler by username.
     */
    public ClientHandler getHandlerByUsername(String username) {
        String sessionId = usernameToSession.get(username);
        return sessionId != null ? sessionHandlers.get(sessionId) : null;
    }

    /**
     * Validate if session ID is valid and active.
     */
    public boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * Check if user is online.
     */
    public boolean isUserOnline(String username) {
        return usernameToSession.containsKey(username);
    }

    /**
     * Update user status.
     */
    public void updateUserStatus(String username, UserSession.Status status) {
        UserSession session = getSessionByUsername(username);
        if (session != null) {
            session.setStatus(status);
            System.out.println("[SessionManager] Status updated: " + username + " -> " + status);
        }
    }

    /**
     * Get list of all online users with their status.
     */
    public List<UserSession> getOnlineUsers() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Get list of online users excluding a specific user.
     */
    public List<UserSession> getOnlineUsersExcept(String excludeUsername) {
        return sessions.values().stream()
                .filter(s -> !s.getUsername().equals(excludeUsername))
                .collect(Collectors.toList());
    }

    // ==================== Game Invitation Management ====================

    /**
     * Create a new game invitation.
     */
    public GameInvitation createInvitation(String fromUsername, String toUsername,
                                           String fromIpAddress, int fromUdpPort) {
        String inviteId = generateInviteId();
        GameInvitation invitation = new GameInvitation(inviteId, fromUsername, toUsername,
                fromIpAddress, fromUdpPort);
        pendingInvitations.put(inviteId, invitation);

        System.out.println("[SessionManager] Invitation created: " + fromUsername + " -> " + toUsername);
        return invitation;
    }

    /**
     * Get pending invitation by ID.
     */
    public GameInvitation getInvitation(String inviteId) {
        return pendingInvitations.get(inviteId);
    }

    /**
     * Remove an invitation.
     */
    public void removeInvitation(String inviteId) {
        pendingInvitations.remove(inviteId);
    }

    /**
     * Clean up expired invitations.
     */
    public void cleanupExpiredInvitations() {
        pendingInvitations.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // ==================== Game Session Management ====================

    /**
     * Create a new game session.
     */
    public GameSession createGameSession(String player1Username, String player2Username) {
        String gameSessionId = generateGameSessionId();
        GameSession game = new GameSession(gameSessionId, player1Username, player2Username);
        activeGames.put(gameSessionId, game);

        // Update player statuses
        updateUserStatus(player1Username, UserSession.Status.IN_GAME);
        updateUserStatus(player2Username, UserSession.Status.IN_GAME);

        System.out.println("[SessionManager] Game session created: " + player1Username + " vs " + player2Username);
        return game;
    }

    /**
     * Get active game session.
     */
    public GameSession getGameSession(String gameSessionId) {
        return activeGames.get(gameSessionId);
    }

    /**
     * End a game session and set players back to free.
     */
    public void endGameSession(String gameSessionId) {
        GameSession game = activeGames.remove(gameSessionId);
        if (game != null) {
            updateUserStatus(game.getPlayer1Username(), UserSession.Status.FREE);
            updateUserStatus(game.getPlayer2Username(), UserSession.Status.FREE);
            System.out.println("[SessionManager] Game session ended: " + gameSessionId);
        }
    }

    // ==================== ID Generation ====================

    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateInviteId() {
        return "inv_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateGameSessionId() {
        return "game_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Get statistics about current state.
     */
    public String getStats() {
        return String.format("Sessions: %d, Invitations: %d, Active Games: %d",
                sessions.size(), pendingInvitations.size(), activeGames.size());
    }
}
