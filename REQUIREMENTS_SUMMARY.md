# Implementation Summary - Your Questions Answered

## After Implementation, Show Me:

### 1. Where the Default Admin is Created

**File:** `src/main/resources/admin.properties`

```properties
admin.username=admin
admin.password=admin123
```

**How it works:**
- When the application starts, `InMemoryAdminRepository` constructor is called
- It calls `loadFromResource()` which loads this properties file
- Username "admin" and password "admin123" are extracted and stored
- If the file is missing, it falls back to hardcoded defaults: `admin` / `admin123`

**Code in InMemoryAdminRepository:**
```java
public InMemoryAdminRepository() {
    loadFromResource();
}

private void loadFromResource() {
    Properties p = new Properties();
    try (InputStream in = InMemoryAdminRepository.class.getResourceAsStream(RESOURCE)) {
        if (in != null) {
            p.load(in);
            String user = p.getProperty("admin.username");      // Gets "admin"
            String pass = p.getProperty("admin.password");      // Gets "admin123"
            if (user != null && pass != null) {
                admins.put(user.trim(), new AdminUser(user.trim(), pass.trim()));
            }
        }
    } catch (IOException e) {
        // Fall back to default
    }
    
    if (admins.isEmpty()) {
        admins.put("admin", new AdminUser("admin", "admin123"));  // Fallback
    }
}
```

---

### 2. How the Repository Stores It

**File:** `src/main/java/org/example/repository/InMemoryAdminRepository.java`

**Storage Structure:**
```
┌─────────────────────────────────────────┐
│   Private HashMap<String, AdminUser>    │
│   private final Map<String, AdminUser>  │
│   admins = new HashMap<>()              │
├─────────────────────────────────────────┤
│  Key: "admin"                           │
│  Value: AdminUser(                      │
│    username: "admin"                    │
│    password: "admin123"                 │
│  )                                      │
└─────────────────────────────────────────┘
```

**Retrieval:**
```java
@Override
public Optional<AdminUser> findByUsername(String username) {
    return Optional.ofNullable(admins.get(username));
}
```

**Example Call:**
```java
// Service calls:
adminRepository.findByUsername("admin")

// Returns:
// Optional[AdminUser("admin", "admin123")]

// If not found:
// Optional.empty()
```

---

### 3. How AdminAuthService Validates Login

**File:** `src/main/java/org/example/service/AdminAuthService.java`

**Validation Process:**

```java
public AuthenticationAttemptResult authenticateWithPolicy(Credentials credentials) {
    // 1. Check if account is locked
    if (loginAttemptTracker.isLocked()) {
        return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
    }

    // 2. Perform authentication
    LoginStatus status = authenticateWithStatus(credentials);
    
    if (status == LoginStatus.SUCCESS) {
        loginAttemptTracker.recordSuccess();
        return AuthenticationAttemptResult.success();
    }

    // 3. Track failed attempt
    loginAttemptTracker.recordFailure();
    if (loginAttemptTracker.isLocked()) {
        return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
    }

    return AuthenticationAttemptResult.failure(status, loginAttemptTracker.getAttemptsRemaining());
}
```

**Core Validation:**
```java
public LoginStatus authenticateWithStatus(Credentials credentials) {
    String username = credentials.getUsername();
    String password = credentials.getPassword();

    // Validate input
    if (isBlank(username) || isBlank(password)) {
        return LoginStatus.BLANK_INPUT;
    }

    String normalizedUsername = username.trim();

    // Query repository and compare password
    boolean authenticated = adminRepository.findByUsername(normalizedUsername)
            .map(admin -> admin.getPassword().equals(password))
            .orElse(false);

    if (authenticated) {
        eventManager.notifyObservers("Admin logged in successfully");
        return LoginStatus.SUCCESS;
    } else {
        eventManager.notifyObservers("Failed login attempt");
        return LoginStatus.INVALID_CREDENTIALS;
    }
}
```

**Step-by-Step Example (Success):**
```
Input: username="admin", password="admin123"

1. Check blank: NO → continue
2. Query: adminRepository.findByUsername("admin")
   → Returns: Optional[AdminUser("admin", "admin123")]
3. Get password: admin.getPassword()
   → Returns: "admin123"
4. Compare: "admin123".equals("admin123")
   → TRUE ✅
5. Return: LoginStatus.SUCCESS
6. Notify observers
7. Return: AuthenticationAttemptResult.success()
```

**Step-by-Step Example (Failure):**
```
Input: username="admin", password="wrongpass"

1. Check blank: NO → continue
2. Query: adminRepository.findByUsername("admin")
   → Returns: Optional[AdminUser("admin", "admin123")]
3. Get password: admin.getPassword()
   → Returns: "admin123"
4. Compare: "admin123".equals("wrongpass")
   → FALSE ❌
5. Return: LoginStatus.INVALID_CREDENTIALS
6. Track failure attempt
7. Return: AuthenticationAttemptResult.failure(...)
```

---

### 4. Example Login Credentials

**Use these credentials to run the app:**

```
Username: admin
Password: admin123
```

**Test Scenarios:**

**✅ Successful Login:**
```
Administrator username: admin
Administrator password: admin123

→ Login successful. Welcome, admin.
→ Admin Menu appears (7. View Slots, 8. Book Appointment, 9. Logout)
```

**❌ Wrong Password:**
```
Administrator username: admin
Administrator password: wrongpass

→ Invalid username or password. Please try again.
→ Attempts remaining: 2
```

**❌ Non-existent User:**
```
Administrator username: hacker
Administrator password: anypass

→ Invalid username or password. Please try again.
→ Attempts remaining: 2
```

**⏱️ Lockout After 3 Failed Attempts:**
```
Failed attempt 1 → "Attempts remaining: 2"
Failed attempt 2 → "Attempts remaining: 1"
Failed attempt 3 → "Account is temporarily locked. Try again in 30 second(s)."

Wait 30 seconds...

→ Can try again
```

---

## Architecture Summary

### Clean Layering Verified ✅

**Presentation Layer (ConsoleMenu.java)**
```java
AuthenticationAttemptResult result = 
    adminAuthService.authenticateWithPolicy(new Credentials(username, password));
if (result.isSuccess()) {
    sessionManager.login(username);
}
```
- Pure UI orchestration
- **NO** hardcoded authentication logic
- Delegates to service

**Service Layer (AdminAuthService.java)**
```java
boolean authenticated = adminRepository.findByUsername(normalizedUsername)
    .map(admin -> admin.getPassword().equals(password))
    .orElse(false);
```
- Validation logic
- Policy enforcement
- Repository queries

**Repository Layer (InMemoryAdminRepository.java)**
```java
@Override
public Optional<AdminUser> findByUsername(String username) {
    return Optional.ofNullable(admins.get(username));
}
```
- Data access abstraction
- HashMap storage
- Properties file loading

**Domain Layer (AdminUser.java, Credentials.java, LoginStatus.java)**
```java
public final class AdminUser {
    private final String username;
    private final String password;
}
```
- Entity models
- Transfer objects
- Result enums

---

## Files Modified

| File | Change |
|------|--------|
| `src/main/resources/admin.properties` | ✅ Password updated to `admin123` |

---

## Files Already Correct (No Changes Needed)

| File | Status |
|------|--------|
| `src/main/java/org/example/domain/AdminUser.java` | ✅ Ready |
| `src/main/java/org/example/domain/Credentials.java` | ✅ Ready |
| `src/main/java/org/example/repository/AdminRepository.java` | ✅ Ready |
| `src/main/java/org/example/repository/InMemoryAdminRepository.java` | ✅ Ready |
| `src/main/java/org/example/service/AdminAuthService.java` | ✅ Ready |
| `src/main/java/org/example/service/AuthenticationAttemptResult.java` | ✅ Ready |
| `src/main/java/org/example/presentation/ConsoleMenu.java` | ✅ Ready |
| `src/main/java/org/example/presentation/ConsoleLogin.java` | ✅ Ready |

---

## Key Features Implemented

✅ **Persistent Storage**: Admin credentials stored in `admin.properties`  
✅ **Repository Pattern**: Clean data access layer  
✅ **Service Validation**: Centralized authentication logic  
✅ **Default Admin**: Auto-created on first run  
✅ **Lockout Policy**: 3 failed attempts → 30-second lockout  
✅ **Event System**: Notifications for audit trail  
✅ **Clean Architecture**: No UI coupling with auth logic  
✅ **Plain Text Passwords**: Simple and readable for development  

---

## Quick Run Guide

1. Build: `mvn clean package`
2. Run: `java -cp target/appointment-system.jar org.example.Main`
3. Login with: `admin` / `admin123`
4. Use the admin menu to view/book appointments
5. Logout and repeat

---

## That's It! ✅

Your appointment system now has working, persistent admin credentials with a clean layered architecture. The default admin (`admin`/`admin123`) is automatically created when the app starts and properly validated through the service layer when you log in.

