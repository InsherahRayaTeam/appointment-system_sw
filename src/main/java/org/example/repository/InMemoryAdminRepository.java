package org.example.repository;

import org.example.domain.AdminUser;
import org.example.domain.UserRole;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * In-memory admin repository backed by optional properties-file seed data.
 *
 * @author appointment-system
 * @version 1.0
 */
public class InMemoryAdminRepository implements AdminRepository {

    private static final String RESOURCE = "/admin.properties";
    private final Map<String, AdminUser> usersByUsername = new HashMap<>();

    /**
     * Creates the repository and loads default/admin seed data.
     */
    public InMemoryAdminRepository() {
        loadFromResource();
    }

    private void loadFromResource() {
        Properties p = new Properties();

        try (InputStream in = InMemoryAdminRepository.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                p.load(in);
                seedUser(
                        p.getProperty("admin.id", "admin-1"),
                        p.getProperty("admin.username"),
                        p.getProperty("admin.password"),
                        UserRole.ADMIN
                );
                seedUser(
                        p.getProperty("user.id", "user-1"),
                        p.getProperty("user.username"),
                        p.getProperty("user.password"),
                        UserRole.USER
                );
            }
        } catch (IOException e) {
            // Fall back to default if loading fails
        }

        // Defaults when properties are missing or incomplete.
        if (!usersByUsername.containsKey("admin")) {
            usersByUsername.put("admin", new AdminUser("admin-1", "admin", "admin123", UserRole.ADMIN));
        }
        if (!usersByUsername.containsKey("user")) {
            usersByUsername.put("user", new AdminUser("user-1", "user", "user123", UserRole.USER));
        }
    }

    private void seedUser(String id, String username, String password, UserRole role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return;
        }
        String normalizedUsername = username.trim();
        usersByUsername.put(
                normalizedUsername,
                new AdminUser(id == null || id.trim().isEmpty() ? normalizedUsername + "-id" : id.trim(),
                        normalizedUsername,
                        password.trim(),
                        role)
        );
    }

    /**
     * Finds an admin user by username.
     *
     * @param username username to look up
     * @return optional admin user
     */
    @Override
    public Optional<AdminUser> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username.trim()));
    }

    /**
     * Saves or replaces a user by username.
     *
     * @param user user to persist
     */
    @Override
    public void save(AdminUser user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return;
        }
        usersByUsername.put(user.getUsername().trim(), user);
    }

    /**
     * Returns all users currently stored in memory.
     *
     * @return list of users
     */
    @Override
    public List<AdminUser> findAll() {
        return List.copyOf(usersByUsername.values());
    }
}