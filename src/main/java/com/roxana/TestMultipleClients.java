package com.roxana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestMultipleClients {
    public static void main(String[] args) {

        for (int i = 1; i <= 50; i++) {
            final int clientId = i;

            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 6379);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String key = "user_" + clientId;
                    writer.println("SET " + key + " active");
                    String setResponse = reader.readLine();

                    writer.println("GET " + key);
                    String getResponse = reader.readLine();

                    System.out.println("Client " + clientId + " received: SET=" + setResponse + ", GET=" + getResponse);

                } catch (Exception e) {
                    System.out.println("Client " + clientId + " error: " + e.getMessage());
                }
            }).start();
        }
    }
}