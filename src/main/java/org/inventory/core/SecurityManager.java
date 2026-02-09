package org.inventory.core;

import org.mindrot.jbcrypt.BCrypt;
import org.inventory.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SecurityManager {
    private DatabaseManager dbManager;
    private User currentUser;

    @Autowired
    public SecurityManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }

    public User convertToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(User.Role.valueOf(rs.getString("role"))); // Ensure Role enum matches DB string
        return user;
    }

    public User authenticate(String username, String password) {
        // Try local DB first (Local First approach)
        try (Connection conn = dbManager.getLocalConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (checkPassword(password, storedHash)) {
                        currentUser = convertToUser(rs);
                        return currentUser;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: Handle DB error, maybe try server if local fails?
            // But if local DB is down, app is likely broken.
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Simple admin creation for first run
    public void createDefaultAdminIfNoUsers() {
        try (Connection conn = dbManager.getLocalConnection();
                Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                // Create default admin
                String hash = hashPassword("admin");
                PreparedStatement ps = conn
                        .prepareStatement("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)");
                ps.setString(1, "admin");
                ps.setString(2, hash);
                ps.setString(3, "ADMIN");
                ps.executeUpdate();
                System.out.println("Default admin user created: admin / admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
