package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.ServiceInstance;

import java.util.List;

public class LeastQueueLoadBalancer extends AbstractLoadBalancer {

    @Override
    public ServiceInstance select(List<ServiceInstance> instances) {

        throw new UnsupportedOperationException("Not implemented yet");
    }
}
