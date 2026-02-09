package org.inventory.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.inventory.core.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
public class PurchaseController {

    @FXML
    private TextField supplierField;
    @FXML
    private TextField refNumberField;
    @FXML
    private TextField productSearchField;
    @FXML
    private TableView<PurchaseItem> purchaseTable;
    @FXML
    private TableColumn<PurchaseItem, String> productNameColumn;
    @FXML
    private TableColumn<PurchaseItem, Integer> quantityColumn;
    @FXML
    private TableColumn<PurchaseItem, BigDecimal> costPriceColumn;
    @FXML
    private TableColumn<PurchaseItem, BigDecimal> totalColumn;
    @FXML
    private Label totalCostLabel;

    private final DatabaseManager dbManager;
    private ObservableList<PurchaseItem> purchaseItems = FXCollections.observableArrayList();

    @Autowired
    public PurchaseController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @FXML
    public void initialize() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        costPriceColumn.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        purchaseTable.setItems(purchaseItems);
    }

    @FXML
    public void handleSelectItem() {
        String query = productSearchField.getText();
        if (query == null || query.isEmpty())
            return;

        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM products WHERE lower(name) LIKE '%" + query.toLowerCase() + "%' OR sku LIKE '%"
                    + query + "%' LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                PurchaseItem item = new PurchaseItem();
                item.setProductId(rs.getLong("id"));
                item.setProductName(rs.getString("name"));
                item.setQuantity(1);
                item.setCostPrice(rs.getBigDecimal("cost_price"));
                item.setTotal(item.getCostPrice());
                purchaseItems.add(item);
                updateTotal();
            } else {
                showError("Product not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCommitPurchases() {
        if (purchaseItems.isEmpty())
            return;

        try (Connection conn = dbManager.getLocalConnection()) {
            conn.setAutoCommit(false);
            try {
                String updateStock = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
                try (PreparedStatement psStock = conn.prepareStatement(updateStock)) {
                    for (PurchaseItem item : purchaseItems) {
                        psStock.setInt(1, item.getQuantity());
                        psStock.setLong(2, item.getProductId());
                        psStock.addBatch();
                    }
                    psStock.executeBatch();
                }

                // Log purchase (Simplified: just update stock for now)
                conn.commit();
                purchaseItems.clear();
                supplierField.clear();
                refNumberField.clear();
                updateTotal();
                showInfo("Purchase committed and stock updated!");
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTotal() {
        BigDecimal total = purchaseItems.stream()
                .map(PurchaseItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCostLabel.setText("$" + String.format("%.2f", total));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.show();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(msg);
        alert.show();
    }

    // Inner class for UI binding
    public static class PurchaseItem {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal costPrice;
        private BigDecimal total;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(BigDecimal costPrice) {
            this.costPrice = costPrice;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }
}
