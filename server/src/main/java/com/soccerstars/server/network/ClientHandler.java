package com.soccerstars.server.network;

import com.soccerstars.server.model.GameInvitation;
import com.soccerstars.server.model.GameSession;
import com.soccerstars.server.model.User;
import com.soccerstars.server.model.UserSession;
import com.soccerstars.server.protocol.Message;
import com.soccerstars.server.protocol.MessageType;
import com.soccerstars.server.protocol.Messages;
import com.soccerstars.server.protocol.Messages.*;
import com.soccerstars.server.service.DatabaseService;
import com.soccerstars.server.service.SessionManager;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles communication with a single client.
 * Each client connection runs in its own thread.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DatabaseService database;
    private final SessionManager sessionManager;
    private final GameServer server;

    private BufferedReader reader;
    private PrintWriter writer;
    private String currentSessionId;
    private String currentUsername;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, DatabaseService database,
                         SessionManager sessionManager, GameServer server) {
        this.socket = socket;
        this.database = database;
        this.sessionManager = sessionManager;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            System.out.println("[ClientHandler] Client connected from " + getClientIp());

            String line;
            while (running && (line = reader.readLine()) != null) {
                processMessage(line);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("[ClientHandler] Connection error: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    private void setupStreams() throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Process an incoming JSON message.
     */
    private void processMessage(String json) {
        try {
            MessageType type = Message.parseType(json);
            System.out.println("[ClientHandler] Received " + type + " from " +
                    (currentUsername != null ? currentUsername : getClientIp()));

            switch (type) {
                case PING -> handlePing();
                case REGISTER_REQUEST -> handleRegister(json);
                case LOGIN_REQUEST -> handleLogin(json);
                case LOGOUT_REQUEST -> handleLogout(json);
                case GET_ONLINE_USERS -> handleGetOnlineUsers(json);
                case GAME_INVITE -> handleGameInvite(json);
                case GAME_INVITE_RESPONSE -> handleGameInviteResponse(json);
                case GAME_RESULT -> handleGameResult(json);
                default -> sendError("UNKNOWN_MESSAGE", "Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("[ClientHandler] Error processing message: " + e.getMessage());
            sendError("PARSE_ERROR", "Failed to process message");
        }
    }

    // ==================== Message Handlers ====================

    private void handlePing() {
        send(new PongMessage("Welcome to Soccer Stars Server!"));
    }

    private void handleRegister(String json) {
        RegisterRequest request = Message.fromJson(json, RegisterRequest.class);

        // Validate input
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            send(new RegisterResponse(false, "Username must be at least 3 characters"));
            return;
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            send(new RegisterResponse(false, "Invalid email address"));
            return;
        }
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            send(new RegisterResponse(false, "Password must be at least 4 characters"));
            return;
        }

        // Check for duplicates
        if (database.usernameExists(request.getUsername())) {
            send(new RegisterResponse(false, "Username already exists"));
            return;
        }
        if (database.emailExists(request.getEmail())) {
            send(new RegisterResponse(false, "Email already registered"));
            return;
        }

        // Register user
        boolean success = database.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        if (success) {
            send(new RegisterResponse(true, "Registration successful! Please login."));
        } else {
            send(new RegisterResponse(false, "Registration failed. Please try again."));
        }
    }

    private void handleLogin(String json) {
        LoginRequest request = Message.fromJson(json, LoginRequest.class);

        // Check if already logged in elsewhere
        if (sessionManager.isUserOnline(request.getUsername())) {
            send(new LoginResponse(false, "User already logged in from another location", null, null));
            return;
        }

        // Validate credentials
        User user = database.validateLogin(request.getUsername(), request.getPassword());

        if (user != null) {
            // Create session
            UserSession session = sessionManager.createSession(
                    user.getUsername(),
                    getClientIp(),
                    this
            );

            currentSessionId = session.getSessionId();
            currentUsername = user.getUsername();

            send(new LoginResponse(true, "Login successful!",
                    session.getSessionId(), user.getUsername()));

            // Broadcast user online status to others
            broadcastUserStatusUpdate(user.getUsername(), "online");
        } else {
            send(new LoginResponse(false, "Invalid username or password", null, null));
        }
    }

    private void handleLogout(String json) {
        LogoutRequest request = Message.fromJson(json, LogoutRequest.class);

        if (currentSessionId != null && currentSessionId.equals(request.getSessionId())) {
            String username = currentUsername;
            sessionManager.removeSession(currentSessionId);

            currentSessionId = null;
            currentUsername = null;

            send(new LogoutResponse(true));

            // Broadcast user offline status
            broadcastUserStatusUpdate(username, "offline");
        } else {
            send(new LogoutResponse(false));
        }
    }

    private void handleGetOnlineUsers(String json) {
        GetOnlineUsersRequest request = Message.fromJson(json, GetOnlineUsersRequest.class);

        if (!sessionManager.isValidSession(request.getSessionId())) {
            sendError("INVALID_SESSION", "Session expired or invalid");
            return;
        }

        UserSession currentSession = sessionManager.getSession(request.getSessionId());
        List<UserSession> users = sessionManager.getOnlineUsersExcept(currentSession.getUsername());

        List<OnlineUser> onlineUsers = users.stream()
                .map(s -> new OnlineUser(s.getUsername(), s.getStatusString()))
                .collect(Collectors.toList());

        send(new OnlineUsersListMessage(onlineUsers));
    }

    private void handleGameInvite(String json) {
        GameInvite request = Message.fromJson(json, GameInvite.class);

        // Validate session
        if (!sessionManager.isValidSession(request.getSessionId())) {
            sendError("INVALID_SESSION", "Session expired or invalid");
            return;
        }

        UserSession inviter = sessionManager.getSession(request.getSessionId());

        // Check if inviter is free
        if (inviter.getStatus() != UserSession.Status.FREE) {
            sendError("NOT_AVAILABLE", "You are not available to start a game");
            return;
        }

        // Check if target exists and is online
        UserSession target = sessionManager.getSessionByUsername(request.getTargetUsername());
        if (target == null) {
            sendError("USER_NOT_FOUND", "User is not online");
            return;
        }

        // Check if target is free
        if (target.getStatus() != UserSession.Status.FREE) {
            sendError("USER_BUSY", "User is not available for a game");
            return;
        }

        // Set both users to BUSY
        sessionManager.updateUserStatus(inviter.getUsername(), UserSession.Status.BUSY);
        sessionManager.updateUserStatus(target.getUsername(), UserSession.Status.BUSY);

        // Create invitation
        GameInvitation invitation = sessionManager.createInvitation(
                inviter.getUsername(),
                request.getTargetUsername(),
                inviter.getIpAddress(),
                request.getUdpPort()
        );

        // Send notification to target
        ClientHandler targetHandler = sessionManager.getHandlerByUsername(request.getTargetUsername());
        if (targetHandler != null) {
            targetHandler.send(new GameInviteNotification(inviter.getUsername(), invitation.getInviteId()));
        }

        // Broadcast status updates
        broadcastUserStatusUpdate(inviter.getUsername(), "busy");
        broadcastUserStatusUpdate(target.getUsername(), "busy");
    }

    private void handleGameInviteResponse(String json) {
        GameInviteResponseMsg response = Message.fromJson(json, GameInviteResponseMsg.class);

        GameInvitation invitation = sessionManager.getInvitation(response.getInviteId());
        if (invitation == null || invitation.isExpired()) {
            sendError("INVITATION_EXPIRED", "Invitation has expired");
            return;
        }

        if (invitation.isResponded()) {
            sendError("INVITATION_RESPONDED", "Invitation already responded to");
            return;
        }

        invitation.setResponded(true);
        sessionManager.removeInvitation(response.getInviteId());

        ClientHandler inviterHandler = sessionManager.getHandlerByUsername(invitation.getFromUsername());

        if (response.isAccepted()) {
            // Create game session
            GameSession game = sessionManager.createGameSession(
                    invitation.getFromUsername(),
                    invitation.getToUsername()
            );

            UserSession accepter = sessionManager.getSessionByUsername(invitation.getToUsername());

            // Send game start to inviter (Player 1 - Blue - Starts first)
            if (inviterHandler != null) {
                inviterHandler.send(new GameStartMessage(
                        game.getGameSessionId(),
                        invitation.getToUsername(),
                        accepter.getIpAddress(),
                        response.getUdpPort(),
                        true  // isInitiator = true
                ));
            }

            // Send game start to accepter (Player 2 - Red)
            send(new GameStartMessage(
                    game.getGameSessionId(),
                    invitation.getFromUsername(),
                    invitation.getFromIpAddress(),
                    invitation.getFromUdpPort(),
                    false  // isInitiator = false
            ));

            // Broadcast status updates
            broadcastUserStatusUpdate(invitation.getFromUsername(), "in_game");
            broadcastUserStatusUpdate(invitation.getToUsername(), "in_game");

        } else {
            // Invitation rejected - set both users back to FREE
            sessionManager.updateUserStatus(invitation.getFromUsername(), UserSession.Status.FREE);
            sessionManager.updateUserStatus(invitation.getToUsername(), UserSession.Status.FREE);

            // Notify inviter
            if (inviterHandler != null) {
                inviterHandler.send(new GameInviteCancelled("Invitation was rejected"));
            }

            // Broadcast status updates
            broadcastUserStatusUpdate(invitation.getFromUsername(), "free");
            broadcastUserStatusUpdate(invitation.getToUsername(), "free");
        }
    }

    private void handleGameResult(String json) {
        GameResultMessage result = Message.fromJson(json, GameResultMessage.class);

        // Validate session
        if (!sessionManager.isValidSession(result.getSessionId())) {
            sendError("INVALID_SESSION", "Session expired or invalid");
            return;
        }

        GameSession game = sessionManager.getGameSession(result.getGameSessionId());
        if (game == null) {
            // Game might already be ended by other player
            send(new GameResultAck(true));
            return;
        }

        // Record result in database
        database.recordGameResult(
                result.getGameSessionId(),
                game.getPlayer1Username(),
                game.getPlayer2Username(),
                result.getWinnerUsername(),
                result.getWinnerScore(),
                result.getLoserScore()
        );

        // End game session and free players
        sessionManager.endGameSession(result.getGameSessionId());

        send(new GameResultAck(true));

        // Broadcast status updates
        broadcastUserStatusUpdate(game.getPlayer1Username(), "free");
        broadcastUserStatusUpdate(game.getPlayer2Username(), "free");
    }

    // ==================== Helper Methods ====================

    /**
     * Send a message to this client.
     */
    public void send(Message message) {
        if (writer != null) {
            writer.println(message.toJson());
        }
    }

    /**
     * Send an error message.
     */
    private void sendError(String code, String message) {
        send(new ErrorMessage(code, message));
    }

    /**
     * Broadcast user status update to all other online users.
     */
    private void broadcastUserStatusUpdate(String username, String status) {
        UserStatusUpdate update = new UserStatusUpdate(username, status);
        for (UserSession session : sessionManager.getOnlineUsers()) {
            if (!session.getUsername().equals(username)) {
                ClientHandler handler = sessionManager.getHandler(session.getSessionId());
                if (handler != null) {
                    handler.send(update);
                }
            }
        }
    }

    /**
     * Get client IP address.
     */
    private String getClientIp() {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * Stop this handler.
     */
    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Cleanup on disconnect.
     */
    private void cleanup() {
        System.out.println("[ClientHandler] Client disconnected: " +
                (currentUsername != null ? currentUsername : getClientIp()));

        if (currentUsername != null) {
            // Broadcast offline status before removing session
            broadcastUserStatusUpdate(currentUsername, "offline");
        }

        if (currentSessionId != null) {
            sessionManager.removeSession(currentSessionId);
        }

        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}
