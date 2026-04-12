package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.service.AppointmentService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Displays appointments in a monthly calendar view with day and appointment interactions.
 */
public class CalendarPanel extends JPanel {

    private static final int MAX_VISIBLE_APPOINTMENTS_PER_DAY = 3;
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color TODAY_COLOR = new Color(225, 240, 255);
    private static final Color DEFAULT_DAY_COLOR = Color.WHITE;
    private static final Color BOOKED_COLOR = new Color(46, 125, 50);
    private static final Color CANCELLED_COLOR = new Color(198, 40, 40);
    private static final Color PENDING_COLOR = new Color(245, 124, 0);

    private final Supplier<List<Appointment>> appointmentSupplier;
    private final JLabel monthTitleLabel;
    private final JPanel weekHeaderGrid;
    private final JPanel dayGrid;

    private YearMonth currentMonth;
    private Map<LocalDate, List<Appointment>> appointmentsByDate;

    /**
     * Creates a new calendar panel object.
     *
     * @param appointmentService service used by the GUI layer
     * @param appointmentSupplier supplier that returns appointments visible in this view
     */
    public CalendarPanel(AppointmentService appointmentService, Supplier<List<Appointment>> appointmentSupplier) {
        Objects.requireNonNull(appointmentService, "appointmentService cannot be null");
        this.appointmentSupplier = Objects.requireNonNull(appointmentSupplier, "appointmentSupplier cannot be null");
        this.currentMonth = YearMonth.now();
        this.appointmentsByDate = new HashMap<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        monthTitleLabel = new JLabel("", JLabel.CENTER);
        monthTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton previousButton = new JButton("Previous Month");
        previousButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton nextButton = new JButton("Next Month");
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        navButtons.add(previousButton);
        navButtons.add(nextButton);
        headerPanel.add(monthTitleLabel, BorderLayout.CENTER);
        headerPanel.add(navButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        weekHeaderGrid = new JPanel(new GridLayout(1, 7, 6, 0));
        dayGrid = new JPanel(new GridLayout(6, 7, 6, 6));

        JPanel calendarContainer = new JPanel(new BorderLayout(0, 6));
        calendarContainer.add(weekHeaderGrid, BorderLayout.NORTH);
        calendarContainer.add(dayGrid, BorderLayout.CENTER);
        add(new JScrollPane(calendarContainer), BorderLayout.CENTER);

        addWeekHeaders();

        refreshCalendar();
    }

    /**
     * Reloads appointments and redraws the current calendar month.
     */
    public final void refreshCalendar() {
        appointmentsByDate = groupByDate(appointmentSupplier.get());
        monthTitleLabel.setText(currentMonth.format(MONTH_LABEL_FORMAT));
        rebuildMonthGrid();
    }

    private Map<LocalDate, List<Appointment>> groupByDate(List<Appointment> appointments) {
        Map<LocalDate, List<Appointment>> grouped = new HashMap<>();
        if (appointments == null) {
            return grouped;
        }

        for (Appointment appointment : appointments) {
            if (appointment == null || appointment.getStartTime() == null) {
                continue;
            }
            LocalDate day = appointment.getStartTime().toLocalDate();
            grouped.computeIfAbsent(day, key -> new ArrayList<>()).add(appointment);
        }

        for (List<Appointment> dailyAppointments : grouped.values()) {
            dailyAppointments.sort(Comparator.comparing(Appointment::getStartTime));
        }
        return grouped;
    }

    private void rebuildMonthGrid() {
        dayGrid.removeAll();

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstColumnIndex = toSundayStartIndex(firstDayOfMonth.getDayOfWeek());
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int i = 0; i < firstColumnIndex; i++) {
            dayGrid.add(createEmptyDayCell());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate cellDate = currentMonth.atDay(day);
            dayGrid.add(buildDayCell(cellDate));
        }

        int targetCells = 42;
        int currentCells = firstColumnIndex + daysInMonth;
        for (int i = currentCells; i < targetCells; i++) {
            dayGrid.add(createEmptyDayCell());
        }

        dayGrid.revalidate();
        dayGrid.repaint();
    }

    private void addWeekHeaders() {
        weekHeaderGrid.removeAll();
        DayOfWeek[] headers = {
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        };

        for (DayOfWeek dayOfWeek : headers) {
            JLabel label = new JLabel(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBackground(new Color(245, 245, 245));
            label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            weekHeaderGrid.add(label);
        }
        weekHeaderGrid.revalidate();
        weekHeaderGrid.repaint();
    }

    private JPanel buildDayCell(LocalDate date) {
        JPanel dayCell = new JPanel(new BorderLayout(4, 4));
        dayCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        dayCell.setBackground(LocalDate.now().equals(date) ? TODAY_COLOR : DEFAULT_DAY_COLOR);
        dayCell.setPreferredSize(new Dimension(140, 110));

        JPanel dayHeaderPanel = new JPanel(new BorderLayout());
        dayHeaderPanel.setOpaque(false);

        JLabel dayNumberLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.RIGHT);
        dayNumberLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dayNumberLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 6));
        dayHeaderPanel.add(dayNumberLabel, BorderLayout.EAST);
        dayCell.add(dayHeaderPanel, BorderLayout.NORTH);

        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setOpaque(false);
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

        List<Appointment> dayAppointments = appointmentsByDate.getOrDefault(date, Collections.emptyList());
        int visibleCount = Math.min(dayAppointments.size(), MAX_VISIBLE_APPOINTMENTS_PER_DAY);
        for (int i = 0; i < visibleCount; i++) {
            appointmentsPanel.add(buildAppointmentButton(dayAppointments.get(i)));
        }

        if (dayAppointments.size() > MAX_VISIBLE_APPOINTMENTS_PER_DAY) {
            JLabel moreLabel = new JLabel("+" + (dayAppointments.size() - MAX_VISIBLE_APPOINTMENTS_PER_DAY) + " more");
            moreLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            appointmentsPanel.add(moreLabel);
        }

        dayCell.add(appointmentsPanel, BorderLayout.CENTER);

        MouseAdapter clickListener = new MouseAdapter() {
            /**
             * Opens the day details dialog when a day cell is clicked.
             *
             * @param e mouse click event from the calendar grid
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                showDayAppointmentsDialog(date, dayAppointments);
            }
        };

        dayCell.addMouseListener(clickListener);
        dayHeaderPanel.addMouseListener(clickListener);
        dayNumberLabel.addMouseListener(clickListener);
        appointmentsPanel.addMouseListener(clickListener);

        return dayCell;
    }

    private JPanel createEmptyDayCell() {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)));
        cell.setBackground(new Color(250, 250, 250));
        return cell;
    }

    private JButton buildAppointmentButton(Appointment appointment) {
        LocalDateTime start = appointment.getStartTime();
        String summary = start == null
                ? "Unknown - Booked"
                : TIME_FORMAT.format(start) + " - " + shortStatusLabel(appointment.getStatus());

        JButton button = new JButton(summary);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(colorForStatus(appointment.getStatus()));
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setToolTipText(buildTooltip(appointment));
        button.addActionListener(e -> showAppointmentDetails(appointment));
        return button;
    }

    private void showDayAppointmentsDialog(LocalDate date, List<Appointment> dayAppointments) {
        List<Appointment> appointments = dayAppointments == null ? Collections.emptyList() : dayAppointments;
        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(this), "Appointments - " + date, true);
        dialog.setLayout(new BorderLayout(8, 8));

        if (appointments.isEmpty()) {
            dialog.add(new JLabel("No appointments for this day.", JLabel.CENTER), BorderLayout.CENTER);
        } else {
            JList<AppointmentListItem> list = new JList<>(appointments.stream()
                    .map(AppointmentListItem::new)
                    .toArray(AppointmentListItem[]::new));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.addMouseListener(new java.awt.event.MouseAdapter() {
                /**
                 * Opens appointment details when the user double-clicks an item.
                 *
                 * @param e mouse click event from the appointments list
                 */
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() >= 2 && list.getSelectedValue() != null) {
                        showAppointmentDetails(list.getSelectedValue().appointment);
                    }
                }
            });

            JButton detailsButton = new JButton("Show Details");
            detailsButton.addActionListener(e -> {
                AppointmentListItem selected = list.getSelectedValue();
                if (selected != null) {
                    showAppointmentDetails(selected.appointment);
                }
            });

            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.add(detailsButton);
            dialog.add(new JScrollPane(list), BorderLayout.CENTER);
            dialog.add(south, BorderLayout.SOUTH);
        }

        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAppointmentDetails(Appointment appointment) {
        String details = "Date/Time: " + safeDateTime(appointment.getStartTime())
                + "\nStatus: " + shortStatusLabel(appointment.getStatus())
                + "\nType: " + appointment.getType();
        JOptionPane.showMessageDialog(this, details, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private String buildTooltip(Appointment appointment) {
        return "Date/Time: " + safeDateTime(appointment.getStartTime())
                + " | Status: " + shortStatusLabel(appointment.getStatus())
                + " | Type: " + appointment.getType();
    }

    private String safeDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(DATE_TIME_FORMAT);
    }

    private String shortStatusLabel(AppointmentStatus status) {
        if (status == null) {
            return "Booked";
        }
        return status.name();
    }

    private Color colorForStatus(AppointmentStatus status) {
        if (status == null) {
            return BOOKED_COLOR;
        }

        Map<AppointmentStatus, Color> colorMap = new EnumMap<>(AppointmentStatus.class);
        colorMap.put(AppointmentStatus.CONFIRMED, BOOKED_COLOR);
        colorMap.put(AppointmentStatus.CANCELLED, CANCELLED_COLOR);
        colorMap.put(AppointmentStatus.WAITLISTED, PENDING_COLOR);
        colorMap.put(AppointmentStatus.MODIFIED, PENDING_COLOR);
        colorMap.put(AppointmentStatus.RESCHEDULED, PENDING_COLOR);

        return colorMap.getOrDefault(status, BOOKED_COLOR);
    }

    private int toSundayStartIndex(DayOfWeek dayOfWeek) {
        int dayValue = dayOfWeek.getValue();
        return dayValue % 7;
    }

    private static final class AppointmentListItem {
        private final Appointment appointment;

        private AppointmentListItem(Appointment appointment) {
            this.appointment = appointment;
        }

        /**
         * Builds one readable line for the appointment list.
         *
         * @return formatted text with time and status
         */
        @Override
        public String toString() {
            LocalDateTime start = appointment.getStartTime();
            String time = start == null ? "N/A" : TIME_FORMAT.format(start);
            String status = appointment.getStatus() == null ? "Booked" : appointment.getStatus().name();
            return time + " - " + status;
        }
    }
}


