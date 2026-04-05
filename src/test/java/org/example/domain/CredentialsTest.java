package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialsTest {

    @Test
    void constructor_NormalizesEmailAndStoresPassword() {
        // Arrange / Act
        Credentials credentials = new Credentials("  ADMIN@Example.COM  ", "admin123");

        // Assert
        assertEquals("admin@example.com", credentials.getEmail());
        assertEquals("admin123", credentials.getPassword());
    }

    @Test
    void constructor_WithNullEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new Credentials(null, "pw"));
    }

    @Test
    void constructor_WithBlankEmail_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new Credentials("   ", "pw"));
    }

    @Test
    void constructor_WithNullPassword_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new Credentials("u@example.com", null));
    }

    @Test
    void constructor_WithBlankPassword_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new Credentials("u@example.com", "  "));
    }

    @Test
    void matches_WithTrimmedCaseInsensitiveEmailAndExactPassword_ReturnsTrue() {
        // Arrange
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        // Act
        boolean matches = credentials.matches("  ADMIN@example.com ", "admin123");

        // Assert
        assertTrue(matches);
    }

    @Test
    void matches_WithWrongEmail_ReturnsFalse() {
        // Arrange
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        // Act
        boolean matches = credentials.matches("other@example.com", "admin123");

        // Assert
        assertFalse(matches);
    }

    @Test
    void matches_WithWrongPassword_ReturnsFalse() {
        // Arrange
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        // Act
        boolean matches = credentials.matches("admin@example.com", "wrong");

        // Assert
        assertFalse(matches);
    }

    @Test
    void matches_WithNullInputs_ReturnsFalse() {
        // Arrange
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        // Act / Assert
        assertFalse(credentials.matches(null, "admin123"));
        assertFalse(credentials.matches("admin@example.com", null));
    }

    @Test
    void equalsAndHashCode_UsesNormalizedEmailOnly() {
        // Arrange
        Credentials left = new Credentials("  USER@EXAMPLE.COM ", "pw1");
        Credentials right = new Credentials("user@example.com", "pw2");
        Credentials other = new Credentials("other@example.com", "pw1");

        // Act / Assert
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, other);
    }

    @Test
    void toString_DoesNotExposePassword() {
        // Arrange
        Credentials credentials = new Credentials("admin@example.com", "secret");

        // Act
        String text = credentials.toString();

        // Assert
        assertTrue(text.contains("admin@example.com"));
        assertFalse(text.contains("secret"));
    }
}
