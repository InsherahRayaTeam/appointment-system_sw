package org.example.repository;

import org.example.domain.AdminUser;
import java.util.Optional;

public interface AdminRepository {

    Optional<AdminUser> findByUsername(String username);

}