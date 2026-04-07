package cz.cvut.fel.pjv2026.core;

public class SimulationEvent {

    public long tick;
    public EventType type;
    public String message;

    public SimulationEvent(long tick, EventType type, String message) {
        this.tick = tick;
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[tick=" + tick + "] " + type + ": " + message;
    }
}
