package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying available appointment slots.
 *
 * Queries AppointmentService (service layer) and renders slots in a table.
 */
public class SlotsPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final Runnable onBack;

    private DefaultTableModel tableModel;
    private JTable slotsTable;
    private JLabel emptyStateLabel;

    /**
     * Creates slots panel with appointment service dependency.
     *
     * @param appointmentService appointment service (service layer)
     * @param onBack callback to return to menu
     */
    public SlotsPanel(AppointmentService appointmentService, Runnable onBack) {
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

        JLabel titleLabel = new JLabel("Available Appointment Slots");
        titleLabel.setFont(UiStyle.FONT_SUBTITLE);
        top.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = UiStyle.createSecondaryButton("Refresh", e -> refresh());
        top.add(refreshButton, BorderLayout.EAST);

        JPanel card = UiStyle.createCardPanel(new BorderLayout(UiStyle.SPACE_XS, UiStyle.SPACE_XS));

        tableModel = new DefaultTableModel(new Object[]{"Time", "Duration", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        slotsTable = new JTable(tableModel);
        slotsTable.setFont(UiStyle.FONT_BODY);
        slotsTable.setRowHeight(24);
        slotsTable.getTableHeader().setFont(UiStyle.FONT_LABEL);
        slotsTable.setFillsViewportHeight(true);
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        slotsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(slotsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        emptyStateLabel = new JLabel("No available slots", SwingConstants.CENTER);
        emptyStateLabel.setFont(UiStyle.FONT_BODY);
        emptyStateLabel.setForeground(Color.DARK_GRAY);

        card.add(scrollPane, BorderLayout.CENTER);
        card.add(emptyStateLabel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);

        JButton backButton = UiStyle.createSecondaryButton("Back to Dashboard", e -> onBack.run());
        bottom.add(backButton);

        add(top, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the table with current available slots.
     */
    public void refresh() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();

        tableModel.setRowCount(0);
        for (AppointmentSlot slot : slots) {
            // Duration is not currently exposed by the domain model.
            tableModel.addRow(new Object[]{
                    slot.getTime(),
                    "N/A",
                    slot.isAvailable() ? "Available" : "Booked"
            });
        }

        boolean hasData = !slots.isEmpty();
        slotsTable.setEnabled(hasData);
        emptyStateLabel.setVisible(!hasData);
    }
}
