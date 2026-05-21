package cz.cvut.fel.pjv2026;

import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.SimulationState;
import cz.cvut.fel.pjv2026.lb.LoadBalancerType;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControlPanelController {

    private final Stage stage;

    private final Button startButton = new Button("▶ Start");
    private final Button stopButton = new Button("■ Stop");
    private final Button resetButton = new Button("↻ Reset");
    private final Button loadConfigButton = new Button("Load config");

    private final Slider trafficRateSlider = new Slider(1, 200, 50);
    private final Label trafficRateValue = new Label("50");
    private final ComboBox<LoadBalancerType> lbCombo = new ComboBox<>(
            FXCollections.observableArrayList(LoadBalancerType.values()));
    private final Spinner<Integer> maxInstancesSpinner = new Spinner<>();

    private final HBox root;

    private Runnable onStart;
    private Runnable onStop;
    private Runnable onReset;
    private Consumer<Path> onConfigLoaded;
    private Supplier<String> logContentSupplier;

    public ControlPanelController(Stage stage) {
        this.stage = stage;

        trafficRateSlider.setShowTickMarks(true);
        trafficRateSlider.setShowTickLabels(true);
        trafficRateSlider.setMajorTickUnit(50);
        trafficRateSlider.setBlockIncrement(1);
        trafficRateSlider.valueProperty().addListener((obs, oldV, newV) ->
                trafficRateValue.setText(String.valueOf(newV.intValue())));

        lbCombo.getSelectionModel().select(LoadBalancerType.ROUND_ROBIN);

        maxInstancesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 32, 8));
        maxInstancesSpinner.setEditable(true);
        maxInstancesSpinner.setPrefWidth(80);

        startButton.setOnAction(e -> { if (onStart != null) onStart.run(); });
        stopButton.setOnAction(e -> { if (onStop != null) onStop.run(); });
        resetButton.setOnAction(e -> { if (onReset != null) onReset.run(); });
        loadConfigButton.setOnAction(e -> handleLoadConfig());
        Button saveLogButton = new Button("Save log");
        saveLogButton.setOnAction(e -> handleSaveLog());

        startButton.setStyle("-fx-base: #4caf50;");
        stopButton.setStyle("-fx-base: #f44336;");

        HBox lifecycle = new HBox(6, startButton, stopButton, resetButton, loadConfigButton, saveLogButton);
        lifecycle.setAlignment(Pos.CENTER_LEFT);

        VBox trafficBox = labeled("Traffic rate (req/tick)",
                new HBox(8, trafficRateSlider, trafficRateValue));
        trafficRateSlider.setPrefWidth(220);

        VBox lbBox = labeled("LB strategy", lbCombo);
        VBox maxBox = labeled("Max instances", maxInstancesSpinner);

        root = new HBox(20, lifecycle, trafficBox, lbBox, maxBox);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER_LEFT);
    }

    public Node getView() {
        return root;
    }

    public int getTrafficRate() {
        return (int) trafficRateSlider.getValue();
    }

    public LoadBalancerType getLoadBalancerType() {
        return lbCombo.getValue();
    }

    public int getMaxInstances() {
        return maxInstancesSpinner.getValue();
    }

    public void setOnStart(Runnable r) { this.onStart = r; }
    public void setOnStop(Runnable r) { this.onStop = r; }
    public void setOnReset(Runnable r) { this.onReset = r; }
    public void setOnConfigLoaded(Consumer<Path> c) { this.onConfigLoaded = c; }
    public void setLogContentSupplier(Supplier<String> supplier) { this.logContentSupplier = supplier; }

    public void applyConfig(SimulationConfig config) {
        trafficRateSlider.setValue(config.trafficRate());
        trafficRateValue.setText(String.valueOf(config.trafficRate()));
        lbCombo.getSelectionModel().select(config.lbStrategy());
        maxInstancesSpinner.getValueFactory().setValue(config.maxInstanceCount());
    }

    public void updateState(SimulationState state) {
        switch (state) {
            case IDLE -> {
                startButton.setDisable(false);
                stopButton.setDisable(true);
                resetButton.setDisable(true);
                loadConfigButton.setDisable(false);
                setControlsDisabled(false);
            }
            case RUNNING, PAUSED -> {
                startButton.setDisable(true);
                stopButton.setDisable(false);
                resetButton.setDisable(true);
                loadConfigButton.setDisable(true);
                setControlsDisabled(true);
            }
            case STOPPED -> {
                startButton.setDisable(true);
                stopButton.setDisable(true);
                resetButton.setDisable(false);
                loadConfigButton.setDisable(true);
                setControlsDisabled(true);
            }
        }
    }

    private void setControlsDisabled(boolean disabled) {
        trafficRateSlider.setDisable(disabled);
        lbCombo.setDisable(disabled);
        maxInstancesSpinner.setDisable(disabled);
    }

    private void handleLoadConfig() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load simulation config");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File scenarios = new File("scenarios");
        if (scenarios.isDirectory()) {
            chooser.setInitialDirectory(scenarios);
        }
        File file = chooser.showOpenDialog(stage);
        if (file != null && onConfigLoaded != null) {
            onConfigLoaded.accept(file.toPath());
        }
    }

    private void handleSaveLog() {
        if (logContentSupplier == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save event log");
        chooser.setInitialFileName("event-log.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text", "*.txt"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            Files.writeString(file.toPath(), logContentSupplier.get());
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save log: " + ex.getMessage());
            alert.setHeaderText("Save failed");
            alert.showAndWait();
        }
    }

    private static VBox labeled(String text, Node node) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        VBox box = new VBox(2, label, node);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
