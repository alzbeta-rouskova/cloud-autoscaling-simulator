package cz.arouskova.autoscaler.lb;

import cz.arouskova.autoscaler.instance.ServiceInstance;

import java.util.List;

/**
 * Selects the ACTIVE service instance with the shortest queue.
 * DRAINING instances are filtered out via {@link AbstractLoadBalancer#activeOnly(List)}
 * so they do not receive new traffic while draining.
 *
 * @see LoadBalancer
 */
public class LeastQueueLoadBalancer extends AbstractLoadBalancer {

    /**
     * Selects the ACTIVE instance with the smallest current queue size.
     * Ties are broken by the first-encountered instance.
     *
     * @param instances list of currently managed service instances (ACTIVE + DRAINING); must not be empty
     * @return the ACTIVE instance with the shortest queue
     * @throws IllegalArgumentException if {@code instances} is null or empty
     * @throws IllegalStateException    if no ACTIVE instance is available
     */
    @Override
    public ServiceInstance select(List<ServiceInstance> instances) {
        validateNotEmpty(instances);

        ServiceInstance best = null;
        int bestSize = Integer.MAX_VALUE;
        for (ServiceInstance i : activeOnly(instances)) {
            int size = i.currentQueueSize();
            if (size < bestSize) {
                bestSize = size;
                best = i;
            }
        }
        if (best == null) {
            throw new IllegalStateException("No active instances available");
        }
        return best;
    }
}
