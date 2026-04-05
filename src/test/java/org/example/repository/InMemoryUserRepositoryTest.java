package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void findByEmail_DefaultAdminEmail_ReturnsAdminSeed() {
        Optional<SystemUser> result = repository.findByEmail("admin@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
        assertEquals("admin123", result.get().getPassword());
        assertEquals(UserRole.ADMIN, result.get().getRole());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findByEmail_DefaultUserEmail_ReturnsUserSeed() {
        Optional<SystemUser> result = repository.findByEmail("user@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("user@gmail.com", result.get().getEmail());
        assertEquals("user123", result.get().getPassword());
        assertEquals(UserRole.USER, result.get().getRole());
        assertEquals("user-1", result.get().getId());
    }

    @Test
    void findByEmail_WithTrimmedUppercaseInput_NormalizesLookup() {
        Optional<SystemUser> result = repository.findByEmail("  ADMIN@GMAIL.COM  ");

        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
    }

    @Test
    void findByEmail_NullInput_ReturnsEmpty() {
        Optional<SystemUser> result = repository.findByEmail(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_NewUser_PersistsAndCanBeQueriedByNormalizedEmail() {
        SystemUser newUser = new SystemUser("user-2", "Alice@Example.com", "alice123", UserRole.USER);

        repository.save(newUser);

        Optional<SystemUser> loaded = repository.findByEmail("  ALICE@EXAMPLE.COM ");
        assertTrue(loaded.isPresent());
        assertEquals("user-2", loaded.get().getId());
        assertEquals(UserRole.USER, loaded.get().getRole());
        assertEquals("alice@example.com", loaded.get().getEmail());
    }

    @Test
    void save_NullUser_IsIgnored() {
        repository.save(null);

        assertTrue(repository.findByEmail("admin@gmail.com").isPresent());
        assertTrue(repository.findByEmail("user@gmail.com").isPresent());
    }

    @Test
    void findAll_ReturnsImmutableSnapshotContainingBothDefaultUsers() {
        List<SystemUser> users = repository.findAll();

        assertTrue(users.stream().anyMatch(user -> "admin@gmail.com".equals(user.getEmail())));
        assertTrue(users.stream().anyMatch(user -> "user@gmail.com".equals(user.getEmail())));
        assertThrows(UnsupportedOperationException.class, () -> users.add(
                new SystemUser("id", "x@example.com", "pw", UserRole.USER)
        ));
    }
}
