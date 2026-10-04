package cz.arouskova.autoscaler.exception;

/**
 * Base class for all domain-specific runtime exceptions raised inside the
 * simulator. Subclasses categorize the error (config validation, instance
 * lifecycle, ...). Unchecked because simulation errors typically indicate
 * misconfiguration or programmer error, not recoverable conditions.
 */
public class SimulationException extends RuntimeException {

    /**
     * Creates a simulation exception with the given error message.
     *
     * @param message error message describing the issue
     */
    public SimulationException(String message) {

        super(message);
    }
}
