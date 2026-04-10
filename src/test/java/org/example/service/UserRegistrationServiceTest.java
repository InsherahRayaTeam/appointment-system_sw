package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventManager eventManager;

    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        userRegistrationService = new UserRegistrationService(userRepository, eventManager);
    }

    @Test
    void registerUser_ValidInput_ReturnsSuccessAndSavesUser() {
        when(userRepository.findById("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "alice@example.com",
                "pass123",
                "pass123"
        );

        assertEquals(SignUpStatus.SUCCESS, status);
        ArgumentCaptor<SystemUser> captor = ArgumentCaptor.forClass(SystemUser.class);
        verify(userRepository).save(captor.capture());
        assertEquals("alice", captor.getValue().getId());
        assertEquals("alice@example.com", captor.getValue().getEmail());
        assertEquals(UserRole.USER, captor.getValue().getRole());
    }

    @Test
    void registerUser_DuplicateUsername_ReturnsUsernameAlreadyExists() {
        when(userRepository.findById("alice")).thenReturn(
                Optional.of(new SystemUser("alice", "other@example.com", "pass123", UserRole.USER))
        );

        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "alice@example.com",
                "pass123",
                "pass123"
        );

        assertEquals(SignUpStatus.USERNAME_ALREADY_EXISTS, status);
    }

    @Test
    void registerUser_DuplicateEmail_ReturnsEmailAlreadyExists() {
        when(userRepository.findById("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(
                Optional.of(new SystemUser("other-id", "alice@example.com", "pass123", UserRole.USER))
        );

        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "alice@example.com",
                "pass123",
                "pass123"
        );

        assertEquals(SignUpStatus.EMAIL_ALREADY_EXISTS, status);
    }

    @Test
    void registerUser_InvalidEmail_ReturnsInvalidEmail() {
        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "bad-email",
                "pass123",
                "pass123"
        );

        assertEquals(SignUpStatus.INVALID_EMAIL, status);
    }

    @Test
    void registerUser_PasswordMismatch_ReturnsPasswordMismatch() {
        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "alice@example.com",
                "pass123",
                "pass456"
        );

        assertEquals(SignUpStatus.PASSWORD_MISMATCH, status);
    }

    @Test
    void registerUser_WeakPassword_ReturnsWeakPassword() {
        SignUpStatus status = userRegistrationService.registerUser(
                "alice",
                "alice@example.com",
                "short",
                "short"
        );

        assertEquals(SignUpStatus.WEAK_PASSWORD, status);
    }

    @Test
    void registerUser_ValidInput_NotifiesObservers() {
        when(userRepository.findById("new-user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        SignUpStatus status = userRegistrationService.registerUser(
                "new-user",
                "new@example.com",
                "new1234",
                "new1234"
        );

        assertEquals(SignUpStatus.SUCCESS, status);
        verify(eventManager).notifyObservers("User registered successfully: new@example.com");
        assertSame(SignUpStatus.SUCCESS, status);
    }

    @Test
    void signUp_WithValidInput_SavesUserInRepository() {
        InMemoryUserRepository inMemoryUserRepository = new InMemoryUserRepository();
        UserRegistrationService registrationService = new UserRegistrationService(inMemoryUserRepository, new EventManager());

        SignUpStatus status = registrationService.signUp("fresh@example.com", "fresh123");

        assertEquals(SignUpStatus.SUCCESS, status);
        Optional<SystemUser> saved = inMemoryUserRepository.findByEmail("fresh@example.com");
        assertTrue(saved.isPresent());
        assertEquals("fresh@example.com", saved.get().getEmail());
        assertEquals(UserRole.USER, saved.get().getRole());
    }

    @Test
    void signUp_WithDuplicateEmail_RejectsRegistration() {
        InMemoryUserRepository inMemoryUserRepository = new InMemoryUserRepository();
        UserRegistrationService registrationService = new UserRegistrationService(inMemoryUserRepository, new EventManager());

        SignUpStatus first = registrationService.signUp("dup@example.com", "dup1234");
        SignUpStatus second = registrationService.signUp("dup@example.com", "other1234");

        assertEquals(SignUpStatus.SUCCESS, first);
        assertEquals(SignUpStatus.EMAIL_ALREADY_EXISTS, second);
    }
}


