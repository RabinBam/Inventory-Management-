package org.inventory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.inventory.service.ViewService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Main extends Application {
    private ConfigurableApplicationContext springContext;
    private BorderPane mainLayout;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(InventoryApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();

        // Sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Initial View
        switchView("Dashboard");

        Scene scene = new Scene(mainLayout, 1400, 900);
        primaryStage.setTitle("Inventory Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 1 0 0;");

        Label companyLogo = new Label("Company");
        companyLogo.setFont(Font.font("System", FontWeight.BOLD, 22));
        companyLogo.setPadding(new Insets(0, 0, 30, 0));

        sidebar.getChildren().add(companyLogo);

        String[] navItems = {"Dashboard", "Orders", "Inventory", "Reports", "Sales", "Employees", "Customers"};
        for (String item : navItems) {
            Button btn = createNavButton(item);
            btn.setOnAction(e -> switchView(item));
            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }

    private void switchView(String viewName) {
        ViewService viewService = springContext.getBean(ViewService.class);
        mainLayout.setCenter(viewService.getView(viewName));
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}