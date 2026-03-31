package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentBookingService;
import org.example.service.BookingStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Admin-only panel for managing appointment slots.
 *
 * Allows administrators to add new slots and cancel existing slots.
 * Authorization is enforced at the service layer.
 *
 * @author appointment-system
 * @version 1.0
 */
public class SlotManagementPanel extends JPanel {

    private final AppointmentBookingService appointmentBookingService;
    private final Runnable onBack;

    private JTextField slotTimeField;
    private JButton addButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    private JList<String> slotsListComponent;
    private DefaultListModel<String> slotsListModel;

    /**
     * Creates slot management panel with booking service dependency.
     *
     * @param appointmentBookingService booking service (service layer)
     * @param onBack callback to return to menu
     */
    public SlotManagementPanel(
            AppointmentBookingService appointmentBookingService,
            Runnable onBack
    ) {
        this.appointmentBookingService = appointmentBookingService;
        this.onBack = onBack;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(UiStyle.SPACE_SM, UiStyle.SPACE_SM));
        setBackground(UiStyle.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS));

        JLabel title = new JLabel("Manage Appointment Slots (Admin Only)");
        title.setFont(UiStyle.FONT_SUBTITLE);

        JPanel card = UiStyle.createCardPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UiStyle.SPACE_XS, UiStyle.SPACE_SM, UiStyle.SPACE_XS, UiStyle.SPACE_SM);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instructions = new JLabel("Add new slots or cancel existing slots.");
        instructions.setFont(UiStyle.FONT_BODY);
        instructions.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        card.add(instructions, gbc);

        JLabel slotTimeLabel = UiStyle.createFieldLabel("Slot Time (e.g., 14:00)");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(slotTimeLabel, gbc);

        slotTimeField = new JTextField();
        UiStyle.styleTextComponent(slotTimeField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(slotTimeField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyle.SPACE_XS, 0));
        buttonPanel.setOpaque(false);

        addButton = UiStyle.createPrimaryButton("Add Slot", e -> handleAddSlot());
        cancelButton = UiStyle.createSecondaryButton("Cancel Slot", e -> handleCancelSlot());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(buttonPanel, gbc);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(UiStyle.FONT_BODY);
        messageLabel.setForeground(UiStyle.COLOR_INFO);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        card.add(messageLabel, gbc);

        JLabel slotsLabel = UiStyle.createFieldLabel("Existing Slots");
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(slotsLabel, gbc);

        slotsListModel = new DefaultListModel<>();
        slotsListComponent = new JList<>(slotsListModel);
        slotsListComponent.setFont(UiStyle.FONT_BODY);
        slotsListComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(slotsListComponent);
        scrollPane.setBorder(UiStyle.createCardBorder());
        scrollPane.setPreferredSize(new Dimension(0, 150));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        card.add(scrollPane, gbc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);

        JButton backButton = UiStyle.createSecondaryButton("Back to Dashboard", e -> onBack.run());
        bottom.add(backButton);

        add(title, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    /**
     * Refreshes the displayed slot list.
     */
    public void refresh() {
        slotsListModel.clear();
        // Display a message - in real implementation, you would query for all slots
        slotsListModel.addElement("(Slots list would appear here)");
    }

    private void handleAddSlot() {
        String time = slotTimeField.getText().trim();
        if (time.isEmpty()) {
            messageLabel.setText("Please enter a slot time");
            messageLabel.setForeground(Color.RED);
            return;
        }

        BookingStatus status = appointmentBookingService.addManagedSlot(time);
        
        if (status == BookingStatus.SUCCESS) {
            messageLabel.setText("Slot added successfully");
            messageLabel.setForeground(new Color(0, 128, 0));
            slotTimeField.setText("");
            refresh();
        } else if (status == BookingStatus.UNAUTHORIZED) {
            messageLabel.setText("Admin access required");
            messageLabel.setForeground(Color.RED);
        } else if (status == BookingStatus.SLOT_ALREADY_BOOKED) {
            messageLabel.setText("Slot already exists");
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("Failed to add slot");
            messageLabel.setForeground(Color.RED);
        }
    }

    private void handleCancelSlot() {
        String time = slotTimeField.getText().trim();
        if (time.isEmpty()) {
            messageLabel.setText("Please enter a slot time to cancel");
            messageLabel.setForeground(Color.RED);
            return;
        }

        BookingStatus status = appointmentBookingService.cancelManagedSlot(time);
        
        if (status == BookingStatus.SUCCESS) {
            messageLabel.setText("Slot cancelled successfully");
            messageLabel.setForeground(new Color(0, 128, 0));
            slotTimeField.setText("");
            refresh();
        } else if (status == BookingStatus.UNAUTHORIZED) {
            messageLabel.setText("Admin access required");
            messageLabel.setForeground(Color.RED);
        } else if (status == BookingStatus.SLOT_NOT_FOUND) {
            messageLabel.setText("Slot not found or already cancelled");
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("Failed to cancel slot");
            messageLabel.setForeground(Color.RED);
        }
    }
}

