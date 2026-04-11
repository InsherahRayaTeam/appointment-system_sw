package org.example.presentation.gui;

import org.example.domain.SystemUser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.FlowLayout;

/**
 * Represents admin dashboard frame in the system.
 */
public class AdminDashboardFrame extends JFrame {

    private static final String CARD_SLOTS = "SLOTS";
    private static final String CARD_RESERVATIONS = "RESERVATIONS";
    private static final String CARD_CALENDAR = "CALENDAR";

    private final SystemUser user;
    private final ApplicationController appController;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final SlotsPanel slotsPanel;
    private final AdminReservationsPanel adminReservationsPanel;
    private final CalendarPanel calendarPanel;

    /**
     * Creates a new admin dashboard frame object with the given values.
     *
     * @param user user involved in this action
     * @param appController controller used for navigation and actions
     */
    public AdminDashboardFrame(
            SystemUser user,
            ApplicationController appController
    ) {
        this.user = user;
        this.appController = appController;

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.slotsPanel = new SlotsPanel(appController.getAppointmentService(), true);
        this.adminReservationsPanel = new AdminReservationsPanel(
                appController.getAppointmentBookingService(),
                appController.getAppointmentService()
        );
        this.calendarPanel = new CalendarPanel(
                appController.getAppointmentService(),
                () -> appController.getAppointmentBookingService().getManagedReservations()
        );

        initializeUi();
    }

    /**
     * Runs initialize ui for this class.
     */
    private void initializeUi() {
        setTitle("Admin Dashboard");
        setSize(900, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Admin Dashboard - " + user.getEmail(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        contentPanel.add(slotsPanel, CARD_SLOTS);
        contentPanel.add(adminReservationsPanel, CARD_RESERVATIONS);
        contentPanel.add(calendarPanel, CARD_CALENDAR);
        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton viewSlotsButton = new JButton("View Slots");
        viewSlotsButton.addActionListener(e -> showSlotsPanel());

        JButton manageReservationsButton = new JButton("Manage Reservations");
        manageReservationsButton.addActionListener(e -> showReservationsPanel());

        JButton calendarViewButton = new JButton("Calendar View");
        calendarViewButton.addActionListener(e -> showCalendarPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> onLogout());

        buttonPanel.add(viewSlotsButton);
        buttonPanel.add(manageReservationsButton);
        buttonPanel.add(calendarViewButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);

        showSlotsPanel();
    }

    /**
     * Shows slots panel to the user.
     */
    private void showSlotsPanel() {
        slotsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_SLOTS);
        slotsPanel.setVisible(true);
        adminReservationsPanel.setVisible(false);
        calendarPanel.setVisible(false);
    }

    /**
     * Shows reservations panel to the user.
     */
    private void showReservationsPanel() {
        adminReservationsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_RESERVATIONS);
        slotsPanel.setVisible(false);
        adminReservationsPanel.setVisible(true);
        calendarPanel.setVisible(false);
    }

    /**
     * Shows calendar panel to the user.
     */
    private void showCalendarPanel() {
        calendarPanel.refreshCalendar();
        cardLayout.show(contentPanel, CARD_CALENDAR);
        slotsPanel.setVisible(false);
        adminReservationsPanel.setVisible(false);
        calendarPanel.setVisible(true);
    }

    /**
     * Runs on logout for this class.
     */
    private void onLogout() {
        appController.logoutAndOpenLogin();
    }
}
