package cz.arouskova.autoscaler.model;

/**
 * Interface representing a model for service time of requests in the system.
 * Implementations of this interface can provide different strategies for determining
 * the service time of requests, such as fixed service time, random service time, etc.
 */
public interface ServiceTimeModel {

    /**
     * Returns the service time in milliseconds for a request.
     *
     * @return the service time in milliseconds
     */
    long serviceTimeMs();
}
