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
    private String authenticatedUsername;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, 3, Duration.ofSeconds(30));
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts) {
        this(authService, maxAttempts, Duration.ofSeconds(30));
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
        authenticatedUsername = null;

        if (attemptTracker.isLocked()) {
            System.out.println("Login is temporarily locked. Try again in "
                    + attemptTracker.getRemainingLockSeconds() + " seconds.");

        }

        Console console = System.console();
        if (console == null) {
            System.out.println("Console not available; falling back to plain input (password will be visible).");
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
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

            if (isCancelInput(user)) {
                System.out.println("Login cancelled.");
                return new LoginPromptResult(LoginPromptStatus.CANCELLED, null);
            }

            Credentials credentials = new Credentials(user, pass);
            LoginStatus status = authService.authenticateWithStatus(credentials);
            if (status == LoginStatus.SUCCESS) {
                authenticatedUsername = user.trim();
                attemptTracker.recordSuccess();
                System.out.println("Administrator login successful.");
                return new LoginPromptResult(LoginPromptStatus.SUCCESS, authenticatedUsername);
            }

            attemptTracker.recordFailure();

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("Username and password cannot be blank.");
            } else {
                System.out.println("Invalid username or password.");
            }

            if (attemptTracker.isLocked()) {
                System.out.println("Too many failed attempts. Login locked for "
                        + attemptTracker.getRemainingLockSeconds() + " seconds.");

            }

            System.out.println("Attempts left: " + (maxAttempts - attempt));
        }

        System.out.println("Maximum login attempts exceeded.");
        return new LoginPromptResult(LoginPromptStatus.FAILED, null);
    }


    private boolean isCancelInput(String username) {
        return username != null && CANCEL_INPUT.equalsIgnoreCase(username.trim());
    }

    private String sanitizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }
}
