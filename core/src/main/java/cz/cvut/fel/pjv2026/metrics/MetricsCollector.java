package cz.cvut.fel.pjv2026.metrics;

import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;

import java.util.List;

public class MetricsCollector {

    private static final int HISTORY_CAPACITY = 120;

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
        this.latencyBuffer = new TimeSeriesBuffer(HISTORY_CAPACITY);
        this.throughputBuffer = new TimeSeriesBuffer(HISTORY_CAPACITY);
        this.instanceCountBuffer = new TimeSeriesBuffer(HISTORY_CAPACITY);
    }

    public Snapshot buildSnapshot(long tick, List<InstanceSnapshot> instanceSnapshots) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
