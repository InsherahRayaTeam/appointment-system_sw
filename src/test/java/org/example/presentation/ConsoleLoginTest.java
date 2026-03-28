package org.example.presentation;

import org.example.domain.AdminUser;
import org.example.presentation.ConsoleLogin;
import org.example.presentation.LoginPromptResult;
import org.example.presentation.LoginPromptStatus;
import org.example.repository.AdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.EventManager;
import org.example.service.LoginAttemptTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsoleLoginTest {

    @Mock
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
    }

    private AdminAuthService buildAuthService(int maxAttempts) {
        return new AdminAuthService(
                adminRepository,
                new EventManager(),
                new LoginAttemptTracker(maxAttempts, java.time.Duration.ofSeconds(30))
        );
    }

    @Test
    void promptForResult_Success_ReturnsNormalizedUsername() {
        when(adminRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new AdminUser("admin", "admin")));

        ConsoleLogin login = new ConsoleLogin(buildAuthService(3));
        LoginPromptResult result = login.promptForResult(new Scanner(" admin \nadmin\n"));

        assertEquals(LoginPromptStatus.SUCCESS, result.getStatus());
        assertEquals("admin", result.getUsername());
    }

    @Test
    void promptForResult_CancelInput_ReturnsCancelled() {
        ConsoleLogin login = new ConsoleLogin(buildAuthService(3));
        LoginPromptResult result = login.promptForResult(new Scanner("q\nignored\n"));

        assertEquals(LoginPromptStatus.CANCELLED, result.getStatus());
    }


    @Test
    void prompt_WhenLocked_ReturnsFalse() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ConsoleLogin login = new ConsoleLogin(buildAuthService(1));
        login.promptForResult(new Scanner("admin\nwrong\n"));

        boolean promptResult = login.prompt(new Scanner("admin\nadmin\n"));

        assertFalse(promptResult);
    }
    @Test
    void promptForResult_ExceedsAttempts_ReturnsLockedThenRemainsLocked() {
        when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ConsoleLogin login = new ConsoleLogin(buildAuthService(2));
        LoginPromptResult firstResult = login.promptForResult(new Scanner("admin\nwrong\nadmin\nwrong\n"));
        LoginPromptResult secondResult = login.promptForResult(new Scanner(""));

        assertEquals(LoginPromptStatus.LOCKED, firstResult.getStatus());
        assertEquals(LoginPromptStatus.LOCKED, secondResult.getStatus());
    }
    @Test
    void promptForResult_InvalidCredentials_LocksWhenMaxAttemptsIsOne() {
        when(adminRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        ConsoleLogin login = new ConsoleLogin(buildAuthService(1));

        LoginPromptResult result =
                login.promptForResult(new Scanner("admin\nwrong\n"));

        assertEquals(LoginPromptStatus.LOCKED, result.getStatus());
    }
}
