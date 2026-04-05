package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminUserTest {

    @Test
    void constructor_CorrectlyStoresUsernameAndPassword() {
        AdminUser adminUser = new AdminUser("admin", "admin123");

        assertEquals("admin", SystemUser.getUsername());
        assertEquals("admin123", SystemUser.getPassword());
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

    @Test
    void constructor_DefaultRoleIsAdmin() {
        SystemUser adminUser = new SystemUser("admin", "admin123");

        assertEquals(UserRole.ADMIN, adminUser.getRole());
    }

    @Test
    void constructor_AssignsIdentifier() {
        SystemUser adminUser = new SystemUser("", "admin123");

        assertEquals("admin-id", SystemUser.getId());
    }
}

