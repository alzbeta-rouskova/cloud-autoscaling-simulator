package cz.cvut.fel.pjv2026.traffic;

public abstract class AbstractTrafficProfile implements TrafficProfile {

    protected int baseRate;

    protected AbstractTrafficProfile(int baseRate) {

        this.baseRate = baseRate;
    }

    @Override
    public abstract int requestsForTick(int tick);
}
