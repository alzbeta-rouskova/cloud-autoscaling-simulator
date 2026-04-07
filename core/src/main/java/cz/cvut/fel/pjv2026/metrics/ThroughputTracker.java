package cz.cvut.fel.pjv2026.metrics;

public class ThroughputTracker {

    private int tickDurationMs;

    public ThroughputTracker(int tickDurationMs) {

        this.tickDurationMs = tickDurationMs;
    }

    public void record(int count, long currentTick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public double requestsPerSecond() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
