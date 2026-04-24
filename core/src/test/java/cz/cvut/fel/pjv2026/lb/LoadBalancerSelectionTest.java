package cz.cvut.fel.pjv2026.lb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoadBalancerSelectionTest {

    @Test
    void creates_round_robin_for_round_robin_type() {
        LoadBalancer lb = LoadBalancerSelection.create(LoadBalancerType.ROUND_ROBIN);

        assertInstanceOf(RoundRobinLoadBalancer.class, lb);
    }

    @Test
    void creates_least_queue_for_least_queue_type() {
        LoadBalancer lb = LoadBalancerSelection.create(LoadBalancerType.LEAST_QUEUE);

        assertInstanceOf(LeastQueueLoadBalancer.class, lb);
    }

    @Test
    void throws_on_null_type() {
        assertThrows(NullPointerException.class, () -> LoadBalancerSelection.create(null));
    }
}
