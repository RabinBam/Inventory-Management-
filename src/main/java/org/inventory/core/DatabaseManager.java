package org.inventory.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DatabaseManager {
    private ConfigManager configManager;
    private static final String LOCAL_DRIVER = "org.h2.Driver";
    private static final String SERVER_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Hardcoded master password for the local DB file encryption (in real app, user
    // might input this or key management)
    // For this requirement "store data in encrypted way", we use a fixed key or
    // prompts.
    // To make it simple for the user without asking password every time, we might
    // store it or hardcode a salt.
    // Let's use a constant for now, but ideally this should be cleaner.
    private static final String FILE_PASSWORD = "SuperSecretKey123";

    @Autowired
    public DatabaseManager(ConfigManager configManager) {
        this.configManager = configManager;
        try {
            Class.forName(LOCAL_DRIVER);
            Class.forName(SERVER_DRIVER);
            initLocalDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getLocalConnectionUrl() {
        String path = configManager.getDatabasePath();
        // H2 Encryption: AES
        return "jdbc:h2:file:" + path + ";CIPHER=AES;AUTO_SERVER=TRUE";
    }

    public Connection getLocalConnection() throws SQLException {
        // Password format: file password + space + user password
        return DriverManager.getConnection(getLocalConnectionUrl(), "admin", FILE_PASSWORD + " admin");
    }

    public Connection getServerConnection(String password) throws SQLException {
        String url = "jdbc:mysql://" + configManager.getServerIp() + ":" + configManager.getServerPort() + "/"
                + configManager.getServerDbName();
        // Server usually requires password input from user session (not stored in plain
        // text maybe?)
        // Or we pass it here.
        return DriverManager.getConnection(url, configManager.getServerUser(), password);
    }

    private void initLocalDatabase() {
        try (Connection conn = getLocalConnection();
                Statement stmt = conn.createStatement()) {

            // Create Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL)");

            // Create Products Table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "sku VARCHAR(100) UNIQUE, " +
                    "category VARCHAR(100), " +
                    "cost_price DECIMAL(10,2), " +
                    "sales_price DECIMAL(10,2), " +
                    "stock_quantity INT DEFAULT 0, " +
                    "min_stock_level INT DEFAULT 0, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create Sales Table
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT, " +
                    "total_amount DECIMAL(10,2), " +
                    "discount_amount DECIMAL(10,2), " +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Create Sale Items Table
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "sale_id BIGINT, " +
                    "product_id BIGINT, " +
                    "quantity INT, " +
                    "unit_price DECIMAL(10,2), " +
                    "subtotal DECIMAL(10,2), " +
                    "FOREIGN KEY (sale_id) REFERENCES sales(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id))");

            // Create Bookings Table (for locking inventory)
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer_name VARCHAR(255), " +
                    "product_id BIGINT, " +
                    "quantity INT, " +
                    "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "status VARCHAR(50), " + // ACTIVE, FULFILLED, CANCELLED
                    "FOREIGN KEY (product_id) REFERENCES products(id))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
