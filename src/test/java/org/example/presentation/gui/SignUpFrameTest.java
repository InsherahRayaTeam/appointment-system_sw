package org.example.presentation.gui;

import org.example.service.SignUpStatus;
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
class SignUpFrameTest extends GuiTestSupport {

    @Mock
    private ApplicationController appController;

    private SignUpFrame frame;

    @BeforeEach
    void setUp() {
        frame = new SignUpFrame(appController);
    }

    @AfterEach
    void tearDown() {
        disposeIfWindow(frame);
    }

    @Test
    void signUpButton_WithValidInput_DelegatesToController() {
        JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
        JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
        AbstractButton signUpButton = findButton(frame.getContentPane(), "Sign Up");

        when(appController.signUp("alice@example.com", "pass123"))
                .thenReturn(SignUpStatus.SUCCESS);

        runOnEdt(() -> {
            emailField.setText("alice@example.com");
            passwordField.setText("pass123");
        });

        clickButton(signUpButton);

        verify(appController).signUp("alice@example.com", "pass123");
        verify(appController).openLoginFrame();
    }

    @Test
    void signUpButton_WithValidationError_ShowsStatusMessage() {
        JTextField emailField = getPrivateField(frame, "emailField", JTextField.class);
        JPasswordField passwordField = getPrivateField(frame, "passwordField", JPasswordField.class);
        JLabel statusLabel = getPrivateField(frame, "statusLabel", JLabel.class);
        AbstractButton signUpButton = findButton(frame.getContentPane(), "Sign Up");

        when(appController.signUp("bad-email", "pass123"))
                .thenReturn(SignUpStatus.INVALID_EMAIL);

        runOnEdt(() -> {
            emailField.setText("bad-email");
            passwordField.setText("pass123");
        });

        clickButton(signUpButton);

        assertEquals("Please provide a valid email address.", statusLabel.getText());
    }

    @Test
    void backToLoginButton_DelegatesToController() {
        AbstractButton backButton = findButton(frame.getContentPane(), "Back to Login");

        clickButton(backButton);

        verify(appController).openLoginFrame();
    }
}


