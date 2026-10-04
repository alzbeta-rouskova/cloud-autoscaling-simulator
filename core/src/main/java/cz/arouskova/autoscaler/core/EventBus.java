package cz.arouskova.autoscaler.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * * Simple event bus for publishing simulation events to multiple listeners.
 */
public class EventBus {

    private final List<Consumer<SimulationEvent>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Publishes a simulation event to all subscribed listeners.
     *
     * @param event the simulation event to publish
     */
    public void publish(SimulationEvent event) {
        for (Consumer<SimulationEvent> listener : listeners) {
            listener.accept(event);
        }
    }

    /**
     * Subscribes a listener to receive simulation events.
     *
     * @param listener the listener to subscribe
     */
    public void subscribe(Consumer<SimulationEvent> listener) {
        listeners.add(listener);
    }

    /**
     * Unsubscribes a listener from receiving simulation events.
     *
     * @param listener the listener to unsubscribe
     */
    public void unsubscribe(Consumer<SimulationEvent> listener) {
        listeners.remove(listener);
    }
}
