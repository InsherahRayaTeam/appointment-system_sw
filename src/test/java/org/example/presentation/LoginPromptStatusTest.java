package org.example.presentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginPromptStatusTest {

    @Test
    void enum_ContainsExpectedValues() {
        assertEquals(LoginPromptStatus.SUCCESS, LoginPromptStatus.valueOf("SUCCESS"));
        assertEquals(LoginPromptStatus.FAILED, LoginPromptStatus.valueOf("FAILED"));
        assertEquals(LoginPromptStatus.LOCKED, LoginPromptStatus.valueOf("LOCKED"));
        assertEquals(LoginPromptStatus.CANCELLED, LoginPromptStatus.valueOf("CANCELLED"));
    }

    @Test
    void valueOf_ReturnsCorrectEnumValue() {
        assertEquals(LoginPromptStatus.SUCCESS, LoginPromptStatus.valueOf("SUCCESS"));
        assertEquals(LoginPromptStatus.FAILED, LoginPromptStatus.valueOf("FAILED"));
        assertEquals(LoginPromptStatus.LOCKED, LoginPromptStatus.valueOf("LOCKED"));
        assertEquals(LoginPromptStatus.CANCELLED, LoginPromptStatus.valueOf("CANCELLED"));
    }

    @Test
    void enum_HasExpectedTotalNumberOfValues() {
        assertEquals(4, LoginPromptStatus.values().length);
    }
}

