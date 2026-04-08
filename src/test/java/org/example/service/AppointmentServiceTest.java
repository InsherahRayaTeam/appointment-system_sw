package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.notification.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
        appointmentService = new AppointmentService(new org.example.repository.InMemoryAppointmentRepository(), eventManager);
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
    void shouldNotifyObserverWhenReminderIsSentForBookedSlot() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        appointmentService.bookSlot("10:00");
        boolean reminderSent = appointmentService.sendReminderForSlot("10:00");

        assertTrue(reminderSent);
        verify(observer, times(1)).update("Reminder: Appointment at 10:00");
    }

    @Test
    void shouldNotNotifyObserverWhenReminderConditionsAreNotMet() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        boolean reminderSent = appointmentService.sendReminderForSlot("10:00");

        assertFalse(reminderSent);
        verify(observer, never()).update(anyString());
    }

    @Test
    void shouldNotifyMultipleObserversWhenReminderIsSent() {
        Observer observer1 = mock(Observer.class);
        Observer observer2 = mock(Observer.class);
        eventManager.subscribe(observer1);
        eventManager.subscribe(observer2);

        appointmentService.bookSlot("11:00");
        appointmentService.sendReminderForSlot("11:00");

        verify(observer1, times(1)).update("Reminder: Appointment at 11:00");
        verify(observer2, times(1)).update("Reminder: Appointment at 11:00");
    }

    @Test
    void shouldNotNotifyUnsubscribedObserverWhenReminderIsSent() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);

        appointmentService.bookSlot("12:00");
        appointmentService.sendReminderForSlot("12:00");

        verify(observer, never()).update(anyString());
    }

    @Test
    void shouldSendAllRemindersForBookedSlots() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);
        appointmentService.bookSlot("10:00");
        appointmentService.bookSlot("11:00");

        int sentCount = appointmentService.sendAllReminders();

        assertEquals(2, sentCount);
        verify(observer, times(1)).update("Reminder: Appointment at 10:00");
        verify(observer, times(1)).update("Reminder: Appointment at 11:00");
    }
}
