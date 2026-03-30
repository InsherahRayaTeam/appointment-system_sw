# Login Flow Implementation - Your Questions Answered

## After implementing, show me:

### 1. Where login starts in Main

**File:** `src/main/java/org/example/Main.java`

**EXACT CODE LOCATION - Lines 77-80:**

```java
try (Scanner scanner = new Scanner(System.in)) {
    ConsoleInputHandler inputHandler = new ConsoleInputHandler(scanner);
    
    // ← LOGIN SCREEN CREATED HERE (Line 78)
    ConsoleLogin loginScreen = new ConsoleLogin(authService);
    
    ConsoleMenu adminMenu = new ConsoleMenu(inputHandler, authService, appointmentService, sessionManager);
    
    // === MAIN APPLICATION LOOP === (Lines 85-127)
    boolean applicationRunning = true;
    
    while (applicationRunning) {
        // ========================================
        // PHASE 1: LOGIN - User MUST authenticate
        // ========================================
        LoginPromptResult loginResult = loginScreen.promptForResult(scanner);
        //                                          ↑
        //                          LOGIN STARTS HERE!
```

**Key Points:**
- ✅ **First thing created in try block**: `ConsoleLogin loginScreen`
- ✅ **First thing called in app loop**: `loginScreen.promptForResult(scanner)`
- ✅ **No menu access before login**: Menu only called after successful login
- ✅ **Enforced before anything**: Login happens in PHASE 1, menu in PHASE 3

---

### 2. How ConsoleMenu checks login state

**File:** `src/main/java/org/example/presentation/ConsoleMenu.java`

**NEW METHOD - Added for separated concerns:**

```java
/**
 * Displays the admin menu and handles user interactions until logout.
 * IMPORTANT: User must already be authenticated before calling this method.
 * Use this method when authentication is handled externally (e.g., in Main).
 * 
 * Prerequisite: sessionManager.isLoggedIn() must be true
 */
public void runAdminMenuUntilLogout() {
    printAppHeader();
    showAdminMenu();  // ← Only shows menu (no login logic)
    // After logout, sessionManager.isLoggedIn() returns false and menu exits
}
```

**LOGIN STATE CHECK - In showAdminMenu() method:**

```java
private void showAdminMenu() {
    // This loop runs ONLY while user is logged in
    while (sessionManager.isLoggedIn()) {  // ← LOGIN STATE CHECK
        bookingView.printSectionHeader("Admin Menu");
        System.out.println("7. View Slots");
        System.out.println("8. Book Appointment");
        System.out.println("9. Logout");
        bookingView.printSeparator();

        int choice = inputHandler.readMenuChoice("Choose an option (7-9): ", 7, 9);
        
        if (choice == 7) {
            handleViewSlots();
        } else if (choice == 8) {
            handleBookAppointment();
        } else {
            // User chose logout
            handleLogout();  // ← Clears session
        }
        System.out.println();
    }
    // Loop exits when sessionManager.isLoggedIn() == false
}
```

**HOW IT WORKS:**
```
1. Main calls: adminMenu.runAdminMenuUntilLogout()
   ↓
2. runAdminMenuUntilLogout() calls: showAdminMenu()
   ↓
3. showAdminMenu() checks: while(sessionManager.isLoggedIn())
   ├─ TRUE:  Show menu options, handle user input
   └─ FALSE: Exit loop, return to Main
   ↓
4. User picks option 9 (Logout)
   ↓
5. handleLogout() called → sessionManager.logoutAndNotify()
   ↓
6. sessionManager.isLoggedIn() → FALSE
   ↓
7. While loop condition fails
   ↓
8. showAdminMenu() returns
   ↓
9. runAdminMenuUntilLogout() returns
   ↓
10. Main loop continues
    ↓
11. Back to login prompt
```

---

### 3. What happens on login success/failure

#### SUCCESS PATH

**File:** `src/main/java/org/example/Main.java` - Lines 105-127

```java
// ========================================
// PHASE 2: LOGIN SUCCESS - Create session
// ========================================
if (loginResult.isSuccess()) {
    String authenticatedUsername = loginResult.getUsername();
    
    // Create session in SessionManager (via service layer)
    // This marks user as authenticated
    sessionManager.login(authenticatedUsername);  // ← Session created
    System.out.println("✓ Login successful. Welcome, " + authenticatedUsername + ".\n");
    
    // ========================================
    // PHASE 3: ADMIN MENU - User can access menu
    // ========================================
    // Menu runs until user logs out
    // SessionManager.isLoggedIn() used by menu to verify auth state
    adminMenu.runAdminMenuUntilLogout();  // ← Now show menu
    
    // ========================================
    // PHASE 4: LOGOUT - Clear session, loop to login
    // ========================================
    // sessionManager.isLoggedIn() now returns false
    // adminMenu returns control to here
    // Loop continues, showing login screen again
    System.out.println();  // blank line for readability
}
```

**SUCCESS FLOW:**

```
User enters: admin / admin123
    ↓
ConsoleLogin.promptForResult() called
    ↓
AdminAuthService.authenticateWithPolicy() called
    ↓
Repository: findByUsername("admin")
    ↓
Returns: Optional[AdminUser("admin", "admin123")]
    ↓
Compare: "admin123".equals("admin123") → TRUE
    ↓
LoginPromptResult.SUCCESS returned
    ↓
Main checks: if (loginResult.isSuccess()) → TRUE
    ↓
sessionManager.login("admin") called
    ├─ Sets internal state: isLoggedIn() = true
    ├─ Notifies observers
    └─ Session created
    ↓
Shows: "✓ Login successful. Welcome, admin."
    ↓
adminMenu.runAdminMenuUntilLogout() called
    ↓
Admin menu displays
```

#### FAILURE PATH

**File:** `src/main/java/org/example/Main.java` - Lines 92-103

```java
// User cancelled login (typed 'q') - Exit application
if (loginResult.getStatus() == LoginPromptStatus.CANCELLED) {
    applicationRunning = false;
    break;  // ← Exit app
}

// Login failed (locked or wrong credentials) - Loop back to login
if (loginResult.getStatus() == LoginPromptStatus.LOCKED) {
    // ConsoleLogin already showed lock message
    // Loop continues to show login again
    continue;  // ← Back to login prompt
}

// If we get here: not cancelled, not locked, not success
// So status must be FAILED - loop back to login automatically
```

**FAILURE FLOW - Wrong Credentials:**

```
User enters: admin / wrongpass
    ↓
ConsoleLogin.promptForResult() called
    ↓
AdminAuthService.authenticateWithPolicy() called
    ↓
Repository: findByUsername("admin")
    ↓
Returns: Optional[AdminUser("admin", "admin123")]
    ↓
Compare: "admin123".equals("wrongpass") → FALSE
    ↓
Track failure: loginAttemptTracker.recordFailure()
    ├─ Attempts: 3 → 2
    └─ Still not locked
    ↓
ConsoleLogin shows: "❌ Invalid username or password."
                    "Attempts remaining: 2"
    ↓
LoginPromptResult.FAILED returned
    ↓
Main checks:
├─ if (loginResult.getStatus() == CANCELLED) → FALSE
├─ if (loginResult.getStatus() == LOCKED) → FALSE
└─ (Not success, so status is FAILED)
    ↓
Loop continues to next iteration
    ↓
Show login screen AGAIN
    ↓
User can retry
```

**FAILURE FLOW - After 3 Wrong Attempts:**

```
Attempt 3: Wrong password
    ↓
loginAttemptTracker.recordFailure()
    ├─ Attempts: 1 → 0
    ├─ Check: isLocked() → TRUE
    └─ Lock account for 30 seconds
    ↓
ConsoleLogin shows: "⚠️  Too many failed login attempts.
                      Please try again in 30 second(s)."
    ↓
LoginPromptResult.LOCKED returned
    ↓
Main checks: if (loginResult.getStatus() == LOCKED) → TRUE
    ↓
continue;  // ← Back to login prompt (loop continues)
    ↓
Show login screen AGAIN
    ↓
User sees lockout message again if they try
    ↓
Wait 30 seconds...
    ↓
Try again with correct password
    ├─ isLocked() → FALSE (timeout reached)
    ├─ Password validation proceeds
    └─ If correct → SUCCESS ✅
```

---

### 4. What happens after logout

**File:** `src/main/java/org/example/presentation/ConsoleMenu.java`

```java
private void handleLogout() {
    sessionManager.logoutAndNotify();  // ← Clear session via service
    System.out.println("You have been logged out successfully.");
}
```

**LOGOUT FLOW:**

```
User in Admin Menu
    ↓
User chooses: 9. Logout
    ↓
ConsoleMenu.handleLogout() called
    ↓
sessionManager.logoutAndNotify() called
    ├─ Clear session state
    ├─ Set isLoggedIn() = false
    └─ Notify observers ("Admin logged out")
    ↓
Show: "You have been logged out successfully."
    ↓
showAdminMenu() loop checks: while(sessionManager.isLoggedIn())
    ├─ isLoggedIn() == FALSE
    └─ Loop condition fails
    ↓
Exit showAdminMenu() loop
    ↓
Return from runAdminMenuUntilLogout()
    ↓
Return from runAdminMenuUntilLogout() in Main
    ↓
Main loop continues: while (applicationRunning)
    ├─ applicationRunning == TRUE (still running)
    └─ Next iteration begins
    ↓
loginScreen.promptForResult() called AGAIN
    ↓
Show login prompt again:
"Administrator username: "
"Administrator password: "
    ↓
User can:
├─ Login again with credentials
├─ Or press 'q' to exit app
└─ Or enter wrong credentials (get lockout tracking again)
```

**SEQUENCE DIAGRAM:**

```
Main.main()
    ↓
[Outer Loop: while (applicationRunning)]
    ├─ 1st iteration:
    │   ├─ Show login screen
    │   ├─ User: admin / admin123
    │   ├─ Login SUCCESS
    │   ├─ Show Admin Menu
    │   ├─ User works...
    │   ├─ User: 9. Logout
    │   ├─ Session cleared
    │   └─ Menu returns
    │
    ├─ 2nd iteration:
    │   ├─ Show login screen AGAIN
    │   ├─ User: admin / admin123
    │   ├─ Login SUCCESS
    │   ├─ Show Admin Menu AGAIN
    │   └─ ...
    │
    └─ Nth iteration:
        ├─ Show login screen
        ├─ User: q (to quit)
        ├─ applicationRunning = false
        └─ break (exit outer loop)
            ↓
        Show: "Thank you for using Appointment System."
            ↓
        Application exits
```

---

## 5. Login Credentials

**Default Admin (created at startup):**
```
Username: admin
Password: admin123
```

**Stored in:** `src/main/resources/admin.properties`
```properties
admin.username=admin
admin.password=admin123
```

**Loaded by:** `InMemoryAdminRepository.loadFromResource()`

---

## 6. Architecture Verification

### Clean Separation of Concerns

✅ **Main.java**
- Orchestrates overall app flow
- Manages login loop
- Creates session
- Calls menu display
- NO authentication logic

✅ **ConsoleLogin.java**
- Gets username/password from user
- Delegates to AdminAuthService for validation
- Returns LoginPromptResult
- NO hardcoded checks

✅ **ConsoleMenu.java**
- New method: `runAdminMenuUntilLogout()` (menu only, no login)
- Checks: `sessionManager.isLoggedIn()`
- Calls: `sessionManager.logoutAndNotify()`
- NO hardcoded checks

✅ **AdminAuthService.java**
- Validates credentials against repository
- Applies lockout policy
- Returns AuthenticationAttemptResult
- All business logic here

✅ **SessionManager.java**
- Tracks login state
- `login(username)` - create session
- `logout()` - clear session
- `isLoggedIn()` - check state

### No Hardcoding Anywhere

```
Main:     ❌ NOT if (username == "admin")
          ❌ NOT if (password == "admin123")
          ✅ Uses AdminAuthService and SessionManager

ConsoleLogin: ❌ NOT if (username == "admin")
              ❌ NOT if (password == "admin123")
              ✅ Delegates to AdminAuthService

ConsoleMenu:  ❌ NOT if (username == "admin")
              ❌ NOT any auth checks
              ✅ Checks sessionManager.isLoggedIn()

Service Layer: ✅ All auth logic here
               ✅ Queries repository
               ✅ Compares password
```

---

## Complete Request Checklist

✅ **1. Login prompt is FIRST thing user sees**
   - ConsoleLogin created and called in PHASE 1

✅ **2. User MUST authenticate before menu access**
   - Menu only called after loginResult.isSuccess()
   - Session created before menu display

✅ **3. Use AdminAuthService for authentication**
   - consoleLogin.promptForResult() calls authService.authenticateWithPolicy()

✅ **4. Use SessionManager to track login state**
   - sessionManager.login() on success
   - sessionManager.logoutAndNotify() on logout
   - sessionManager.isLoggedIn() checked by menu

✅ **5. Keep authentication in service layer**
   - NO hardcoded checks in Main or ConsoleMenu
   - All validation through AdminAuthService

✅ **6. Handle incorrect credentials**
   - Wrong credentials → show error → allow retry
   - 3 failed attempts → lockout for 30 seconds

✅ **7. Handle correct credentials**
   - Create session
   - Proceed to ConsoleMenu

✅ **8. Handle logout**
   - Clear session
   - Return to login screen
   - Loop back to step 1

✅ **9. No hardcoding in ConsoleMenu or Main**
   - All service calls
   - All proper delegation

✅ **10. Default admin created at startup**
   - Loaded from admin.properties
   - Falls back to hardcoded default if file missing

---

## Summary

**Login Flow Now:**
```
Main starts
    ↓
Create login screen
    ↓
PHASE 1: LOGIN LOOP
├─ Show login prompt
├─ Get credentials
├─ Validate via service
├─ Handle success/failure/lockout
└─ On success: proceed to PHASE 2
    ↓
PHASE 2: CREATE SESSION
├─ sessionManager.login(username)
├─ Show success message
└─ Proceed to PHASE 3
    ↓
PHASE 3: ADMIN MENU
├─ Show menu
├─ Handle user choices
├─ Check: while(sessionManager.isLoggedIn())
├─ User chooses logout
└─ Session cleared
    ↓
PHASE 4: BACK TO LOGIN
├─ Loop continues
└─ Back to PHASE 1
```

**All requirements met!** ✅

