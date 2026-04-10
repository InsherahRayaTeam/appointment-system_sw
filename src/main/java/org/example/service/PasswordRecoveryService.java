package org.example.service;

import org.example.domain.SystemUser;
import org.example.notification.NotificationService;
import org.example.repository.UserRepository;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Handles forgot password and password reset business rules.
 */
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EventManager eventManager;
    private final Supplier<String> resetCodeGenerator;
    private final Map<String, String> resetCodesByEmail = new HashMap<>();

    /**
     * Creates a new password recovery service object with the given values.
     *
     * @param userRepository repository used to read and save data
     * @param notificationService service used to send notifications
     * @param eventManager manager object used for shared app state
     */
    public PasswordRecoveryService(
            UserRepository userRepository,
            NotificationService notificationService,
            EventManager eventManager
    ) {
        this(userRepository, notificationService, eventManager, PasswordRecoveryService::generateCode);
    }

    /**
     * Creates a new password recovery service object with the given values.
     *
     * @param userRepository repository used to read and save data
     * @param notificationService service used to send notifications
     * @param eventManager manager object used for shared app state
     * @param resetCodeGenerator generator used to create reset codes
     */
    public PasswordRecoveryService(
            UserRepository userRepository,
            NotificationService notificationService,
            EventManager eventManager,
            Supplier<String> resetCodeGenerator
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.resetCodeGenerator = Objects.requireNonNull(resetCodeGenerator, "resetCodeGenerator cannot be null");
    }

    /**
     * Sends a reset code when user exists.
     *
     * @param usernameOrEmail username or email used for identity
     * @return status that explains the operation result
     */
    public ForgotPasswordStatus requestReset(String usernameOrEmail) {
        SystemUser user = resolveUser(usernameOrEmail);
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return ForgotPasswordStatus.BLANK_IDENTIFIER;
        }
        if (user == null) {
            return ForgotPasswordStatus.UNKNOWN_USER;
        }

        String resetCode = resetCodeGenerator.get();
        resetCodesByEmail.put(user.getEmail(), resetCode);

        notificationService.send(
                user.getEmail(),
                "Password Reset Request",
                "Use this reset code to update your password: " + resetCode
                        + " . If you did not request this, ignore this email."
        );
        eventManager.notifyObservers("Password reset requested: " + user.getEmail());
        return ForgotPasswordStatus.RESET_REQUESTED;
    }

    /**
     * Resets a password after validating reset code and new password rules.
     *
     * @param usernameOrEmail username or email used for identity
     * @param resetCode reset verification code
     * @param newPassword password text entered by the user
     * @param confirmPassword password confirmation text entered by the user
     * @return status that explains the operation result
     */
    public ForgotPasswordStatus resetPassword(
            String usernameOrEmail,
            String resetCode,
            String newPassword,
            String confirmPassword
    ) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return ForgotPasswordStatus.BLANK_IDENTIFIER;
        }

        SystemUser user = resolveUser(usernameOrEmail);
        if (user == null) {
            return ForgotPasswordStatus.UNKNOWN_USER;
        }

        String storedCode = resetCodesByEmail.get(user.getEmail());
        if (storedCode == null || resetCode == null || !storedCode.equals(resetCode.trim())) {
            return ForgotPasswordStatus.INVALID_RESET_CODE;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ForgotPasswordStatus.BLANK_NEW_PASSWORD;
        }

        if (!newPassword.equals(confirmPassword)) {
            return ForgotPasswordStatus.PASSWORD_MISMATCH;
        }

        if (!isStrongPassword(newPassword)) {
            return ForgotPasswordStatus.WEAK_PASSWORD;
        }

        ForgotPasswordStatus updateStatus = resetPassword(user.getEmail(), newPassword);
        if (updateStatus != ForgotPasswordStatus.PASSWORD_RESET_SUCCESS) {
            return ForgotPasswordStatus.UPDATE_FAILED;
        }

        resetCodesByEmail.remove(user.getEmail());
        return ForgotPasswordStatus.PASSWORD_RESET_SUCCESS;
    }

    /**
     * Resets a password directly by user email.
     *
     * @param email email address used for login or matching
     * @param newPassword password text entered by the user
     * @return status that explains the operation result
     */
    public ForgotPasswordStatus resetPassword(String email, String newPassword) {
        if (email == null || email.trim().isEmpty()) {
            return ForgotPasswordStatus.BLANK_IDENTIFIER;
        }

        SystemUser user = resolveUser(email);
        if (user == null) {
            return ForgotPasswordStatus.UNKNOWN_USER;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ForgotPasswordStatus.BLANK_NEW_PASSWORD;
        }

        if (!isStrongPassword(newPassword)) {
            return ForgotPasswordStatus.WEAK_PASSWORD;
        }

        SystemUser updatedUser = new SystemUser(
                user.getId(),
                user.getEmail(),
                newPassword.trim(),
                user.getRole()
        );

        boolean updated = userRepository.update(updatedUser);
        if (!updated) {
            return ForgotPasswordStatus.UPDATE_FAILED;
        }

        resetCodesByEmail.remove(user.getEmail());
        eventManager.notifyObservers("Password reset completed: " + user.getEmail());
        return ForgotPasswordStatus.PASSWORD_RESET_SUCCESS;
    }

    private SystemUser resolveUser(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return null;
        }

        String normalized = usernameOrEmail.trim();
        Optional<SystemUser> byEmail = userRepository.findByEmail(normalized);
        return byEmail.orElseGet(() -> userRepository.findById(normalized).orElse(null));
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

    private static String generateCode() {
        SecureRandom random = new SecureRandom();
        int value = 100000 + random.nextInt(900000);
        return Integer.toString(value);
    }
}

