package org.example;

import org.example.service.AdminAuthService;
import org.example.presentation.ConsoleLogin;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Require administrator login before continuing.
        AdminAuthService authService = new AdminAuthService();
        ConsoleLogin login = new ConsoleLogin(authService);
        boolean ok = login.prompt();
        if (!ok) {
            // exit with non-zero to indicate failure
            System.exit(1);
        }

        // Existing demo behavior continues after successful login
        System.out.println("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}
