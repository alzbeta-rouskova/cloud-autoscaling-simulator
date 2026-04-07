package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.ServiceInstance;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    protected void validateNotEmpty(List<ServiceInstance> instances) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
