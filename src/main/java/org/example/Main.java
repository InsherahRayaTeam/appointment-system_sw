package org.example;

import org.example.notification.LoginNotifier;
import org.example.presentation.ConsoleLogin;
import org.example.presentation.ConsoleViewSlots;
import org.example.presentation.LoginPromptResult;
import org.example.presentation.LoginPromptStatus;
import org.example.repository.AdminRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.SessionManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AdminRepository adminRepository = new InMemoryAdminRepository();
        AdminAuthService authService = new AdminAuthService(adminRepository);
        SessionManager sessionManager = new SessionManager();
        ConsoleLogin login = new ConsoleLogin(authService);
        AuthEventLogger authEventLogger = new AuthEventLogger();
        LoginNotifier loginNotifier = new LoginNotifier();
        AppointmentService appointmentService = new AppointmentService();
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                LoginPromptResult loginResult = login.promptForResult(scanner);

                if (loginResult.getStatus() == LoginPromptStatus.CANCELLED) {
                    System.out.println("Exiting application.");
                    return;
                }

                if (loginResult.getStatus() == LoginPromptStatus.LOCKED) {
                    System.out.print("Login is locked. Press Enter to retry later or type q to quit: ");
                    String lockChoice = scanner.nextLine();
                    if ("q".equalsIgnoreCase(lockChoice.trim())) {
                        System.out.println("Exiting application.");
                        return;
                    }
                    continue;
                }

                if (!loginResult.isSuccess()) {
                    authEventLogger.logLoginFailure(loginResult.getUsername());
                    loginNotifier.notifyLoginFailure(loginResult.getUsername());
                    continue;
                }

                String username = loginResult.getUsername();
                sessionManager.login(username);
                authEventLogger.logLoginSuccess(username);
                loginNotifier.notifyLoginSuccess(username);
                runAdminMenu(scanner, sessionManager, viewSlots, authEventLogger, loginNotifier);
            }
        }
    }

    private static void runAdminMenu(
            Scanner scanner,
            SessionManager sessionManager,
            ConsoleViewSlots viewSlots,
            AuthEventLogger authEventLogger,
            LoginNotifier loginNotifier
    ) {
        boolean active = true;

        while (active) {
            System.out.println();
            System.out.println("Admin menu");
            System.out.println("1. View slots");
            System.out.println("2. Book slot");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if ("1".equals(choice)) {
                if (!sessionManager.isLoggedIn()) {
                    System.out.println("Access denied. Please log in again.");
                    active = false;
                    continue;
                }
                viewSlots.show();
            } else if ("2".equals(choice)) {
                if (!sessionManager.isLoggedIn()) {
                    System.out.println("Access denied. Please log in again.");
                    active = false;
                    continue;
                }
                viewSlots.bookSlot(scanner);
            } else if ("3".equals(choice)) {
                String username = sessionManager.getCurrentUsername();
                sessionManager.logout();
                authEventLogger.logLogout(username);
                loginNotifier.notifyLogout(username);
                System.out.println("You have been logged out successfully.");
                active = false;
            } else {
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }
}
