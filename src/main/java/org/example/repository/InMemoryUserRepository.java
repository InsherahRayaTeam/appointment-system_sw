package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Represents in-memory user repository in the system.
 */
public class InMemoryUserRepository implements UserRepository {

    private static final String ADMIN_PROPERTIES_RESOURCE = "admin.properties";
    private static final String ADMIN_ID_KEY = "admin.id";
    private static final String ADMIN_EMAIL_KEY = "admin.email";
    private static final String ADMIN_PASSWORD_KEY = "admin.password";
    private static final String USER_ID_KEY = "user.id";
    private static final String USER_EMAIL_KEY = "user.email";
    private static final String USER_PASSWORD_KEY = "user.password";
    private static final String USER10_ID_KEY = "user10.id";
    private static final String USER10_EMAIL_KEY = "user10.email";
    private static final String USER10_PASSWORD_KEY = "user10.password";

    private final List<SystemUser> users = new ArrayList<>();

    /**
     * Creates repository with default users.
     */
    public InMemoryUserRepository() {
        seedDefaultUsers();
    }

    /**
     * Finds user by email.
     *
     * @param email email used for lookup
     * @return optional user
     */
    @Override
    public Optional<SystemUser> findByEmail(String email) {
        String normalizedEmail = normalize(email);

        if (normalizedEmail == null) {
            return Optional.empty();
        }

        for (SystemUser user : users) {
            if (normalizedEmail.equals(normalize(user.getEmail()))) {
                return Optional.of(copyOf(user));
            }
        }

        return Optional.empty();
    }

    /**
     * Finds user by id.
     *
     * @param id user id
     * @return optional user
     */
    @Override
    public Optional<SystemUser> findById(String id) {
        String normalizedId = normalize(id);

        if (normalizedId == null) {
            return Optional.empty();
        }

        for (SystemUser user : users) {
            if (normalizedId.equals(normalize(user.getId()))) {
                return Optional.of(copyOf(user));
            }
        }

        return Optional.empty();
    }

    /**
     * Returns all users.
     *
     * @return defensive copy of users
     */
    @Override
    public List<SystemUser> findAll() {
        List<SystemUser> copies = new ArrayList<>();

        for (SystemUser user : users) {
            copies.add(copyOf(user));
        }

        return Collections.unmodifiableList(copies);
    }

    /**
     * Saves user.
     *
     * @param user user to save
     */
    @Override
    public void save(SystemUser user) {
        if (user == null || normalize(user.getEmail()) == null) {
            return;
        }

        if (findByEmail(user.getEmail()).isPresent()) {
            update(user);
            return;
        }

        users.add(copyOf(user));
    }

    /**
     * Updates existing user by email.
     *
     * @param user replacement user
     * @return true if updated
     */
    @Override
    public boolean update(SystemUser user) {
        if (user == null || normalize(user.getEmail()) == null) {
            return false;
        }

        String normalizedEmail = normalize(user.getEmail());

        for (int i = 0; i < users.size(); i++) {
            SystemUser current = users.get(i);

            if (normalizedEmail.equals(normalize(current.getEmail()))) {
                users.set(i, copyOf(user));
                return true;
            }
        }

        return false;
    }

    /**
     * Updates password for an existing user.
     *
     * @param userId user id
     * @param newPassword new password
     * @return true if password changed
     */
    @Override
    public boolean updatePassword(String userId, String newPassword) {
        String normalizedId = normalize(userId);

        if (normalizedId == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        for (int i = 0; i < users.size(); i++) {
            SystemUser current = users.get(i);

            if (normalizedId.equals(normalize(current.getId()))) {
                users.set(i, new SystemUser(
                        current.getId(),
                        current.getEmail(),
                        newPassword,
                        current.getRole()
                ));
                return true;
            }
        }

        return false;
    }

    /**
     * Seeds default admin and regular user accounts.
     */
    private void seedDefaultUsers() {
        Properties properties = loadAdminProperties();

        seedUser(properties, ADMIN_ID_KEY, ADMIN_EMAIL_KEY, ADMIN_PASSWORD_KEY, UserRole.ADMIN);
        seedUser(properties, USER_ID_KEY, USER_EMAIL_KEY, USER_PASSWORD_KEY, UserRole.USER);
        seedUser(properties, USER10_ID_KEY, USER10_EMAIL_KEY, USER10_PASSWORD_KEY, UserRole.USER);
    }

    /**
     * Loads demo user credentials from the classpath.
     *
     * @return loaded properties, or empty properties when unavailable
     */
    private Properties loadAdminProperties() {
        Properties properties = new Properties();

        try (InputStream input = InMemoryUserRepository.class
                .getClassLoader()
                .getResourceAsStream(ADMIN_PROPERTIES_RESOURCE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load demo user credentials", exception);
        }

        return properties;
    }

    /**
     * Seeds a user from property keys.
     *
     * @param properties credential properties
     * @param idKey property key for id
     * @param emailKey property key for email
     * @param passwordKey property key for password
     * @param role user role
     */
    private void seedUser(Properties properties, String idKey, String emailKey, String passwordKey, UserRole role) {
        seedUser(
                properties.getProperty(idKey),
                properties.getProperty(emailKey),
                properties.getProperty(passwordKey),
                role
        );
    }

    /**
     * Seeds a user if all required values are present.
     *
     * @param id user id or blank to derive from email
     * @param email user email
     * @param password user password
     * @param role user role
     */
    private void seedUser(String id, String email, String password, UserRole role) {
        String normalizedEmail = normalize(email);

        if (normalizedEmail == null
                || password == null
                || password.trim().isEmpty()
                || role == null) {
            return;
        }

        String normalizedId = normalize(id);
        String userId = normalizedId == null ? normalizedEmail + "-id" : id.trim();

        save(new SystemUser(userId, normalizedEmail, password, role));
    }

    /**
     * Creates a defensive copy.
     *
     * @param user user to copy
     * @return copied user
     */
    private SystemUser copyOf(SystemUser user) {
        return new SystemUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    /**
     * Normalizes text for lookup.
     *
     * @param value raw value
     * @return normalized value, or null when blank
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}
