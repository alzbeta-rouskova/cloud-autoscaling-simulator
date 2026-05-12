package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.Snapshot;

/**
 * Scaling policy that decides based on the average queue length across ACTIVE
 * instances. Uses separate upper and lower thresholds (hysteresis) and refuses
 * to cross the configured min/max instance bounds, returning
 * {@link Decision#NO_ACTION} when at a limit instead of throwing.
 *
 * @see ScalingPolicy
 */
public class ThresholdScalingPolicy implements ScalingPolicy {

    private final int scaleUpThreshold;
    private final int scaleDownThreshold;
    private final int minInstances;
    private final int maxInstances;

    /**
     * @param scaleUpThreshold   avg queue length strictly above which SCALE_UP is issued
     * @param scaleDownThreshold avg queue length strictly below which SCALE_DOWN is issued
     * @param minInstances       soft floor — at or below this count, SCALE_DOWN yields NO_ACTION
     * @param maxInstances       soft ceiling — at or above this count, SCALE_UP yields NO_ACTION
     */
    public ThresholdScalingPolicy(int scaleUpThreshold, int scaleDownThreshold, int minInstances, int maxInstances) {
        this.scaleUpThreshold = scaleUpThreshold;
        this.scaleDownThreshold = scaleDownThreshold;
        this.minInstances = minInstances;
        this.maxInstances = maxInstances;
    }

    /**
     * Inspects the snapshot's average queue length and active instance count
     * and returns the corresponding scaling decision. Returns NO_ACTION when
     * the queue is in the normal range, or when scaling would violate the
     * configured min/max bounds.
     *
     * @param snapshot current simulation snapshot
     * @return a scale-up, scale-down, or no-action decision
     */
    @Override
    public ScalingDecision evaluate(Snapshot snapshot) {
        double avgQueue = snapshot.avgQueueLength();
        int active = snapshot.activeInstanceCount();

        if (avgQueue > scaleUpThreshold && active < maxInstances) {
            return ScalingDecision.scaleUp(avgQueue);
        }
        if (avgQueue < scaleDownThreshold && active > minInstances) {
            return ScalingDecision.scaleDown(avgQueue);
        }
        return ScalingDecision.noAction();
    }
}
