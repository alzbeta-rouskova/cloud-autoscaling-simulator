package cz.cvut.fel.pjv2026.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.TrafficProfileType;
import cz.cvut.fel.pjv2026.exception.ConfigValidationException;
import cz.cvut.fel.pjv2026.lb.LoadBalancerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads and validates simulation configuration from a JSON file.
 */
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConfigValidator validator = new ConfigValidator();

    /**
     * Loads a simulation configuration from the specified JSON file path.
     *
     * @param path the path to the JSON configuration file
     * @return the loaded and validated {@link SimulationConfig}
     * @throws ConfigValidationException if the configuration is invalid or cannot be read
     */
    public SimulationConfig load(Path path) {
        SimulationConfigDto dto;
        try {
            dto = mapper.readValue(path.toFile(), SimulationConfigDto.class);
        } catch (IOException e) {
            log.error("failed to load config from {}: {}", path, e.getMessage());
            throw new ConfigValidationException(
                    "failed to read config from " + path + ": " + e.getMessage());
        }

        validator.validate(dto);

        TrafficProfileType trafficProfile = parseTrafficProfile(dto.getTrafficProfile());
        LoadBalancerType lbStrategy = parseLoadBalancerStrategy(dto.getLoadBalancerStrategy());

        SimulationConfig config = SimulationConfig.builder()
                .trafficRate(dto.getTrafficRate())
                .trafficProfile(trafficProfile)
                .burstMultiplier(dto.getBurstMultiplier())
                .burstIntervalTicks(dto.getBurstIntervalTicks())
                .initialInstanceCount(dto.getInitialInstanceCount())
                .minInstanceCount(dto.getMinInstanceCount())
                .maxInstanceCount(dto.getMaxInstanceCount())
                .queueCapacity(dto.getQueueCapacity())
                .workerCount(dto.getWorkerCount())
                .serviceTimeMs(dto.getServiceTimeMs())
                .lbStrategy(lbStrategy)
                .autoscalerEnabled(dto.isAutoscalerEnabled())
                .scaleUpQueueThreshold(dto.getScaleUpQueueThreshold())
                .scaleDownQueueThreshold(dto.getScaleDownQueueThreshold())
                .cooldownTicks(dto.getCooldownTicks())
                .autoscalerEvaluationIntervalTicks(dto.getAutoscalerEvaluationIntervalTicks())
                .tickDurationMs(dto.getTickDurationMs())
                .build();

        log.info("loaded config from {} (trafficRate={}, instances={}..{}, autoscaler={})",
                path, dto.getTrafficRate(),
                dto.getMinInstanceCount(), dto.getMaxInstanceCount(),
                dto.isAutoscalerEnabled());

        return config;
    }

    /** Parses the traffic profile type from a string,
     * throwing a ConfigValidationException if the value is unknown.
     */
    private static TrafficProfileType parseTrafficProfile(String raw) {
        try {
            return TrafficProfileType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ConfigValidationException("unknown trafficProfile: '" + raw + "'");
        }
    }

    /** Parses the load balancer strategy from a string,
     * throwing a ConfigValidationException if the value is unknown.
     */
    private static LoadBalancerType parseLoadBalancerStrategy(String raw) {
        try {
            return LoadBalancerType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ConfigValidationException("unknown loadBalancerStrategy: '" + raw + "'");
        }
    }
}
