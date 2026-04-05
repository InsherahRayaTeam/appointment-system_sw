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
 * Basic regular-user dashboard screen.
 *
 * @author appointment-system
 * @version 1.0
 */
public class UserDashboardFrame extends JFrame {

    private static final String CARD_SLOTS = "SLOTS";
    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_RESERVATIONS = "RESERVATIONS";

    private final SystemUser user;
    private final ApplicationController appController;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final SlotsPanel slotsPanel;
    private final BookingPanel bookingPanel;
    private final ReservationsPanel reservationsPanel;

    /**
     * Creates the user dashboard frame.
     *
     * @param user authenticated regular user
     * @param appController app controller used for navigation and service access
     */
    public UserDashboardFrame(
            SystemUser user,
            ApplicationController appController
    ) {
        this.user = user;
        this.appController = appController;

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.slotsPanel = new SlotsPanel(appController.getAppointmentService());
        this.reservationsPanel = new ReservationsPanel(user, appController.getAppointmentBookingService());
        this.bookingPanel = new BookingPanel(
                user,
                appController.getAppointmentService(),
                appController.getAppointmentBookingService(),
                this::refreshPanelsAfterBooking
        );

        initializeUi();
    }

    private void initializeUi() {
        setTitle("User Dashboard");
        setSize(900, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("User Dashboard - " + user.getEmail(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        contentPanel.add(slotsPanel, CARD_SLOTS);
        contentPanel.add(bookingPanel, CARD_BOOKING);
        contentPanel.add(reservationsPanel, CARD_RESERVATIONS);
        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton viewSlotsButton = new JButton("View Slots");
        viewSlotsButton.addActionListener(e -> showSlotsPanel());

        JButton bookButton = new JButton("Book Appointment");
        bookButton.addActionListener(e -> showBookingPanel());

        JButton myReservationsButton = new JButton("My Reservations");
        myReservationsButton.addActionListener(e -> showReservationsPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> onLogout());

        buttonPanel.add(viewSlotsButton);
        buttonPanel.add(bookButton);
        buttonPanel.add(myReservationsButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);

        showSlotsPanel();
    }

    private void showSlotsPanel() {
        slotsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_SLOTS);
    }

    private void showBookingPanel() {
        bookingPanel.refreshData();
        cardLayout.show(contentPanel, CARD_BOOKING);
    }

    private void showReservationsPanel() {
        reservationsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_RESERVATIONS);
    }

    private void refreshPanelsAfterBooking() {
        slotsPanel.refreshData();
        reservationsPanel.refreshData();
    }

    private void onLogout() {
        appController.logoutAndOpenLogin();
    }
}