package cz.arouskova.autoscaler.lb;

import cz.arouskova.autoscaler.instance.InstanceStatus;
import cz.arouskova.autoscaler.instance.ServiceInstance;

import java.util.List;

/**
 * Abstract base class for load balancers, providing common validation logic
 * and a shared filter for ACTIVE instances.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    /**
     * Validates that the provided list of service instances is not null or empty.
     *
     * @param instances the list of service instances to validate
     * @throws IllegalArgumentException if the list is null or empty
     */
    protected void validateNotEmpty(List<ServiceInstance> instances) {

        if (instances == null || instances.isEmpty()) {
            throw new IllegalArgumentException("Instances list cannot be null or empty");
        }
    }

    /**
     * Returns a new list containing only instances in {@link InstanceStatus#ACTIVE}.
     * DRAINING instances are filtered out so they do not receive new traffic
     * while draining their queues.
     *
     * @param instances list of instances to filter (may contain ACTIVE and DRAINING)
     * @return new list with only ACTIVE instances (possibly empty)
     */
    protected List<ServiceInstance> activeOnly(List<ServiceInstance> instances) {
        return instances.stream()
                .filter(i -> i.getStatus() == InstanceStatus.ACTIVE)
                .toList();
    }
}
