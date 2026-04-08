package org.example.presentation.gui;

import org.example.domain.Credentials;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AdminAuthService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginFrameTest extends GuiTestSupport {

    @Mock
    private AdminAuthService authService;

    @Mock
    private ApplicationController appController;

    private LoginFrame frame;

    @BeforeEach
    void setUp() {
        frame = new LoginFrame(authService, appController);
    }

    @AfterEach
    void tearDown() {
        disposeIfWindow(frame);
    }

    @Test
    void testLoginButton_withValidCredentials_callsController() {
        // Arrange
        SystemUser user = new SystemUser("admin-1", "admin@example.com", "secret", UserRole.ADMIN);
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(user);
        when(authService.authenticateWithPolicy(any(Credentials.class))).thenReturn(result);

        JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
        JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
        JButton loginButton = getPrivateField(frame, "loginButton", JButton.class);

        runOnEdt(() -> {
            emailField.setText("admin@example.com");
            passwordField.setText("secret");
        });

        // Act
        clickButton(loginButton);

        // Assert
        verify(authService).authenticateWithPolicy(argThat(credentials ->
                "admin@example.com".equals(credentials.getEmail())
                        && "secret".equals(credentials.getPassword())
        ));
        verify(appController).handleSuccessfulLogin(result);
    }

    @Test
    void testLoginButton_withInvalidCredentials_showsError() {
        // Arrange
        when(authService.authenticateWithPolicy(any(Credentials.class)))
                .thenReturn(AuthenticationAttemptResult.failure(LoginStatus.INVALID_CREDENTIALS, 2));

        JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
        JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
        JButton loginButton = getPrivateField(frame, "loginButton", JButton.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);

        runOnEdt(() -> {
            emailField.setText("wrong@example.com");
            passwordField.setText("bad-password");
        });

        // Act
        clickButton(loginButton);

        // Assert
        assertEquals("Invalid email or password. Attempts remaining: 2", statusLabel.getText());
        assertTrue(loginButton.isEnabled());
        verifyNoInteractions(appController);
    }

    @Test
    void testLoginButton_withBlankInputs_showsValidationError() {

        JButton loginButton = getPrivateField(frame, "loginButton", JButton.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);

        runOnEdt(() -> {
            JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
            JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
            emailField.setText("");
            passwordField.setText("");
        });

        // Act
        clickButton(loginButton);

        // Assert
        assertEquals("Email and password are required.", statusLabel.getText());
        assertTrue(loginButton.isEnabled());
    }

    @Test
    void testClearButton_resetsInputsAndStatus() {
        // Arrange
        JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
        JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);
        JButton loginButton = getPrivateField(frame, "loginButton", JButton.class);
        AbstractButton clearButton = findButton(frame.getContentPane(), "Clear");

        runOnEdt(() -> {
            emailField.setText("changed@example.com");
            passwordField.setText("changed");
            statusLabel.setText("Something went wrong");
            loginButton.setEnabled(false);
        });

        // Act
        clickButton(clearButton);

        // Assert
        assertEquals("", emailField.getText());
        assertEquals("", new String(passwordField.getPassword()));
        assertEquals(" ", statusLabel.getText());
        assertTrue(loginButton.isEnabled());
    }
}

