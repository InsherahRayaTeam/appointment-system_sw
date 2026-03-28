package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.notification.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService();
    }

    @Test
    void getAvailableSlots_initiallyContainsAllSlots() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(3, slots.size());
    }

    @Test
    void bookSlot_validSlot_succeeds() {
        boolean result = appointmentService.bookSlot("10:00");
        assertTrue(result);
    }

    @Test
    void bookSlot_sameSlotTwice_failsSecondTime() {
        appointmentService.bookSlot("10:00");
        boolean result = appointmentService.bookSlot("10:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nonExistingSlot_fails() {
        boolean result = appointmentService.bookSlot("15:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nullTime_fails() {
        boolean result = appointmentService.bookSlot(null);
        assertFalse(result);
    }

    @Test
    void bookSlot_blankTime_fails() {
        boolean result = appointmentService.bookSlot("   ");
        assertFalse(result);
    }

    @Test
    void getAvailableSlots_afterBooking_decreases() {
        appointmentService.bookSlot("10:00");
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(2, slots.size());
    }
    @Test
    void shouldNotifyObserverWhenReminderIsSentForBookedSlot() throws Exception {
        Observer observer = mock(Observer.class);
        EventManager eventManager = extractEventManager(appointmentService);
        eventManager.subscribe(observer);

        appointmentService.bookSlot("10:00");
        appointmentService.sendReminder("10:00");

        verify(observer, times(1)).update("Reminder: Appointment at 10:00");
    }

    private EventManager extractEventManager(AppointmentService service) throws Exception {
        Field eventManagerField = AppointmentService.class.getDeclaredField("eventManager");
        eventManagerField.setAccessible(true);
        return (EventManager) eventManagerField.get(service);
    }
}