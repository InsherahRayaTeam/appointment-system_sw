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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Represents reservations panel in the system.
 */
public class ReservationsPanel extends JPanel {

    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String CANCEL_RESERVATION_TITLE = "Cancel Reservation";
    private static final String PLEASE_SELECT_RESERVATION_FIRST = "Please select a reservation first.";
    private static final String INPUT_REQUIRED_TITLE = "Input Required";

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
                new Object[] {"Reservation ID", "Customer", "Date", "Day", "Time", "Duration", "Participants", "Status"},
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
        JButton refreshButton = new JButton(REFRESH_BUTTON_LABEL);
        JButton cancelButton = new JButton(CANCEL_RESERVATION_TITLE);

        refreshButton.addActionListener(e -> refreshData());
        cancelButton.addActionListener(e -> onCancelReservation());

        actionPanel.add(refreshButton);
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
            showSelectionWarning();
            return;
        }

        BookingStatus status = appointmentBookingService.cancelOwnAppointment(reservationId);
        showCancelResult(status);
        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.removeRow(selectedRow);
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
     */
    private void showCancelResult(BookingStatus status) {
        int messageType = status == BookingStatus.SUCCESS
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, GuiMessageHelper.toMessage(status), CANCEL_RESERVATION_TITLE, messageType);
    }

    /**
     * Shows warning to the user.
     */
    private void showSelectionWarning() {
        JOptionPane.showMessageDialog(
                this,
                PLEASE_SELECT_RESERVATION_FIRST,
                INPUT_REQUIRED_TITLE,
                JOptionPane.WARNING_MESSAGE
        );
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
                appointment.getSlotDate(),
                appointment.getSlotDay(),
                appointment.getSlotTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getStatus()
        };
    }
}

