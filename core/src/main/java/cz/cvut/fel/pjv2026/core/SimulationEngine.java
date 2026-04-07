package cz.cvut.fel.pjv2026.core;

import cz.cvut.fel.pjv2026.autoscaler.AutoScaler;
import cz.cvut.fel.pjv2026.instance.InstanceManager;
import cz.cvut.fel.pjv2026.lb.LoadBalancer;
import cz.cvut.fel.pjv2026.metrics.MetricsCollector;
import cz.cvut.fel.pjv2026.traffic.TrafficGenerator;

import java.util.function.Consumer;

public class SimulationEngine {

    private SimulationState state;
    private final SimulationClock clock;
    private final SimulationConfig config;
    private final TrafficGenerator trafficGenerator;
    private final LoadBalancer loadBalancer;
    private final InstanceManager instanceManager;
    private final MetricsCollector metricsCollector;
    private final AutoScaler autoScaler;
    private final EventBus eventBus;

    public SimulationEngine(SimulationConfig config, TrafficGenerator trafficGenerator, LoadBalancer loadBalancer, InstanceManager instanceManager, MetricsCollector metricsCollector, AutoScaler autoScaler, EventBus eventBus) {
        this.config = config;
        this.trafficGenerator = trafficGenerator;
        this.loadBalancer = loadBalancer;
        this.instanceManager = instanceManager;
        this.metricsCollector = metricsCollector;
        this.autoScaler = autoScaler;
        this.eventBus = eventBus;
        this.clock = new SimulationClock(config.tickDurationMs);
        this.state = SimulationState.IDLE;
    }

    public void start() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void pause() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void resume() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void stop() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void reset() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setOnSnapshotReady(Consumer<Snapshot> listener) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
