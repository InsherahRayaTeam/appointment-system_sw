# Role-Based Slot Management - Visual Workflows

## User Journey

### Regular User Workflow

```
┌─────────────┐
│   LOGIN     │
│  (as USER)  │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────┐
│    USER DASHBOARD MENU       │
├──────────────────────────────┤
│ 1. View Slots                │ ✅
│ 2. Book Appointment          │ ✅
│ 3. My Appointments           │ ✅
│ 4. Logout                    │
│                              │
│ (No Slot Management!)        │ ❌
└──────┬───────────────────────┘
       │
       ├─→ [View Slots] ──→ List all AVAILABLE slots
       │
       ├─→ [Book Appointment] ──→ Select slot + enter details
       │                           │
       │                           ├─→ SUCCESS: Appointment booked
       │                           └─→ FAILED: Slot booked/cancelled/invalid
       │
       └─→ [My Appointments] ──→ View & manage own appointments
```

### Admin Workflow

```
┌─────────────┐
│   LOGIN     │
│  (as ADMIN) │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────┐
│   ADMIN DASHBOARD MENU       │
├──────────────────────────────┤
│ 1. View Slots                │ ✅
│ 2. Manage Slots              │ ✅ ← NEW!
│ 3. Manage Reservations       │ ✅
│ 4. Logout                    │
│                              │
│ (NO Book Appointment!)       │ ❌
└──────┬───────────────────────┘
       │
       ├─→ [View Slots] ──→ List all AVAILABLE slots
       │
       ├─→ [Manage Slots] ──→ ┌──────────────────────┐
       │   (NEW!)              │ SLOT MANAGEMENT      │
       │                       ├──────────────────────┤
       │                       │ Time: [input field]  │
       │                       │                      │
       │                       │ [Add Slot] [Cancel]  │
       │                       │                      │
       │                       │ Existing Slots:      │
       │                       │ • 10:00 (AVAILABLE)  │
       │                       │ • 11:00 (BOOKED)     │
       │                       │ • 12:00 (AVAILABLE)  │
       │                       │                      │
       │                       │ [Back to Dashboard]  │
       │                       └──────────────────────┘
       │                              │
       │                              ├─→ [Add "14:00"]
       │                              │   └─→ SUCCESS: New slot created
       │                              │
       │                              └─→ [Cancel "10:00"]
       │                                  └─→ SUCCESS: Slot cancelled
       │                                      (users can't book it anymore)
       │
       └─→ [Manage Reservations] ──→ View all user appointments
                                      Modify/cancel reservations
```

---

## Slot State Transitions

```
┌─────────────┐
│  AVAILABLE  │
└──────┬──────┘
       │
       ├─→ User books ──→ ┌─────────┐
       │                   │ BOOKED  │
       │                   └────┬────┘
       │                        │
       │                        └─→ User cancels ──→ ┌─────────────┐
       │                                              │ RELEASED    │
       │                                              │ (AVAILABLE) │
       │                                              └─────────────┘
       │
       └─→ Admin cancels ──→ ┌──────────┐
                             │ CANCELLED│
                             └──────────┘
                                  │
                                  └─→ NOT BOOKABLE!
                                      (Users get SLOT_NOT_FOUND error)
                                      (Does not appear in available list)
```

---

## Authorization Flow

### Adding a Slot

```
USER ACTION: Click "Add Slot" → Input "14:00" → Click "Add"
                    │
                    ▼
         ┌──────────────────────┐
         │  UI validates input  │
         │  (blank check, etc)  │
         └──────────┬───────────┘
                    │
                    ▼
      ┌─────────────────────────────┐
      │ AppointmentBookingService   │
      │   addManagedSlot("14:00")   │
      └──────────────┬──────────────┘
                     │
                     ▼
          ┌──────────────────────┐
          │ isCurrentUserAdmin() │
          │        CHECK         │
          └────┬──────────────┬──┘
             YES            NO
              │              │
              ▼              ▼
         ┌────────────┐  ┌────────────┐
         │ Proceed    │  │ Return     │
         │ with add   │  │ UNAUTHORIZED
         └────┬───────┘  └────────────┘
              │
              ▼
      ┌────────────────────┐
      │ Slot already       │ YES ──→ ┌──────────────────┐
      │ exists?            │         │ Return           │
      └────┬───────────────┘         │ SLOT_ALREADY_BOOKEDx
          NO                         └──────────────────┘
           │
           ▼
      ┌────────────────────┐
      │ AppointmentRepository
      │     .save(slot)    │
      └────┬───────────────┘
           │
           ▼
      ┌────────────────────┐
      │ SUCCESS!           │
      │ Slot created       │
      │ & available        │
      │ immediately        │
      └────────────────────┘
```

### Booking a Slot

```
USER ACTION: Select slot "10:00" → Enter details → Book
                    │
                    ▼
         ┌──────────────────────┐
         │  UI validates input  │
         │  (name, duration)    │
         └──────────┬───────────┘
                    │
                    ▼
      ┌─────────────────────────────┐
      │ AppointmentBookingService   │
      │   bookAppointment(...)      │
      └──────────────┬──────────────┘
                     │
                     ▼
          ┌──────────────────────┐
          │ isCurrentUserAdmin() │
          │        CHECK         │
          └────┬──────────────┬──┘
             YES            NO
              │              │
              ▼              ▼
         ┌────────────┐  ┌────────────┐
         │ Return     │  │ Proceed    │
         │ UNAUTHORIZED
         └────────────┘  └────┬───────┘
                              │
                              ▼
                      ┌──────────────────┐
                      │ Find slot by     │
                      │ time "10:00"     │
                      └────┬─────────────┘
                           │
                ┌──────────┴──────────┐
              NOT          OR         FOUND
              FOUND      CANCELLED    & AVAILABLE
               │            │             │
               ▼            ▼             ▼
          ┌────────┐  ┌────────┐  ┌──────────────┐
          │ Return │  │ Return │  │ Book slot    │
          │ SLOT_  │  │ SLOT_  │  │ & save       │
          │NOT_FOUND NOT_FOUND│  │ appointment  │
          └────────┘  └────────┘  └──────┬───────┘
                                         │
                                         ▼
                                    ┌──────────────┐
                                    │ SUCCESS!     │
                                    │ Appointment  │
                                    │ confirmed    │
                                    └──────────────┘
```

---

## Slot Visibility Rules

### What Users See (Available Slots)

```
┌─────────────────────────────────┐
│  ALL SLOTS IN REPOSITORY        │
├─────────────────────────────────┤
│ 10:00: booked=false, cancelled= │
│        false          ─→ VISIBLE │
│                                 │
│ 11:00: booked=true,  cancelled= │
│        false          ─→ HIDDEN  │
│        (already booked)          │
│                                 │
│ 12:00: booked=false, cancelled= │
│        true           ─→ HIDDEN  │
│        (admin cancelled)         │
│                                 │
│ 14:00: booked=false, cancelled= │
│        false          ─→ VISIBLE │
└─────────────────────────────────┘

FILTERED LIST (via getAvailableSlots()):
├─ 10:00 ✅
└─ 14:00 ✅
```

### Status Display in Admin UI

```
┌──────────────────────┐
│ ADMIN SEES:          │
├──────────────────────┤
│ • 10:00 (AVAILABLE)  │ ✅
│ • 11:00 (BOOKED)     │ ⏱️
│ • 12:00 (AVAILABLE)  │ ✅ (previously cancelled, re-added)
│ • 14:00 (AVAILABLE)  │ ✅
│                      │
│ Cancelled slots:     │
│ (Not shown in list)  │
│ (Hidden after cancel)│
└──────────────────────┘

Note: Admin doesn't see cancelled slots
in the normal list. Only non-cancelled
slots appear in "Existing Slots" display.
```

---

## Data Model Changes

### AppointmentSlot Object

#### Before
```java
AppointmentSlot {
    String time;           // e.g., "10:00"
    boolean booked;        // true if user booked it
}

STATES:
├─ time="10:00", booked=false  → AVAILABLE for booking
└─ time="10:00", booked=true   → BOOKED, cannot book
```

#### After
```java
AppointmentSlot {
    String time;           // e.g., "10:00"
    boolean booked;        // true if user booked it
    boolean cancelled;     // true if admin cancelled it (NEW)
}

STATES:
├─ time="10:00", booked=false, cancelled=false  → AVAILABLE for booking
├─ time="10:00", booked=true,  cancelled=false  → BOOKED, cannot book
└─ time="10:00", booked=*,     cancelled=true   → CANCELLED, not bookable
                                                   (hidden from users)
```

---

## Service Method Signatures

### AppointmentService (No Auth)
```java
// EXISTING
List<AppointmentSlot> getAvailableSlots()
    // Returns slots: booked=false AND cancelled=false

boolean bookSlot(String time)
    // Requires: not booked AND not cancelled

// NEW
boolean addSlot(String time)
    // No auth check - delegate to AppointmentBookingService

boolean cancelSlot(String time)
    // No auth check - delegate to AppointmentBookingService
```

### AppointmentBookingService (With Auth)
```java
// EXISTING
BookingStatus bookAppointment(String customerName, String slotTime, 
                               int duration, int participants)
    // BLOCKS admins with UNAUTHORIZED status

// NEW
BookingStatus addManagedSlot(String slotTime)
    // ✅ Admin-only check
    // Returns: SUCCESS, UNAUTHORIZED, BLANK_SLOT_TIME, 
    //          SLOT_ALREADY_BOOKED

BookingStatus cancelManagedSlot(String slotTime)
    // ✅ Admin-only check
    // Returns: SUCCESS, UNAUTHORIZED, BLANK_SLOT_TIME, SLOT_NOT_FOUND
```

---

## Event Flow

### When Admin Adds Slot "14:00"

```
┌──────────────────────────────────────┐
│ Admin clicks "Add Slot" → Input      │
│ "14:00" → Click "Add"                │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ AppointmentBookingService            │
│   .addManagedSlot("14:00")           │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ ✅ Auth check: isCurrentUserAdmin()  │
│                true → Continue       │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ AppointmentRepository.save(slot)     │
│   Persist slot to repository         │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ EventManager.notifyObservers(        │
│   "New slot added: 14:00"            │
│ )                                    │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Return BookingStatus.SUCCESS         │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ GUI shows: "Slot added successfully" │
│ (green message)                      │
│ Clear input field                    │
│ Slot "14:00" now in available list  │
└──────────────────────────────────────┘
```

### When Admin Cancels Slot "10:00"

```
┌──────────────────────────────────────┐
│ Admin clicks "Cancel Slot" → Input   │
│ "10:00" → Click "Cancel"             │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ AppointmentBookingService            │
│   .cancelManagedSlot("10:00")        │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ ✅ Auth check: isCurrentUserAdmin()  │
│                true → Continue       │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ AppointmentRepository.removeSlot()   │
│   Find slot "10:00"                  │
│   Call slot.cancel()                 │
│   Set cancelled = true               │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ EventManager.notifyObservers(        │
│   "Slot cancelled: 10:00"            │
│ )                                    │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Return BookingStatus.SUCCESS         │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ GUI shows: "Slot cancelled success"  │
│ (green message)                      │
│ Slot "10:00" disappears from list   │
│ Users cannot book it anymore         │
└──────────────────────────────────────┘
```

### When User Tries to Book Cancelled Slot

```
┌──────────────────────────────────────┐
│ User selects cancelled slot "10:00"  │
│ (but it's hidden - can't select)     │
│                                      │
│ OR User knows the time and tries:    │
│ bookAppointment(..., "10:00", ...)   │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ AppointmentBookingService            │
│   .bookAppointment(...)              │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ ❌ User role check: isCurrentUserRegularUser()
│    true → Continue (not admin)       │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Find slot by time: "10:00"           │
│ Check AppointmentRepository          │
│   .findByTime("10:00")               │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Repository filter check:             │
│ if (slot.cancelled) return empty     │
│ → SLOT NOT FOUND!                    │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Return BookingStatus.SLOT_NOT_FOUND  │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ GUI shows error: "Slot not found"    │
│ (red message)                        │
│ Booking fails                        │
└──────────────────────────────────────┘
```

---

This visual guide shows the complete flow of role-based slot management functionality.

