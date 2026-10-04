package cz.arouskova.autoscaler.autoscaler;

import java.util.Locale;

/**
 * Immutable result of a {@link ScalingPolicy} evaluation. Use the static
 * factory methods {@link #noAction()}, {@link #scaleUp(double)} and
 * {@link #scaleDown(double)} to obtain instances.
 *
 * @param decision the action the autoscaler should take
 * @param reason   short human-readable explanation, included in scaling
 *                 events and log lines (may be empty for {@link #noAction()})
 */
public record ScalingDecision(Decision decision, String reason) {

    private static final ScalingDecision NO_ACTION = new ScalingDecision(Decision.NO_ACTION, "");

    /**
     * Returns the shared "do nothing" decision. Always carries
     * {@link Decision#NO_ACTION} and an empty reason.
     *
     * @return the singleton no-action decision
     */
    public static ScalingDecision noAction() {
        return NO_ACTION;
    }

    /**
     * Creates a SCALE_UP decision tagged with the metric value that triggered it.
     *
     * @param metric the average queue length that breached the upper threshold
     * @return a new ScalingDecision with {@link Decision#SCALE_UP} and a reason
     *         like {@code "avgQueue=9.2"}
     */
    public static ScalingDecision scaleUp(double metric) {
        return new ScalingDecision(Decision.SCALE_UP, String.format(Locale.ROOT, "avgQueue=%.1f", metric));
    }

    /**
     * Creates a SCALE_DOWN decision tagged with the metric value that triggered it.
     *
     * @param metric the average queue length that fell below the lower threshold
     * @return a new ScalingDecision with {@link Decision#SCALE_DOWN} and a reason
     *         like {@code "avgQueue=1.1"}
     */
    public static ScalingDecision scaleDown(double metric) {
        return new ScalingDecision(Decision.SCALE_DOWN, String.format(Locale.ROOT, "avgQueue=%.1f", metric));
    }
}
