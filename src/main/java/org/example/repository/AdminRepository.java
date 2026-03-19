package org.example.repository;

import org.example.domain.AdminUser;
import java.util.Optional;

/**
 * Repository interface for accessing administrator user data.
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