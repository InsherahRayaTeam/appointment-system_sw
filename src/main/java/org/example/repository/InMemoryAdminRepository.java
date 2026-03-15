package org.example.repository;

import org.example.domain.AdminUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryAdminRepository implements AdminRepository {

    private final Map<String, AdminUser> admins = new HashMap<>();

    public InMemoryAdminRepository() {
        admins.put("admin", new AdminUser("admin", "admin"));
    }

    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return Optional.ofNullable(admins.get(username));
    }
}