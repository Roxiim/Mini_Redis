package com.roxana;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static volatile boolean isRunning = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        int port = 6379;
        StorageEngine engine = new StorageEngine();
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n(Graceful Shutdown)");
            isRunning = false;

            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing the port: " + e.getMessage());
            }

            threadPool.shutdown();

            try {
                System.out.println("[System] Waiting to finalize active orders (max 5s)");
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("[System] Forcing shutdown");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }

            System.out.println("[System] Mini-Redis has stopped");
        }));

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Mini-Redis started on port " + port + " with a Thread Pool.");

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, engine);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (isRunning) {
                        System.out.println("Error client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error in turning the server on: " + e.getMessage());
        }
    }
}