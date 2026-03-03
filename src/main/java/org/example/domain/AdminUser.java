package org.example.domain;

/**
 * Simple admin user model for demo purposes.
 * NOTE: This demo stores plaintext passwords only for simplicity. Replace with hashed
 * storage (bcrypt/argon2) and a secure secret store for production use.
 */
public final class AdminUser {
    private final String username;
    private final String password;

    public AdminUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
