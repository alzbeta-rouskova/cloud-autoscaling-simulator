package cz.cvut.fel.pjv2026.config;

import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.exception.ConfigValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @Test
    void loads_valid_config_from_file(@TempDir Path tempDir) throws IOException {
        Path file = writeJson(tempDir, "valid.json", validJson());

        SimulationConfig config = loader.load(file);

        assertNotNull(config);
        assertEquals(50, config.trafficRate());
    }

    @Test
    void throws_on_missing_file(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.json");

        assertThrows(ConfigValidationException.class, () -> loader.load(missing));
    }

    @Test
    void throws_on_invalid_json(@TempDir Path tempDir) throws IOException {
        Path file = writeJson(tempDir, "broken.json", "{ this is not valid json");

        assertThrows(ConfigValidationException.class, () -> loader.load(file));
    }

    @Test
    void uses_defaults_for_optional_fields(@TempDir Path tempDir) throws IOException {
        String minimal = """
                {
                  "trafficRate": 50,
                  "trafficProfile": "CONSTANT",
                  "burstMultiplier": 3.0,
                  "burstIntervalTicks": 20,
                  "initialInstanceCount": 2,
                  "minInstanceCount": 1,
                  "maxInstanceCount": 8,
                  "queueCapacity": 100,
                  "workerCount": 4,
                  "serviceTimeMs": 20,
                  "loadBalancerStrategy": "ROUND_ROBIN",
                  "autoscalerEnabled": true,
                  "scaleUpQueueThreshold": 8,
                  "scaleDownQueueThreshold": 2,
                  "cooldownTicks": 10
                }
                """;
        Path file = writeJson(tempDir, "minimal.json", minimal);

        SimulationConfig config = loader.load(file);

        assertEquals(5, config.autoscalerEvaluationIntervalTicks());
        assertEquals(100, config.tickDurationMs());
    }

    @Test
    void loads_service_time_correctly(@TempDir Path tempDir) throws IOException {
        Path file = writeJson(tempDir, "valid.json", validJson());

        SimulationConfig config = loader.load(file);

        assertEquals(20L, config.serviceTimeMs());
    }

    private static Path writeJson(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    private static String validJson() {
        return """
                {
                  "trafficRate": 50,
                  "trafficProfile": "CONSTANT",
                  "burstMultiplier": 3.0,
                  "burstIntervalTicks": 20,
                  "initialInstanceCount": 2,
                  "minInstanceCount": 1,
                  "maxInstanceCount": 8,
                  "queueCapacity": 100,
                  "workerCount": 4,
                  "serviceTimeMs": 20,
                  "loadBalancerStrategy": "ROUND_ROBIN",
                  "autoscalerEnabled": true,
                  "scaleUpQueueThreshold": 8,
                  "scaleDownQueueThreshold": 2,
                  "cooldownTicks": 10,
                  "autoscalerEvaluationIntervalTicks": 5,
                  "tickDurationMs": 100
                }
                """;
    }
}
