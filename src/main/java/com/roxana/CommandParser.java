package com.roxana;

import java.util.Arrays;

public class CommandParser {

    public static String processCommand(String inputLine, StorageEngine engine) {
        String[] parts = inputLine.trim().split(" ");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "-ERR empty command";
        }

        String command = parts[0].toUpperCase();

        if (command.equals("PING")) {
            return "+PONG";

        } else if (command.equals("SET") && parts.length >= 3) {
            String key = parts[1];

            String value = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

            engine.set(key, value);
            return "+OK";

        } else if (command.equals("GET") && parts.length >= 2) {
            String key = parts[1];
            String value = engine.get(key);
            return (value != null) ? value : "(nil)";

        } else if (command.equals("LPUSH") && parts.length >= 3) {
        String key = parts[1];
        String value = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

        int newSize = engine.lpush(key, value);

        return "(integer) " + newSize;

        } else if (command.equals("LRANGE") && parts.length >= 2) {
            String key = parts[1];
            return engine.lrange(key);

        } else if (command.equals("DEL") && parts.length >= 2) {
            String key = parts[1];
            boolean deleted = engine.delete(key);
            return deleted ? "1" : "0";

        } else if (command.equals("EXISTS") && parts.length >= 2) {
            String key = parts[1];
            boolean exists = engine.exists(key);
            return exists ? "1" : "0";

        } else if (command.equals("EXPIRE") && parts.length >= 3) {
            String key = parts[1];
            try {
                int seconds = Integer.parseInt(parts[2]);
                boolean success = engine.expire(key, seconds);
                return success ? "1" : "0";
            } catch (NumberFormatException e) {
                return "-ERR value is not an integer or out of range";
            }
        }
        else {
            return "-ERR unknown command or wrong number of arguments";
        }
    }
}