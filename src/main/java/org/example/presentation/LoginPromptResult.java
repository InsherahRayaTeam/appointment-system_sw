package org.example.presentation;

/**
 * Represents the result of a login prompt interaction.
 * Contains the terminal status and username (if applicable) of the login attempt.
 *
 * @author appointment-system
 * @version 1.0
 */
public class LoginPromptResult {

    private final LoginPromptStatus status;
    private final String username;

    /**
     * Creates a login prompt result.
     *
     * @param status   the terminal status of the login attempt
     * @param username the authenticated username (may be null for failed/cancelled attempts)
     */
    public LoginPromptResult(LoginPromptStatus status, String username) {
        this.status = status;
        this.username = username;
    }

    /**
     * Returns the terminal status of this login attempt.
     *
     * @return the login prompt status
     */
    public LoginPromptStatus getStatus() {
        return status;
    }

    /**
     * Returns the authenticated username, if the login was successful.
     *
     * @return the username, or null if login did not succeed
     */
    public String getUsername() {
        return username;
    }

    /**
     * Checks if this login attempt was successful.
     *
     * @return true if status is SUCCESS, false otherwise
     */
    public boolean isSuccess() {
        return status == LoginPromptStatus.SUCCESS;
    }
}
