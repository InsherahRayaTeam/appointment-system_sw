package org.example.presentation.gui;

import org.example.service.AdminAuthService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;
import org.example.domain.Credentials;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;

/**
 * Represents login frame in the system.
 */
public class LoginFrame extends JFrame {

    private static final String LOGIN_SUCCESS_TITLE = GuiText.LOGIN_SUCCESS_TITLE;

    private final AdminAuthService authService;
    private final ApplicationController appController;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    /**
     * Creates a new login frame object with the given values.
     *
     * @param authService service used to run business logic
     * @param appController controller used for navigation and actions
     */
    public LoginFrame(
            AdminAuthService authService,
            ApplicationController appController
    ) {
        this.authService = Objects.requireNonNull(authService, "authService cannot be null");
        this.appController = Objects.requireNonNull(appController, "appController cannot be null");

        initializeUi();
    }

    /**
     * Builds and wires the login screen UI.
     */
    private void initializeUi() {
        setTitle(GuiText.APP_LOGIN_TITLE);
        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(GuiText.APP_LOGIN_HEADER, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(new JLabel(GuiText.EMAIL_LABEL));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel(GuiText.PASSWORD_LABEL));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel(""));
        statusLabel = new JLabel(GuiText.EMPTY_STATUS_TEXT);
        statusLabel.setForeground(new Color(170, 0, 0));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        loginButton = new JButton(GuiText.LOGIN_BUTTON);
        JButton signUpButton = new JButton(GuiText.SIGN_UP_BUTTON);
        JButton forgotPasswordButton = new JButton(GuiText.FORGOT_PASSWORD_BUTTON);
        JButton clearButton = new JButton(GuiText.CLEAR_BUTTON);
        JButton exitButton = new JButton(GuiText.EXIT_BUTTON);

        loginButton.addActionListener(e -> onLogin());
        signUpButton.addActionListener(e -> appController.openSignUpFrame());
        forgotPasswordButton.addActionListener(e -> appController.openForgotPasswordFrame());
        clearButton.addActionListener(e -> onClear());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        buttonPanel.add(forgotPasswordButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Handles the login button click.
     */
    private void onLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        loginButton.setEnabled(false);

        AuthenticationAttemptResult result;
        try {
            result = authService.authenticateWithPolicy(new Credentials(email, password));
        } catch (IllegalArgumentException ex) {
            showError(GuiText.EMAIL_AND_PASSWORD_REQUIRED);
            loginButton.setEnabled(true);
            return;
        }

        if (result.isLocked()) {
            showError(GuiText.ACCOUNT_LOCKED_PREFIX + result.getRemainingLockSeconds() + " seconds.");
            loginButton.setEnabled(true);
            return;
        }

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(
                    this,
                    GuiText.LOGIN_SUCCESS_PREFIX + result.getAuthenticatedRole(),
                    LOGIN_SUCCESS_TITLE,
                    JOptionPane.INFORMATION_MESSAGE
            );
            appController.handleSuccessfulLogin(result);
            return;
        }

        if (result.getStatus() == LoginStatus.BLANK_INPUT) {
            showError(GuiText.EMAIL_AND_PASSWORD_CANNOT_BE_BLANK);
        } else {
            showError(GuiText.INVALID_CREDENTIALS_PREFIX + result.getAttemptsRemaining());
        }

        loginButton.setEnabled(true);
    }

    /**
     * Clears input fields and resets the status message.
     */
    private void onClear() {
        emailField.setText("");
        passwordField.setText("");
        setStatusMessage(" ");
        loginButton.setEnabled(true);
        emailField.requestFocusInWindow();
    }

    /**
     * Shows error to the user.
     *
     * @param message message text to show or send
     */
    private void showError(String message) {
        setStatusMessage(message);
        JOptionPane.showMessageDialog(this, message, GuiText.LOGIN_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Updates the status message.
     *
     * @param message message text to show or send
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message == null || message.trim().isEmpty() ? GuiText.EMPTY_STATUS_TEXT : message);
    }
}
