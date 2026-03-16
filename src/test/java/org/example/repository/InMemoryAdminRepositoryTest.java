package org.example.repository;

import org.example.domain.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryAdminRepositoryTest {

    private InMemoryAdminRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAdminRepository();
    }

    @Test
    void findByUsername_DefaultAdminUsername_ReturnsDefaultAdmin() {
        Optional<AdminUser> result = repository.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("admin", result.get().getPassword());
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        Optional<AdminUser> result = repository.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
        assertFalse(result.isPresent());
    }

    @Test
    void repository_LoadsDefaultAdminWhenPropertiesFileMissing() {
        // When properties file is missing or cannot be loaded,
        // the repository defaults to "admin" / "admin"
        Optional<AdminUser> result = repository.findByUsername("admin");

        assertTrue(result.isPresent());
        AdminUser admin = result.get();
        assertEquals("admin", admin.getUsername());
        assertEquals("admin", admin.getPassword());
    }
}

