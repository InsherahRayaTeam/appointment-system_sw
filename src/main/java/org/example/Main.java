package org.example;

import org.example.presentation.ConsoleLogin;
import org.example.presentation.ConsoleViewSlots;
import org.example.repository.AdminRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    private static final DateTimeFormatter SESSION_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        AdminRepository adminRepository = new InMemoryAdminRepository();
        AdminAuthService authService = new AdminAuthService(adminRepository);
        SessionManager sessionManager = new SessionManager();
        AuthEventLogger authEventLogger = new AuthEventLogger();
        ConsoleLogin login = new ConsoleLogin(authService, 3, java.time.Duration.ofSeconds(30), authEventLogger);
        AppointmentService appointmentService = new AppointmentService();
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                boolean authenticated = login.prompt(scanner);
                String username = login.getAuthenticatedUsername();

                if (!authenticated || username == null) {
                    return;
                }

                sessionManager.login(username);
                authEventLogger.logLoginSuccess(username);
                runAdminMenu(scanner, sessionManager, viewSlots, authEventLogger);
            }
        }
    }

    private static void runAdminMenu(
            Scanner scanner,
            SessionManager sessionManager,
            ConsoleViewSlots viewSlots,
            AuthEventLogger authEventLogger
    ) {
        boolean active = true;

        while (active) {
            System.out.println();
            System.out.println("Admin menu");
            System.out.println("1. View slots");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if ("1".equals(choice)) {
                if (!sessionManager.isLoggedIn()) {
                    System.out.println("Access denied. Please log in again.");
                    active = false;
                    continue;
                }

                System.out.println(
                        "Logged in as " + sessionManager.getCurrentUsername() +
                                " since " + sessionManager.getLoginTime().format(SESSION_TIME_FORMAT)
                );
                viewSlots.show();
            } else if ("2".equals(choice)) {
                String username = sessionManager.getCurrentUsername();
                sessionManager.logout();
                authEventLogger.logLogout(username);
                System.out.println("You have been logged out successfully.");
                active = false;
            } else {
                System.out.println("Invalid option. Please choose 1 or 2.");
            }
        }
    }
}
