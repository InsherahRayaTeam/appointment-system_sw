package org.example.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthEventLoggerTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;
    private AuthEventLogger logger;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        logger = new AuthEventLogger();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void logLoginSuccess_MessageContainsUsername() {
        logger.logLoginSuccess("alice");

        String output = capturedOutput();
        assertTrue(output.contains("[AUTH] Login successful for user 'alice'"));
    }

    @Test
    void logLoginFailure_BlankUsername_ShowsBlankPlaceholder() {
        logger.logLoginFailure("   ");

        String output = capturedOutput();
        assertTrue(output.contains("[AUTH] Login failed for user '<blank>'"));
    }

    @Test
    void logLoginFailure_UsernameIsTrimmedInMessage() {
        logger.logLoginFailure("  bob  ");

        String output = capturedOutput();
        assertTrue(output.contains("[AUTH] Login failed for user 'bob'"));
    }

    @Test
    void logLogout_MessagePrintsUsername() {
        logger.logLogout("carol");

        String output = capturedOutput();
        assertTrue(output.contains("[AUTH] Logout for user 'carol'"));
    }

    @Test
    void logLogout_NullUsername_ShowsUnknownPlaceholder() {
        logger.logLogout(null);

        String output = capturedOutput();
        assertTrue(output.contains("[AUTH] Logout for user '<unknown>'"));
    }

    @Test
    void logLoginFailure_NullUsername_ShowsBlankPlaceholder() {
        logger.logLoginFailure(null);

        String output = capturedOutput();
        assertTrue(output.contains("<blank>"));
    }

    private String capturedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}

