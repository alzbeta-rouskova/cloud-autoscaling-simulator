package cz.cvut.fel.pjv2026;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Entry point for the Cloud Autoscaling Simulator JavaFX application.
 * Displays an empty window to verify the UI module is correctly set up.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Cloud Autoscaling Simulator");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Cloud Autoscaling Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
