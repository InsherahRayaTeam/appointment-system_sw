package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(mock(AuthEventLogger.class), mock(EventManager.class));
    }

    @Test
    void initialState_NotLoggedIn() {
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentEmail());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentUserRole());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void login_WithEmail_DefaultsToUserRoleAndNormalizesEmail() {
        sessionManager.login("  User@Example.com  ");

        assertTrue(sessionManager.isLoggedIn());
        assertNotNull(sessionManager.getLoginTime());
        assertEquals("user@example.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertNotNull(sessionManager.getCurrentUser());
        assertTrue(sessionManager.isUser());
        assertFalse(sessionManager.isAdmin());
    }

    @Test
    void login_WithAdminRole_SetsAdminSessionState() {
        sessionManager.login("admin@example.com", UserRole.ADMIN);

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.isAdmin());
        assertFalse(sessionManager.isUser());
        assertEquals(UserRole.ADMIN, sessionManager.getCurrentUserRole());
    }

    @Test
    void login_WithUserObject_SetsCurrentUserDirectly() {
        SystemUser user = new SystemUser("user-1", "USER@Example.com", "user123", UserRole.USER);

        sessionManager.login(user);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("user@example.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertEquals("user-1", sessionManager.getCurrentUser().getId());
        assertEquals(user, sessionManager.getCurrentUser());
    }

    @Test
    void login_BlankEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("   "));
    }

    @Test
    void login_BackwardCompatibleNoArgLogin_SetsLoggedIn() {
        sessionManager.login();

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("admin@gmail.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.ADMIN, sessionManager.getCurrentUserRole());
    }

    @Test
    void logout_ClearsSessionData() {
        sessionManager.login("admin@example.com");

        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentEmail());
        assertNull(sessionManager.getCurrentUserRole());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void logout_WhenAlreadyLoggedOut_RemainsSafeNoStateChange() {
        sessionManager.logout();

        assertNull(sessionManager.getCurrentEmail());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void login_NullEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login(null));
    }

    @Test
    void login_NullRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("user", null));
    }

    @Test
    void login_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login((SystemUser) null));
    }

    @Test
    void logoutAndNotify_WithLoggedInUser_LogsAndNotifiesThroughServices() {
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        EventManager eventManager = mock(EventManager.class);
        SessionManager managedSession = new SessionManager(authEventLogger, eventManager);
        managedSession.login("  ADMIN@EXAMPLE.COM ");

        managedSession.logoutAndNotify();

        assertFalse(managedSession.isLoggedIn());
        assertNull(managedSession.getCurrentEmail());
        verify(authEventLogger).logLogout("admin@example.com");
        verify(eventManager).notifyObservers("Goodbye, admin@example.com! You have been logged out.");
    }

    @Test
    void logoutAndNotify_WithoutUser_UsesUnknownPlaceholder() {
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        EventManager eventManager = mock(EventManager.class);
        SessionManager managedSession = new SessionManager(authEventLogger, eventManager);

        managedSession.logoutAndNotify();

        verify(authEventLogger).logLogout(null);
        verify(eventManager).notifyObservers("Goodbye, <unknown>! You have been logged out.");
    }

    @Test
    void constructor_NullLogger_ThrowsException() {
        assertThrows(NullPointerException.class, () -> new SessionManager(null, mock(EventManager.class)));
    }

    @Test
    void constructor_NullEventManager_ThrowsException() {
        assertThrows(NullPointerException.class, () -> new SessionManager(mock(AuthEventLogger.class), null));
    }
}
