package org.example.domain;

/**
 * Immutable credential payload used during authentication.
 *
 * @author appointment-system
 * @version 1.0
 */
public class Credentials {

    private final String username;
    private final String password;

    /**
     * Creates a credential payload.
     *
     * @param username username value
     * @param password password value
     */
    public Credentials(String username, String password) {
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