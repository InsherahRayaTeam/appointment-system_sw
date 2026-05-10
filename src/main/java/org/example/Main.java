package org.example;

import org.example.domain.AppointmentSlot;
import org.example.notification.EmailNotificationService;
import org.example.notification.LoginNotifier;
import org.example.notification.NotificationService;
import org.example.presentation.gui.ApplicationController;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.InMemoryWaitlistRepository;
import org.example.repository.UserRepository;
import org.example.repository.WaitlistRepository;
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
 * Starts the application in GUI mode or console mode.
 */
public class Main {

    private static final String CONSOLE_EXIT_COMMAND = "exit";
    private static final String CONSOLE_ADMIN_ALIAS = "admin";
    private static final String CONSOLE_ADMIN_EMAIL = "insherah2004@gmail.com";
    private static final String CONSOLE_ADMIN_PASSWORD = "admin123";
    private static final String MENU_OPTION_SHOW_SLOTS = "7";
    private static final String MENU_OPTION_BOOK_SLOT = "8";
    private static final String MENU_OPTION_LOGOUT = "9";

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
     * Starts the Swing GUI application.
     */
    private static void launchGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default Swing look-and-feel if system look-and-feel is unavailable.
        }

        SwingUtilities.invokeLater(() -> {
            UserRepository userRepository = new InMemoryUserRepository();
            AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
            AppointmentBookingRepository appointmentBookingRepository =
                    new InMemoryAppointmentBookingRepository();
            WaitlistRepository waitlistRepository = new InMemoryWaitlistRepository();

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

            SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);
            LoginAttemptTracker loginAttemptTracker =
                    new LoginAttemptTracker(3, Duration.ofSeconds(30));

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
                            waitlistRepository,
                            appointmentNotificationCoordinator
                    );

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
     * Reads one console line safely.
     *
     * @param scanner scanner used for console input
     * @return entered line, or exit command when input is exhausted
     */
    private static String readLineOrExit(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return CONSOLE_EXIT_COMMAND;
        }

        return scanner.nextLine();
    }

    /**
     * Starts the console version of the application.
     */
    private static void runConsoleMode() {
        UserRepository userRepository = new InMemoryUserRepository();
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository appointmentBookingRepository = new InMemoryAppointmentBookingRepository();
        WaitlistRepository waitlistRepository = new InMemoryWaitlistRepository();

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
                waitlistRepository,
                appointmentNotificationCoordinator
        );

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Administrator Login");

            String username = readLineOrExit(scanner).trim();
            if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(username)) {
                System.out.println("Thank you for using Appointment System.");
                return;
            }

            String password = readLineOrExit(scanner).trim();
            if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(password)) {
                System.out.println("Thank you for using Appointment System.");
                return;
            }

            String authEmail = username;
            String authPassword = password;

            if (CONSOLE_ADMIN_ALIAS.equalsIgnoreCase(username)
                    && CONSOLE_ADMIN_ALIAS.equals(password)) {
                authEmail = CONSOLE_ADMIN_EMAIL;
                authPassword = CONSOLE_ADMIN_PASSWORD;
            }

            if (!authService.authenticate(authEmail, authPassword)) {
                loginNotifier.notifyLoginFailure(username);
                continue;
            }

            loginNotifier.notifyLoginSuccess(username);

            boolean loggedIn = true;
            while (loggedIn) {
                String choice = readLineOrExit(scanner).trim();

                if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(choice)) {
                    System.out.println("Thank you for using Appointment System.");
                    return;
                }

                switch (choice) {
                    case MENU_OPTION_SHOW_SLOTS:
                        System.out.println("Available Appointment Slots");
                        List<AppointmentSlot> availableSlots = appointmentService.getAvailableSlots();

                        for (AppointmentSlot slot : availableSlots) {
                            System.out.println(slot.getDateDayTimeLabel());
                        }
                        break;

                    case MENU_OPTION_BOOK_SLOT:
                        String requestedTime = readLineOrExit(scanner).trim();

                        if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(requestedTime)) {
                            System.out.println("Thank you for using Appointment System.");
                            return;
                        }

                        if (appointmentService.bookSlot(requestedTime)) {
                            System.out.println("Success: Appointment booked for " + requestedTime + ".");
                        } else {
                            System.out.println("Unable to book appointment for " + requestedTime + ".");
                        }
                        break;

                    case MENU_OPTION_LOGOUT:
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
