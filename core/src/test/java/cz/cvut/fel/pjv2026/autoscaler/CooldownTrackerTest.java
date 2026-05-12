package cz.cvut.fel.pjv2026.autoscaler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownTrackerTest {

    @Test
    void allows_scale_after_cooldown_period() {
        CooldownTracker tracker = new CooldownTracker(5);
        tracker.recordScale(10);
        assertTrue(tracker.canScale(15));
    }

    @Test
    void blocks_scale_during_cooldown() {
        CooldownTracker tracker = new CooldownTracker(5);
        tracker.recordScale(10);
        assertFalse(tracker.canScale(12));
    }

    @Test
    void reset_clears_cooldown() {
        CooldownTracker tracker = new CooldownTracker(5);
        tracker.recordScale(10);
        tracker.reset();
        assertTrue(tracker.canScale(11));
    }
}
