package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for administrator reservation management operations.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ReservationsPanel extends JPanel {

    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentService appointmentService;
    private final Runnable onBack;

    private final List<Appointment> currentReservations = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable reservationsTable;
    private JLabel statusLabel;
    private JButton modifyButton;
    private JButton cancelButton;

    /**
     * Creates reservations management panel.
     *
     * @param appointmentBookingService booking service used for management actions
     * @param appointmentService appointment service used for reading available slots
     * @param onBack callback to return to dashboard menu
     */
    public ReservationsPanel(
            AppointmentBookingService appointmentBookingService,
            AppointmentService appointmentService,
            Runnable onBack
    ) {
        this.appointmentBookingService = appointmentBookingService;
        this.appointmentService = appointmentService;
        this.onBack = onBack;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(UiStyle.SPACE_SM, UiStyle.SPACE_SM));
        setBackground(UiStyle.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLabel = new JLabel("Manage Reservations");
        titleLabel.setFont(UiStyle.FONT_SUBTITLE);
        top.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = UiStyle.createSecondaryButton("Refresh", e -> refresh());
        top.add(refreshButton, BorderLayout.EAST);

        JPanel card = UiStyle.createCardPanel(new BorderLayout(UiStyle.SPACE_XS, UiStyle.SPACE_XS));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Customer", "Time", "Duration", "Participants", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setFont(UiStyle.FONT_BODY);
        reservationsTable.setRowHeight(24);
        reservationsTable.getTableHeader().setFont(UiStyle.FONT_LABEL);
        reservationsTable.getTableHeader().setReorderingAllowed(false);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        card.add(scrollPane, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyle.SPACE_XS, 0));
        actionRow.setOpaque(false);

        modifyButton = UiStyle.createPrimaryButton("Modify Reservation", e -> onModifyReservation());
        cancelButton = UiStyle.createSecondaryButton("Cancel Reservation", e -> onCancelReservation());
        JButton backButton = UiStyle.createSecondaryButton("Back to Dashboard", e -> onBack.run());

        actionRow.add(modifyButton);
        actionRow.add(cancelButton);
        actionRow.add(backButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UiStyle.FONT_BODY);
        statusLabel.setForeground(UiStyle.COLOR_INFO);

        add(top, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        add(actionRow, BorderLayout.SOUTH);
        card.add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Reloads reservation table content for the current admin session.
     */
    public void refresh() {
        tableModel.setRowCount(0);
        currentReservations.clear();

        if (!appointmentBookingService.canCurrentUserManageReservations()) {
            setStatus("Only authenticated administrators can manage reservations.", UiStyle.COLOR_ERROR);
            setActionButtonsEnabled(false);
            return;
        }

        List<Appointment> reservations = appointmentBookingService.getManagedReservations();
        currentReservations.addAll(reservations);

        for (Appointment appointment : reservations) {
            tableModel.addRow(new Object[]{
                    appointment.getId(),
                    appointment.getCustomerName(),
                    appointment.getSlotTime(),
                    appointment.getDurationMinutes(),
                    appointment.getParticipantCount(),
                    appointment.getStatusValue()
            });
        }

        if (reservations.isEmpty()) {
            setStatus("No reservations available.", UiStyle.COLOR_INFO);
        } else {
            setStatus("Select a reservation to modify or cancel.", UiStyle.COLOR_INFO);
        }

        setActionButtonsEnabled(!reservations.isEmpty());
    }

    private void onCancelReservation() {
        Appointment selected = getSelectedReservation();
        if (selected == null) {
            showError("Please select a reservation first.");
            return;
        }

        BookingStatus status = appointmentBookingService.cancelAppointment(selected.getId());
        if (status == BookingStatus.SUCCESS) {
            JOptionPane.showMessageDialog(
                    this,
                    "Reservation cancelled successfully.",
                    "Reservation Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refresh();
            return;
        }

        showError(messageFor(status));
    }

    private void onModifyReservation() {
        Appointment selected = getSelectedReservation();
        if (selected == null) {
            showError("Please select a reservation first.");
            return;
        }

        List<String> availableSlots = new ArrayList<>();
        appointmentService.getAvailableSlots().forEach(slot -> availableSlots.add(slot.getTime()));

        if (availableSlots.isEmpty()) {
            showError("No available target slot found for modification.");
            return;
        }

        Object selectedSlot = JOptionPane.showInputDialog(
                this,
                "Select new slot time:",
                "Modify Reservation",
                JOptionPane.QUESTION_MESSAGE,
                null,
                availableSlots.toArray(new String[0]),
                availableSlots.get(0)
        );

        if (selectedSlot == null) {
            return;
        }

        BookingStatus status = appointmentBookingService.modifyAppointment(selected.getId(), selectedSlot.toString());
        if (status == BookingStatus.SUCCESS) {
            JOptionPane.showMessageDialog(
                    this,
                    "Reservation modified successfully.",
                    "Reservation Modified",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refresh();
            return;
        }

        showError(messageFor(status));
    }

    private Appointment getSelectedReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReservations.size()) {
            return null;
        }
        return currentReservations.get(selectedRow);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        modifyButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }

    private void showError(String message) {
        setStatus(message, UiStyle.COLOR_ERROR);
        JOptionPane.showMessageDialog(this, message, "Reservation Management", JOptionPane.ERROR_MESSAGE);
    }

    private String messageFor(BookingStatus status) {
        if (status == null) {
            return "Operation failed due to an unknown error.";
        }

        switch (status) {
            case UNAUTHORIZED:
                return "Only authenticated administrators can manage reservations.";
            case APPOINTMENT_NOT_FOUND:
                return "Selected reservation no longer exists.";
            case APPOINTMENT_NOT_FUTURE:
                return "Only future reservations can be modified or cancelled.";
            case APPOINTMENT_ALREADY_CANCELLED:
                return "This reservation has already been cancelled.";
            case SLOT_NOT_FOUND:
                return "Selected target slot does not exist.";
            case SLOT_ALREADY_BOOKED:
                return "Selected target slot is unavailable.";
            case BLANK_SLOT_TIME:
                return "A new slot selection is required.";
            case UPDATE_FAILED:
                return "Reservation update failed. Please refresh and retry.";
            default:
                return "Operation failed. Please refresh and retry.";
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message == null || message.trim().isEmpty() ? " " : message);
        statusLabel.setForeground(color);
    }
}

