package org.example.service;

import org.example.domain.Credentials;
import org.example.repository.AdminRepository;

public class AdminAuthService {

    private final AdminRepository adminRepository;

    public AdminAuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Backward-compatible boolean authentication API.
     */
    public boolean authenticate(String username, String password) {
        return authenticateWithStatus(new Credentials(username, password)) == LoginStatus.SUCCESS;
    }

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
