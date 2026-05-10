package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void encode_WithRawPassword_ReturnsSaltedHash() {
        String rawPassword = "valid123";

        String encodedPassword = PasswordHasher.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(PasswordHasher.isEncoded(encodedPassword));
        assertTrue(PasswordHasher.matches(rawPassword, encodedPassword));
    }

    @Test
    void encode_SamePasswordTwice_UsesDifferentSalt() {
        String rawPassword = "valid123";

        String firstHash = PasswordHasher.encode(rawPassword);
        String secondHash = PasswordHasher.encode(rawPassword);

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordHasher.matches(rawPassword, firstHash));
        assertTrue(PasswordHasher.matches(rawPassword, secondHash));
    }

    @Test
    void matches_WithWrongPassword_ReturnsFalse() {
        String encodedPassword = PasswordHasher.encode("valid123");

        assertFalse(PasswordHasher.matches("invalid123", encodedPassword));
    }
}
