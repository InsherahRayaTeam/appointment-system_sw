package org.example.service;
import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
class VirtualRuleTest {
    private final VirtualRule virtualRule = new VirtualRule();
    @Test
    void shouldAlwaysAllowVirtualAppointment() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 30, 1);
        assertTrue(virtualRule.isValid(appointment));
    }
    @Test
    void shouldExposeSupportedType() {
        assertEquals(AppointmentType.VIRTUAL, virtualRule.getSupportedType());
    }
    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> virtualRule.isValid(null));
    }
}
