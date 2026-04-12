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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        assertEquals(1, waitlistRepository.findAll().size());
        WaitlistEntry entry = waitlistRepository.findAll().get(0);
        assertEquals(ALICE_EMAIL, entry.getCustomerEmail());
        assertEquals(slot.getDateTime(), entry.getSlotDateTime());
        assertEquals(60, entry.getDurationMinutes());
        assertEquals(1, entry.getParticipantCount());
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void bookAppointment_AvailableSlot_DoesNotCreateWaitlistEntry() {
        AppointmentSlot availableSlot = new AppointmentSlot(SLOT_TIME);
        when(appointmentRepository.findAll()).thenReturn(List.of(availableSlot));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(waitlistRepository.findAll().isEmpty());
        verify(appointmentBookingRepository).save(any(Appointment.class));
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
    void bookAppointment_BlankCustomerName_IsRejected() {
        BookingStatus result = appointmentBookingService.bookAppointment(" ", SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.BLANK_CUSTOMER_NAME, result);
        verifyNoInteractions(appointmentRepository);
        assertTrue(waitlistRepository.findAll().isEmpty());
    }

    @Test
    void bookAppointment_SlotNotFound_ReturnsSlotNotFound() {
        when(appointmentRepository.findAll()).thenReturn(List.of(new AppointmentSlot("11:00")));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.SLOT_NOT_FOUND, result);
        assertTrue(waitlistRepository.findAll().isEmpty());
    }

    @Test
    void bookAppointment_FullSlot_WithType_PreservesTypeInWaitlist() {
        AppointmentSlot slot = bookedSlot();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 30, 1, AppointmentType.URGENT);

        assertEquals(BookingStatus.WAITLISTED, result);
        assertEquals(AppointmentType.URGENT, waitlistRepository.findAll().get(0).getType());
    }

    @Test
    void cancelAppointment_FutureReservation_PromotesFirstWaitlistedUserAndNotifies() {
        authenticateAsAdmin();
        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-cancel", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-cancel"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        enqueue("wait-1", BOB_EMAIL, slot.getDateTime(), LocalDateTime.now().minusMinutes(2));
        enqueue("wait-2", CAROL_EMAIL, slot.getDateTime(), LocalDateTime.now().minusMinutes(1));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-cancel");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isBooked());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(CAROL_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());

        ArgumentCaptor<Appointment> savedAppointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        ArgumentCaptor<Appointment> updatedAppointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(updatedAppointmentCaptor.capture());
        verify(appointmentBookingRepository).save(savedAppointmentCaptor.capture());
        assertEquals(AppointmentStatus.CANCELLED, updatedAppointmentCaptor.getValue().getStatus());
        assertEquals(AppointmentStatus.CONFIRMED, savedAppointmentCaptor.getValue().getStatus());
        assertEquals(BOB_EMAIL, savedAppointmentCaptor.getValue().getCustomerName());
        verify(eventManager).notifyObservers(contains("Reservation cancelled: apt-cancel"));
        verify(eventManager).notifyObservers(contains("Waitlist promotion confirmed:"));
        verify(appointmentNotificationCoordinator, times(1))
                .sendWaitlistPromotionNotification(savedAppointmentCaptor.getValue());
    }

    @Test
    void cancelAppointment_WhenNoWaitlistExists_SlotBecomesAvailableAndNoPromotionNotification() {
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
    void waitlistPromotionFifoOrder_UsesOldestEntryFirst_AndPreservesRemainingOrder() {
        authenticateAsAdmin();
        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-fifo", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-fifo"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        LocalDateTime baseTime = LocalDateTime.now();
        enqueue("wait-old", BOB_EMAIL, slot.getDateTime(), baseTime.minusMinutes(3));
        enqueue("wait-new", CAROL_EMAIL, slot.getDateTime(), baseTime.minusMinutes(1));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-fifo");

        assertEquals(BookingStatus.SUCCESS, result);
        ArgumentCaptor<Appointment> savedAppointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).save(savedAppointmentCaptor.capture());
        assertEquals(BOB_EMAIL, savedAppointmentCaptor.getValue().getCustomerName());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(CAROL_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());
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

        enqueue("wait-fail", BOB_EMAIL, slot.getDateTime(), LocalDateTime.now().minusMinutes(1));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-fail");

        assertEquals(BookingStatus.WAITLIST_PROMOTION_FAILED, result);
        assertTrue(slot.isAvailable());
        assertEquals(1, waitlistRepository.findAll().size());
        assertEquals(BOB_EMAIL, waitlistRepository.findAll().get(0).getCustomerEmail());
        verify(appointmentNotificationCoordinator, never()).sendWaitlistPromotionNotification(any(Appointment.class));
        verify(eventManager, never()).notifyObservers(contains("Waitlist promotion confirmed:"));
    }

    @Test
    void cancelAppointment_UpdateFails_RestoresOriginalSlotStateAndDoesNotPromote() {
        authenticateAsAdmin();
        AppointmentSlot slot = bookedSlot();
        Appointment cancelled = appointmentAt("apt-update-fail", slot.getDateTime());
        when(appointmentBookingRepository.findById(eq("apt-update-fail"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);
        enqueue("wait-admin", BOB_EMAIL, slot.getDateTime(), LocalDateTime.now().minusMinutes(1));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-update-fail");

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        assertTrue(slot.isBooked());
        assertEquals(1, waitlistRepository.findAll().size());
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_AlreadyCancelled_DoesNotPromote() {
        authenticateAsAdmin();
        Appointment alreadyCancelled = appointmentAt("apt-cancelled", LocalDateTime.now().plusHours(1))
                .withStatus(AppointmentStatus.CANCELLED);
        when(appointmentBookingRepository.findById(eq("apt-cancelled"))).thenReturn(Optional.of(alreadyCancelled));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-cancelled");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_CANCELLED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_PastAppointment_DoesNotPromote() {
        authenticateAsAdmin();
        Appointment pastAppointment = appointmentAt("apt-past", LocalDateTime.now().minusHours(1));
        when(appointmentBookingRepository.findById(eq("apt-past"))).thenReturn(Optional.of(pastAppointment));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-past");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_NotFoundAppointment_ReturnsNotFound() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById(anyString())).thenReturn(Optional.empty());

        BookingStatus result = appointmentBookingService.cancelAppointment("missing-id");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelAppointment_UnauthenticatedAdminFlow_ReturnsUnauthorized() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-test");

        assertEquals(BookingStatus.UNAUTHORIZED, result);
    }

    @Test
    void cancelAppointment_LoggedInButNotAdmin_ReturnsUnauthorized() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-test");

        assertEquals(BookingStatus.UNAUTHORIZED, result);
    }

    @Test
    void cancelAppointment_MultipleSlots_PromotesFromMatchingSlotOnly() {
        authenticateAsAdmin();
        LocalDateTime slotTime1 = LocalDateTime.now().plusHours(1);
        LocalDateTime slotTime2 = LocalDateTime.now().plusHours(2);
        AppointmentSlot slot1 = new AppointmentSlot(slotTime1.toLocalDate(), slotTime1.toLocalTime());
        AppointmentSlot slot2 = new AppointmentSlot(slotTime2.toLocalDate(), slotTime2.toLocalTime());
        slot1.book();
        slot2.book();

        Appointment cancelled = new Appointment("apt-multi", ALICE_EMAIL, slotTime1, 60, 1, AppointmentStatus.CONFIRMED);
        when(appointmentBookingRepository.findById(eq("apt-multi"))).thenReturn(Optional.of(cancelled));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot1, slot2));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);
        enqueue("wait-slot1", BOB_EMAIL, slotTime1, LocalDateTime.now().minusMinutes(2));
        enqueue("wait-slot2", CAROL_EMAIL, slotTime2, LocalDateTime.now().minusMinutes(1));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-multi");

        assertEquals(BookingStatus.SUCCESS, result);
        ArgumentCaptor<Appointment> promotedCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).save(promotedCaptor.capture());
        assertEquals(BOB_EMAIL, promotedCaptor.getValue().getCustomerName());
        assertEquals(slotTime1, promotedCaptor.getValue().getStartTime());
        assertEquals(1, waitlistRepository.findBySlotDateTime(slotTime2).size());
    }

    @Test
    void bookAppointment_FullSlot_DoesNotSendNotificationWhenWaitlisted() {
        AppointmentSlot slot = bookedSlot();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, SLOT_TIME, 60, 1);

        assertEquals(BookingStatus.WAITLISTED, result);
        verifyNoInteractions(appointmentNotificationCoordinator);
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

      private void enqueue(String id, String email, LocalDateTime slotDateTime, LocalDateTime createdAt) {
          waitlistRepository.save(new WaitlistEntry(
                  id,
                  email,
                  email,
                  null,
                  slotDateTime,
                  60,
                  1,
                  AppointmentType.NORMAL,
                  createdAt
          ));
      }
}

