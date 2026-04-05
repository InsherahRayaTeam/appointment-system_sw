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
 * Basic admin dashboard screen.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminDashboardFrame extends JFrame {

    private static final String CARD_SLOTS = "SLOTS";
    private static final String CARD_RESERVATIONS = "RESERVATIONS";

    private final SystemUser user;
    private final ApplicationController appController;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final SlotsPanel slotsPanel;
    private final AdminReservationsPanel adminReservationsPanel;

    /**
     * Creates the admin dashboard frame.
     *
     * @param user authenticated admin user
     * @param appController app controller used for navigation and service access
     */
    public AdminDashboardFrame(
            SystemUser user,
            ApplicationController appController
    ) {
        this.user = user;
        this.appController = appController;

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.slotsPanel = new SlotsPanel(appController.getAppointmentService());
        this.adminReservationsPanel = new AdminReservationsPanel(
                appController.getAppointmentBookingService(),
                appController.getAppointmentService()
        );

        initializeUi();
    }

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
        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton viewSlotsButton = new JButton("View Slots");
        viewSlotsButton.addActionListener(e -> showSlotsPanel());

        JButton manageReservationsButton = new JButton("Manage Reservations");
        manageReservationsButton.addActionListener(e -> showReservationsPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> onLogout());

        buttonPanel.add(viewSlotsButton);
        buttonPanel.add(manageReservationsButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);

        showSlotsPanel();
    }

    private void showSlotsPanel() {
        slotsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_SLOTS);
    }

    private void showReservationsPanel() {
        adminReservationsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_RESERVATIONS);
    }

    private void onLogout() {
        appController.logoutAndOpenLogin();
    }
}