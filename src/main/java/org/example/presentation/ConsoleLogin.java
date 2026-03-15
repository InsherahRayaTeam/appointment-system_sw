package org.example.presentation;

import org.example.service.AdminAuthService;
import org.example.service.LoginStatus;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Simple console-based login prompt. Limits attempts to 3 by default.
 */
public class ConsoleLogin {
    private final AdminAuthService authService;
    private final int maxAttempts;
    private String authenticatedUsername;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, 3);
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts) {
        this.authService = authService;
        this.maxAttempts = maxAttempts;
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

            LoginStatus status = authService.authenticateWithStatus(user, pass);
            if (status == LoginStatus.SUCCESS) {
                authenticatedUsername = user.trim();
                System.out.println("Administrator login successful.");
                return true;
            }

            if (status == LoginStatus.BLANK_INPUT) {
                System.out.println("Username and password cannot be blank.");
            } else {
                System.out.println("Invalid username or password.");
            }

            System.out.println("Attempts left: " + (maxAttempts - attempt));
        }

        System.out.println("Maximum login attempts exceeded.");
        return false;
    }

    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }
}
