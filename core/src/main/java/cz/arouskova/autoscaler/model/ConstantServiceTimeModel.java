package cz.arouskova.autoscaler.model;

/**
 * Implementation of the ServiceTimeModel interface that provides a constant service time for all requests.
 * The value is configured once at construction and never changes.
 */
public class ConstantServiceTimeModel implements ServiceTimeModel {

    private final long serviceTimeMs;

    /**
     * Creates a model that always returns the given service time.
     *
     * @param serviceTimeMs fixed service time in milliseconds returned for every request
     */
    public ConstantServiceTimeModel(long serviceTimeMs) {
        this.serviceTimeMs = serviceTimeMs;
    }

    /**
     * Returns the fixed service time configured at construction.
     *
     * @return the configured service time in milliseconds
     */
    @Override
    public long serviceTimeMs() {
        return serviceTimeMs;
    }
}
