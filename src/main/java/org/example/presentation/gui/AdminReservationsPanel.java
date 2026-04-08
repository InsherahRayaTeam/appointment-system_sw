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
 * Represents admin reservations panel in the system.
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
     * Creates a new admin reservations panel object with the given values.
     *
     * @param appointmentBookingService service used to run business logic
     * @param appointmentService service used to run business logic
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
     * Reloads and updates data.
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

    /**
     * Runs on cancel reservation for this class.
     */
    private void onCancelReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning("Please select a reservation first.");
            return;
        }

        BookingStatus status = appointmentBookingService.cancelAppointment(reservationId);
        showResult(status, "Cancel Reservation");

        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.removeRow(selectedRow);
            }
        }
    }

    /**
     * Runs on modify reservation for this class.
     */
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

        int selectedRow = reservationsTable.getSelectedRow();
        String newSlotTime = slotComboBox.getSelectedItem().toString();
        BookingStatus status = appointmentBookingService.modifyAppointment(reservationId, newSlotTime);
        showResult(status, "Modify Reservation");

        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.setValueAt(newSlotTime, selectedRow, 2);
                tableModel.setValueAt("MODIFIED", selectedRow, 5);
            }
        }
    }

    /**
     * Runs selected reservation id for this class.
     *
     * @return text result from this method
     */
    private String selectedReservationId() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        Object value = tableModel.getValueAt(selectedRow, 0);
        return value == null ? null : value.toString();
    }

    /**
     * Runs to row for this class.
     *
     * @param appointment value for appointment
     *
     * @return result produced by this method
     */
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

    /**
     * Shows result to the user.
     *
     * @param status status value used for this operation
     * @param title title text for the dialog or view
     */
    private void showResult(BookingStatus status, String title) {
        int messageType = status == BookingStatus.SUCCESS
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;

        JOptionPane.showMessageDialog(this, GuiMessageHelper.toMessage(status), title, messageType);
    }

    /**
     * Shows warning to the user.
     *
     * @param message message text to show or send
     */
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Required", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Updates the actions enabled.
     *
     * @param enabled flag that tells if this feature is enabled
     */
    private void setActionsEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        modifyButton.setEnabled(enabled && slotComboBox.getItemCount() > 0);
        slotComboBox.setEnabled(enabled && slotComboBox.getItemCount() > 0);
    }
}

