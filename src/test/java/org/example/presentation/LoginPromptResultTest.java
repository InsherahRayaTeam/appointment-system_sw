package org.example.presentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginPromptResultTest {

    @Test
    void constructor_CorrectlyStoresStatusAndUsername() {
        LoginPromptResult result = new LoginPromptResult(LoginPromptStatus.SUCCESS, "admin");

        assertEquals(LoginPromptStatus.SUCCESS, result.getStatus());
        assertEquals("admin", result.getUsername());
    }

    @Test
    void getStatus_ReturnsCorrectStatus() {
        LoginPromptResult result = new LoginPromptResult(LoginPromptStatus.SUCCESS, "admin");

        assertEquals(LoginPromptStatus.SUCCESS, result.getStatus());
    }

    @Test
    void getUsername_ReturnsCorrectUsername() {
        LoginPromptResult result = new LoginPromptResult(LoginPromptStatus.SUCCESS, "admin");

        assertEquals("admin", result.getUsername());
    }
}

