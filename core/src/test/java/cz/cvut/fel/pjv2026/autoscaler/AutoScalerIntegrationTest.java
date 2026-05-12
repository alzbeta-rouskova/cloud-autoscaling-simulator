package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.EventBus;
import cz.cvut.fel.pjv2026.core.EventType;
import cz.cvut.fel.pjv2026.core.SimulationClock;
import cz.cvut.fel.pjv2026.core.SimulationConfig;
import cz.cvut.fel.pjv2026.core.SimulationEngine;
import cz.cvut.fel.pjv2026.core.SimulationEvent;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.core.TrafficProfileType;
import cz.cvut.fel.pjv2026.instance.InstanceConfig;
import cz.cvut.fel.pjv2026.instance.InstanceManager;
import cz.cvut.fel.pjv2026.lb.LoadBalancer;
import cz.cvut.fel.pjv2026.lb.LoadBalancerSelection;
import cz.cvut.fel.pjv2026.lb.LoadBalancerType;
import cz.cvut.fel.pjv2026.metrics.MetricsCollector;
import cz.cvut.fel.pjv2026.model.ConstantServiceTimeModel;
import cz.cvut.fel.pjv2026.model.RequestIdGenerator;
import cz.cvut.fel.pjv2026.traffic.ConstantTrafficProfile;
import cz.cvut.fel.pjv2026.traffic.TrafficGenerator;
import cz.cvut.fel.pjv2026.traffic.TrafficProfile;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoScalerIntegrationTest {

    @Test
    void scales_up_under_high_load() throws InterruptedException {
        SimulationConfig config = highLoadConfig().toBuilder()
                .initialInstanceCount(1)
                .maxInstanceCount(4)
                .build();
        Harness h = new Harness(config);
        CountDownLatch latch = new CountDownLatch(30);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        h.engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        h.engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(reached);
        assertTrue(last.get().activeInstanceCount() > 1);
    }

    @Test
    void scales_down_under_low_load() throws InterruptedException {
        SimulationConfig config = SimulationConfig.builder()
                .tickDurationMs(20)
                .trafficRate(0)
                .trafficProfile(TrafficProfileType.CONSTANT)
                .initialInstanceCount(4)
                .minInstanceCount(1)
                .maxInstanceCount(4)
                .queueCapacity(50)
                .workerCount(2)
                .serviceTimeMs(10)
                .lbStrategy(LoadBalancerType.ROUND_ROBIN)
                .autoscalerEnabled(true)
                .scaleUpQueueThreshold(10)
                .scaleDownQueueThreshold(2)
                .cooldownTicks(2)
                .autoscalerEvaluationIntervalTicks(2)
                .build();
        Harness h = new Harness(config);
        CountDownLatch latch = new CountDownLatch(30);
        AtomicReference<Snapshot> last = new AtomicReference<>();
        h.engine.setOnSnapshotReady(s -> {
            last.set(s);
            latch.countDown();
        });

        h.engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(reached);
        assertTrue(last.get().activeInstanceCount() < 4);
    }

    @Test
    void does_not_thrash() throws InterruptedException {
        SimulationConfig config = highLoadConfig().toBuilder()
                .cooldownTicks(5)
                .autoscalerEvaluationIntervalTicks(1)
                .maxInstanceCount(8)
                .build();
        Harness h = new Harness(config);
        CopyOnWriteArrayList<SimulationEvent> events = new CopyOnWriteArrayList<>();
        h.eventBus.subscribe(events::add);
        CountDownLatch latch = new CountDownLatch(10);
        h.engine.setOnSnapshotReady(s -> latch.countDown());

        h.engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(reached);
        long scaleEvents = events.stream()
                .filter(e -> e.type() == EventType.SCALE_UP || e.type() == EventType.SCALE_DOWN)
                .count();
        assertTrue(scaleEvents <= 2);
    }

    @Test
    void scale_event_is_published_to_event_bus() throws InterruptedException {
        SimulationConfig config = highLoadConfig();
        Harness h = new Harness(config);
        CopyOnWriteArrayList<SimulationEvent> events = new CopyOnWriteArrayList<>();
        h.eventBus.subscribe(events::add);
        CountDownLatch latch = new CountDownLatch(30);
        h.engine.setOnSnapshotReady(s -> latch.countDown());

        h.engine.start();
        boolean reached = latch.await(5, TimeUnit.SECONDS);
        h.engine.stop();

        assertTrue(reached);
        boolean hasScaleUp = events.stream().anyMatch(e -> e.type() == EventType.SCALE_UP);
        assertTrue(hasScaleUp);
    }

    private static SimulationConfig highLoadConfig() {
        return SimulationConfig.builder()
                .tickDurationMs(20)
                .trafficRate(200)
                .trafficProfile(TrafficProfileType.CONSTANT)
                .initialInstanceCount(1)
                .minInstanceCount(1)
                .maxInstanceCount(4)
                .queueCapacity(50)
                .workerCount(1)
                .serviceTimeMs(30)
                .lbStrategy(LoadBalancerType.ROUND_ROBIN)
                .autoscalerEnabled(true)
                .scaleUpQueueThreshold(5)
                .scaleDownQueueThreshold(1)
                .cooldownTicks(2)
                .autoscalerEvaluationIntervalTicks(2)
                .build();
    }

    private static class Harness {
        final SimulationConfig config;
        final InstanceManager instanceManager;
        final EventBus eventBus;
        final SimulationEngine engine;

        Harness(SimulationConfig config) {
            this.config = config;
            SimulationClock clock = new SimulationClock(config.tickDurationMs());
            MetricsCollector metricsCollector = new MetricsCollector(config);
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

            this.eventBus = new EventBus();
            ScalingPolicy policy = new ThresholdScalingPolicy(
                    config.scaleUpQueueThreshold(),
                    config.scaleDownQueueThreshold(),
                    config.minInstanceCount(),
                    config.maxInstanceCount()
            );
            CooldownTracker cooldown = new CooldownTracker(config.cooldownTicks());
            AutoScaler autoScaler = new AutoScaler(
                    config.autoscalerEvaluationIntervalTicks(),
                    policy, cooldown, instanceManager, eventBus
            );

            this.engine = new SimulationEngine(
                    config, clock, trafficGenerator, lb,
                    instanceManager, metricsCollector, autoScaler, eventBus
            );
        }
    }
}
