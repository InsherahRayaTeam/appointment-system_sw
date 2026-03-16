package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminUserTest {

    @Test
    void constructor_CorrectlyStoresUsernameAndPassword() {
        AdminUser adminUser = new AdminUser("admin", "admin123");

        assertEquals("admin", adminUser.getUsername());
        assertEquals("admin123", adminUser.getPassword());
    }

    @Test
    void getUsername_ReturnsCorrectUsername() {
        AdminUser adminUser = new AdminUser("admin", "admin123");

        assertEquals("admin", adminUser.getUsername());
    }

    @Test
    void getPassword_ReturnsCorrectPassword() {
        AdminUser adminUser = new AdminUser("admin", "admin123");

        assertEquals("admin123", adminUser.getPassword());
    }
}

