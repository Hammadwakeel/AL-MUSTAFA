package com.hypermall;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import com.hypermall.database.DatabaseManager;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        DatabaseManager.initializeDatabase();        
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main_layout.fxml"));

        primaryStage.setTitle("AL Mustafa Electric Wholesale Store - POS System");
        
        Scene scene = new Scene(root, 1100, 700);
        
        // --- GLOBAL HOTKEYS ---
        // Ctrl+P will now trigger printing if an invoice was recently generated
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_ANY),
            () -> System.out.println("Shortcut Ctrl+P triggered. (Handled in POS Controller)")
        );

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}