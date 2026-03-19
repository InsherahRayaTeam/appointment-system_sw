package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;

import java.util.Objects;

/**
 * Handles booking workflow validation and persistence for customer appointments.
 */
public class AppointmentBookingService {

    private static final int MAX_DURATION_MINUTES = 120;
    private static final int MAX_PARTICIPANT_COUNT = 5;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;

    /**
     * Creates a booking service using repository dependencies.
     *
     * @param appointmentRepository repository used to read and update slot availability
     * @param appointmentBookingRepository repository used to store confirmed appointments
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository
    ) {
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.appointmentBookingRepository = Objects.requireNonNull(
                appointmentBookingRepository,
                "appointmentBookingRepository cannot be null"
        );
    }

    /**
     * Attempts to book an appointment using raw string input values.
     * This keeps request validation and conversion in the service layer.
     *
     * @param customerName the customer name for the booking
     * @param slotTime the requested slot time label
     * @param durationMinutesInput the requested duration input in minutes
     * @param participantCountInput the requested participant count input
     * @return the booking outcome status
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Integer durationMinutes = parseInteger(durationMinutesInput);
        if (durationMinutes == null) {
            return BookingStatus.INVALID_DURATION;
        }

        Integer participantCount = parseInteger(participantCountInput);
        if (participantCount == null) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }

        return bookAppointment(customerName, slotTime, durationMinutes, participantCount);
    }

    /**
     * Attempts to book an appointment using the provided request details.
     * The service validates request constraints, checks slot availability,
     * books the slot, and persists a confirmed appointment on success.
     *
     * @param customerName the customer name for the booking
     * @param slotTime the requested slot time label
     * @param durationMinutes the requested duration in minutes
     * @param participantCount the number of participants for the booking
     * @return the booking outcome status
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            int durationMinutes,
            int participantCount
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            return BookingStatus.INVALID_DURATION;
        }
        if (participantCount > MAX_PARTICIPANT_COUNT) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }

        String normalizedCustomerName = customerName.trim();
        String normalizedSlotTime = slotTime.trim();
        boolean matchingSlotFound = false;

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.getTime().equals(normalizedSlotTime)) {
                if (!slot.isAvailable()) {
                    matchingSlotFound = true;
                    continue;
                }

                slot.book();
                appointmentBookingRepository.save(new Appointment(
                        normalizedCustomerName,
                        normalizedSlotTime,
                        durationMinutes,
                        participantCount,
                        AppointmentStatus.CONFIRMED
                ));
                return BookingStatus.SUCCESS;
            }
        }

        if (matchingSlotFound) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        return BookingStatus.SLOT_NOT_FOUND;
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

