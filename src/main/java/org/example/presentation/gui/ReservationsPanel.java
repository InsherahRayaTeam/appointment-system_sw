package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.SystemUser;
import org.example.service.AppointmentBookingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        actionPanel.add(refreshButton);
        add(actionPanel, BorderLayout.SOUTH);

        refreshData();
    }

    /**
     * Reloads reservation data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);

        List<Appointment> reservations = appointmentBookingService.getReservationsForCustomer(user.getEmail());
        int count = 0;
        for (Appointment appointment : reservations) {
            tableModel.addRow(toRow(appointment));
            count++;
        }

        if (count == 0) {
            infoLabel.setText("No reservations found for " + user.getEmail());
        } else {
            infoLabel.setText("Reservations for " + user.getEmail() + ": " + count + " record(s)");
        }
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

