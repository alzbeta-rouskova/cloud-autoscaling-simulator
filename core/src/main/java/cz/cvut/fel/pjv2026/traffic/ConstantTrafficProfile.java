package cz.cvut.fel.pjv2026.traffic;

public class ConstantTrafficProfile extends AbstractTrafficProfile {

    public ConstantTrafficProfile(int baseRate) {

        super(baseRate);
    }

    @Override
    public int requestsForTick(long tick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
