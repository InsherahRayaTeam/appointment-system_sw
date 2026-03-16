package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredentialsTest {

    @Test
    void constructor_CorrectlyStoresUsernameAndPassword() {
        Credentials credentials = new Credentials("admin", "admin123");

        assertEquals("admin", credentials.getUsername());
        assertEquals("admin123", credentials.getPassword());
    }

    @Test
    void getUsername_ReturnsCorrectUsername() {
        Credentials credentials = new Credentials("admin", "admin123");

        assertEquals("admin", credentials.getUsername());
    }

    @Test
    void getPassword_ReturnsCorrectPassword() {
        Credentials credentials = new Credentials("admin", "admin123");

        assertEquals("admin123", credentials.getPassword());
    }
}

