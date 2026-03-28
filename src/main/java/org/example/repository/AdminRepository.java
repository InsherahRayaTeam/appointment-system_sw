package org.example.repository;

import org.example.domain.AdminUser;
import java.util.Optional;

/**
 * Repository interface for accessing administrator user data.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface AdminRepository {

    /**
     * Finds an administrator by username.
     *
     * @param username the administrator username to search for
     * @return an Optional containing the AdminUser if found, empty otherwise
     */
    Optional<AdminUser> findByUsername(String username);

}