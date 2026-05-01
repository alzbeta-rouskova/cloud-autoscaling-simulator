package cz.cvut.fel.pjv2026.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LatencyTrackerTest {

    @Test
    void average_latency_is_correct() {
        LatencyTracker tracker = new LatencyTracker();

        tracker.record(10);
        tracker.record(20);
        tracker.record(30);

        assertEquals(20.0, tracker.average(), 0.0001);
    }

    @Test
    void returns_zero_with_no_data() {
        LatencyTracker tracker = new LatencyTracker();

        assertEquals(0.0, tracker.average(), 0.0001);
    }

    @Test
    void reset_clears_all_data() {
        LatencyTracker tracker = new LatencyTracker();
        tracker.record(50);
        tracker.record(150);

        tracker.reset();

        assertEquals(0.0, tracker.average(), 0.0001);
    }
}
