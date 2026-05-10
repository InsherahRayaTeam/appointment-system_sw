package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Represents in memory user repository in the system.
 */
public class InMemoryUserRepository implements UserRepository {

    private static final String RESOURCE = "/admin.properties";
    private static final String ADMIN_ID_KEY = "admin.id";
    private static final String ADMIN_EMAIL_KEY = "admin.email";
    private static final String ADMIN_PASSWORD_KEY = "admin.password";
    private static final String USER_ID_KEY = "user.id";
    private static final String USER_EMAIL_KEY = "user.email";
    private static final String USER_PASSWORD_KEY = "user.password";
    private static final String DEFAULT_ADMIN_ID = "admin-1";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
   
    private static final String DEFAULT_ADMIN_PASSWORD_ENV = "APP_ADMIN_PASSWORD";

    private static final String DEFAULT_USER_ID = "user-1";
    private static final String DEFAULT_USER_EMAIL = "user@gmail.com";
    private static final String DEFAULT_USER_PASSWORD_ENV = "APP_USER_PASSWORD";
    /**
     * Demo-only fallback passwords used when neither environment variable nor properties provide a password.
     * These exist to make local development and automated tests convenient. DO NOT use these in production.
     * Prefer overriding via environment variables `APP_ADMIN_PASSWORD` and `APP_USER_PASSWORD`.
     */
    private static final String DEMO_FALLBACK_ADMIN_PASSWORD = "admin123";
    private static final String DEMO_FALLBACK_USER_PASSWORD = "user123";
    private final Map<String, SystemUser> usersByEmail = new HashMap<>();

    /**
     * Creates a new in memory user repository object with the given values.
     */
    public InMemoryUserRepository() {
        loadFromResource();
    }

private void loadFromResource() {
    Properties p = new Properties();

    try (InputStream in = InMemoryUserRepository.class.getResourceAsStream(RESOURCE)) {
        if (in != null) {
            p.load(in);
            seedUser(
                    p.getProperty(ADMIN_ID_KEY, DEFAULT_ADMIN_ID),
                    p.getProperty(ADMIN_EMAIL_KEY, DEFAULT_ADMIN_EMAIL),
                    resolvePassword(p.getProperty(ADMIN_PASSWORD_KEY), DEFAULT_ADMIN_PASSWORD_ENV),
                    UserRole.ADMIN
            );
            seedConfiguredRegularUsers(p);
        }
    } catch (IOException e) {
        // Fall back to environment variables if loading fails
    }

    if (!usersByEmail.containsKey(DEFAULT_ADMIN_EMAIL)) {
        seedUser(
                DEFAULT_ADMIN_ID,
                DEFAULT_ADMIN_EMAIL,
                resolvePassword(null, DEFAULT_ADMIN_PASSWORD_ENV),
                UserRole.ADMIN
        );
    }

    if (!usersByEmail.containsKey(DEFAULT_USER_EMAIL)) {
        seedUser(
                DEFAULT_USER_ID,
                DEFAULT_USER_EMAIL,
                resolvePassword(null, DEFAULT_USER_PASSWORD_ENV),
                UserRole.USER
        );
    }
}

private String resolvePassword(String configuredPassword, String envKey) {
    String envPassword = System.getenv(envKey);

    if (envPassword != null && !envPassword.trim().isEmpty()) {
        return envPassword.trim();
    }

    if (configuredPassword != null && !configuredPassword.trim().isEmpty()) {
        return configuredPassword.trim();
    }

    // Provide fallback defaults if neither env nor config is set
    if (DEFAULT_ADMIN_PASSWORD_ENV.equals(envKey)) {
        return DEMO_FALLBACK_ADMIN_PASSWORD;
    }
    if (DEFAULT_USER_PASSWORD_ENV.equals(envKey)) {
        return DEMO_FALLBACK_USER_PASSWORD;
    }

    return null;
}

  private void seedConfiguredRegularUsers(Properties p) {
    seedUser(
            p.getProperty(USER_ID_KEY, DEFAULT_USER_ID),
            p.getProperty(USER_EMAIL_KEY),
            resolvePassword(p.getProperty(USER_PASSWORD_KEY), DEFAULT_USER_PASSWORD_ENV),
            UserRole.USER
    );

    Set<String> propertyNames = p.stringPropertyNames();
    for (String propertyName : propertyNames) {
        if (!propertyName.matches("user\\d+\\.email")) {
            continue;
        }

        String userPrefix = propertyName.substring(0, propertyName.length() - ".email".length());
        String envKey = "APP_" + userPrefix.toUpperCase() + "_PASSWORD";

        seedUser(
                p.getProperty(userPrefix + ".id"),
                p.getProperty(userPrefix + ".email"),
                resolvePassword(p.getProperty(userPrefix + ".password"), envKey),
                UserRole.USER
        );
    }
}

    private void seedUser(String id, String email, String password, UserRole role) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        usersByEmail.put(
                normalizedEmail,
                new SystemUser(
                        (id == null || id.trim().isEmpty()) ? normalizedEmail + "-id" : id.trim(),
                        normalizedEmail,
                        password.trim(),
                        role
                )
        );
    }

    /**
     * Finds by email using the given input.
     *
     * @param email email address used for login or matching
     *
     * @return optional value if data is found
     */
    @Override
    public Optional<SystemUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(email.trim().toLowerCase()));
    }

    /**
     * Finds by id using the given input.
     *
     * @param id unique id used to find the record
     *
     * @return optional value if data is found
     */
    @Override
    public Optional<SystemUser> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedId = id.trim();
        for (SystemUser user : usersByEmail.values()) {
            if (normalizedId.equalsIgnoreCase(user.getId())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Saves a user record.
     *
     * @param user user involved in this action
     */
    @Override
    public void save(SystemUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return;
        }
        usersByEmail.put(user.getEmail().trim().toLowerCase(), user);
    }

    /**
     * Updates an existing user record.
     *
     * @param user user entity with updated values
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean update(SystemUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (!usersByEmail.containsKey(normalizedEmail)) {
            return false;
        }

        usersByEmail.put(normalizedEmail, user);
        return true;
    }

    /**
     * Updates user password when record exists.
     *
     * @param userId unique id used to find the record
     * @param newPassword password text entered by the user
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean updatePassword(String userId, String newPassword) {
        if (userId == null || userId.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        Optional<SystemUser> existing = findById(userId);
        if (existing.isEmpty()) {
            return false;
        }

        SystemUser current = existing.get();
        SystemUser updated = new SystemUser(
                current.getId(),
                current.getEmail(),
                newPassword.trim(),
                current.getRole()
        );

        return update(updated);
    }

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    @Override
    public List<SystemUser> findAll() {
        return List.copyOf(usersByEmail.values());
    }
}
