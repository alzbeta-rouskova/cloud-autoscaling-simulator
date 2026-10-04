package cz.arouskova.autoscaler.config;

import cz.arouskova.autoscaler.exception.ConfigValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigValidatorTest {

    private final ConfigValidator validator = new ConfigValidator();

    @Test
    void rejects_zero_traffic_rate() {
        SimulationConfigDto dto = validDto();
        dto.setTrafficRate(0);
        assertThrows(ConfigValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void rejects_zero_or_negative_service_time() {
        SimulationConfigDto dto = validDto();
        dto.setServiceTimeMs(0);
        assertThrows(ConfigValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void rejects_min_greater_than_max_instances() {
        SimulationConfigDto dto = validDto();
        dto.setMinInstanceCount(5);
        dto.setMaxInstanceCount(3);
        assertThrows(ConfigValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void accepts_valid_config() {
        SimulationConfigDto dto = validDto();
        assertDoesNotThrow(() -> validator.validate(dto));
    }

    private static SimulationConfigDto validDto() {
        SimulationConfigDto dto = new SimulationConfigDto();
        dto.setTrafficRate(50);
        dto.setTrafficProfile("CONSTANT");
        dto.setBurstMultiplier(3.0);
        dto.setBurstIntervalTicks(20);
        dto.setInitialInstanceCount(2);
        dto.setMinInstanceCount(1);
        dto.setMaxInstanceCount(8);
        dto.setQueueCapacity(100);
        dto.setWorkerCount(4);
        dto.setServiceTimeMs(20);
        dto.setLoadBalancerStrategy("ROUND_ROBIN");
        dto.setAutoscalerEnabled(true);
        dto.setScaleUpQueueThreshold(8);
        dto.setScaleDownQueueThreshold(2);
        dto.setCooldownTicks(10);
        dto.setAutoscalerEvaluationIntervalTicks(5);
        dto.setTickDurationMs(100);
        return dto;
    }
}
