package cz.arouskova.autoscaler.lb;

import cz.arouskova.autoscaler.instance.ServiceInstance;

import java.util.List;

/**
 * Distributes requests across ACTIVE service instances in a fixed cyclic order.
 * DRAINING instances are skipped via {@link AbstractLoadBalancer#activeOnly(List)}.
 * Each call to {@link #select(List)} advances an internal counter and returns
 * the next ACTIVE instance modulo the filtered list size.
 *
 * @see LoadBalancer
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    private int nextIndex = 0;

    /**
     * Selects the next ACTIVE instance in round-robin order.
     *
     * @param instances list of currently managed service instances (ACTIVE + DRAINING); must not be empty
     * @return the selected ACTIVE service instance
     * @throws IllegalArgumentException if {@code instances} is null or empty
     * @throws IllegalStateException    if no ACTIVE instance is available
     */
    @Override
    public ServiceInstance select(List<ServiceInstance> instances) {
        validateNotEmpty(instances);

        List<ServiceInstance> active = activeOnly(instances);
        if (active.isEmpty()) {
            throw new IllegalStateException("No active instances available");
        }

        ServiceInstance selected = active.get(nextIndex % active.size());
        nextIndex++;
        return selected;
    }
}
