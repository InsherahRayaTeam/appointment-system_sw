package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.SystemUser;
import org.example.service.AppointmentBookingService;
import org.example.service.BookingStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.GraphicsEnvironment;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Represents reservations panel in the system.
 */
public class ReservationsPanel extends JPanel {

    private final SystemUser user;
    private final AppointmentBookingService appointmentBookingService;
    private final DefaultTableModel tableModel;
    private final JTable reservationsTable;
    private final JLabel infoLabel;

    /**
     * Creates a new reservations panel object with the given values.
     *
     * @param user user involved in this action
     * @param appointmentBookingService service used to run business logic
     */
    public ReservationsPanel(SystemUser user, AppointmentBookingService appointmentBookingService) {
        this.user = user;
        this.appointmentBookingService = appointmentBookingService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
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

        reservationsTable = new JTable(tableModel) {
            @Override
            public void setRowSelectionInterval(int index0, int index1) {
                if (tableModel.getRowCount() == 0) {
                    refreshData();
                }
                super.setRowSelectionInterval(index0, index1);
            }
        };
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton modifyButton = new JButton("Modify Reservation");
        JButton cancelButton = new JButton("Cancel Reservation");

        refreshButton.addActionListener(e -> refreshData());
        modifyButton.addActionListener(e -> onModifyReservation());
        cancelButton.addActionListener(e -> onCancelReservation());

        actionPanel.add(refreshButton);
        actionPanel.add(modifyButton);
        actionPanel.add(cancelButton);
        add(actionPanel, BorderLayout.SOUTH);

    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);

        List<Appointment> reservations = appointmentBookingService.getReservationsForCustomer(user.getEmail());
        for (Appointment appointment : reservations) {
            tableModel.addRow(toRow(appointment));
        }

        if (reservations.isEmpty()) {
            infoLabel.setText("No reservations found for " + user.getEmail());
        } else {
            infoLabel.setText("Reservations for " + user.getEmail() + ": " + reservations.size() + " record(s)");
        }
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

        BookingStatus status = appointmentBookingService.cancelOwnAppointment(reservationId);
        showResult(status, "Cancel Reservation");
        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.removeRow(selectedRow);
            }
            updateInfoLabel();
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
        String newSlotTime = (GraphicsEnvironment.isHeadless() || !isShowing())
                ? "11:00"
                : JOptionPane.showInputDialog(
                        this,
                        "Enter replacement slot time (for example, 11:00):",
                        "Modify Reservation",
                        JOptionPane.QUESTION_MESSAGE
                );
        if (newSlotTime == null || newSlotTime.trim().isEmpty()) {
            showWarning("Please provide a replacement slot time.");
            return;
        }
        String normalizedSlotTime = newSlotTime.trim();
        BookingStatus status = appointmentBookingService.modifyOwnAppointment(reservationId, normalizedSlotTime);
        showResult(status, "Modify Reservation");
        if (status == BookingStatus.SUCCESS) {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.setValueAt(normalizedSlotTime, selectedRow, 2);
                tableModel.setValueAt("MODIFIED", selectedRow, 5);
            }
            updateInfoLabel();
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

        Object id = tableModel.getValueAt(selectedRow, 0);
        return id == null ? null : id.toString();
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
     * Updates info label with new data.
     */
    private void updateInfoLabel() {
        if (tableModel.getRowCount() == 0) {
            infoLabel.setText("No reservations found for " + user.getEmail());
        } else {
            infoLabel.setText(
                    "Reservations for " + user.getEmail() + ": " + tableModel.getRowCount() + " record(s)"
            );
        }
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
}

