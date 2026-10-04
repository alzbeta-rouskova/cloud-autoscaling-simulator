package cz.arouskova.autoscaler.instance;

import cz.arouskova.autoscaler.metrics.LatencyTracker;
import cz.arouskova.autoscaler.model.Request;
import cz.arouskova.autoscaler.model.RequestStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceInstanceTest {

    private ServiceInstance instance;

    @AfterEach
    void cleanup() {
        // Retire the instance to stop worker threads between tests.
        if (instance != null) {
            instance.retire();
        }
    }

    private ServiceInstance newInstance(int queueCapacity, int workerCount) {
        instance = new ServiceInstance("test", new InstanceConfig(queueCapacity, workerCount), new LatencyTracker());
        return instance;
    }

    private static Request request(long id, long serviceTimeMs) {
        return new Request(id, 0L, serviceTimeMs);
    }

    private static void awaitStatus(Request r, RequestStatus expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (r.getStatus() != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
    }

    @Test
    void processes_submitted_request() throws InterruptedException {
        ServiceInstance inst = newInstance(10, 2);
        Request r = request(1, 20);

        inst.submit(r);

        awaitStatus(r, RequestStatus.COMPLETED, 1000);
        assertEquals(RequestStatus.COMPLETED, r.getStatus());
    }

    @Test
    void drops_request_when_queue_full() {
        // queueCapacity=1, workerCount=1 → at most 2 accepted; submitting more
        // forces at least one drop regardless of worker scheduling timing.
        ServiceInstance inst = newInstance(1, 1);

        Request firstDropped = null;
        for (int i = 1; i <= 10; i++) {
            Request r = request(i, 10_000);
            if (!inst.submit(r)) {
                firstDropped = r;
                break;
            }
        }

        assertNotNull(firstDropped, "expected at least one drop within 10 submits");
        assertEquals(RequestStatus.DROPPED, firstDropped.getStatus());
    }

    @Test
    void increments_dropped_counter_on_drop() {
        ServiceInstance inst = newInstance(1, 1);
        inst.submit(request(1, 10_000));
        inst.submit(request(2, 10_000));

        inst.submit(request(3, 10_000));

        assertTrue(inst.snapshot().droppedCount >= 1);
    }

    @Test
    void respects_capacity_limit() {
        // workerCount=2, queueCapacity=2 → at most 4 in flight at any moment.
        // Submitting 10 with a long service time MUST produce drops, regardless
        // of when worker threads actually wake up (lazy thread pool startup).
        ServiceInstance inst = newInstance(2, 2);

        int dropCount = 0;
        for (int i = 1; i <= 10; i++) {
            if (!inst.submit(request(i, 10_000))) {
                dropCount++;
            }
        }

        assertTrue(dropCount >= 5, "expected at least 5 drops out of 10, got " + dropCount);
    }

    @Test
    void retire_marks_status_draining() {
        ServiceInstance inst = newInstance(10, 2);

        inst.retire();

        assertEquals(InstanceStatus.DRAINING, inst.getStatus());
    }

    @Test
    void submit_rejects_after_retire() {
        ServiceInstance inst = newInstance(10, 2);
        inst.retire();

        assertFalse(inst.submit(request(1, 10)));
    }

    @Test
    void retire_is_idempotent() {
        ServiceInstance inst = newInstance(10, 2);

        inst.retire();
        inst.retire();

        assertEquals(InstanceStatus.DRAINING, inst.getStatus());
    }

    @Test
    void initial_status_is_active() {
        ServiceInstance inst = newInstance(10, 2);

        assertEquals(InstanceStatus.ACTIVE, inst.getStatus());
    }
}
