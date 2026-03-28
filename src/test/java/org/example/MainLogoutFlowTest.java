package org.example;

import org.example.domain.AppointmentSlot;
import org.example.notification.Observer;
import org.example.presentation.ConsoleViewSlots;
import org.example.service.AppointmentService;
import org.example.service.AuthEventLogger;
import org.example.service.EventManager;
import org.example.service.SessionManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainLogoutFlowTest {

    @Test
    void runAdminMenu_LogoutOption_LogsOutAndPrintsConfirmation() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        ConsoleViewSlots viewSlots = new ConsoleViewSlots(new AppointmentService());
        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        Observer observer = org.mockito.Mockito.mock(Observer.class);
        eventManager.subscribe(observer);
        Scanner scanner = new Scanner("3\n");

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
                    EventManager.class
            );
            runAdminMenu.setAccessible(true);
            runAdminMenu.invoke(null, scanner, sessionManager, viewSlots, authEventLogger, eventManager);
        } finally {
            System.setOut(originalOut);
            scanner.close();
        }

        String printed = output.toString(StandardCharsets.UTF_8);

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUsername());
        assertTrue(printed.contains("You have been logged out successfully."));
        org.mockito.Mockito.verify(observer).update("Goodbye, admin! You have been logged out.");
    }

    @Test
    void runAdminMenu_ViewAvailableSlots_PrintsOnlyUnbookedSlots() throws Exception {
        SessionManager sessionManager = new SessionManager();
        sessionManager.login("admin");

        AppointmentService appointmentService = new AppointmentService() {
            @Override
            public List<AppointmentSlot> getAvailableSlots() {
                List<AppointmentSlot> slots = new ArrayList<>();
                slots.add(new AppointmentSlot("11:00"));
                slots.add(new AppointmentSlot("12:00"));
                return slots;
            }
        };

        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService);
        AuthEventLogger authEventLogger = new AuthEventLogger();
        EventManager eventManager = new EventManager();
        Scanner scanner = new Scanner("1\n3\n");

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
                    EventManager.class
            );
            runAdminMenu.setAccessible(true);
            runAdminMenu.invoke(null, scanner, sessionManager, viewSlots, authEventLogger, eventManager);
        } finally {
            System.setOut(originalOut);
            scanner.close();
        }

        String printed = output.toString(StandardCharsets.UTF_8);

        assertTrue(printed.contains("Available Appointment Slots"));
        assertTrue(printed.contains("11:00"));
        assertTrue(printed.contains("12:00"));
        assertFalse(printed.contains("10:00"));
    }
}

