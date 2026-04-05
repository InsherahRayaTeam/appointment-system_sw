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
 * Displays reservations for the current user when backend support is available.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ReservationsPanel extends JPanel {

    private final SystemUser user;
    private final AppointmentBookingService appointmentBookingService;
    private final DefaultTableModel tableModel;
    private final JTable reservationsTable;
    private final JLabel infoLabel;

    /**
     * Creates a user reservations panel.
     *
     * @param user current authenticated user
     * @param appointmentBookingService service used to load reservations
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

        reservationsTable = new JTable(tableModel);
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

        refreshData();
    }

    /**
     * Reloads reservation data.
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

    private void onCancelReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning("Please select a reservation first.");
            return;
        }

        BookingStatus status = appointmentBookingService.cancelOwnAppointment(reservationId);
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
        String newSlotTime = JOptionPane.showInputDialog(
                this,
                "Enter replacement slot time (for example, 11:00):",
                "Modify Reservation",
                JOptionPane.QUESTION_MESSAGE
        );
        if (newSlotTime == null || newSlotTime.trim().isEmpty()) {
            showWarning("Please provide a replacement slot time.");
            return;
        }
        BookingStatus status = appointmentBookingService.modifyOwnAppointment(reservationId, newSlotTime);
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

        Object id = tableModel.getValueAt(selectedRow, 0);
        return id == null ? null : id.toString();
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

