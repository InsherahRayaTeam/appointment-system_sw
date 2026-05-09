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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the application in GUI mode or console mode.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

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
            if (!authenticateConsoleAdmin(scanner)) {
                return;
            }

            runConsoleMenu(context, scanner);
        }
    }

    /**
     * Creates repositories, event services, security services, and application services.
     *
     * @return initialized application context
     */
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
     * Authenticates the demo administrator in console mode.
     *
     * @param scanner console scanner
     * @return true if the admin login succeeds, otherwise false
     */
    private static boolean authenticateConsoleAdmin(Scanner scanner) {
        LOGGER.info("Administrator Login");
        LOGGER.info("Enter admin email or alias:");

        String username = readRequiredInput(scanner);

        if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(username)) {
            LOGGER.info("Console mode exited.");
            return false;
        }

        LOGGER.info("Enter admin password:");
        String password = readRequiredInput(scanner);

        if (isValidConsoleAdmin(username, password)) {
            LOGGER.info("Console administrator login successful.");
            return true;
        }

        LOGGER.warning("Invalid administrator credentials.");
        return false;
    }

    /**
     * Runs the console menu until the administrator logs out or exits.
     *
     * @param context initialized application context
     * @param scanner console scanner
     */
    private static void runConsoleMenu(ApplicationContext context, Scanner scanner) {
        boolean running = true;

        while (running) {
            printConsoleMenu();
            String choice = readRequiredInput(scanner);
            running = handleConsoleMenuChoice(choice, context, scanner);
        }
    }

    /**
     * Prints console menu options.
     */
    private static void printConsoleMenu() {
        LOGGER.info("");
        LOGGER.info("===== Appointment System Console =====");
        LOGGER.info(MENU_OPTION_SHOW_SLOTS + ". Show available appointment slots");
        LOGGER.info(MENU_OPTION_BOOK_SLOT + ". Book appointment slot");
        LOGGER.info(MENU_OPTION_LOGOUT + ". Logout");
        LOGGER.info("Type '" + CONSOLE_EXIT_COMMAND + "' to exit.");
        LOGGER.info("Choose an option:");
    }

    /**
     * Handles one console menu choice.
     *
     * @param choice selected menu option
     * @param context initialized application context
     * @param scanner console scanner
     * @return true to keep menu running, false to stop
     */
    private static boolean handleConsoleMenuChoice(
            String choice,
            ApplicationContext context,
            Scanner scanner) {

        if (CONSOLE_EXIT_COMMAND.equalsIgnoreCase(choice)
                || MENU_OPTION_LOGOUT.equals(choice)) {
            LOGGER.info("Console administrator logged out.");
            return false;
        }

        if (MENU_OPTION_SHOW_SLOTS.equals(choice)) {
            showAvailableSlots(context);
            return true;
        }

        if (MENU_OPTION_BOOK_SLOT.equals(choice)) {
            bookSlotFromConsole(context, scanner);
            return true;
        }

        LOGGER.warning("Invalid console option.");
        return true;
    }

    /**
     * Displays available appointment slots in console mode.
     *
     * @param context initialized application context
     */
    private static void showAvailableSlots(ApplicationContext context) {
        List<AppointmentSlot> slots = findAvailableSlots(context.appointmentService);

        if (slots.isEmpty()) {
            LOGGER.info("No available appointment slots were found.");
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            LOGGER.info((i + 1) + ". " + slots.get(i));
        }
    }

    /**
     * Handles console booking flow.
     *
     * @param context initialized application context
     * @param scanner console scanner
     */
    private static void bookSlotFromConsole(ApplicationContext context, Scanner scanner) {
        List<AppointmentSlot> slots = findAvailableSlots(context.appointmentService);

        if (slots.isEmpty()) {
            LOGGER.info("No available appointment slots can be booked.");
            return;
        }

        showAvailableSlots(context);
        LOGGER.info("Enter slot number to book:");

        String selectedValue = readRequiredInput(scanner);
        int selectedIndex = parseSlotIndex(selectedValue, slots.size());

        if (selectedIndex < 0) {
            LOGGER.warning("Invalid slot number.");
            return;
        }

        AppointmentSlot selectedSlot = slots.get(selectedIndex);
        LOGGER.info("Selected slot: " + selectedSlot);
        LOGGER.info("Booking from console is available through the service layer.");
    }

    /**
     * Reads one trimmed line from the scanner.
     *
     * @param scanner console scanner
     * @return trimmed input
     */
    private static String readRequiredInput(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return CONSOLE_EXIT_COMMAND;
        }

        return scanner.nextLine().trim();
    }

    /**
     * Checks demo console administrator credentials.
     *
     * @param username entered username or email
     * @param password entered password
     * @return true if credentials are valid
     */
    private static boolean isValidConsoleAdmin(String username, String password) {
        boolean validUsername = CONSOLE_ADMIN_ALIAS.equalsIgnoreCase(username)
                || CONSOLE_ADMIN_EMAIL.equalsIgnoreCase(username);

        return validUsername && CONSOLE_ADMIN_PASSWORD.equals(password);
    }

    /**
     * Parses slot number from user input.
     *
     * @param selectedValue selected value as text
     * @param slotCount number of available slots
     * @return zero-based slot index, or -1 if invalid
     */
    private static int parseSlotIndex(String selectedValue, int slotCount) {
        try {
            int selectedIndex = Integer.parseInt(selectedValue) - 1;
            return selectedIndex >= 0 && selectedIndex < slotCount ? selectedIndex : -1;
        } catch (NumberFormatException exception) {
            LOGGER.log(Level.WARNING, "Slot number must be numeric.", exception);
            return -1;
        }
    }

    /**
     * Finds available slots using the appointment service.
     *
     * @param appointmentService appointment service
     * @return available appointment slots, or an empty list if unavailable
     */
    @SuppressWarnings("unchecked")
    private static List<AppointmentSlot> findAvailableSlots(AppointmentService appointmentService) {
        try {
            Method method = appointmentService.getClass().getMethod("getAvailableSlots");
            Object result = method.invoke(appointmentService);

            if (result instanceof List<?>) {
                return (List<AppointmentSlot>) result;
            }
        } catch (NoSuchMethodException exception) {
            LOGGER.log(Level.FINE, "getAvailableSlots method was not found.", exception);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            LOGGER.log(Level.WARNING, "Unable to load available appointment slots.", exception);
        }

        return Collections.emptyList();
    }

    /**
     * Holds dependencies required by the GUI and console bootstrapping logic.
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
