package com.roxana;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private StorageEngine engine;
    private final String TEST_AOF_FILE = "database.aof";

    @BeforeEach
    void setUp() {
        engine = new StorageEngine();
    }

    @AfterEach
    void tearDown() {
        File aof = new File(TEST_AOF_FILE);
        if (aof.exists()) {
            aof.delete();
        }
    }

    @Test
    void testPingCommand() {
        String response = CommandParser.processCommand("PING", engine);
        assertEquals("+PONG", response);
    }

    @Test
    void testSetAndGetWithMultipleWords() {
        String setResponse = CommandParser.processCommand("SET profile Java Developer", engine);
        assertEquals("+OK", setResponse);

        String getResponse = CommandParser.processCommand("GET profile", engine);
        assertEquals("Java Developer", getResponse);
    }

    @Test
    void testGetNonExistentKeyReturnsNil() {
        String response = CommandParser.processCommand("GET fake_key", engine);
        assertEquals("(nil)", response);
    }

    @Test
    void testDelCommand() {
        CommandParser.processCommand("SET car Toyota", engine);

        String delResponse = CommandParser.processCommand("DEL car", engine);
        assertEquals("1", delResponse);

        String delAgainResponse = CommandParser.processCommand("DEL car", engine);
        assertEquals("0", delAgainResponse);
    }

    @Test
    void testExistsCommand() {
        CommandParser.processCommand("SET phone Samsung", engine);

        assertEquals("1", CommandParser.processCommand("EXISTS phone", engine));
        assertEquals("0", CommandParser.processCommand("EXISTS laptop", engine));
    }

    @Test
    void testListOperations() {
        String push1 = CommandParser.processCommand("LPUSH languages Java", engine);
        assertEquals("(integer) 1", push1);

        String push2 = CommandParser.processCommand("LPUSH languages Python", engine);
        assertEquals("(integer) 2", push2);

        String rangeResponse = CommandParser.processCommand("LRANGE languages", engine);
        assertTrue(rangeResponse.contains("Java"));
        assertTrue(rangeResponse.contains("Python"));
    }

    @Test
    void testWrongTypeError() {
        CommandParser.processCommand("SET name Roxana", engine);

        String errorResponse = CommandParser.processCommand("LRANGE name", engine);
        assertEquals("-ERR WRONGTYPE", errorResponse);
    }

    @Test
    void testExpireCommand() throws InterruptedException {
        CommandParser.processCommand("SET password secret", engine);

        String expireResponse = CommandParser.processCommand("EXPIRE password 1", engine);
        assertEquals("1", expireResponse); // 1 = timer successfully set

        assertEquals("secret", CommandParser.processCommand("GET password", engine));

        Thread.sleep(1100);

        assertEquals("(nil)", CommandParser.processCommand("GET password", engine));
    }
}