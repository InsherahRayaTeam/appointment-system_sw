package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemUserCompatibilityTest {

    @Test
    void constructor_WithGeneratedId_StoresNormalizedEmailPasswordAndRole() {
        SystemUser systemUser = new SystemUser("admin@gmail.com", "admin123", UserRole.ADMIN);

        assertEquals("admin@gmail.com-id", systemUser.getId());
        assertEquals("admin@gmail.com", systemUser.getEmail());
        assertEquals("admin123", systemUser.getPassword());
        assertEquals(UserRole.ADMIN, systemUser.getRole());
    }

    @Test
    void constructor_WithExplicitId_AssignsIdentifier() {
        SystemUser systemUser = new SystemUser("admin-id", "admin@gmail.com", "admin123", UserRole.ADMIN);

        assertEquals("admin-id", systemUser.getId());
    }

    @Test
    void getEmail_ReturnsCorrectEmail() {
        SystemUser systemUser = new SystemUser("admin@gmail.com", "admin123", UserRole.ADMIN);

        assertEquals("admin@gmail.com", systemUser.getEmail());
    }

    @Test
    void getPassword_ReturnsCorrectPassword() {
        SystemUser systemUser = new SystemUser("admin@gmail.com", "admin123", UserRole.ADMIN);

        assertEquals("admin123", systemUser.getPassword());
    }

    @Test
    void getRole_ReturnsCorrectRole() {
        SystemUser systemUser = new SystemUser("admin@gmail.com", "admin123", UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, systemUser.getRole());
    }
}
