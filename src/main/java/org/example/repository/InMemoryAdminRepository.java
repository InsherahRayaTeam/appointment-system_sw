package org.example.repository;

import org.example.domain.AdminUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
    private final Map<String, AdminUser> admins = new HashMap<>();

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
                String user = p.getProperty("admin.username");
                String pass = p.getProperty("admin.password");
                if (user != null && pass != null) {
                    admins.put(user.trim(), new AdminUser(user.trim(), pass.trim()));
                }
            }
        } catch (IOException e) {
            // Fall back to default if loading fails
        }

        // Default admin if not loaded
        if (admins.isEmpty()) {
            admins.put("admin", new AdminUser("admin", "admin"));
        }
    }

    /**
     * Finds an admin user by username.
     *
     * @param username username to look up
     * @return optional admin user
     */
    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return Optional.ofNullable(admins.get(username));
    }
}