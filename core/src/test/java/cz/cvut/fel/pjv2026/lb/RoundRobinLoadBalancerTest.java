package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.InstanceConfig;
import cz.cvut.fel.pjv2026.instance.InstanceStatus;
import cz.cvut.fel.pjv2026.instance.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundRobinLoadBalancerTest {

    private static ServiceInstance instance(String id, InstanceStatus status) {
        return new ServiceInstance(id, new InstanceConfig(10, 1), null) {
            @Override
            public InstanceStatus getStatus() {
                return status;
            }
        };
    }

    private static ServiceInstance active(String id) {
        return instance(id, InstanceStatus.ACTIVE);
    }

    @Test
    void distributes_requests_evenly() {
        RoundRobinLoadBalancer lb = new RoundRobinLoadBalancer();
        List<ServiceInstance> instances = List.of(active("a"), active("b"), active("c"));

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            ServiceInstance picked = lb.select(instances);
            counts.merge(picked.getId(), 1, Integer::sum);
        }

        assertEquals(3, counts.get("a"));
    }

    @Test
    void wraps_around_after_last_instance() {
        RoundRobinLoadBalancer lb = new RoundRobinLoadBalancer();
        ServiceInstance first = active("a");
        List<ServiceInstance> instances = List.of(first, active("b"), active("c"));

        lb.select(instances);
        lb.select(instances);
        lb.select(instances);
        ServiceInstance fourth = lb.select(instances);

        assertEquals(first.getId(), fourth.getId());
    }

    @Test
    void throws_on_empty_instance_list() {
        RoundRobinLoadBalancer lb = new RoundRobinLoadBalancer();

        assertThrows(IllegalArgumentException.class, () -> lb.select(Collections.emptyList()));
    }

    @Test
    void skips_draining_instances() {
        RoundRobinLoadBalancer lb = new RoundRobinLoadBalancer();
        List<ServiceInstance> instances = List.of(
                instance("a", InstanceStatus.DRAINING),
                instance("b", InstanceStatus.ACTIVE),
                instance("c", InstanceStatus.DRAINING));

        ServiceInstance first = lb.select(instances);
        ServiceInstance second = lb.select(instances);

        assertEquals("b", first.getId());
        assertEquals("b", second.getId());
    }
}
