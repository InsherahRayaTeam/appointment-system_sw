package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemUserTest {

    @Test
    void constructor_WithGeneratedId_NormalizesEmailAndGeneratesTrimmedId() {
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
        assertEquals("pw", user.getPassword());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void constructor_WithGeneratedIdAndBlankEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new SystemUser("   ", "pw", UserRole.USER));
    }

    @Test
    void constructor_WithGeneratedIdAndNullEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new SystemUser(null, "pw", UserRole.USER));
    }

    @Test
    void constructor_WithExplicitIdAndNullId_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new SystemUser(null, "user@example.com", "pw", UserRole.USER)
        );
    }

    @Test
    void constructor_WithExplicitIdAndBlankId_ThrowsException() {
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

    @Test
    void constructor_WithPasswordContainingSpaces_PreservesRawPassword() {
        // Arrange / Act
        SystemUser user = new SystemUser("id", "user@example.com", "  pw  ", UserRole.USER);

        // Assert
        assertEquals("  pw  ", user.getPassword());
        assertTrue(user.getPassword().startsWith(" "));
    }
}
