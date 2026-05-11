package cz.cvut.fel.pjv2026.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThroughputTrackerTest {

    @Test
    void returns_zero_with_no_data() {
        ThroughputTracker tracker = new ThroughputTracker(100);

        assertEquals(0.0, tracker.requestsPerSecond(), 0.0001);
    }

    @Test
    void calculates_requests_per_second() {
        ThroughputTracker tracker = new ThroughputTracker(100);

        for (long tick = 0; tick < 10; tick++) {
            tracker.record(10, tick);
        }

        assertEquals(100.0, tracker.requestsPerSecond(), 0.0001);
    }

    @Test
    void sliding_window_excludes_old_data() {
        ThroughputTracker tracker = new ThroughputTracker(100);

        tracker.record(1000, 0);
        tracker.record(50, 100);

        assertEquals(50.0, tracker.requestsPerSecond(), 0.0001);
    }
}
