package cz.arouskova.autoscaler.model;

/**
 * Represents a request in the system with its associated properties and status.
 */
public class Request {

    private final long id;
    private final long arrivalTime;
    private final long serviceTimeMs;
    private volatile RequestStatus status;

    /**
     * Creates a new request with the given parameters.
     *
     * @param id unique identifier for the request
     * @param arrivalTime time when the request arrives in the system
     * @param serviceTimeMs time required to process the request in milliseconds
     */
    public Request(long id, long arrivalTime, long serviceTimeMs) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTimeMs = serviceTimeMs;
        this.status = RequestStatus.PENDING;
    }

    /**
     * Gets the current status of the request.
     *
     * @return status of the request
     */
    public RequestStatus getStatus() {

        return status;
    }

    /**
     * Marks the request as being processed.
     */
    public void markProcessing() {

        this.status = RequestStatus.PROCESSING;
    }

    /**
     * Marks the request as completed.
     */
    public void markCompleted() {

        this.status = RequestStatus.COMPLETED;
    }

    /**
     * Marks the request as dropped.
     */
    public void markDropped() {

        this.status = RequestStatus.DROPPED;
    }

    public long getId() {
        return id;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getServiceTimeMs() {
        return serviceTimeMs;
    }
}
