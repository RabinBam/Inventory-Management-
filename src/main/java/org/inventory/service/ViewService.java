package org.inventory.service;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

    public Node getView(String viewName) {
        VBox container = new VBox(25);
        container.setPadding(new Insets(40));

        Label title = new Label(viewName);
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        container.getChildren().add(title);

        switch (viewName) {
            case "Dashboard": return buildDashboard(container);
            case "Inventory": return buildInventoryTable(container);
            case "Sales": return buildSalesAnalytics(container);
            default: return container;
        }
    }

    private Node buildDashboard(VBox container) {
        // Stats Row (Cards)
        HBox statsRow = new HBox(20);
        statsRow.getChildren().addAll(
                createCard("Average Value", "$128,100", "Won Deals"),
                createCard("Monthly Goal", "$6.2M", "Target: $8.1M")
        );

        // Charts Row
        HBox chartsRow = new HBox(20);
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Sales by Rep");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Rep A", 190));
        series.getData().add(new XYChart.Data<>("Rep B", 160));
        barChart.getData().add(series);

        chartsRow.getChildren().add(barChart);
        container.getChildren().addAll(statsRow, chartsRow);
        return container;
    }

    private Node buildInventoryTable(VBox container) {
        TableView<Object> table = new TableView<>();
        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        TableColumn<Object, String> priceCol = new TableColumn<>("Price");
        TableColumn<Object, String> stockCol = new TableColumn<>("Stock");

        table.getColumns().addAll(nameCol, priceCol, stockCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        container.getChildren().add(table);
        return container;
    }

    private Node buildSalesAnalytics(VBox container) {
        LineChart<String, Number> salesTrend = new LineChart<>(new CategoryAxis(), new NumberAxis());
        salesTrend.setTitle("Sales Growth Trend");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Oct 1", 400));
        series.getData().add(new XYChart.Data<>("Oct 15", 800));
        series.getData().add(new XYChart.Data<>("Oct 22", 750));
        salesTrend.getData().add(series);

        container.getChildren().add(salesTrend);
        return container;
    }

    private StackPane createCard(String title, String value, String subtitle) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label t = new Label(title);
        Label v = new Label(value);
        v.setFont(Font.font("System", FontWeight.BOLD, 28));
        Label s = new Label(subtitle);
        s.setTextFill(Color.GREY);

        content.getChildren().addAll(t, v, s);
        return new StackPane(content);
    }
}