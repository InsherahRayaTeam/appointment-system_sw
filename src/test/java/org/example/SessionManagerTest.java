package org.example;

import org.example.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    void initialState_NotLoggedIn() {
        // Assert
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    void login_SetsLoggedIn() {
        // Act
        sessionManager.login();

        // Assert
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    void logout_SetsNotLoggedIn() {
        // Arrange
        sessionManager.login();

        // Act
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    void logout_WhenNotLoggedIn_DoesNothing() {
        // Act
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
    }
}
