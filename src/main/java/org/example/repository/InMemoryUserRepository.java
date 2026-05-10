package org.example.repository;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents in-memory user repository in the system.
 */
public class InMemoryUserRepository implements UserRepository {

    private static final String DEFAULT_ADMIN_ID = "admin-1";
    private static final String DEFAULT_ADMIN_EMAIL = "insherah2004@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final String DEFAULT_USER_ID = "user-1";
    private static final String DEFAULT_USER_EMAIL = "insherahdwikat@gmail.com";
    private static final String DEFAULT_USER_PASSWORD = "user123";

    private static final String PROPERTIES_USER_ID = "user-10";
    private static final String PROPERTIES_USER_EMAIL = "mlkschool10@gmail.com";
    private static final String PROPERTIES_USER_PASSWORD = "user10pass";

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
        seedUser(DEFAULT_ADMIN_ID, DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD, UserRole.ADMIN);
        seedUser(DEFAULT_USER_ID, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD, UserRole.USER);
        seedUser(PROPERTIES_USER_ID, PROPERTIES_USER_EMAIL, PROPERTIES_USER_PASSWORD, UserRole.USER);
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
