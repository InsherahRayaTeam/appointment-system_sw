package org.example.presentation.gui;

import org.example.service.ForgotPasswordStatus;

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
 * Represents forgot password frame in the system.
 */
public class ForgotPasswordFrame extends JFrame {

    private final ApplicationController appController;

    private JTextField identifierField;
    private JTextField resetCodeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    /**
     * Creates a new forgot password frame object with the given values.
     *
     * @param appController controller used for navigation and actions
     */
    public ForgotPasswordFrame(ApplicationController appController) {
        this.appController = Objects.requireNonNull(appController, "appController cannot be null");
        initializeUi();
    }

    private void initializeUi() {
        setTitle("Appointment System - Forgot Password");
        setSize(500, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Reset Your Password", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(new JLabel("Username or Email:"));
        identifierField = new JTextField();
        formPanel.add(identifierField);

        formPanel.add(new JLabel("Reset Code:"));
        resetCodeField = new JTextField();
        formPanel.add(resetCodeField);

        formPanel.add(new JLabel("New Password:"));
        newPasswordField = new JPasswordField();
        formPanel.add(newPasswordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        formPanel.add(new JLabel(""));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(170, 0, 0));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton requestButton = new JButton("Send Reset Code");
        JButton resetButton = new JButton("Reset Password");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Login");

        requestButton.addActionListener(e -> onRequestResetCode());
        resetButton.addActionListener(e -> onResetPassword());
        clearButton.addActionListener(e -> clearInputs());
        backButton.addActionListener(e -> appController.openLoginFrame());

        buttonPanel.add(requestButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(requestButton);
    }

    private void onRequestResetCode() {
        ForgotPasswordStatus status = appController.requestPasswordReset(identifierField.getText());
        if (status == ForgotPasswordStatus.RESET_REQUESTED) {
            String message = "A reset code has been sent if the account exists.";
            statusLabel.setText(message);
            JOptionPane.showMessageDialog(this, message, "Forgot Password", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        showError(status);
    }

    private void onResetPassword() {
        ForgotPasswordStatus status = appController.resetPassword(
                identifierField.getText(),
                resetCodeField.getText(),
                new String(newPasswordField.getPassword()),
                new String(confirmPasswordField.getPassword())
        );

        if (status == ForgotPasswordStatus.PASSWORD_RESET_SUCCESS) {
            JOptionPane.showMessageDialog(
                    this,
                    "Password updated successfully. You can now log in.",
                    "Forgot Password",
                    JOptionPane.INFORMATION_MESSAGE
            );
            appController.openLoginFrame();
            return;
        }

        showError(status);
    }

    private void clearInputs() {
        identifierField.setText("");
        resetCodeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
        identifierField.requestFocusInWindow();
    }

    private void showError(ForgotPasswordStatus status) {
        String message = toMessage(status);
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Forgot Password Error", JOptionPane.ERROR_MESSAGE);
    }

    private String toMessage(ForgotPasswordStatus status) {
        if (status == null) {
            return "Unable to process password reset.";
        }

        return switch (status) {
            case BLANK_IDENTIFIER -> "Username or email is required.";
            case UNKNOWN_USER -> "Account was not found.";
            case INVALID_RESET_CODE -> "Reset code is invalid.";
            case BLANK_NEW_PASSWORD -> "New password is required.";
            case PASSWORD_MISMATCH -> "Password confirmation does not match.";
            case WEAK_PASSWORD -> "Password must be at least 6 characters and include letters and numbers.";
            case UPDATE_FAILED -> "Unable to update password. Please try again.";
            default -> "Unable to process password reset.";
        };
    }
}

