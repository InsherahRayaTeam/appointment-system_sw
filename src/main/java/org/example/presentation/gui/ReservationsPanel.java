package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.SystemUser;
import org.example.service.AppointmentCalendarExportService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Represents reservations panel in the system.
 */
public class ReservationsPanel extends JPanel {

    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String CANCEL_RESERVATION_TITLE = "Cancel Reservation";
    private static final String MODIFY_RESERVATION_TITLE = "Modify Reservation";
    private static final String PLEASE_SELECT_RESERVATION_FIRST = "Please select a reservation first.";
    private static final String PLEASE_SELECT_A_REPLACEMENT_SLOT = "Please select a replacement slot.";
    private static final String INPUT_REQUIRED_TITLE = "Input Required";
    private static final String NEW_DATE_TIME_LABEL = "New Date/Time:";
    private static final String NO_AVAILABLE_SLOTS_PLACEHOLDER = "No available slots";
    private static final String ADD_TO_CALENDAR_LABEL = "Add to Calendar";
    private static final String CALENDAR_EXPORT_TITLE = "Add to Calendar";
    private static final EnumSet<AppointmentStatus> EXPORTABLE_STATUSES = EnumSet.of(
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.MODIFIED,
            AppointmentStatus.RESCHEDULED
    );

    private final SystemUser user;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentCalendarExportService calendarExportService;
    private final DefaultTableModel tableModel;
    private final JTable reservationsTable;
    private final JLabel infoLabel;
    private final JComboBox<String> slotComboBox;
    private final JButton modifyButton;
    private final JButton cancelButton;
    private final JButton addToCalendarButton;
    private final List<Appointment> currentReservations;

    /**
     * Creates a new reservations panel object with the given values.
     *
     * @param user user involved in this action
     * @param appointmentService service used to run business logic
     * @param appointmentBookingService service used to run business logic
     */
    public ReservationsPanel(
            SystemUser user,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService
    ) {
        this(user, appointmentService, appointmentBookingService, new AppointmentCalendarExportService());
    }

    /**
     * Creates a new reservations panel object with the given values.
     *
     * @param user user involved in this action
     * @param appointmentService service used to run business logic
     * @param appointmentBookingService service used to run business logic
     * @param calendarExportService service used to export appointments as ICS files
     */
    public ReservationsPanel(
            SystemUser user,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            AppointmentCalendarExportService calendarExportService
    ) {
        this.user = user;
        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
        this.calendarExportService = calendarExportService;
        this.currentReservations = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
        add(infoLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] {
                        "Reservation ID",
                        "Customer",
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

        reservationsTable = new JTable(tableModel) {
            @Override
            public void setRowSelectionInterval(int index0, int index1) {
                if (tableModel.getRowCount() == 0) {
                    refreshData();
                }
                super.setRowSelectionInterval(index0, index1);
            }
        };
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        slotComboBox = new JComboBox<>();
        JButton refreshButton = new JButton(REFRESH_BUTTON_LABEL);
        modifyButton = new JButton(MODIFY_RESERVATION_TITLE);
        cancelButton = new JButton(CANCEL_RESERVATION_TITLE);
        addToCalendarButton = new JButton(ADD_TO_CALENDAR_LABEL);

        refreshButton.addActionListener(e -> refreshData());
        modifyButton.addActionListener(e -> onModifyReservation());
        cancelButton.addActionListener(e -> onCancelReservation());
        addToCalendarButton.addActionListener(e -> onAddToCalendar());
        reservationsTable.getSelectionModel().addListSelectionListener(e -> updateActionState());
        slotComboBox.addActionListener(e -> updateActionState());

        actionPanel.add(new JLabel(NEW_DATE_TIME_LABEL));
        actionPanel.add(slotComboBox);
        actionPanel.add(refreshButton);
        actionPanel.add(modifyButton);
        actionPanel.add(cancelButton);
        actionPanel.add(addToCalendarButton);
        add(actionPanel, BorderLayout.SOUTH);

        refreshData();

    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);
        slotComboBox.removeAllItems();
        currentReservations.clear();

        List<Appointment> reservations = appointmentBookingService.getReservationsForCustomer(user.getEmail());
        currentReservations.addAll(reservations);
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

        if (reservations.isEmpty()) {
            infoLabel.setText("No reservations found for " + user.getEmail());
        } else {
            infoLabel.setText("Reservations for " + user.getEmail() + ": " + reservations.size() + " record(s)");
        }

        updateActionState();
    }

    /**
     * Runs on modify reservation for this class.
     */
    private void onModifyReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showSelectionWarning();
            return;
        }

        if (slotComboBox.getSelectedItem() == null || !slotComboBox.isEnabled()) {
            JOptionPane.showMessageDialog(
                    this,
                    PLEASE_SELECT_A_REPLACEMENT_SLOT,
                    INPUT_REQUIRED_TITLE,
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String newSlotSelection = slotComboBox.getSelectedItem().toString();
        BookingStatus status = appointmentBookingService.modifyOwnAppointment(reservationId, newSlotSelection);
        showResult(status, MODIFY_RESERVATION_TITLE);

        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    /**
     * Runs on cancel reservation for this class.
     */
    private void onCancelReservation() {
        String reservationId = selectedReservationId();
        if (reservationId == null) {
            showSelectionWarning();
            return;
        }

        BookingStatus status = appointmentBookingService.cancelOwnAppointment(reservationId);
        showResult(status, CANCEL_RESERVATION_TITLE);
        if (status == BookingStatus.SUCCESS) {
            refreshData();
        }
    }

    private void onAddToCalendar() {
        Appointment selected = selectedReservation();
        if (selected == null) {
            showSelectionWarning();
            return;
        }
        if (!isExportableStatus(selected.getStatus())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only booked/approved reservations can be exported.",
                    CALENDAR_EXPORT_TITLE,
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            File exportedFile = calendarExportService.generateCalendarFile(selected);
            JOptionPane.showMessageDialog(
                    this,
                    "Calendar file saved to: " + exportedFile.getAbsolutePath(),
                    CALENDAR_EXPORT_TITLE,
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to export calendar file: " + ex.getMessage(),
                    CALENDAR_EXPORT_TITLE,
                    JOptionPane.ERROR_MESSAGE
            );
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

        Object id = tableModel.getValueAt(selectedRow, 0);
        return id == null ? null : id.toString();
    }

    private Appointment selectedReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReservations.size()) {
            return null;
        }
        return currentReservations.get(selectedRow);
    }

    private boolean isExportableStatus(AppointmentStatus status) {
        return status != null && EXPORTABLE_STATUSES.contains(status);
    }

    /**
     * Shows result to the user.
     *
     * @param status status value used for this operation
     */
    private void showResult(BookingStatus status, String title) {
        int messageType = GuiMessageHelper.isSuccessLike(status)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(
                this,
                GuiMessageHelper.toMessage(status),
                title,
                messageType
        );
    }

    /**
     * Shows warning to the user.
     */
    private void showSelectionWarning() {
        JOptionPane.showMessageDialog(
                this,
                PLEASE_SELECT_RESERVATION_FIRST,
                INPUT_REQUIRED_TITLE,
                JOptionPane.WARNING_MESSAGE
        );
    }


    private void updateActionState() {
        boolean hasReservationSelection = reservationsTable.getSelectedRow() >= 0;
        boolean hasReplacementSlots = slotComboBox.getItemCount() > 0
                && !NO_AVAILABLE_SLOTS_PLACEHOLDER.equals(String.valueOf(slotComboBox.getItemAt(0)));

        slotComboBox.setEnabled(hasReplacementSlots);
        modifyButton.setEnabled(hasReservationSelection && hasReplacementSlots);
        cancelButton.setEnabled(hasReservationSelection);
        addToCalendarButton.setEnabled(hasReservationSelection && isExportableStatus(selectedReservationStatus()));
    }

    private AppointmentStatus selectedReservationStatus() {
        Appointment selected = selectedReservation();
        return selected == null ? null : selected.getStatus();
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
                appointment.getSlotDate(),
                appointment.getSlotDay(),
                appointment.getSlotTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getType(),
                appointment.getStatus()
        };
    }
}

