package com.soccerstars.server.protocol;

import java.util.List;

/**
 * Contains all specific message types used in the protocol.
 */
public class Messages {

    // ==================== Connection Messages ====================

    public static class PingMessage extends Message {
        public PingMessage() {
            super(MessageType.PING);
        }
    }

    public static class PongMessage extends Message {
        private String welcomeMessage;
        private long serverTime;

        public PongMessage() {
            super(MessageType.PONG);
        }

        public PongMessage(String welcomeMessage) {
            super(MessageType.PONG);
            this.welcomeMessage = welcomeMessage;
            this.serverTime = System.currentTimeMillis();
        }

        public String getWelcomeMessage() { return welcomeMessage; }
        public long getServerTime() { return serverTime; }
    }

    // ==================== Authentication Messages ====================

    public static class RegisterRequest extends Message {
        private String username;
        private String email;
        private String password;

        public RegisterRequest() {
            super(MessageType.REGISTER_REQUEST);
        }

        public RegisterRequest(String username, String email, String password) {
            super(MessageType.REGISTER_REQUEST);
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    public static class RegisterResponse extends Message {
        private boolean success;
        private String message;

        public RegisterResponse() {
            super(MessageType.REGISTER_RESPONSE);
        }

        public RegisterResponse(boolean success, String message) {
            super(MessageType.REGISTER_RESPONSE);
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class LoginRequest extends Message {
        private String username;
        private String password;

        public LoginRequest() {
            super(MessageType.LOGIN_REQUEST);
        }

        public LoginRequest(String username, String password) {
            super(MessageType.LOGIN_REQUEST);
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }

    public static class LoginResponse extends Message {
        private boolean success;
        private String message;
        private String sessionId;
        private String username;

        public LoginResponse() {
            super(MessageType.LOGIN_RESPONSE);
        }

        public LoginResponse(boolean success, String message, String sessionId, String username) {
            super(MessageType.LOGIN_RESPONSE);
            this.success = success;
            this.message = message;
            this.sessionId = sessionId;
            this.username = username;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
    }

    public static class LogoutRequest extends Message {
        private String sessionId;

        public LogoutRequest() {
            super(MessageType.LOGOUT_REQUEST);
        }

        public LogoutRequest(String sessionId) {
            super(MessageType.LOGOUT_REQUEST);
            this.sessionId = sessionId;
        }

        public String getSessionId() { return sessionId; }
    }

    public static class LogoutResponse extends Message {
        private boolean success;

        public LogoutResponse() {
            super(MessageType.LOGOUT_RESPONSE);
        }

        public LogoutResponse(boolean success) {
            super(MessageType.LOGOUT_RESPONSE);
            this.success = success;
        }

        public boolean isSuccess() { return success; }
    }

    // ==================== Lobby Messages ====================

    public static class GetOnlineUsersRequest extends Message {
        private String sessionId;

        public GetOnlineUsersRequest() {
            super(MessageType.GET_ONLINE_USERS);
        }

        public GetOnlineUsersRequest(String sessionId) {
            super(MessageType.GET_ONLINE_USERS);
            this.sessionId = sessionId;
        }

        public String getSessionId() { return sessionId; }
    }

    public static class OnlineUser {
        private String username;
        private String status; // "free" or "in_game"

        public OnlineUser() {}

        public OnlineUser(String username, String status) {
            this.username = username;
            this.status = status;
        }

        public String getUsername() { return username; }
        public String getStatus() { return status; }
    }

    public static class OnlineUsersListMessage extends Message {
        private List<OnlineUser> users;

        public OnlineUsersListMessage() {
            super(MessageType.ONLINE_USERS_LIST);
        }

        public OnlineUsersListMessage(List<OnlineUser> users) {
            super(MessageType.ONLINE_USERS_LIST);
            this.users = users;
        }

        public List<OnlineUser> getUsers() { return users; }
    }

    public static class UserStatusUpdate extends Message {
        private String username;
        private String status; // "online", "in_game", "offline"

        public UserStatusUpdate() {
            super(MessageType.USER_STATUS_UPDATE);
        }

        public UserStatusUpdate(String username, String status) {
            super(MessageType.USER_STATUS_UPDATE);
            this.username = username;
            this.status = status;
        }

        public String getUsername() { return username; }
        public String getStatus() { return status; }
    }

    // ==================== Matchmaking Messages ====================

    public static class GameInvite extends Message {
        private String sessionId;
        private String targetUsername;
        private int udpPort; // Port that the inviter will listen on for P2P

        public GameInvite() {
            super(MessageType.GAME_INVITE);
        }

        public GameInvite(String sessionId, String targetUsername, int udpPort) {
            super(MessageType.GAME_INVITE);
            this.sessionId = sessionId;
            this.targetUsername = targetUsername;
            this.udpPort = udpPort;
        }

        public String getSessionId() { return sessionId; }
        public String getTargetUsername() { return targetUsername; }
        public int getUdpPort() { return udpPort; }
    }

    public static class GameInviteNotification extends Message {
        private String fromUsername;
        private String inviteId;

        public GameInviteNotification() {
            super(MessageType.GAME_INVITE_NOTIFICATION);
        }

        public GameInviteNotification(String fromUsername, String inviteId) {
            super(MessageType.GAME_INVITE_NOTIFICATION);
            this.fromUsername = fromUsername;
            this.inviteId = inviteId;
        }

        public String getFromUsername() { return fromUsername; }
        public String getInviteId() { return inviteId; }
    }

    public static class GameInviteResponseMsg extends Message {
        private String inviteId;
        private boolean accepted;
        private int udpPort; // Port that the accepter will listen on for P2P

        public GameInviteResponseMsg() {
            super(MessageType.GAME_INVITE_RESPONSE);
        }

        public GameInviteResponseMsg(String inviteId, boolean accepted, int udpPort) {
            super(MessageType.GAME_INVITE_RESPONSE);
            this.inviteId = inviteId;
            this.accepted = accepted;
            this.udpPort = udpPort;
        }

        public String getInviteId() { return inviteId; }
        public boolean isAccepted() { return accepted; }
        public int getUdpPort() { return udpPort; }
    }

    public static class GameStartMessage extends Message {
        private String gameSessionId;
        private String opponentUsername;
        private String opponentIp;
        private int opponentUdpPort;
        private boolean isInitiator; // true = blue pieces, starts first

        public GameStartMessage() {
            super(MessageType.GAME_START);
        }

        public GameStartMessage(String gameSessionId, String opponentUsername,
                                String opponentIp, int opponentUdpPort, boolean isInitiator) {
            super(MessageType.GAME_START);
            this.gameSessionId = gameSessionId;
            this.opponentUsername = opponentUsername;
            this.opponentIp = opponentIp;
            this.opponentUdpPort = opponentUdpPort;
            this.isInitiator = isInitiator;
        }

        public String getGameSessionId() { return gameSessionId; }
        public String getOpponentUsername() { return opponentUsername; }
        public String getOpponentIp() { return opponentIp; }
        public int getOpponentUdpPort() { return opponentUdpPort; }
        public boolean isInitiator() { return isInitiator; }
    }

    public static class GameInviteCancelled extends Message {
        private String reason;

        public GameInviteCancelled() {
            super(MessageType.GAME_INVITE_CANCELLED);
        }

        public GameInviteCancelled(String reason) {
            super(MessageType.GAME_INVITE_CANCELLED);
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    // ==================== Game Result Messages ====================

    public static class GameResultMessage extends Message {
        private String sessionId;
        private String gameSessionId;
        private String winnerUsername;
        private int winnerScore;
        private int loserScore;

        public GameResultMessage() {
            super(MessageType.GAME_RESULT);
        }

        public GameResultMessage(String sessionId, String gameSessionId,
                                 String winnerUsername, int winnerScore, int loserScore) {
            super(MessageType.GAME_RESULT);
            this.sessionId = sessionId;
            this.gameSessionId = gameSessionId;
            this.winnerUsername = winnerUsername;
            this.winnerScore = winnerScore;
            this.loserScore = loserScore;
        }

        public String getSessionId() { return sessionId; }
        public String getGameSessionId() { return gameSessionId; }
        public String getWinnerUsername() { return winnerUsername; }
        public int getWinnerScore() { return winnerScore; }
        public int getLoserScore() { return loserScore; }
    }

    public static class GameResultAck extends Message {
        private boolean success;

        public GameResultAck() {
            super(MessageType.GAME_RESULT_ACK);
        }

        public GameResultAck(boolean success) {
            super(MessageType.GAME_RESULT_ACK);
            this.success = success;
        }

        public boolean isSuccess() { return success; }
    }

    // ==================== Error Messages ====================

    public static class ErrorMessage extends Message {
        private String errorCode;
        private String errorMessage;

        public ErrorMessage() {
            super(MessageType.ERROR);
        }

        public ErrorMessage(String errorCode, String errorMessage) {
            super(MessageType.ERROR);
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }
}
