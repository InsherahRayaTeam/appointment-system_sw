# Persistent Admin Credentials Implementation

## ✅ Status: READY FOR LOGIN

The appointment system now has persistent admin credentials properly implemented. Here's how everything works:

---

## 1. Where Default Admin is Created

### File: `src/main/resources/admin.properties`
```properties
# Demo administrator credentials
admin.username=admin
admin.password=admin123
```

**How it works:**
- When the app starts, the repository loads this properties file
- If the file exists and contains valid credentials, those are used
- If the file is missing/corrupted, it falls back to hardcoded defaults (also `admin`/`admin123`)

---

## 2. How the Repository Stores It

### File: `src/main/java/org/example/repository/InMemoryAdminRepository.java`

**Initialization:**
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
        // Fall back to default if loading fails
    }
    
    // Default admin if not loaded
    if (admins.isEmpty()) {
        admins.put("admin", new AdminUser("admin", "admin123"));
    }
}
```

**Storage:**
- Uses an in-memory `HashMap<String, AdminUser>` 
- Key: username ("admin")
- Value: `AdminUser` object with username and password

**Lookup Method:**
```java
@Override
public Optional<AdminUser> findByUsername(String username) {
    return Optional.ofNullable(admins.get(username));
}
```

---

## 3. How AdminAuthService Validates Login

### File: `src/main/java/org/example/service/AdminAuthService.java`

**Core Authentication Flow:**
```java
public AuthenticationAttemptResult authenticateWithPolicy(Credentials credentials) {
    // Step 1: Check if account is locked
    if (loginAttemptTracker.isLocked()) {
        return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
    }

    // Step 2: Validate credentials
    LoginStatus status = authenticateWithStatus(credentials);
    
    if (status == LoginStatus.SUCCESS) {
        loginAttemptTracker.recordSuccess();
        return AuthenticationAttemptResult.success();
    }

    // Step 3: Track failed attempt
    loginAttemptTracker.recordFailure();
    if (loginAttemptTracker.isLocked()) {
        return AuthenticationAttemptResult.locked(loginAttemptTracker.getRemainingLockSeconds());
    }

    return AuthenticationAttemptResult.failure(status, loginAttemptTracker.getAttemptsRemaining());
}
```

**Validation Logic:**
```java
public LoginStatus authenticateWithStatus(Credentials credentials) {
    // Validate input
    String username = credentials.getUsername();
    String password = credentials.getPassword();
    
    if (isBlank(username) || isBlank(password)) {
        return LoginStatus.BLANK_INPUT;
    }

    String normalizedUsername = username.trim();

    // Look up user in repository and compare password
    boolean authenticated = adminRepository.findByUsername(normalizedUsername)
            .map(admin -> admin.getPassword().equals(password))  // Plain text comparison
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

**Features:**
- ✅ Queries repository for user by username
- ✅ Compares plaintext password (acceptable for this project)
- ✅ Returns `LoginStatus.SUCCESS` or `LoginStatus.INVALID_CREDENTIALS`
- ✅ Sends event notifications for logging
- ✅ Applies lockout policy (3 failed attempts, 30-second lockout)

---

## 4. Authentication Flow Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        USER INPUT                           │
│                   (ConsoleMenu.java)                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  AUTHENTICATION SERVICE                     │
│              (AdminAuthService.java)                        │
│  • Validates input (blank check)                            │
│  • Queries repository for user                             │
│  • Compares password                                       │
│  • Applies lockout policy                                  │
│  • Sends notifications                                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    ADMIN REPOSITORY                         │
│          (InMemoryAdminRepository.java)                    │
│  • Stores admin users in HashMap                           │
│  • Loads from admin.properties at startup                  │
│  • Provides findByUsername() method                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN MODEL                             │
│                (AdminUser.java)                            │
│  • username: "admin"                                       │
│  • password: "admin123"                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Login Credentials to Use

### Default Admin User:
```
Username: admin
Password: admin123
```

### How to Test:

**Step 1: Start the application**
```bash
java -jar appointment-system.jar
# or run Main.java directly
```

**Step 2: When prompted for login, enter:**
```
Administrator username: admin
Administrator password: admin123
```

**Step 3: Expected result:**
```
Login successful. Welcome, admin.

Admin Menu
7. View Slots
8. Book Appointment
9. Logout
```

---

## 6. Layered Architecture Confirmation

### ✅ Repository Layer
- **`AdminRepository` (interface)**: Defines `findByUsername(String username): Optional<AdminUser>`
- **`InMemoryAdminRepository` (implementation)**: Stores and retrieves admin users
- **Responsibility**: Data access and persistence

### ✅ Service Layer
- **`AdminAuthService`**: Handles authentication logic
- **`LoginAttemptTracker`**: Handles lockout policy
- **`EventManager`**: Notifies observers of auth events
- **Responsibility**: Business logic, validation, policy enforcement

### ✅ Presentation Layer
- **`ConsoleMenu`**: Orchestrates UI flow, delegates to services
- **`ConsoleLogin`**: Prompts for credentials, displays results
- **`ConsoleInputHandler`**: Handles console I/O
- **Responsibility**: User interaction only, no authentication logic

### ✅ Domain Layer
- **`AdminUser`**: Represents admin data (username, password)
- **`Credentials`**: Immutable credential payload
- **`LoginStatus`**: Enum for authentication outcomes
- **`AuthenticationAttemptResult`**: Policy-aware result wrapper

---

## 7. Key Features

✅ **Persistent Storage**: Admin credentials loaded from `admin.properties` at startup  
✅ **Fallback Default**: Creates default admin if properties file missing  
✅ **Repository Pattern**: Clean separation of data access  
✅ **Service Validation**: All auth through `AdminAuthService`  
✅ **Lockout Policy**: 3 failed attempts → 30-second lockout  
✅ **Event Notifications**: Login events sent to observers  
✅ **Clean Architecture**: No hardcoded checks in UI layer  
✅ **Plain Text Passwords**: Simple and readable for development  

---

## 8. How to Verify It Works

After building/running the application:

1. **Start the app** → It loads the properties file
2. **Try correct credentials** → `admin` / `admin123` → Login succeeds ✅
3. **Try wrong password** → `admin` / `wrongpass` → Login fails ❌
4. **Try non-existent user** → `hacker` / `pass123` → Login fails ❌
5. **Try wrong 3 times** → Account locked for 30 seconds ⏱️
6. **After 30 seconds** → Can try again

---

## 9. Code References

| File | Purpose |
|------|---------|
| `src/main/resources/admin.properties` | Default credentials storage |
| `src/main/java/org/example/domain/AdminUser.java` | Admin entity |
| `src/main/java/org/example/repository/AdminRepository.java` | Repository interface |
| `src/main/java/org/example/repository/InMemoryAdminRepository.java` | In-memory storage + properties loading |
| `src/main/java/org/example/service/AdminAuthService.java` | Authentication logic |
| `src/main/java/org/example/service/AuthenticationAttemptResult.java` | Policy-aware result |
| `src/main/java/org/example/presentation/ConsoleMenu.java` | UI orchestration |
| `src/main/java/org/example/presentation/ConsoleLogin.java` | Login prompt |

---

## Next Steps

- ✅ **Done**: Default admin created and persistent
- ✅ **Done**: Repository properly stores users
- ✅ **Done**: Service validates credentials correctly
- ⏭️ **Future**: Replace plaintext with bcrypt/argon2 for production
- ⏭️ **Future**: Add database backend replacing in-memory storage
- ⏭️ **Future**: Add user management (create/edit/delete admins)


