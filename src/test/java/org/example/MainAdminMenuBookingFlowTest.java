package org.example;

import org.example.domain.Appointment;
import org.example.notification.LoginNotifier;
import org.example.presentation.ConsoleViewSlots;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.SessionManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MainAdminMenuBookingFlowTest {

    @Test
    void runAdminMenu_BookAppointment_ValidInput_PrintsSuccessMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n10:00\n60\n2\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking confirmed successfully."));
        verify(bookingRepository).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_BlankCustomerName_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\n   \n   \n121\n6\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: customer name cannot be blank."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_BlankSlotTime_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n   \n60\n2\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: slot time cannot be blank."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_DurationAboveMaximum_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n10:00\n121\n2\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: duration exceeds the allowed maximum (120 minutes)."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_ParticipantCountAboveMaximum_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n10:00\n60\n6\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: participant count exceeds the allowed maximum (5)."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_NonExistentSlot_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n15:00\n60\n2\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: selected slot does not exist."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_BookAppointment_AlreadyBookedSlot_PrintsFailureMessage() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        for (org.example.domain.AppointmentSlot slot : appointmentRepository.findAll()) {
            if ("10:00".equals(slot.getTime())) {
                slot.book();
            }
        }

        AppointmentBookingRepository bookingRepository = mock(AppointmentBookingRepository.class);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        AppointmentBookingService bookingService = new AppointmentBookingService(
                appointmentRepository,
                bookingRepository
        );
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "2\nAlice\n10:00\n60\n2\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Booking failed: selected slot is already booked."));
        verify(bookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void runAdminMenu_ViewAvailableSlotsAndLogout_StillWorks() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
        AppointmentService appointmentService = new AppointmentService(appointmentRepository);
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService);
        AuthEventLogger authEventLogger = mock(AuthEventLogger.class);
        LoginNotifier loginNotifier = mock(LoginNotifier.class);

        String printed = runMenuAndCaptureOutput(
                "1\n3\n",
                sessionManager,
                viewSlots,
                authEventLogger,
                loginNotifier
        );

        assertTrue(printed.contains("Available Appointment Slots"));
        assertTrue(printed.contains("10:00"));
        assertTrue(printed.contains("11:00"));
        assertTrue(printed.contains("12:00"));
        assertTrue(printed.contains("1. View available slots"));
        assertTrue(printed.contains("2. Book appointment"));
        assertTrue(printed.contains("3. Logout"));
        assertTrue(printed.contains("You have been logged out successfully."));
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        verify(authEventLogger).logLogout("admin");
        verify(loginNotifier).notifyLogout("admin");
    }

    private String runMenuAndCaptureOutput(
            String scannerInput,
            SessionManager sessionManager,
            ConsoleViewSlots viewSlots,
            AuthEventLogger authEventLogger,
            LoginNotifier loginNotifier
    ) throws Exception {
        Scanner scanner = new Scanner(scannerInput);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

            Method runAdminMenu = Main.class.getDeclaredMethod(
                    "runAdminMenu",
                    Scanner.class,
                    SessionManager.class,
                    ConsoleViewSlots.class,
                    AuthEventLogger.class,
                    LoginNotifier.class
            );
            runAdminMenu.setAccessible(true);
            runAdminMenu.invoke(null, scanner, sessionManager, viewSlots, authEventLogger, loginNotifier);
        } finally {
            System.setOut(originalOut);
            scanner.close();
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}


