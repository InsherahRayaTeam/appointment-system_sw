package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.SystemUser;
import org.example.notification.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Coordinates appointment notifications for business events.
 */
public class AppointmentNotificationCoordinator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd (EEEE) HH:mm",
            Locale.ENGLISH
    );

    private final NotificationService notificationService;

    /**
     * Creates a new appointment notification coordinator object with the given values.
     *
     * @param notificationService service used to send notifications
     */
    public AppointmentNotificationCoordinator(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
    }

    /**
     * Sends a pending notification for a newly booked appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendPendingNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Request Received";
        String body = "Your appointment request is under review and is being processed."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends an approved notification for an approved appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendApprovedNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Approved";
        String body = "Your appointment has been approved."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends a cancelled notification for a cancelled or rejected appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendCancelledNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Cancelled";
        String body = "Your appointment has been cancelled/rejected."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends a modified notification for an updated appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendModifiedNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Updated";
        String body = "Your appointment has been updated."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends a detailed reschedule notification with old/new appointment details.
     *
     * @param previousAppointment appointment before rescheduling
     * @param updatedAppointment appointment after rescheduling
     */
    public void sendRescheduledNotification(Appointment previousAppointment, Appointment updatedAppointment) {
        SystemUser user = requireUser(updatedAppointment);
        String oldDateTime = formatAppointmentDateTime(previousAppointment);
        String newDateTime = formatAppointmentDateTime(updatedAppointment);
        String appointmentId = updatedAppointment.getId() == null ? "N/A" : updatedAppointment.getId();

        String subject = "Appointment Rescheduled";
        String body = "Your appointment schedule has been changed by the administrator."
                + " Appointment ID: " + appointmentId
                + " User: " + displayBookedUserName(updatedAppointment)
                + " Old date/time: " + oldDateTime
                + " New date/day/time: " + newDateTime
                + " If the new schedule does not suit you, you may cancel this reservation and book another slot.";

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends a notification for an attended appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendAttendedNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Marked as Attended";
        String body = "The appointment has been marked as attended."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    /**
     * Sends a notification for a completed appointment.
     *
     * @param appointment appointment involved in this action
     */
    public void sendCompletedNotification(Appointment appointment) {
        SystemUser user = requireUser(appointment);
        String appointmentDateTime = formatAppointmentDateTime(appointment);
        String subject = "Appointment Completed";
        String body = "The appointment has been completed."
                + " Booked for: " + displayBookedUserName(appointment)
                + " Appointment date/time: " + appointmentDateTime;

        notificationService.send(user.getEmail(), subject, body);
    }

    private SystemUser requireUser(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        SystemUser user = appointment.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Appointment user cannot be null");
        }

        return user;
    }

    private String formatAppointmentDateTime(Appointment appointment) {
        LocalDateTime startTime = appointment.getStartTime();
        return startTime == null ? "N/A" : startTime.format(DATE_TIME_FORMATTER);
    }

    private String displayBookedUserName(Appointment appointment) {
        if (appointment == null) {
            return "N/A";
        }

        String bookedName = appointment.getCustomerName();
        return bookedName == null || bookedName.trim().isEmpty() ? "N/A" : bookedName;
    }
}

