package org.inventory.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.inventory.core.SecurityManager;
import org.inventory.model.User;
import org.inventory.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final SecurityManager securityManager;
    private final ViewService viewService;
    // We need a way to callback to Main or ViewService to switch view
    // ViewService load views, but Main holds the BorderPane.
    // We might need to inject MainController or expose a method in ViewService to
    // get Main Layout?
    // Or better, let ViewService handle scene switching if it had reference to
    // Stage/Root.
    // Only Main has reference to BorderPane.
    // For now, let's assume we can get Main access or design differently.
    // Or we use an EventBus.
    // But since Main.mainLayout is private, we can't access it easily.
    // Let's implement a simple callback or navigation interface.
    // Actually, Main.switchView() is private.
    // Let's make Main expose a static instance or NavigationService.

    // Simplest: LoginController uses a "Navigation" bean.
    // But Main isn't a bean (it's extended Application).

    // Workaround: Inject a "NavigationCallbacks" interface that Main
    // implements/provides?
    // Let's assume for now we just print success.

    @Autowired
    public LoginController(SecurityManager securityManager, ViewService viewService) {
        this.securityManager = securityManager;
        this.viewService = viewService;
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password.");
            return;
        }

        User user = securityManager.authenticate(username, password);
        if (user != null) {
            System.out.println("Login successful: " + user.getRole());
            viewService.show("Dashboard");
        } else {
            showAlert("Error", "Invalid credentials.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
