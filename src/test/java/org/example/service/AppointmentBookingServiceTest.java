package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppointmentBookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    private AppointmentBookingService appointmentBookingService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void bookAppointment_ValidRequest_ReturnsSuccessAndSavesAppointment() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 3);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        assertEquals(BookingStatus.SUCCESS, result);
        assertFalse(slot.isAvailable());
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());

        Appointment appointment = appointmentCaptor.getValue();
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals("10:00", appointment.getSlotTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(3, appointment.getParticipantCount());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_ValidRequest_SetsStatusToConfirmed() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 2);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        assertEquals(BookingStatus.SUCCESS, result);
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());
        assertEquals(AppointmentStatus.CONFIRMED, appointmentCaptor.getValue().getStatus());
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_BlankCustomerName_ReturnsBlankCustomerName() {
        BookingStatus result = appointmentBookingService.bookAppointment("   ", "10:00", 60, 2);

        assertEquals(BookingStatus.BLANK_CUSTOMER_NAME, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_BlankSlotTime_ReturnsBlankSlotTime() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "  ", 60, 2);

        assertEquals(BookingStatus.BLANK_SLOT_TIME, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_DurationAbove120_ReturnsInvalidDuration() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 121, 2);

        assertEquals(BookingStatus.INVALID_DURATION, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_ParticipantCountAbove5_ReturnsInvalidParticipantCount() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 6);

        assertEquals(BookingStatus.INVALID_PARTICIPANT_COUNT, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_NonExistentSlot_ReturnsSlotNotFound() {
        AppointmentSlot slot = new AppointmentSlot("11:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 1);

        assertEquals(BookingStatus.SLOT_NOT_FOUND, result);
        assertTrue(slot.isAvailable());
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_AlreadyBookedSlot_ReturnsSlotAlreadyBooked() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 1);

        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, result);
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_StringInput_ValidNumericValues_ReturnsSuccessAndSavesAppointment() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", "60", "2");

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        assertEquals(BookingStatus.SUCCESS, result);
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());

        Appointment appointment = appointmentCaptor.getValue();
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals("10:00", appointment.getSlotTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(2, appointment.getParticipantCount());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_StringInput_BlankCustomerName_ReturnsBlankCustomerNameAndDoesNotSave() {
        BookingStatus result = appointmentBookingService.bookAppointment("   ", "10:00", "60", "2");

        assertEquals(BookingStatus.BLANK_CUSTOMER_NAME, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_StringInput_BlankSlotTime_ReturnsBlankSlotTimeAndDoesNotSave() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "   ", "60", "2");

        assertEquals(BookingStatus.BLANK_SLOT_TIME, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_StringInput_NonNumericValues_ReturnsInvalidDurationAndDoesNotSave() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", "sixty", "two");

        assertEquals(BookingStatus.INVALID_DURATION, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_StringInput_InvalidParticipantCount_ReturnsInvalidParticipantCountAndDoesNotSave() {
        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", "60", "two");

        assertEquals(BookingStatus.INVALID_PARTICIPANT_COUNT, result);
        verifyNoInteractions(appointmentRepository);
        verify(appointmentBookingRepository, never()).save(any(Appointment.class));
        verifyNoMoreInteractions(appointmentBookingRepository);
    }

}

