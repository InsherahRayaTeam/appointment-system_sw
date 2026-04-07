package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertEquals("09:00", model.getValueAt(0, 0));
        assertEquals("Available", model.getValueAt(0, 1));
        assertEquals("10:00", model.getValueAt(1, 0));
        assertEquals("Available", model.getValueAt(1, 1));
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
        assertEquals("No available slots", model.getValueAt(0, 1));
    }
}

