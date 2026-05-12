package cz.cvut.fel.pjv2026.exception;

/**
 * Exception thrown when there is an issue with the simulation instance,
 * such as invalid configuration or runtime errors.
 */
public class InstanceException extends SimulationException {

    /**
     * Creates an instance exception with the given error message.
     *
     * @param message error message describing the issue
     */
    public InstanceException(String message) {

        super(message);
    }
}
