package org.inventory.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.inventory.core.DatabaseManager;
import org.inventory.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
public class UsersController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;

    private final DatabaseManager dbManager;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Autowired
    public UsersController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(userList);
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setRole(User.Role.valueOf(rs.getString("role")));
                userList.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddUser() {
        // Implementation for adding user
    }
}
