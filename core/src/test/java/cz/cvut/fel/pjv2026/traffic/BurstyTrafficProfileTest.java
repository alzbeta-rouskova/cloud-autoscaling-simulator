package cz.cvut.fel.pjv2026.traffic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BurstyTrafficProfileTest {

    @Test
    void returns_baseline_outside_burst() {
        BurstyTrafficProfile profile = new BurstyTrafficProfile(5, 3.0, 10);

        assertEquals(5, profile.requestsForTick(3L));
    }

    @Test
    void returns_spike_during_burst() {
        BurstyTrafficProfile profile = new BurstyTrafficProfile(5, 3.0, 10);

        assertEquals(15, profile.requestsForTick(10L));
    }

    @Test
    void burst_is_periodic() {
        BurstyTrafficProfile profile = new BurstyTrafficProfile(5, 3.0, 10);

        assertEquals(15, profile.requestsForTick(20L));
    }
}
