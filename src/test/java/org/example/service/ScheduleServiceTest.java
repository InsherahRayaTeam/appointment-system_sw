package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.domain.UserRole;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ScheduleService focusing on role-based access control and slot management.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private EventManager eventManager;

    private ScheduleService scheduleService;

    @BeforeEach
    void setup() {
        when(appointmentRepository.findAll()).thenReturn(new ArrayList<>());
        scheduleService = new ScheduleService(appointmentRepository, sessionManager, eventManager);
    }

    @Test
    void testListAvailableSlotsRequiresLogin() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        assertThrows(IllegalStateException.class, scheduleService::listAvailableSlots);
    }

    @Test
    void testListAvailableSlotsWhenLoggedIn() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        
        AppointmentSlot slot1 = new AppointmentSlot("10:00");
        AppointmentSlot slot2 = new AppointmentSlot("11:00");
        slot2.book(); // This one is booked, should not appear

        when(appointmentRepository.findAll()).thenReturn(List.of(slot1, slot2));
        scheduleService = new ScheduleService(appointmentRepository, sessionManager, eventManager);

        List<AppointmentSlot> available = scheduleService.listAvailableSlots();

        assertEquals(1, available.size());
        assertEquals("10:00", available.get(0).getTime());
    }

    @Test
    void testAddSlotRequiresAdminRole() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> scheduleService.addSlot("10:00"));
    }

    @Test
    void testAddSlotWithAdminRole() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentRepository.save(any(AppointmentSlot.class))).thenReturn(true);

        boolean result = scheduleService.addSlot("10:00");

        assertTrue(result);
        verify(appointmentRepository).save(any(AppointmentSlot.class));
        verify(eventManager).notifyObservers(contains("New slot created"));
    }

    @Test
    void testAddSlotWithInvalidTime() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        assertFalse(scheduleService.addSlot(null));
        assertFalse(scheduleService.addSlot(""));
        assertFalse(scheduleService.addSlot("   "));
    }

    @Test
    void testCancelSlotRequiresAdminRole() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> scheduleService.cancelSlot("10:00"));
    }

    @Test
    void testCancelSlotWithAdminRole() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(sessionManager.getCurrentUsername()).thenReturn("admin");
        when(appointmentRepository.removeSlot("10:00")).thenReturn(true);

        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        scheduleService = new ScheduleService(appointmentRepository, sessionManager, eventManager);

        boolean result = scheduleService.cancelSlot("10:00");

        assertTrue(result);
        assertTrue(slot.isCancelled());
        verify(eventManager).notifyObservers(contains("Slot cancelled by admin"));
    }

    @Test
    void testGetSlotCounts() {
        AppointmentSlot slot1 = new AppointmentSlot("10:00");
        AppointmentSlot slot2 = new AppointmentSlot("11:00");
        slot2.book();
        AppointmentSlot slot3 = new AppointmentSlot("12:00");
        slot3.cancel();

        when(appointmentRepository.findAll()).thenReturn(List.of(slot1, slot2, slot3));
        scheduleService = new ScheduleService(appointmentRepository, sessionManager, eventManager);

        assertEquals(3, scheduleService.getTotalSlotCount());
        assertEquals(1, scheduleService.getAvailableSlotCount());
        assertEquals(1, scheduleService.getBookedSlotCount());
        assertEquals(1, scheduleService.getCancelledSlotCount());
    }
}

