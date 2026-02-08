package org.inventory;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // This bypasses the JavaFX runtime check that often crashes JARs/EXEs
        Application.launch(Main.class, args);
    }
}