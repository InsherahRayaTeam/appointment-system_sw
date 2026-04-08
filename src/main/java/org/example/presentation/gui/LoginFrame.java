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
     * Runs initialize ui for this class.
     */
    private void initializeUi() {
        setTitle("Appointment System - Login");
        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Appointment Scheduling System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel(""));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(170, 0, 0));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        loginButton = new JButton("Login");
        JButton clearButton = new JButton("Clear");
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> onLogin());
        clearButton.addActionListener(e -> onClear());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Runs on login for this class.
     */
    private void onLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        loginButton.setEnabled(false);

        AuthenticationAttemptResult result;
        try {
            result = authService.authenticateWithPolicy(new Credentials(email, password));
        } catch (IllegalArgumentException ex) {
            showError("Email and password are required.");
            loginButton.setEnabled(true);
            return;
        }

        if (result.isLocked()) {
            showError("Account locked. Try again in " + result.getRemainingLockSeconds() + " seconds.");
            loginButton.setEnabled(true);
            return;
        }

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Login successful: " + result.getAuthenticatedRole(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            appController.handleSuccessfulLogin(result);
            return;
        }

        if (result.getStatus() == LoginStatus.BLANK_INPUT) {
            showError("Email and password cannot be blank.");
        } else {
            showError("Invalid email or password. Attempts remaining: " + result.getAttemptsRemaining());
        }

        loginButton.setEnabled(true);
    }

    /**
     * Runs on clear for this class.
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
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Updates the status message.
     *
     * @param message message text to show or send
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message == null || message.trim().isEmpty() ? " " : message);
    }
}
