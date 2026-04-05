package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemUserTest {

    @Test
    void constructor_WithGeneratedId_NormalizesEmailAndAssignsFields() {
        // Arrange / Act
        SystemUser user = new SystemUser("  ADMIN@GMAIL.COM  ", "secret", UserRole.ADMIN);

        // Assert
        assertEquals("admin@gmail.com-id", user.getId());
        assertEquals("admin@gmail.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void constructor_WithExplicitId_TrimsIdAndNormalizesEmail() {
        // Arrange / Act
        SystemUser user = new SystemUser("  user-42  ", " User@Example.com ", "pw", UserRole.USER);

        // Assert
        assertEquals("user-42", user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void constructor_WithNullGeneratedEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new SystemUser(null, "pw", UserRole.USER));
    }

    @Test
    void constructor_WithBlankId_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new SystemUser("   ", "user@example.com", "pw", UserRole.USER)
        );
    }

    @Test
    void constructor_WithBlankEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new SystemUser("id", "   ", "pw", UserRole.USER)
        );
    }

    @Test
    void constructor_WithBlankPassword_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new SystemUser("id", "user@example.com", "   ", UserRole.USER)
        );
    }

    @Test
    void constructor_WithNullRole_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new SystemUser("id", "user@example.com", "pw", null)
        );
    }
}


