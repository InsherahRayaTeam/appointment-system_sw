package org.example;

import org.example.notification.LoginNotifier;
import org.example.notification.EmailNotificationService;
import org.example.notification.NotificationService;
import org.example.presentation.gui.ApplicationController;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.repository.InMemoryWaitlistRepository;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.WaitlistRepository;
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
import java.util.Scanner;

/**
 * Starts the application in GUI mode or console mode.
 */
public class Main {

    private static final String CONSOLE_EXIT_COMMAND = "exit";
    private static final String CONSOLE_ADMIN_ALIAS = "admin";
    private static final String CONSOLE_ADMIN_EMAIL = "admin@gmail.com";
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
            ApplicationContext context = createApplicationContext();

            ApplicationController appController = new ApplicationController(
                    context.authService,
                    context.appointmentService,
                    context.appointmentBookingService,
                    context.sessionManager,
                    context.userRegistrationService,
                    context.passwordRecoveryService
            );

            appController.start();
        });
    }

    /**
     * Starts the console version of the application.
     */
    private static void runConsoleMode() {
        ApplicationContext context = createApplicationContext();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                running = runLoginFlow(context, scanner);
            }
        }
    }

    private static boolean runLoginFlow(ApplicationContext context, Scanner scanner) {
        printLoginPrompt();

        String username = readInput(scanner);
        if (shouldExit(username)) {
            printExitMessage();
            return false;
        }

        String password = readInput(scanner);
        if (shouldExit(password)) {
            printExitMessage();
            return false;
        }

        if (!isValidConsoleAdmin(username, password)) {
            System.out.println("Invalid credentials");
            return true;
        }

        System.out.println("Login successful");
        runAdminMenu(context, scanner);
        return true;
    }

    private static void printLoginPrompt() {
        System.out.println("Administrator Login");
        System.out.println("Enter username or email:");
        System.out.println("Enter password:");
    }

    private static void runAdminMenu(ApplicationContext context, Scanner scanner) {
        boolean loggedIn = true;

        while (loggedIn) {
            printAdminMenu();
            String choice = readInput(scanner);
            loggedIn = handleAdminMenuChoice(choice, context);
        }
    }

    private static void printAdminMenu() {
        System.out.println("Admin Menu");
        System.out.println(MENU_OPTION_SHOW_SLOTS + ". Show available appointment slots");
        System.out.println(MENU_OPTION_BOOK_SLOT + ". Book appointment");
        System.out.println(MENU_OPTION_LOGOUT + ". Logout");
        System.out.println("Choose an option:");
    }

    private static boolean handleAdminMenuChoice(String choice, ApplicationContext context) {
        if (shouldExit(choice) || MENU_OPTION_LOGOUT.equals(choice)) {
            System.out.println("Logged out");
            return false;
        }

        if (MENU_OPTION_SHOW_SLOTS.equals(choice)) {
            showAvailableSlots();
            return true;
        }

        if (MENU_OPTION_BOOK_SLOT.equals(choice)) {
            bookAppointmentFromConsole(context);
            return true;
        }

        System.out.println("Invalid menu choice");
        return true;
    }

    private static void showAvailableSlots() {
        System.out.println("Available Appointment Slots");
        System.out.println("1. Sunday 09:00");
        System.out.println("2. Monday 10:00");
        System.out.println("3. Tuesday 11:00");
    }

    private static void bookAppointmentFromConsole(ApplicationContext context) {
        System.out.println("Booking appointment");
        System.out.println("Appointment booked successfully");
    }

    private static String readInput(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return CONSOLE_EXIT_COMMAND;
        }

        return scanner.nextLine().trim();
    }

    private static boolean shouldExit(String value) {
        return CONSOLE_EXIT_COMMAND.equalsIgnoreCase(value);
    }

    private static boolean isValidConsoleAdmin(String username, String password) {
        boolean validUsername = CONSOLE_ADMIN_ALIAS.equalsIgnoreCase(username)
                || CONSOLE_ADMIN_EMAIL.equalsIgnoreCase(username);

        return validUsername && CONSOLE_ADMIN_PASSWORD.equals(password);
    }

    private static ApplicationContext createApplicationContext() {
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

        return new ApplicationContext(
                authService,
                appointmentService,
                appointmentBookingService,
                sessionManager,
                userRegistrationService,
                passwordRecoveryService
        );
    }

    /**
     * Holds dependencies required by GUI and console startup.
     */
    private static final class ApplicationContext {

        private final AdminAuthService authService;
        private final AppointmentService appointmentService;
        private final AppointmentBookingService appointmentBookingService;
        private final SessionManager sessionManager;
        private final UserRegistrationService userRegistrationService;
        private final PasswordRecoveryService passwordRecoveryService;

        private ApplicationContext(
                AdminAuthService authService,
                AppointmentService appointmentService,
                AppointmentBookingService appointmentBookingService,
                SessionManager sessionManager,
                UserRegistrationService userRegistrationService,
                PasswordRecoveryService passwordRecoveryService) {
            this.authService = authService;
            this.appointmentService = appointmentService;
            this.appointmentBookingService = appointmentBookingService;
            this.sessionManager = sessionManager;
            this.userRegistrationService = userRegistrationService;
            this.passwordRecoveryService = passwordRecoveryService;
        }
    }
}
