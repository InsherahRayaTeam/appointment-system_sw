package org.example.domain;

import java.util.Objects;

/**
 * Represents credentials in the system.
 */
public class Credentials {

    private final String email;
    private final String password;

    /**
     * Creates a new credentials object with the given values.
     *
     * @param email email address used for login or matching
     * @param password password text entered by the user
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
     * Runs matches for this class.
     *
     * @param email email address used for login or matching
     * @param password password text entered by the user
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean matches(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        return this.email.equals(email.trim().toLowerCase()) && this.password.equals(password);
    }

    /**
     * Runs equals for this class.
     *
     * @param o value for o
     *
     * @return true when the action is valid or successful, otherwise false
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
     * Checks whether it has h code.
     *
     * @return numeric result from this method
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    /**
     * Runs to string for this class.
     *
     * @return text result from this method
     */
    @Override
    public String toString() {
        return "Credentials{" +
                "email='" + email + '\'' +
                '}';
    }
}
