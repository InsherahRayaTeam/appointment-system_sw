package org.example.presentation;

import org.example.service.AdminAuthService;

import java.io.Console;
import java.util.Scanner;

/**
 * Simple console-based login prompt. Limits attempts to 3 by default.
 */
public class ConsoleLogin {
    private final AdminAuthService authService;
    private final int maxAttempts;

    public ConsoleLogin(AdminAuthService authService) {
        this(authService, 3);
    }

    public ConsoleLogin(AdminAuthService authService, int maxAttempts) {
        this.authService = authService;
        this.maxAttempts = maxAttempts;
    }

    public boolean prompt() {
        Console console = System.console();
        Scanner scanner = null;
        if (console == null) {
            // Running inside IDE or without a console; use Scanner as fallback (password not masked)
            scanner = new Scanner(System.in);
            System.out.println("Console not available; falling back to plain input (password will be visible).");
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String user;
            String pass;
            if (console != null) {
                user = console.readLine("Administrator username: ");
                char[] pwd = console.readPassword("Password: ");
                pass = pwd == null ? null : new String(pwd);
            } else {
                System.out.print("Administrator username: ");
                user = scanner.nextLine();
                System.out.print("Password: ");
                pass = scanner.nextLine();
            }

            if (authService.authenticate(user, pass)) {
                System.out.println("Login successful.");
                return true;
            } else {
                System.out.println("Invalid credentials. Attempts left: " + (maxAttempts - attempt));
            }
        }

        System.out.println("Maximum login attempts exceeded. Exiting.");
        return false;
    }
}
