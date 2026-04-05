package org.example.domain;

import java.util.Objects;

/**
 * Represents user login credentials using email and password.
 *
 * The email is used as the unique identifier (username).
 *
 * @author appointment-system
 * @version 1.0
 */
public class Credentials {

    private final String email;
    private final String password;

    /**
     * Constructs credentials with email and password.
     *
     * @param email the user's email (used as username)
     * @param password the user's password
     */
    public Credentials(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    /**
     * Returns the email (username).
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Checks if the provided credentials match this object.
     *
     * @param email input email
     * @param password input password
     * @return true if both match
     */
    public boolean matches(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        return this.email.equals(email.trim().toLowerCase()) && this.password.equals(password);
    }

    /**
     * Equality based on email (unique identifier).
     *
     * @param o other object
     * @return true if both credentials use the same email
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credentials)) {
            return false;
        }
        Credentials that = (Credentials) o;
        return email.equals(that.email);
    }

    /**
     * Returns hash code based on email.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    /**
     * Returns safe string representation without password.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Credentials{" +
                "email='" + email + '\'' +
                '}';
    }
}