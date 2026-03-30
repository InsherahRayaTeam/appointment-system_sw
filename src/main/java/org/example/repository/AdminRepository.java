package org.example.repository;

import org.example.domain.AdminUser;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing authenticated system users.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface AdminRepository {

    /**
     * Finds a user by username.
     *
     * @param username username to search for
     * @return optional user
     */
    Optional<AdminUser> findByUsername(String username);

    /**
     * Stores or replaces a user entry keyed by username.
     *
     * @param user user to persist
     */
    void save(AdminUser user);

    /**
     * Returns all known users.
     *
     * @return user list
     */
    List<AdminUser> findAll();

}