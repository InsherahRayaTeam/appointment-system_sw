package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        Optional<SystemUser> result = repository.findByEmail("insherah2004@gmail.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("insherah2004@gmail.com", result.get().getEmail());
        assertEquals(UserRole.ADMIN, result.get().getRole());
    }

    @Test
    void findByEmail_DefaultRegularUser_IsPresentWithUserRole() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail("insherahdwikat@gmail.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("insherahdwikat@gmail.com", result.get().getEmail());
        assertEquals(UserRole.USER, result.get().getRole());
    }

    @Test
    void findByEmail_NumberedRegularUserFromProperties_IsLoaded() {
        Optional<SystemUser> result = repository.findByEmail("mlkschool10@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("mlkschool10@gmail.com", result.get().getEmail());
        assertEquals(UserRole.USER, result.get().getRole());
    }

    @Test
    void findByEmail_WithTrimmedUppercaseInput_UsesNormalizedLookup() {
        // Arrange / Act
        Optional<SystemUser> result = repository.findByEmail("  INSHERAH2004@GMAIL.COM  ");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("insherah2004@gmail.com", result.get().getEmail());
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
        assertEquals("admin-1", result.get().getId());
        assertEquals(UserRole.ADMIN, result.get().getRole());
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
        SystemUser replacement = new SystemUser("admin-replacement", "insherah2004@gmail.com", "new-pass", UserRole.ADMIN);

        // Act
        repository.save(replacement);
        Optional<SystemUser> loaded = repository.findByEmail("insherah2004@gmail.com");

        // Assert
        assertTrue(loaded.isPresent());
        assertEquals("admin-replacement", loaded.get().getId());
        assertNotEquals("new-pass", loaded.get().getPassword());
        assertTrue(loaded.get().passwordMatches("new-pass"));
    }

    @Test
    void save_WithNullUser_IsIgnored() {
        // Arrange / Act
        repository.save(null);

        // Assert
        assertTrue(repository.findByEmail("insherah2004@gmail.com").isPresent());
        assertTrue(repository.findByEmail("insherahdwikat@gmail.com").isPresent());
    }

    @Test
    void seedUser_WithBlankId_UsesDerivedIdentifier() throws Exception {
        invokeSeedUser("   ", "New.Admin@Example.com", "secure-pass", UserRole.ADMIN);

        Optional<SystemUser> loaded = repository.findByEmail("new.admin@example.com");

        assertTrue(loaded.isPresent());
        assertEquals("new.admin@example.com-id", loaded.get().getId());
        assertTrue(PasswordHasher.isEncoded(loaded.get().getPassword()));
        assertTrue(loaded.get().passwordMatches("secure-pass"));
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
        SystemUser updatedUser = repository.findById("user-1").orElseThrow();
        assertNotEquals("updated123", updatedUser.getPassword());
        assertTrue(updatedUser.passwordMatches("updated123"));
    }

    @Test
    void updatePassword_WithUnknownUser_ReturnsFalse() {
        boolean updated = repository.updatePassword("missing-user", "updated123");

        assertFalse(updated);
    }

    @Test
    void update_WithExistingEmail_ReplacesStoredUser() {
        SystemUser updatedUser = new SystemUser("user-1", "insherahdwikat@gmail.com", "changed123", UserRole.USER);

        boolean updated = repository.update(updatedUser);

        assertTrue(updated);
        SystemUser loaded = repository.findByEmail("insherahdwikat@gmail.com").orElseThrow();
        assertNotEquals("changed123", loaded.getPassword());
        assertTrue(loaded.passwordMatches("changed123"));
    }

    @Test
    void update_WithUnknownEmail_ReturnsFalse() {
        SystemUser missingUser = new SystemUser("new-id", "missing@example.com", "changed123", UserRole.USER);

        boolean updated = repository.update(missingUser);

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
