package cz.arouskova.autoscaler.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventBusTest {

    @Test
    void subscriber_receives_published_event() {
        EventBus bus = new EventBus();
        AtomicReference<SimulationEvent> received = new AtomicReference<>();
        bus.subscribe(received::set);

        SimulationEvent event = new SimulationEvent(1L, EventType.SCALE_UP, "test");
        bus.publish(event);

        assertEquals(event, received.get());
    }

    @Test
    void multiple_subscribers_all_receive_event() {
        EventBus bus = new EventBus();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(e -> count.incrementAndGet());
        bus.subscribe(e -> count.incrementAndGet());

        bus.publish(new SimulationEvent(1L, EventType.SCALE_UP, "test"));

        assertEquals(2, count.get());
    }

    @Test
    void unsubscribed_listener_does_not_receive_event() {
        EventBus bus = new EventBus();
        AtomicReference<SimulationEvent> received = new AtomicReference<>();
        Consumer<SimulationEvent> listener = received::set;
        bus.subscribe(listener);
        bus.unsubscribe(listener);

        bus.publish(new SimulationEvent(1L, EventType.SCALE_UP, "test"));

        assertNull(received.get());
    }
}
