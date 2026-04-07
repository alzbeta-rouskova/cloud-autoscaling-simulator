package cz.cvut.fel.pjv2026.instance;

import cz.cvut.fel.pjv2026.exception.InstanceException;

import java.util.List;

public class InstanceManager {

    private final InstanceConfig config;

    public InstanceManager(InstanceConfig config) {

        this.config = config;
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
