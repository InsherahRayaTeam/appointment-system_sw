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

/**
 * Main entry point for the appointment system application.
 * Orchestrates the login flow and administrative menu for managing appointment slots.
 */
public class Main {
    /**
     * Starts the application, presenting a login prompt and admin menu loop.
     *
     * @param args command-line arguments (not used)
     */
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
            System.out.println("========================================");
            System.out.println("   Appointment System Administrator");
            System.out.println("========================================\n");

            while (true) {
                LoginPromptResult loginResult = login.promptForResult(scanner);

                if (loginResult.getStatus() == LoginPromptStatus.CANCELLED) {
                    System.out.println("\n========================================");
                    System.out.println("Thank you for using Appointment System.");
                    System.out.println("========================================");
                    return;
                }

                if (loginResult.getStatus() == LoginPromptStatus.LOCKED) {
                    System.out.println("\n⚠️  Account temporarily locked due to too many failed attempts.");
                    System.out.print("Press Enter to continue or type 'exit' to quit: ");
                    String lockChoice = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(lockChoice.trim())) {
                        System.out.println("\n========================================");
                        System.out.println("Thank you for using Appointment System.");
                        System.out.println("========================================");
                        return;
                    }
                    System.out.println();
                    continue;
                }

                if (!loginResult.isSuccess()) {
                    authEventLogger.logLoginFailure(loginResult.getUsername());
                    loginNotifier.notifyLoginFailure(loginResult.getUsername());
                    System.out.println();
                    continue;
                }

                String username = loginResult.getUsername();
                sessionManager.login(username);
                authEventLogger.logLoginSuccess(username);
                loginNotifier.notifyLoginSuccess(username);
                System.out.println();
                runAdminMenu(scanner, sessionManager, viewSlots, authEventLogger, loginNotifier);
                System.out.println();
            }
        }
    }

    /**
     * Displays the admin menu with options to view available slots or logout.
     *
     * @param scanner       the input scanner for reading user choices
     * @param sessionManager the session manager for tracking login state
     * @param viewSlots     the console view for displaying slots
     * @param authEventLogger the event logger for recording authentication events
     * @param loginNotifier the notifier for user-facing login messages
     */
    private static void runAdminMenu(
            Scanner scanner,
            SessionManager sessionManager,
            ConsoleViewSlots viewSlots,
            AuthEventLogger authEventLogger,
            LoginNotifier loginNotifier
    ) {
        boolean active = true;

        while (active) {
            System.out.println("========================================");
            System.out.println("            Admin Menu");
            System.out.println("========================================");
            System.out.println("1) View available slots");
            System.out.println("2) Logout");
            System.out.println("----------------------------------------");
            System.out.print("Please select an option (1-2): ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                if (!sessionManager.isLoggedIn()) {
                    System.out.println("\n❌ Session expired. Please log in again.\n");
                    active = false;
                    continue;
                }
                System.out.println();
                viewSlots.show();
                System.out.println();
            } else if ("2".equals(choice)) {
                String username = sessionManager.getCurrentUsername();
                sessionManager.logout();
                authEventLogger.logLogout(username);
                loginNotifier.notifyLogout(username);
                System.out.println("\n✓ You have been successfully logged out.\n");
                active = false;
            } else {
                System.out.println("\n❌ Invalid option. Please enter 1 or 2.\n");
            }
        }
    }
}
