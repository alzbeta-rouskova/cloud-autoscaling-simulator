package cz.cvut.fel.pjv2026.metrics;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sliding-window throughput tracker measuring requests per second
 * over the most recent {@value #WINDOW_MS} ms.
 * <p>
 * Not thread-safe: intended to be touched only from the engine thread
 * via {@link MetricsCollector}.
 */
public class ThroughputTracker {

    private record Sample(long tick, int count) {}

    /** Sliding window length in milliseconds. */
    private static final long WINDOW_MS = 1000L;

    private final int windowTicks;
    private final Deque<Sample> samples = new ArrayDeque<>();

    /**
     * Creates a tracker scaled to the given tick duration.
     *
     * @param tickDurationMs duration of one simulated tick; must be positive
     * @throws IllegalArgumentException if {@code tickDurationMs} is not positive
     */
    public ThroughputTracker(int tickDurationMs) {
        if (tickDurationMs <= 0) {
            throw new IllegalArgumentException("tickDurationMs must be positive, got " + tickDurationMs);
        }
        this.windowTicks = (int) Math.max(1L, WINDOW_MS / tickDurationMs);
    }

    /**
     * Records that {@code count} requests completed during {@code currentTick}.
     * Old samples that fall outside the sliding window are pruned.
     *
     * @param count       number of requests completed in this tick
     * @param currentTick index of the tick to which the count belongs
     */
    public void record(int count, long currentTick) {
        samples.addLast(new Sample(currentTick, count));
        long cutoff = currentTick - windowTicks + 1;
        while (!samples.isEmpty() && samples.peekFirst().tick() < cutoff) {
            samples.removeFirst();
        }
    }

    /**
     * Returns the throughput in requests per second over the current window.
     *
     * @return throughput in req/s, or 0 if no data has been recorded
     */
    public double requestsPerSecond() {
        if (samples.isEmpty()) {
            return 0.0;
        }
        long total = 0;
        for (Sample s : samples) {
            total += s.count();
        }
        return total * 1000.0 / WINDOW_MS;
    }
}
