package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Allows administrators to view, modify, and cancel reservations.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminReservationsPanel extends JPanel {

    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentService appointmentService;
    private final DefaultTableModel tableModel;
    private final JTable reservationsTable;
    private final JComboBox<String> slotComboBox;
    private final JLabel infoLabel;
    private final JButton modifyButton;
    private final JButton cancelButton;

    /**
     * Creates an admin reservations management panel.
     *
     * @param appointmentBookingService service used to load/manage reservations
     * @param appointmentService service used to load available replacement slots
     */
    public AdminReservationsPanel(
            AppointmentBookingService appointmentBookingService,
            AppointmentService appointmentService
    ) {
        this.appointmentBookingService = appointmentBookingService;
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel("Manage reservations (future reservations only for modify/cancel).");
        add(infoLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] {"Reservation ID", "Customer", "Slot", "Duration", "Participants", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        slotComboBox = new JComboBox<>();
        JButton refreshButton = new JButton("Refresh");
        modifyButton = new JButton("Modify Slot");
        cancelButton = new JButton("Cancel Reservation");

        refreshButton.addActionListener(e -> refreshData());
        modifyButton.addActionListener(e -> onModifyReservation());
        cancelButton.addActionListener(e -> onCancelReservation());

        actionPanel.add(new JLabel("New Slot:"));
        actionPanel.add(slotComboBox);
        actionPanel.add(refreshButton);
        actionPanel.add(modifyButton);
        actionPanel.add(cancelButton);

        add(actionPanel, BorderLayout.SOUTH);

        refreshData();
    }

    /**
     * Reloads reservation rows and available slots.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);
        slotComboBox.removeAllItems();

        if (!appointmentBookingService.canCurrentUserManageReservations()) {
            infoLabel.setText("Only admins can manage reservations.");
            setActionsEnabled(false);
            return;
        }

        List<Appointment> reservations = appointmentBookingService.getManagedReservations();
        for (Appointment appointment : reservations) {
            tableModel.addRow(toRow(appointment));
        }

        for (AppointmentSlot slot : appointmentService.getAvailableSlots()) {
            slotComboBox.addItem(slot.getTime());
        }

        infoLabel.setText("Manage reservations (future reservations only for modify/cancel).");
        setActionsEnabled(true);
    }

    private void onCancelReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning("Please select a reservation first.");
            return;
        }

        BookingStatus status = appointmentBookingService.cancelAppointment(reservationId);
        showResult(status, "Cancel Reservation");

        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    private void onModifyReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning("Please select a reservation first.");
            return;
        }
        if (slotComboBox.getSelectedItem() == null) {
            showWarning("Please select a replacement slot.");
            return;
        }

        String newSlotTime = slotComboBox.getSelectedItem().toString();
        BookingStatus status = appointmentBookingService.modifyAppointment(reservationId, newSlotTime);
        showResult(status, "Modify Reservation");

        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    private String selectedReservationId() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        Object value = tableModel.getValueAt(selectedRow, 0);
        return value == null ? null : value.toString();
    }

    private Object[] toRow(Appointment appointment) {
        return new Object[] {
                appointment.getId(),
                appointment.getCustomerName(),
                appointment.getSlotTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getStatus()
        };
    }

    private void showResult(BookingStatus status, String title) {
        int messageType = status == BookingStatus.SUCCESS
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;

        JOptionPane.showMessageDialog(this, GuiMessageHelper.toMessage(status), title, messageType);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Required", JOptionPane.WARNING_MESSAGE);
    }

    private void setActionsEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        modifyButton.setEnabled(enabled && slotComboBox.getItemCount() > 0);
        slotComboBox.setEnabled(enabled && slotComboBox.getItemCount() > 0);
    }
}

