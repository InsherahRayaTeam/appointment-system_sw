package org.example.presentation;

import org.example.service.AdminAuthService;
import org.example.service.LoginAttemptService;
import org.example.service.LoginStatus;

import java.io.Console;
import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Simple console-based login prompt.
 */
public class ConsoleLogin {
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(30);

    private final AdminAuthService authService;
    private final LoginAttemptService attemptService;
    private String authenticatedUsername;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, new LoginAttemptService());
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts) {
        this(authService, new LoginAttemptService(maxAttempts, DEFAULT_LOCK_DURATION));
    }

    public ConsoleLogin(AdminAuthService authService, LoginAttemptService attemptService) {
        this.authService = authService;
        this.attemptService = attemptService;
    }

    /**
     * Backward-compatible prompt API.
     */
    public boolean prompt() {
        return prompt(new Scanner(System.in));
    }

    public boolean prompt(Scanner scanner) {
        authenticatedUsername = null;

        if (attemptService.isLocked()) {
            System.out.println("Too many failed attempts. Try again in " + attemptService.getRemainingLockSeconds() + " seconds.");
            return false;
        }

        Console console = System.console();
        if (console == null) {
            System.out.println("Console not available; falling back to plain input (password will be visible).");
        }

        while (attemptService.getRemainingAttempts() > 0) {
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
                attemptService.recordSuccess();
                System.out.println("Administrator login successful.");
                return true;
            }

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("Username and password cannot be blank.");
                continue;
            }

            attemptService.recordFailure();
            if (attemptService.isLocked()) {
                System.out.println("Too many failed attempts. Login locked for " + attemptService.getRemainingLockSeconds() + " seconds.");
                return false;
            }

            System.out.println("Invalid username or password.");
            System.out.println("Attempts left: " + attemptService.getRemainingAttempts());
        }

        return false;
    }

    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }
}
