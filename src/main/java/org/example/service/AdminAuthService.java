package org.example.service;

import org.example.domain.Credentials;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents admin auth service in the system.
 */
public class AdminAuthService {

    private final UserRepository userRepository;
    private final EventManager eventManager;
    private final LoginAttemptTracker loginAttemptTracker;

    /**
     * Creates a new admin auth service object with the given values.
     *
     * @param userRepository user involved in this action
     * @param eventManager manager object used for shared app state
     * @param loginAttemptTracker value for login attempt tracker
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
     * Runs authenticate for this class.
     *
     * @param email email address used for login or matching
     * @param password password text entered by the user
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean authenticate(String email, String password) {
        return authenticateWithStatus(new Credentials(email, password)) == LoginStatus.SUCCESS;
    }

    /**
     * Runs authenticate with status for this class.
     *
     * @param credentials value for credentials
     *
     * @return status that explains the operation result
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
     * Runs authenticate with status for this class.
     *
     * @param email email address used for login or matching
     * @param password password text entered by the user
     *
     * @return status that explains the operation result
     */
    public LoginStatus authenticateWithStatus(String email, String password) {
        return authenticateWithStatus(new Credentials(email, password));
    }

    /**
     * Runs authenticate with policy for this class.
     *
     * @param credentials value for credentials
     *
     * @return result produced by this method
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
     * Checks whether locked is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isLocked() {
        return loginAttemptTracker.isLocked();
    }

    /**
     * Returns the remaining lock seconds.
     *
     * @return numeric result from this method
     */
    public long getRemainingLockSeconds() {
        return loginAttemptTracker.getRemainingLockSeconds();
    }

    /**
     * Sends successful login to listeners.
     *
     * @param user user involved in this action
     */
    private void notifySuccessfulLogin(SystemUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            eventManager.notifyObservers("Admin logged in successfully");
        } else {
            eventManager.notifyObservers("User logged in successfully");
        }
    }

    /**
     * Checks whether blank is true.
     *
     * @param value value used by this method
     *
     * @return true when the action is valid or successful, otherwise false
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Runs resolve authenticated user for this class.
     *
     * @param credentials value for credentials
     *
     * @return optional value if data is found
     */
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
