package org.example.domain;

/**
 * Represents a system user who can log into the application.
 * The email is used as the username for authentication.
 *
 * NOTE: This demo stores plaintext passwords only for simplicity.
 * Replace with hashed storage (bcrypt/argon2) and a secure secret store for production use.
 *
 * @author appointment-system
 * @version 1.0
 */
public final class SystemUser {

    private final String id;
    private final String email;
    private final String password;
    private final UserRole role;

    /**
     * Creates a system user with generated id.
     *
     * @param email user email (used as username)
     * @param password user password
     * @param role user role
     */
    public SystemUser(String email, String password, UserRole role) {
        this(email == null ? null : email.trim().toLowerCase() + "-id", email, password, role);
    }

    /**
     * Creates a system user with explicit identity and role.
     *
     * @param id unique identifier
     * @param email user email
     * @param password user password
     * @param role user role
     */
    public SystemUser(String id, String email, String password, UserRole role) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        this.id = id.trim();
        this.email = email.trim().toLowerCase();
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
     * Returns email used for login.
     *
     * @return email
     */
    public String getEmail() {
        return email;
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