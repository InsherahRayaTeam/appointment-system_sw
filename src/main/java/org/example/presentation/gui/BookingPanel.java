package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentType;
import org.example.domain.SystemUser;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Represents booking panel in the system.
 */
public class BookingPanel extends JPanel {

    private final SystemUser user;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final Runnable onBookingSuccess;

    private final JTextField customerNameField;
    private final JComboBox<String> slotComboBox;
    private final JComboBox<AppointmentType> typeComboBox;
    private final JTextField durationField;
    private final JTextField participantCountField;

    /**
     * Creates a new booking panel object with the given values.
     *
     * @param user user involved in this action
     * @param appointmentService service used to run business logic
     * @param appointmentBookingService service used to run business logic
     * @param onBookingSuccess value for on booking success
     */
    public BookingPanel(
            SystemUser user,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            Runnable onBookingSuccess
    ) {
        this.user = user;
        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
        this.onBookingSuccess = onBookingSuccess;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        formPanel.add(new JLabel("Customer (email):"));
        customerNameField = new JTextField(user.getEmail());
        customerNameField.setEditable(false);
        formPanel.add(customerNameField);

        formPanel.add(new JLabel("Date/Day/Time:"));
        slotComboBox = new JComboBox<>();
        formPanel.add(slotComboBox);

        formPanel.add(new JLabel("Appointment Type:"));
        typeComboBox = new JComboBox<>(AppointmentType.values());
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField("60");
        formPanel.add(durationField);

        formPanel.add(new JLabel("Participants:"));
        participantCountField = new JTextField("1");
        formPanel.add(participantCountField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Slots");
        JButton clearButton = new JButton("Clear");
        JButton bookButton = new JButton("Book Appointment");

        refreshButton.addActionListener(e -> refreshData());
        clearButton.addActionListener(e -> clearInputs());
        bookButton.addActionListener(e -> onBookAppointment());

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(bookButton);

        add(buttonPanel, BorderLayout.SOUTH);

        refreshData();
    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        slotComboBox.removeAllItems();

        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        for (AppointmentSlot slot : slots) {
            slotComboBox.addItem(slot.getDateDayTimeLabel());
        }

        if (slotComboBox.getItemCount() == 0) {
            slotComboBox.addItem("No available slots");
            slotComboBox.setEnabled(false);
        } else {
            slotComboBox.setEnabled(true);
        }
    }

    /**
     * Runs on book appointment for this class.
     */
    private void onBookAppointment() {
        if (!slotComboBox.isEnabled()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No slots are currently available.",
                    "Booking",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        BookingStatus status = appointmentBookingService.bookAppointment(
                user.getId(),
                selectedSlot(),
                durationField.getText(),
                participantCountField.getText(),
                selectedType()
        );

        if (status == BookingStatus.SUCCESS) {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment booked successfully.",
                    "Booking",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshData();
            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                GuiMessageHelper.toMessage(status),
                "Booking Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Runs selected slot for this class.
     *
     * @return text result from this method
     */
    private String selectedSlot() {
        Object selected = slotComboBox.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    /**
     * Runs selected type for this class.
     *
     * @return result produced by this method
     */
    private AppointmentType selectedType() {
        Object selected = typeComboBox.getSelectedItem();
        return selected instanceof AppointmentType ? (AppointmentType) selected : AppointmentType.NORMAL;
    }

    /**
     * Clears inputs.
     */
    private void clearInputs() {
        customerNameField.setText(user.getEmail());
        durationField.setText("60");
        participantCountField.setText("1");
        typeComboBox.setSelectedItem(AppointmentType.NORMAL);
        if (slotComboBox.getItemCount() > 0) {
            slotComboBox.setSelectedIndex(0);
        }
    }
}

