package cz.cvut.fel.pjv2026.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RequestTest {

    @Test
    void request_has_unique_id() {
        Request a = new Request(1L, 0L, 10L);
        Request b = new Request(2L, 0L, 10L);

        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void request_stores_arrival_time() {
        Request r = new Request(1L, 42L, 10L);

        assertEquals(42L, r.getArrivalTime());
    }

    @Test
    void request_stores_service_time() {
        Request r = new Request(1L, 0L, 25L);

        assertEquals(25L, r.getServiceTimeMs());
    }

}
