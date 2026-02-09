package org.inventory.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViewService {
    private final ApplicationContext context;
    private final Map<String, Node> viewCache = new HashMap<>();
    private javafx.scene.layout.BorderPane mainLayout;
    private Node sidebar;

    public void setSidebar(Node sidebar) {
        this.sidebar = sidebar;
    }

    public void setMainLayout(javafx.scene.layout.BorderPane mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void show(String viewName) {
        if (mainLayout != null) {
            mainLayout.setCenter(getView(viewName));
            if ("Login".equalsIgnoreCase(viewName)) {
                mainLayout.setLeft(null);
            } else if (sidebar != null && mainLayout.getLeft() == null) {
                mainLayout.setLeft(sidebar);
            }
        }
    }

    public ViewService(ApplicationContext context) {
        this.context = context;
    }

    public Node getView(String viewName) {
        // Caching reduces boot and navigation time
        return viewCache.computeIfAbsent(viewName, name -> {
            try {
                String path = "/fxml/" + name.toLowerCase() + ".fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                loader.setControllerFactory(context::getBean);
                return loader.load();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}