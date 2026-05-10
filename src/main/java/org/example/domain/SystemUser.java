package org.example.domain;

/**
 * Represents system user in the system.
 */
public final class SystemUser {

    private final String id;
    private final String email;
    private final String password;
    private final UserRole role;

    /**
     * Creates a new system user object with the given values.
     *
     * @param email email address used for login or matching
     * @param password password text entered by the user
     * @param role role value used for access control
     */
    public SystemUser(String email, String password, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String normalizedEmail = email.trim().toLowerCase();

        this.id = normalizedEmail + "-id";
        this.email = normalizedEmail;
        this.password = password;
        this.role = role;
    }

    /**
     * Creates a new system user object with the given values.
     *
     * @param id unique id used to find the record
     * @param email email address used for login or matching
     * @param password password text entered by the user
     * @param role role value used for access control
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
     * Returns the id.
     *
     * @return text result from this method
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the email.
     *
     * @return text result from this method
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password.
     *
     * @return text result from this method
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the role.
     *
     * @return requested value from this object
     */
    public UserRole getRole() {
        return role;
    }
}