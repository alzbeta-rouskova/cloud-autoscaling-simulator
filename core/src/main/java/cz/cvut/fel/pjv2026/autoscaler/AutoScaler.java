package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.EventBus;
import cz.cvut.fel.pjv2026.core.EventType;
import cz.cvut.fel.pjv2026.core.SimulationEvent;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.exception.InstanceException;
import cz.cvut.fel.pjv2026.instance.InstanceManager;
import cz.cvut.fel.pjv2026.instance.InstanceStatus;
import cz.cvut.fel.pjv2026.instance.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wires a {@link ScalingPolicy} together with a {@link CooldownTracker} and
 * applies the resulting decisions to the {@link InstanceManager}. Invoked
 * every tick from {@code SimulationEngine.tick()} when the autoscaler is
 * enabled; the cadence gate ({@code evaluationIntervalTicks}) lives here, not
 * in the engine.
 * <p>
 * On SCALE_UP a new instance is added. On SCALE_DOWN the newest ACTIVE
 * instance is retired (LIFO). Both outcomes are logged at INFO and published
 * as a {@link SimulationEvent} on the bus.
 */
public class AutoScaler {

    private static final Logger log = LoggerFactory.getLogger(AutoScaler.class);

    private final int evaluationIntervalTicks;
    private final ScalingPolicy policy;
    private final CooldownTracker cooldownTracker;
    private final InstanceManager instanceManager;
    private final EventBus eventBus;

    /**
     * @param evaluationIntervalTicks how often to evaluate the policy, in ticks
     * @param policy                  decides scale up / down / no action from a snapshot
     * @param cooldownTracker         blocks back-to-back scale actions within a cooldown window
     * @param instanceManager         where to apply scale up / down actions
     * @param eventBus                where to publish SCALE_UP / SCALE_DOWN events
     */
    public AutoScaler(int evaluationIntervalTicks, ScalingPolicy policy,
                      CooldownTracker cooldownTracker, InstanceManager instanceManager,
                      EventBus eventBus) {
        this.evaluationIntervalTicks = evaluationIntervalTicks;
        this.policy = policy;
        this.cooldownTracker = cooldownTracker;
        this.instanceManager = instanceManager;
        this.eventBus = eventBus;
    }

    /**
     * Called once per tick by the engine. Skips evaluation outside the configured
     * cadence; otherwise runs the policy, applies the decision (subject to the
     * cooldown gate), and publishes a scale event when an action is taken.
     *
     * @param snapshot    snapshot of the simulation state at {@code currentTick}
     * @param currentTick monotonic tick index
     */
    public void evaluate(Snapshot snapshot, long currentTick) {
        if (currentTick % evaluationIntervalTicks != 0) {
            return;
        }
        ScalingDecision decision = policy.evaluate(snapshot);
        log.debug("evaluation at tick {}: {} ({})",
                currentTick, decision.decision(), decision.reason());

        if (decision.decision() == Decision.NO_ACTION) {
            return;
        }
        if (!cooldownTracker.canScale(currentTick)) {
            return;
        }

        switch (decision.decision()) {
            case SCALE_UP -> applyScaleUp(decision, currentTick);
            case SCALE_DOWN -> applyScaleDown(decision, currentTick);
            default -> { /* NO_ACTION already handled above */ }
        }
    }

    private void applyScaleUp(ScalingDecision decision, long currentTick) {
        String newId = instanceManager.addInstance();
        String message = "scale up: added " + newId + " (" + decision.reason() + ")";
        log.info(message);
        eventBus.publish(new SimulationEvent(currentTick, EventType.SCALE_UP, message));
        cooldownTracker.recordScale(currentTick);
    }

    private void applyScaleDown(ScalingDecision decision, long currentTick) {
        String targetId = findNewestActiveId();
        if (targetId == null) {
            log.warn("scale down requested at tick {} but no ACTIVE instance found", currentTick);
            return;
        }
        try {
            instanceManager.retireInstance(targetId);
        } catch (InstanceException e) {
            log.warn("scale down at tick {} blocked by hard floor: {}", currentTick, e.getMessage());
            return;
        }
        String message = "scale down: retiring " + targetId + " (" + decision.reason() + ")";
        log.info(message);
        eventBus.publish(new SimulationEvent(currentTick, EventType.SCALE_DOWN, message));
        cooldownTracker.recordScale(currentTick);
    }

    private String findNewestActiveId() {
        List<ServiceInstance> instances = instanceManager.getInstances();
        for (int i = instances.size() - 1; i >= 0; i--) {
            ServiceInstance inst = instances.get(i);
            if (inst.getStatus() == InstanceStatus.ACTIVE) {
                return inst.getId();
            }
        }
        return null;
    }
}
