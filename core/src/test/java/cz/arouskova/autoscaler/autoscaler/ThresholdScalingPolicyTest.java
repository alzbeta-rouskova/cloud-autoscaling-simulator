package cz.arouskova.autoscaler.autoscaler;

import cz.arouskova.autoscaler.core.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThresholdScalingPolicyTest {

    @Test
    void scale_up_when_queue_above_threshold() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(10, 2, 1, 8);
        ScalingDecision d = policy.evaluate(snapshot(11.0, 3));
        assertEquals(Decision.SCALE_UP, d.decision());
    }

    @Test
    void scale_down_when_queue_below_threshold() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(10, 2, 1, 8);
        ScalingDecision d = policy.evaluate(snapshot(1.0, 3));
        assertEquals(Decision.SCALE_DOWN, d.decision());
    }

    @Test
    void no_action_in_normal_range() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(10, 2, 1, 8);
        ScalingDecision d = policy.evaluate(snapshot(5.0, 3));
        assertEquals(Decision.NO_ACTION, d.decision());
    }

    @Test
    void respects_max_instances_limit() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(10, 2, 1, 3);
        ScalingDecision d = policy.evaluate(snapshot(20.0, 3));
        assertEquals(Decision.NO_ACTION, d.decision());
    }

    @Test
    void respects_min_instances_limit() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(10, 2, 2, 8);
        ScalingDecision d = policy.evaluate(snapshot(1.0, 2));
        assertEquals(Decision.NO_ACTION, d.decision());
    }

    @Test
    void scale_up_decision_contains_reason() {
        ThresholdScalingPolicy policy = new ThresholdScalingPolicy(8, 2, 1, 8);
        ScalingDecision d = policy.evaluate(snapshot(9.2, 3));
        assertTrue(d.reason().contains("9.2"));
    }

    private static Snapshot snapshot(double avgQueueLength, int activeInstanceCount) {
        return new Snapshot(
                0L,
                0.0,
                0.0,
                avgQueueLength,
                0,
                0.0,
                0.0,
                activeInstanceCount,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
