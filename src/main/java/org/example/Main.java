package org.example;

import org.example.repository.AdminRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.SessionManager;
import org.example.presentation.ConsoleLogin;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Require administrator login before continuing.
        AdminRepository adminRepository = new InMemoryAdminRepository();
        AdminAuthService authService = new AdminAuthService(adminRepository);
        SessionManager sessionManager = new SessionManager();
        ConsoleLogin login = new ConsoleLogin(authService);
        boolean ok = login.prompt();
        if (!ok) {
            // exit with non-zero to indicate failure
            System.exit(1);
        }

        // Set session as logged in
        sessionManager.login();

        // Existing demo behavior continues after successful login
        System.out.println("Hello and welcome! You are now logged in.");

        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }

        // Logout at the end
        sessionManager.logout();
    }
}
