package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.SystemUser;
import org.example.notification.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Coordinates appointment notifications for business events.
 */
public class AppointmentNotificationCoordinator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
}

