package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Represents slots panel in the system.
 */
public class SlotsPanel extends JPanel {

    private static final String AVAILABLE_SLOTS_NOTE = "Available slots only (booked slots are not selectable).";
    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String AVAILABLE_STATUS = "Available";
    private static final String NO_AVAILABLE_SLOTS = "No available slots";
    private static final String ADD_SLOT_TITLE = "Add Slot";

    private final AppointmentService appointmentService;
    private final DefaultTableModel tableModel;
    private JSpinner slotDateSpinner;
    private JLabel slotDayValueLabel;
    private JComboBox<String> slotTimeComboBox;

    /**
     * Creates a new slots panel object with the given values.
     *
     * @param appointmentService service used to run business logic
     */
    public SlotsPanel(AppointmentService appointmentService) {
        this(appointmentService, false);
    }

    /**
     * Creates a new slots panel object with the given values.
     *
     * @param appointmentService service used to run business logic
     * @param adminSlotCreationEnabled flag that enables admin slot creation controls
     */
    public SlotsPanel(AppointmentService appointmentService, boolean adminSlotCreationEnabled) {
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel noteLabel = new JLabel(AVAILABLE_SLOTS_NOTE);
        add(noteLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[] {"Date", "Day", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable slotsTable = new JTable(tableModel);
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(slotsTable), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton(REFRESH_BUTTON_LABEL);
        refreshButton.addActionListener(e -> refreshData());

        if (adminSlotCreationEnabled) {
            slotDateSpinner = new JSpinner(new SpinnerDateModel());
            slotDateSpinner.setEditor(new JSpinner.DateEditor(slotDateSpinner, "yyyy-MM-dd"));
            slotDayValueLabel = new JLabel("");
            slotTimeComboBox = new JComboBox<>(SlotInputOptions.timeValues());
            JButton addSlotButton = new JButton(ADD_SLOT_TITLE);
            addSlotButton.addActionListener(e -> onAddSlot());
            slotDateSpinner.addChangeListener(e -> syncDerivedDaySelection());

            footerPanel.add(new JLabel("Date (yyyy-MM-dd):"));
            footerPanel.add(slotDateSpinner);
            footerPanel.add(new JLabel("Day (derived):"));
            footerPanel.add(slotDayValueLabel);
            footerPanel.add(new JLabel("Time (HH:mm):"));
            footerPanel.add(slotTimeComboBox);
            footerPanel.add(addSlotButton);
        }

        footerPanel.add(refreshButton);
        add(footerPanel, BorderLayout.SOUTH);

        refreshData();
        syncDerivedDaySelection();
    }

    /**
     * Reloads and updates data.
     */
    public final void refreshData() {
        tableModel.setRowCount(0);

        List<AppointmentSlot> availableSlots = appointmentService.getAvailableSlots();
        for (AppointmentSlot slot : availableSlots) {
            tableModel.addRow(new Object[] {slot.getDate(), slot.getDay(), slot.getTime(), AVAILABLE_STATUS});
        }

        if (availableSlots.isEmpty()) {
            tableModel.addRow(new Object[] {"-", "-", "-", NO_AVAILABLE_SLOTS});
        }
    }

    BookingStatus addSlotFromInputs(String dateText, String timeText) {
        return appointmentService.addSlot(dateText, timeText);
    }

    private void onAddSlot() {
        syncDerivedDaySelection();
        BookingStatus status = addSlotFromInputs(selectedDateText(), selectedTimeText());

        int messageType = GuiMessageHelper.isSuccessLike(status)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, GuiMessageHelper.toMessage(status), ADD_SLOT_TITLE, messageType);

        if (status == BookingStatus.SUCCESS) {
            slotDateSpinner.setValue(new Date());
            syncDerivedDaySelection();
            slotTimeComboBox.setSelectedIndex(0);
            refreshData();
        }
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

