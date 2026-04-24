package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractLoadBalancerTest {

    private static final class TestLoadBalancer extends AbstractLoadBalancer {
        @Override
        public ServiceInstance select(List<ServiceInstance> instances) {
            validateNotEmpty(instances);
            return instances.get(0);
        }
    }

    @Test
    void validate_throws_on_empty_list() {
        TestLoadBalancer lb = new TestLoadBalancer();

        assertThrows(IllegalArgumentException.class, () -> lb.select(Collections.emptyList()));
    }
}
