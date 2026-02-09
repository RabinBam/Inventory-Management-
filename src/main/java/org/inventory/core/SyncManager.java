package org.inventory.core;

import org.inventory.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncManager {

    private final DatabaseManager dbManager;

    @Autowired
    public SyncManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // A simple conflict object
    public static class Conflict {
        public Product local;
        public Product remote;
        public String reason;
    }

    public List<Conflict> checkForConflicts() {
        List<Conflict> conflicts = new ArrayList<>();
        try {
            // Check connection to server first
            // Note: In real app we might want to fail gracefully if server is down
            try (Connection serverConn = dbManager.getServerConnection("server_password_placeholder")) { // TODO: Real
                                                                                                         // password
                                                                                                         // handling
                if (serverConn == null)
                    return conflicts;

                // Compare products
                // 1. Get all local products
                List<Product> localProducts = getAllProducts(dbManager.getLocalConnection());
                // 2. Get all server products
                List<Product> serverProducts = getAllProducts(serverConn);

                // 3. Compare
                for (Product server : serverProducts) {
                    Product local = localProducts.stream()
                            .filter(p -> p.getSku().equals(server.getSku()))
                            .findFirst().orElse(null);

                    if (local == null) {
                        // New on server? Auto-add to local? Or conflict?
                        // Let's assume auto-add for now or mark as New.
                        // For this requirements: "update itself from the server"
                    } else {
                        // Compare fields or timestamp if we had it
                        // For now, compare stock or price
                        if (server.getStockQuantity() != local.getStockQuantity()) {
                            Conflict c = new Conflict();
                            c.local = local;
                            c.remote = server;
                            c.reason = "Stock Mismatch: Local=" + local.getStockQuantity() + ", Remote="
                                    + server.getStockQuantity();
                            conflicts.add(c);
                        }
                    }
                }

            } catch (SQLException e) {
                System.err.println("Server sync failed: " + e.getMessage());
                // Offline mode, no conflicts
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conflicts;
    }

    private List<Product> getAllProducts(Connection conn) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setSku(rs.getString("sku"));
                p.setCategory(rs.getString("category"));
                p.setCostPrice(rs.getBigDecimal("cost_price"));
                p.setSalesPrice(rs.getBigDecimal("sales_price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setMinStockLevel(rs.getInt("min_stock_level"));
                products.add(p);
            }
        }
        return products;
    }
}
