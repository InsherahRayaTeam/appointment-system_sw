package org.example.service;
import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
class NormalRuleTest {
    private final NormalRule normalRule = new NormalRule();
    @Test
    void shouldAlwaysAllowNormalAppointment() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 60, 1);
        assertTrue(normalRule.isValid(appointment));
    }
    @Test
    void shouldExposeSupportedType() {
        assertEquals(AppointmentType.NORMAL, normalRule.getSupportedType());
    }
    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> normalRule.isValid(null));
    }
}
