package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.model.Request;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestQueueTest {

    private static Request request(long id) {
        return new Request(id, 0L, 10L);
    }

    @Test
    void accepts_request_within_capacity() {
        RequestQueue queue = new RequestQueue(2);

        assertTrue(queue.offer(request(1)));
        assertTrue(queue.offer(request(2)));
    }

    @Test
    void rejects_request_when_full() {
        RequestQueue queue = new RequestQueue(1);
        queue.offer(request(1));

        assertFalse(queue.offer(request(2)));
    }

    @Test
    void tracks_dropped_count() {
        RequestQueue queue = new RequestQueue(1);
        queue.offer(request(1));

        queue.offer(request(2));

        assertEquals(1, queue.droppedCount());
    }
}
