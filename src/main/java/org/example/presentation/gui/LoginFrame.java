package org.example.presentation.gui;

import org.example.domain.Credentials;
import org.example.service.AdminAuthService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.LoginStatus;
import org.example.service.SessionManager;
import org.example.domain.AdminUser;
import org.example.domain.UserRole;

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
        setTitle("Appointment System - Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(560, 420));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        JPanel page = UiStyle.createPagePanel(new BorderLayout(UiStyle.SPACE_MD, UiStyle.SPACE_MD));
        page.setBorder(BorderFactory.createEmptyBorder(UiStyle.SPACE_MD, UiStyle.SPACE_MD, UiStyle.SPACE_MD, UiStyle.SPACE_MD));

        JPanel headerPanel = buildHeaderPanel();
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        JPanel card = buildFormCard();
        centerPanel.add(card);

        JPanel footerPanel = buildFooterPanel();

        page.add(headerPanel, BorderLayout.NORTH);
        page.add(centerPanel, BorderLayout.CENTER);
        page.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(page);

        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = UiStyle.createCardPanel(new BorderLayout());

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Appointment System");
        titleLabel.setFont(UiStyle.FONT_TITLE);

        JLabel subtitleLabel = new JLabel("Sign in with your account");
        subtitleLabel.setFont(UiStyle.FONT_BODY);
        subtitleLabel.setForeground(Color.DARK_GRAY);

        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(4));
        titleStack.add(subtitleLabel);

        headerPanel.add(titleStack, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel buildFormCard() {
        JPanel card = UiStyle.createCardPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(460, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UiStyle.SPACE_XS, UiStyle.SPACE_SM, UiStyle.SPACE_XS, UiStyle.SPACE_SM);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel usernameLabel = UiStyle.createFieldLabel("Username");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        card.add(usernameLabel, gbc);

        usernameField = new JTextField();
        UiStyle.styleTextComponent(usernameField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(usernameField, gbc);

        JLabel passwordLabel = UiStyle.createFieldLabel("Password");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        card.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        UiStyle.styleTextComponent(passwordField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        card.add(passwordField, gbc);

        return card;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = UiStyle.createCardPanel(new BorderLayout(UiStyle.SPACE_SM, UiStyle.SPACE_SM));

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UiStyle.FONT_BODY);
        errorLabel.setForeground(UiStyle.COLOR_ERROR);
        footer.add(errorLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyle.SPACE_XS, 0));
        buttonPanel.setOpaque(false);

        loginButton = UiStyle.createPrimaryButton("Login", this::onLogin);
        buttonPanel.add(loginButton);

        exitButton = UiStyle.createSecondaryButton("Exit", e -> onExit());
        buttonPanel.add(exitButton);

        footer.add(buttonPanel, BorderLayout.SOUTH);
        return footer;
    }

    private void onLogin(ActionEvent e) {
        clearError();
        setAuthenticating(true);

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setErrorMessage("Please enter both username and password.");
            setAuthenticating(false);
            return;
        }

        try {
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

            AdminUser authenticatedUser = result.getAuthenticatedUser();
            if (authenticatedUser != null) {
                sessionManager.login(authenticatedUser);
            } else {
                UserRole authenticatedRole = result.getAuthenticatedRole() == null
                        ? UserRole.USER
                        : result.getAuthenticatedRole();
                String authenticatedUsername = result.getAuthenticatedUsername() == null
                        ? username
                        : result.getAuthenticatedUsername();
                sessionManager.login(authenticatedUsername, authenticatedRole);
            }
            clearFields();
            onLoginSuccess.run();
        } finally {
            setAuthenticating(false);
        }
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

    private void setAuthenticating(boolean authenticating) {
        loginButton.setEnabled(!authenticating);
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
