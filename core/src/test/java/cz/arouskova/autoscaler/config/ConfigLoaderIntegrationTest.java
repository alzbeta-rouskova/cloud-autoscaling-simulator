package cz.arouskova.autoscaler.config;

import cz.arouskova.autoscaler.autoscaler.AutoScaler;
import cz.arouskova.autoscaler.core.EventBus;
import cz.arouskova.autoscaler.core.SimulationClock;
import cz.arouskova.autoscaler.core.SimulationConfig;
import cz.arouskova.autoscaler.core.SimulationEngine;
import cz.arouskova.autoscaler.core.Snapshot;
import cz.arouskova.autoscaler.instance.InstanceConfig;
import cz.arouskova.autoscaler.instance.InstanceManager;
import cz.arouskova.autoscaler.lb.LoadBalancer;
import cz.arouskova.autoscaler.lb.LoadBalancerSelection;
import cz.arouskova.autoscaler.metrics.MetricsCollector;
import cz.arouskova.autoscaler.model.ConstantServiceTimeModel;
import cz.arouskova.autoscaler.model.RequestIdGenerator;
import cz.arouskova.autoscaler.traffic.ConstantTrafficProfile;
import cz.arouskova.autoscaler.traffic.TrafficGenerator;
import cz.arouskova.autoscaler.traffic.TrafficProfile;
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
