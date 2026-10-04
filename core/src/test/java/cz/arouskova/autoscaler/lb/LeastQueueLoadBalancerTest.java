package cz.arouskova.autoscaler.lb;

import cz.arouskova.autoscaler.instance.InstanceConfig;
import cz.arouskova.autoscaler.instance.InstanceStatus;
import cz.arouskova.autoscaler.instance.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LeastQueueLoadBalancerTest {

    private static ServiceInstance instance(String id, int queueSize, InstanceStatus status) {
        return new ServiceInstance(id, new InstanceConfig(100, 1), null) {
            @Override
            public int currentQueueSize() {
                return queueSize;
            }

            @Override
            public InstanceStatus getStatus() {
                return status;
            }
        };
    }

    @Test
    void selects_instance_with_shortest_queue() {
        LeastQueueLoadBalancer lb = new LeastQueueLoadBalancer();
        List<ServiceInstance> instances = List.of(
                instance("a", 5, InstanceStatus.ACTIVE),
                instance("b", 1, InstanceStatus.ACTIVE),
                instance("c", 3, InstanceStatus.ACTIVE));

        ServiceInstance picked = lb.select(instances);

        assertEquals("b", picked.getId());
    }

    @Test
    void handles_equal_queue_lengths() {
        LeastQueueLoadBalancer lb = new LeastQueueLoadBalancer();
        List<ServiceInstance> instances = List.of(
                instance("a", 2, InstanceStatus.ACTIVE),
                instance("b", 2, InstanceStatus.ACTIVE));

        ServiceInstance picked = lb.select(instances);

        assertNotNull(picked);
    }

    @Test
    void skips_draining_instances() {
        LeastQueueLoadBalancer lb = new LeastQueueLoadBalancer();
        List<ServiceInstance> instances = List.of(
                instance("a", 0, InstanceStatus.DRAINING),
                instance("b", 5, InstanceStatus.ACTIVE));

        ServiceInstance picked = lb.select(instances);

        assertEquals("b", picked.getId());
    }
}
