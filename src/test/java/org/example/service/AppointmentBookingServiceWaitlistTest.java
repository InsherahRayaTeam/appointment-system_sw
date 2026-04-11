package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.domain.WaitlistEntry;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryWaitlistRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Focused waitlist tests for booking, duplicate rejection, FIFO promotion, and rollback safety.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceWaitlistTest {

    private static final String SLOT_TIME = "10:00";
    private static final String ALICE_EMAIL = "alice@example.com";
    private static final String BOB_EMAIL = "bob@example.com";
    private static final String CAROL_EMAIL = "carol@example.com";

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventManager eventManager;

    @Mock
    private AppointmentNotificationCoordinator appointmentNotificationCoordinator;

    private InMemoryWaitlistRepository waitlistRepository;
    private AppointmentBookingService appointmentBookingService;

    @BeforeEach
    void setUp() {
        waitlistRepository = new InMemoryWaitlistRepository();
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager,
                waitlistRepository,
                appointmentNotificationCoordinator
        );
    }

    @Test
    void bookAppointment_FullSlot_AddsCustomerToWaitlistAndReturnsWaitlisted() {
        AppointmentSlot slot = bookedSlot();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.WAITLISTED, result);
        assertFalse(waitlistRepository.findAll().isEmpty());
        WaitlistEntry entry = waitlistRepository.findAll().get(0);
        assertEquals(ALICE_EMAIL, entry.getCustomerEmail());
        assertEquals(slot.getDateTime(), entry.getSlotDateTime());
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void bookAppointment_DuplicateWaitlistAttempt_IsRejected() {
        AppointmentSlot slot = bookedSlot();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus first = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);
        BookingStatus second = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.WAITLISTED, first);
        assertEquals(BookingStatus.WAITLIST_ALREADY_EXISTS, second);
        assertEquals(1, waitlistRepository.findAll().size());
    }

    @Test
    void bookAppointment_ConfirmedAppointmentAlreadyBookedOnSameSlot_IsRejectedFromWaitlist() {
        AppointmentSlot slot = bookedSlot();
        Appointment confirmed = appointmentAt("apt-confirmed", slot.getDateTime());
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.findAll()).thenReturn(List.of(confirmed));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.WAITLIST_ALREADY_BOOKED, result);
        assertTrue(waitlistRepository.findAll().isEmpty());
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_FutureReservation_PromotesFirstWaitlistedUserAndNotifies() {
        authenticateAsAdmin();

        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-cancel", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-cancel"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        waitlistRepository.save(new WaitlistEntry(
                "wait-1",
                BOB_EMAIL,
                BOB_EMAIL,
                null,
                slot.getDateTime(),
                60,
                1,
                AppointmentType.NORMAL,
                LocalDateTime.now().minusMinutes(2)
        ));
        waitlistRepository.save(new WaitlistEntry(
                "wait-2",
                CAROL_EMAIL,
                CAROL_EMAIL,
                null,
                slot.getDateTime(),
                60,
                1,
                AppointmentType.NORMAL,
                LocalDateTime.now().minusMinutes(1)
        ));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-cancel");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isBooked());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(CAROL_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());

        ArgumentCaptor<Appointment> savedAppointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(any(Appointment.class));
        verify(appointmentBookingRepository).save(savedAppointmentCaptor.capture());
        assertEquals(AppointmentStatus.CONFIRMED, savedAppointmentCaptor.getValue().getStatus());
        assertEquals(BOB_EMAIL, savedAppointmentCaptor.getValue().getCustomerName());
        verify(eventManager).notifyObservers(contains("Reservation cancelled: apt-cancel"));
        verify(eventManager).notifyObservers(contains("Waitlist promotion confirmed:"));
        verify(appointmentNotificationCoordinator).sendWaitlistPromotionNotification(savedAppointmentCaptor.getValue());
    }

    @Test
    void cancelAppointment_WhenNoWaitlistExists_SlotBecomesAvailable() {
        authenticateAsAdmin();

        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-no-waitlist", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-no-waitlist"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-no-waitlist");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isAvailable());
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verify(appointmentNotificationCoordinator, never()).sendWaitlistPromotionNotification(any(Appointment.class));
    }

    @Test
    void cancelAppointment_PromotionSaveFails_RestoresWaitlistEntryAndLeavesSlotAvailable() {
        authenticateAsAdmin();

        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-fail", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-fail"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);
        doThrow(new RuntimeException("save failed")).when(appointmentBookingRepository).save(any(Appointment.class));

        waitlistRepository.save(new WaitlistEntry(
                "wait-fail",
                BOB_EMAIL,
                BOB_EMAIL,
                null,
                slot.getDateTime(),
                60,
                1,
                AppointmentType.NORMAL,
                LocalDateTime.now().minusMinutes(1)
        ));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-fail");

        assertEquals(BookingStatus.WAITLIST_PROMOTION_FAILED, result);
        assertTrue(slot.isAvailable());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(BOB_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());
        verify(appointmentNotificationCoordinator, never()).sendWaitlistPromotionNotification(any(Appointment.class));
        verify(eventManager, never()).notifyObservers(contains("Waitlist promotion confirmed:"));
    }

    @Test
    void waitlistPromotionFifoOrder_UsesOldestEntryFirst() {
        authenticateAsAdmin();

        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-fifo", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-fifo"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        waitlistRepository.save(new WaitlistEntry(
                "wait-old",
                BOB_EMAIL,
                BOB_EMAIL,
                null,
                slot.getDateTime(),
                60,
                1,
                AppointmentType.NORMAL,
                LocalDateTime.now().minusMinutes(3)
        ));
        waitlistRepository.save(new WaitlistEntry(
                "wait-new",
                CAROL_EMAIL,
                CAROL_EMAIL,
                null,
                slot.getDateTime(),
                60,
                1,
                AppointmentType.NORMAL,
                LocalDateTime.now().minusMinutes(1)
        ));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-fifo");

        assertEquals(BookingStatus.SUCCESS, result);
        ArgumentCaptor<Appointment> savedAppointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).save(savedAppointmentCaptor.capture());
        assertEquals(BOB_EMAIL, savedAppointmentCaptor.getValue().getCustomerName());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(CAROL_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());
    }

    private void authenticateAsAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
    }

    private AppointmentSlot bookedSlot() {
        AppointmentSlot slot = new AppointmentSlot(SLOT_TIME);
        slot.book();
        return slot;
    }

    private Appointment appointmentAt(String id, LocalDateTime dateTime) {
        return new Appointment(id, ALICE_EMAIL, dateTime, 60, 1, AppointmentStatus.CONFIRMED);
    }
}

