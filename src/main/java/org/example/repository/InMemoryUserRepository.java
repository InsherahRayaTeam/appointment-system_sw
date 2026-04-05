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
 * In-memory user repository backed by optional properties-file seed data.
 *
 * @author appointment-system
 * @version 1.0
 */
public class InMemoryUserRepository implements UserRepository {

    private static final String RESOURCE = "/admin.properties";
    private final Map<String, SystemUser> usersByEmail = new HashMap<>();

    /**
     * Creates the repository and loads default seed data.
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
     * Finds a user by email.
     *
     * @param email email to look up
     * @return optional user
     */
    @Override
    public Optional<SystemUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(email.trim().toLowerCase()));
    }

    /**
     * Saves or replaces a user by email.
     *
     * @param user user to persist
     */
    @Override
    public void save(SystemUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return;
        }
        usersByEmail.put(user.getEmail().trim().toLowerCase(), user);
    }

    /**
     * Returns all users currently stored in memory.
     *
     * @return list of users
     */
    @Override
    public List<SystemUser> findAll() {
        return List.copyOf(usersByEmail.values());
    }
}