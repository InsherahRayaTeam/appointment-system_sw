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
    private final LoginNotifier loginNotifier = new LoginNotifier();

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void update_PrintsNotificationMessage() {
        loginNotifier.update("test event");

        assertTrue(capturedOutput().contains("Notification: test event"));
    }

    @Test
    void notifyLoginSuccess_PrintsSuccessMessage() {
        loginNotifier.notifyLoginSuccess("admin");

        assertTrue(capturedOutput().contains("Login successful."));
    }

    @Test
    void notifyLoginFailure_WithNullUsername_UsesBlankPlaceholder() {
        loginNotifier.notifyLoginFailure(null);

        assertTrue(capturedOutput().contains("Failed login attempt for user '<blank>'."));
    }

    @Test
    void notifyLoginFailure_WithEmptyUsername_UsesBlankPlaceholder() {
        loginNotifier.notifyLoginFailure("");

        assertTrue(capturedOutput().contains("Failed login attempt for user '<blank>'."));
    }

    @Test
    void notifyLogout_PrintsLogoutMessage() {
        loginNotifier.notifyLogout(null);

        assertTrue(capturedOutput().contains("You have been logged out successfully."));
    }

    private String capturedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}

