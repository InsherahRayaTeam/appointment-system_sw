package org.example.service;

import org.example.domain.UserRole;
import org.example.domain.AdminUser;

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

public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(mock(AuthEventLogger.class), mock(EventManager.class));
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
        assertNotNull(sessionManager.getLoginTime());
        assertEquals("admin", sessionManager.getCurrentUsername());
        assertEquals(UserRole.ADMIN, sessionManager.getCurrentUserRole());
        assertNotNull(sessionManager.getCurrentUser());
    }

    @Test
    void login_WithRegularUserRole_SetsUserRoleState() {
        sessionManager.login("user", UserRole.USER);

        assertTrue(sessionManager.isLoggedIn());
        assertTrue(sessionManager.isUser());
        assertFalse(sessionManager.isAdmin());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertEquals("user", sessionManager.getCurrentUser().getUsername());
    }

    @Test
    void login_WithUserObject_SetsCurrentUserDirectly() {
        AdminUser user = new AdminUser("user-1", "user", "user123", UserRole.USER);

        sessionManager.login(user);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("user", sessionManager.getCurrentUsername());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertEquals("user-1", sessionManager.getCurrentUser().getId());
    }

    @Test
    void login_TrimmedUsername_SetsNormalizedSessionUser() {
        sessionManager.login(" admin ");

        assertEquals("admin", sessionManager.getCurrentUsername());
    }

    @Test
    void login_BlankUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("   "));
    }

    @Test
    void login_BackwardCompatibleNoArgLogin_SetsLoggedIn() {
        sessionManager.login();

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("admin", sessionManager.getCurrentUsername());
    }

    @Test
    void logout_ClearsSessionData() {
        sessionManager.login("admin");

        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentUsername());
        assertNull(sessionManager.getCurrentUserRole());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void logout_UserIsNoLongerAuthenticated() {
        sessionManager.login("admin");

        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    void logout_WhenNotLoggedIn_DoesNothing() {
        sessionManager.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        assertNull(sessionManager.getLoginTime());
    }
    @Test
    void login_NullUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login(null));
    }

    @Test
    void login_NullRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("user", null));
    }

    @Test
    void logoutAndNotify_WithLoggedInUser_LogsAndNotifiesThroughServices() {
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        EventManager eventManager = mock(EventManager.class);
        SessionManager managedSession = new SessionManager(authEventLogger, eventManager);
        managedSession.login("admin");

        managedSession.logoutAndNotify();

        assertFalse(managedSession.isLoggedIn());
        assertNull(managedSession.getCurrentUsername());
        verify(authEventLogger).logLogout("admin");
        verify(eventManager).notifyObservers("Goodbye, admin! You have been logged out.");
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
