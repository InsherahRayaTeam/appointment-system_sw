package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Represents slots panel in the system.
 */
public class SlotsPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final DefaultTableModel tableModel;

    /**
     * Creates a new slots panel object with the given values.
     *
     * @param appointmentService service used to run business logic
     */
    public SlotsPanel(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel noteLabel = new JLabel("Available slots only (booked slots are not selectable).");
        add(noteLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[] {"Date", "Day", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable slotsTable = new JTable(tableModel);
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(slotsTable), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        footerPanel.add(refreshButton);
        add(footerPanel, BorderLayout.SOUTH);

        refreshData();
    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);

        List<AppointmentSlot> availableSlots = appointmentService.getAvailableSlots();
        for (AppointmentSlot slot : availableSlots) {
            tableModel.addRow(new Object[] {slot.getDate(), slot.getDay(), slot.getTime(), "Available"});
        }

        if (availableSlots.isEmpty()) {
            tableModel.addRow(new Object[] {"-", "-", "-", "No available slots"});
        }
    }
}

