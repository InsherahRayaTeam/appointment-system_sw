package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CalendarPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentService appointmentService;

    @Test
    void calendarNavigation_changesMonthLabel() {
        CalendarPanel panel = new CalendarPanel(appointmentService, List::of);

        JLabel monthTitle = getPrivateField(panel, "monthTitleLabel", JLabel.class);
        String initial = monthTitle.getText();

        AbstractButton nextMonthButton = findButton(panel, "Next Month");
        clickButton(nextMonthButton);

        String afterNext = monthTitle.getText();
        assertNotEquals(initial, afterNext);
    }

    @Test
    void refreshCalendar_rendersAppointmentSummaryButton() {
        LocalDate date = LocalDate.now().plusDays(2);
        Appointment appointment = new Appointment(
                "id-1",
                "user@example.com",
                "user@example.com",
                LocalDateTime.of(date, LocalTime.of(10, 0)),
                30,
                1,
                AppointmentStatus.CONFIRMED
        );

        CalendarPanel panel = new CalendarPanel(appointmentService, () -> List.of(appointment));
        panel.refreshCalendar();

        AbstractButton appointmentButton = findButton(panel, "10:00 - CONFIRMED");
        assertNotNull(appointmentButton);
        assertEquals(new Color(46, 125, 50), appointmentButton.getForeground());
    }

    @Test
    void refreshCalendar_appliesCancelledAndPendingColors() {
        LocalDate date = LocalDate.now().plusDays(3);
        Appointment cancelled = new Appointment(
                "id-2",
                "user@example.com",
                "user@example.com",
                LocalDateTime.of(date, LocalTime.of(11, 0)),
                30,
                1,
                AppointmentStatus.CANCELLED
        );
        Appointment pending = new Appointment(
                "id-3",
                "user@example.com",
                "user@example.com",
                LocalDateTime.of(date, LocalTime.of(12, 0)),
                30,
                1,
                AppointmentStatus.WAITLISTED
        );

        CalendarPanel panel = new CalendarPanel(appointmentService, () -> List.of(cancelled, pending));
        panel.refreshCalendar();

        AbstractButton cancelledButton = findButton(panel, "11:00 - CANCELLED");
        AbstractButton pendingButton = findButton(panel, "12:00 - WAITLISTED");

        assertNotNull(cancelledButton);
        assertNotNull(pendingButton);
        assertEquals(new Color(198, 40, 40), cancelledButton.getForeground());
        assertEquals(new Color(245, 124, 0), pendingButton.getForeground());
    }

    @Test
    void refreshCalendar_alignsFirstDayUsingMonthOffset() {
        CalendarPanel panel = new CalendarPanel(appointmentService, List::of);
        YearMonth month = YearMonth.of(2026, 4);
        setPrivateField(panel, "currentMonth", month);

        panel.refreshCalendar();

        JPanel dayGrid = getPrivateField(panel, "dayGrid", JPanel.class);
        int firstDayIndex = indexOfDayCell(dayGrid, "1");
        int expectedOffset = month.atDay(1).getDayOfWeek().getValue() % 7;

        assertEquals(expectedOffset, firstDayIndex);
    }

    private int indexOfDayCell(JPanel dayGrid, String dayText) {
        Component[] components = dayGrid.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (!(component instanceof JPanel)) {
                continue;
            }

            JLabel label = findLabel((JPanel) component, dayText);
            if (label != null) {
                return i;
            }
        }
        return -1;
    }

    private JLabel findLabel(JPanel panel, String text) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (text.equals(label.getText())) {
                    return label;
                }
            }
            if (component instanceof JPanel) {
                JLabel nested = findLabel((JPanel) component, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to set field '" + fieldName + "'", ex);
        }
    }
}

