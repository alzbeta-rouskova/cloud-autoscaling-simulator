package cz.cvut.fel.pjv2026.autoscaler;

import cz.cvut.fel.pjv2026.core.Snapshot;

public interface ScalingPolicy {
    ScalingDecision evaluate(Snapshot snapshot);
}
