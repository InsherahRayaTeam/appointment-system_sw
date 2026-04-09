package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Represents in memory user repository in the system.
 */
public class InMemoryUserRepository implements UserRepository {

    private static final String RESOURCE = "/admin.properties";
    private final Map<String, SystemUser> usersByEmail = new HashMap<>();

    /**
     * Creates a new in memory user repository object with the given values.
     */
    public InMemoryUserRepository() {
        loadFromResource();
    }

    private void loadFromResource() {
        Properties p = new Properties();

        try (InputStream in = InMemoryUserRepository.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                p.load(in);
                seedUser(
                        p.getProperty("admin.id", "admin-1"),
                        p.getProperty("admin.email"),
                        p.getProperty("admin.password"),
                        UserRole.ADMIN
                );
                seedUser(
                        p.getProperty("user.id", "user-1"),
                        p.getProperty("user.email"),
                        p.getProperty("user.password"),
                        UserRole.USER
                );
            }
        } catch (IOException e) {
            // Fall back to defaults if loading fails
        }

        // Defaults when properties are missing or incomplete.
        if (!usersByEmail.containsKey("admin@gmail.com")) {
            usersByEmail.put(
                    "admin@gmail.com",
                    new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN)
            );
        }
        if (!usersByEmail.containsKey("user@gmail.com")) {
            usersByEmail.put(
                    "user@gmail.com",
                    new SystemUser("user-1", "user@gmail.com", "user123", UserRole.USER)
            );
        }
    }

    private void seedUser(String id, String email, String password, UserRole role) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        usersByEmail.put(
                normalizedEmail,
                new SystemUser(
                        (id == null || id.trim().isEmpty()) ? normalizedEmail + "-id" : id.trim(),
                        normalizedEmail,
                        password.trim(),
                        role
                )
        );
    }

    /**
     * Finds by email using the given input.
     *
     * @param email email address used for login or matching
     *
     * @return optional value if data is found
     */
    @Override
    public Optional<SystemUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(email.trim().toLowerCase()));
    }

    /**
     * Finds by id using the given input.
     *
     * @param id unique id used to find the record
     *
     * @return optional value if data is found
     */
    @Override
    public Optional<SystemUser> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedId = id.trim();
        for (SystemUser user : usersByEmail.values()) {
            if (normalizedId.equalsIgnoreCase(user.getId())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Runs save for this class.
     *
     * @param user user involved in this action
     */
    @Override
    public void save(SystemUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return;
        }
        usersByEmail.put(user.getEmail().trim().toLowerCase(), user);
    }

    /**
     * Updates user password when record exists.
     *
     * @param userId unique id used to find the record
     * @param newPassword password text entered by the user
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean updatePassword(String userId, String newPassword) {
        if (userId == null || userId.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        Optional<SystemUser> existing = findById(userId);
        if (existing.isEmpty()) {
            return false;
        }

        SystemUser current = existing.get();
        SystemUser updated = new SystemUser(
                current.getId(),
                current.getEmail(),
                newPassword.trim(),
                current.getRole()
        );
        usersByEmail.put(current.getEmail().toLowerCase(), updated);
        return true;
    }

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    @Override
    public List<SystemUser> findAll() {
        return List.copyOf(usersByEmail.values());
    }
}
