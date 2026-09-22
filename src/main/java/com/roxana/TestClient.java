package com.roxana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6379);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println("SET age 21");
            System.out.println("Immediate reading: " + reader.readLine());

            writer.println("GET age");
            System.out.println("Immediate reading: " + reader.readLine());


        } catch (Exception e) {
            System.out.println("Error connecting: " + e.getMessage());
        }
    }
}