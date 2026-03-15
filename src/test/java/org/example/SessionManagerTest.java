package org.example;

import org.example.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    void initialState_NotLoggedIn() {
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void login_SetsSessionData() {
        sessionManager.login("admin");

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.getLoginTime() != null);
        assertTrue("admin".equals(sessionManager.getCurrentUsername()));
    }

    @Test
    void login_TrimmedUsername_SetsNormalizedSessionUser() {
        sessionManager.login(" admin ");

        assertTrue("admin".equals(sessionManager.getCurrentUsername()));
    }

    @Test
    void login_BlankUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("   "));
    }

    @Test
    void login_BackwardCompatibleNoArgLogin_SetsLoggedIn() {
        sessionManager.login();

        assertTrue(sessionManager.isLoggedIn());
        assertTrue("admin".equals(sessionManager.getCurrentUsername()));
    }

    @Test
    void logout_ClearsSessionData() {
        sessionManager.login("admin");

        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void logout_WhenNotLoggedIn_DoesNothing() {
        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        assertNull(sessionManager.getLoginTime());
    }
}
