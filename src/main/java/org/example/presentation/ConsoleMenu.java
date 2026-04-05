package org.example.presentation;

import org.example.domain.AppointmentSlot;
import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
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
    private final SessionManager sessionManager;
    private final ConsoleBookingView bookingView;

    /**
     * Creates a console menu controller with required UI and service dependencies.
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
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler cannot be null");
        this.adminAuthService = Objects.requireNonNull(adminAuthService, "adminAuthService cannot be null");
        this.appointmentService = Objects.requireNonNull(appointmentService, "appointmentService cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.bookingView = new ConsoleBookingView();
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
            showAdminMenu();
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
        showAdminMenu();
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

    private void showAdminMenu() {
        while (sessionManager.isLoggedIn()) {
            bookingView.printSectionHeader("Admin Menu");
            System.out.println("7. View Slots");
            System.out.println("8. Book Appointment");
            System.out.println("9. Logout");
            bookingView.printSeparator();

            int choice = inputHandler.readMenuChoice("Choose an option (7-9): ", 7, 9);
            if (choice == 7) {
                handleViewSlots();
            } else if (choice == 8) {
                handleBookAppointment();
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
        System.out.println("   Appointment System Administrator");
        System.out.println("========================================");
    }
}

