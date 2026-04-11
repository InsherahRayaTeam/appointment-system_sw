package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Represents admin reservations panel in the system.
 */
public class AdminReservationsPanel extends JPanel {

    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String MANAGE_RESERVATIONS_INFO =
            "Manage reservations (future reservations only for modify/cancel).";
    private static final String ONLY_ADMINS_CAN_MANAGE_RESERVATIONS = "Only admins can manage reservations.";
    private static final String NEW_DATE_TIME_LABEL = "New Date/Time:";
    private static final String CANCEL_RESERVATION_TITLE = "Cancel Reservation";
    private static final String MODIFY_APPOINTMENT_TITLE = "Modify Appointment";
    private static final String MARK_AS_ATTENDED_TITLE = "Mark as Attended";
    private static final String MARK_AS_NOT_ATTENDED_TITLE = "Mark as Not Attended";
    private static final String MARK_AS_COMPLETED_TITLE = "Mark as Completed";
    private static final String APPROVE_RESERVATION_TITLE = "Approve Reservation";
    private static final String ADD_SLOT_TITLE = "Add Slot";
    private static final String SLOT_DATE_LABEL = "Date:";
    private static final String SLOT_DAY_LABEL = "Day:";
    private static final String SLOT_TIME_LABEL = "Time:";
    private static final String ADD_SLOT_BUTTON_LABEL = "Add Slot";
    private static final String PLEASE_SELECT_RESERVATION_FIRST = "Please select a reservation first.";
    private static final String PLEASE_SELECT_A_REPLACEMENT_SLOT = "Please select a replacement slot.";
    private static final String INPUT_REQUIRED_TITLE = "Input Required";

    private static final String NO_AVAILABLE_SLOTS_PLACEHOLDER = "No available slots";

    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentService appointmentService;
    private final DefaultTableModel tableModel;
    private final JTable reservationsTable;
    private final JComboBox<String> slotComboBox;
    private final JLabel infoLabel;
    private final JButton modifyButton;
    private final JButton attendedButton;
    private final JButton notAttendedButton;
    private final JButton completedButton;
    private final JButton approveButton;
    private final JButton cancelButton;
    private final JSpinner slotDateSpinner;
    private final JLabel slotDayValueLabel;
    private final JComboBox<String> slotTimeComboBox;

    /**
     * Creates a new admin reservations panel object with the given values.
     *
     * @param appointmentBookingService service used to run business logic
     * @param appointmentService service used to run business logic
     */
    public AdminReservationsPanel(
            AppointmentBookingService appointmentBookingService,
            AppointmentService appointmentService
    ) {
        this.appointmentBookingService = appointmentBookingService;
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel(MANAGE_RESERVATIONS_INFO);
        add(infoLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] {
                        "Reservation ID",
                        "Booked For",
                        "Phone",
                        "Date",
                        "Day",
                        "Time",
                        "Duration",
                        "Participants",
                        "Type",
                        "Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        slotComboBox = new JComboBox<>();
        JButton refreshButton = new JButton(REFRESH_BUTTON_LABEL);
        modifyButton = new JButton(MODIFY_APPOINTMENT_TITLE);
        attendedButton = new JButton(MARK_AS_ATTENDED_TITLE);
        notAttendedButton = new JButton(MARK_AS_NOT_ATTENDED_TITLE);
        completedButton = new JButton(MARK_AS_COMPLETED_TITLE);
        approveButton = new JButton(APPROVE_RESERVATION_TITLE);
        cancelButton = new JButton(CANCEL_RESERVATION_TITLE);
        slotDateSpinner = new JSpinner(new SpinnerDateModel());
        slotDateSpinner.setEditor(new JSpinner.DateEditor(slotDateSpinner, "yyyy-MM-dd"));
        slotDayValueLabel = new JLabel("");
        slotTimeComboBox = new JComboBox<>(SlotInputOptions.timeValues());
        JButton addSlotButton = new JButton(ADD_SLOT_BUTTON_LABEL);

        refreshButton.addActionListener(e -> refreshData());
        modifyButton.addActionListener(e -> onModifyReservation());
        attendedButton.addActionListener(e -> onMarkAttended());
        notAttendedButton.addActionListener(e -> onMarkNotAttended());
        completedButton.addActionListener(e -> onMarkCompleted());
        approveButton.addActionListener(e -> onApproveReservation());
        cancelButton.addActionListener(e -> onCancelReservation());
        addSlotButton.addActionListener(e -> onAddSlot());
        reservationsTable.getSelectionModel().addListSelectionListener(e -> updateActionState());
        slotComboBox.addActionListener(e -> updateActionState());
        slotDateSpinner.addChangeListener(e -> syncDerivedDaySelection());

        actionPanel.add(new JLabel(NEW_DATE_TIME_LABEL));
        actionPanel.add(slotComboBox);
        actionPanel.add(refreshButton);
        actionPanel.add(modifyButton);
        actionPanel.add(attendedButton);
        actionPanel.add(notAttendedButton);
        actionPanel.add(completedButton);
        actionPanel.add(approveButton);
        actionPanel.add(cancelButton);
        actionPanel.add(new JLabel(SLOT_DATE_LABEL));
        actionPanel.add(slotDateSpinner);
        actionPanel.add(new JLabel(SLOT_DAY_LABEL));
        actionPanel.add(slotDayValueLabel);
        actionPanel.add(new JLabel(SLOT_TIME_LABEL));
        actionPanel.add(slotTimeComboBox);
        actionPanel.add(addSlotButton);

        add(actionPanel, BorderLayout.SOUTH);

        refreshData();
        syncDerivedDaySelection();
    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);
        slotComboBox.removeAllItems();

        if (!appointmentBookingService.canCurrentUserManageReservations()) {
            infoLabel.setText(ONLY_ADMINS_CAN_MANAGE_RESERVATIONS);
            setActionsEnabled(false);
            return;
        }

        List<Appointment> reservations = appointmentBookingService.getManagedReservations();
        for (Appointment appointment : reservations) {
            tableModel.addRow(toRow(appointment));
        }

        for (AppointmentSlot slot : appointmentService.getAvailableSlots()) {
            if (slot.isFutureSlot()) {
                slotComboBox.addItem(slot.getDateDayTimeLabel());
            }
        }

        if (slotComboBox.getItemCount() == 0) {
            slotComboBox.addItem(NO_AVAILABLE_SLOTS_PLACEHOLDER);
        }

        infoLabel.setText(MANAGE_RESERVATIONS_INFO);
        setActionsEnabled(true);
        updateActionState();
    }

    /**
     * Runs on cancel reservation for this class.
     */
    private void onCancelReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.cancelAppointment(reservationId);
        showResult(status, CANCEL_RESERVATION_TITLE);

        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    /**
     * Runs on modify reservation for this class.
     */
    private void onModifyReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }
        if (slotComboBox.getSelectedItem() == null) {
            showWarning(PLEASE_SELECT_A_REPLACEMENT_SLOT);
            return;
        }

        String newSlotTime = slotComboBox.getSelectedItem().toString();
        BookingStatus status = appointmentBookingService.modifyAppointment(reservationId, newSlotTime);
        showResult(status, MODIFY_APPOINTMENT_TITLE);

        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    private void onApproveReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.approveAppointment(reservationId);
        showResult(status, APPROVE_RESERVATION_TITLE);
        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    /**
     * Runs on mark attended for this class.
     */
    private void onMarkAttended() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.markAppointmentAsAttended(reservationId);
        showResult(status, MARK_AS_ATTENDED_TITLE);
        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    /**
     * Runs on mark completed for this class.
     */
    private void onMarkCompleted() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.markAppointmentAsCompleted(reservationId);
        showResult(status, MARK_AS_COMPLETED_TITLE);
        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    /**
     * Runs on mark not attended for this class.
     */
    private void onMarkNotAttended() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.markAppointmentAsNotAttended(reservationId);
        showResult(status, MARK_AS_NOT_ATTENDED_TITLE);
        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    private void onAddSlot() {
        syncDerivedDaySelection();
        BookingStatus status = appointmentService.addSlot(selectedDateText(), selectedTimeText());
        showResult(status, ADD_SLOT_TITLE);
        if (status == BookingStatus.SUCCESS) {
            slotDateSpinner.setValue(new Date());
            syncDerivedDaySelection();
            slotTimeComboBox.setSelectedIndex(0);
            refreshData();
        }
    }

    /**
     * Runs selected reservation id for this class.
     *
     * @return text result from this method
     */
    private String selectedReservationId() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        Object value = tableModel.getValueAt(selectedRow, 0);
        return value == null ? null : value.toString();
    }

    /**
     * Runs to row for this class.
     *
     * @param appointment value for appointment
     *
     * @return result produced by this method
     */
    private Object[] toRow(Appointment appointment) {
        return new Object[] {
                appointment.getId(),
                appointment.getCustomerName(),
                appointment.getCustomerPhoneNumber(),
                appointment.getSlotDate(),
                appointment.getSlotDay(),
                appointment.getSlotTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getType(),
                appointment.getStatus()
        };
    }

    /**
     * Shows result to the user.
     *
     * @param status status value used for this operation
     * @param title title text for the dialog or view
     */
    private void showResult(BookingStatus status, String title) {
        int messageType = GuiMessageHelper.isSuccessLike(status)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, GuiMessageHelper.toMessage(status), title, messageType);
    }

    /**
     * Shows warning to the user.
     *
     * @param message message text to show or send
     */
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, INPUT_REQUIRED_TITLE, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Updates the actions enabled.
     *
     * @param enabled flag that tells if this feature is enabled
     */
    private void setActionsEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        modifyButton.setEnabled(enabled);
        slotComboBox.setEnabled(enabled);
        attendedButton.setEnabled(enabled);
        notAttendedButton.setEnabled(enabled);
        completedButton.setEnabled(enabled);
        approveButton.setEnabled(enabled);
    }

    private void updateActionState() {
        boolean hasSelection = reservationsTable.getSelectedRow() >= 0;
        boolean hasReplacementSlots = slotComboBox.getItemCount() > 0
                && !NO_AVAILABLE_SLOTS_PLACEHOLDER.equals(String.valueOf(slotComboBox.getItemAt(0)));

        slotComboBox.setEnabled(hasReplacementSlots);
        modifyButton.setEnabled(hasSelection && hasReplacementSlots);
        attendedButton.setEnabled(hasSelection);
        notAttendedButton.setEnabled(hasSelection);
        completedButton.setEnabled(hasSelection);
        approveButton.setEnabled(hasSelection);
        cancelButton.setEnabled(hasSelection);
    }

    private void syncDerivedDaySelection() {
        if (slotDayValueLabel == null || slotDateSpinner == null) {
            return;
        }

        DayOfWeek derivedDay = SlotInputOptions.deriveDay(selectedLocalDate());
        slotDayValueLabel.setText(derivedDay == null ? "" : derivedDay.name());
    }

    private String selectedDateText() {
        LocalDate selectedDate = selectedLocalDate();
        return selectedDate == null ? null : selectedDate.toString();
    }

    private LocalDate selectedLocalDate() {
        if (slotDateSpinner == null) {
            return null;
        }

        Date selectedDate = (Date) slotDateSpinner.getValue();
        if (selectedDate == null) {
            return null;
        }

        return selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    private String selectedTimeText() {
        Object selected = slotTimeComboBox.getSelectedItem();
        return selected == null ? null : selected.toString();
    }
}

