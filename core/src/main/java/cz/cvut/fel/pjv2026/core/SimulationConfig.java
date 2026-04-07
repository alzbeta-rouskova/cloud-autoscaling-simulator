package cz.cvut.fel.pjv2026.core;

import cz.cvut.fel.pjv2026.lb.LoadBalancerType;

public class SimulationConfig {

    public int trafficRate;
    public TrafficProfileType trafficProfile;
    public double burstMultiplier;
    public int burstIntervalTicks;
    public int initialInstanceCount;
    public int minInstanceCount;
    public int maxInstanceCount;
    public int queueCapacity;
    public int workerCount;
    public long serviceTimeMs;
    public LoadBalancerType lbStrategy;
    public boolean autoscalerEnabled;
    public int scaleUpQueueThreshold;
    public int scaleDownQueueThreshold;
    public int cooldownTicks;
    public int autoscalerEvaluationIntervalTicks;
    public int tickDurationMs;
}
