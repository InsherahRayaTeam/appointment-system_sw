package org.example.presentation.gui;

import org.example.service.AppointmentService;
import org.example.service.AppointmentBookingService;
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
 *
 * @author appointment-system
 * @version 1.0
 */
public class MainDashboardFrame extends JFrame {

    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final SessionManager sessionManager;
    private final Runnable onLogout;
    private final Runnable onExit;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SlotsPanel slotsPanel;
    private BookingPanel bookingPanel;
    private ReservationsPanel reservationsPanel;

    private JLabel welcomeLabel;
    private JLabel subtitleLabel;
    private JLabel statusLabel;
    private JButton manageReservationsButton;
    private JLabel menuSpacerLabel;

    /**
     * Creates main dashboard frame with appointment and session dependencies.
     *
     * @param appointmentService appointment service (service layer)
     * @param appointmentBookingService booking service (service layer)
     * @param sessionManager session manager (service layer)
     * @param onLogout callback when user logs out
     * @param onExit callback when user exits the application
     */
    public MainDashboardFrame(
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            SessionManager sessionManager,
            Runnable onLogout,
            Runnable onExit
    ) {
        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
        this.sessionManager = sessionManager;
        this.onLogout = onLogout;
        this.onExit = onExit;

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Appointment System - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(860, 560));

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
        JPanel footerPanel = buildFooterPanel();

        page.add(headerPanel, BorderLayout.NORTH);
        page.add(centerPanel, BorderLayout.CENTER);
        page.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(page);
        pack();
        setLocationRelativeTo(null);
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

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = UiStyle.createCardPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UiStyle.FONT_BODY);
        statusLabel.setForeground(UiStyle.COLOR_INFO);
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private JPanel buildCenterPanel() {
        JPanel menuPanel = buildMenuPanel();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        slotsPanel = new SlotsPanel(appointmentService, this::showMenuPanel);
        bookingPanel = new BookingPanel(appointmentService, appointmentBookingService, this::showMenuPanel);
        reservationsPanel = new ReservationsPanel(appointmentBookingService, appointmentService, this::showMenuPanel);

        contentPanel.add(menuPanel, "MENU");
        contentPanel.add(slotsPanel, "SLOTS");
        contentPanel.add(bookingPanel, "BOOKING");
        contentPanel.add(reservationsPanel, "RESERVATIONS");

        return contentPanel;
    }

    private JPanel buildMenuPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = UiStyle.createCardPanel(new BorderLayout(UiStyle.SPACE_MD, UiStyle.SPACE_MD));
        card.setPreferredSize(new Dimension(560, 340));

        JLabel title = new JLabel("Dashboard", SwingConstants.CENTER);
        title.setFont(UiStyle.FONT_TITLE);

        JLabel hint = new JLabel("Choose an action to continue", SwingConstants.CENTER);
        hint.setFont(UiStyle.FONT_BODY);
        hint.setForeground(Color.DARK_GRAY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(UiStyle.SPACE_XS));
        titleBlock.add(hint);

        JPanel buttonGrid = new JPanel(new GridLayout(3, 2, UiStyle.SPACE_SM, UiStyle.SPACE_SM));
        buttonGrid.setOpaque(false);

        JButton viewSlotsButton = UiStyle.createPrimaryButton("View Slots", e -> showSlotsPanel());
        JButton bookButton = UiStyle.createPrimaryButton("Book Appointment", e -> showBookingPanel());
        manageReservationsButton = UiStyle.createPrimaryButton("Manage Reservations", e -> showReservationsPanel());
        JButton logoutButton = UiStyle.createSecondaryButton("Logout", e -> handleLogout());
        JButton exitButton = UiStyle.createSecondaryButton("Exit", e -> handleExit());
        menuSpacerLabel = new JLabel();

        buttonGrid.add(viewSlotsButton);
        buttonGrid.add(bookButton);
        buttonGrid.add(manageReservationsButton);
        buttonGrid.add(menuSpacerLabel);
        buttonGrid.add(logoutButton);
        buttonGrid.add(exitButton);

        card.add(titleBlock, BorderLayout.NORTH);
        card.add(buttonGrid, BorderLayout.CENTER);

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
        subtitleLabel.setText(sessionManager.isAdmin()
                ? "Appointment administration dashboard"
                : "Appointment user dashboard");

        boolean adminSession = sessionManager.isAdmin();
        manageReservationsButton.setVisible(adminSession);
        menuSpacerLabel.setVisible(adminSession);
        setStatus("Logged in as " + safeName + ".");
    }

    /**
     * Returns dashboard content to the default menu state.
     */
    public void resetToMenu() {
        showMenuPanel();
    }

    private void showMenuPanel() {
        cardLayout.show(contentPanel, "MENU");
        setStatus("Dashboard ready.");
    }

    private void showSlotsPanel() {
        slotsPanel.refresh();
        cardLayout.show(contentPanel, "SLOTS");
        setStatus("Viewing available appointment slots.");
    }

    private void showBookingPanel() {
        bookingPanel.refresh();
        cardLayout.show(contentPanel, "BOOKING");
        setStatus("Booking form ready.");
    }

    private void showReservationsPanel() {
        if (!sessionManager.isAdmin()) {
            setStatus("Only administrators can manage reservations.");
            showMenuPanel();
            return;
        }
        reservationsPanel.refresh();
        cardLayout.show(contentPanel, "RESERVATIONS");
        setStatus("Reservation management ready.");
    }

    private void setStatus(String message) {
        statusLabel.setText(message == null || message.trim().isEmpty() ? "Ready" : message);
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
