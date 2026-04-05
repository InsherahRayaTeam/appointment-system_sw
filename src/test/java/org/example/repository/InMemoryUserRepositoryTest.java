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
    void findByEmail_DefaultAdminUser_IsPresentWithAdminRole() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail("admin@gmail.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
        assertEquals(UserRole.ADMIN, result.get().getRole());
    }

    @Test
    void findByEmail_DefaultRegularUser_IsPresentWithUserRole() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail("user@gmail.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user@gmail.com", result.get().getEmail());
        assertEquals(UserRole.USER, result.get().getRole());
    }

    @Test
    void findByEmail_WithTrimmedUppercaseInput_UsesNormalizedLookup() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail("  ADMIN@GMAIL.COM  ");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
    }

    @Test
    void findByEmail_WithNullInput_ReturnsEmptyOptional() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void save_WithNewUser_PersistsByNormalizedEmail() {
        // Arrange
        SystemUser newUser = new SystemUser("user-2", " Alice@Example.com ", "alice123", UserRole.USER);

        // Act
        repository.save(newUser);
        Optional<SystemUser> loaded = repository.findByEmail("ALICE@EXAMPLE.COM");

        // Assert
        assertTrue(loaded.isPresent());
        assertEquals("user-2", loaded.get().getId());
        assertEquals("alice@example.com", loaded.get().getEmail());
        assertEquals(UserRole.USER, loaded.get().getRole());
    }

    @Test
    void save_WithExistingEmail_OverwritesStoredRecord() {
        // Arrange
        SystemUser replacement = new SystemUser("admin-replacement", "admin@gmail.com", "new-pass", UserRole.ADMIN);

        // Act
        repository.save(replacement);
        Optional<SystemUser> loaded = repository.findByEmail("admin@gmail.com");

        // Assert
        assertTrue(loaded.isPresent());
        assertEquals("admin-replacement", loaded.get().getId());
        assertEquals("new-pass", loaded.get().getPassword());
    }

    @Test
    void save_WithNullUser_IsIgnored() {
        // Arrange / Act
        repository.save(null);

        // Assert
        assertTrue(repository.findByEmail("admin@gmail.com").isPresent());
        assertTrue(repository.findByEmail("user@gmail.com").isPresent());
    }

    @Test
    void findAll_ReturnsImmutableSnapshot() {
        // Arrange
        List<SystemUser> initialSnapshot = repository.findAll();

        // Act / Assert
        assertThrows(
                UnsupportedOperationException.class,
                () -> initialSnapshot.add(new SystemUser("id", "x@example.com", "pw", UserRole.USER))
        );

        repository.save(new SystemUser("new-user", "new@example.com", "pw", UserRole.USER));
        List<SystemUser> secondSnapshot = repository.findAll();

        assertEquals(initialSnapshot.size() + 1, secondSnapshot.size());
    }
}
