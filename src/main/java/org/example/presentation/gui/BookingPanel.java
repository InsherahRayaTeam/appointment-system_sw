package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for booking appointments.
 *
 * Allows user to select a slot and submit a booking request.
 * Delegates business logic to AppointmentService (service layer).
 *
 * @author appointment-system
 * @version 1.0
 */
public class BookingPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final Runnable onBack;

    private JTextField customerNameField;
    private JComboBox<String> slotsComboBox;
    private JTextField durationField;
    private JTextField participantField;
    private JButton submitButton;
    private JLabel messageLabel;

    /**
     * Creates booking panel with appointment service dependency.
     *
     * @param appointmentService appointment service (service layer)
     * @param appointmentBookingService booking service (service layer)
     * @param onBack callback to return to menu
     */
    public BookingPanel(
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            Runnable onBack
    ) {
        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
        this.onBack = onBack;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(UiStyle.SPACE_SM, UiStyle.SPACE_SM));
        setBackground(UiStyle.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS, UiStyle.SPACE_XS));

        JLabel title = new JLabel("Book Appointment");
        title.setFont(UiStyle.FONT_SUBTITLE);

        JPanel card = UiStyle.createCardPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UiStyle.SPACE_XS, UiStyle.SPACE_SM, UiStyle.SPACE_XS, UiStyle.SPACE_SM);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instructions = new JLabel("Enter reservation details and choose an available slot.");
        instructions.setFont(UiStyle.FONT_BODY);
        instructions.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        card.add(instructions, gbc);

        JLabel customerNameLabel = UiStyle.createFieldLabel("Customer Name");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(customerNameLabel, gbc);

        customerNameField = new JTextField();
        UiStyle.styleTextComponent(customerNameField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(customerNameField, gbc);

        JLabel slotLabel = UiStyle.createFieldLabel("Available Slot");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(slotLabel, gbc);

        slotsComboBox = new JComboBox<>();
        slotsComboBox.setFont(UiStyle.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(slotsComboBox, gbc);

        JLabel durationLabel = UiStyle.createFieldLabel("Duration (minutes)");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        card.add(durationLabel, gbc);

        durationField = new JTextField("60");
        UiStyle.styleTextComponent(durationField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(durationField, gbc);

        JLabel participantLabel = UiStyle.createFieldLabel("Participants");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        card.add(participantLabel, gbc);

        participantField = new JTextField("1");
        UiStyle.styleTextComponent(participantField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(participantField, gbc);

        submitButton = UiStyle.createPrimaryButton("Submit Booking", e -> onBookSlot());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        card.add(submitButton, gbc);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(UiStyle.FONT_BODY);
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(messageLabel, gbc);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton refreshButton = UiStyle.createSecondaryButton("Refresh Slots", e -> refresh());

        JButton backButton = UiStyle.createSecondaryButton("Back to Dashboard", e -> onBack.run());

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
        String customerName = customerNameField.getText();
        if (!slotsComboBox.isEnabled() || slotsComboBox.getItemCount() == 0) {
            showError("No slot is available for booking.");
            return;
        }

        String selectedSlot = (String) slotsComboBox.getSelectedItem();
        if (selectedSlot == null || selectedSlot.trim().isEmpty()) {
            showError("Please select a slot to continue.");
            return;
        }

        String durationInput = durationField.getText();
        String participantInput = participantField.getText();

        submitButton.setEnabled(false);
        try {
            BookingStatus status = appointmentBookingService.bookAppointment(
                    customerName,
                    selectedSlot,
                    durationInput,
                    participantInput
            );

            if (status == BookingStatus.SUCCESS) {
                showMessage("Booking confirmed for slot: " + selectedSlot, UiStyle.COLOR_SUCCESS);
                JOptionPane.showMessageDialog(
                        this,
                        "Appointment booked successfully for " + selectedSlot,
                        "Booking Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refresh();
                return;
            }

            showError(messageFor(status));
        } finally {
            submitButton.setEnabled(slotsComboBox.isEnabled());
        }
    }

    private void showError(String message) {
        showMessage(message, UiStyle.COLOR_ERROR);
        JOptionPane.showMessageDialog(this, message, "Booking Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message == null || message.trim().isEmpty() ? " " : message);
        messageLabel.setForeground(color);
    }

    private String messageFor(BookingStatus status) {
        if (status == null) {
            return "Booking failed due to an unknown error.";
        }

        switch (status) {
            case BLANK_CUSTOMER_NAME:
                return "Customer name is required.";
            case BLANK_SLOT_TIME:
                return "Please choose a slot.";
            case INVALID_DURATION:
                return "Duration must be a number up to 120 minutes.";
            case INVALID_PARTICIPANT_COUNT:
                return "Participants must be a number up to 5.";
            case SLOT_NOT_FOUND:
                return "Selected slot does not exist.";
            case SLOT_ALREADY_BOOKED:
                return "Selected slot is no longer available.";
            default:
                return "Booking failed. Please refresh and try again.";
        }
    }
}
