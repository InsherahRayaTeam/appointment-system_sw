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
 * Shows the form where a user books a new appointment.
 */
public class BookingPanel extends JPanel {

    private static final String REFRESH_SLOTS_BUTTON = "Refresh Slots";
    private static final String CLEAR_BUTTON = "Clear";
    private static final String BOOK_APPOINTMENT_BUTTON = "Book Appointment";
    private static final String NO_AVAILABLE_SLOTS_PLACEHOLDER = "No available slots";
    private static final String NO_SLOTS_AVAILABLE_MESSAGE = "No slots are currently available.";
    private static final String BOOKING_DIALOG_TITLE = "Booking";
    private static final String BOOKING_ERROR_TITLE = "Booking Error";
    private static final String BOOKED_FOR_NAME_LABEL = "Booked For (name):";
    private static final String PHONE_NUMBER_LABEL = "Phone Number:";

    private final SystemUser user;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final Runnable onBookingSuccess;
    private final DialogDisplayer dialogDisplayer;

    private final JTextField bookedForNameField;
    private final JTextField phoneNumberField;
    private final JComboBox<String> slotComboBox;
    private final JComboBox<AppointmentType> typeComboBox;
    private final JTextField durationField;
    private final JTextField participantCountField;
    private final JButton bookButton;

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
        this(user, appointmentService, appointmentBookingService, onBookingSuccess, DefaultDialogDisplayer.INSTANCE);
    }

    /**
     * Constructor allowing dialog displayer injection for tests.
     */
    public BookingPanel(
            SystemUser user,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            Runnable onBookingSuccess,
            DialogDisplayer dialogDisplayer
    ) {
        this.user = user;
        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
        this.onBookingSuccess = onBookingSuccess;
        this.dialogDisplayer = dialogDisplayer;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        formPanel.add(new JLabel(BOOKED_FOR_NAME_LABEL));
        bookedForNameField = new JTextField(user.getEmail());
        formPanel.add(bookedForNameField);

        formPanel.add(new JLabel(PHONE_NUMBER_LABEL));
        phoneNumberField = new JTextField();
        formPanel.add(phoneNumberField);

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
        JButton refreshButton = new JButton(REFRESH_SLOTS_BUTTON);
        JButton clearButton = new JButton(CLEAR_BUTTON);
        bookButton = new JButton(BOOK_APPOINTMENT_BUTTON);

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
            slotComboBox.addItem(NO_AVAILABLE_SLOTS_PLACEHOLDER);
            slotComboBox.setEnabled(false);
            bookButton.setEnabled(false);
        } else {
            slotComboBox.setEnabled(true);
            bookButton.setEnabled(true);
        }
    }

    /**
     * Tries to book an appointment using the current form values.
     */
    private void onBookAppointment() {
        if (!slotComboBox.isEnabled()) {
            dialogDisplayer.showMessage(this, NO_SLOTS_AVAILABLE_MESSAGE, BOOKING_DIALOG_TITLE, javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookingStatus status = appointmentBookingService.bookAppointment(
                bookedForNameField.getText(),
                phoneNumberField.getText(),
                selectedSlot(),
                durationField.getText(),
                participantCountField.getText(),
                selectedType()
        );

        if (GuiMessageHelper.isSuccessLike(status)) {
            dialogDisplayer.showMessage(null, "Appointment booked successfully!");
            clearInputs();
            refreshData();
            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }
            return;
        }

        dialogDisplayer.showMessage(this, GuiMessageHelper.toMessage(status), BOOKING_ERROR_TITLE, javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Returns the selected slot text from the combo box.
     *
     * @return selected slot text, or null when nothing is selected
     */
    private String selectedSlot() {
        Object selected = slotComboBox.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    /**
     * Returns the selected appointment type.
     *
     * @return selected type, or NORMAL when the selection is invalid
     */
    private AppointmentType selectedType() {
        Object selected = typeComboBox.getSelectedItem();
        return selected instanceof AppointmentType ? (AppointmentType) selected : AppointmentType.NORMAL;
    }

    /**
     * Clears inputs.
     */
    private void clearInputs() {
        bookedForNameField.setText(user.getEmail());
        phoneNumberField.setText("");
        durationField.setText("60");
        participantCountField.setText("1");
        typeComboBox.setSelectedItem(AppointmentType.NORMAL);
        if (slotComboBox.getItemCount() > 0) {
            slotComboBox.setSelectedIndex(0);
        }
    }
}

