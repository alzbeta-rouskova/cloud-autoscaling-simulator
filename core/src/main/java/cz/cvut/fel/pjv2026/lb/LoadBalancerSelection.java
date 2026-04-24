package cz.cvut.fel.pjv2026.lb;

/**
 * Factory class for creating LoadBalancer instances based on a specified LoadBalancerType.
 * This class provides a static method to create load balancers, ensuring that the correct
 * implementation is returned based on the provided type. It also includes error handling
 * for null input to prevent potential issues during load balancer creation.
 */
public final class LoadBalancerSelection {

    private LoadBalancerSelection() {}

    /**
     * Factory method to create a LoadBalancer instance based on the provided LoadBalancerType.
     *
     * @param type the type of load balancer to create; must not be null
     * @return a LoadBalancer instance corresponding to the specified type
     * @throws NullPointerException if the provided type is null
     */
    public static LoadBalancer create(LoadBalancerType type) {

        if (type == null) {
            throw new NullPointerException("LoadBalancerType cannot be null");
        }

        return switch (type) {
            case ROUND_ROBIN -> new RoundRobinLoadBalancer();
            case LEAST_QUEUE -> new LeastQueueLoadBalancer();
        };
    }
}
