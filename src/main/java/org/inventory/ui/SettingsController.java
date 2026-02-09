package org.inventory.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.inventory.core.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SettingsController {

    @FXML
    private TextField dbPathField;
    @FXML
    private TextField serverUrlField;
    @FXML
    private Label syncStatusLabel;

    private final ConfigManager configManager;

    @Autowired
    public SettingsController(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @FXML
    public void initialize() {
        dbPathField.setText(configManager.getDatabasePath());
        // Load server URL from config if available
    }

    @FXML
    public void handleBrowseDB() {
        // Implementation for directory chooser
    }

    @FXML
    public void handleSaveSettings() {
        // Implementation for saving config
    }
}
