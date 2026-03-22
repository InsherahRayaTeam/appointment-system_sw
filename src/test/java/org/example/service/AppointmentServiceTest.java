package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService();
    }

    @Test
    void shouldReturnAllAvailableSlotsInitially() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();

        assertEquals(3, slots.size());
    }

    @Test
    void shouldBookSlotWhenTimeExists() {
        boolean result = appointmentService.bookSlot("10:00");

        assertTrue(result);
    }

    @Test
    void shouldBookSlotWhenTimeContainsWhitespace() {
        boolean result = appointmentService.bookSlot(" 10:00 ");

        assertTrue(result);
    }

    @Test
    void shouldNotBookSlotTwiceForSameTime() {
        appointmentService.bookSlot("10:00");
        boolean result = appointmentService.bookSlot("10:00");

        assertFalse(result);
    }

    @Test
    void shouldNotBookSlotWhenTimeDoesNotExist() {
        boolean result = appointmentService.bookSlot("15:00");

        assertFalse(result);
    }

    @Test
    void shouldNotBookSlotWhenTimeIsNull() {
        boolean result = appointmentService.bookSlot(null);

        assertFalse(result);
    }

    @Test
    void shouldNotBookSlotWhenTimeIsBlank() {
        boolean result = appointmentService.bookSlot("   ");

        assertFalse(result);
    }

    @Test
    void shouldDecreaseAvailableSlotsAfterBookingSlot() {
        appointmentService.bookSlot("10:00");

        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(2, slots.size());
    }

    @Test
    void shouldBookAppointmentWhenInputIsValid() {
        Appointment appointment = appointmentAt(10, 0, 60, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldRejectAppointmentWhenDurationExceedsLimit() {
        Appointment appointment = appointmentAt(10, 0, 121, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldBookAppointmentWhenDurationIsAtLimit() {
        Appointment appointment = appointmentAt(10, 0, 120, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldRejectAppointmentWhenParticipantsExceedLimit() {
        Appointment appointment = appointmentAt(11, 0, 60, 6);

        boolean result = appointmentService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldBookAppointmentWhenParticipantsAreAtLimit() {
        Appointment appointment = appointmentAt(11, 0, 60, 5);

        boolean result = appointmentService.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldRejectBookingWhenSlotAlreadyReserved() {
        appointmentService.bookSlot("12:00");
        Appointment appointment = appointmentAt(12, 0, 60, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectAppointmentWhenInputIsNull() {
        boolean result = appointmentService.bookAppointment(null);

        assertFalse(result);
    }

    @Test
    void shouldRejectAppointmentWhenStartTimeIsNull() {
        Appointment appointment = new Appointment("apt-null-time", null, 60, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectAppointmentWhenNoMatchingTimeSlotExists() {
        Appointment appointment = appointmentAt(15, 0, 60, 3);

        boolean result = appointmentService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldBookAppointmentWhenRulesAllowBoundaryValues() {
        Appointment appointment = appointmentAt(10, 0, 120, 5);

        boolean result = appointmentService.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldDecreaseAvailableSlotsAfterBookingAppointment() {
        int slotsBeforeBooking = appointmentService.getAvailableSlots().size();
        Appointment appointment = appointmentAt(11, 0, 60, 2);

        appointmentService.bookAppointment(appointment);

        int slotsAfterBooking = appointmentService.getAvailableSlots().size();
        assertEquals(slotsBeforeBooking - 1, slotsAfterBooking);
    }

    @Test
    void shouldAllowBookingWhenRulesListIsNull() {
        AppointmentService serviceWithNullRules = new AppointmentService(null);
        Appointment appointment = appointmentAt(10, 0, 999, 99);

        boolean result = serviceWithNullRules.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowBookingWhenRulesListIsEmpty() {
        AppointmentService serviceWithNoRules = new AppointmentService(List.of());
        Appointment appointment = appointmentAt(10, 0, 999, 99);

        boolean result = serviceWithNoRules.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldUseInjectedRulesForValidation() {
        AppointmentService customRuleService = new AppointmentService(List.of(
                appointment -> true,
                appointment -> false
        ));
        Appointment appointment = appointmentAt(11, 0, 60, 2);

        boolean result = customRuleService.bookAppointment(appointment);

        assertFalse(result);
    }

    @Test
    void shouldAllowLargeValuesWhenInjectedRulesAllow() {
        AppointmentService customRuleService = new AppointmentService(List.of(
                appointment -> true,
                appointment -> true
        ));
        Appointment appointment = appointmentAt(10, 0, 999, 99);

        boolean result = customRuleService.bookAppointment(appointment);

        assertTrue(result);
    }

    @Test
    void shouldNotChangeAvailableSlotsWhenRuleValidationFails() {
        int slotsBeforeBooking = appointmentService.getAvailableSlots().size();
        Appointment appointment = appointmentAt(10, 0, 121, 2);

        boolean result = appointmentService.bookAppointment(appointment);
        int slotsAfterBooking = appointmentService.getAvailableSlots().size();

        assertFalse(result);
        assertEquals(slotsBeforeBooking, slotsAfterBooking);
    }

    private Appointment appointmentAt(int hour, int minute, int duration, int participants) {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, hour, minute);
        return new Appointment("apt-" + hour + minute, startTime, duration, participants);
    }
}