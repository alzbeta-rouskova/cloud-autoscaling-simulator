package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.metrics.LatencyTracker;
import cz.cvut.fel.pjv2026.model.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceInstance {

    private final String id;
    private final RequestQueue queue;
    private final InstanceConfig config;
    private final LatencyTracker latencyTracker;
    private final ExecutorService workerPool;
    private InstanceStatus status;

    public ServiceInstance(String id, InstanceConfig config, LatencyTracker latencyTracker) {
        this.id = id;
        this.config = config;
        this.latencyTracker = latencyTracker;
        this.queue = new RequestQueue(config.queueCapacity);
        this.workerPool = Executors.newFixedThreadPool(config.workerCount);
        this.status = InstanceStatus.ACTIVE;
    }

    public boolean submit(Request r) {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int currentQueueSize() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public InstanceSnapshot snapshot() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void shutdown() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isTerminated() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public InstanceStatus getStatus() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
