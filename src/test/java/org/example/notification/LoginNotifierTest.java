package org.example.notification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginNotifierTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;
    private LoginNotifier loginNotifier;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        loginNotifier = new LoginNotifier();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void notifyLoginSuccess_PrintsWelcomeMessageWithUsername() {
        loginNotifier.notifyLoginSuccess("alice");

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Welcome, alice! You are now logged in."));
    }

    @Test
    void notifyLoginFailure_PrintsMessageWithTrimmedUsername() {
        loginNotifier.notifyLoginFailure("  bob  ");

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Failed login attempt detected for user 'bob'."));
    }

    @Test
    void notifyLoginFailure_NullUsername_PrintsBlankPlaceholder() {
        loginNotifier.notifyLoginFailure(null);

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Failed login attempt detected for user '<blank>'."));
    }

    @Test
    void notifyLoginFailure_BlankUsername_PrintsBlankPlaceholder() {
        loginNotifier.notifyLoginFailure("   ");

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Failed login attempt detected for user '<blank>'."));
    }

    @Test
    void notifyLogout_PrintsGoodbyeMessageWithUsername() {
        loginNotifier.notifyLogout("carol");

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Goodbye, carol! You have been logged out."));
    }

    @Test
    void notifyLogout_NullUsername_PrintsUnknownPlaceholder() {
        loginNotifier.notifyLogout(null);

        String output = capturedOutput();
        assertTrue(output.contains("[NOTIFY] Goodbye, <unknown>! You have been logged out."));
    }

    private String capturedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}

