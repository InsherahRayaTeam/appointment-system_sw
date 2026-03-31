package org.example;

import org.example.notification.LoginNotifier;
import org.example.presentation.gui.ApplicationController;
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
import org.example.service.LoginAttemptTracker;
import org.example.service.SessionManager;

import javax.swing.UIManager;
import java.time.Duration;

/**
 * Application bootstrap class that wires dependencies and launches GUI.
 * ARCHITECTURE:
 * - Presentation Layer (GUI): LoginFrame, MainDashboardFrame, SlotsPanel, BookingPanel, ReservationsPanel
 * - Service Layer: AdminAuthService, AppointmentService, AppointmentBookingService, SessionManager, etc.
 * - Repository Layer: InMemoryAdminRepository, InMemoryAppointmentRepository, InMemoryAppointmentBookingRepository
 * - Domain Layer: AdminUser, AppointmentSlot, Appointment, Credentials, etc.
 * GUI FLOW:
 * 1. Application Startup - LoginFrame shown first
 * 2. User logs in via GUI - AdminAuthService validates credentials
 * 3. Session created - SessionManager tracks authentication
 * 4. MainDashboardFrame shown - User can view slots, book appointments, and manage reservations
 * 5. User logs out - Session cleared, back to LoginFrame
 *
 * @author appointment-system
 * @version 1.0
 */
public class Main {

    /**
     * Starts the appointment system with GUI interface.
     * Sets up all service layer dependencies and launches the GUI application.
     * All authentication and business logic is delegated to appropriate services.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default Swing look-and-feel if system look-and-feel is unavailable.
        }

        // ===== DEPENDENCY WIRING =====
        // Repository layer: stores admin users
        AdminRepository adminRepository = new InMemoryAdminRepository();
        
        // Repository layer: stores appointments
        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();

        // Repository layer: stores confirmed reservations
        AppointmentBookingRepository appointmentBookingRepository = new InMemoryAppointmentBookingRepository();
        
        // Service layer: event notification system
        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        
        // Service layer: session management
        SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);
        
        // Service layer: login attempt tracking & lockout policy
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        
        // Observers: listen for auth events
        LoginNotifier loginNotifier = new LoginNotifier();
        eventManager.subscribe(loginNotifier);

        // Service layer: authentication logic
        AdminAuthService authService = new AdminAuthService(adminRepository, eventManager, loginAttemptTracker);
        
        // Service layer: appointment operations
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, eventManager);

        // Service layer: reservation booking/management operations
        AppointmentBookingService appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                adminRepository,
                eventManager
        );

        // ===== GUI APPLICATION LAUNCH =====
        // Create and start GUI application controller
        // Presentation layer (GUI) is launched here
        ApplicationController appController = new ApplicationController(
            authService,
            appointmentService,
            appointmentBookingService,
            sessionManager
        );
        
        // Start GUI application
        appController.start();
    }
}
