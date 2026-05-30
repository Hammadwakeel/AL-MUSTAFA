package com.hypermall.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class MainLayoutController {
    
    @FXML private StackPane contentArea;

    @FXML 
    public void initialize() { 
        // Load the POS view by default when the app starts
        showPosView(); 

        // Register Global Keyboard Shortcuts (like Ctrl+P)
        Platform.runLater(() -> {
            if (contentArea.getScene() != null) {
                // This 'Accelerator' works globally across the whole application window
                contentArea.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_ANY),
                    () -> {
                        System.out.println("Global Print Shortcut (Ctrl+P) Activated.");
                    }
                );
            }
        });
    }

    @FXML private void showPosView() { 
        loadView("/fxml/pos_view.fxml"); 
    }

    @FXML private void showInventoryView() { 
        loadView("/fxml/inventory_view.fxml"); 
    }

    @FXML private void showCustomerView() { 
        loadView("/fxml/customer_view.fxml"); 
    }

    // ==========================================
    // Method for the Reports Button
    // ==========================================
    @FXML private void showReportsView() { 
        loadView("/fxml/reports_view.fxml"); 
    }

    /**
     * Loads a fresh instance of an FXML view into the center content area.
     * Loading fresh every time ensures the database data is always updated on-screen.
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) { 
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace(); 
        }
    }
}