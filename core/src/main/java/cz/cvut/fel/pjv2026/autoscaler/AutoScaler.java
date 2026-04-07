package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.EventBus;
import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.instance.InstanceManager;

public class AutoScaler {

    private int evaluationIntervalTicks;
    private final ScalingPolicy policy;
    private final CooldownTracker cooldownTracker;
    private final InstanceManager instanceManager;
    private final EventBus eventBus;

    public AutoScaler(int evaluationIntervalTicks, ScalingPolicy policy,
                      CooldownTracker cooldownTracker, InstanceManager instanceManager,
                      EventBus eventBus) {
        this.evaluationIntervalTicks = evaluationIntervalTicks;
        this.policy = policy;
        this.cooldownTracker = cooldownTracker;
        this.instanceManager = instanceManager;
        this.eventBus = eventBus;
    }

    public void evaluate(Snapshot snapshot, long currentTick) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
