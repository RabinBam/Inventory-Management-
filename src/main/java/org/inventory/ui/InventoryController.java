package org.inventory.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.inventory.core.DatabaseManager;
import org.inventory.core.SecurityManager;
import org.inventory.model.Product;
import org.inventory.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
public class InventoryController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> skuColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn; // Sales Price
    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private HBox adminToolbar;

    private final SecurityManager securityManager;
    private final DatabaseManager dbManager;
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<Product> filteredData;

    @Autowired
    public InventoryController(SecurityManager securityManager, DatabaseManager dbManager) {
        this.securityManager = securityManager;
        this.dbManager = dbManager;
    }

    @FXML
    public void initialize() {
        // Setup columns
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("salesPrice"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Load data
        loadProducts();

        // Wrap for filtering
        filteredData = new javafx.collections.transformation.FilteredList<>(productList, p -> true);

        // Populate categories
        ObservableList<String> categories = FXCollections.observableArrayList("All Categories");
        productList.stream().map(Product::getCategory).distinct().forEach(categories::add);
        categoryFilter.setItems(categories);

        // Combined filtering logic
        searchField.textProperty().addListener((o, old, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((o, old, newValue) -> applyFilters());

        javafx.collections.transformation.SortedList<Product> sortedData = new javafx.collections.transformation.SortedList<>(
                filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);

        // Role Check
        User user = securityManager.getCurrentUser();
        if (user != null && user.getRole() == User.Role.EMPLOYEE) {
            adminToolbar.setVisible(false);
            adminToolbar.setManaged(false);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        filteredData.setPredicate(product -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    (product.getName() != null && product.getName().toLowerCase().contains(searchText)) ||
                    (product.getSku() != null && product.getSku().toLowerCase().contains(searchText));

            boolean matchesCategory = selectedCategory == null ||
                    selectedCategory.equals("All Categories") ||
                    (product.getCategory() != null && product.getCategory().equals(selectedCategory));

            return matchesSearch && matchesCategory;
        });
    }

    @FXML
    public void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
    }

    private void loadProducts() {
        productList.clear();
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement();
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
                productList.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAdd() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");
        TextField sku = new TextField();
        sku.setPromptText("SKU");
        TextField category = new TextField();
        category.setPromptText("Category");
        TextField cost = new TextField();
        cost.setPromptText("Cost Price");
        TextField sale = new TextField();
        sale.setPromptText("Sales Price");
        TextField stock = new TextField();
        stock.setPromptText("Stock Quantity");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("SKU:"), 0, 1);
        grid.add(sku, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(category, 1, 2);
        grid.add(new Label("Cost Price:"), 0, 3);
        grid.add(cost, 1, 3);
        grid.add(new Label("Sales Price:"), 0, 4);
        grid.add(sale, 1, 4);
        grid.add(new Label("Stock:"), 0, 5);
        grid.add(stock, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Product p = new Product();
                p.setName(name.getText());
                p.setSku(sku.getText());
                p.setCategory(category.getText());
                p.setCostPrice(new BigDecimal(cost.getText().isEmpty() ? "0" : cost.getText()));
                p.setSalesPrice(new BigDecimal(sale.getText().isEmpty() ? "0" : sale.getText()));
                p.setStockQuantity(Integer.parseInt(stock.getText().isEmpty() ? "0" : stock.getText()));
                p.setMinStockLevel(5);
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            saveProduct(product);
            loadProducts();
        });
    }

    private void saveProduct(Product p) {
        String sql = "INSERT INTO products (name, sku, category, cost_price, sales_price, stock_quantity, min_stock_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getLocalConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getSku());
            pstmt.setString(3, p.getCategory());
            pstmt.setBigDecimal(4, p.getCostPrice());
            pstmt.setBigDecimal(5, p.getSalesPrice());
            pstmt.setInt(6, p.getStockQuantity());
            pstmt.setInt(7, p.getMinStockLevel());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleImportExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.getExtensionFilters()
                .add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        java.io.File file = fileChooser.showOpenDialog(productTable.getScene().getWindow());

        if (file != null) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory
                            .create(fis)) {

                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    Product p = new Product();
                    p.setName(row.getCell(0).getStringCellValue());
                    p.setSku(row.getCell(1).getStringCellValue());
                    p.setCategory(row.getCell(2).getStringCellValue());
                    p.setCostPrice(BigDecimal.valueOf(row.getCell(3).getNumericCellValue()));
                    p.setSalesPrice(BigDecimal.valueOf(row.getCell(4).getNumericCellValue()));
                    p.setStockQuantity((int) row.getCell(5).getNumericCellValue());
                    p.setMinStockLevel(5);
                    saveProduct(p);
                }
                loadProducts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleEdit() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Edit Product: " + selected.getName());
        }
    }

    @FXML
    public void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Delete Product: " + selected.getName());
        }
    }
}
