# Role-Based Slot Management - Quick Reference Guide

## What Changed?

### For Admins
Admins now have two additional capabilities:
- **Add new appointment slots** via GUI (Dashboard → Manage Slots → Add Slot)
- **Cancel appointment slots** via GUI (Dashboard → Manage Slots → Cancel Slot)

### For Regular Users
No changes to user capabilities - they can still:
- View available slots
- Book appointments
- Modify their own future appointments
- Cancel their own future appointments

### What's New in Code

#### Service Layer API

```java
// NEW: Admin-only slot management methods
AppointmentBookingService bookingService = ...;

// Add a new appointment slot (admin-only)
BookingStatus status = bookingService.addManagedSlot("14:00");
if (status == BookingStatus.SUCCESS) {
    // Slot created and available for booking
}

// Cancel an appointment slot (admin-only)
BookingStatus status = bookingService.cancelManagedSlot("14:00");
if (status == BookingStatus.SUCCESS) {
    // Slot cancelled - users cannot book it anymore
}
```

#### Key Authorization Checks

```java
// In AppointmentBookingService
public BookingStatus addManagedSlot(String slotTime) {
    if (!isCurrentUserAdmin()) {
        return BookingStatus.UNAUTHORIZED;  // ❌ Not admin
    }
    // ... proceed with adding slot
}

public BookingStatus cancelManagedSlot(String slotTime) {
    if (!isCurrentUserAdmin()) {
        return BookingStatus.UNAUTHORIZED;  // ❌ Not admin
    }
    // ... proceed with cancelling slot
}
```

#### Cancelled Slot Filtering

```java
// Cancelled slots automatically filtered everywhere
List<AppointmentSlot> available = appointmentService.getAvailableSlots();
// Already excludes cancelled slots!

// Repository enforces it
boolean booked = appointmentService.bookSlot("14:00");
// Returns false if slot is cancelled (not just if booked)

// Even at repository level
Optional<AppointmentSlot> slot = appointmentRepository.findByTime("14:00");
// Returns empty if slot is cancelled
```

## Domain Model

### AppointmentSlot Fields
```java
private String time;        // Slot time (e.g., "10:00")
private boolean booked;     // Is it booked by a user?
private boolean cancelled;  // Was it cancelled by admin? (NEW)
```

### AppointmentSlot Methods
```java
// Existing
String getTime()
boolean isBooked()
boolean isAvailable()
void book()
void release()

// NEW
boolean isCancelled()
void cancel()
```

## GUI Components

### MainDashboardFrame
```
Dashboard Menu
├─ View Slots (visible to all)
├─ Book Appointment (visible to users only)
├─ Manage Reservations (visible to admins only)
├─ Manage Slots ← NEW (visible to admins only)
├─ Logout
└─ Exit
```

### SlotManagementPanel (NEW)
- Admin-only panel accessible from Dashboard → Manage Slots
- Input field for slot time (e.g., "14:00")
- "Add Slot" button - creates new available slot
- "Cancel Slot" button - cancels existing slot (users can't book it)
- Feedback messages with success/error status

## Console Menu

### Admin Menu
```
1. View Slots
2. Add Slot        ← NEW
3. Cancel Slot     ← NEW
4. Manage Reservations
5. Logout
```

### User Menu
```
1. View Slots
2. Book Appointment
3. My Appointments
4. Logout
```

## Testing

### Run all tests
```bash
mvn test
```

### Run role-based tests only
```bash
mvn test -Dtest=AdminSlotManagementTest
```

### Test scenarios covered:
- ✅ Admin can add slots
- ✅ Admin cannot add duplicate slots
- ✅ Admin can cancel slots
- ✅ Cancelled slots not bookable
- ✅ Users cannot add/cancel slots
- ✅ Unauthenticated users cannot add/cancel slots
- ✅ Invalid input handling (blank times)
- ✅ Nonexistent slot handling
- ✅ Cancelled slots filtered from UI
- ✅ Admin cannot book appointments
- ✅ Users can book available slots
- ✅ Users cannot book cancelled slots

## Architecture Layers

```
┌─────────────────────────────────────┐
│     Presentation Layer (GUI/CLI)    │
│  - MainDashboardFrame               │
│  - SlotManagementPanel              │
│  - ConsoleMenu                      │
└──────────────┬──────────────────────┘
               │ Uses
┌──────────────▼──────────────────────┐
│     Service Layer                   │
│  - AppointmentService               │
│    • addSlot(time)                  │
│    • cancelSlot(time)               │
│  - AppointmentBookingService        │
│    • addManagedSlot(time) [AUTH]    │
│    • cancelManagedSlot(time) [AUTH] │
└──────────────┬──────────────────────┘
               │ Uses
┌──────────────▼──────────────────────┐
│     Repository Layer                │
│  - AppointmentRepository            │
│    • save(slot)                     │
│    • findByTime(time)               │
│    • removeSlot(time)               │
│  - InMemoryAppointmentRepository    │
└──────────────┬──────────────────────┘
               │ Manages
┌──────────────▼──────────────────────┐
│     Domain Model                    │
│  - AppointmentSlot                  │
│    • time                           │
│    • booked                         │
│    • cancelled                      │
└─────────────────────────────────────┘
```

## Status Codes

### BookingStatus Return Values
- `SUCCESS` - Operation completed
- `UNAUTHORIZED` - User lacks permission
- `BLANK_SLOT_TIME` - Empty or null time input
- `SLOT_ALREADY_BOOKED` - Slot exists (for add operations)
- `SLOT_NOT_FOUND` - Slot doesn't exist or is cancelled

## Example Usage

### Adding a Slot (Admin)
```java
sessionManager.login("admin1", UserRole.ADMIN);
BookingStatus status = bookingService.addManagedSlot("14:30");
// Returns BookingStatus.SUCCESS if slot created
// Returns BookingStatus.UNAUTHORIZED if not admin
// Returns BookingStatus.SLOT_ALREADY_BOOKED if slot exists
```

### Cancelling a Slot (Admin)
```java
sessionManager.login("admin1", UserRole.ADMIN);
BookingStatus status = bookingService.cancelManagedSlot("14:30");
// Returns BookingStatus.SUCCESS if slot cancelled
// Returns BookingStatus.UNAUTHORIZED if not admin
// Returns BookingStatus.SLOT_NOT_FOUND if doesn't exist
```

### User Trying to Add Slot (Blocked)
```java
sessionManager.login("user1", UserRole.USER);
BookingStatus status = bookingService.addManagedSlot("14:30");
// Returns BookingStatus.UNAUTHORIZED - prevents operation
```

### Booking a Regular Slot (User)
```java
sessionManager.login("user1", UserRole.USER);
BookingStatus status = bookingService.bookAppointment(
    "John Doe",           // customer name
    "10:00",              // slot time
    60,                   // duration minutes
    1                     // participants
);
// Returns BookingStatus.SUCCESS
```

### Booking a Cancelled Slot (User)
```java
// Admin cancelled slot "14:00"
BookingStatus status = bookingService.bookAppointment(
    "Jane Doe",
    "14:00",              // This slot was cancelled!
    60,
    1
);
// Returns BookingStatus.SLOT_NOT_FOUND - cannot book it
```

## Breaking Changes

None! The refactoring is fully backward compatible.
- Old ConsoleMenu constructor still works
- AppointmentService methods work as before
- New admin methods are additive only

## Files to Review

1. **Core Logic:**
   - `src/main/java/org/example/service/AppointmentBookingService.java` - Auth checks
   - `src/main/java/org/example/service/AppointmentService.java` - Slot filtering
   - `src/main/java/org/example/repository/InMemoryAppointmentRepository.java` - Persistence

2. **GUI:**
   - `src/main/java/org/example/presentation/gui/MainDashboardFrame.java` - Menu
   - `src/main/java/org/example/presentation/gui/SlotManagementPanel.java` - Admin UI

3. **Tests:**
   - `src/test/java/org/example/service/AdminSlotManagementTest.java` - Comprehensive tests

## Common Issues & Solutions

### Q: User sees "Manage Slots" button but cannot access it
**A:** This shouldn't happen - UI should hide it for non-admins. Check:
- `MainDashboardFrame.refreshForCurrentSession()` sets `manageSlotButton.setVisible(adminSession)`
- `SlotManagementPanel.handleAddSlot()` has service-layer auth check

### Q: Admin adds slot but it doesn't appear in booking list
**A:** Check that:
1. Slot was saved to repository (check return value)
2. New slot isn't already cancelled
3. UI called `refresh()` on panels after adding

### Q: Cancelled slot still appears as bookable
**A:** Repository filters should handle this. Verify:
1. `AppointmentSlot.isCancelled()` returns true
2. Repository's `findAvailable()` checks `!slot.isCancelled()`

### Q: Tests are failing
**A:** Ensure:
1. All new repository methods are implemented
2. SessionManager is initialized in tests
3. AppointmentBookingService has SessionManager injected

