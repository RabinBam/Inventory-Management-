package org.inventory.service;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

    public Node getView(String viewName) {
        // Modular: Each view is built in its own method only when requested
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        switch (viewName) {
            case "Dashboard":
                return buildDashboar(layout);
            case "Inventory":
                return buildInventory(layout);
            case "Sales":
                return buildSales(layout);
            default:
                layout.getChildren().add(new Label("View not found"));
                return layout;
        }
    }

    private Node buildInventory(VBox layout) {
        // Use a simple TableView - very efficient for low RAM
        TableView<Object> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Auto-adjust columns

        // Add columns logic here...

        VBox.setVgrow(table, Priority.ALWAYS);
        layout.getChildren().add(table);
        return layout;
    }

    // Other build methods...
}