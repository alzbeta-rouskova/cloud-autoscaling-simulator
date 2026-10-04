package cz.arouskova.autoscaler.traffic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantTrafficProfileTest {

    @Test
    void returns_constant_rate_every_tick() {
        ConstantTrafficProfile profile = new ConstantTrafficProfile(10);

        for (long tick = 0; tick < 50; tick++) {
            assertEquals(10, profile.requestsForTick(tick));
        }
    }

    @Test
    void returns_zero_for_zero_rate() {
        ConstantTrafficProfile profile = new ConstantTrafficProfile(0);

        assertEquals(0, profile.requestsForTick(42L));
    }
}
