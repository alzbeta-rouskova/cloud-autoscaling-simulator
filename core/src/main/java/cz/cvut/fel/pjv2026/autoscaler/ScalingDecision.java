package cz.cvut.fel.pjv2026.autoscaler;

public class ScalingDecision {

    public Decision decision;
    public String reason;

    private ScalingDecision(Decision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public static ScalingDecision noAction() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static ScalingDecision scaleUp(double metric) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static ScalingDecision scaleDown(double metric) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
