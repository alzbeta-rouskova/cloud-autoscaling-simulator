package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.exception.InstanceException;
import cz.cvut.fel.pjv2026.metrics.LatencyTracker;

import java.util.List;

public class InstanceManager {

    private final InstanceConfig config;
    private final LatencyTracker latencyTracker;

    public InstanceManager(InstanceConfig config, LatencyTracker latencyTracker) {

        this.config = config;
        this.latencyTracker = latencyTracker;
    }

    public void addInstance() {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void removeInstance(String instanceId) throws InstanceException {

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<ServiceInstance> getInstances() {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
