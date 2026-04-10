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
     * Finds by id using the given input.
     *
     * @param id unique id used to find the record
     *
     * @return optional value if data is found
     */
    Optional<SystemUser> findById(String id);

    /**
     * Runs save for this class.
     *
     * @param user user involved in this action
     */
    void save(SystemUser user);

    /**
     * Updates an existing user record.
     *
     * @param user user entity with updated values
     *
     * @return true when the action is valid or successful, otherwise false
     */
    boolean update(SystemUser user);

    /**
     * Updates user password when record exists.
     *
     * @param userId unique id used to find the record
     * @param newPassword password text entered by the user
     *
     * @return true when the action is valid or successful, otherwise false
     */
    boolean updatePassword(String userId, String newPassword);

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    List<SystemUser> findAll();
}
