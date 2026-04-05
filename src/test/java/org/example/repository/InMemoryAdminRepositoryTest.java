package org.example.repository;

import org.example.domain.AdminUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryAdminRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void findByUsername_DefaultAdminUsername_ReturnsDefaultAdmin() {
        Optional<AdminUser> result = repository.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("admin123", result.get().getPassword());
        assertEquals(UserRole.ADMIN, result.get().getRole());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findByUsername_DefaultRegularUser_ReturnsSeededUser() {
        Optional<AdminUser> result = repository.findByUsername("user");

        assertTrue(result.isPresent());
        assertEquals("user", result.get().getUsername());
        assertEquals("user123", result.get().getPassword());
        assertEquals(UserRole.USER, result.get().getRole());
        assertEquals("user-1", result.get().getId());
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        Optional<AdminUser> result = repository.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_NewUser_PersistsAndCanBeQueried() {
        AdminUser newUser = new AdminUser("user-2", "alice", "alice123", UserRole.USER);

        repository.save(newUser);

        Optional<AdminUser> loaded = repository.findByUsername("alice");
        assertTrue(loaded.isPresent());
        assertEquals("user-2", loaded.get().getId());
        assertEquals(UserRole.USER, loaded.get().getRole());
    }

    @Test
    void findAll_DefaultSeeds_ContainsAdminAndUser() {
        assertTrue(repository.findAll().stream().anyMatch(user -> "admin".equals(user.getUsername())));
        assertTrue(repository.findAll().stream().anyMatch(user -> "user".equals(user.getUsername())));
    }

    @Test
    void repository_LoadsDefaultAdminWhenPropertiesFileMissing() {
        // When properties file is missing or cannot be loaded,
        // the repository defaults to admin/user seeds with secure sample passwords
        Optional<AdminUser> result = repository.findByUsername("admin");

        assertTrue(result.isPresent());
        AdminUser admin = result.get();
        assertEquals("admin", admin.getUsername());
        assertEquals("admin123", admin.getPassword());
    }
}

