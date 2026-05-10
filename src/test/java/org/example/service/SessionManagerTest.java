package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock
    private AuthEventLogger authEventLogger;

    @Mock
    private EventManager eventManager;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(authEventLogger, eventManager);
    }

    @Test
    void initialState_IsLoggedOutWithNoSessionData() {
        // Arrange / Act / Assert
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentEmail());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentUserRole());
        assertNull(sessionManager.getLoginTime());
        assertFalse(sessionManager.isAdmin());
        assertFalse(sessionManager.isUser());
    }

    @Test
    void login_WithEmail_DefaultsToUserRoleAndNormalizesEmail() {
        // Arrange / Act
        sessionManager.login("  User@Example.com  ");

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertNotNull(sessionManager.getLoginTime());
        assertEquals("user@example.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertNotNull(sessionManager.getCurrentUser());
        assertEquals("user@example.com-session", sessionManager.getCurrentUser().getId());
        assertTrue(sessionManager.isUser());
        assertFalse(sessionManager.isAdmin());
    }

    @Test
    void login_WithRole_SetsAdminSessionState() {
        // Arrange / Act
        sessionManager.login("admin@example.com", UserRole.ADMIN);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("admin@example.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.ADMIN, sessionManager.getCurrentUserRole());
        assertTrue(sessionManager.isAdmin());
        assertFalse(sessionManager.isUser());
    }

    @Test
    void login_WithSystemUser_StoresProvidedUserAndRole() {
        // Arrange
        SystemUser user = new SystemUser("user-1", "USER@Example.com", "user123", UserRole.USER);

        // Act
        sessionManager.login(user);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("user@example.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.USER, sessionManager.getCurrentUserRole());
        assertEquals(user, sessionManager.getCurrentUser());
    }

    @Test
    void login_NoArgBackwardCompatibleLogin_StartsAdminSession() {
        // Arrange / Act
        sessionManager.login();

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("admin@gmail.com", sessionManager.getCurrentEmail());
        assertEquals(UserRole.ADMIN, sessionManager.getCurrentUserRole());
        assertTrue(sessionManager.isAdmin());
    }

    @Test
    void login_WithBlankEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("   "));
    }

    @Test
    void login_WithNullEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login((String) null));
    }

    @Test
    void login_WithNullRole_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login("user@example.com", null));
    }

    @Test
    void login_WithNullUser_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> sessionManager.login((SystemUser) null));
    }

    @Test
    void logout_ClearsAllSessionState() {
        // Arrange
        sessionManager.login("admin@example.com", UserRole.ADMIN);

        // Act
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getCurrentEmail());
        assertNull(sessionManager.getCurrentUserRole());
        assertNull(sessionManager.getLoginTime());
    }

    @Test
    void logoutAndNotify_WhenLoggedIn_LogsAndPublishesGoodbyeMessage() {
        // Arrange
        sessionManager.login("  ADMIN@EXAMPLE.COM ", UserRole.ADMIN);

        // Act
        sessionManager.logoutAndNotify();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
        verify(authEventLogger).logLogout("admin@example.com");
        verify(eventManager).notifyObservers("Goodbye! You have been logged out.");
    }

    @Test
    void logoutAndNotify_WhenNoActiveSession_UsesUnknownPlaceholder() {
        // Arrange / Act
        sessionManager.logoutAndNotify();

        // Assert
        verify(authEventLogger).logLogout(null);
        verify(eventManager).notifyObservers("Goodbye! You have been logged out.");
    }

    @Test
    void constructor_WithNullDependencies_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(NullPointerException.class, () -> new SessionManager(null, eventManager));
        assertThrows(NullPointerException.class, () -> new SessionManager(authEventLogger, null));
    }
}
