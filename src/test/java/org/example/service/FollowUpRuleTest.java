package org.example.service;
import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
class FollowUpRuleTest {
    private final FollowUpRule followUpRule = new FollowUpRule();
    @Test
    void shouldAlwaysAllowFollowUpAppointment() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 45, 1);
        assertTrue(followUpRule.isValid(appointment));
    }
    @Test
    void shouldExposeSupportedType() {
        assertEquals(AppointmentType.FOLLOW_UP, followUpRule.getSupportedType());
    }
    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> followUpRule.isValid(null));
    }
}
