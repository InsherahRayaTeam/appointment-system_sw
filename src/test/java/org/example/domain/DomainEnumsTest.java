package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainEnumsTest {

    @Test
    void appointmentType_ContainsAllSupportedTypesAndDisplayNames() {
        assertEquals(8, AppointmentType.values().length);
        assertEquals("Normal", AppointmentType.NORMAL.getDisplayName());
        assertEquals("Urgent", AppointmentType.URGENT.getDisplayName());
        assertEquals("Follow-up", AppointmentType.FOLLOW_UP.getDisplayName());
        assertEquals("Assessment", AppointmentType.ASSESSMENT.getDisplayName());
        assertEquals("Virtual", AppointmentType.VIRTUAL.getDisplayName());
        assertEquals("In-person", AppointmentType.IN_PERSON.getDisplayName());
        assertEquals("Individual", AppointmentType.INDIVIDUAL.getDisplayName());
        assertEquals("Group", AppointmentType.GROUP.getDisplayName());
    }

    @Test
    void appointmentStatus_ContainsExpectedValues() {
        assertEquals(AppointmentStatus.CONFIRMED, AppointmentStatus.valueOf("CONFIRMED"));
        assertEquals(AppointmentStatus.WAITLISTED, AppointmentStatus.valueOf("WAITLISTED"));
        assertEquals(AppointmentStatus.MODIFIED, AppointmentStatus.valueOf("MODIFIED"));
        assertEquals(AppointmentStatus.RESCHEDULED, AppointmentStatus.valueOf("RESCHEDULED"));
        assertEquals(AppointmentStatus.ATTENDED, AppointmentStatus.valueOf("ATTENDED"));
        assertEquals(AppointmentStatus.COMPLETED, AppointmentStatus.valueOf("COMPLETED"));
        assertEquals(AppointmentStatus.CANCELLED, AppointmentStatus.valueOf("CANCELLED"));
        assertEquals(8, AppointmentStatus.values().length);
    }

    @Test
    void userRole_ContainsExpectedValues() {
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(UserRole.USER, UserRole.valueOf("USER"));
        assertEquals(2, UserRole.values().length);
    }
}

