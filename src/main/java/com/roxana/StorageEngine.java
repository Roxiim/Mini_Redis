package com.roxana;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class StorageEngine {
    private final ConcurrentHashMap<String, Object> storage;
    private final ConcurrentHashMap<String, Long> expirations;

    private final String AOF_FILE = "database.aof";
    private boolean isRestoring = false;

    public StorageEngine() {
        this.storage = new ConcurrentHashMap<>();
        this.expirations = new ConcurrentHashMap<>();

        restoreFromAof();
    }

    private synchronized void appendToAof(String command) {
        if (isRestoring) return;

        try (PrintWriter out = new PrintWriter(new FileWriter(AOF_FILE, true))) {
            out.println(command);
        } catch (IOException e) {
            System.out.println("Error writing to AOF: " + e.getMessage());
        }
    }

    private void restoreFromAof() {
        File file = new File(AOF_FILE);
        if (!file.exists()) return;

        isRestoring = true;
        System.out.println("Restoring data from AOF...");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                CommandParser.processCommand(line, this);
            }
            System.out.println("AOF restoration complete!");
        } catch (IOException e) {
            System.out.println("Error reading from AOF: " + e.getMessage());
        } finally {
            isRestoring = false;
        }
    }

    private void checkExpiry(String key) {
        Long expireTime = expirations.get(key);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            storage.remove(key);
            expirations.remove(key);
            appendToAof("DEL " + key);
        }
    }

    public boolean expire(String key, int seconds) {
        if (storage.containsKey(key)) {
            long expirationTime = System.currentTimeMillis() + (seconds * 1000L);
            expirations.put(key, expirationTime);
            appendToAof("EXPIRE " + key + " " + seconds);
            return true;
        }
        return false;
    }

    public void set(String key, String value) {
        storage.put(key, value);
        expirations.remove(key);
        appendToAof("SET " + key + " " + value);
    }

    public String get(String key) {
        checkExpiry(key);
        Object value = storage.get(key);
        if (value == null) return null;

        if (value instanceof String) {
            return (String) value;
        } else {
            return "-ERR WRONGTYPE";
        }
    }

    public int lpush(String key, String value) {
        checkExpiry(key);
        storage.putIfAbsent(key, new ArrayList<String>());
        Object savedObject = storage.get(key);

        if (savedObject instanceof List) {
            List<String> list = (List<String>) savedObject;
            list.add(value);
            appendToAof("LPUSH " + key + " " + value);
            return list.size();
        }
        return 0;
    }

    public String lrange(String key) {
        checkExpiry(key);
        Object savedObject = storage.get(key);
        if (savedObject == null) return "(empty list or set)";

        if (savedObject instanceof List) {
            return savedObject.toString();
        } else {
            return "-ERR WRONGTYPE";
        }
    }

    public boolean delete(String key) {
        expirations.remove(key);
        boolean deleted = storage.remove(key) != null;
        if (deleted) {
            appendToAof("DEL " + key);
        }
        return deleted;
    }

    public boolean exists(String key) {
        checkExpiry(key);
        return storage.containsKey(key);
    }
}