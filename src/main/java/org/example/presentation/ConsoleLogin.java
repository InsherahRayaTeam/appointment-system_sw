package org.example.presentation;

import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.LoginAttemptTracker;
import org.example.service.LoginStatus;

import java.io.Console;
import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Simple console-based login prompt. Limits attempts to 3 by default.
 */
public class ConsoleLogin {
    private static final String CANCEL_INPUT = "q";

    private final AdminAuthService authService;
    private final int maxAttempts;
    private final LoginAttemptTracker attemptTracker;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, 3, Duration.ofSeconds(30));
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts, Duration lockDuration) {
        this.authService = authService;
        this.maxAttempts = maxAttempts;
        this.attemptTracker = new LoginAttemptTracker(maxAttempts, lockDuration);
    }

    public boolean prompt(Scanner scanner) {
        return promptForResult(scanner).isSuccess();
    }

    public LoginPromptResult promptForResult(Scanner scanner) {
        if (attemptTracker.isLocked()) {
            long remainingSeconds = attemptTracker.getRemainingLockSeconds();
            System.out.println("\n⚠️  Too many failed login attempts.");
            System.out.println("   Please try again in " + remainingSeconds + " second(s).\n");
            return new LoginPromptResult(LoginPromptStatus.LOCKED, null);
        }

        Console console = System.console();
        if (console == null) {
            System.out.println("Note: Password input will be visible. For security, use a terminal that supports System.console().\n");
        }

        while (true) {
            String user;
            String pass;

            if (console != null) {
                user = console.readLine("Administrator username: ");
                char[] pwd = console.readPassword("Administrator password: ");
                pass = pwd == null ? null : new String(pwd);
                if (pwd != null) {
                    Arrays.fill(pwd, ' ');
                }
            } else {
                System.out.print("Administrator username: ");
                user = scanner.nextLine();
                System.out.print("Administrator password: ");
                pass = scanner.nextLine();
            }

            if (isCancelInput(user)) {
                System.out.println("\n✓ Login cancelled.\n");
                return new LoginPromptResult(LoginPromptStatus.CANCELLED, null);
            }

            Credentials credentials = new Credentials(user, pass);
            LoginStatus status = authService.authenticateWithStatus(credentials);
            if (status == LoginStatus.SUCCESS) {
                String authenticatedUsername = sanitizeUsername(user);
                attemptTracker.recordSuccess();
                return new LoginPromptResult(LoginPromptStatus.SUCCESS, authenticatedUsername);
            }

            attemptTracker.recordFailure();

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("❌ Username and password are required. Please try again.\n");
            } else {
                System.out.println("❌ Invalid username or password. Please try again.\n");
            }

            if (attemptTracker.isLocked()) {
                long remainingSeconds = attemptTracker.getRemainingLockSeconds();
                System.out.println("⚠️  Account locked due to too many failed attempts.");
                System.out.println("   Please try again in " + remainingSeconds + " second(s).\n");
                return new LoginPromptResult(LoginPromptStatus.LOCKED, null);
            }

            int attemptsLeft = maxAttempts - attemptTracker.getFailedAttempts();
            System.out.println("Attempts remaining: " + Math.max(0, attemptsLeft) + "\n");
        }
    }


    private boolean isCancelInput(String username) {
        return username != null && CANCEL_INPUT.equalsIgnoreCase(username.trim());
    }

    private String sanitizeUsername(String username) {
        return username == null ? null : username.trim();
    }
}
