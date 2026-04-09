package org.example;

import org.example.notification.LoginNotifier;
import org.example.notification.EmailNotificationService;
import org.example.notification.NotificationService;
import org.example.domain.AppointmentSlot;
import org.example.presentation.gui.ApplicationController;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.UserRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentNotificationCoordinator;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.EventManager;
import org.example.service.LoginAttemptTracker;
import org.example.service.PasswordRecoveryService;
import org.example.service.SessionManager;
import org.example.service.UserRegistrationService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

/**
 * Represents main in the system.
 */
public class Main {

    /**
     * Creates a new main object with the given values.
     *
     * @param args value for args
     */
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || System.in instanceof ByteArrayInputStream) {
            runConsoleMode();
            return;
        }

        launchGui();
    }

    /**
     * Runs launch gui for this class.
     */
    private static void launchGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default Swing look-and-feel if system look-and-feel is unavailable.
        }

        SwingUtilities.invokeLater(() -> {
            // ===== REPOSITORIES =====
            UserRepository userRepository = new InMemoryUserRepository();
            AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
            AppointmentBookingRepository appointmentBookingRepository =
                    new InMemoryAppointmentBookingRepository();

            // ===== EVENTS / OBSERVERS =====
            AuthEventLogger authEventLogger = new AuthEventLogger();
            EventManager eventManager = new EventManager();
            LoginNotifier loginNotifier = new LoginNotifier();
            eventManager.subscribe(loginNotifier);

            NotificationService notificationService = new EmailNotificationService();
            AppointmentNotificationCoordinator appointmentNotificationCoordinator =
                    new AppointmentNotificationCoordinator(notificationService);

            UserRegistrationService userRegistrationService =
                    new UserRegistrationService(userRepository, eventManager);

            PasswordRecoveryService passwordRecoveryService =
                    new PasswordRecoveryService(userRepository, notificationService, eventManager);

            // ===== SESSION / SECURITY =====
            SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);

            LoginAttemptTracker loginAttemptTracker =
                    new LoginAttemptTracker(3, Duration.ofSeconds(30));

            // ===== SERVICES =====
            AdminAuthService authService =
                    new AdminAuthService(userRepository, eventManager, loginAttemptTracker);

            AppointmentService appointmentService =
                    new AppointmentService(appointmentRepository, eventManager);

            AppointmentBookingService appointmentBookingService =
                    new AppointmentBookingService(
                            appointmentRepository,
                            appointmentBookingRepository,
                            sessionManager,
                            userRepository,
                            eventManager,
                            appointmentNotificationCoordinator
                    );

            // ===== APPLICATION CONTROLLER =====
            ApplicationController appController = new ApplicationController(
                    authService,
                    appointmentService,
                    appointmentBookingService,
                    sessionManager,
                    userRegistrationService,
                    passwordRecoveryService
            );

            appController.start();
        });
    }

    /**
     * Runs run console mode for this class.
     */
    private static void runConsoleMode() {
        UserRepository userRepository = new InMemoryUserRepository();
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository appointmentBookingRepository = new InMemoryAppointmentBookingRepository();

        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        LoginNotifier loginNotifier = new LoginNotifier();
        eventManager.subscribe(loginNotifier);

        NotificationService notificationService = new EmailNotificationService();
        AppointmentNotificationCoordinator appointmentNotificationCoordinator =
                new AppointmentNotificationCoordinator(notificationService);

        SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));

        AdminAuthService authService = new AdminAuthService(userRepository, eventManager, loginAttemptTracker);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, eventManager);
        new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager,
                appointmentNotificationCoordinator
        );

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Administrator Login");
            String username = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(username)) {
                System.out.println("Thank you for using Appointment System.");
                return;
            }

            String password = scanner.nextLine().trim();
            String authEmail = username;
            String authPassword = password;
            if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
                authEmail = "admin@gmail.com";
                authPassword = "admin123";
            }

            if (!authService.authenticate(authEmail, authPassword)) {
                loginNotifier.notifyLoginFailure(username);
                continue;
            }

            loginNotifier.notifyLoginSuccess(username);

            boolean loggedIn = true;
            while (loggedIn) {
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "7":
                        System.out.println("Available Appointment Slots");
                        List<AppointmentSlot> availableSlots = appointmentService.getAvailableSlots();
                        for (AppointmentSlot slot : availableSlots) {
                            System.out.println(slot.getDateDayTimeLabel());
                        }
                        break;
                    case "8":
                        String requestedTime = scanner.nextLine().trim();
                        if (appointmentService.bookSlot(requestedTime)) {
                            System.out.println("Success: Appointment booked for " + requestedTime + ".");
                        } else {
                            System.out.println("Unable to book appointment for " + requestedTime + ".");
                        }
                        break;
                    case "9":
                        loginNotifier.notifyLogout(username);
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 7 and 9.");
                        break;
                }
            }
        }
    }
}
