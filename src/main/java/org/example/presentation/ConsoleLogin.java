package org.example.presentation;

import org.example.service.AdminAuthService;
import org.example.service.AuthEventLogger;
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
    private final AdminAuthService authService;
    private final LoginAttemptTracker attemptTracker;
    private final AuthEventLogger authEventLogger;
    private String authenticatedUsername;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, 3);
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts) {
        this(authService, maxAttempts, Duration.ofSeconds(30), new AuthEventLogger());
    }

    public ConsoleLogin(
            AdminAuthService authService,
            int maxAttempts,
            Duration lockDuration,
            AuthEventLogger authEventLogger
    ) {
        this.authService = authService;
        this.attemptTracker = new LoginAttemptTracker(maxAttempts, lockDuration);
        this.authEventLogger = authEventLogger;
    }

    /**
     * Backward-compatible prompt API.
     */
    public boolean prompt() {
        return prompt(new Scanner(System.in));
    }

    public boolean prompt(Scanner scanner) {
        authenticatedUsername = null;

        Console console = System.console();
        if (console == null) {
            System.out.println("Console not available; falling back to plain input (password will be visible).");
        }

        while (true) {
            if (attemptTracker.isLocked()) {
                long remaining = attemptTracker.getRemainingLockSeconds();
                System.out.println("Too many failed attempts. Please wait " + remaining + " second(s) before retrying.");
                authEventLogger.logLoginLocked(remaining);
                return false;
            }

            String user;
            String pass;

            if (console != null) {
                user = console.readLine("Administrator username: ");
                char[] pwd = console.readPassword("Password: ");
                pass = pwd == null ? null : new String(pwd);
                if (pwd != null) {
                    Arrays.fill(pwd, ' ');
                }
            } else {
                System.out.print("Administrator username: ");
                user = scanner.nextLine();
                System.out.print("Password: ");
                pass = scanner.nextLine();
            }

            LoginStatus status = authService.authenticateWithStatus(user, pass);
            if (status == LoginStatus.SUCCESS) {
                authenticatedUsername = user.trim();
                attemptTracker.recordSuccess();
                System.out.println("Administrator login successful.");
                return true;
            }

            attemptTracker.recordFailure();
            authEventLogger.logLoginFailure(user);

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("Username and password cannot be blank.");
            } else {
                System.out.println("Invalid username or password.");
            }

            int attemptsLeft = attemptTracker.getRemainingAttempts();
            if (attemptsLeft > 0) {
                System.out.println("Attempts left: " + attemptsLeft);
            }
        }
    }

    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }
}
