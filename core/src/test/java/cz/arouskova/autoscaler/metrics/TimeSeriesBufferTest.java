package cz.arouskova.autoscaler.metrics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeSeriesBufferTest {

    @Test
    void empty_buffer_returns_empty_list() {
        TimeSeriesBuffer buffer = new TimeSeriesBuffer(5);

        assertEquals(List.of(), buffer.values());
    }

    @Test
    void returns_correct_values_in_order() {
        TimeSeriesBuffer buffer = new TimeSeriesBuffer(5);

        buffer.add(1.0);
        buffer.add(2.0);
        buffer.add(3.0);

        assertEquals(List.of(1.0, 2.0, 3.0), buffer.values());
    }

    @Test
    void buffer_wraps_around_at_max_capacity() {
        TimeSeriesBuffer buffer = new TimeSeriesBuffer(3);

        buffer.add(1.0);
        buffer.add(2.0);
        buffer.add(3.0);
        buffer.add(4.0);

        assertEquals(List.of(2.0, 3.0, 4.0), buffer.values());
    }
}
