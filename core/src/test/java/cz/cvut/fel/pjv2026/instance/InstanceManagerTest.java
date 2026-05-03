package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.exception.InstanceException;
import cz.cvut.fel.pjv2026.metrics.LatencyTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceManagerTest {

    private InstanceManager manager;

    @AfterEach
    void cleanup() {
        // Retire every managed instance so no worker thread leaks between tests.
        if (manager != null) {
            for (ServiceInstance inst : manager.getInstances()) {
                inst.retire();
            }
        }
    }

    private InstanceManager newManager() {
        manager = new InstanceManager(new InstanceConfig(10, 2), new LatencyTracker());
        return manager;
    }

    private static void awaitTermination(ServiceInstance inst, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!inst.isTerminated() && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
    }

    @Test
    void add_instance_increases_count() {
        InstanceManager m = newManager();
        int before = m.getInstances().size();

        m.addInstance();

        assertEquals(before + 1, m.getInstances().size());
    }

    @Test
    void retire_instance_marks_draining() throws InstanceException {
        InstanceManager m = newManager();
        m.addInstance();
        m.addInstance();
        ServiceInstance target = m.getInstances().get(0);

        m.retireInstance(target.getId());

        assertEquals(InstanceStatus.DRAINING, target.getStatus());
        assertTrue(m.getInstances().contains(target),
                "DRAINING instance must still be visible in getInstances() until swept");
    }

    @Test
    void sweep_removes_terminated_instances() throws InstanceException, InterruptedException {
        InstanceManager m = newManager();
        m.addInstance();
        m.addInstance();
        ServiceInstance target = m.getInstances().get(0);
        m.retireInstance(target.getId());

        // Empty queue → pool terminates quickly after shutdown().
        awaitTermination(target, 1000);
        m.sweepTerminated();

        assertFalse_contains(m, target);
    }

    private static void assertFalse_contains(InstanceManager m, ServiceInstance target) {
        if (m.getInstances().contains(target)) {
            throw new AssertionError("instance should have been swept after termination");
        }
    }

    @Test
    void cannot_retire_last_active_instance() {
        InstanceManager m = newManager();
        m.addInstance();
        ServiceInstance only = m.getInstances().get(0);

        assertThrows(InstanceException.class, () -> m.retireInstance(only.getId()));
    }

    @Test
    void retire_unknown_id_throws() {
        InstanceManager m = newManager();
        m.addInstance();
        m.addInstance();

        assertThrows(InstanceException.class, () -> m.retireInstance("does-not-exist"));
    }
}
