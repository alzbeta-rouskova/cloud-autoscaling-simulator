package cz.cvut.fel.pjv2026.lb;

import cz.cvut.fel.pjv2026.instance.ServiceInstance;

import java.util.List;

public interface LoadBalancer {

    ServiceInstance select(List<ServiceInstance> instances);
}
