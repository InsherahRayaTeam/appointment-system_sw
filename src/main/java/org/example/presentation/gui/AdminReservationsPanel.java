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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
    private static final String ADD_SLOT_TITLE = "Add Slot";
    private static final String SLOT_DATE_LABEL = "Date:";
    private static final String SLOT_TIME_LABEL = "Time:";
    private static final String ADD_SLOT_BUTTON_LABEL = "Add Slot";
    private static final String PLEASE_SELECT_RESERVATION_FIRST = "Please select a reservation first.";
    private static final String PLEASE_SELECT_A_REPLACEMENT_SLOT = "Please select a replacement slot.";
    private static final String INPUT_REQUIRED_TITLE = "Input Required";

    private static final int DATE_COLUMN_INDEX = 3;
    private static final int DAY_COLUMN_INDEX = 4;
    private static final int TIME_COLUMN_INDEX = 5;
    private static final int STATUS_COLUMN_INDEX = 8;

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
    private final JButton cancelButton;
    private final JTextField slotDateField;
    private final JTextField slotTimeField;

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
        cancelButton = new JButton(CANCEL_RESERVATION_TITLE);
        slotDateField = new JTextField(10);
        slotTimeField = new JTextField(6);
        JButton addSlotButton = new JButton(ADD_SLOT_BUTTON_LABEL);

        refreshButton.addActionListener(e -> refreshData());
        modifyButton.addActionListener(e -> onModifyReservation());
        attendedButton.addActionListener(e -> onMarkAttended());
        notAttendedButton.addActionListener(e -> onMarkNotAttended());
        completedButton.addActionListener(e -> onMarkCompleted());
        cancelButton.addActionListener(e -> onCancelReservation());
        addSlotButton.addActionListener(e -> onAddSlot());

        actionPanel.add(new JLabel(NEW_DATE_TIME_LABEL));
        actionPanel.add(slotComboBox);
        actionPanel.add(refreshButton);
        actionPanel.add(modifyButton);
        actionPanel.add(attendedButton);
        actionPanel.add(notAttendedButton);
        actionPanel.add(completedButton);
        actionPanel.add(cancelButton);
        actionPanel.add(new JLabel(SLOT_DATE_LABEL));
        actionPanel.add(slotDateField);
        actionPanel.add(new JLabel(SLOT_TIME_LABEL));
        actionPanel.add(slotTimeField);
        actionPanel.add(addSlotButton);

        add(actionPanel, BorderLayout.SOUTH);

        refreshData();
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

        infoLabel.setText(MANAGE_RESERVATIONS_INFO);
        setActionsEnabled(true);
    }

    /**
     * Runs on cancel reservation for this class.
     */
    private void onCancelReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showWarning(PLEASE_SELECT_RESERVATION_FIRST);
            return;
        }

        BookingStatus status = appointmentBookingService.cancelAppointment(reservationId);
        showResult(status, CANCEL_RESERVATION_TITLE);

        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                tableModel.removeRow(selectedRow);
            }
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

        int selectedRow = reservationsTable.getSelectedRow();
        String newSlotTime = slotComboBox.getSelectedItem().toString();
        BookingStatus status = appointmentBookingService.modifyAppointment(reservationId, newSlotTime);
        showResult(status, MODIFY_APPOINTMENT_TITLE);

        if (status == BookingStatus.SUCCESS) {
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                String[] parts = splitSlotLabel(newSlotTime);
                tableModel.setValueAt(parts[0], selectedRow, DATE_COLUMN_INDEX);
                tableModel.setValueAt(parts[1], selectedRow, DAY_COLUMN_INDEX);
                tableModel.setValueAt(parts[2], selectedRow, TIME_COLUMN_INDEX);
                tableModel.setValueAt("RESCHEDULED", selectedRow, STATUS_COLUMN_INDEX);
            }
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
            updateSelectedStatus("ATTENDED");
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
            updateSelectedStatus("COMPLETED");
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
            updateSelectedStatus("NOT_ATTENDED");
        }
    }

    private void onAddSlot() {
        BookingStatus status = appointmentService.addSlot(
                slotDateField.getText(),
                slotTimeField.getText()
        );
        showResult(status, ADD_SLOT_TITLE);
        if (status == BookingStatus.SUCCESS) {
            slotDateField.setText("");
            slotTimeField.setText("");
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
                appointment.getStatus()
        };
    }

    private String[] splitSlotLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return new String[] {"N/A", "N/A", "N/A"};
        }

        String normalized = label.trim();
        if (normalized.length() >= 17) {
            String date = normalized.substring(0, 10);
            String time = normalized.substring(normalized.length() - 5);

            int open = normalized.indexOf('(');
            int close = normalized.indexOf(')');
            String day = (open >= 0 && close > open)
                    ? normalized.substring(open + 1, close).toUpperCase()
                    : "N/A";

            return new String[] {date, day, time};
        }

        return new String[] {"N/A", "N/A", normalized};
    }

    /**
     * Shows result to the user.
     *
     * @param status status value used for this operation
     * @param title title text for the dialog or view
     */
    private void showResult(BookingStatus status, String title) {
        int messageType = status == BookingStatus.SUCCESS
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

    private void updateSelectedStatus(String status) {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            tableModel.setValueAt(status, selectedRow, STATUS_COLUMN_INDEX);
        }
    }

    /**
     * Updates the actions enabled.
     *
     * @param enabled flag that tells if this feature is enabled
     */
    private void setActionsEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
        modifyButton.setEnabled(enabled && slotComboBox.getItemCount() > 0);
        slotComboBox.setEnabled(enabled && slotComboBox.getItemCount() > 0);
        attendedButton.setEnabled(enabled);
        notAttendedButton.setEnabled(enabled);
        completedButton.setEnabled(enabled);
    }
}

