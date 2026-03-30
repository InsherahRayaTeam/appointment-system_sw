package org.example.presentation.gui;

import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;
import org.example.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Login window for GUI-based appointment system.
 *
 * Allows user to enter username and password, delegates authentication
 * to AdminAuthService (service layer), and manages session via SessionManager.
 */
public class LoginFrame extends JFrame {

    private final AdminAuthService authService;
    private final SessionManager sessionManager;
    private final Runnable onLoginSuccess;
    private final Runnable onExitRequested;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel errorLabel;

    /**
     * Creates login frame with authentication and session dependencies.
     *
     * @param authService authentication service (service layer)
     * @param sessionManager session manager (service layer)
     * @param onLoginSuccess callback when login succeeds
     * @param onExitRequested callback when user wants to exit
     */
    public LoginFrame(
            AdminAuthService authService,
            SessionManager sessionManager,
            Runnable onLoginSuccess,
            Runnable onExitRequested
    ) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.onLoginSuccess = onLoginSuccess;
        this.onExitRequested = onExitRequested;

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Appointment Scheduling System - Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(520, 380));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        JPanel page = UiStyle.createPagePanel(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UiStyle.COLOR_CARD);
        card.setBorder(UiStyle.createCardBorder());
        card.setPreferredSize(new Dimension(420, 260));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Administrator Login", SwingConstants.CENTER);
        titleLabel.setFont(UiStyle.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Sign in to manage appointment scheduling", SwingConstants.CENTER);
        subtitleLabel.setFont(UiStyle.FONT_BODY);
        subtitleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 14, 8);
        card.add(subtitleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UiStyle.FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        card.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(UiStyle.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UiStyle.FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        card.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(UiStyle.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(passwordField, gbc);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UiStyle.FONT_BODY);
        errorLabel.setForeground(UiStyle.COLOR_ERROR);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 8, 8, 8);
        card.add(errorLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        loginButton = new JButton("Login");
        UiStyle.stylePrimaryButton(loginButton);
        loginButton.addActionListener(this::onLogin);
        buttonPanel.add(loginButton);

        exitButton = new JButton("Exit");
        UiStyle.styleSecondaryButton(exitButton);
        exitButton.addActionListener(e -> onExit());
        buttonPanel.add(exitButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 8, 0, 8);
        card.add(buttonPanel, gbc);

        page.add(card);
        setContentPane(page);

        getRootPane().setDefaultButton(loginButton);
    }

    private void onLogin(ActionEvent e) {
        clearError();

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setErrorMessage("Please enter both username and password.");
            return;
        }

        AuthenticationAttemptResult result = authService.authenticateWithPolicy(
                new Credentials(username, password)
        );

        if (result.isLocked()) {
            setErrorMessage("Too many failed attempts. Try again in "
                    + result.getRemainingLockSeconds() + " second(s).");
            passwordField.setText("");
            return;
        }

        if (!result.isSuccess()) {
            if (result.getStatus() == LoginStatus.BLANK_INPUT) {
                setErrorMessage("Please enter both username and password.");
            } else {
                setErrorMessage("Invalid username or password. Attempts remaining: "
                        + result.getAttemptsRemaining());
            }
            passwordField.setText("");
            return;
        }

        sessionManager.login(username);
        clearFields();
        onLoginSuccess.run();
    }

    private void onExit() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Exit Appointment Scheduling System?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            onExitRequested.run();
        }
    }

    private void setErrorMessage(String message) {
        errorLabel.setText(message == null ? " " : message);
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
    }

    /**
     * Resets the frame for another login attempt.
     * Called after logout to return to login screen.
     */
    public void reset() {
        clearFields();
        usernameField.requestFocusInWindow();
    }
}
