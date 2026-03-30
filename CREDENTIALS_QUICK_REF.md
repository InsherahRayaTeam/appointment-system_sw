# Quick Reference: Admin Credentials & Architecture

## 🔑 Login Credentials
```
Username: admin
Password: admin123
```

## 📍 Key Locations

### 1. Default Admin Storage
**File:** `src/main/resources/admin.properties`
```properties
admin.username=admin
admin.password=admin123
```

### 2. Repository (Data Access)
**File:** `src/main/java/org/example/repository/InMemoryAdminRepository.java`
- Loads `admin.properties` on startup
- Stores users in `HashMap<String, AdminUser>`
- Method: `findByUsername(String username): Optional<AdminUser>`

### 3. Authentication Service
**File:** `src/main/java/org/example/service/AdminAuthService.java`
- Method: `authenticateWithPolicy(Credentials credentials): AuthenticationAttemptResult`
- Validates username/password against repository
- Applies 3-strike lockout policy (30 seconds)

### 4. UI Layer
**File:** `src/main/java/org/example/presentation/ConsoleMenu.java`
- Calls: `adminAuthService.authenticateWithPolicy(new Credentials(username, password))`
- No hardcoded auth checks

## 🔄 Authentication Flow

```
User enters credentials
         ↓
ConsoleMenu.showLoginScreen()
         ↓
AdminAuthService.authenticateWithPolicy(Credentials)
         ↓
Check if account locked?
    YES → return locked result
    NO  → continue
         ↓
Query: adminRepository.findByUsername(username)
         ↓
Compare: storedPassword == inputPassword?
    YES → LoginStatus.SUCCESS ✅
    NO  → LoginStatus.INVALID_CREDENTIALS ❌
         ↓
Track attempt & apply lockout policy
         ↓
Return AuthenticationAttemptResult
         ↓
ConsoleMenu displays result to user
```

## 📊 Authentication Result Types

| Status | Meaning | Next Action |
|--------|---------|-------------|
| `SUCCESS` | Valid credentials | Log user in |
| `INVALID_CREDENTIALS` | Wrong username/password | Show attempts remaining |
| `BLANK_INPUT` | Empty username or password | Show validation error |
| `LOCKED` | 3 failed attempts | Show countdown timer |

## 🛡️ Lockout Policy

- **Max Failed Attempts:** 3
- **Lockout Duration:** 30 seconds
- **After Lockout:** Can retry

## 🏛️ Architecture Layers

### Domain Layer (org.example.domain)
- `AdminUser` - Entity with username & password
- `Credentials` - Immutable payload for auth

### Repository Layer (org.example.repository)
- `AdminRepository` - Interface
- `InMemoryAdminRepository` - Implementation with properties loading

### Service Layer (org.example.service)
- `AdminAuthService` - Authentication logic
- `LoginAttemptTracker` - Lockout policy
- `EventManager` - Notifications

### Presentation Layer (org.example.presentation)
- `ConsoleMenu` - UI orchestration
- `ConsoleLogin` - Login prompts
- `ConsoleInputHandler` - I/O handling

## ✅ Verification

To verify everything works:

1. Run application
2. Enter: `admin` / `admin123`
3. Expected: Login succeeds ✅

Try invalid credentials:
- Enter: `admin` / `wrongpass`
- Expected: Login fails ❌

Try 3 times with wrong password:
- Expected: Account locked for 30 seconds ⏱️

## 🔍 Key Code Snippets

### Load Admin from Properties
```java
// InMemoryAdminRepository.loadFromResource()
Properties p = new Properties();
InputStream in = InMemoryAdminRepository.class.getResourceAsStream("/admin.properties");
p.load(in);
String user = p.getProperty("admin.username");      // "admin"
String pass = p.getProperty("admin.password");      // "admin123"
admins.put(user.trim(), new AdminUser(user.trim(), pass.trim()));
```

### Validate Credentials
```java
// AdminAuthService.authenticateWithStatus()
boolean authenticated = adminRepository.findByUsername(normalizedUsername)
    .map(admin -> admin.getPassword().equals(password))  // Compare plaintext
    .orElse(false);

if (authenticated) {
    return LoginStatus.SUCCESS;
} else {
    return LoginStatus.INVALID_CREDENTIALS;
}
```

### Apply Lockout Policy
```java
// AdminAuthService.authenticateWithPolicy()
if (loginAttemptTracker.isLocked()) {
    return AuthenticationAttemptResult.locked(
        loginAttemptTracker.getRemainingLockSeconds()
    );
}
```

## 📝 Notes

- Passwords stored **plaintext** (acceptable for demo/dev)
- For **production**: Use bcrypt/argon2 hashing
- Repository can be swapped for **database backend**
- Event system allows **audit logging**
- Lockout policy prevents **brute force attacks**

## 🎯 What's Implemented

✅ Persistent admin credentials from properties file  
✅ Proper repository layer for data access  
✅ Authentication service with validation  
✅ Lockout policy (3 attempts, 30-second lockout)  
✅ Event notifications  
✅ Clean layered architecture  
✅ No hardcoding in UI layer  
✅ Testable design  

## 🚀 Next Steps (Future)

- Add more admin users programmatically
- Implement password hashing (bcrypt/argon2)
- Add database backend (replace in-memory)
- Add admin management endpoints
- Add encryption for properties file
- Add session management improvements

