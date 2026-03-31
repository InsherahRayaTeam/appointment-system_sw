package org.example.presentation;

import org.example.domain.AppointmentSlot;
import org.example.domain.Credentials;
import org.example.domain.UserRole;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.BookingStatus;
import org.example.service.LoginStatus;
import org.example.service.SessionManager;

import java.util.List;
import java.util.Objects;

/**
 * Console menu orchestration layer. Handles UI flow only and delegates all business operations to services.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ConsoleMenu {

    private final ConsoleInputHandler inputHandler;
    private final AdminAuthService adminAuthService;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final SessionManager sessionManager;
    private final ConsoleBookingView bookingView;

    /**
     * Creates a console menu controller with required UI and service dependencies.
     *
     * @param inputHandler input helper for console prompts
     * @param adminAuthService authentication service
     * @param appointmentService appointment service
     * @param appointmentBookingService booking service
     * @param sessionManager session service
     */
    public ConsoleMenu(
            ConsoleInputHandler inputHandler,
            AdminAuthService adminAuthService,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            SessionManager sessionManager
    ) {
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler cannot be null");
        this.adminAuthService = Objects.requireNonNull(adminAuthService, "adminAuthService cannot be null");
        this.appointmentService = Objects.requireNonNull(appointmentService, "appointmentService cannot be null");
        this.appointmentBookingService = Objects.requireNonNull(appointmentBookingService, "appointmentBookingService cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.bookingView = new ConsoleBookingView();
    }

    /**
     * Creates a console menu controller with required UI and service dependencies.
     * Backward compatible constructor that initializes appointmentBookingService to null.
     *
     * @param inputHandler input helper for console prompts
     * @param adminAuthService authentication service
     * @param appointmentService appointment service
     * @param sessionManager session service
     */
    public ConsoleMenu(
            ConsoleInputHandler inputHandler,
            AdminAuthService adminAuthService,
            AppointmentService appointmentService,
            SessionManager sessionManager
    ) {
        this(inputHandler, adminAuthService, appointmentService, null, sessionManager);
    }

    /**
     * Starts the console UI loop until the user exits from the login screen.
     */
    public void run() {
        printAppHeader();

        boolean running = true;
        while (running) {
            if (!sessionManager.isLoggedIn()) {
                running = showLoginScreen();
                continue;
            }
            showMenu();
        }

        System.out.println("\nThank you for using Appointment System.");
    }

    /**
     * Displays the admin menu and handles user interactions until logout.
     * IMPORTANT: User must already be authenticated before calling this method.
     * Use this method when authentication is handled externally (e.g., in Main).
     * 
     * Prerequisite: sessionManager.isLoggedIn() must be true
     */
    public void runAdminMenuUntilLogout() {
        printAppHeader();
        showMenu();
        // After logout, sessionManager.isLoggedIn() returns false and menu exits
    }

    private boolean showLoginScreen() {
        bookingView.printSectionHeader("Administrator Login");
        String username = inputHandler.readLine("Username (type 'exit' to quit): ").trim();
        if (isExitCommand(username)) {
            return false;
        }

        String password = inputHandler.readLine("Password (type 'exit' to quit): ");
        if (isExitCommand(password)) {
            return false;
        }

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(new Credentials(username, password));
        if (result.isSuccess()) {
            sessionManager.login(username);
            System.out.println("Login successful. Welcome, " + username.trim() + ".\n");
            return true;
        }

        if (result.isLocked()) {
            System.out.println("Account is temporarily locked. Try again in "
                    + result.getRemainingLockSeconds() + " second(s).\n");
            return true;
        }

        showLoginError(result.getStatus(), result.getAttemptsRemaining());
        return true;
    }

    private void showMenu() {
        if (sessionManager.isAdmin()) {
            showAdminMenu();
        } else if (sessionManager.isUser()) {
            showUserMenu();
        }
    }

    private void showAdminMenu() {
        while (sessionManager.isLoggedIn() && sessionManager.isAdmin()) {
            bookingView.printSectionHeader("Admin Menu");
            System.out.println("1. View Slots");
            System.out.println("2. Add Slot");
            System.out.println("3. Cancel Slot");
            System.out.println("4. Manage Reservations");
            System.out.println("5. Logout");
            bookingView.printSeparator();

            int choice = inputHandler.readMenuChoice("Choose an option (1-5): ", 1, 5);
            if (choice == 1) {
                handleViewSlots();
            } else if (choice == 2) {
                handleAddSlot();
            } else if (choice == 3) {
                handleCancelSlot();
            } else if (choice == 4) {
                handleManageReservations();
            } else {
                handleLogout();
            }
            System.out.println();
        }
    }

    private void showUserMenu() {
        while (sessionManager.isLoggedIn() && sessionManager.isUser()) {
            bookingView.printSectionHeader("User Menu");
            System.out.println("1. View Slots");
            System.out.println("2. Book Appointment");
            System.out.println("3. My Appointments");
            System.out.println("4. Logout");
            bookingView.printSeparator();

            int choice = inputHandler.readMenuChoice("Choose an option (1-4): ", 1, 4);
            if (choice == 1) {
                handleViewSlots();
            } else if (choice == 2) {
                handleBookAppointment();
            } else if (choice == 3) {
                handleViewMyAppointments();
            } else {
                handleLogout();
            }
            System.out.println();
        }
    }

    private void handleViewSlots() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        bookingView.showSlots(slots);
    }

    private void handleAddSlot() {
        if (appointmentBookingService == null) {
            System.out.println("Slot management not available.");
            return;
        }
        String slotTime = inputHandler.readRequiredLine("Enter new slot time (e.g., 14:00): ");
        BookingStatus status = appointmentBookingService.addManagedSlot(slotTime);
        if (status == BookingStatus.SUCCESS) {
            System.out.println("Slot '" + slotTime + "' added successfully.");
        } else if (status == BookingStatus.UNAUTHORIZED) {
            System.out.println("Admin access required.");
        } else if (status == BookingStatus.SLOT_ALREADY_BOOKED) {
            System.out.println("Slot already exists.");
        } else {
            System.out.println("Failed to add slot.");
        }
    }

    private void handleCancelSlot() {
        if (appointmentBookingService == null) {
            System.out.println("Slot management not available.");
            return;
        }
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        if (slots.isEmpty()) {
            System.out.println("No available slots to cancel.");
            return;
        }
        bookingView.showSlots(slots);
        String slotTime = inputHandler.readRequiredLine("Enter slot time to cancel: ");
        BookingStatus status = appointmentBookingService.cancelManagedSlot(slotTime);
        if (status == BookingStatus.SUCCESS) {
            System.out.println("Slot '" + slotTime + "' cancelled successfully.");
        } else if (status == BookingStatus.UNAUTHORIZED) {
            System.out.println("Admin access required.");
        } else if (status == BookingStatus.SLOT_NOT_FOUND) {
            System.out.println("Slot not found.");
        } else {
            System.out.println("Failed to cancel slot.");
        }
    }

    private void handleManageReservations() {
        System.out.println("(Reservation management functionality would be implemented here)");
    }

    private void handleViewMyAppointments() {
        System.out.println("(My appointments functionality would be implemented here)");
    }

    private void handleBookAppointment() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        if (slots.isEmpty()) {
            bookingView.showNoSlotsToBook();
            return;
        }

        bookingView.showSlots(slots);
        String slotTime = inputHandler.readRequiredLine("Enter slot time to book: ");
        boolean booked = appointmentService.bookSlot(slotTime);
        bookingView.showBookingResult(slotTime, booked);
    }

    private void handleLogout() {
        sessionManager.logoutAndNotify();
        System.out.println("You have been logged out successfully.");
    }

    private void showLoginError(LoginStatus status, int attemptsRemaining) {
        if (status == LoginStatus.BLANK_INPUT) {
            System.out.println("Username and password are required.");
        } else {
            System.out.println("Invalid username or password.");
        }
        System.out.println("Attempts remaining: " + attemptsRemaining + "\n");
    }

    private boolean isExitCommand(String value) {
        return "exit".equalsIgnoreCase(value == null ? "" : value.trim());
    }

    private void printAppHeader() {
        System.out.println("========================================");
        System.out.println("   Appointment System");
        System.out.println("========================================");
    }
}

