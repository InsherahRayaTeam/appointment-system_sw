package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for booking appointments.
 *
 * Allows user to select a slot and submit a booking request.
 * Delegates business logic to AppointmentService (service layer).
 */
public class BookingPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final Runnable onBack;

    private JComboBox<String> slotsComboBox;
    private JButton submitButton;
    private JLabel messageLabel;

    /**
     * Creates booking panel with appointment service dependency.
     *
     * @param appointmentService appointment service (service layer)
     * @param onBack callback to return to menu
     */
    public BookingPanel(AppointmentService appointmentService, Runnable onBack) {
        this.appointmentService = appointmentService;
        this.onBack = onBack;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UiStyle.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JLabel title = new JLabel("Book Appointment");
        title.setFont(UiStyle.FONT_SUBTITLE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UiStyle.COLOR_CARD);
        card.setBorder(UiStyle.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instructions = new JLabel("Select an available slot and submit booking.");
        instructions.setFont(UiStyle.FONT_BODY);
        instructions.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        card.add(instructions, gbc);

        JLabel slotLabel = new JLabel("Available Slot");
        slotLabel.setFont(UiStyle.FONT_LABEL);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(slotLabel, gbc);

        slotsComboBox = new JComboBox<>();
        slotsComboBox.setFont(UiStyle.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(slotsComboBox, gbc);

        submitButton = new JButton("Submit Booking");
        UiStyle.stylePrimaryButton(submitButton);
        submitButton.addActionListener(e -> onBookSlot());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        card.add(submitButton, gbc);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(UiStyle.FONT_BODY);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(messageLabel, gbc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton refreshButton = new JButton("Refresh Slots");
        UiStyle.styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> refresh());

        JButton backButton = new JButton("Back to Dashboard");
        UiStyle.styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        bottom.add(refreshButton);
        bottom.add(backButton);

        add(title, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the available slot choices in the combo box.
     */
    public void refresh() {
        slotsComboBox.removeAllItems();
        showMessage("", UiStyle.COLOR_INFO);

        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        if (slots.isEmpty()) {
            slotsComboBox.addItem("No available slots");
            slotsComboBox.setEnabled(false);
            submitButton.setEnabled(false);
            showMessage("No open slots are available right now.", UiStyle.COLOR_INFO);
            return;
        }

        slotsComboBox.setEnabled(true);
        submitButton.setEnabled(true);
        for (AppointmentSlot slot : slots) {
            slotsComboBox.addItem(slot.getTime());
        }
    }

    private void onBookSlot() {
        if (!slotsComboBox.isEnabled() || slotsComboBox.getItemCount() == 0) {
            showMessage("No slot is available for booking.", UiStyle.COLOR_ERROR);
            return;
        }

        String selectedSlot = (String) slotsComboBox.getSelectedItem();
        if (selectedSlot == null || selectedSlot.trim().isEmpty()) {
            showMessage("Please select a slot to continue.", UiStyle.COLOR_ERROR);
            return;
        }

        boolean booked = appointmentService.bookSlot(selectedSlot);
        if (!booked) {
            showMessage("Booking failed. Please refresh and try again.", UiStyle.COLOR_ERROR);
            return;
        }

        showMessage("Booking confirmed for slot: " + selectedSlot, UiStyle.COLOR_SUCCESS);
        JOptionPane.showMessageDialog(
                this,
                "Appointment booked successfully for " + selectedSlot,
                "Booking Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
        refresh();
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message == null || message.trim().isEmpty() ? " " : message);
        messageLabel.setForeground(color);
    }
}
