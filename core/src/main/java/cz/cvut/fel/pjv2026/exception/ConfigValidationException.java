package cz.cvut.fel.pjv2026.exception;

/**
 * Exception thrown when there is an issue with the configuration validation,
 * such as missing required fields or invalid values.
 */
public class ConfigValidationException extends SimulationException {

    /**
     * Creates a configuration validation exception with the given error message.
     *
     * @param message error message describing the issue
     */
    public ConfigValidationException(String message) {

        super(message);
    }
}
