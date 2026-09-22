package com.roxana;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Benchmark {
    private static final int TOTAL_REQUESTS = 100000;
    private static final int CONCURRENT_CLIENTS = 50;
    private static final int REQUESTS_PER_CLIENT = TOTAL_REQUESTS / CONCURRENT_CLIENTS;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Benchmark: " + TOTAL_REQUESTS + " requests, " + CONCURRENT_CLIENTS + " active connections.\n");

        runBenchmark("SET", "SET test_key benchmark_test_value");

        Thread.sleep(1000);

        runBenchmark("GET", "GET test_key");
    }

    private static void runBenchmark(String testName, String command) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_CLIENTS);
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_CLIENTS);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_CLIENTS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_CLIENTS; i++) {
            pool.execute(() -> {
                try (Socket socket = new Socket("localhost", 6379);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Signal that this client is connected and wait for all others
                    readyLatch.countDown();
                    readyLatch.await();

                    // Execute the traffic payload for this client
                    for (int j = 0; j < REQUESTS_PER_CLIENT; j++) {
                        writer.println(command);
                        reader.readLine();
                    }
                } catch (Exception e) {
                    System.out.println("Client error: " + e.getMessage());
                } finally {
                    doneLatch.countDown(); // Signal completion
                }
            });
        }

        // Wait for all requests to finish
        doneLatch.await();
        long endTime = System.currentTimeMillis();
        pool.shutdown();

        long durationMs = (endTime - startTime);
        double seconds = durationMs / 1000.0;
        long ops = (long) (TOTAL_REQUESTS / seconds);

        System.out.println("► " + testName + " Results (AOF Enabled):");
        System.out.println("  Total time: " + durationMs + " ms");
        System.out.println("  Throughput: " + ops + " requests/second\n");
    }
}