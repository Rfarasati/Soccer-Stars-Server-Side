package com.soccerstars.server.network;

import com.soccerstars.server.service.DatabaseService;
import com.soccerstars.server.service.SessionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private final int port;
    private final DatabaseService database;
    private final SessionManager sessionManager;
    private final ExecutorService clientPool;
    private final ScheduledExecutorService scheduler;
    private final List<ClientHandler> activeHandlers;

    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public GameServer(int port) {
        this.port = port;
        this.database = new DatabaseService();
        this.sessionManager = new SessionManager();
        this.clientPool = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.activeHandlers = new ArrayList<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;

            System.out.println("         SOCCER STARS SERVER STARTED                ");
            System.out.println("  Port: " + port);
            System.out.println("  Status: Running                                   ");

            // Start periodic cleanup task
            startCleanupTask();

            // Start stats logging
            startStatsLogging();

            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Server] Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Failed to start: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void handleNewConnection(Socket socket) {
        ClientHandler handler = new ClientHandler(socket, database, sessionManager, this);
        synchronized (activeHandlers) {
            activeHandlers.add(handler);
        }
        clientPool.submit(handler);
        System.out.println("[Server] New connection from " + socket.getInetAddress().getHostAddress());
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            sessionManager.cleanupExpiredInvitations();
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void startStatsLogging() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[Server Stats] " + sessionManager.getStats());
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        System.out.println("[Server] Shutting down...");

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error closing server socket: " + e.getMessage());
        }

        // Stop all client handlers
        synchronized (activeHandlers) {
            for (ClientHandler handler : activeHandlers) {
                handler.stop();
            }
            activeHandlers.clear();
        }

        // Shutdown thread pools
        clientPool.shutdown();
        scheduler.shutdown();

        try {
            clientPool.awaitTermination(5, TimeUnit.SECONDS);
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Close database
        database.close();

        System.out.println("[Server] Shutdown complete");
    }

}
