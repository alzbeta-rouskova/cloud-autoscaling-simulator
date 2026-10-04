package cz.arouskova.autoscaler.core;

import cz.arouskova.autoscaler.autoscaler.AutoScaler;
import cz.arouskova.autoscaler.instance.InstanceConfig;
import cz.arouskova.autoscaler.instance.InstanceManager;
import cz.arouskova.autoscaler.instance.ServiceInstance;
import cz.arouskova.autoscaler.lb.LoadBalancer;
import cz.arouskova.autoscaler.lb.LoadBalancerSelection;
import cz.arouskova.autoscaler.lb.LoadBalancerType;
import cz.arouskova.autoscaler.metrics.MetricsCollector;
import cz.arouskova.autoscaler.model.ConstantServiceTimeModel;
import cz.arouskova.autoscaler.model.RequestIdGenerator;
import cz.arouskova.autoscaler.traffic.ConstantTrafficProfile;
import cz.arouskova.autoscaler.traffic.TrafficGenerator;
import cz.arouskova.autoscaler.traffic.TrafficProfile;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineIntegrationTest {

    @Test
    void engine_produces_snapshot_after_ticks() throws InterruptedException {
        Harness h = new Harness(defaultConfig());
        CountDownLatch latch = new CountDownLatch(10);
        h.engine.setOnSnapshotReady(s -> latch.countDown());

        h.engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(reached);
    }

    @Test
    void throughput_is_positive_under_load() throws InterruptedException {
        Harness h = new Harness(defaultConfig());
        CountDownLatch latch = new CountDownLatch(25);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        h.engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        h.engine.start();
        latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(last.get().throughput() > 0.0);
    }

    @Test
    void instance_count_matches_config() throws InterruptedException {
        SimulationConfig config = defaultConfig().toBuilder()
                .initialInstanceCount(3)
                .build();
        Harness h = new Harness(config);
        CountDownLatch latch = new CountDownLatch(3);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        h.engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        h.engine.start();
        latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertEquals(3, last.get().activeInstanceCount());
    }

    @Test
    void engine_stops_cleanly() throws InterruptedException {
        Harness h = new Harness(defaultConfig());
        CountDownLatch latch = new CountDownLatch(3);
        h.engine.setOnSnapshotReady(s -> latch.countDown());

        h.engine.start();
        latch.await(2, TimeUnit.SECONDS);
        h.engine.stop();

        boolean allTerminated = h.instanceManager.getInstances().stream()
                .allMatch(ServiceInstance::isTerminated);
        assertTrue(allTerminated);
    }

    @Test
    void engine_resets_to_idle() throws InterruptedException {
        Harness h = new Harness(defaultConfig());
        CountDownLatch latch = new CountDownLatch(2);
        h.engine.setOnSnapshotReady(s -> latch.countDown());

        h.engine.start();
        latch.await(2, TimeUnit.SECONDS);
        h.engine.stop();
        h.engine.reset();
        h.engine.start();

        SimulationState afterRestart = h.engine.state();
        h.engine.stop();

        assertEquals(SimulationState.RUNNING, afterRestart);
    }

    @Test
    void dropped_count_increases_when_overloaded() throws InterruptedException {
        SimulationConfig config = defaultConfig().toBuilder()
                .trafficRate(200)
                .queueCapacity(5)
                .workerCount(1)
                .serviceTimeMs(50)
                .initialInstanceCount(1)
                .build();
        Harness h = new Harness(config);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        h.engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        h.engine.start();
        latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(last.get().droppedCount() > 0);
    }

    private static SimulationConfig defaultConfig() {
        return SimulationConfig.builder()
                .tickDurationMs(50)
                .trafficRate(5)
                .trafficProfile(TrafficProfileType.CONSTANT)
                .burstMultiplier(1.0)
                .burstIntervalTicks(0)
                .initialInstanceCount(2)
                .minInstanceCount(1)
                .maxInstanceCount(4)
                .queueCapacity(20)
                .workerCount(2)
                .serviceTimeMs(10)
                .lbStrategy(LoadBalancerType.ROUND_ROBIN)
                .autoscalerEnabled(false)
                .scaleUpQueueThreshold(8)
                .scaleDownQueueThreshold(2)
                .cooldownTicks(10)
                .autoscalerEvaluationIntervalTicks(5)
                .build();
    }

    private static class Harness {
        final SimulationConfig config;
        final SimulationClock clock;
        final InstanceManager instanceManager;
        final MetricsCollector metricsCollector;
        final SimulationEngine engine;

        Harness(SimulationConfig config) {
            this.config = config;
            this.clock = new SimulationClock(config.tickDurationMs());
            this.metricsCollector = new MetricsCollector(config);

            InstanceConfig instConfig = new InstanceConfig(config.queueCapacity(), config.workerCount());
            this.instanceManager = new InstanceManager(instConfig, metricsCollector.latencyTracker());

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

            this.engine = new SimulationEngine(
                    config, clock, trafficGenerator, lb,
                    instanceManager, metricsCollector, autoScaler, eventBus
            );
        }
    }
}
