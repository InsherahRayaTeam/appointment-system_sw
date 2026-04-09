package org.example.presentation.gui;

import org.example.service.SignUpStatus;

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
 * Represents sign up frame in the system.
 */
public class SignUpFrame extends JFrame {

    private final ApplicationController appController;

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    /**
     * Creates a new sign up frame object with the given values.
     *
     * @param appController controller used for navigation and actions
     */
    public SignUpFrame(ApplicationController appController) {
        this.appController = Objects.requireNonNull(appController, "appController cannot be null");
        initializeUi();
    }

    private void initializeUi() {
        setTitle("Appointment System - Sign Up");
        setSize(460, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Create Your Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        formPanel.add(new JLabel(""));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(170, 0, 0));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton signUpButton = new JButton("Sign Up");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Login");

        signUpButton.addActionListener(e -> onSignUp());
        clearButton.addActionListener(e -> clearInputs());
        backButton.addActionListener(e -> appController.openLoginFrame());

        buttonPanel.add(signUpButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(signUpButton);
    }

    private void onSignUp() {
        SignUpStatus status = appController.registerUser(
                usernameField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmPasswordField.getPassword())
        );

        if (status == SignUpStatus.SUCCESS) {
            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully. You can now log in.",
                    "Sign Up",
                    JOptionPane.INFORMATION_MESSAGE
            );
            appController.openLoginFrame();
            return;
        }

        String message = toMessage(status);
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Sign Up Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearInputs() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
        usernameField.requestFocusInWindow();
    }

    private String toMessage(SignUpStatus status) {
        if (status == null) {
            return "Unable to create account.";
        }

        return switch (status) {
            case BLANK_USERNAME -> "Username is required.";
            case BLANK_EMAIL -> "Email is required.";
            case BLANK_PASSWORD -> "Password is required.";
            case PASSWORD_MISMATCH -> "Password confirmation does not match.";
            case INVALID_EMAIL -> "Please provide a valid email address.";
            case WEAK_PASSWORD -> "Password must be at least 6 characters and include letters and numbers.";
            case USERNAME_ALREADY_EXISTS -> "Username already exists. Please choose another username.";
            case EMAIL_ALREADY_EXISTS -> "An account with this email already exists.";
            default -> "Unable to create account.";
        };
    }
}

