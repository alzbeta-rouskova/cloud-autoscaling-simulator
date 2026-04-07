package cz.cvut.fel.pjv2026.model;

public class ConstantServiceTimeModel implements ServiceTimeModel {

    private final long serviceTimeMs;

    public ConstantServiceTimeModel(long serviceTimeMs) {
        this.serviceTimeMs = serviceTimeMs;
    }

    @Override
    public long serviceTimeMs() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
