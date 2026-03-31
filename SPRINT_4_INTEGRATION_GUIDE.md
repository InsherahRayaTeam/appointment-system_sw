# Sprint 4 Integration Guide

## Overview
This guide explains how to integrate the new Sprint 4 services into your application's presentation layer and main flows.

## Dependency Injection Setup

### Application Bootstrap (Main.java or Spring/DI Container)

```java
// Initialize repositories
AppointmentRepository appointmentRepository = new InMemoryAppointmentRepository();
AppointmentBookingRepository appointmentBookingRepository = new InMemoryAppointmentBookingRepository();
AdminRepository adminRepository = new InMemoryAdminRepository();

// Initialize services
AuthEventLogger authEventLogger = new AuthEventLogger();
EventManager eventManager = new EventManager();
SessionManager sessionManager = new SessionManager(authEventLogger, eventManager);

// NEW - Create ScheduleService for slot management
ScheduleService scheduleService = new ScheduleService(
    appointmentRepository,
    sessionManager,
    eventManager
);

// NEW - Create UserAppointmentService for user flows
UserAppointmentService userAppointmentService = new UserAppointmentService(
    appointmentBookingRepository,
    appointmentRepository,
    sessionManager,
    eventManager
);

// NEW - Create AdminReservationService for admin flows
AdminReservationService adminReservationService = new AdminReservationService(
    appointmentBookingRepository,
    sessionManager,
    eventManager
);

// Existing services (unchanged)
AppointmentBookingService appointmentBookingService = new AppointmentBookingService(
    appointmentRepository,
    appointmentBookingRepository,
    sessionManager,
    adminRepository,
    eventManager
);

AppointmentService appointmentService = new AppointmentService(
    appointmentRepository,
    eventManager
);
```

## Presentation Layer Integration

### User Login Flow
```java
// This is the entry point for any user
String username = promptUsername();
String password = promptPassword();
UserRole role = authenticateUser(username, password);  // Returns USER or ADMIN

// Update session with role
sessionManager.login(username, role);
```

### Role-Based Menu System

```java
if (sessionManager.isAdmin()) {
    showAdminMenu(scheduleService, adminReservationService, appointmentBookingService);
} else if (sessionManager.isUser()) {
    showUserMenu(userAppointmentService, appointmentBookingService);
}
```

## Admin Menu Integration

### Slot Management Submenu
```java
void adminSlotManagement(ScheduleService scheduleService) {
    while (true) {
        System.out.println("=== Slot Management ===");
        System.out.println("1. Add Slot");
        System.out.println("2. Cancel Slot");
        System.out.println("3. View Available Slots");
        System.out.println("4. View Slot Statistics");
        System.out.println("5. Back");
        
        int choice = getUserChoice();
        switch (choice) {
            case 1 -> {
                String time = promptTime();
                if (scheduleService.addSlot(time)) {
                    System.out.println("✓ Slot created: " + time);
                } else {
                    System.out.println("✗ Slot already exists or invalid time");
                }
            }
            case 2 -> {
                String time = promptTime();
                if (scheduleService.cancelSlot(time)) {
                    System.out.println("✓ Slot cancelled: " + time);
                } else {
                    System.out.println("✗ Slot not found or already cancelled");
                }
            }
            case 3 -> {
                List<AppointmentSlot> slots = scheduleService.listAllSlots();
                displaySlots(slots);
            }
            case 4 -> {
                System.out.println("Total slots: " + scheduleService.getTotalSlotCount());
                System.out.println("Available: " + scheduleService.getAvailableSlotCount());
                System.out.println("Booked: " + scheduleService.getBookedSlotCount());
                System.out.println("Cancelled: " + scheduleService.getCancelledSlotCount());
            }
            case 5 -> break;
        }
    }
}
```

### Reservation Management Submenu
```java
void adminReservationManagement(AdminReservationService adminService) {
    while (true) {
        System.out.println("=== Reservation Management ===");
        System.out.println("1. View All Reservations");
        System.out.println("2. View Customer Reservations");
        System.out.println("3. Cancel Reservation");
        System.out.println("4. View Statistics");
        System.out.println("5. Back");
        
        int choice = getUserChoice();
        switch (choice) {
            case 1 -> {
                List<Appointment> all = adminService.getAllReservations();
                displayAppointments(all);
            }
            case 2 -> {
                String customer = promptCustomerName();
                List<Appointment> customerAppts = adminService.getReservationsByCustomer(customer);
                displayAppointments(customerAppts);
            }
            case 3 -> {
                String appointmentId = promptAppointmentId();
                String reason = promptCancellationReason();
                if (adminService.cancelReservation(appointmentId, reason)) {
                    System.out.println("✓ Reservation cancelled");
                } else {
                    System.out.println("✗ Reservation not found");
                }
            }
            case 4 -> {
                AdminReservationService.ReservationStats stats = adminService.getReservationStats();
                System.out.println("Total: " + stats.getTotalReservations());
                System.out.println("Active: " + stats.getActiveReservations());
                System.out.println("Cancelled: " + stats.getCancelledReservations());
            }
            case 5 -> break;
        }
    }
}
```

## User Menu Integration

### My Appointments Flow
```java
void userMyAppointments(UserAppointmentService userService) {
    while (true) {
        System.out.println("=== My Appointments ===");
        System.out.println("1. View All My Appointments");
        System.out.println("2. View Upcoming Appointments");
        System.out.println("3. View Past Appointments");
        System.out.println("4. Modify Appointment");
        System.out.println("5. Cancel Appointment");
        System.out.println("6. Back");
        
        int choice = getUserChoice();
        switch (choice) {
            case 1 -> {
                List<Appointment> all = userService.getMyAppointments();
                displayAppointments(all);
            }
            case 2 -> {
                List<Appointment> future = userService.getMyFutureAppointments();
                displayAppointments(future);
            }
            case 3 -> {
                List<Appointment> past = userService.getMyPastAppointments();
                displayAppointments(past);
            }
            case 4 -> {
                String appointmentId = promptAppointmentId();
                LocalDateTime newTime = promptDateTime();
                Integer newDuration = promptDuration();
                
                try {
                    if (userService.modifyAppointment(appointmentId, newTime, newDuration)) {
                        System.out.println("✓ Appointment modified");
                    } else {
                        System.out.println("✗ Modification failed");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("✗ Cannot modify past appointments");
                }
            }
            case 5 -> {
                String appointmentId = promptAppointmentId();
                
                try {
                    if (userService.cancelAppointment(appointmentId)) {
                        System.out.println("✓ Appointment cancelled");
                    } else {
                        System.out.println("✗ Cancellation failed");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("✗ Cannot cancel past appointments");
                }
            }
            case 6 -> break;
        }
    }
}
```

### User Booking Flow (Unchanged)
```java
void userBookAppointment(AppointmentBookingService bookingService, ScheduleService scheduleService) {
    // Show available slots (using NEW ScheduleService)
    List<AppointmentSlot> available = scheduleService.listAvailableSlots();
    System.out.println("Available slots:");
    for (AppointmentSlot slot : available) {
        System.out.println("  - " + slot.getTime());
    }
    
    // Get booking details
    String customerName = sessionManager.getCurrentUsername();  // Auto-filled for users
    String slotTime = promptSlotSelection();
    String duration = promptDuration();
    String participants = promptParticipants();
    
    // Book appointment (existing flow, unchanged)
    BookingStatus status = bookingService.bookAppointment(
        customerName,
        slotTime,
        duration,
        participants
    );
    
    displayBookingStatus(status);
}
```

## Error Handling Patterns

### Role-Based Access Errors
```java
try {
    // USER tries to use admin-only service
    scheduleService.addSlot("10:00");
} catch (IllegalStateException e) {
    System.out.println("✗ Access denied: " + e.getMessage());
    // "only ADMIN role can create slots"
}
```

### Time-Based Constraint Errors
```java
try {
    // USER tries to modify past appointment
    userService.modifyAppointment("past-appt-id", newTime, 90);
} catch (IllegalArgumentException e) {
    System.out.println("✗ Cannot modify past appointments");
}
```

### Login State Errors
```java
try {
    if (!sessionManager.isLoggedIn()) {
        userService.getMyAppointments();
    }
} catch (IllegalStateException e) {
    System.out.println("✗ User must be logged in");
}
```

## Notification Integration

### Subscribe to Events (Example)
```java
class AppointmentNotifier implements EventObserver {
    private EventManager eventManager;
    
    AppointmentNotifier(EventManager eventManager) {
        this.eventManager = eventManager;
        // Could register as typed observer when EventManager supports it
    }
    
    @Override
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        System.out.println("📅 " + event.getUsername() + " booked an appointment");
    }
    
    @Override
    public void onAppointmentModified(AppointmentModifiedEvent event) {
        System.out.println("✏️ " + event.getUsername() + " modified their appointment");
    }
    
    @Override
    public void onAppointmentCancelled(AppointmentCancelledEvent event) {
        System.out.println("❌ " + event.getUsername() + " cancelled an appointment");
    }
    
    @Override
    public void onSlotCancelled(SlotCancelledEvent event) {
        System.out.println("🔴 Admin " + event.getAdminUsername() + 
                          " cancelled slot: " + event.getSlotTime());
    }
}

// Register notifier
AppointmentNotifier notifier = new AppointmentNotifier(eventManager);
eventManager.subscribe(notifier);
```

## Testing Integration

### Integration Tests Example
```java
@ExtendWith(MockitoExtension.class)
class ApplicationFlowIntegrationTest {
    
    private ScheduleService scheduleService;
    private UserAppointmentService userService;
    private AdminReservationService adminService;
    private SessionManager sessionManager;
    
    @BeforeEach
    void setup() {
        // Initialize with test repositories
        AppointmentRepository appointmentRepo = new InMemoryAppointmentRepository();
        AppointmentBookingRepository bookingRepo = new InMemoryAppointmentBookingRepository();
        
        EventManager eventManager = new EventManager();
        sessionManager = new SessionManager(mock(AuthEventLogger.class), eventManager);
        
        scheduleService = new ScheduleService(appointmentRepo, sessionManager, eventManager);
        userService = new UserAppointmentService(bookingRepo, appointmentRepo, sessionManager, eventManager);
        adminService = new AdminReservationService(bookingRepo, sessionManager, eventManager);
    }
    
    @Test
    void testAdminCreatesSlotAndUserBooks() {
        // Admin logs in
        sessionManager.login("admin", UserRole.ADMIN);
        scheduleService.addSlot("10:00");
        
        // User logs in
        sessionManager.login("john", UserRole.USER);
        List<AppointmentSlot> available = scheduleService.listAvailableSlots();
        
        assertEquals(1, available.size());
        assertEquals("10:00", available.get(0).getTime());
    }
    
    @Test
    void testUserCannotModifyPastAppointment() {
        // Setup
        sessionManager.login("john", UserRole.USER);
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        
        // Would need to mock TimeProvider for precise control
        assertThrows(IllegalArgumentException.class, 
            () -> userService.modifyAppointment("past-id", LocalDateTime.now(), 60));
    }
}
```

## Migration Checklist

- [ ] **Step 1**: Update application bootstrap to create new service instances
- [ ] **Step 2**: Inject services into presentation layer controllers
- [ ] **Step 3**: Add admin slot management menu (using ScheduleService)
- [ ] **Step 4**: Add admin reservation management menu (using AdminReservationService)
- [ ] **Step 5**: Add user "My Appointments" menu (using UserAppointmentService)
- [ ] **Step 6**: Update user booking menu to use ScheduleService for slot listing
- [ ] **Step 7**: Implement error handling for all role-based exceptions
- [ ] **Step 8**: Add EventObserver implementation for notifications
- [ ] **Step 9**: Run full integration tests
- [ ] **Step 10**: Update documentation with new flows

## Key Points for Implementation

1. **Services are stateless** - Create once, reuse across requests
2. **SessionManager tracks state** - Check isLoggedIn() and role before each operation
3. **Exceptions indicate errors** - Catch and display IllegalStateException and IllegalArgumentException
4. **TimeProvider enables testing** - Inject mock time in tests for time-based operations
5. **Events are optional** - EventObserver integration is separate from core operations
6. **Backward compatible** - Existing AppointmentService and AppointmentBookingService unchanged

## Common Patterns

### Admin Workflow
```
Admin Login → Check Role (ADMIN) → Access Slot/Reservation Management → Operations
```

### User Workflow
```
User Login → Check Role (USER) → Access Booking/MyAppointments → Operations
```

### Error Workflow
```
Operation → Check Result → If Exception → Catch & Display Message → Retry or Back to Menu
```

## Performance Considerations

- **ScheduleService**: Loads all slots into memory on creation (InMemoryAppointmentRepository)
  - Suitable for moderate slot counts (<10,000)
  - For large datasets, implement pagination in repository

- **UserAppointmentService**: Queries appointments on demand
  - Each call fetches fresh data from repository
  - Consider caching for frequently accessed user data

- **AdminReservationService**: Full scan for statistics
  - O(n) operation to count active/cancelled
  - Consider materialized views or counters for high-volume systems

---

**Last Updated**: March 31, 2026
**Sprint**: 4
**Status**: Ready for Integration

