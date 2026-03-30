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
    private final String id;
    private final String username;
    private final String password;
    private final UserRole role;

    public AdminUser(String username, String password) {
        this(username == null ? null : username + "-id", username, password, UserRole.ADMIN);
    }

    /**
     * Creates a system user with explicit identity and role.
     *
     * @param id unique identifier
     * @param username username
     * @param password password
     * @param role user role
     */
    public AdminUser(String id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Returns unique identifier.
     *
     * @return user identifier
     */
    public String getId() {
        return id;
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

    /**
     * Returns the user's authorization role.
     *
     * @return user role
     */
    public UserRole getRole() {
        return role;
    }
}
