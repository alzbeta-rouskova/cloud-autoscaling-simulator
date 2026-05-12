package cz.cvut.fel.pjv2026.autoscaler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anti-thrashing gate for the autoscaler. After a scaling action is recorded,
 * subsequent {@link #canScale(long)} queries return {@code false} until the
 * configured number of ticks has elapsed.
 * <p>
 * Boundary convention: {@code canScale} returns {@code true} once at least
 * {@code cooldownTicks} ticks have elapsed since the last recorded scale —
 * the comparison is {@code >=}, not strict.
 */
public class CooldownTracker {

    private static final Logger log = LoggerFactory.getLogger(CooldownTracker.class);

    /** Sentinel meaning "no scale has been recorded yet". */
    private static final long NO_SCALE = -1L;

    private final int cooldownTicks;
    private long lastScaleTick = NO_SCALE;

    /**
     * @param cooldownTicks minimum number of ticks that must elapse between
     *                      two scaling actions (zero disables the cooldown)
     */
    public CooldownTracker(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    /**
     * Reports whether a scaling action is allowed at the given tick.
     *
     * @param currentTick the tick at which the autoscaler is considering a scale action
     * @return {@code true} if no scale has been recorded yet, or if at least
     *         {@code cooldownTicks} ticks have elapsed since the last recorded
     *         scale; {@code false} otherwise
     */
    public boolean canScale(long currentTick) {
        if (lastScaleTick == NO_SCALE) {
            return true;
        }
        long elapsed = currentTick - lastScaleTick;
        if (elapsed >= cooldownTicks) {
            return true;
        }
        log.debug("scale blocked at tick {}, {} ticks remaining in cooldown",
                currentTick, cooldownTicks - elapsed);
        return false;
    }

    /**
     * Records that a scaling action has just been performed. Subsequent calls
     * to {@link #canScale(long)} will be blocked until {@code cooldownTicks}
     * ticks have elapsed.
     *
     * @param currentTick the tick at which the scaling action was performed
     */
    public void recordScale(long currentTick) {
        this.lastScaleTick = currentTick;
    }

    /**
     * Clears any recorded scale event. After calling this, {@link #canScale(long)}
     * returns {@code true} at any tick until a new scale is recorded.
     */
    public void reset() {
        this.lastScaleTick = NO_SCALE;
    }
}
