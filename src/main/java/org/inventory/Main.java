package org.inventory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Main extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Initializes Spring Boot when the JavaFX app starts
        springContext = new SpringApplicationBuilder(InventoryApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Basic JavaFX setup - you can replace this with an FXML loader later
        Label label = new Label("Inventory System Active");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Inventory Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Shutdown Spring Boot when the window is closed
        springContext.close();
        Platform.exit();
    }
}