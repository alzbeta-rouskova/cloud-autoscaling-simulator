package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.exception.InstanceException;
import cz.cvut.fel.pjv2026.metrics.LatencyTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the lifecycle of service instances during simulation.
 * <p>
 * An instance progresses through three observable phases:
 * <ol>
 *   <li>ACTIVE — accepts new requests, visible to load balancers</li>
 *   <li>DRAINING — after {@link #retireInstance(String)}: worker pool is shut down,
 *       queue continues draining; still visible in {@link #getInstances()},
 *       but load balancers filter it out via {@code activeOnly()}</li>
 *   <li>terminated — worker pool has finished; removed from the list by
 *       {@link #sweepTerminated()} at the next tick</li>
 * </ol>
 * <p>
 * All methods are expected to run on the engine thread; the underlying list
 * is therefore not synchronized.
 */
public class InstanceManager {

    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);

    /** Hard floor on the number of ACTIVE instances (cannot retire below this). */
    private static final int MIN_ACTIVE_INSTANCES = 1;

    private final InstanceConfig config;
    private final LatencyTracker latencyTracker;
    private final List<ServiceInstance> instances = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    /**
     * Creates an empty manager. Instances must be added explicitly via
     * {@link #addInstance()}.
     *
     * @param config         shared per-instance configuration (queue capacity, worker count)
     * @param latencyTracker shared latency tracker injected into every new instance
     */
    public InstanceManager(InstanceConfig config, LatencyTracker latencyTracker) {
        this.config = config;
        this.latencyTracker = latencyTracker;
    }

    /**
     * Creates a new ACTIVE service instance with a fresh id, adds it to the
     * managed list, and returns the id so callers (e.g. the autoscaler) can
     * include it in event messages and logs.
     *
     * @return id of the newly added instance
     */
    public String addInstance() {
        String id = "instance-" + idCounter.incrementAndGet();
        ServiceInstance instance = new ServiceInstance(id, config, latencyTracker);
        instances.add(instance);
        log.info("added instance {}", id);
        return id;
    }

    /**
     * Starts retirement of the given instance: calls {@link ServiceInstance#retire()},
     * which marks it DRAINING and shuts down its worker pool. The instance
     * remains in the managed list until its pool terminates and
     * {@link #sweepTerminated()} removes it on a subsequent tick.
     * <p>
     * Refuses to retire if it would drop the ACTIVE count below
     * {@value #MIN_ACTIVE_INSTANCES} (only checked when the target is itself ACTIVE
     * — retiring an already-DRAINING instance is a no-op).
     *
     * @param instanceId id of the instance to retire
     * @throws InstanceException if the id is unknown or retiring would breach the minimum
     */
    public void retireInstance(String instanceId) throws InstanceException {
        ServiceInstance target = findById(instanceId);
        if (target.getStatus() == InstanceStatus.ACTIVE && countActive() <= MIN_ACTIVE_INSTANCES) {
            throw new InstanceException(
                    "cannot retire instance " + instanceId + ": would drop ACTIVE count below "
                            + MIN_ACTIVE_INSTANCES);
        }
        log.info("retiring instance {}", instanceId);
        target.retire();
    }

    /**
     * Removes DRAINING instances whose worker pool has terminated.
     * Called by the engine thread at the start of every tick.
     */
    public void sweepTerminated() {
        instances.removeIf(inst -> {
            if (inst.isTerminated()) {
                log.debug("sweeping terminated instance {}", inst.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * Returns all managed instances, including DRAINING ones that have not yet
     * been swept. Load balancers must filter to ACTIVE via the
     * {@code activeOnly} helper on {@link cz.cvut.fel.pjv2026.lb.AbstractLoadBalancer}.
     *
     * @return unmodifiable view of currently managed instances
     */
    public List<ServiceInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    private ServiceInstance findById(String instanceId) throws InstanceException {
        for (ServiceInstance inst : instances) {
            if (inst.getId().equals(instanceId)) {
                return inst;
            }
        }
        throw new InstanceException("unknown instance id: " + instanceId);
    }

    private long countActive() {
        return instances.stream()
                .filter(i -> i.getStatus() == InstanceStatus.ACTIVE)
                .count();
    }
}
