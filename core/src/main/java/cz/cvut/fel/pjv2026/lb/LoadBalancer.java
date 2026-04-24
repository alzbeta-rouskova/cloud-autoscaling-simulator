package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.ServiceInstance;

import java.util.List;

/**
 * Interface for load balancers, defining the contract for selecting a service instance from a list.
 */
public interface LoadBalancer {

    /**
     * Selects a service instance from the provided list of instances.
     *
     * @param instances list of currently available service instances; must not be empty
     * @return the selected service instance
     * @throws IllegalArgumentException if the instances list is null or empty
     */
    ServiceInstance select(List<ServiceInstance> instances);
}
