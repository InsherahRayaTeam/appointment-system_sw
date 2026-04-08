package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemorySystemUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void findByEmail_DefaultAdminEmail_ReturnsDefaultAdmin() {
        Optional<SystemUser> result = repository.findByEmail("admin@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
        assertEquals("admin123", result.get().getPassword());
        assertEquals(UserRole.ADMIN, result.get().getRole());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findByEmail_DefaultRegularUser_ReturnsSeededUser() {
        Optional<SystemUser> result = repository.findByEmail("user@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("user@gmail.com", result.get().getEmail());
        assertEquals("user123", result.get().getPassword());
        assertEquals(UserRole.USER, result.get().getRole());
        assertEquals("user-1", result.get().getId());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        Optional<SystemUser> result = repository.findByEmail("nonexistent@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_NewUser_PersistsAndCanBeQueried() {
        SystemUser newUser = new SystemUser("user-2", "alice@example.com", "alice123", UserRole.USER);

        repository.save(newUser);

        Optional<SystemUser> loaded = repository.findByEmail("alice@example.com");
        assertTrue(loaded.isPresent());
        assertEquals("user-2", loaded.get().getId());
        assertEquals(UserRole.USER, loaded.get().getRole());
    }

    @Test
    void findAll_DefaultSeeds_ContainsAdminAndUser() {
        assertTrue(repository.findAll().stream().anyMatch(user -> "admin@gmail.com".equals(user.getEmail())));
        assertTrue(repository.findAll().stream().anyMatch(user -> "user@gmail.com".equals(user.getEmail())));
    }

    @Test
    void repository_LoadsDefaultAdminWhenPropertiesFileMissing() {
        Optional<SystemUser> result = repository.findByEmail("admin@gmail.com");

        assertTrue(result.isPresent());
        SystemUser admin = result.get();
        assertEquals("admin@gmail.com", admin.getEmail());
        assertEquals("admin123", admin.getPassword());
    }
}
