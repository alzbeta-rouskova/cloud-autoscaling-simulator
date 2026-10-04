package cz.arouskova.autoscaler.autoscaler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalingDecisionTest {

    @Test
    void scale_up_decision_has_scale_up_type() {
        ScalingDecision d = ScalingDecision.scaleUp(9.2);
        assertEquals(Decision.SCALE_UP, d.decision());
    }

    @Test
    void scale_up_decision_contains_reason() {
        ScalingDecision d = ScalingDecision.scaleUp(9.2);
        assertTrue(d.reason().contains("9.2"));
    }

    @Test
    void scale_down_decision_has_scale_down_type() {
        ScalingDecision d = ScalingDecision.scaleDown(1.1);
        assertEquals(Decision.SCALE_DOWN, d.decision());
    }

    @Test
    void scale_down_decision_contains_reason() {
        ScalingDecision d = ScalingDecision.scaleDown(1.1);
        assertTrue(d.reason().contains("1.1"));
    }

    @Test
    void no_action_decision_has_no_action_type() {
        ScalingDecision d = ScalingDecision.noAction();
        assertEquals(Decision.NO_ACTION, d.decision());
    }

    @Test
    void no_action_reason_is_not_null() {
        ScalingDecision d = ScalingDecision.noAction();
        assertNotNull(d.reason());
    }
}
