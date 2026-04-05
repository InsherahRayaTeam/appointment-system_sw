package org.example;

import org.example.notification.LoginNotifier;
import org.example.presentation.gui.ApplicationController;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.UserRepository;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.EventManager;
import org.example.service.LoginAttemptTracker;
import org.example.service.SessionManager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.Duration;

/**
 * Application bootstrap class that wires dependencies and launches GUI.
 *
 * @author appointment-system
 * @version 1.0
 */
public class Main {

    /**
     * Starts the appointment system with GUI interface.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
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
                            eventManager
                    );

            // ===== APPLICATION CONTROLLER =====
            ApplicationController appController = new ApplicationController(
                    authService,
                    appointmentService,
                    appointmentBookingService,
                    sessionManager
            );

            appController.start();
        });
    }
}