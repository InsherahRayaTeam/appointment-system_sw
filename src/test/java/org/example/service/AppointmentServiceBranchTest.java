package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceBranchTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EventManager eventManager;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, eventManager);
    }

    @ParameterizedTest
    @CsvSource({
            "'   ',10:00,INVALID_SLOT_DATE_TIME",
            "2026/12/12,10:00,INVALID_SLOT_DATE_TIME",
            "2030-12-12,'   ',BLANK_SLOT_TIME",
            "2030-12-12,2pm,INVALID_SLOT_DATE_TIME"
    })
    void addSlot_TextInputValidationMatrix_ReturnsExpectedStatus(String dateText, String timeText, BookingStatus expected) {
        BookingStatus result = appointmentService.addSlot(dateText, timeText);

        assertEquals(expected, result);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(eventManager);
    }

    @Test
    void addSlot_DateTimeNotFuture_ReturnsInvalidSlotDateTime() {
        BookingStatus result = appointmentService.addSlot(LocalDate.now(), LocalTime.now().minusMinutes(1));

        assertEquals(BookingStatus.INVALID_SLOT_DATE_TIME, result);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void addSlot_DuplicateDateTimeInRepository_ReturnsDuplicateSlotWithoutPersisting() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(10, 30);
        when(appointmentRepository.findAll()).thenReturn(List.of(new AppointmentSlot(date, time)));

        BookingStatus result = appointmentService.addSlot(date, time);

        assertEquals(BookingStatus.DUPLICATE_SLOT, result);
        verify(appointmentRepository, never()).addSlot(any(AppointmentSlot.class));
        verifyNoInteractions(eventManager);
    }

    @Test
    void addSlot_RepositoryRejectsAddition_ReturnsDuplicateSlotAndSkipsNotification() {
        LocalDate date = LocalDate.now().plusDays(4);
        LocalTime time = LocalTime.of(16, 0);
        when(appointmentRepository.findAll()).thenReturn(List.of());
        when(appointmentRepository.addSlot(any(AppointmentSlot.class))).thenReturn(false);

        BookingStatus result = appointmentService.addSlot(date, time);

        assertEquals(BookingStatus.DUPLICATE_SLOT, result);
        verify(eventManager, never()).notifyObservers(contains("Appointment slot added:"));
    }

    @Test
    void addSlot_ValidUniqueFutureDateTime_ReturnsSuccessAndNotifiesObservers() {
        LocalDate date = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(9, 0);
        LocalDateTime dateTime = date.atTime(time);
        when(appointmentRepository.findAll()).thenReturn(List.of());
        when(appointmentRepository.addSlot(any(AppointmentSlot.class))).thenReturn(true);

        BookingStatus result = appointmentService.addSlot(date, time);

        assertEquals(BookingStatus.SUCCESS, result);
        verify(eventManager).notifyObservers("Appointment slot added: " + dateTime);
    }

    @Test
    void sendReminderForSlot_MatchingSlotNotBooked_ReturnsFalseWithoutNotification() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        boolean sent = appointmentService.sendReminderForSlot("10:00");

        assertFalse(sent);
        verify(eventManager, never()).notifyObservers(contains("Reminder:"));
    }

    @Test
    void bookSlot_TrimmedInputMatchesUnbookedSlot_BooksAndNotifiesOnce() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        boolean result = appointmentService.bookSlot(" 10:00 ");

        assertTrue(result);
        assertTrue(slot.isBooked());
        verify(eventManager).notifyObservers(contains("Appointment booked successfully at"));
    }

    @Test
    void sendAllReminders_MixedBookedAndUnbookedSlots_NotifiesOnlyBookedSlots() {
        AppointmentSlot first = new AppointmentSlot("10:00");
        AppointmentSlot second = new AppointmentSlot("11:00");
        AppointmentSlot third = new AppointmentSlot("12:00");
        first.book();
        third.book();
        when(appointmentRepository.findAll()).thenReturn(List.of(first, second, third));

        int sentCount = appointmentService.sendAllReminders();

        assertEquals(2, sentCount);
        verify(eventManager).notifyObservers(contains(first.getDateDayTimeLabel()));
        verify(eventManager).notifyObservers(contains(third.getDateDayTimeLabel()));
    }
}

