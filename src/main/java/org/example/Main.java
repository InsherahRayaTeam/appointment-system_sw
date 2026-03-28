package org.example;

import org.example.notification.LoginNotifier;
import org.example.presentation.ConsoleInputHandler;
import org.example.presentation.ConsoleMenu;
import org.example.repository.AdminRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.EventManager;
import org.example.service.LoginAttemptTracker;
import org.example.service.SessionManager;

import java.time.Duration;
import java.util.Scanner;

/**
 * Application bootstrap class that wires dependencies and starts the console UI.
 *
 * @author appointment-system
 * @version 1.0
 */
public class Main {

    /**
     * Starts the appointment system console application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        AdminRepository adminRepository = new InMemoryAdminRepository();
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        LoginNotifier loginNotifier = new LoginNotifier();
        eventManager.subscribe(loginNotifier);

        AdminAuthService authService = new AdminAuthService(adminRepository, eventManager, loginAttemptTracker);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, eventManager);

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleInputHandler inputHandler = new ConsoleInputHandler(scanner);
            ConsoleMenu menu = new ConsoleMenu(inputHandler, authService, appointmentService, sessionManager);
            menu.run();
        }
    }
}
