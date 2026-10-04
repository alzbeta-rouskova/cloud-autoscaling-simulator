package cz.arouskova.autoscaler.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationClockTest {

    @Test
    void starts_at_tick_zero() {
        SimulationClock clock = new SimulationClock(100);

        assertEquals(0L, clock.tick());
    }

    @Test
    void advance_increments_tick() {
        SimulationClock clock = new SimulationClock(100);

        clock.advance();

        assertEquals(1L, clock.tick());
    }

    @Test
    void simulated_time_is_tick_times_duration() {
        SimulationClock clock = new SimulationClock(100);

        for (int i = 0; i < 5; i++) {
            clock.advance();
        }

        assertEquals(500L, clock.simulatedTimeMs());
    }
}
