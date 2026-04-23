package cz.cvut.fel.pjv2026.core;

/**
 * Tracks the current simulation tick and converts ticks to simulated time in milliseconds.
 * A tick is a single discrete step of the simulation; one tick represents
 * {@code tickDurationMs} milliseconds of simulated time.
 *
 * Only the engine thread is expected to call {@link #advance()};
 * reads are safe from the same thread.
 */
public class SimulationClock {

    private long currentTick;
    private final int tickDurationMs;

    /**
     * Creates a clock starting at tick 0.
     *
     * @param tickDurationMs duration of one tick in simulated milliseconds
     */
    public SimulationClock(int tickDurationMs) {
        this.currentTick = 0;
        this.tickDurationMs = tickDurationMs;
    }

    /**
     * Advances the clock by one tick.
     */
    public void advance() {

        currentTick++;
    }

    /**
     * Returns the current tick counter value.
     *
     * @return current tick (0 immediately after construction)
     */
    public long tick() {

        return currentTick;
    }

    /**
     * Returns the elapsed simulated time in milliseconds.
     *
     * @return {@code currentTick * tickDurationMs}
     */
    public long simulatedTimeMs() {
        return currentTick * tickDurationMs;
    }
}
