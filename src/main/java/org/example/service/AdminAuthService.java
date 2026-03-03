package org.example.service;

import org.example.domain.AdminUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Optional;

/**
 * Authentication service for administrator. Loads credentials from
 * classpath resource `admin.properties`.
 */
public class AdminAuthService {
    private static final String RESOURCE = "/admin.properties";
    private final AdminUser adminUser;

    public AdminAuthService() {
        this.adminUser = loadFromResource().orElseGet(() -> new AdminUser("admin", "admin"));
    }

    Optional<AdminUser> loadFromResource() {
        Properties p = new Properties();
        try (InputStream in = AdminAuthService.class.getResourceAsStream(RESOURCE)) {
            if (in == null) return Optional.empty();
            p.load(in);
            String user = p.getProperty("admin.username");
            String pass = p.getProperty("admin.password");
            if (user == null || pass == null) return Optional.empty();
            return Optional.of(new AdminUser(user.trim(), pass.trim()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Authenticate a username and password against loaded admin credentials.
     * Plaintext comparison for demo only.
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        return adminUser.getUsername().equals(username) && adminUser.getPassword().equals(password);
    }
}
