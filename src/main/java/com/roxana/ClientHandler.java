package com.roxana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final StorageEngine engine;

    public ClientHandler(Socket socket, StorageEngine engine) {
        this.clientSocket = socket;
        this.engine = engine;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Received command: " + inputLine);

                String response = CommandParser.processCommand(inputLine, engine);

                writer.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}