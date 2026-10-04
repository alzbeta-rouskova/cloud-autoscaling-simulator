package cz.arouskova.autoscaler.traffic;

/**
 * Traffic profile emitting a baseline rate with periodic spikes.
 * Every {@code burstIntervalTicks} ticks the rate is multiplied by
 * {@code burstMultiplier} (spike lasts for a single tick).
 */
public class BurstyTrafficProfile extends AbstractTrafficProfile {

    private final double burstMultiplier;
    private final int burstIntervalTicks;

    /**
     * Creates a bursty profile.
     *
     * @param baseRate           baseline requests per tick
     * @param burstMultiplier    factor applied to {@code baseRate} during a spike (e.g. 3.0)
     * @param burstIntervalTicks period between spikes, in ticks; must be positive
     */
    public BurstyTrafficProfile(int baseRate, double burstMultiplier, int burstIntervalTicks) {
        super(baseRate);
        this.burstMultiplier = burstMultiplier;
        this.burstIntervalTicks = burstIntervalTicks;
    }

    /**
     * Returns baseline rate, or the spiked rate on burst ticks.
     * A spike occurs when {@code tick > 0 && tick % burstIntervalTicks == 0}.
     *
     * @param tick simulation tick index
     * @return requests for this tick
     */
    @Override
    public int requestsForTick(long tick) {
        if (tick > 0 && tick % burstIntervalTicks == 0) {

            return (int) Math.round(baseRate * burstMultiplier);
        }
        return baseRate;
    }
}
