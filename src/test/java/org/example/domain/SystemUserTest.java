package org.example.domain;

import org.example.service.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        assertNotEquals("secret", user.getPassword());
        assertTrue(PasswordHasher.isEncoded(user.getPassword()));
        assertTrue(user.passwordMatches("secret"));
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void constructor_WithExplicitId_TrimsIdAndNormalizesEmail() {
        // Arrange / Act
        SystemUser user = new SystemUser("  user-42  ", " User@Example.com ", "pw", UserRole.USER);

        // Assert
        assertEquals("user-42", user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertTrue(user.passwordMatches("pw"));
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
    void constructor_WithPasswordContainingSpaces_HashesRawPassword() {
        // Arrange / Act
        SystemUser user = new SystemUser("id", "user@example.com", "  pw  ", UserRole.USER);

        // Assert
        assertNotEquals("  pw  ", user.getPassword());
        assertTrue(user.passwordMatches("  pw  "));
    }
}
