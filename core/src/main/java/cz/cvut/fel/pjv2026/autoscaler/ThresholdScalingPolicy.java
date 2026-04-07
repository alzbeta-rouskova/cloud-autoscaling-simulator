package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.Snapshot;

public class ThresholdScalingPolicy implements ScalingPolicy {

    private int scaleUpThreshold;
    private int scaleDownThreshold;
    private int minInstances;
    private int maxInstances;

    public ThresholdScalingPolicy(int scaleUpThreshold, int scaleDownThreshold, int minInstances, int maxInstances) {
        this.scaleUpThreshold = scaleUpThreshold;
        this.scaleDownThreshold = scaleDownThreshold;
        this.minInstances = minInstances;
        this.maxInstances = maxInstances;
    }

    @Override
    public ScalingDecision evaluate(Snapshot snapshot) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
