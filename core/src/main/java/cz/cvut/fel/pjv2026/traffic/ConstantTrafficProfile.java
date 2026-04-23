package cz.cvut.fel.pjv2026.traffic;

/**
 * Traffic profile that emits the same number of requests every tick.
 */
public class ConstantTrafficProfile extends AbstractTrafficProfile {

    /**
     * Creates a constant profile with the given per-tick rate.
     *
     * @param baseRate number of requests emitted every tick
     */
    public ConstantTrafficProfile(int baseRate) {

        super(baseRate);
    }

    /**
     * Returns the constant baseline rate regardless of the tick.
     *
     * @param tick simulation tick index (ignored)
     * @return the configured baseline rate
     */
    @Override
    public int requestsForTick(long tick) {
        return baseRate;
    }
}
