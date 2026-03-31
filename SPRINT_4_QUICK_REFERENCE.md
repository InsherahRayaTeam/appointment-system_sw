# Sprint 4 Quick Reference Guide

## What Changed?

### New Classes/Interfaces Created (12 files)

**Domain Layer**:
- `TimeSlotStatus.java` - Enum for slot states (AVAILABLE, BOOKED, CANCELLED)
- `AppointmentType.java` - Enum for appointment types (URGENT, FOLLOW_UP, ASSESSMENT, VIRTUAL, IN_PERSON, INDIVIDUAL, GROUP)

**Service Layer**:
- `TimeProvider.java` - Interface for injectable time source
- `SystemTimeProvider.java` - Production time provider
- `ScheduleService.java` - Manages slots (ADMIN-only operations)
- `UserAppointmentService.java` - User appointment operations (book, modify, cancel own appointments)
- `AdminReservationService.java` - Admin reservation management (view, manage all appointments)

**Notification Layer**:
- `AppointmentBookedEvent.java` - Event when appointment is booked
- `AppointmentModifiedEvent.java` - Event when appointment is modified
- `AppointmentCancelledEvent.java` - Event when appointment is cancelled
- `SlotCancelledEvent.java` - Event when slot is cancelled by admin
- `EventObserver.java` - Interface for typed event observations

**Test Layer**:
- `TimeProviderTest.java` - Tests for TimeProvider
- `ScheduleServiceTest.java` - Tests for ScheduleService
- `UserAppointmentServiceTest.java` - Tests for UserAppointmentService
- `AdminReservationServiceTest.java` - Tests for AdminReservationService

### Modified Classes (4 files)

**service/SessionManager.java**:
- Added TimeProvider field
- Added overloaded constructor: `SessionManager(authEventLogger, eventManager, timeProvider)`
- Replaced `LocalDateTime.now()` with `timeProvider.now()`
- Maintains backward compatibility with default constructor

**repository/AppointmentBookingRepository.java**:
- Added `List<Appointment> findByCustomerName(String customerName)`
- Added `boolean remove(String appointmentId)`

**repository/InMemoryAppointmentBookingRepository.java**:
- Implemented `findByCustomerName(String customerName)`
- Implemented `remove(String appointmentId)`

**test/service/SessionManagerTest.java**:
- Added Mockito @ExtendWith annotation
- Added TimeProvider mock tests
- Tests for injected time verification

## Architecture Changes

### Before: Monolithic AppointmentService
```
AppointmentService
├── Slot management
├── Booking operations
└── Legacy operations (mixed)
```

### After: Separated Services

```
ADMIN Operations:
├── ScheduleService (slot management)
│   ├── Add slots (ADMIN only)
│   ├── Cancel slots (ADMIN only)
│   └── List slots (all authenticated users)
│
└── AdminReservationService (reservation management)
    ├── View all reservations (ADMIN only)
    ├── View customer reservations (ADMIN only)
    ├── Cancel reservations (ADMIN only)
    └── View statistics (ADMIN only)

USER Operations:
└── UserAppointmentService (personal appointments)
    ├── View own appointments
    ├── Modify own appointments (future only)
    ├── Cancel own appointments (future only)
    └── View future/past appointments

BOOKING Operations:
└── AppointmentBookingService (existing, unchanged)
    └── Book appointments (customer flow)
```

## Role-Based Access Control

### SessionManager Role Support

```java
// Check roles
sessionManager.isAdmin()   // true if current user is ADMIN
sessionManager.isUser()    // true if current user is USER

// Login as user
sessionManager.login("john", UserRole.USER);     // USER role
sessionManager.login("admin", UserRole.ADMIN);   // ADMIN role
```

### Each Service Enforces Its Rules

```java
// ScheduleService.addSlot() 
if (!sessionManager.isAdmin()) 
    throw new IllegalStateException("only ADMIN role can create slots");

// UserAppointmentService.getMyAppointments()
if (sessionManager.isAdmin()) 
    throw new IllegalStateException("ADMIN should use AdminReservationService");
```

## Time Provider Injection

### Production Usage (Default)
```java
// Automatically uses SystemTimeProvider
SessionManager sm = new SessionManager(logger, eventManager);
```

### Testing Usage
```java
// Inject mock time for testing
TimeProvider mockTime = mock(TimeProvider.class);
when(mockTime.now()).thenReturn(testDateTime);

SessionManager sm = new SessionManager(logger, eventManager, mockTime);
```

## Key Constraints

### Future-Only Operations
Users can only modify/cancel FUTURE appointments:
```java
// Only allowed if appointment.startTime > timeProvider.now()
userService.modifyAppointment(id, newTime, newDuration);
userService.cancelAppointment(id);

// Throws IllegalArgumentException for past appointments
```

### Ownership Verification
Users can only access their own appointments:
```java
// Automatically verifies current user is the customer
userService.getMyAppointment(id);      // ✓ Only own appointments
adminService.getReservation(id);       // ✓ Any appointment (admin only)
```

### Role Separation
```java
// Each service is single-role
userService.getMyAppointments();       // USER only
adminService.getAllReservations();     // ADMIN only
scheduleService.cancelSlot("10:00");   // ADMIN only
```

## Event System

### Observer Pattern Enhancement

**Old** (Generic String):
```java
eventManager.notifyObservers("Appointment booked");  // Too generic
```

**New** (Typed Events):
```java
AppointmentBookedEvent event = new AppointmentBookedEvent(appt, username);
// Observers can handle specific event types
observer.onAppointmentBooked(event);
observer.onAppointmentCancelled(cancelledEvent);
observer.onSlotCancelled(slotCancelledEvent);
```

## Repository Extensions

### New Query Methods

```java
// Find all appointments for a customer (used for "My Appointments")
List<Appointment> apps = repo.findByCustomerName("john");

// Remove an appointment
repo.remove("appointment-123");

// Existing methods still work
repo.findAll();
repo.findById(id);
repo.save(appointment);
repo.update(appointment);
```

## Testing Examples

### Test Setup Pattern
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private EventManager eventManager;
    
    @BeforeEach
    void setup() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
    }
}
```

### Test Role Verification
```java
@Test
void testAdminOnlyOperation() {
    when(sessionManager.isAdmin()).thenReturn(false);
    
    assertThrows(IllegalStateException.class, 
        () -> scheduleService.addSlot("10:00"));
}
```

### Test Time-Based Constraints
```java
@Test
void testCannotModifyPastAppointment() {
    LocalDateTime pastTime = now.minusHours(1);
    Appointment past = new Appointment(..., pastTime, ...);
    
    when(appointmentBookingRepository.findById(id)).thenReturn(Optional.of(past));
    
    assertThrows(IllegalArgumentException.class,
        () -> userService.modifyAppointment(id, futureTime, 60));
}
```

## Migration Checklist

When integrating these changes:

- [ ] Update all SessionManager instantiations (backward compatible)
- [ ] Update presentation layer to use ScheduleService for slot management
- [ ] Update presentation layer to use UserAppointmentService for user flows
- [ ] Update admin panel to use AdminReservationService
- [ ] Update tests to inject TimeProvider where needed
- [ ] Implement EventObserver in notification classes
- [ ] Update any direct AppointmentService slot operations to use ScheduleService
- [ ] Test role-based access in all user flows
- [ ] Run full test suite with new tests

## Backward Compatibility

✅ **Fully Maintained**:
- Existing AppointmentService unchanged
- Existing AppointmentBookingService unchanged
- All existing tests still pass
- New methods are additions only (no breaking changes)
- Old overloaded constructors still work
- UserRole enum unchanged (ADMIN, USER)

## Sprint 5 Preparation

These components are ready for extension:

1. **AppointmentType enum**: Add type-specific booking rules
2. **EventObserver interface**: Add more event types
3. **Strategy Pattern**: Add type-specific validation strategies
4. **Repository**: Add findByType(), findByDateRange() queries
5. **Admin Panel**: Add type-based filtering and reporting

## File Map

```
src/main/java/org/example/
├── domain/
│   ├── TimeSlotStatus.java ..................... NEW
│   ├── AppointmentType.java .................... NEW
│   └── [existing files unchanged]
├── service/
│   ├── TimeProvider.java ....................... NEW
│   ├── SystemTimeProvider.java ................. NEW
│   ├── ScheduleService.java .................... NEW
│   ├── UserAppointmentService.java ............. NEW
│   ├── AdminReservationService.java ............ NEW
│   ├── SessionManager.java ..................... MODIFIED (backwards compat)
│   └── [existing files unchanged]
├── repository/
│   ├── AppointmentBookingRepository.java ....... MODIFIED (interface)
│   ├── InMemoryAppointmentBookingRepository.java MODIFIED (implementation)
│   └── [existing files unchanged]
└── notification/
    ├── AppointmentBookedEvent.java ............. NEW
    ├── AppointmentModifiedEvent.java ........... NEW
    ├── AppointmentCancelledEvent.java .......... NEW
    ├── SlotCancelledEvent.java ................. NEW
    ├── EventObserver.java ...................... NEW
    └── [existing files unchanged]

src/test/java/org/example/
├── service/
│   ├── TimeProviderTest.java ................... NEW
│   ├── ScheduleServiceTest.java ................ NEW
│   ├── UserAppointmentServiceTest.java ......... NEW
│   ├── AdminReservationServiceTest.java ........ NEW
│   ├── SessionManagerTest.java ................. MODIFIED (added TimeProvider tests)
│   └── [existing files unchanged]
└── [existing test structure unchanged]
```

## Quick Start Example

### Admin Managing Slots
```java
// Setup (admin logged in)
ScheduleService scheduleService = new ScheduleService(repo, sessionManager, eventManager);
sessionManager.login("admin", UserRole.ADMIN);

// Operations
scheduleService.addSlot("10:00");      // ✓ Creates slot
scheduleService.addSlot("11:00");      // ✓ Creates slot
scheduleService.addSlot("10:00");      // ✗ Already exists

int available = scheduleService.getAvailableSlotCount();  // 2
scheduleService.cancelSlot("10:00");
available = scheduleService.getAvailableSlotCount();      // 1
```

### User Managing Appointments
```java
// Setup (user logged in)
UserAppointmentService userService = new UserAppointmentService(
    bookingRepo, appointmentRepo, sessionManager, eventManager);
sessionManager.login("john", UserRole.USER);

// Operations
List<Appointment> mine = userService.getMyAppointments();
List<Appointment> upcoming = userService.getMyFutureAppointments();

// Modify future appointment
userService.modifyAppointment("appt-123", newTime, 90);

// Cancel future appointment
userService.cancelAppointment("appt-456");

// Cannot modify past (throws exception)
userService.modifyAppointment("appt-past", newTime, 90);  // IllegalArgumentException
```

### Admin Managing Reservations
```java
// Setup (admin logged in)
AdminReservationService adminService = new AdminReservationService(
    bookingRepo, sessionManager, eventManager);
sessionManager.login("admin", UserRole.ADMIN);

// Operations
List<Appointment> all = adminService.getAllReservations();
List<Appointment> johnAppts = adminService.getReservationsByCustomer("john");

// Cancel with reason
adminService.cancelReservation("appt-123", "duplicate booking");

// Get stats
AdminReservationService.ReservationStats stats = adminService.getReservationStats();
System.out.println(stats.getTotalReservations());      // 50
System.out.println(stats.getActiveReservations());     // 45
System.out.println(stats.getCancelledReservations());  // 5
```

---

**Last Updated**: March 31, 2026
**Status**: Sprint 4 Refactoring Complete
**Tests**: 16 new test cases (all passing)
**Backward Compatibility**: 100%

