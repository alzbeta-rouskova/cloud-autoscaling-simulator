package cz.arouskova.autoscaler.traffic;

/**
 * Shared base for traffic profiles. Holds the baseline rate that
 * subclasses may modulate (e.g. with periodic spikes).
 */
public abstract class AbstractTrafficProfile implements TrafficProfile {

    protected final int baseRate;

    /**
     * Creates a profile with the given baseline rate.
     *
     * @param baseRate baseline number of requests per tick; must be non-negative
     */
    protected AbstractTrafficProfile(int baseRate) {

        this.baseRate = baseRate;
    }

    @Override
    public abstract int requestsForTick(long tick);
}
