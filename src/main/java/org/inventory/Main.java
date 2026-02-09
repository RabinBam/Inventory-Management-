package org.inventory;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.inventory.service.ViewService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class Main extends Application {
    private ConfigurableApplicationContext springContext;
    private BorderPane mainLayout;
    private ViewService viewService;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(InventoryApplication.class).run();
        viewService = springContext.getBean(ViewService.class);

        // Ensure at least one admin exists
        springContext.getBean(org.inventory.core.SecurityManager.class).createDefaultAdminIfNoUsers();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        mainLayout = new BorderPane();
        viewService.setMainLayout(mainLayout);
        viewService.setSidebar(createSidebar());
        viewService.show("Login");

        // Custom Title Bar
        HBox titleBar = createTitleBar(primaryStage);
        mainLayout.setTop(titleBar);

        Scene scene = new Scene(mainLayout, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Inventory Pro");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTitleBar(Stage stage) {
        HBox titleBar = new HBox(10);
        titleBar.getStyleClass().add("title-bar");
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setAlignment(Pos.CENTER_RIGHT);

        Label title = new Label("Inventory Management - Pro");
        title.getStyleClass().add("title-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnMin = new Button("-");
        btnMin.getStyleClass().add("window-button");
        btnMin.setOnAction(e -> stage.setIconified(true));

        Button btnMax = new Button("□");
        btnMax.getStyleClass().add("window-button");
        btnMax.setOnAction(e -> {
            stage.setMaximized(!stage.isMaximized());
            btnMax.setText(stage.isMaximized() ? "❐" : "□");
        });

        Button btnClose = new Button("×");
        btnClose.getStyleClass().add("window-button-close");
        btnClose.setOnAction(e -> {
            stop();
            System.exit(0);
        });

        titleBar.getChildren().addAll(title, spacer, btnMin, btnMax, btnClose);

        // Window Dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        return titleBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);

        Label logo = new Label("INVENTORY PRO");
        logo.getStyleClass().add("sidebar-logo");
        sidebar.getChildren().add(logo);

        String[] navItems = { "Dashboard", "Inventory", "Sales", "Purchases", "Reports", "Users", "Settings" };
        for (String item : navItems) {
            Button btn = new Button(item);
            btn.getStyleClass().add("nav-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> switchView(item));
            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("nav-button-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            viewService.show("Login");
        });
        sidebar.getChildren().add(logoutBtn);

        return sidebar;
    }

    private void switchView(String viewName) {
        viewService.show(viewName);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}