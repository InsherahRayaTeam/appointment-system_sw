package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Handles user registration business rules.
 */
public class UserRegistrationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final EventManager eventManager;

    /**
     * Creates a new user registration service object with the given values.
     *
     * @param userRepository repository used to read and save data
     * @param eventManager manager object used for shared app state
     */
    public UserRegistrationService(UserRepository userRepository, EventManager eventManager) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
    }

    /**
     * Registers a user when validation passes.
     *
     * @param username unique username for login identity
     * @param email email address used for login or matching
     * @param password password text entered by the user
     * @param confirmPassword password confirmation text entered by the user
     * @return status that explains the operation result
     */
    public SignUpStatus registerUser(String username, String email, String password, String confirmPassword) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalizeEmail(email);

        if (normalizedUsername == null) {
            return SignUpStatus.BLANK_USERNAME;
        }
        if (normalizedEmail == null) {
            return SignUpStatus.BLANK_EMAIL;
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return SignUpStatus.INVALID_EMAIL;
        }
        if (password == null || password.trim().isEmpty()) {
            return SignUpStatus.BLANK_PASSWORD;
        }
        if (!password.equals(confirmPassword)) {
            return SignUpStatus.PASSWORD_MISMATCH;
        }
        if (!isStrongPassword(password)) {
            return SignUpStatus.WEAK_PASSWORD;
        }
        if (userRepository.findById(normalizedUsername).isPresent()) {
            return SignUpStatus.USERNAME_ALREADY_EXISTS;
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return SignUpStatus.EMAIL_ALREADY_EXISTS;
        }

        SystemUser user = new SystemUser(normalizedUsername, normalizedEmail, password, UserRole.USER);
        userRepository.save(user);
        eventManager.notifyObservers("User registered successfully: " + normalizedEmail);
        return SignUpStatus.SUCCESS;
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            }
            if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
    }
}

