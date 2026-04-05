package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
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
 * Captures user booking input and delegates booking to the service layer.
 *
 * @author appointment-system
 * @version 1.0
 */
public class BookingPanel extends JPanel {

    private final SystemUser user;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final Runnable onBookingSuccess;

    private final JTextField customerNameField;
    private final JComboBox<String> slotComboBox;
    private final JTextField durationField;
    private final JTextField participantCountField;

    /**
     * Creates a booking panel for the currently authenticated user.
     *
     * @param user current authenticated user
     * @param appointmentService service used to load available slots
     * @param appointmentBookingService service used to perform booking
     * @param onBookingSuccess callback run after successful booking
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

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        formPanel.add(new JLabel("Customer (email):"));
        customerNameField = new JTextField(user.getEmail());
        customerNameField.setEditable(false);
        formPanel.add(customerNameField);

        formPanel.add(new JLabel("Slot:"));
        slotComboBox = new JComboBox<>();
        formPanel.add(slotComboBox);

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
     * Reloads the available slot options.
     */
    public final void refreshData() {
        slotComboBox.removeAllItems();

        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        for (AppointmentSlot slot : slots) {
            slotComboBox.addItem(slot.getTime());
        }

        if (slotComboBox.getItemCount() == 0) {
            slotComboBox.addItem("No available slots");
            slotComboBox.setEnabled(false);
        } else {
            slotComboBox.setEnabled(true);
        }
    }

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
                user.getEmail(),
                selectedSlot(),
                durationField.getText(),
                participantCountField.getText()
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

    private String selectedSlot() {
        Object selected = slotComboBox.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    private void clearInputs() {
        customerNameField.setText(user.getEmail());
        durationField.setText("60");
        participantCountField.setText("1");
        if (slotComboBox.getItemCount() > 0) {
            slotComboBox.setSelectedIndex(0);
        }
    }
}

