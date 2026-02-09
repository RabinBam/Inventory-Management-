package org.inventory.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.inventory.core.SecurityManager;
import org.inventory.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    // Charts can be added dynamically or defined in FXML
    @FXML
    private Label salesTodayLabel;
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label lowStockLabel;
    @FXML
    private TableView<org.inventory.model.Sale> recentTransactionsTable;
    @FXML
    private TableColumn<org.inventory.model.Sale, String> dateColumn;
    @FXML
    private TableColumn<org.inventory.model.Sale, Long> idColumn;
    @FXML
    private TableColumn<org.inventory.model.Sale, java.math.BigDecimal> amountColumn;

    private final SecurityManager securityManager;
    private final org.inventory.service.AnalyticsService analyticsService;

    @Autowired
    public DashboardController(SecurityManager securityManager,
            org.inventory.service.AnalyticsService analyticsService) {
        this.securityManager = securityManager;
        this.analyticsService = analyticsService;
    }

    @FXML
    public void initialize() {
        User user = securityManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome Back, " + user.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome Guest");
        }

        // Setup Recent Transactions Table
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Analytics
        java.math.BigDecimal today = analyticsService.getTotalSalesToday();
        salesTodayLabel.setText("$" + String.format("%.2f", today != null ? today : java.math.BigDecimal.ZERO));

        totalItemsLabel.setText(String.valueOf(analyticsService.getTotalItemsCount()));
        lowStockLabel.setText(String.valueOf(analyticsService.getLowStockCount()));

        // Load data
        recentTransactionsTable
                .setItems(javafx.collections.FXCollections.observableArrayList(analyticsService.getRecentSales(10)));
    }
}
