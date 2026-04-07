package cz.cvut.fel.pjv2026.autoscaler;

public class CooldownTracker {

    private int cooldownTicks;

    public CooldownTracker(int cooldownTicks) {

        this.cooldownTicks = cooldownTicks;
    }

    public boolean canScale(int currentTick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void recordScale(int currentTick) {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void reset() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
