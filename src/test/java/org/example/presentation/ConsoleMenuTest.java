package org.example.presentation;

import org.example.domain.AppointmentSlot;
import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;
import org.example.service.SessionManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleMenuTest {

    @Test
    void invalidMenuChoiceThenValidChoice_IsHandledGracefullyAndContinuesFlow() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.success());
        when(sessionManager.isLoggedIn()).thenReturn(false, true, true, true, false, false);
        when(appointmentService.getAvailableSlots()).thenReturn(List.of(new AppointmentSlot("10:00")));

        String printed = runMenuAndCaptureOutput(
                "admin\nsecret\ninvalid\n7\n9\nexit\n",
                authService,
                appointmentService,
                sessionManager
        );

        assertTrue(printed.contains("Invalid choice. Please enter a number between 7 and 9."));
        verify(authService, times(1)).authenticateWithPolicy(any(Credentials.class));
        verify(sessionManager).login("admin");
        verify(appointmentService, times(1)).getAvailableSlots();
        verify(sessionManager, times(1)).logoutAndNotify();
    }

    @Test
    void failedLoginPath_DoesNotEnterAdminMenuOrTriggerSessionActions() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(sessionManager.isLoggedIn()).thenReturn(false, false);
        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.failure(LoginStatus.INVALID_CREDENTIALS, 2));

        String printed = runMenuAndCaptureOutput(
                "admin\nwrong\nexit\n",
                authService,
                appointmentService,
                sessionManager
        );

        assertTrue(printed.contains("Invalid username or password."));
        verify(authService, times(1)).authenticateWithPolicy(any(Credentials.class));
        verify(sessionManager, never()).login(anyString());
        verify(appointmentService, never()).getAvailableSlots();
        verify(sessionManager, never()).logoutAndNotify();
    }

    @Test
    void successfulLogin_TransitionsToAdminMenuAndLogoutReturnsToLoginScreen() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.success());
        when(sessionManager.isLoggedIn()).thenReturn(false, true, true, false, false);

        String printed = runMenuAndCaptureOutput(
                "admin\nsecret\n9\nexit\n",
                authService,
                appointmentService,
                sessionManager
        );

        assertTrue(printed.contains("Login successful."));
        assertTrue(printed.contains("You have been logged out successfully."));
        verify(sessionManager).login("admin");
        verify(sessionManager).logoutAndNotify();
        verify(authService, times(1)).authenticateWithPolicy(any(Credentials.class));
    }

    @Test
    void viewSlotsAction_DelegatesToAppointmentService() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.success());
        when(sessionManager.isLoggedIn()).thenReturn(false, true, true, true, false, false);
        when(appointmentService.getAvailableSlots())
                .thenReturn(List.of(new AppointmentSlot("10:00"), new AppointmentSlot("11:00")));

        runMenuAndCaptureOutput(
                "admin\nsecret\n7\n9\nexit\n",
                authService,
                appointmentService,
                sessionManager
        );

        verify(appointmentService, times(1)).getAvailableSlots();
        verify(appointmentService, never()).bookSlot(anyString());
        verify(sessionManager).logoutAndNotify();
    }

    @Test
    void bookAppointmentAction_DelegatesToAppointmentServiceBookSlot() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.success());
        when(sessionManager.isLoggedIn()).thenReturn(false, true, true, true, false, false);
        when(appointmentService.getAvailableSlots()).thenReturn(List.of(new AppointmentSlot("10:00")));
        when(appointmentService.bookSlot("10:00")).thenReturn(true);

        String printed = runMenuAndCaptureOutput(
                "admin\nsecret\n8\n10:00\n9\nexit\n",
                authService,
                appointmentService,
                sessionManager
        );

        assertTrue(printed.contains("Success: Appointment booked for 10:00."));
        verify(appointmentService, times(1)).getAvailableSlots();
        verify(appointmentService, times(1)).bookSlot(eq("10:00"));
        verify(sessionManager).logoutAndNotify();
    }

    @Test
    void exitFromLoginScreen_TerminatesCleanlyWithoutServiceCalls() {
        AdminAuthService authService = mock(AdminAuthService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        SessionManager sessionManager = mock(SessionManager.class);

        when(sessionManager.isLoggedIn()).thenReturn(false);

        String printed = runMenuAndCaptureOutput(
                "exit\n",
                authService,
                appointmentService,
                sessionManager
        );

        assertTrue(printed.contains("Thank you for using Appointment System."));
        verify(authService, never()).authenticateWithPolicy(any(Credentials.class));
        verify(appointmentService, never()).getAvailableSlots();
        verify(appointmentService, never()).bookSlot(anyString());
        verify(sessionManager, never()).login(anyString());
        verify(sessionManager, never()).logoutAndNotify();
    }

    private String runMenuAndCaptureOutput(
            String scannerInput,
            AdminAuthService authService,
            AppointmentService appointmentService,
            SessionManager sessionManager
    ) {
        Scanner scanner = new Scanner(scannerInput);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            ConsoleInputHandler inputHandler = new ConsoleInputHandler(scanner);
            ConsoleMenu menu = new ConsoleMenu(inputHandler, authService, appointmentService, sessionManager);
            menu.run();
        } finally {
            System.setOut(originalOut);
            scanner.close();
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}

