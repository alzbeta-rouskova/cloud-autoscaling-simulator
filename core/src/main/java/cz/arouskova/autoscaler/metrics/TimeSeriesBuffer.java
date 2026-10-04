package cz.arouskova.autoscaler.metrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Fixed-capacity ring buffer of double values used to back chart history.
 * When the buffer reaches its capacity, adding a new value overwrites
 * the oldest one. Values are returned in insertion order (oldest first).
 * <p>
 * Not thread-safe: intended to be touched only from the engine thread
 * via {@link MetricsCollector}.
 */
public class TimeSeriesBuffer {

    private final int capacity;
    private final Deque<Double> values;

    /**
     * Creates a new buffer with the given fixed capacity.
     *
     * @param capacity maximum number of values retained; must be positive
     * @throws IllegalArgumentException if {@code capacity} is not positive
     */
    public TimeSeriesBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, got " + capacity);
        }
        this.capacity = capacity;
        this.values = new ArrayDeque<>(capacity);
    }

    /**
     * Appends a value to the buffer. If the buffer is at capacity,
     * the oldest value is removed first.
     *
     * @param value the value to append
     */
    public void add(double value) {
        if (values.size() == capacity) {
            values.removeFirst();
        }
        values.addLast(value);
    }

    /**
     * Returns an immutable snapshot of all currently retained values
     * in insertion order (oldest first).
     *
     * @return immutable list of values, oldest first
     */
    public List<Double> values() {
        return List.copyOf(values);
    }
}
