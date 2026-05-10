package org.example.config;

/**
 * Demo configuration values used by the console/demo application.
 *
 * These values are intentionally provided as defaults for local/demo runs.
 * They can be overridden via system properties:
 *  - app.demo.admin.email
 *  - app.demo.admin.password
 */
public final class DemoConfig {

    private DemoConfig() {}

    public static String getAdminEmail() {
        return System.getProperty("app.demo.admin.email", "admin@gmail.com");
    }

    public static String getAdminPassword() {
        return System.getProperty("app.demo.admin.password", "admin123");
    }
}

