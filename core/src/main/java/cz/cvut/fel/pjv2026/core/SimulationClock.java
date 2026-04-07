package cz.cvut.fel.pjv2026.core;

public class SimulationClock {

    private long currentTick;
    private int tickDurationMs;

    public SimulationClock(int tickDurationMs) {
        this.currentTick = 0;
        this.tickDurationMs = tickDurationMs;
    }

    public void advance() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public long tick() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public long simulatedTimeMs() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
