package com.soccerstars.server.service;

import com.soccerstars.server.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:soccerstars.db";
    private Connection connection;

    public DatabaseService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("[Database] Initialized successfully");
        } catch (SQLException e) {
            System.err.println("[Database] Failed to initialize: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                games_played INTEGER DEFAULT 0,
                games_won INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createGameHistoryTable = """
            CREATE TABLE IF NOT EXISTS game_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_session_id TEXT NOT NULL,
                player1_username TEXT NOT NULL,
                player2_username TEXT NOT NULL,
                winner_username TEXT,
                player1_score INTEGER,
                player2_score INTEGER,
                played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createGameHistoryTable);
        }
    }

    public synchronized boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPassword(password));
            pstmt.executeUpdate();
            System.out.println("[Database] User registered: " + username);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("[Database] Registration failed - duplicate: " + username);
                return false;
            }
            System.err.println("[Database] Registration error: " + e.getMessage());
            return false;
        }
    }

    public synchronized User validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setGamesPlayed(rs.getInt("games_played"));
                user.setGamesWon(rs.getInt("games_won"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[Database] Login validation error: " + e.getMessage());
        }
        return null;
    }

    public synchronized boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[Database] Username check error: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[Database] Email check error: " + e.getMessage());
            return false;
        }
    }

    public synchronized void recordGameResult(String gameSessionId, String player1, String player2,
                                              String winner, int player1Score, int player2Score) {
        String insertGame = """
            INSERT INTO game_history (game_session_id, player1_username, player2_username,
                                      winner_username, player1_score, player2_score)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String updateWinner = "UPDATE users SET games_played = games_played + 1, games_won = games_won + 1 WHERE username = ?";
        String updateLoser = "UPDATE users SET games_played = games_played + 1 WHERE username = ?";

        try {
            // Insert game record
            try (PreparedStatement pstmt = connection.prepareStatement(insertGame)) {
                pstmt.setString(1, gameSessionId);
                pstmt.setString(2, player1);
                pstmt.setString(3, player2);
                pstmt.setString(4, winner);
                pstmt.setInt(5, player1Score);
                pstmt.setInt(6, player2Score);
                pstmt.executeUpdate();
            }

            // Update winner stats
            try (PreparedStatement pstmt = connection.prepareStatement(updateWinner)) {
                pstmt.setString(1, winner);
                pstmt.executeUpdate();
            }

            // Update loser stats
            String loser = winner.equals(player1) ? player2 : player1;
            try (PreparedStatement pstmt = connection.prepareStatement(updateLoser)) {
                pstmt.setString(1, loser);
                pstmt.executeUpdate();
            }

            System.out.println("[Database] Game recorded: " + winner + " won against " +
                    (winner.equals(player1) ? player2 : player1));
        } catch (SQLException e) {
            System.err.println("[Database] Error recording game: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[Database] Connection closed");
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error closing connection: " + e.getMessage());
        }
    }
}
