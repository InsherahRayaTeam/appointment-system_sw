package org.example.service;

import org.example.domain.Credentials;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Authenticates system users and applies login-attempt policy.
 *
 * Supports both ADMIN and USER accounts using email-based login.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminAuthService {

    private final UserRepository userRepository;
    private final EventManager eventManager;
    private final LoginAttemptTracker loginAttemptTracker;

    /**
     * Creates an authentication service with repository, event manager,
     * and login-attempt tracking dependencies.
     *
     * @param userRepository repository used to resolve user credentials
     * @param eventManager event dispatcher used for login notifications
     * @param loginAttemptTracker tracker used for lockout policy
     */
    public AdminAuthService(
            UserRepository userRepository,
            EventManager eventManager,
            LoginAttemptTracker loginAttemptTracker
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository cannot be null"
        );
        this.eventManager = Objects.requireNonNull(
                eventManager,
                "eventManager cannot be null"
        );
        this.loginAttemptTracker = Objects.requireNonNull(
                loginAttemptTracker,
                "loginAttemptTracker cannot be null"
        );
    }

    /**
     * Backward-compatible boolean authentication API.
     *
     * @param email raw email input
     * @param password raw password input
     * @return true when credentials are valid, otherwise false
     */
    public boolean authenticate(String email, String password) {
        return authenticateWithStatus(new Credentials(email, password)) == LoginStatus.SUCCESS;
    }

    /**
     * Authenticates credentials and returns login status.
     *
     * @param credentials credential payload
     * @return authentication status for the provided credentials
     */
    public LoginStatus authenticateWithStatus(Credentials credentials) {
        if (credentials == null) {
            return LoginStatus.BLANK_INPUT;
        }

        String email = credentials.getEmail();
        String password = credentials.getPassword();

        if (isBlank(email) || isBlank(password)) {
            return LoginStatus.BLANK_INPUT;
        }

        Optional<SystemUser> user = resolveAuthenticatedUser(credentials);

        if (user.isPresent()) {
            notifySuccessfulLogin(user.get());
            return LoginStatus.SUCCESS;
        }

        eventManager.notifyObservers("Failed login attempt");
        return LoginStatus.INVALID_CREDENTIALS;
    }

    /**
     * Convenience overload for raw email/password input.
     *
     * @param email raw email input
     * @param password raw password input
     * @return authentication status
     */
    public LoginStatus authenticateWithStatus(String email, String password) {
        return authenticateWithStatus(new Credentials(email, password));
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

        Optional<SystemUser> user = resolveAuthenticatedUser(credentials);

        if (user.isPresent()) {
            notifySuccessfulLogin(user.get());
            loginAttemptTracker.recordSuccess();
            return AuthenticationAttemptResult.success(user.get());
        }

        LoginStatus status = credentials == null
                || isBlank(credentials.getEmail())
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
     * Returns remaining lockout duration in seconds.
     *
     * @return remaining lock duration in seconds
     */
    public long getRemainingLockSeconds() {
        return loginAttemptTracker.getRemainingLockSeconds();
    }

    private void notifySuccessfulLogin(SystemUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            eventManager.notifyObservers("Admin logged in successfully");
        } else {
            eventManager.notifyObservers("User logged in successfully");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Optional<SystemUser> resolveAuthenticatedUser(Credentials credentials) {
        if (credentials == null
                || isBlank(credentials.getEmail())
                || isBlank(credentials.getPassword())) {
            return Optional.empty();
        }

        String email = credentials.getEmail().trim().toLowerCase();
        String password = credentials.getPassword();

        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }
}