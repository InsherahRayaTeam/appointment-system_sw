package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginStatusTest {

    @Test
    void enum_ContainsExpectedStatuses() {
        assertEquals(LoginStatus.SUCCESS, LoginStatus.valueOf("SUCCESS"));
        assertEquals(LoginStatus.BLANK_INPUT, LoginStatus.valueOf("BLANK_INPUT"));
        assertEquals(LoginStatus.INVALID_CREDENTIALS, LoginStatus.valueOf("INVALID_CREDENTIALS"));
    }

    @Test
    void enum_HasExpectedTotalNumberOfValues() {
        assertEquals(3, LoginStatus.values().length);
    }
}

