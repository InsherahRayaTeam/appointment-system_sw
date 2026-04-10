package org.example.presentation.gui;

import org.example.service.ForgotPasswordStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordFrameTest extends GuiTestSupport {

    @Mock
    private ApplicationController appController;

    private ForgotPasswordFrame frame;

    @BeforeEach
    void setUp() {
        frame = new ForgotPasswordFrame(appController);
    }

    @AfterEach
    void tearDown() {
        disposeIfWindow(frame);
    }

    @Test
    void resetPasswordButton_WithValidInput_DelegatesToController() {
        JTextField identifierField = getPrivateField(frame, "identifierField", JTextField.class);
        JPasswordField newPasswordField = getPrivateField(frame, "newPasswordField", JPasswordField.class);
        AbstractButton resetButton = findButton(frame.getContentPane(), "Reset Password");

        when(appController.resetPassword("alice@example.com", "new1234"))
                .thenReturn(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS);

        runOnEdt(() -> {
            identifierField.setText("alice@example.com");
            newPasswordField.setText("new1234");
        });

        clickButton(resetButton);

        verify(appController).resetPassword("alice@example.com", "new1234");
        verify(appController).openLoginFrame();
    }

    @Test
    void resetPasswordButton_WithWeakPassword_ShowsErrorMessage() {
        JTextField identifierField = getPrivateField(frame, "identifierField", JTextField.class);
        JPasswordField newPasswordField = getPrivateField(frame, "newPasswordField", JPasswordField.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);
        AbstractButton resetButton = findButton(frame.getContentPane(), "Reset Password");

        when(appController.resetPassword("alice@example.com", "weak"))
                .thenReturn(ForgotPasswordStatus.WEAK_PASSWORD);

        runOnEdt(() -> {
            identifierField.setText("alice@example.com");
            newPasswordField.setText("weak");
        });

        clickButton(resetButton);

        assertEquals("Password must be at least 6 characters and include letters and numbers.", statusLabel.getText());
    }

    @Test
    void backToLoginButton_DelegatesToController() {
        AbstractButton backButton = findButton(frame.getContentPane(), "Back to Login");

        clickButton(backButton);

        verify(appController).openLoginFrame();
    }
}

