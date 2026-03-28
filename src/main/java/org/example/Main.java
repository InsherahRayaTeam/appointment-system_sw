package org.example;

import org.example.notification.LoginNotifier;
import org.example.presentation.ConsoleLogin;
import org.example.presentation.ConsoleViewSlots;
import org.example.presentation.LoginPromptResult;
import org.example.presentation.LoginPromptStatus;
import org.example.repository.AdminRepository;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.EventManager;
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
        SessionManager sessionManager = new SessionManager();
        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        LoginNotifier loginNotifier = new LoginNotifier();
        eventManager.subscribe(loginNotifier);
        AdminAuthService authService = new AdminAuthService(adminRepository, eventManager);
        ConsoleLogin login = new ConsoleLogin(authService);
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository appointmentBookingRepository = new InMemoryAppointmentBookingRepository();
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, eventManager);
        AppointmentBookingService appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, appointmentBookingService);

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
                    System.out.println();
                    continue;
                }

                String username = loginResult.getUsername();
                sessionManager.login(username);
                authEventLogger.logLoginSuccess(username);
                System.out.println();
                runAdminMenu(scanner, sessionManager, viewSlots, authEventLogger, eventManager);
                System.out.println();
            }
        }
    }

    /**
     * Displays the admin menu with options to view available slots, book appointments, or logout.
     * This method only orchestrates user flow and delegates business operations.
     *
     * @param scanner       the input scanner for reading user choices
     * @param sessionManager the session manager for tracking login state
     * @param viewSlots     the console view for displaying slots
     * @param authEventLogger the event logger for recording authentication events
     * @param eventManager the notification dispatcher for observer updates
     */
    private static void runAdminMenu(
            Scanner scanner,
            SessionManager sessionManager,
            ConsoleViewSlots viewSlots,
            AuthEventLogger authEventLogger,
            EventManager eventManager
    ) {
        boolean active = true;

        while (active) {
            System.out.println("========================================");
            System.out.println("            Admin Menu");
            System.out.println("========================================");
            System.out.println("1. View available slots");
            System.out.println("2. Book appointment");
            System.out.println("3. Logout");
            System.out.println("----------------------------------------");
            System.out.print("Please select an option (1-3): ");

            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                if (isSessionExpired(sessionManager)) {
                    active = false;
                    continue;
                }
                System.out.println();
                viewSlots.show();
                System.out.println();
            } else if ("2".equals(choice)) {
                if (isSessionExpired(sessionManager)) {
                    active = false;
                    continue;
                }
                System.out.println();
                viewSlots.bookAppointment(scanner);
                System.out.println();
            } else if ("3".equals(choice)) {
                String username = sessionManager.getCurrentUsername();
                sessionManager.logout();
                authEventLogger.logLogout(username);
                String displayUser = username == null ? "<unknown>" : username;
                eventManager.notifyObservers("Goodbye, " + displayUser + "! You have been logged out.");
                System.out.println("\nYou have been logged out successfully.\n");
                active = false;
            } else {
                System.out.println("\n❌ Invalid option. Please enter 1, 2, or 3.\n");
            }
        }
    }

    /**
     * Checks whether the admin session is expired before handling menu actions.
     *
     * @param sessionManager the session manager tracking login state
     * @return true when session is expired, otherwise false
     */
    private static boolean isSessionExpired(SessionManager sessionManager) {
        if (!sessionManager.isLoggedIn()) {
            System.out.println("\n❌ Session expired. Please log in again.\n");
            return true;
        }
        return false;
    }
}
