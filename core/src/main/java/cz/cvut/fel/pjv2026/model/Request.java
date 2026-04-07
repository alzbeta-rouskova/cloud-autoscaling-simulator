package cz.cvut.fel.pjv2026.model;

public class Request {

    public final long id;
    public final long arrivalTime;
    public final long serviceTimeMs;
    private volatile RequestStatus status;

    public Request(long id, long arrivalTime, long serviceTimeMs) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTimeMs = serviceTimeMs;
        this.status = RequestStatus.PENDING;
    }

    public RequestStatus getStatus() {

        return status;
    }

    public void markProcessing() {

        this.status = RequestStatus.PROCESSING;
    }

    public void markCompleted() {

        this.status = RequestStatus.COMPLETED;
    }

    public void markDropped() {

        this.status = RequestStatus.DROPPED;
    }
}
