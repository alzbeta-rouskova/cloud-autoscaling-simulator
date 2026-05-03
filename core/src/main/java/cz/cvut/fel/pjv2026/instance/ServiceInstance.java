package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.metrics.LatencyTracker;
import cz.cvut.fel.pjv2026.model.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Single simulated server instance with a bounded request queue and a fixed
 * thread pool of workers that process requests in parallel.
 * <p>
 * Implements a classic producer/consumer pattern:
 * {@link #submit(Request)} places requests into {@link RequestQueue};
 * long-lived worker threads pull and process them until {@link #shutdown()}
 * is requested and the queue is drained.
 */
public class ServiceInstance {

    private static final Logger log = LoggerFactory.getLogger(ServiceInstance.class);

    /** How long a worker waits for the next request before re-checking the stop flag. */
    private static final long POLL_TIMEOUT_MS = 50;

    private final String id;
    private final RequestQueue queue;
    private final InstanceConfig config;
    private final LatencyTracker latencyTracker;
    private final ExecutorService workerPool;

    private volatile InstanceStatus status;
    private volatile boolean stopRequested = false;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    /**
     * Creates an active instance and immediately starts {@code workerCount}
     * worker threads that consume requests from the queue.
     *
     * @param id             unique instance id (used in logs and snapshots)
     * @param config         queue capacity and worker count
     * @param latencyTracker shared tracker that workers report request latency to
     */
    public ServiceInstance(String id, InstanceConfig config, LatencyTracker latencyTracker) {
        this.id = id;
        this.config = config;
        this.latencyTracker = latencyTracker;
        this.queue = new RequestQueue(config.queueCapacity);
        this.workerPool = Executors.newFixedThreadPool(config.workerCount);
        this.status = InstanceStatus.ACTIVE;
        for (int i = 0; i < config.workerCount; i++) {
            workerPool.submit(this::runWorker);
        }
    }

    /**
     * Returns the unique id of this instance.
     *
     * @return instance id
     */
    public String getId() {

        return id;
    }

    /**
     * Attempts to enqueue the request for processing.
     * Rejects (marks {@code DROPPED}) when the instance is not ACTIVE
     * or when the queue is full.
     *
     * @param request the request to enqueue
     * @return {@code true} if the request was accepted, {@code false} if it was dropped
     */
    public boolean submit(Request request) {
        if (status != InstanceStatus.ACTIVE) {
            request.markDropped();
            log.warn("instance {} rejected request {}: status is {}", id, request.getId(), status);
            return false;
        }
        if (!queue.offer(request)) {
            request.markDropped();
            log.warn("instance {} dropped request {}: queue full", id, request.getId());
            return false;
        }
        return true;
    }

    /**
     * Returns the current number of pending requests in the queue.
     *
     * @return current queue size
     */
    public int currentQueueSize() {

        return queue.size();
    }

    /**
     * Builds an immutable snapshot of the instance's current observable state.
     * Safe to call from any thread; values are read independently and may
     * reflect a slightly inconsistent point in time across fields.
     *
     * @return a new immutable {@link InstanceSnapshot}
     */
    public InstanceSnapshot snapshot() {
        return new InstanceSnapshot(
                id,
                queue.size(),
                activeWorkers.get(),
                config.workerCount,
                processedCount.get(),
                queue.droppedCount(),
                status
        );
    }

    /**
     * Retires the instance: marks it DRAINING, signals workers to stop after
     * the queue is drained, and shuts down the underlying executor service.
     * <p>
     * After this call {@link #submit(Request)} rejects new requests, but
     * requests already in the queue are processed to completion — workers
     * are <em>not</em> interrupted mid-flight. Once the queue is drained,
     * workers exit and {@link #isTerminated()} starts returning {@code true}.
     * <p>
     * Idempotent — repeated calls have no effect.
     */
    public void retire() {
        status = InstanceStatus.DRAINING;
        stopRequested = true;
        workerPool.shutdown();
    }

    /**
     * Returns whether the worker pool has fully terminated (all workers exited).
     *
     * @return {@code true} after shutdown completed and all in-flight tasks finished
     */
    public boolean isTerminated() {

        return workerPool.isTerminated();
    }

    /**
     * Returns the current lifecycle status (ACTIVE or DRAINING).
     *
     * @return current status
     */
    public InstanceStatus getStatus() {

        return status;
    }

    /**
     * Worker loop: blocks on the queue until a request arrives or the timeout
     * elapses. When the timeout elapses and {@code stopRequested} is set,
     * the worker exits gracefully (the queue is empty at that moment, since
     * a non-empty queue would have made {@code poll} return a request).
     */
    private void runWorker() {
        while (true) {
            Request request;
            try {
                request = queue.poll(POLL_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (request != null) {
                processRequest(request);
            } else if (stopRequested) {
                return;
            }
        }
    }

    /**
     * Simulates processing of a single request: marks it PROCESSING,
     * sleeps for its service time, marks it COMPLETED, and reports latency.
     * The {@code activeWorkers} counter is bumped for the duration of the call.
     */
    private void processRequest(Request request) {
        activeWorkers.incrementAndGet();
        long startMs = System.currentTimeMillis();
        try {
            request.markProcessing();
            try {
                Thread.sleep(request.getServiceTimeMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            request.markCompleted();
            long latencyMs = System.currentTimeMillis() - startMs;
            latencyTracker.record(latencyMs);
            processedCount.incrementAndGet();
        } finally {
            activeWorkers.decrementAndGet();
        }
    }
}
