package org.example.repository;

import org.example.domain.SystemUser;

import java.util.List;
import java.util.Optional;

/**
 * Defines the operations for user repository.
 */
public interface UserRepository {

    /**
     * Finds by email using the given input.
     *
     * @param email email address used for login or matching
     *
     * @return optional value if data is found
     */
    Optional<SystemUser> findByEmail(String email);

    /**
     * Runs save for this class.
     *
     * @param user user involved in this action
     */
    void save(SystemUser user);

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    List<SystemUser> findAll();
}
