package org.inventory.service;

import org.inventory.core.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {
    private final DatabaseManager dbManager;

    @Autowired
    public AnalyticsService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Map<LocalDate, BigDecimal> getDailySales(int days) {
        Map<LocalDate, BigDecimal> data = new HashMap<>();
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement()) {

            // H2 Date logic. Use CURRENT_DATE - days?
            // Query sales from last N days
            String sql = "SELECT CAST(sale_date AS DATE) as sdate, SUM(total_amount) as total " +
                    "FROM sales " +
                    "WHERE sale_date >= DATEADD('DAY', -" + days + ", CURRENT_DATE) " +
                    "GROUP BY sdate";

            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("sdate");
                    if (d != null) {
                        data.put(d.toLocalDate(), rs.getBigDecimal("total"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public BigDecimal getTotalSalesToday() {
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt
                    .executeQuery("SELECT SUM(total_amount) FROM sales WHERE CAST(sale_date AS DATE) = CURRENT_DATE");
            if (rs.next()) {
                BigDecimal val = rs.getBigDecimal(1);
                return val != null ? val : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public long getTotalItemsCount() {
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLowStockCount() {
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT COUNT(*) FROM products WHERE stock_quantity <= min_stock_level")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.util.List<org.inventory.model.Sale> getRecentSales(int limit) {
        java.util.List<org.inventory.model.Sale> sales = new java.util.ArrayList<>();
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM sales ORDER BY sale_date DESC LIMIT " + limit)) {
            while (rs.next()) {
                org.inventory.model.Sale sale = new org.inventory.model.Sale();
                sale.setId(rs.getLong("id"));
                sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                sales.add(sale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sales;
    }
}
