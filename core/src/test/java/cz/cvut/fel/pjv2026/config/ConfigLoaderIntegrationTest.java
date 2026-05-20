package cz.cvut.fel.pjv2026.config;

import cz.cvut.fel.pjv2026.autoscaler.AutoScaler;
import cz.cvut.fel.pjv2026.core.EventBus;
import cz.cvut.fel.pjv2026.core.SimulationClock;
import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.SimulationEngine;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.instance.InstanceConfig;
import cz.cvut.fel.pjv2026.instance.InstanceManager;
import cz.cvut.fel.pjv2026.lb.LoadBalancer;
import cz.cvut.fel.pjv2026.lb.LoadBalancerSelection;
import cz.cvut.fel.pjv2026.metrics.MetricsCollector;
import cz.cvut.fel.pjv2026.model.ConstantServiceTimeModel;
import cz.cvut.fel.pjv2026.model.RequestIdGenerator;
import cz.cvut.fel.pjv2026.traffic.ConstantTrafficProfile;
import cz.cvut.fel.pjv2026.traffic.TrafficGenerator;
import cz.cvut.fel.pjv2026.traffic.TrafficProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderIntegrationTest {

    @Test
    void loaded_config_creates_working_engine(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path file = tempDir.resolve("default.json");
        Files.writeString(file, """
                {
                  "trafficRate": 5,
                  "trafficProfile": "CONSTANT",
                  "burstMultiplier": 1.0,
                  "burstIntervalTicks": 0,
                  "initialInstanceCount": 2,
                  "minInstanceCount": 1,
                  "maxInstanceCount": 4,
                  "queueCapacity": 20,
                  "workerCount": 2,
                  "serviceTimeMs": 10,
                  "loadBalancerStrategy": "ROUND_ROBIN",
                  "autoscalerEnabled": false,
                  "scaleUpQueueThreshold": 8,
                  "scaleDownQueueThreshold": 2,
                  "cooldownTicks": 10,
                  "autoscalerEvaluationIntervalTicks": 5,
                  "tickDurationMs": 50
                }
                """);

        SimulationConfig config = new ConfigLoader().load(file);
        SimulationEngine engine = buildEngine(config);

        CountDownLatch latch = new CountDownLatch(5);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        engine.stop();

        assertTrue(reached);
        assertNotNull(last.get());
    }

    private static SimulationEngine buildEngine(SimulationConfig config) {
        SimulationClock clock = new SimulationClock(config.tickDurationMs());
        MetricsCollector metrics = new MetricsCollector(config);

        InstanceConfig instConfig = new InstanceConfig(config.queueCapacity(), config.workerCount());
        InstanceManager instanceManager = new InstanceManager(instConfig, metrics.latencyTracker());

        TrafficProfile profile = new ConstantTrafficProfile(config.trafficRate());
        TrafficGenerator trafficGenerator = new TrafficGenerator(
                profile,
                new RequestIdGenerator(),
                new ConstantServiceTimeModel(config.serviceTimeMs()),
                clock
        );

        LoadBalancer lb = LoadBalancerSelection.create(config.lbStrategy());

        EventBus eventBus = new EventBus();
        AutoScaler autoScaler = new AutoScaler(
                config.autoscalerEvaluationIntervalTicks(),
                null, null, instanceManager, eventBus
        );

        return new SimulationEngine(
                config, clock, trafficGenerator, lb,
                instanceManager, metrics, autoScaler, eventBus
        );
    }
}
