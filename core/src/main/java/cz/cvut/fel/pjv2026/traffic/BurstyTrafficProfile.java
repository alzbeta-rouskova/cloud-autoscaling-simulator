package cz.cvut.fel.pjv2026.traffic;

public class BurstyTrafficProfile extends AbstractTrafficProfile {

    private double burstMultiplier;
    private int burstIntervalTicks;

    public BurstyTrafficProfile(int baseRate, double burstMultiplier, int burstIntervalTicks) {
        super(baseRate);
        this.burstMultiplier = burstMultiplier;
        this.burstIntervalTicks = burstIntervalTicks;
    }

    @Override
    public int requestsForTick(long tick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
