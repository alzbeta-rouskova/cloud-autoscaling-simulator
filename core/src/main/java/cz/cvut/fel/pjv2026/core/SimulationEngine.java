package cz.cvut.fel.pjv2026.core;

import cz.cvut.fel.pjv2026.autoscaler.AutoScaler;
import cz.cvut.fel.pjv2026.instance.InstanceManager;
import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;
import cz.cvut.fel.pjv2026.instance.InstanceStatus;
import cz.cvut.fel.pjv2026.instance.ServiceInstance;
import cz.cvut.fel.pjv2026.lb.LoadBalancer;
import cz.cvut.fel.pjv2026.metrics.MetricsCollector;
import cz.cvut.fel.pjv2026.model.Request;
import cz.cvut.fel.pjv2026.traffic.TrafficGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Orchestrates the simulation tick loop on a dedicated engine thread.
 * <p>
 * Lifecycle: {@code IDLE → RUNNING ⇄ PAUSED → STOPPED → IDLE (via reset)}.
 * Each tick: sweep terminated instances → generate traffic → distribute via
 * load balancer → snapshot every instance → aggregate via {@link MetricsCollector}
 * → publish to the registered listener → advance clock → sleep.
 * <p>
 * The engine thread is the <em>only</em> thread that mutates simulation state;
 * UI and other consumers receive immutable {@link Snapshot} objects through
 * {@link #setOnSnapshotReady(Consumer)}.
 */
public class SimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngine.class);

    /** Upper bound for waiting on instance termination during stop. */
    private static final long STOP_TIMEOUT_MS = 5_000L;

    /** Polling interval while waiting for instances to terminate. */
    private static final long STOP_POLL_INTERVAL_MS = 20L;

    private final SimulationConfig config;
    private final SimulationClock clock;
    private final TrafficGenerator trafficGenerator;
    private final LoadBalancer loadBalancer;
    private final InstanceManager instanceManager;
    private final MetricsCollector metricsCollector;
    private final AutoScaler autoScaler;
    private final EventBus eventBus;
    private final Object pauseLock = new Object();

    private volatile SimulationState state = SimulationState.IDLE;
    private volatile Consumer<Snapshot> snapshotListener;
    private Thread engineThread;

    /**
     * Wires the engine with all collaborators. The {@link SimulationClock} is
     * shared with {@link TrafficGenerator} so request arrival times stay
     * consistent with the engine's tick counter.
     */
    public SimulationEngine(SimulationConfig config,
                            SimulationClock clock,
                            TrafficGenerator trafficGenerator,
                            LoadBalancer loadBalancer,
                            InstanceManager instanceManager,
                            MetricsCollector metricsCollector,
                            AutoScaler autoScaler,
                            EventBus eventBus) {
        this.config = config;
        this.clock = clock;
        this.trafficGenerator = trafficGenerator;
        this.loadBalancer = loadBalancer;
        this.instanceManager = instanceManager;
        this.metricsCollector = metricsCollector;
        this.autoScaler = autoScaler;
        this.eventBus = eventBus;
    }

    /**
     * Returns the current lifecycle state. Thread-safe.
     */
    public SimulationState state() {

        return state;
    }

    /**
     * Registers a listener that will receive every snapshot produced by the
     * engine thread. UI subscribers must wrap their handler in
     * {@code Platform.runLater(...)}.
     */
    public void setOnSnapshotReady(Consumer<Snapshot> listener) {

        this.snapshotListener = listener;
    }

    /**
     * Transitions IDLE → RUNNING. Provisions {@code initialInstanceCount}
     * instances and starts the engine thread. No-op if not in IDLE state.
     */
    public void start() {
        if (state != SimulationState.IDLE) {
            log.warn("start() called in state {}, ignoring", state);
            return;
        }
        for (int i = 0; i < config.initialInstanceCount(); i++) {
            instanceManager.addInstance();
        }
        state = SimulationState.RUNNING;
        engineThread = new Thread(this::runLoop, "engine-thread");
        engineThread.start();
        log.info("simulation started with {} initial instance(s)", config.initialInstanceCount());
        eventBus.publish(new SimulationEvent(clock.tick(), EventType.SIMULATION_STARTED, "simulation started with " + config.initialInstanceCount() + " instance(s)"));
    }

    /**
     * Transitions RUNNING → PAUSED. The engine thread blocks on the pause lock
     * until {@link #resume()} or {@link #stop()} is called. No-op otherwise.
     */
    public void pause() {
        if (state == SimulationState.RUNNING) {
            state = SimulationState.PAUSED;
            log.info("simulation paused");
        }
    }

    /**
     * Transitions PAUSED → RUNNING and wakes the engine thread. No-op otherwise.
     */
    public void resume() {
        if (state == SimulationState.PAUSED) {
            state = SimulationState.RUNNING;
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
            log.info("simulation resumed");
        }
    }

    /**
     * Transitions to STOPPED, joins the engine thread, retires all remaining
     * ACTIVE instances, and blocks until every instance is terminated
     * (up to {@value #STOP_TIMEOUT_MS} ms). Terminated instances stay in the
     * manager's list — {@link #reset()} cleans them up.
     */
    public void stop() {
        if (state == SimulationState.IDLE || state == SimulationState.STOPPED) {
            return;
        }
        state = SimulationState.STOPPED;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
        if (engineThread != null) {
            engineThread.interrupt();
            try {
                engineThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            engineThread = null;
        }
        for (ServiceInstance instance : instanceManager.getInstances()) {
            if (instance.getStatus() == InstanceStatus.ACTIVE) {
                instance.retire();
            }
        }
        awaitInstancesTerminated();
        log.info("simulation stopped");
        eventBus.publish(new SimulationEvent(clock.tick(), EventType.SIMULATION_STOPPED, "simulation stopped"));
    }

    /**
     * Transitions STOPPED → IDLE. Sweeps terminated instances so a subsequent
     * {@link #start()} provisions a fresh set. No-op outside STOPPED state.
     */
    public void reset() {
        if (state != SimulationState.STOPPED) {
            log.warn("reset() called in state {}, ignoring", state);
            return;
        }
        instanceManager.sweepTerminated();
        state = SimulationState.IDLE;
        log.info("simulation reset to IDLE");
    }

    /** Engine-thread loop. Handles pause/stop signals and drives {@link #tick()}. */
    private void runLoop() {
        while (state != SimulationState.STOPPED) {
            if (state == SimulationState.PAUSED) {
                synchronized (pauseLock) {
                    while (state == SimulationState.PAUSED) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                continue;
            }
            tick();
            try {
                Thread.sleep(config.tickDurationMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** Single deterministic step of the simulation. */
    private void tick() {
        try {
            long currentTick = clock.tick();

            instanceManager.sweepTerminated();

            List<Request> requests = trafficGenerator.generate(currentTick);
            metricsCollector.recordGeneratedRequests(requests.size());
            for (Request request : requests) {
                List<ServiceInstance> instances = instanceManager.getInstances();
                if (instances.isEmpty()) {
                    request.markDropped();
                    continue;
                }
                try {
                    ServiceInstance target = loadBalancer.select(instances);
                    target.submit(request);
                } catch (IllegalArgumentException noActive) {
                    request.markDropped();
                }
            }

            List<InstanceSnapshot> instanceSnapshots = instanceManager.getInstances().stream()
                    .map(ServiceInstance::snapshot)
                    .toList();
            Snapshot snapshot = metricsCollector.buildSnapshot(currentTick, instanceSnapshots);

            if (config.autoscalerEnabled()) {
                autoScaler.evaluate(snapshot, currentTick);
            }

            // Re-capture instance states after the autoscaler ran so DRAINING
            // transitions (and freshly added instances) are visible in the UI.
            List<InstanceSnapshot> postScalingInstances = instanceManager.getInstances().stream()
                    .map(ServiceInstance::snapshot)
                    .toList();
            Snapshot uiSnapshot = new Snapshot(
                    snapshot.tick(),
                    snapshot.throughput(),
                    snapshot.avgLatency(),
                    snapshot.avgQueueLength(),
                    snapshot.droppedCount(),
                    snapshot.dropRate(),
                    snapshot.utilization(),
                    snapshot.activeInstanceCount(),
                    snapshot.requestsThisTick(),
                    snapshot.latencyHistory(),
                    snapshot.throughputHistory(),
                    snapshot.instanceCountHistory(),
                    postScalingInstances
            );

            Consumer<Snapshot> listener = snapshotListener;
            if (listener != null) {
                listener.accept(uiSnapshot);
            }

            log.debug("tick {} throughput={} dropped={} active={}",
                    currentTick, snapshot.throughput(), snapshot.droppedCount(),
                    snapshot.activeInstanceCount());

            clock.advance();
        } catch (RuntimeException e) {
            log.error("tick failed", e);
        }
    }

    /** Polls until every managed instance reports {@code isTerminated()} or the timeout elapses. */
    private void awaitInstancesTerminated() {
        long deadline = System.currentTimeMillis() + STOP_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            boolean allTerminated = instanceManager.getInstances().stream()
                    .allMatch(ServiceInstance::isTerminated);
            if (allTerminated) {
                return;
            }
            try {
                Thread.sleep(STOP_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("not all instances terminated within {} ms", STOP_TIMEOUT_MS);
    }
}
