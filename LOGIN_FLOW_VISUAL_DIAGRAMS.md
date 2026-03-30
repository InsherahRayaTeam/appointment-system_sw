# Login Flow - Visual Diagrams

## 1. Application Startup Sequence

```
┌─────────────────────────────────────────────────────────┐
│ Application Starts: java org.example.Main               │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │ Setup Dependencies:           │
        │ • AdminRepository loaded      │
        │ • AdminAuthService created   │
        │ • SessionManager created     │
        │ • ConsoleLogin created       │
        │ • ConsoleMenu created        │
        └──────────────┬───────────────┘
                       │
                       ▼
        ╔══════════════════════════════════════════╗
        ║                                          ║
        ║  MAIN APPLICATION LOOP                  ║
        ║  while (applicationRunning)             ║
        ║                                          ║
        ╚══════════════════════════════════════════╝
                       │
                       ▼
        ┌──────────────────────────────────────┐
        │  PHASE 1: LOGIN                      │
        │  User MUST authenticate              │
        │                                      │
        │  Show login prompt:                 │
        │  "Administrator username: "          │
        │  "Administrator password: "          │
        │                                      │
        │  Delegate to service layer:          │
        │  AdminAuthService.authenticate()     │
        └──────────────┬───────────────────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
    CANCELLED?    LOCKED?        SUCCESS?
         │             │             │
    Exit App      Loop back      Continue
                   to login
         │             │             │
         └─────────────┼─────────────┘
                       │
                       ▼ (if SUCCESS)
        ┌──────────────────────────────────────┐
        │  PHASE 2: CREATE SESSION             │
        │  sessionManager.login(username)      │
        │                                      │
        │  Show: "✓ Login successful..."       │
        └──────────────┬───────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────────────┐
        │  PHASE 3: ADMIN MENU                 │
        │  adminMenu.runAdminMenuUntilLogout()│
        │                                      │
        │  Show menu:                         │
        │  7. View Slots                      │
        │  8. Book Appointment                │
        │  9. Logout                          │
        │                                      │
        │  User interacts with menu...        │
        │  User chooses logout                │
        └──────────────┬───────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────────────┐
        │  PHASE 4: LOGOUT                     │
        │  sessionManager.logoutAndNotify()    │
        │                                      │
        │  Show: "Logged out successfully"     │
        └──────────────┬───────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │  LOOP BACK TO PHASE 1       │
        │  (Show login again)         │
        └─────────────────────────────┘
```

---

## 2. Login Success Path - Detailed

```
┌─────────────────────────────────────────────┐
│ User enters credentials:                    │
│ Username: admin                             │
│ Password: admin123                          │
└──────────────┬──────────────────────────────┘
               │
               ▼
       ┌───────────────────────────┐
       │ ConsoleLogin gets input   │
       └───────────────┬───────────┘
               │
               ▼
       ┌────────────────────────────────────┐
       │ Create Credentials object:         │
       │ new Credentials("admin",           │
       │                 "admin123")        │
       └───────────────┬────────────────────┘
               │
               ▼
       ┌────────────────────────────────────┐
       │ Call AdminAuthService:             │
       │ authenticateWithPolicy(credentials)│
       └───────────────┬────────────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ AdminAuthService execution:  │
       │                              │
       │ 1. Check if locked? NO       │
       │ 2. Validate input? YES       │
       │ 3. Query repository:         │
       │    findByUsername("admin")   │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Repository returns:          │
       │ Optional[AdminUser(          │
       │   username: "admin"          │
       │   password: "admin123"       │
       │ )]                           │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Extract password:            │
       │ admin.getPassword()          │
       │ → "admin123"                 │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Compare passwords:           │
       │ "admin123".equals(            │
       │   "admin123"                 │
       │ ) → TRUE ✅                  │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Return:                      │
       │ LoginStatus.SUCCESS          │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Send notification:           │
       │ "Admin logged in successfully"
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ ConsoleLogin returns:        │
       │ LoginPromptResult.SUCCESS    │
       │ (username: "admin")          │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Main.main() receives result  │
       │ loginResult.isSuccess() ==   │
       │ true                         │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Create session:              │
       │ sessionManager.login(        │
       │   "admin"                    │
       │ )                            │
       │                              │
       │ SessionManager now           │
       │ isLoggedIn() == true         │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Show success message:        │
       │ "✓ Login successful.         │
       │  Welcome, admin."            │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Call:                        │
       │ adminMenu.                   │
       │   runAdminMenuUntilLogout()  │
       └───────────────┬──────────────┘
               │
               ▼
       ┌──────────────────────────────┐
       │ Admin Menu displayed         │
       │ (User can now interact)      │
       └──────────────────────────────┘
```

---

## 3. Login Failure Path - Wrong Credentials

```
┌─────────────────────────────────────────┐
│ User enters credentials:                │
│ Username: admin                         │
│ Password: wrongpass                     │
└──────────────┬──────────────────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ ConsoleLogin gets input       │
       └───────────────┬───────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ Call AdminAuthService:         │
       │ authenticateWithPolicy(        │
       │   new Credentials("admin",     │
       │                   "wrongpass") │
       │ )                              │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ AdminAuthService execution:    │
       │                                │
       │ 1. Check if locked? NO         │
       │ 2. Query repository:           │
       │    findByUsername("admin")     │
       │    → returns AdminUser         │
       │ 3. Extract password:           │
       │    "admin123"                  │
       │ 4. Compare:                    │
       │    "admin123".equals(          │
       │      "wrongpass"               │
       │    ) → FALSE ❌                │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ Record failure attempt:        │
       │ loginAttemptTracker.           │
       │   recordFailure()              │
       │                                │
       │ Attempts remaining: 2          │
       │ (was 3, now 2)                 │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ Return:                        │
       │ AuthenticationAttemptResult    │
       │ .failure(                      │
       │   INVALID_CREDENTIALS,         │
       │   attemptsRemaining: 2         │
       │ )                              │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ ConsoleLogin shows error:      │
       │ "❌ Invalid username or        │
       │  password. Please try again."  │
       │                                │
       │ "Attempts remaining: 2"        │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ ConsoleLogin returns:          │
       │ LoginPromptResult.FAILED       │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ Main.main() receives result    │
       │ loginResult.isSuccess() ==     │
       │ false                          │
       │                                │
       │ status != CANCELLED            │
       │ status != LOCKED               │
       │ status == FAILED               │
       └───────────────┬────────────────┘
               │
               ▼
       ┌────────────────────────────────┐
       │ Continue to next loop          │
       │ iteration                      │
       │                                │
       │ Show login screen AGAIN        │
       │ User can retry                 │
       │                                │
       │ "Administrator username: "     │
       │ (prompt again)                 │
       └────────────────────────────────┘
```

---

## 4. Lockout Path - 3 Failed Attempts

```
Attempt 1: Wrong credentials
└─ Shows: "Attempts remaining: 2"
   └─ Loop back to login

Attempt 2: Wrong credentials
└─ Shows: "Attempts remaining: 1"
   └─ Loop back to login

Attempt 3: Wrong credentials
└─ Shows: "Attempts remaining: 0"
   └─ loginAttemptTracker.recordFailure()
   └─ Check: isLocked() → TRUE
   └─ Lock account for 30 seconds
   └─ Return: AuthenticationAttemptResult.locked(30)
   └─ ConsoleLogin shows: "⚠️  Too many failed attempts.
                            Please try again in 30 second(s)."
   └─ Return: LoginPromptStatus.LOCKED
   └─ Main sees LOCKED status
   └─ Continues loop
   └─ Show login screen again

[30 seconds pass...]

Attempt 4: Try again
└─ loginAttemptTracker.isLocked() → FALSE (timeout reached)
   └─ Lockout is cleared
   └─ Can try credentials again
   └─ If correct: LoginStatus.SUCCESS ✅
   └─ If wrong: Another failure, repeat process
```

---

## 5. Logout and Return to Login

```
┌──────────────────────────────────────┐
│ User in Admin Menu                   │
│                                      │
│ 7. View Slots                       │
│ 8. Book Appointment                 │
│ 9. Logout                           │
│                                      │
│ Choose an option (7-9): 9           │
└──────────────┬───────────────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ ConsoleMenu.handleLogout()    │
       │ called                        │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ sessionManager.               │
       │   logoutAndNotify()           │
       │                               │
       │ Clear session state:          │
       │ isLoggedIn() → false          │
       │                               │
       │ Notify observers:             │
       │ "Admin logged out"            │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ Show message:                 │
       │ "You have been logged out     │
       │  successfully."               │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ showAdminMenu() loop:         │
       │ while(                        │
       │   sessionManager.             │
       │   isLoggedIn()                │
       │ )                             │
       │ → FALSE (condition fails)     │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ Menu loop exits               │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ runAdminMenuUntilLogout()     │
       │ returns to Main.main()        │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ Main loop continues:          │
       │ while (applicationRunning)    │
       │ → still true                  │
       └───────────────┬───────────────┘
               │
               ▼
       ┌───────────────────────────────┐
       │ Show login screen AGAIN:      │
       │                               │
       │ "Administrator username: "    │
       │ (back to PHASE 1)             │
       └───────────────────────────────┘
```

---

## 6. Session State Flow

```
Application starts
    │
    ├─ SessionManager.isLoggedIn() → FALSE
    │
    ▼
User enters login screen
    │
    ├─ Shows: "Username: ", "Password: "
    │
    ▼
User enters credentials
    │
    ├─ AdminAuthService validates
    │
    ▼
├─ SUCCESS → SessionManager.login(username)
│             └─ isLoggedIn() → TRUE
│
├─ FAILURE → Stay at login
│             └─ isLoggedIn() → FALSE
│
└─ LOCKED  → Stay at login with error
             └─ isLoggedIn() → FALSE

While isLoggedIn() == TRUE:
    │
    ├─ ConsoleMenu.showAdminMenu() runs
    ├─ User can interact with menu
    ├─ User chooses logout
    │
    ▼
SessionManager.logoutAndNotify()
    │
    ├─ isLoggedIn() → FALSE
    │
    ▼
ConsoleMenu.showAdminMenu() loop exits
    │
    ├─ Menu returns to Main
    ├─ Main loops
    │
    ▼
Show login screen again
```

---

## 7. Architecture Layers - Data Flow

```
┌─────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER                                      │
│ ┌──────────────────────────────────────────────────┐   │
│ │ ConsoleLogin                                     │   │
│ │ • promptForResult(scanner)                       │   │
│ │ • Gets username/password from user               │   │
│ │ • Calls service layer for authentication         │   │
│ │ • Returns LoginPromptResult                      │   │
│ └──────────────┬───────────────────────────────────┘   │
│                │                                         │
│                │ passes Credentials                      │
│                ▼                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ ConsoleMenu                                      │   │
│ │ • runAdminMenuUntilLogout()                      │   │
│ │ • showAdminMenu()                                │   │
│ │ • Checks: sessionManager.isLoggedIn()            │   │
│ │ • Calls: sessionManager.logoutAndNotify()        │   │
│ └──────────────┬───────────────────────────────────┘   │
└─────────────────┼────────────────────────────────────────┘
                  │
                  │ Credentials object
                  ▼
┌─────────────────────────────────────────────────────────┐
│ SERVICE LAYER                                           │
│ ┌──────────────────────────────────────────────────┐   │
│ │ AdminAuthService                                 │   │
│ │ • authenticateWithPolicy(credentials)            │   │
│ │ • authenticateWithStatus(credentials)            │   │
│ │ • isLocked()                                     │   │
│ │ • Queries repository                             │   │
│ │ • Compares password                              │   │
│ │ • Applies lockout policy                         │   │
│ │ • Returns AuthenticationAttemptResult            │   │
│ └──────────────┬───────────────────────────────────┘   │
│                │                                         │
│                │ findByUsername()                        │
│                ▼                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ SessionManager                                   │   │
│ │ • login(username)                                │   │
│ │ • logout()                                       │   │
│ │ • isLoggedIn()                                   │   │
│ │ • logoutAndNotify()                              │   │
│ └──────────────────────────────────────────────────┘   │
└─────────────────┼────────────────────────────────────────┘
                  │
                  │ Query: findByUsername(string)
                  ▼
┌─────────────────────────────────────────────────────────┐
│ REPOSITORY LAYER                                        │
│ ┌──────────────────────────────────────────────────┐   │
│ │ AdminRepository (interface)                      │   │
│ │ • findByUsername(username)                       │   │
│ │   → Optional<AdminUser>                          │   │
│ └──────────────┬───────────────────────────────────┘   │
│                │                                         │
│                │ implements                              │
│                ▼                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ InMemoryAdminRepository                          │   │
│ │ • HashMap<String, AdminUser> admins              │   │
│ │ • loadFromResource() → loads admin.properties    │   │
│ │ • findByUsername(username)                       │   │
│ │   → returns Optional from HashMap                │   │
│ └──────────────┬───────────────────────────────────┘   │
│                │                                         │
│                │ Read: admin.properties                  │
│                ▼                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ Resources File                                   │   │
│ │ src/main/resources/admin.properties              │   │
│ │ admin.username=admin                             │   │
│ │ admin.password=admin123                          │   │
│ └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                  │
                  │ wraps
                  ▼
┌─────────────────────────────────────────────────────────┐
│ DOMAIN LAYER                                            │
│ ┌──────────────────────────────────────────────────┐   │
│ │ AdminUser                                        │   │
│ │ • username: String                               │   │
│ │ • password: String                               │   │
│ └──────────────────────────────────────────────────┘   │
│                                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ Credentials                                      │   │
│ │ • username: String                               │   │
│ │ • password: String                               │   │
│ └──────────────────────────────────────────────────┘   │
│                                                         │
│ ┌──────────────────────────────────────────────────┐   │
│ │ LoginStatus (enum)                               │   │
│ │ • SUCCESS                                        │   │
│ │ • INVALID_CREDENTIALS                            │   │
│ │ • BLANK_INPUT                                    │   │
│ └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

This visual guide shows the complete flow of the login system!

