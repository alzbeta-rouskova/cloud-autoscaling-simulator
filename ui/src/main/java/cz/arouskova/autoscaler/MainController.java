package cz.arouskova.autoscaler;

import cz.arouskova.autoscaler.autoscaler.AutoScaler;
import cz.arouskova.autoscaler.autoscaler.CooldownTracker;
import cz.arouskova.autoscaler.autoscaler.ScalingPolicy;
import cz.arouskova.autoscaler.autoscaler.ThresholdScalingPolicy;
import cz.arouskova.autoscaler.config.ConfigLoader;
import cz.arouskova.autoscaler.core.EventBus;
import cz.arouskova.autoscaler.core.SimulationClock;
import cz.arouskova.autoscaler.core.SimulationConfig;
import cz.arouskova.autoscaler.core.SimulationEngine;
import cz.arouskova.autoscaler.core.SimulationEvent;
import cz.arouskova.autoscaler.core.SimulationState;
import cz.arouskova.autoscaler.core.Snapshot;
import cz.arouskova.autoscaler.core.TrafficProfileType;
import cz.arouskova.autoscaler.exception.ConfigValidationException;
import cz.arouskova.autoscaler.instance.InstanceConfig;
import cz.arouskova.autoscaler.instance.InstanceManager;
import cz.arouskova.autoscaler.lb.LoadBalancer;
import cz.arouskova.autoscaler.lb.LoadBalancerSelection;
import cz.arouskova.autoscaler.metrics.MetricsCollector;
import cz.arouskova.autoscaler.model.ConstantServiceTimeModel;
import cz.arouskova.autoscaler.model.RequestIdGenerator;
import cz.arouskova.autoscaler.traffic.BurstyTrafficProfile;
import cz.arouskova.autoscaler.traffic.ConstantTrafficProfile;
import cz.arouskova.autoscaler.traffic.TrafficGenerator;
import cz.arouskova.autoscaler.traffic.TrafficProfile;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Root controller of the JavaFX dashboard. Owns the three sub-controllers
 * (control panel, charts, instances table), wires their callbacks to the
 * lifecycle of a {@link SimulationEngine}, and forwards engine snapshots
 * and events back to the UI on the JavaFX thread.
 * <p>
 * A fresh engine is constructed on every {@code start()}: the controller
 * keeps the most recent {@link SimulationConfig}, applies live UI overrides
 * (traffic rate, load-balancer strategy, max instances) on top of it, and
 * passes the result to {@link #buildEngine(SimulationConfig)}.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ControlPanelController controlPanel;
    private final ChartsController charts;
    private final InstancesTableController instancesTable;

    private final Label tickLabel = new Label("Tick: 0");
    private final Label generatedLabel = new Label("Generated: 0");
    private final Label throughputLabel = new Label("Throughput: 0.0 req/s");
    private final Label latencyLabel = new Label("Latency: 0.0 ms");
    private final Label dropLabel = new Label("Dropped: 0");
    private final Label activeLabel = new Label("Active: 0");
    private final TextArea eventLog = new TextArea();

    private final ConfigLoader configLoader = new ConfigLoader();
    private final UiMapper uiMapper = new UiMapper();

    private final BorderPane root;

    private SimulationConfig currentConfig = SimulationConfig.builder().build();
    private SimulationEngine engine;
    private EventBus engineEventBus;
    private Consumer<SimulationEvent> eventSubscriber;

    public MainController(Stage stage) {
        this.controlPanel = new ControlPanelController(stage);
        this.charts = new ChartsController();
        this.instancesTable = new InstancesTableController();

        controlPanel.setOnStart(this::startSimulation);
        controlPanel.setOnStop(this::stopSimulation);
        controlPanel.setOnReset(this::resetSimulation);
        controlPanel.setOnConfigLoaded(this::loadConfigFile);
        controlPanel.setLogContentSupplier(eventLog::getText);
        controlPanel.applyConfig(currentConfig);
        controlPanel.updateState(SimulationState.IDLE);

        eventLog.setEditable(false);
        eventLog.setPrefRowCount(8);

        HBox statusBar = new HBox(20, tickLabel, generatedLabel, throughputLabel, latencyLabel, dropLabel, activeLabel);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");

        Label eventLogTitle = new Label("Event log");
        eventLogTitle.setPadding(new Insets(4, 8, 0, 8));

        VBox center = new VBox(6, statusBar, charts.getView(), eventLogTitle, eventLog);
        VBox.setVgrow(charts.getView(), Priority.ALWAYS);

        root = new BorderPane();
        root.setTop(controlPanel.getView());
        root.setCenter(center);
        root.setRight(instancesTable.getView());
    }

    /**
     * @return the root node hosting the full dashboard
     */
    public Node getView() {
        return root;
    }

    /**
     * Stops the engine if it is running or paused. Safe to call multiple times
     * and from JavaFX lifecycle hooks (window close, {@code Application.stop}).
     */
    public void shutdown() {
        if (engine == null) {
            return;
        }
        SimulationState s = engine.state();
        if (s == SimulationState.RUNNING || s == SimulationState.PAUSED) {
            engine.stop();
        }
    }

    private void loadConfigFile(Path path) {
        try {
            currentConfig = configLoader.load(path);
            controlPanel.applyConfig(currentConfig);
            appendEventLog("[CONFIG] loaded " + path.getFileName());
        } catch (ConfigValidationException e) {
            log.error("config load failed: {}", e.getMessage());
            showError("Failed to load config", e.getMessage());
        }
    }

    private void startSimulation() {
        if (engine != null && engine.state() != SimulationState.IDLE) {
            return;
        }
        SimulationConfig effective = applyUiOverrides(currentConfig);
        engine = buildEngine(effective);

        engine.setOnSnapshotReady(snapshot ->
                Platform.runLater(() -> updateUi(snapshot)));
        eventSubscriber = event ->
                Platform.runLater(() -> appendEventLog(event.toString()));
        engineEventBus.subscribe(eventSubscriber);

        charts.clear();
        instancesTable.clear();
        eventLog.clear();
        resetLabels();

        engine.start();
        controlPanel.updateState(SimulationState.RUNNING);
    }

    private void stopSimulation() {
        if (engine == null) {
            return;
        }
        engine.stop();
        unsubscribeEvents();
        controlPanel.updateState(SimulationState.STOPPED);
    }

    private void resetSimulation() {
        if (engine != null) {
            if (engine.state() == SimulationState.RUNNING || engine.state() == SimulationState.PAUSED) {
                engine.stop();
                unsubscribeEvents();
            }
            engine.reset();
            engine = null;
        }
        charts.clear();
        instancesTable.clear();
        resetLabels();
        controlPanel.updateState(SimulationState.IDLE);
    }

    private void updateUi(Snapshot snapshot) {
        ChartData data = uiMapper.toChartData(snapshot);
        tickLabel.setText("Tick: " + snapshot.tick());
        generatedLabel.setText("Generated: " + snapshot.requestsThisTick());
        throughputLabel.setText(String.format("Throughput: %.1f req/s", data.throughput));
        latencyLabel.setText(String.format("Latency: %.1f ms", data.latency));
        dropLabel.setText("Dropped: " + snapshot.droppedCount());
        activeLabel.setText("Active: " + snapshot.activeInstanceCount());
        charts.update(snapshot);
        instancesTable.update(snapshot);
    }

    private void appendEventLog(String line) {
        eventLog.appendText(line + System.lineSeparator());
    }

    private void resetLabels() {
        tickLabel.setText("Tick: 0");
        generatedLabel.setText("Generated: 0");
        throughputLabel.setText("Throughput: 0.0 req/s");
        latencyLabel.setText("Latency: 0.0 ms");
        dropLabel.setText("Dropped: 0");
        activeLabel.setText("Active: 0");
    }

    private void unsubscribeEvents() {
        if (engineEventBus != null && eventSubscriber != null) {
            engineEventBus.unsubscribe(eventSubscriber);
        }
        eventSubscriber = null;
    }

    private SimulationConfig applyUiOverrides(SimulationConfig base) {
        return base.toBuilder()
                .trafficRate(controlPanel.getTrafficRate())
                .lbStrategy(controlPanel.getLoadBalancerType())
                .maxInstanceCount(controlPanel.getMaxInstances())
                .build();
    }

    private SimulationEngine buildEngine(SimulationConfig config) {
        SimulationClock clock = new SimulationClock(config.tickDurationMs());
        MetricsCollector metrics = new MetricsCollector(config);

        InstanceConfig instConfig = new InstanceConfig(config.queueCapacity(), config.workerCount());
        InstanceManager instanceManager = new InstanceManager(instConfig, metrics.latencyTracker());

        TrafficProfile profile = (config.trafficProfile() == TrafficProfileType.BURSTY)
                ? new BurstyTrafficProfile(config.trafficRate(), config.burstMultiplier(), config.burstIntervalTicks())
                : new ConstantTrafficProfile(config.trafficRate());

        TrafficGenerator trafficGenerator = new TrafficGenerator(
                profile,
                new RequestIdGenerator(),
                new ConstantServiceTimeModel(config.serviceTimeMs()),
                clock
        );

        LoadBalancer lb = LoadBalancerSelection.create(config.lbStrategy());

        EventBus eventBus = new EventBus();
        this.engineEventBus = eventBus;

        ScalingPolicy policy = new ThresholdScalingPolicy(
                config.scaleUpQueueThreshold(),
                config.scaleDownQueueThreshold(),
                config.minInstanceCount(),
                config.maxInstanceCount()
        );
        CooldownTracker cooldown = new CooldownTracker(config.cooldownTicks());
        AutoScaler autoScaler = new AutoScaler(
                config.autoscalerEvaluationIntervalTicks(),
                policy, cooldown, instanceManager, eventBus
        );

        return new SimulationEngine(
                config, clock, trafficGenerator, lb,
                instanceManager, metrics, autoScaler, eventBus
        );
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
