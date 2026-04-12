package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotsPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentService appointmentService;

    @Test
    void testDisplayAvailableSlots_onlyShowsAvailable() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Arrays.asList(
                new AppointmentSlot("09:00"),
                new AppointmentSlot("10:00")
        ));

        // Act
        SlotsPanel panel = new SlotsPanel(appointmentService);

        // Assert
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        assertEquals(2, model.getRowCount());
        assertEquals("09:00", model.getValueAt(0, 2));
        assertEquals("Available", model.getValueAt(0, 3));
        assertEquals("10:00", model.getValueAt(1, 2));
        assertEquals("Available", model.getValueAt(1, 3));
        assertFalse(model.isCellEditable(0, 0));
        verify(appointmentService).getAvailableSlots();
    }

    @Test
    void testRefreshData_withNoSlots_showsPlaceholderRow() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());

        // Act
        SlotsPanel panel = new SlotsPanel(appointmentService);

        // Assert
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        assertEquals(1, model.getRowCount());
        assertEquals("-", model.getValueAt(0, 0));
        assertEquals("No available slots", model.getValueAt(0, 3));
    }

    @Test
    void adminMode_displaysAddSlotButton() {
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());

        SlotsPanel panel = new SlotsPanel(appointmentService, true);
        AbstractButton addSlotButton = findButton(panel, "Add Slot");
        JLabel dayLabel = getPrivateField(panel, "slotDayValueLabel", JLabel.class);
        JSpinner dateSpinner = getPrivateField(panel, "slotDateSpinner", JSpinner.class);
        JComboBox<?> timeComboBox = getPrivateField(panel, "slotTimeComboBox", JComboBox.class);
        Date spinnerDate = (Date) dateSpinner.getValue();
        LocalDate expectedDayDate = spinnerDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        assertNotNull(addSlotButton);
        assertEquals(expectedDayDate.getDayOfWeek().name(), dayLabel.getText());
        assertEquals(7, timeComboBox.getItemCount());
        assertNotNull(dateSpinner);
    }

    @Test
    void addSlotForm_derivesDayAndUsesSelectedTime() {
        LocalDate date = LocalDate.of(2030, 12, 12);
        String expectedResetDay = LocalDate.now().getDayOfWeek().name();
        when(appointmentService.addSlot(date.toString(), "10:00"))
                .thenReturn(BookingStatus.SUCCESS);
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());

        SlotsPanel panel = new SlotsPanel(appointmentService, true);
        JSpinner slotDateSpinner = getPrivateField(panel, "slotDateSpinner", JSpinner.class);
        JLabel slotDayValueLabel = getPrivateField(panel, "slotDayValueLabel", JLabel.class);
        JComboBox<?> slotTimeComboBox = getPrivateField(panel, "slotTimeComboBox", JComboBox.class);
        AbstractButton addSlotButton = findButton(panel, "Add Slot");

        Date pickerDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        runOnEdt(() -> {
            slotDateSpinner.setValue(pickerDate);
            slotTimeComboBox.setSelectedItem("10:00");
        });

        clickButton(addSlotButton);

        assertEquals(expectedResetDay, slotDayValueLabel.getText());
        verify(appointmentService).addSlot(date.toString(), "10:00");
    }
}

