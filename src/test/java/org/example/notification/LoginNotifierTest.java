package org.example.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LoginNotifierTest {

    private final LoginNotifier loginNotifier = new LoginNotifier();

    @Test
    void update_PrintsMessageWithoutThrowingException() {
        assertDoesNotThrow(() -> loginNotifier.update("test event"));
    }

    @Test
    void notifyLoginSuccess_WithValidUsername_DoesNotThrow() {
        assertDoesNotThrow(() -> loginNotifier.notifyLoginSuccess("admin"));
    }

    @Test
    void notifyLoginFailure_WithNullUsername_DoesNotThrow() {
        assertDoesNotThrow(() -> loginNotifier.notifyLoginFailure(null));
    }

    @Test
    void notifyLoginFailure_WithEmptyUsername_DoesNotThrow() {
        assertDoesNotThrow(() -> loginNotifier.notifyLoginFailure(""));
    }

    @Test
    void notifyLogout_WithNullUsername_DoesNotThrow() {
        assertDoesNotThrow(() -> loginNotifier.notifyLogout(null));
    }
}

