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
        assertThrows(IllegalArgumentException.class, () -> new Credentials(null, "pw"));
    }

    @Test
    void constructor_WithBlankEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Credentials("   ", "pw"));
    }

    @Test
    void constructor_WithNullPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Credentials("u@example.com", null));
    }

    @Test
    void constructor_WithBlankPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Credentials("u@example.com", "  "));
    }

    @Test
    void matches_WithTrimmedCaseInsensitiveEmailAndExactPassword_ReturnsTrue() {
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        assertTrue(credentials.matches("  ADMIN@example.com ", "admin123"));
    }

    @Test
    void matches_WithWrongPassword_ReturnsFalse() {
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        assertFalse(credentials.matches("admin@example.com", "wrong"));
    }

    @Test
    void matches_WithNullInputs_ReturnsFalse() {
        Credentials credentials = new Credentials("admin@example.com", "admin123");

        assertFalse(credentials.matches(null, "admin123"));
        assertFalse(credentials.matches("admin@example.com", null));
    }

    @Test
    void equalsAndHashCode_UsesNormalizedEmailOnly() {
        Credentials left = new Credentials("  USER@EXAMPLE.COM ", "pw1");
        Credentials right = new Credentials("user@example.com", "pw2");
        Credentials other = new Credentials("other@example.com", "pw1");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, other);
    }

    @Test
    void toString_DoesNotExposePassword() {
        Credentials credentials = new Credentials("admin@example.com", "secret");
        String text = credentials.toString();

        assertTrue(text.contains("admin@example.com"));
        assertFalse(text.contains("secret"));
    }
}
