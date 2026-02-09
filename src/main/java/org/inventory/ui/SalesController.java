package org.inventory.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.inventory.core.DatabaseManager;
import org.inventory.core.SecurityManager;
import org.inventory.model.Product;
import org.inventory.model.SaleItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
public class SalesController {

    @FXML
    private TextField productSearchField;
    @FXML
    private TableView<Product> productSearchTable;
    @FXML
    private TableColumn<Product, String> searchNameColumn;
    @FXML
    private TableColumn<Product, BigDecimal> searchPriceColumn;
    @FXML
    private TableColumn<Product, Integer> searchStockColumn;

    @FXML
    private TableView<SaleItem> cartTable;
    @FXML
    private TableColumn<SaleItem, String> cartItemColumn; // Needs mapping
    @FXML
    private TableColumn<SaleItem, Integer> cartQtyColumn;
    @FXML
    private TableColumn<SaleItem, BigDecimal> cartPriceColumn;
    @FXML
    private TableColumn<SaleItem, BigDecimal> cartTotalColumn;

    @FXML
    private Label totalLabel;

    private final DatabaseManager dbManager;
    private final SecurityManager securityManager;

    private ObservableList<Product> searchResults = FXCollections.observableArrayList();
    private ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();

    @Autowired
    public SalesController(DatabaseManager dbManager, SecurityManager securityManager) {
        this.dbManager = dbManager;
        this.securityManager = securityManager;
    }

    @FXML
    public void initialize() {
        // Setup Search Table
        searchNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        searchPriceColumn.setCellValueFactory(new PropertyValueFactory<>("salesPrice"));
        searchStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        productSearchTable.setItems(searchResults);

        // Setup Cart Table
        // Note: SaleItem doesn't have product name directly. We need a way to show it.
        // We can use a custom cell value factory or add productName to SaleItem
        // transiently.
        // For now, let's assume we can add a transient getter in SaleItem or just bind
        // ID (ugly).
        // Better: Create a CartItem wrapper or use Product in Cart.
        // Let's use SaleItem and assume we can lookup name or add transient field.
        cartQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        cartTotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        cartTable.setItems(cartItems);

        // Search Listener
        productSearchField.textProperty().addListener((obs, old, val) -> searchProducts(val));
    }

    private void searchProducts(String query) {
        if (query == null || query.length() < 2)
            return;
        searchResults.clear();
        // Simple search query
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM products WHERE lower(name) LIKE '%" + query.toLowerCase() + "%' OR sku LIKE '%"
                    + query + "%'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setSalesPrice(rs.getBigDecimal("sales_price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                searchResults.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddToCart() {
        Product p = productSearchTable.getSelectionModel().getSelectedItem();
        if (p == null)
            return;

        if (p.getStockQuantity() <= 0) {
            showAlert("Error", "Out of Stock!");
            return;
        }

        // Check if already in cart
        SaleItem existing = cartItems.stream().filter(i -> i.getProductId().equals(p.getId())).findFirst().orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            existing.setSubtotal(existing.getUnitPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
            cartTable.refresh();
        } else {
            SaleItem item = new SaleItem();
            item.setProductId(p.getId());
            item.setQuantity(1);
            item.setUnitPrice(p.getSalesPrice());
            item.setSubtotal(p.getSalesPrice());
            cartItems.add(item);
        }
        updateTotal();
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty())
            return;

        try (Connection conn = dbManager.getLocalConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create Sale
                BigDecimal total = cartItems.stream()
                        .map(SaleItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                String insertSale = "INSERT INTO sales (user_id, total_amount, discount_amount, sale_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
                long saleId = -1;

                try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertSale,
                        Statement.RETURN_GENERATED_KEYS)) {
                    org.inventory.model.User user = securityManager.getCurrentUser();
                    stmt.setLong(1, user != null ? user.getId() : 1);
                    stmt.setBigDecimal(2, total);
                    stmt.setBigDecimal(3, BigDecimal.ZERO);
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next())
                            saleId = rs.getLong(1);
                    }
                }

                // 2. Insert Items & Decrease Level
                String insertItem = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
                String updateStock = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";

                try (java.sql.PreparedStatement curItem = conn.prepareStatement(insertItem);
                        java.sql.PreparedStatement curStock = conn.prepareStatement(updateStock)) {

                    for (SaleItem item : cartItems) {
                        // Insert Item
                        curItem.setLong(1, saleId);
                        curItem.setLong(2, item.getProductId());
                        curItem.setInt(3, item.getQuantity());
                        curItem.setBigDecimal(4, item.getUnitPrice());
                        curItem.setBigDecimal(5, item.getSubtotal());
                        curItem.addBatch();

                        // Update Stock
                        curStock.setInt(1, item.getQuantity());
                        curStock.setLong(2, item.getProductId());
                        curStock.addBatch();
                    }
                    curItem.executeBatch();
                    curStock.executeBatch();
                }

                conn.commit();

                // Generate Invoice
                org.inventory.model.Sale sale = new org.inventory.model.Sale();
                sale.setId(saleId);
                sale.setTotalAmount(total);
                sale.setSaleDate(java.time.LocalDateTime.now());

                org.inventory.utils.PDFGenerator generator = new org.inventory.utils.PDFGenerator();
                String fileName = "Invoice_" + saleId + ".pdf";
                generator.generateInvoice(sale, new java.util.ArrayList<>(cartItems), fileName);

                showAlert("Success", "Sale Completed! Invoice saved to " + fileName);
                cartItems.clear();
                updateTotal();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                showAlert("Error", "Checkout Failed: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database Connection Error");
        }
    }

    private void updateTotal() {
        BigDecimal total = cartItems.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: " + total.toString());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
