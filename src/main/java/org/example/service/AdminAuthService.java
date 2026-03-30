package org.example.service;

import org.example.domain.Credentials;
import org.example.domain.UserRole;
import org.example.domain.AdminUser;
import org.example.repository.AdminRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Authenticates system users and applies login-attempt policy.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final EventManager eventManager;
    private final LoginAttemptTracker loginAttemptTracker;

    /**
     * Creates an authentication service with repository, notifier, and lockout policy dependencies.
     *
     * @param adminRepository repository used to resolve admin credentials
     * @param eventManager event dispatcher used for auth notifications
     * @param loginAttemptTracker lockout/attempt policy tracker
     */
    public AdminAuthService(
            AdminRepository adminRepository,
            EventManager eventManager,
            LoginAttemptTracker loginAttemptTracker
    ) {
        this.adminRepository = Objects.requireNonNull(adminRepository, "adminRepository cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.loginAttemptTracker = Objects.requireNonNull(
                loginAttemptTracker,
                "loginAttemptTracker cannot be null"
        );
    }

    /**
     * Backward-compatible boolean authentication API.
     *
     * @param username raw username input
     * @param password raw password input
     * @return true when credentials are valid, otherwise false
     */
    public boolean authenticate(String username, String password) {
        return authenticateWithStatus(new Credentials(username, password)) == LoginStatus.SUCCESS;
    }

    /**
     * Authenticates credentials and returns a status result.
     *
     * @param credentials credential payload
     * @return authentication status for the provided credentials
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


        Optional<AdminUser> authenticatedUser = resolveAuthenticatedUser(credentials);
        boolean authenticated = authenticatedUser.isPresent();

        if (authenticated) {
            if (authenticatedUser.get().getRole() == UserRole.ADMIN) {
                eventManager.notifyObservers("Admin logged in successfully");
            } else {
                eventManager.notifyObservers("User logged in successfully");
            }
            return LoginStatus.SUCCESS;
        } else {
            eventManager.notifyObservers("Failed login attempt");
            return LoginStatus.INVALID_CREDENTIALS;
        }
    }

    /**
     * Convenience overload for raw username/password input.
     *
     * @param username raw username input
     * @param password raw password input
     * @return authentication status
     */
    public LoginStatus authenticateWithStatus(String username, String password) {
        return authenticateWithStatus(new Credentials(username, password));
    }

    /**
     * Authenticates and applies lockout policy in one operation.
     *
     * @param credentials credential payload
     * @return policy-aware authentication result
     */
    public AuthenticationAttemptResult authenticateWithPolicy(Credentials credentials) {
        if (loginAttemptTracker.isLocked()) {
            return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
        }

        Optional<AdminUser> authenticatedUser = resolveAuthenticatedUser(credentials);
        if (authenticatedUser.isPresent()) {
            if (authenticatedUser.get().getRole() == UserRole.ADMIN) {
                eventManager.notifyObservers("Admin logged in successfully");
            } else {
                eventManager.notifyObservers("User logged in successfully");
            }
            loginAttemptTracker.recordSuccess();
            return AuthenticationAttemptResult.success(authenticatedUser.get());
        }

        LoginStatus status = credentials == null
                || isBlank(credentials.getUsername())
                || isBlank(credentials.getPassword())
                ? LoginStatus.BLANK_INPUT
                : LoginStatus.INVALID_CREDENTIALS;

        eventManager.notifyObservers("Failed login attempt");

        loginAttemptTracker.recordFailure();
        if (loginAttemptTracker.isLocked()) {
            return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
        }

        return AuthenticationAttemptResult.failure(status, loginAttemptTracker.getAttemptsRemaining());
    }

    /**
     * Indicates whether authentication is currently locked.
     *
     * @return true when lockout is active, otherwise false
     */
    public boolean isLocked() {
        return loginAttemptTracker.isLocked();
    }

    /**
     * Returns remaining lockout duration.
     *
     * @return remaining lock duration in seconds
     */
    public long getRemainingLockSeconds() {
        return loginAttemptTracker.getRemainingLockSeconds();
    }


    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Optional<AdminUser> resolveAuthenticatedUser(Credentials credentials) {
        if (credentials == null || isBlank(credentials.getUsername()) || isBlank(credentials.getPassword())) {
            return Optional.empty();
        }

        String normalizedUsername = credentials.getUsername().trim();
        String password = credentials.getPassword();

        return adminRepository.findByUsername(normalizedUsername)
                .filter(user -> user.getPassword().equals(password));
    }
}