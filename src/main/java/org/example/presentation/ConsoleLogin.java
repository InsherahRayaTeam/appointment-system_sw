package org.example.presentation;

import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;

import java.io.Console;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * Console-based login prompt that handles user input/output for authentication attempts.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ConsoleLogin {
    private static final String CANCEL_INPUT = "q";

    private final AdminAuthService authService;

    /**
     * Creates a login prompt bound to an authentication service.
     *
     * @param authService authentication service
     */
    public ConsoleLogin(AdminAuthService authService) {
        this.authService = Objects.requireNonNull(authService, "authService cannot be null");
    }

    /**
     * Prompts for credentials and returns whether login succeeded.
     *
     * @param scanner scanner used for console input
     * @return true when login succeeds, otherwise false
     */
    public boolean prompt(Scanner scanner) {
        return promptForResult(scanner).isSuccess();
    }

    /**
     * Prompts for credentials and returns the full prompt outcome.
     *
     * @param scanner scanner used for console input
     * @return login prompt result containing outcome status and username when applicable
     */
    public LoginPromptResult promptForResult(Scanner scanner) {
        if (authService.isLocked()) {
            long remainingSeconds = authService.getRemainingLockSeconds();
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

            AuthenticationAttemptResult result = authService.authenticateWithPolicy(new Credentials(user, pass));
            if (result.isSuccess()) {
                String authenticatedUsername = sanitizeUsername(user);
                return new LoginPromptResult(LoginPromptStatus.SUCCESS, authenticatedUsername);
            }

            LoginStatus status = result.getStatus();

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("❌ Username and password are required. Please try again.\n");
            } else {
                System.out.println("❌ Invalid username or password. Please try again.\n");
            }

            if (result.isLocked()) {
                long remainingSeconds = result.getRemainingLockSeconds();
                System.out.println("⚠️  Account locked due to too many failed attempts.");
                System.out.println("   Please try again in " + remainingSeconds + " second(s).\n");
                return new LoginPromptResult(LoginPromptStatus.LOCKED, null);
            }

            System.out.println("Attempts remaining: " + result.getAttemptsRemaining() + "\n");
        }
    }


    private boolean isCancelInput(String username) {
        return username != null && CANCEL_INPUT.equalsIgnoreCase(username.trim());
    }

    private String sanitizeUsername(String username) {
        return username == null ? null : username.trim();
    }
}
