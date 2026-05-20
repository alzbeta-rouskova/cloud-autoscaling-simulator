package cz.cvut.fel.pjv2026.config;

import cz.cvut.fel.pjv2026.exception.ConfigValidationException;

public class ConfigValidator {

    /**
     * Validates the given simulation configuration DTO. Checks that all parameters
     * are within reasonable bounds and that min/max relationships hold. Throws a
     * ConfigValidationException with a descriptive message if any validation check
     * fails.
     *
     * @param dto the simulation configuration to validate
     * @throws ConfigValidationException if any validation check fails
     */
    public void validate(SimulationConfigDto dto) {
        if (dto.getTrafficRate() <= 0) {
            throw new ConfigValidationException(
                    "trafficRate must be positive, got " + dto.getTrafficRate());
        }
        if (dto.getServiceTimeMs() <= 0) {
            throw new ConfigValidationException(
                    "serviceTimeMs must be positive, got " + dto.getServiceTimeMs());
        }
        if (dto.getMinInstanceCount() > dto.getMaxInstanceCount()) {
            throw new ConfigValidationException(
                    "minInstanceCount (" + dto.getMinInstanceCount()
                            + ") must not exceed maxInstanceCount (" + dto.getMaxInstanceCount() + ")");
        }
        if (dto.getInitialInstanceCount() < dto.getMinInstanceCount()
                || dto.getInitialInstanceCount() > dto.getMaxInstanceCount()) {
            throw new ConfigValidationException(
                    "initialInstanceCount (" + dto.getInitialInstanceCount()
                            + ") must be between minInstanceCount (" + dto.getMinInstanceCount()
                            + ") and maxInstanceCount (" + dto.getMaxInstanceCount() + ")");
        }
        if (dto.getQueueCapacity() <= 0) {
            throw new ConfigValidationException(
                    "queueCapacity must be positive, got " + dto.getQueueCapacity());
        }
        if (dto.getWorkerCount() <= 0) {
            throw new ConfigValidationException(
                    "workerCount must be positive, got " + dto.getWorkerCount());
        }
        if (dto.getTickDurationMs() <= 0) {
            throw new ConfigValidationException(
                    "tickDurationMs must be positive, got " + dto.getTickDurationMs());
        }
        if (dto.getScaleDownQueueThreshold() >= dto.getScaleUpQueueThreshold()) {
            throw new ConfigValidationException(
                    "scaleDownQueueThreshold (" + dto.getScaleDownQueueThreshold()
                            + ") must be less than scaleUpQueueThreshold ("
                            + dto.getScaleUpQueueThreshold() + ")");
        }
    }
}
