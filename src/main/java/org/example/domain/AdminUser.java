package org.example.domain;

/**
 * Simple admin user model for demo purposes.
 * NOTE: This demo stores plaintext passwords only for simplicity. Replace with hashed
 * storage (bcrypt/argon2) and a secure secret store for production use.
 *
 * @author appointment-system
 * @version 1.0
 */
public final class AdminUser {
    private final String username;
    private final String password;

    public AdminUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }
}
