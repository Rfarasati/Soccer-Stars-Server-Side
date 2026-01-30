package com.soccerstars.server;

import com.soccerstars.server.network.GameServer;

public class Main {
    private static final int DEFAULT_PORT = 5001;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    System.err.println("Invalid port number. Using default: " + DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument. Using default: " + DEFAULT_PORT);
            }
        }

        // Create and start server
        GameServer server = new GameServer(port);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Main] Received shutdown signal");
            server.stop();
        }));

        // Start the server (blocking call)
        server.start();
    }
}
