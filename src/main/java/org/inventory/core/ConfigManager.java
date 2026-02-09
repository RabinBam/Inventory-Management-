package org.inventory.core;

import java.io.*;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_DB_PATH = "db.path";
    private static final String KEY_SERVER_IP = "server.ip";
    private static final String KEY_SERVER_PORT = "server.port";
    private static final String KEY_SERVER_DB = "server.db";
    private static final String KEY_SERVER_USER = "server.user";

    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Inventory Management System Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDatabasePath() {
        return properties.getProperty(KEY_DB_PATH, "./data/inventory_db");
    }

    public void setDatabasePath(String path) {
        properties.setProperty(KEY_DB_PATH, path);
    }

    // Server config getters/setters
    public String getServerIp() {
        return properties.getProperty(KEY_SERVER_IP, "localhost");
    }

    public void setServerIp(String ip) {
        properties.setProperty(KEY_SERVER_IP, ip);
    }

    public String getServerPort() {
        return properties.getProperty(KEY_SERVER_PORT, "3306");
    }

    public void setServerPort(String port) {
        properties.setProperty(KEY_SERVER_PORT, port);
    }

    public String getServerDbName() {
        return properties.getProperty(KEY_SERVER_DB, "inventory_server");
    }

    public void setServerDbName(String name) {
        properties.setProperty(KEY_SERVER_DB, name);
    }

    public String getServerUser() {
        return properties.getProperty(KEY_SERVER_USER, "root");
    }

    public void setServerUser(String user) {
        properties.setProperty(KEY_SERVER_USER, user);
    }
}
