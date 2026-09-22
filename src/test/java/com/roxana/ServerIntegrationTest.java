package com.roxana;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerIntegrationTest {
    private Thread serverThread;
    private final String TEST_AOF_FILE = "database.aof";

    @BeforeAll
    void startServer() throws InterruptedException {
        File aof = new File(TEST_AOF_FILE);
        if (aof.exists()) {
            aof.delete();
        }

        serverThread = new Thread(() -> Main.main(new String[]{}));
        serverThread.start();

        Thread.sleep(1000);
    }

    @Test
    void testFullNetworkFlow() throws Exception {
        try (Socket socket = new Socket("localhost", 6379);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println("PING");
            assertEquals("+PONG", reader.readLine());

            writer.println("SET user Roxana");
            assertEquals("+OK", reader.readLine());

            writer.println("GET user");
            assertEquals("Roxana", reader.readLine());

            writer.println("LPUSH colors Red");
            assertEquals("(integer) 1", reader.readLine());
        }
    }
}