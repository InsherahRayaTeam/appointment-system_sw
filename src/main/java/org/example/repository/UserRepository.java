package org.example.repository;

import org.example.domain.SystemUser;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing system users (admin and regular users).
 *
 * Users are identified by email, which is used as the login username.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface UserRepository {

    /**
     * Finds a user by email.
     *
     * @param email email to search for
     * @return optional user
     */
    Optional<SystemUser> findByEmail(String email);

    /**
     * Stores or replaces a user entry keyed by email.
     *
     * @param user user to persist
     */
    void save(SystemUser user);

    /**
     * Returns all known users.
     *
     * @return user list
     */
    List<SystemUser> findAll();
}