package org.inventory.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.inventory.core.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ReportsController {

    @Autowired
    public ReportsController() {
    }

    @FXML
    public void handleInventoryExcel() {
        showInfo("Exporting Inventory to Excel...");
        // Logic to use Apache POI to export products table
    }

    @FXML
    public void handleInventoryPDF() {
        showInfo("Exporting Inventory to PDF...");
        // Logic to use PDFBox
    }

    @FXML
    public void handleSalesReport() {
        showInfo("Generating Sales Report...");
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Status");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
