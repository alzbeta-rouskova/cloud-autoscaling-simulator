package cz.cvut.fel.pjv2026;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Builds the primary scene from
 * {@link MainController}, wires the close handler to a graceful engine
 * shutdown, and forwards JavaFX lifecycle events to the controller.
 * For jar packaging see {@link Launcher}.
 */
public class MainApp extends Application {

    private MainController controller;

    @Override
    public void start(Stage primaryStage) {
        controller = new MainController(primaryStage);
        Scene scene = new Scene((Parent) controller.getView(), 1280, 800);
        primaryStage.setTitle("Cloud Autoscaling Simulator");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> controller.shutdown());
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
