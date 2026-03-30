# Visual Architecture & Flow Diagrams

## 1. Complete Authentication Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     APPOINTMENT SYSTEM STARTUP                          │
└─────────────────────┬───────────────────────────────────────────────────┘
                      │
                      ▼
          ┌─────────────────────────┐
          │   Main.main() starts    │
          └────────────┬────────────┘
                       │
                       ▼
          ┌──────────────────────────────────────┐
          │ new InMemoryAdminRepository()         │
          │   → constructor runs                 │
          │   → loadFromResource()               │
          │   → loads admin.properties           │
          └────────────┬─────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
   ┌─────────────────┐        ┌──────────────────┐
   │ File exists?    │        │ Create default   │
   │ YES/NO          │        │ admin: admin/123 │
   │                 │        │ if loading fails │
   │ Read:           │        └──────────────────┘
   │ admin=admin     │
   │ password=123    │
   └────────┬────────┘
            │
            ▼
┌────────────────────────────────────────────────┐
│     HashMap<String, AdminUser> populated       │
│     {                                          │
│       "admin": AdminUser("admin", "admin123")  │
│     }                                          │
└────────────────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────────────────────┐
│           Application Ready for Login                │
│        adminRepository.findByUsername("admin")       │
│        → returns Optional[AdminUser(...)]            │
└──────────────────────────────────────────────────────┘
```

## 2. User Login Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                    CONSOLE MENU LOGIN SCREEN                     │
└─────────────────────┬────────────────────────────────────────────┘
                      │
         ┌────────────┴────────────┐
         │                         │
         ▼                         ▼
   ┌──────────────┐         ┌──────────────┐
   │ Username:    │         │ Password:    │
   │ admin        │         │ ••••••••     │
   └──────┬───────┘         └──────┬───────┘
          │                        │
          └────────────┬───────────┘
                       │
                       ▼
   ┌─────────────────────────────────────┐
   │ Create Credentials object           │
   │ new Credentials("admin", "admin123")│
   └────────────┬────────────────────────┘
                │
                ▼
   ┌────────────────────────────────────────────┐
   │ AdminAuthService.                          │
   │   authenticateWithPolicy(Credentials)      │
   └────────────┬─────────────────────────────┘
                │
                ▼
         ┌──────────────────┐
         │ Account locked?  │
         │ (3 strikes + 30s)│
         └┬────────────────┬┘
          │ YES            │ NO
          │                ▼
          │      ┌──────────────────────────┐
          │      │ Query Repository:        │
          │      │ findByUsername("admin")  │
          │      └───────────┬──────────────┘
          │                  │
          │                  ▼
          │      ┌───────────────────────────┐
          │      │ Admin found?              │
          │      │ Optional[AdminUser]       │
          │      └┬──────────────────────────┤
          │        │ YES              │ NO
          │        │                  ▼
          │        │      ┌─────────────────────┐
          │        │      │ Return:             │
          │        │      │ INVALID_CREDENTIALS │
          │        │      └─────────┬───────────┘
          │        │                │
          │        ▼                │
          │ ┌──────────────────────┐│
          │ │ Compare passwords:   ││
          │ │ stored == input?     ││
          │ └┬──────────────────┬──┘│
          │  │ YES      │ NO    │   │
          │  │          ▼       ▼   │
          │  │    ┌──────────┐  │   │
          │  │    │ INVALID_ │  │   │
          │  │    │ CREDS    │  │   │
          │  │    └──────────┘  │   │
          │  │          ▲        │   │
          │  │          └────────┘   │
          │  │                       │
          │  ▼                       │
          │ ┌────────────────────┐   │
          │ │ Record Success     │   │
          │ │ Reset lockout      │   │
          │ │ Return: SUCCESS    │   │
          │ └──────────┬─────────┘   │
          │            │              │
          │     ┌──────┴──────┐       │
          │     │             │       │
          └─────┼─────────────┼───────┘
                │             │
                ▼             ▼
     ┌──────────────────┐  ┌──────────────────┐
     │ Lockout Result   │  │ Auth Result      │
     │ Remaining: XX s  │  │ (SUCCESS/FAILED) │
     └────────┬─────────┘  └────────┬─────────┘
              │                     │
              └──────────┬──────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │ ConsoleMenu displays │
              │ result to user       │
              └──────────────────────┘
```

## 3. Data Flow Through Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                            │
│                   (User Interface Only)                            │
│                                                                     │
│  ┌──────────────────────────────────────┐                         │
│  │ ConsoleMenu.showLoginScreen()         │                         │
│  │  • Prompt for username               │                         │
│  │  • Prompt for password               │                         │
│  │  • Create Credentials object         │                         │
│  │  • Call service layer                │                         │
│  └────────────────┬─────────────────────┘                         │
│                   │                                                 │
└───────────────────┼─────────────────────────────────────────────────┘
                    │ Passes Credentials object
                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                                  │
│                (Business Logic & Policy)                           │
│                                                                     │
│  ┌──────────────────────────────────────┐                         │
│  │ AdminAuthService                      │                         │
│  │  • Validate input (blank check)      │                         │
│  │  • Check lockout status              │                         │
│  │  • Query repository for user         │                         │
│  │  • Compare password                  │                         │
│  │  • Track failed attempts             │                         │
│  │  • Send event notifications          │                         │
│  │  • Return AuthenticationAttemptResult│                         │
│  └────────────────┬─────────────────────┘                         │
│                   │                                                 │
└───────────────────┼─────────────────────────────────────────────────┘
                    │ Query: findByUsername(String)
                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                               │
│               (Data Access Abstraction)                             │
│                                                                     │
│  ┌──────────────────────────────────────┐                         │
│  │ AdminRepository (interface)           │                         │
│  │  • findByUsername(String username):  │                         │
│  │    Optional<AdminUser>               │                         │
│  │                                       │                         │
│  │ InMemoryAdminRepository (impl)       │                         │
│  │  • HashMap<String, AdminUser>        │                         │
│  │  • Loaded from admin.properties      │                         │
│  │  • Returns Optional[AdminUser] or    │                         │
│  │    Optional.empty()                  │                         │
│  └────────────────┬─────────────────────┘                         │
│                   │                                                 │
└───────────────────┼─────────────────────────────────────────────────┘
                    │ Returns Optional<AdminUser>
                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                                   │
│               (Data Models & Entities)                              │
│                                                                     │
│  ┌──────────────────────────────────────┐                         │
│  │ AdminUser                             │                         │
│  │  • username: String                  │                         │
│  │  • password: String                  │                         │
│  │                                       │                         │
│  │ Credentials                           │                         │
│  │  • username: String                  │                         │
│  │  • password: String                  │                         │
│  │                                       │                         │
│  │ LoginStatus (enum)                   │                         │
│  │  • SUCCESS                           │                         │
│  │  • INVALID_CREDENTIALS               │                         │
│  │  • BLANK_INPUT                       │                         │
│  │                                       │                         │
│  │ AuthenticationAttemptResult           │                         │
│  │  • status: LoginStatus               │                         │
│  │  • locked: boolean                   │                         │
│  │  • attemptsRemaining: int            │                         │
│  └──────────────────────────────────────┘                         │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 4. InMemoryAdminRepository Initialization

```
New Application Instance
         │
         ▼
Main.main() {
    new InMemoryAdminRepository()
}
         │
         ▼
InMemoryAdminRepository() {
    admins = new HashMap<>()
    loadFromResource()
}
         │
         ▼
loadFromResource() {
    Properties p = new Properties()
    load("/admin.properties")
}
         │
    ┌────┴────┐
    │          │
    ▼          ▼
 FOUND      NOT FOUND
  │            │
  │            ▼
  │       Try hardcoded default:
  │       admins.put("admin",
  │         new AdminUser("admin", "admin123"))
  │            │
  └────┬───────┘
       │
       ▼
admins = {
  "admin" → AdminUser("admin", "admin123")
}
       │
       ▼
Ready to handle:
adminRepository.findByUsername("admin")
→ Optional[AdminUser("admin", "admin123")]
```

## 5. Password Validation Sequence

```
Input: username="admin", password="admin123"

Step 1: Get from Repository
────────────────────────────
adminRepository.findByUsername("admin")
                     ↓
            HashMap.get("admin")
                     ↓
    Optional[AdminUser("admin", "admin123")]

Step 2: Extract Password from AdminUser
─────────────────────────────────────────
AdminUser.getPassword()
        ↓
  Returns: "admin123"

Step 3: Compare Passwords
─────────────────────────
"admin123".equals("admin123")
        ↓
     → TRUE ✅

Result: LoginStatus.SUCCESS

Alternative (wrong password):
─────────────────────────────
Input password: "wrong123"

"admin123".equals("wrong123")
        ↓
     → FALSE ❌

Result: LoginStatus.INVALID_CREDENTIALS
```

## 6. Lockout Policy Timeline

```
Timeline:    0s     5s    10s    15s    20s    25s    30s    35s
             │      │     │      │      │      │      │      │
Attempt 1    X      (fail) → "Attempts: 2"
             │             
Attempt 2         (fail) → "Attempts: 1"
             │             
Attempt 3              (fail) → "LOCKED - try in 30s"
             │                   └─────locked─────────┘
             │                                         ✓ Unlocked
             │
User retry at 15s     → LOCKED - 15 seconds remaining
User retry at 35s     → LOGIN ALLOWED (try new credentials)
```

## 7. Integration Points

```
Main.java
   │
   ├─ Creates: AdminRepository (InMemoryAdminRepository)
   │              │
   │              └─ Loads from: src/main/resources/admin.properties
   │
   ├─ Creates: AdminAuthService
   │              │
   │              ├─ Uses: AdminRepository.findByUsername()
   │              ├─ Uses: EventManager (notifications)
   │              └─ Uses: LoginAttemptTracker (lockout policy)
   │
   ├─ Creates: ConsoleMenu
   │              │
   │              └─ Uses: AdminAuthService.authenticateWithPolicy()
   │
   └─ Creates: Scanner (for ConsoleInputHandler)
```

## 8. File Structure & Responsibilities

```
src/main/resources/
└── admin.properties ─────────────────── Stores default credentials
                                        (admin / admin123)

src/main/java/org/example/
│
├── domain/
│   ├── AdminUser.java ──────────────── Entity: username + password
│   ├── Credentials.java ────────────── DTO: credential payload
│   └── LoginStatus.java ────────────── Enum: SUCCESS, INVALID_*, BLANK_*
│
├── repository/
│   ├── AdminRepository.java ────────── Interface: findByUsername()
│   └── InMemoryAdminRepository.java ─ Implementation: loads properties,
│                                       stores in HashMap
│
├── service/
│   ├── AdminAuthService.java ──────── Validates credentials,
│   │                                   applies policy
│   ├── LoginAttemptTracker.java ──── Tracks attempts & lockout
│   ├── EventManager.java ──────────── Sends notifications
│   └── AuthenticationAttemptResult.java Result wrapper with policy info
│
├── presentation/
│   ├── ConsoleMenu.java ───────────── UI orchestration (NO auth logic)
│   ├── ConsoleLogin.java ──────────── Login prompts
│   └── ConsoleInputHandler.java ──── I/O helper
│
└── Main.java ────────────────────────── Wires everything together
```

## 9. Key Method Call Sequence

```
1. Application Starts
   Main.main()
        │
        ▼
2. Repository Loads Credentials
   new InMemoryAdminRepository()
        │
        ├─ loadFromResource()
        │   └─ admins.put("admin", AdminUser("admin", "admin123"))
        │
        ▼
3. User Logs In
   ConsoleMenu.showLoginScreen()
        │
        ├─ inputHandler.readLine("Username: ")    → "admin"
        ├─ inputHandler.readLine("Password: ")    → "admin123"
        │
        ▼
4. Create Credential Payload
   new Credentials("admin", "admin123")
        │
        ▼
5. Authenticate with Policy
   authService.authenticateWithPolicy(credentials)
        │
        ├─ isLocked() → NO
        ├─ authenticateWithStatus(credentials)
        │   │
        │   ├─ adminRepository.findByUsername("admin")
        │   │      │
        │   │      ├─ admins.get("admin")
        │   │      │   └─ Returns: AdminUser("admin", "admin123")
        │   │      │
        │   │      └─ adminUser.getPassword().equals("admin123")
        │   │         └─ "admin123".equals("admin123") → TRUE
        │   │
        │   └─ Return: LoginStatus.SUCCESS
        │
        ├─ eventManager.notifyObservers("Admin logged in successfully")
        │
        └─ Return: AuthenticationAttemptResult.success()
           │
           ▼
6. Handle Result
   if (result.isSuccess()) {
       sessionManager.login("admin")
       System.out.println("Login successful")
   }
```

---

This visual guide shows the complete architecture and flow of your admin credential system!

