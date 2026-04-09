package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void findByEmail_DefaultSystemUser_IsPresentWithAdminRole() {
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
    void findById_WithExistingUserId_ReturnsUser() {
        Optional<SystemUser> result = repository.findById("admin-1");

        assertTrue(result.isPresent());
        assertEquals("admin@gmail.com", result.get().getEmail());
    }

    @Test
    void findById_WithUnknownUserId_ReturnsEmpty() {
        Optional<SystemUser> result = repository.findById("unknown-id");

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
    void seedUser_WithBlankId_UsesDerivedIdentifier() throws Exception {
        invokeSeedUser("   ", "New.Admin@Example.com", "secure-pass", UserRole.ADMIN);

        Optional<SystemUser> loaded = repository.findByEmail("new.admin@example.com");

        assertTrue(loaded.isPresent());
        assertEquals("new.admin@example.com-id", loaded.get().getId());
        assertEquals("secure-pass", loaded.get().getPassword());
        assertEquals(UserRole.ADMIN, loaded.get().getRole());
    }

    @Test
    void seedUser_WithMissingRequiredValues_IsIgnored() throws Exception {
        invokeSeedUser("ignored-1", null, "pw", UserRole.USER);
        invokeSeedUser("ignored-2", "   ", "pw", UserRole.USER);
        invokeSeedUser("ignored-3", "valid@example.com", null, UserRole.USER);
        invokeSeedUser("ignored-4", "valid@example.com", "   ", UserRole.USER);

        assertTrue(repository.findByEmail("valid@example.com").isEmpty());
        assertTrue(repository.findByEmail("ignored@example.com").isEmpty());
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

    @Test
    void updatePassword_WithExistingUser_ChangesStoredPassword() {
        boolean updated = repository.updatePassword("user-1", "updated123");

        assertTrue(updated);
        assertEquals("updated123", repository.findByEmail("user@gmail.com").orElseThrow().getPassword());
    }

    @Test
    void updatePassword_WithUnknownUser_ReturnsFalse() {
        boolean updated = repository.updatePassword("missing-user", "updated123");

        assertFalse(updated);
    }

    private void invokeSeedUser(String id, String email, String password, UserRole role) throws Exception {
        Method seedUser = InMemoryUserRepository.class.getDeclaredMethod(
                "seedUser",
                String.class,
                String.class,
                String.class,
                UserRole.class
        );
        seedUser.setAccessible(true);
        seedUser.invoke(repository, id, email, password, role);
    }
}
