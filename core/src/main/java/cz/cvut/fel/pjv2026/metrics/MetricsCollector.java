package cz.cvut.fel.pjv2026.metrics;

import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;

import java.util.List;

public class MetricsCollector {

    private final LatencyTracker latencyTracker;
    private final ThroughputTracker throughputTracker;
    private final TimeSeriesBuffer latencyBuffer;
    private final TimeSeriesBuffer throughputBuffer;
    private final TimeSeriesBuffer instanceCountBuffer;
    private final SimulationConfig config;

    public MetricsCollector(SimulationConfig config) {
        this.config = config;
        this.latencyTracker = new LatencyTracker();
        this.throughputTracker = new ThroughputTracker(config.tickDurationMs);
        this.latencyBuffer = new TimeSeriesBuffer();
        this.throughputBuffer = new TimeSeriesBuffer();
        this.instanceCountBuffer = new TimeSeriesBuffer();
    }

    public Snapshot buildSnapshot(long tick, List<InstanceSnapshot> instanceSnapshots) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
