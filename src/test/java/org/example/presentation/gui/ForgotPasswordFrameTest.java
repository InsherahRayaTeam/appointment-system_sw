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
    void sendResetCodeButton_WithKnownUser_DelegatesToController() {
        JTextField identifierField = getPrivateField(frame, "identifierField", JTextField.class);
        AbstractButton requestButton = findButton(frame.getContentPane(), "Send Reset Code");

        when(appController.requestPasswordReset("alice@example.com")).thenReturn(ForgotPasswordStatus.RESET_REQUESTED);

        runOnEdt(() -> identifierField.setText("alice@example.com"));

        clickButton(requestButton);

        verify(appController).requestPasswordReset("alice@example.com");
    }

    @Test
    void resetPasswordButton_WithValidInput_DelegatesToController() {
        JTextField identifierField = getPrivateField(frame, "identifierField", JTextField.class);
        JTextField resetCodeField = getPrivateField(frame, "resetCodeField", JTextField.class);
        JPasswordField newPasswordField = getPrivateField(frame, "newPasswordField", JPasswordField.class);
        JPasswordField confirmPasswordField = getPrivateField(frame, "confirmPasswordField", JPasswordField.class);
        AbstractButton resetButton = findButton(frame.getContentPane(), "Reset Password");

        when(appController.resetPassword("alice@example.com", "123456", "new1234", "new1234"))
                .thenReturn(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS);

        runOnEdt(() -> {
            identifierField.setText("alice@example.com");
            resetCodeField.setText("123456");
            newPasswordField.setText("new1234");
            confirmPasswordField.setText("new1234");
        });

        clickButton(resetButton);

        verify(appController).resetPassword("alice@example.com", "123456", "new1234", "new1234");
        verify(appController).openLoginFrame();
    }

    @Test
    void resetPasswordButton_WithInvalidCode_ShowsErrorMessage() {
        JTextField identifierField = getPrivateField(frame, "identifierField", JTextField.class);
        JTextField resetCodeField = getPrivateField(frame, "resetCodeField", JTextField.class);
        JPasswordField newPasswordField = getPrivateField(frame, "newPasswordField", JPasswordField.class);
        JPasswordField confirmPasswordField = getPrivateField(frame, "confirmPasswordField", JPasswordField.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);
        AbstractButton resetButton = findButton(frame.getContentPane(), "Reset Password");

        when(appController.resetPassword("alice@example.com", "bad", "new1234", "new1234"))
                .thenReturn(ForgotPasswordStatus.INVALID_RESET_CODE);

        runOnEdt(() -> {
            identifierField.setText("alice@example.com");
            resetCodeField.setText("bad");
            newPasswordField.setText("new1234");
            confirmPasswordField.setText("new1234");
        });

        clickButton(resetButton);

        assertEquals("Reset code is invalid.", statusLabel.getText());
    }

    @Test
    void backToLoginButton_DelegatesToController() {
        AbstractButton backButton = findButton(frame.getContentPane(), "Back to Login");

        clickButton(backButton);

        verify(appController).openLoginFrame();
    }
}

