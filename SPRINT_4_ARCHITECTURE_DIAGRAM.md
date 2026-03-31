# Sprint 4 Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────┐  ┌─────────────────────┐                   │
│  │   ADMIN MENU        │  │   USER MENU         │                   │
│  │                     │  │                     │                   │
│  │ • Slot Management   │  │ • My Appointments   │                   │
│  │ • Reservations Mgmt │  │ • Book Appointment  │                   │
│  │ • Statistics        │  │ • Modify Appt       │                   │
│  │                     │  │ • Cancel Appt       │                   │
│  └──────────┬──────────┘  └──────────┬──────────┘                   │
│             │                        │                              │
│             └────────────┬───────────┘                              │
│                          │                                          │
└──────────────────────────┼──────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              ROLE-BASED ACCESS CONTROL                      │   │
│  │  • SessionManager tracks (username, UserRole)              │   │
│  │  • Each service verifies role requirements                 │   │
│  │  • IllegalStateException for unauthorized access          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              SLOT MANAGEMENT (ADMIN)                        │   │
│  │                                                              │   │
│  │  ScheduleService                                            │   │
│  │  • addSlot(time) .......................... Admin only      │   │
│  │  • cancelSlot(time) ....................... Admin only      │   │
│  │  • listAvailableSlots() .................. All users        │   │
│  │  • listAllSlots() ........................ All users        │   │
│  │  • getSlotCounts() ....................... Utilities       │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              BOOKING OPERATIONS (EXISTING)                  │   │
│  │                                                              │   │
│  │  AppointmentBookingService (unchanged)                     │   │
│  │  • bookAppointment() ..................... Customers        │   │
│  │  • Applies DurationRule strategy                           │   │
│  │  • Applies ParticipantRule strategy                        │   │
│  │                                                              │   │
│  │  AppointmentService (unchanged - legacy)                   │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │           USER APPOINTMENT MANAGEMENT (USER)               │   │
│  │                                                              │   │
│  │  UserAppointmentService                                     │   │
│  │  • getMyAppointments() ................... Current user      │   │
│  │  • modifyAppointment() ................... Future only       │   │
│  │  • cancelAppointment() ................... Future only       │   │
│  │  • getMyFutureAppointments()                               │   │
│  │  • getMyPastAppointments()                                 │   │
│  │                                                              │   │
│  │  Constraints:                                               │   │
│  │  ✓ Ownership verified                                       │   │
│  │  ✓ Time-based constraints enforced                          │   │
│  │  ✓ Cannot use for ADMIN users                              │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │         ADMIN RESERVATION MANAGEMENT (ADMIN)               │   │
│  │                                                              │   │
│  │  AdminReservationService                                    │   │
│  │  • getAllReservations() .................. Admin only        │   │
│  │  • getReservationsByCustomer() .......... Admin only        │   │
│  │  • cancelReservation() .................. Admin only        │   │
│  │  • getReservationStats() ................ Admin only        │   │
│  │                                                              │   │
│  │  Constraints:                                               │   │
│  │  ✓ ADMIN role required                                      │   │
│  │  ✓ Audit trail on cancellations                            │   │
│  │  ✓ Cannot use for USER appointments                        │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              SESSION & UTILITY SERVICES                     │   │
│  │                                                              │   │
│  │  SessionManager (enhanced)                                  │   │
│  │  • login(username, role)                                   │   │
│  │  • isAdmin() / isUser()                                    │   │
│  │  • Uses injected TimeProvider                              │   │
│  │                                                              │   │
│  │  EventManager (enhanced event publishing)                  │   │
│  │  • notifyObservers(message)                                │   │
│  │  • subscribe(observer)                                     │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              STRATEGY PATTERN (EXTENSIBLE)                  │   │
│  │                                                              │   │
│  │  BookingRuleStrategy (interface)                            │   │
│  │  ├─ DurationRule (1-120 minutes)                           │   │
│  │  ├─ ParticipantRule (1-5 participants)                     │   │
│  │  └─ [Sprint 5] TypeSpecificRules                           │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   NOTIFICATION LAYER (EVENTS)                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Domain Events (Spring 5 Preparation)                               │
│                                                                       │
│  ┌─ AppointmentBookedEvent                                         │
│  │  └─ Observers receive: appointment, username                    │
│  │                                                                   │
│  ├─ AppointmentModifiedEvent                                       │
│  │  └─ Observers receive: old appointment, new appointment,        │
│  │                        username                                 │
│  │                                                                   │
│  ├─ AppointmentCancelledEvent                                      │
│  │  └─ Observers receive: appointment, username, reason            │
│  │                                                                   │
│  ├─ SlotCancelledEvent                                             │
│  │  └─ Observers receive: slot time, admin username               │
│  │                                                                   │
│  └─ EventObserver (interface for typed subscriptions)              │
│     └─ Observers implement handlers for each event type           │
│                                                                       │
│  Publisher: EventManager                                            │
│  • notifyObservers(message) - current implementation               │
│  • Can be enhanced to dispatch typed events                        │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │  AppointmentRepository (Slots)                             │    │
│  │  ├─ findAll()                                              │    │
│  │  ├─ findAvailable()                                        │    │
│  │  ├─ findByTime(time)                                       │    │
│  │  ├─ save(slot)                                             │    │
│  │  └─ removeSlot(time)                                       │    │
│  │                                                              │    │
│  │  InMemoryAppointmentRepository (implementation)            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │  AppointmentBookingRepository (Appointments)              │    │
│  │  ├─ findAll()                                              │    │
│  │  ├─ findById(id)                                           │    │
│  │  ├─ findByCustomerName(name) ........ NEW                 │    │
│  │  ├─ save(appointment)                                      │    │
│  │  ├─ update(appointment)                                    │    │
│  │  └─ remove(id) ..................... NEW                  │    │
│  │                                                              │    │
│  │  InMemoryAppointmentBookingRepository (implementation)     │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─ Appointment (booking data)                                     │
│  │  ├─ id, customerName, startTime                                │
│  │  ├─ duration, participants, status                             │
│  │  └─ [Sprint 5] appointmentType                                │
│  │                                                                   │
│  ├─ AppointmentSlot (time slot)                                    │
│  │  ├─ time, booked, cancelled                                    │
│  │  └─ [Future] status (TimeSlotStatus enum)                     │
│  │                                                                   │
│  ├─ UserRole (enum)                                                │
│  │  ├─ ADMIN                                                       │
│  │  └─ USER                                                        │
│  │                                                                   │
│  ├─ AppointmentStatus (enum)                                       │
│  │  ├─ CONFIRMED                                                   │
│  │  └─ CANCELLED                                                   │
│  │                                                                   │
│  ├─ TimeSlotStatus (enum) ......................... NEW             │
│  │  ├─ AVAILABLE                                                   │
│  │  ├─ BOOKED                                                      │
│  │  └─ CANCELLED                                                   │
│  │                                                                   │
│  └─ AppointmentType (enum) ....................... NEW (Sprint 5) │
│     ├─ URGENT                                                      │
│     ├─ FOLLOW_UP                                                   │
│     ├─ ASSESSMENT                                                  │
│     ├─ VIRTUAL                                                     │
│     ├─ IN_PERSON                                                   │
│     ├─ INDIVIDUAL                                                  │
│     └─ GROUP                                                       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagrams

### Admin Slot Management Flow

```
Admin User
    │
    ├─ Login: sessionManager.login("admin", UserRole.ADMIN)
    │
    ├─ Add Slot:
    │  scheduleService.addSlot("10:00")
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Repository: save(AppointmentSlot)
    │  ├─ Event: EventManager.notifyObservers("New slot created")
    │  └─ Return: true/false
    │
    ├─ Cancel Slot:
    │  scheduleService.cancelSlot("10:00")
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Update: slot.cancel()
    │  ├─ Repository: removeSlot("10:00")
    │  ├─ Event: EventManager.notifyObservers("Slot cancelled")
    │  └─ Return: true/false
    │
    ├─ View Slot Stats:
    │  scheduleService.getAvailableSlotCount()
    │  ├─ Iterate: slots in memory
    │  └─ Return: count
    │
    └─ Logout: sessionManager.logoutAndNotify()
```

### User Appointment Management Flow

```
Regular User
    │
    ├─ Login: sessionManager.login("john", UserRole.USER)
    │
    ├─ View My Appointments:
    │  userService.getMyAppointments()
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✗ → Exception if ADMIN
    │  ├─ Repository: findByCustomerName("john")
    │  └─ Return: [Appointment[], ...]
    │
    ├─ View Future Only:
    │  userService.getMyFutureAppointments()
    │  ├─ Get: getMyAppointments()
    │  ├─ Filter: appointment.startTime > timeProvider.now()
    │  └─ Return: [Future Appointments]
    │
    ├─ Modify Appointment:
    │  userService.modifyAppointment("appt-123", newTime, 90)
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✗
    │  ├─ Ownership: findByCustomerName + verify
    │  ├─ Time Check: startTime > now() ✓ (must be future)
    │  ├─ Update: appointmentRepository.update(modified)
    │  ├─ Event: EventManager.notifyObservers("Appointment modified")
    │  └─ Return: true/false
    │
    ├─ Cancel Appointment:
    │  userService.cancelAppointment("appt-456")
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✗
    │  ├─ Ownership: verified
    │  ├─ Time Check: startTime > now() ✓ (must be future)
    │  ├─ Update: appointment.status = CANCELLED
    │  ├─ Repository: update(cancelled)
    │  ├─ Event: EventManager.notifyObservers("Appointment cancelled")
    │  └─ Return: true/false
    │
    └─ Logout: sessionManager.logoutAndNotify()
```

### Admin Reservation Management Flow

```
Admin User
    │
    ├─ Login: sessionManager.login("admin", UserRole.ADMIN)
    │
    ├─ View All Reservations:
    │  adminService.getAllReservations()
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Repository: findAll()
    │  └─ Return: [Appointment[], ...]
    │
    ├─ View Customer Reservations:
    │  adminService.getReservationsByCustomer("jane")
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Repository: findByCustomerName("jane")
    │  └─ Return: [Jane's Appointments]
    │
    ├─ Cancel Reservation:
    │  adminService.cancelReservation("appt-789", "double booking")
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Find: appointmentRepository.findById("appt-789")
    │  ├─ Update: appointment.status = CANCELLED
    │  ├─ Repository: update(cancelled)
    │  ├─ Event: Notify with reason and admin username
    │  └─ Return: true/false
    │
    ├─ View Statistics:
    │  adminService.getReservationStats()
    │  ├─ Check: isLoggedIn() ✓
    │  ├─ Check: isAdmin() ✓
    │  ├─ Repository: findAll()
    │  ├─ Count: total, active, cancelled
    │  └─ Return: ReservationStats(total, active, cancelled)
    │
    └─ Logout: sessionManager.logoutAndNotify()
```

---

## Time-Based Constraints Enforcement

```
userService.modifyAppointment(id, newTime, duration)
    │
    └─ Get Current Appointment
       └─ TimeProvider.now() → 2026-03-31 10:00
    │
    └─ Check: appointment.startTime > now()
       │
       ├─ IF: 2026-04-15 14:00 > 2026-03-31 10:00 → TRUE ✓
       │  └─ Modification allowed
       │
       └─ IF: 2026-03-20 09:00 > 2026-03-31 10:00 → FALSE ✗
          └─ Throw IllegalArgumentException("cannot modify past appointments")
```

---

## Role-Based Access Control Enforcement

```
All Service Methods:
    │
    └─ if (!sessionManager.isLoggedIn())
       └─ throw IllegalStateException("user must be logged in")
    │
    └─ Method-specific role check:
       │
       ├─ Admin-only (scheduleService.addSlot, etc.):
       │  └─ if (!sessionManager.isAdmin())
       │     └─ throw IllegalStateException("only ADMIN role can...")
       │
       ├─ User-only (userService.getMyAppointments, etc.):
       │  └─ if (sessionManager.isAdmin())
       │     └─ throw IllegalStateException("ADMIN should use AdminReservationService")
       │
       └─ All-users (scheduleService.listAvailableSlots, etc.):
          └─ No additional check (just needs login)
```

---

## Testing Architecture

```
Test Class Hierarchy:

TimeProviderTest
    └─ Tests SystemTimeProvider.now()
       ├─ Returns current time
       └─ Multiple calls consistent

ScheduleServiceTest
    ├─ Tests role-based access (Admin only)
    ├─ Tests login requirement
    ├─ Tests slot operations
    └─ Uses @Mock(SessionManager, EventManager, Repository)

UserAppointmentServiceTest
    ├─ Tests user-only operations
    ├─ Tests time-based constraints
    ├─ Tests ownership verification
    ├─ Tests future/past filtering
    └─ Uses @Mock(SessionManager, EventManager, Repository, TimeProvider)

AdminReservationServiceTest
    ├─ Tests admin-only operations
    ├─ Tests reservation retrieval
    ├─ Tests cancellation with audit
    ├─ Tests statistics calculation
    └─ Uses @Mock(SessionManager, EventManager, Repository)

SessionManagerTest (Enhanced)
    ├─ Tests TimeProvider injection
    ├─ Tests role tracking
    ├─ Tests backward compatibility
    └─ Uses @Mock(TimeProvider, AuthEventLogger, EventManager)

All tests:
    ├─ Use @ExtendWith(MockitoExtension.class)
    ├─ Use @BeforeEach for setup
    ├─ Use when().thenReturn() for mocking
    ├─ Use assertThrows() for exception testing
    └─ Use verify() for behavior verification
```

---

## Deployment View

```
Production Environment:

┌─────────────────────────────────────────────┐
│         Application Instance                │
├─────────────────────────────────────────────┤
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │      Spring/DI Container            │   │
│  │  (or manual dependency injection)   │   │
│  ├─────────────────────────────────────┤   │
│  │                                     │   │
│  │  • Create Repositories              │   │
│  │  • Create SessionManager            │   │
│  │  • Create ScheduleService           │   │
│  │  • Create UserAppointmentService    │   │
│  │  • Create AdminReservationService   │   │
│  │  • Inject into Controllers          │   │
│  │                                     │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │    Service Instances (Singleton)    │   │
│  │    - Stateless                      │   │
│  │    - Thread-safe                    │   │
│  │    - Shared across requests         │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │    In-Memory Repositories           │   │
│  │    - Data stored in HashMaps        │   │
│  │    - Volatile (lost on shutdown)    │   │
│  │    - For production: use database   │   │
│  └─────────────────────────────────────┘   │
│                                             │
└─────────────────────────────────────────────┘
```

---

## Future Extensibility (Sprint 5)

```
Current Structure:

Strategy Pattern:
    BookingRuleStrategy
    ├─ DurationRule
    ├─ ParticipantRule
    └─ [Sprint 5] Can add:
       ├─ TypeSpecificRule
       ├─ VirtualMeetingRule
       ├─ GroupSizeRule
       └─ [More rules as needed]

Events System:
    EventObserver interface
    ├─ onAppointmentBooked()
    ├─ onAppointmentModified()
    ├─ onAppointmentCancelled()
    ├─ onSlotCancelled()
    └─ [Sprint 5] Can add:
       ├─ onAppointmentCompleted()
       ├─ onAppointmentRescheduled()
       └─ [More event handlers]

Repository Queries:
    AppointmentBookingRepository
    ├─ findByCustomerName() ............ [Sprint 4] NEW
    ├─ [Sprint 5] Can add:
    │  ├─ findByType(AppointmentType)
    │  ├─ findByStatus(AppointmentStatus)
    │  ├─ findByDateRange(from, to)
    │  └─ findWithPagination(page, size)

AppointmentType Enum:
    Currently defined
    ├─ URGENT
    ├─ FOLLOW_UP
    ├─ ASSESSMENT
    ├─ VIRTUAL
    ├─ IN_PERSON
    ├─ INDIVIDUAL
    └─ GROUP
    
    [Sprint 5] Use for:
    ├─ Type-specific validation rules
    ├─ Type-specific notifications
    ├─ Type-based filtering in UI
    └─ Type-based reporting
```

---

**Diagram Generated**: March 31, 2026
**Status**: Sprint 4 Architecture Complete
**Ready for Integration**: Yes
**Ready for Sprint 5 Extension**: Yes

