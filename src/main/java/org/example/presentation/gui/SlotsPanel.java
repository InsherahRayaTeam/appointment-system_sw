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

    private static final String AVAILABLE_SLOTS_NOTE = "Available slots only (booked slots are not selectable).";
    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String AVAILABLE_STATUS = "Available";
    private static final String NO_AVAILABLE_SLOTS = "No available slots";

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

        JLabel noteLabel = new JLabel(AVAILABLE_SLOTS_NOTE);
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
        JButton refreshButton = new JButton(REFRESH_BUTTON_LABEL);
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
            tableModel.addRow(new Object[] {slot.getDate(), slot.getDay(), slot.getTime(), AVAILABLE_STATUS});
        }

        if (availableSlots.isEmpty()) {
            tableModel.addRow(new Object[] {"-", "-", "-", NO_AVAILABLE_SLOTS});
        }
    }
}

