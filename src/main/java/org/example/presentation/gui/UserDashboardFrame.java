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
 * Represents user dashboard frame in the system.
 */
public class UserDashboardFrame extends JFrame {

    private static final String CARD_SLOTS = "SLOTS";
    private static final String CARD_BOOKING = "BOOKING";
    private static final String CARD_RESERVATIONS = "RESERVATIONS";
    private static final String CARD_CALENDAR = "CALENDAR";

    private final SystemUser user;
    private final ApplicationController appController;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final SlotsPanel slotsPanel;
    private final BookingPanel bookingPanel;
    private final ReservationsPanel reservationsPanel;
    private final CalendarPanel calendarPanel;

    /**
     * Creates a new user dashboard frame object with the given values.
     *
     * @param user user involved in this action
     * @param appController controller used for navigation and actions
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
        this.reservationsPanel = new ReservationsPanel(
                user,
                appController.getAppointmentService(),
                appController.getAppointmentBookingService()
        );
        this.bookingPanel = new BookingPanel(
                user,
                appController.getAppointmentService(),
                appController.getAppointmentBookingService(),
                this::refreshPanelsAfterBooking
        );
        this.calendarPanel = new CalendarPanel(
                appController.getAppointmentService(),
                () -> appController.getAppointmentBookingService().getReservationsForCustomer(user.getEmail())
        );

        initializeUi();
    }

    /**
     * Builds and wires the user dashboard UI.
     */
    private void initializeUi() {
        setTitle("User Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("User Dashboard - " + user.getEmail(), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        contentPanel.add(slotsPanel, CARD_SLOTS);
        contentPanel.add(bookingPanel, CARD_BOOKING);
        contentPanel.add(reservationsPanel, CARD_RESERVATIONS);
        contentPanel.add(calendarPanel, CARD_CALENDAR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton viewSlotsButton = new JButton("View Slots");
        viewSlotsButton.addActionListener(e -> showSlotsPanel());

        JButton bookButton = new JButton("Book Appointment");
        bookButton.addActionListener(e -> showBookingPanel());

        JButton myReservationsButton = new JButton("My Reservations");
        myReservationsButton.addActionListener(e -> showReservationsPanel());

        JButton calendarViewButton = new JButton("Calendar View");
        calendarViewButton.addActionListener(e -> showCalendarPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> onLogout());

        buttonPanel.add(viewSlotsButton);
        buttonPanel.add(bookButton);
        buttonPanel.add(myReservationsButton);
        buttonPanel.add(calendarViewButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        // Prime reservations data once so dashboard initialization is consistent.
        reservationsPanel.refreshData();

        showSlotsPanel();
    }

    /**
     * Shows slots panel to the user.
     */
    private void showSlotsPanel() {
        slotsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_SLOTS);
        slotsPanel.setVisible(true);
        bookingPanel.setVisible(false);
        reservationsPanel.setVisible(false);
        calendarPanel.setVisible(false);
    }

    /**
     * Shows booking panel to the user.
     */
    private void showBookingPanel() {
        bookingPanel.refreshData();
        cardLayout.show(contentPanel, CARD_BOOKING);
        slotsPanel.setVisible(false);
        bookingPanel.setVisible(true);
        reservationsPanel.setVisible(false);
        calendarPanel.setVisible(false);
    }

    /**
     * Shows reservations panel to the user.
     */
    private void showReservationsPanel() {
        reservationsPanel.refreshData();
        cardLayout.show(contentPanel, CARD_RESERVATIONS);
        slotsPanel.setVisible(false);
        bookingPanel.setVisible(false);
        reservationsPanel.setVisible(true);
        calendarPanel.setVisible(false);
    }

    /**
     * Shows calendar panel to the user.
     */
    private void showCalendarPanel() {
        calendarPanel.refreshCalendar();
        cardLayout.show(contentPanel, CARD_CALENDAR);
        slotsPanel.setVisible(false);
        bookingPanel.setVisible(false);
        reservationsPanel.setVisible(false);
        calendarPanel.setVisible(true);
    }

    /**
     * Reloads and updates panels after booking.
     */
    private void refreshPanelsAfterBooking() {
        slotsPanel.refreshData();
        reservationsPanel.refreshData();
        calendarPanel.refreshCalendar();
    }

    /**
     * Logs out the current user and opens the login screen.
     */
    private void onLogout() {
        appController.logoutAndOpenLogin();
    }
}
