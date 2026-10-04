package cz.arouskova.autoscaler.autoscaler;

import cz.arouskova.autoscaler.core.Snapshot;

/**
 * Interface representing a scaling policy that evaluates the current state of the system
 * and makes a decision on whether to scale up, scale down, or take no action.
 */
public interface ScalingPolicy {

    /**
     * Evaluates the given snapshot of the system and returns a scaling decision.
     *
     * @param snapshot the current state of the system
     * @return a ScalingDecision indicating whether to scale up, scale down, or take no action
     */
    ScalingDecision evaluate(Snapshot snapshot);
}
