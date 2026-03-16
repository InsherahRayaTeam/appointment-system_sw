package org.example.service;

import org.example.domain.Credentials;
import org.example.repository.AdminRepository;

public class AdminAuthService {

    private final AdminRepository adminRepository;

    public AdminAuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Convenience boolean wrapper around {@link #authenticateWithStatus(Credentials)}.
     * Returns true only when credentials are valid; all other cases (blank input,
     * unknown user, wrong password) return false.
     */
    public boolean authenticate(String username, String password) {
        return authenticateWithStatus(new Credentials(username, password)) == LoginStatus.SUCCESS;
    }

    /**
     * Authenticates an administrator using the provided credentials.
     *
     * @param credentials the login credentials containing username and password
     * @return LoginStatus.SUCCESS if credentials are valid,
     *         LoginStatus.BLANK_INPUT if username or password is blank,
     *         LoginStatus.INVALID_CREDENTIALS if authentication fails
     */
    public LoginStatus authenticateWithStatus(Credentials credentials) {
        if (credentials == null) {
            return LoginStatus.BLANK_INPUT;
        }

        String username = credentials.getUsername();
        String password = credentials.getPassword();

        if (isBlank(username) || isBlank(password)) {
            return LoginStatus.BLANK_INPUT;
        }

        String normalizedUsername = username.trim();

        boolean authenticated = adminRepository.findByUsername(normalizedUsername)
                .map(admin -> admin.getPassword().equals(password))
                .orElse(false);

        return authenticated ? LoginStatus.SUCCESS : LoginStatus.INVALID_CREDENTIALS;
    }

    public LoginStatus authenticateWithStatus(String username, String password) {
        return authenticateWithStatus(new Credentials(username, password));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
