package org.example.presentation.gui;

import org.example.service.AppointmentService;
import org.example.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main dashboard window for GUI-based appointment system.
 *
 * Shows menu options for viewing slots, booking appointments,
 * and session actions. Manages switching between different panels.
 */
public class MainDashboardFrame extends JFrame {

    private final AppointmentService appointmentService;
    private final SessionManager sessionManager;
    private final Runnable onLogout;
    private final Runnable onExit;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SlotsPanel slotsPanel;
    private BookingPanel bookingPanel;

    private JLabel welcomeLabel;
    private JLabel subtitleLabel;

    /**
     * Creates main dashboard frame with appointment and session dependencies.
     *
     * @param appointmentService appointment service (service layer)
     * @param sessionManager session manager (service layer)
     * @param onLogout callback when user logs out
     * @param onExit callback when user exits the application
     */
    public MainDashboardFrame(
            AppointmentService appointmentService,
            SessionManager sessionManager,
            Runnable onLogout,
            Runnable onExit
    ) {
        this.appointmentService = appointmentService;
        this.sessionManager = sessionManager;
        this.onLogout = onLogout;
        this.onExit = onExit;

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Appointment Scheduling System - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        JPanel page = UiStyle.createPagePanel(new BorderLayout(14, 14));
        page.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = buildHeaderPanel();
        JPanel centerPanel = buildCenterPanel();

        page.add(headerPanel, BorderLayout.NORTH);
        page.add(centerPanel, BorderLayout.CENTER);

        setContentPane(page);
        showMenuPanel();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setBackground(UiStyle.COLOR_CARD);
        header.setBorder(UiStyle.createCardBorder());

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(UiStyle.FONT_SUBTITLE);

        subtitleLabel = new JLabel("Appointment administration dashboard");
        subtitleLabel.setFont(UiStyle.FONT_BODY);
        subtitleLabel.setForeground(Color.DARK_GRAY);

        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton logoutButton = new JButton("Logout");
        UiStyle.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(e -> handleLogout());

        JButton exitButton = new JButton("Exit");
        UiStyle.styleSecondaryButton(exitButton);
        exitButton.addActionListener(e -> handleExit());

        actions.add(logoutButton);
        actions.add(exitButton);

        header.add(textPanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenterPanel() {
        JPanel menuPanel = buildMenuPanel();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        slotsPanel = new SlotsPanel(appointmentService, this::showMenuPanel);
        bookingPanel = new BookingPanel(appointmentService, this::showMenuPanel);

        contentPanel.add(menuPanel, "MENU");
        contentPanel.add(slotsPanel, "SLOTS");
        contentPanel.add(bookingPanel, "BOOKING");

        return contentPanel;
    }

    private JPanel buildMenuPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UiStyle.COLOR_CARD);
        card.setBorder(UiStyle.createCardBorder());
        card.setPreferredSize(new Dimension(520, 320));

        JLabel title = new JLabel("Administrator Actions", SwingConstants.CENTER);
        title.setFont(UiStyle.FONT_TITLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Choose an action to continue", SwingConstants.CENTER);
        hint.setFont(UiStyle.FONT_BODY);
        hint.setForeground(Color.DARK_GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton viewSlotsButton = new JButton("View Available Slots");
        UiStyle.stylePrimaryButton(viewSlotsButton);
        viewSlotsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewSlotsButton.addActionListener(e -> showSlotsPanel());

        JButton bookButton = new JButton("Book Appointment");
        UiStyle.stylePrimaryButton(bookButton);
        bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookButton.addActionListener(e -> showBookingPanel());

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(hint);
        card.add(Box.createVerticalStrut(28));
        card.add(viewSlotsButton);
        card.add(Box.createVerticalStrut(14));
        card.add(bookButton);
        card.add(Box.createVerticalGlue());

        wrapper.add(card);
        return wrapper;
    }

    /**
     * Refreshes the welcome message to reflect the current session's username.
     */
    public void refreshForCurrentSession() {
        String username = sessionManager.getCurrentUsername();
        String safeName = (username == null || username.trim().isEmpty()) ? "Administrator" : username.trim();
        welcomeLabel.setText("Welcome, " + safeName);
    }

    private void showMenuPanel() {
        cardLayout.show(contentPanel, "MENU");
    }

    private void showSlotsPanel() {
        slotsPanel.refresh();
        cardLayout.show(contentPanel, "SLOTS");
    }

    private void showBookingPanel() {
        bookingPanel.refresh();
        cardLayout.show(contentPanel, "BOOKING");
    }

    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            sessionManager.logoutAndNotify();
            onLogout.run();
        }
    }

    private void handleExit() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Exit Appointment Scheduling System?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            onExit.run();
        }
    }
}
