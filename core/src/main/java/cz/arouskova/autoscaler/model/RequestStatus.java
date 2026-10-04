package cz.arouskova.autoscaler.model;

/**
 * Enum representing the status of a request in the system.
 * This enum is used to track the lifecycle of a request as it moves through different stages of processing.
 */
public enum RequestStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    DROPPED
}
