package cz.cvut.fel.pjv2026.core;

/**
 * Represents an event that occurs during the simulation, such as scaling actions, traffic changes, or errors.
 *
 * @param tick    the simulation tick at which the event occurred
 * @param type    the type of the event
 * @param message a descriptive message about the event
 */
public record SimulationEvent(long tick, EventType type, String message) {

    /**
     * Returns a string representation of the simulation event, including the tick, event type, and message.
     *
     * @return a string representation of the simulation event
     */
    @Override
    public String toString() {
        return "[tick=" + tick + "] " + type + ": " + message;
    }
}
