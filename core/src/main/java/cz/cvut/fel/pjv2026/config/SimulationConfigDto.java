package cz.cvut.fel.pjv2026.config;


public class SimulationConfigDto {

    public int trafficRate;
    public String trafficProfile;
    public double burstMultiplier;
    public int burstIntervalTicks;
    public int initialInstanceCount;
    public int minInstanceCount;
    public int maxInstanceCount;
    public int queueCapacity;
    public int workerCount;
    public long serviceTimeMs;
    public String loadBalancerStrategy;
    public boolean autoscalerEnabled;
    public int scaleUpQueueThreshold;
    public int scaleDownQueueThreshold;
    public int cooldownTicks;
    public int autoscalerEvaluationIntervalTicks;
    public int tickDurationMs;
}
