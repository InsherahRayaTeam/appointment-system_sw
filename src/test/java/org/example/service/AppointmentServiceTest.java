package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.notification.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AppointmentServiceTest {

    private static final String TEN_AM = "10:00";
    private static final String ELEVEN_AM = "11:00";
    private static final String TWELVE_PM = "12:00";
    private static final String THREE_PM = "15:00";
    private static final String REMINDER_PREFIX = "Reminder: Appointment at";

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
        boolean result = appointmentService.bookSlot(TEN_AM);
        assertTrue(result);
    }

    @Test
    void bookSlot_sameSlotTwice_failsSecondTime() {
        appointmentService.bookSlot(TEN_AM);
        boolean result = appointmentService.bookSlot(TEN_AM);
        assertFalse(result);
    }

    @Test
    void bookSlot_nonExistingSlot_fails() {
        boolean result = appointmentService.bookSlot(THREE_PM);
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
        appointmentService.bookSlot(TEN_AM);
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(2, slots.size());
    }

    @Test
    void shouldNotifyObserverWhenReminderIsSentForBookedSlot() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        appointmentService.bookSlot(TEN_AM);
        boolean reminderSent = appointmentService.sendReminderForSlot(TEN_AM);

        assertTrue(reminderSent);
        verify(observer, times(1)).update(contains(REMINDER_PREFIX));
    }

    @Test
    void shouldNotNotifyObserverWhenReminderConditionsAreNotMet() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        boolean reminderSent = appointmentService.sendReminderForSlot(TEN_AM);

        assertFalse(reminderSent);
        verify(observer, never()).update(anyString());
    }

    @Test
    void shouldNotifyMultipleObserversWhenReminderIsSent() {
        Observer observer1 = mock(Observer.class);
        Observer observer2 = mock(Observer.class);
        eventManager.subscribe(observer1);
        eventManager.subscribe(observer2);

        appointmentService.bookSlot(ELEVEN_AM);
        appointmentService.sendReminderForSlot(ELEVEN_AM);

        verify(observer1, times(1)).update(contains(REMINDER_PREFIX));
        verify(observer2, times(1)).update(contains(REMINDER_PREFIX));
    }

    @Test
    void shouldNotNotifyUnsubscribedObserverWhenReminderIsSent() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);

        appointmentService.bookSlot(TWELVE_PM);
        appointmentService.sendReminderForSlot(TWELVE_PM);

        verify(observer, never()).update(anyString());
    }

    @Test
    void shouldSendAllRemindersForBookedSlots() {
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);
        appointmentService.bookSlot(TEN_AM);
        appointmentService.bookSlot(ELEVEN_AM);

        int sentCount = appointmentService.sendAllReminders();

        assertEquals(2, sentCount);
        verify(observer, times(2)).update(contains(REMINDER_PREFIX));
    }

    @Test
    void addSlot_ValidFutureDateTime_ReturnsSuccessAndAddsVisibleSlot() {
        LocalDate date = LocalDate.now().plusDays(2);

        BookingStatus status = appointmentService.addSlot(date.toString(), "14:30");

        assertEquals(BookingStatus.SUCCESS, status);
        assertTrue(
                appointmentService.getAvailableSlots()
                        .stream()
                        .anyMatch(slot -> date.equals(slot.getDate()) && "14:30".equals(slot.getTime()))
        );
    }

    @Test
    void addSlot_DuplicateDateTime_ReturnsDuplicateSlot() {
        LocalDate date = LocalDate.now().plusDays(2);

        BookingStatus first = appointmentService.addSlot(date.toString(), "14:30");
        BookingStatus second = appointmentService.addSlot(date.toString(), "14:30");

        assertEquals(BookingStatus.SUCCESS, first);
        assertEquals(BookingStatus.DUPLICATE_SLOT, second);
    }

    @Test
    void addSlot_InvalidDateOrTimeFormat_ReturnsInvalidSlotDateTime() {
        assertEquals(BookingStatus.INVALID_SLOT_DATE_TIME, appointmentService.addSlot("2026/12/12", "14:30"));
        assertEquals(BookingStatus.INVALID_SLOT_DATE_TIME, appointmentService.addSlot(LocalDate.now().plusDays(2).toString(), "2pm"));
    }

    @Test
    void addSlot_BlankInput_ReturnsExpectedStatus() {
        assertEquals(BookingStatus.INVALID_SLOT_DATE_TIME, appointmentService.addSlot("   ", "10:00"));
        assertEquals(BookingStatus.BLANK_SLOT_TIME, appointmentService.addSlot(LocalDate.now().plusDays(2).toString(), "   "));
    }
}
