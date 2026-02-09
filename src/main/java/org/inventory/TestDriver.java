package org.inventory;

import org.inventory.core.ConfigManager;
import org.inventory.core.DatabaseManager;
import org.inventory.core.SecurityManager;
import org.inventory.model.User;

public class TestDriver {
    public static void main(String[] args) {
        System.out.println("Starting Test Driver...");

        ConfigManager config = new ConfigManager();
        System.out.println("Config loaded. DB Path: " + config.getDatabasePath());

        DatabaseManager db = new DatabaseManager(config);
        System.out.println("DatabaseManager initialized.");

        SecurityManager sec = new SecurityManager(db);
        sec.createDefaultAdminIfNoUsers();

        User user = sec.authenticate("admin", "admin");
        if (user != null) {
            System.out.println("Authentication Successful!");
            System.out.println("User: " + user.getUsername() + ", Role: " + user.getRole());
        } else {
            System.out.println("Authentication Failed!");
        }
    }
}
