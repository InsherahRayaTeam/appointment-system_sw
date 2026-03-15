package org.example;

import org.example.domain.AdminUser;
import org.example.presentation.ConsoleLogin;
import org.example.presentation.LoginPromptResult;
import org.example.presentation.LoginPromptStatus;
import org.example.repository.AdminRepository;
import org.example.service.AdminAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ConsoleLoginTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminAuthService = new AdminAuthService(adminRepository);
    }

    @Test
    void promptForResult_Success_ReturnsNormalizedUsername() {
        when(adminRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new AdminUser("admin", "admin")));

        ConsoleLogin login = new ConsoleLogin(adminAuthService, 3, Duration.ofSeconds(30));
        LoginPromptResult result = login.promptForResult(new Scanner(" admin \nadmin\n"));

        assertEquals(LoginPromptStatus.SUCCESS, result.getStatus());
        assertEquals("admin", result.getUsername());
    }

    @Test
    void promptForResult_CancelInput_ReturnsCancelled() {
        ConsoleLogin login = new ConsoleLogin(adminAuthService, 3, Duration.ofSeconds(30));
        LoginPromptResult result = login.promptForResult(new Scanner("q\nignored\n"));

        assertEquals(LoginPromptStatus.CANCELLED, result.getStatus());
    }

    @Test
    void promptForResult_ExceedsAttempts_ReturnsLockedThenRemainsLocked() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ConsoleLogin login = new ConsoleLogin(adminAuthService, 2, Duration.ofSeconds(30));
        LoginPromptResult firstResult = login.promptForResult(new Scanner("admin\nwrong\nadmin\nwrong\n"));
        LoginPromptResult secondResult = login.promptForResult(new Scanner(""));

        assertEquals(LoginPromptStatus.LOCKED, firstResult.getStatus());
        assertEquals(LoginPromptStatus.LOCKED, secondResult.getStatus());
    }
}
