package org.inventory.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.springframework.stereotype.Component;

@Component
public class DashboardController {

    @FXML private BarChart<String, Number> salesByRepChart;
    @FXML private PieChart salesPipelineChart;

    @FXML
    public void initialize() {
        // Sample data to match your design
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Rep A", 180));
        series.getData().add(new XYChart.Data<>("Rep B", 150));
        salesByRepChart.getData().add(series);

        salesPipelineChart.getData().add(new PieChart.Data("Won", 60));
        salesPipelineChart.getData().add(new PieChart.Data("New", 40));
    }
}