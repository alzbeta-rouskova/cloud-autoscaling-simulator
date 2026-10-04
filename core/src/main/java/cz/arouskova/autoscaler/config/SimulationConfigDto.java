package cz.arouskova.autoscaler.config;

/**
 * Data Transfer Object (DTO) for simulation configuration parameters.
 * This class encapsulates all the configurable parameters for the simulation,
 * allowing for easy serialization and deserialization when saving/loading configurations.
 * Populated by {@link ConfigLoader} via the no-arg constructor and setters,
 * validated by {@link ConfigValidator}, then mapped to
 * {@link cz.arouskova.autoscaler.core.SimulationConfig}.
 * <p>
 * Field defaults match {@code SimulationConfig.Builder} defaults so missing
 * JSON properties fall back to sensible values.
 */

public class SimulationConfigDto {

    private int trafficRate = 50;
    private String trafficProfile = "CONSTANT";
    private double burstMultiplier = 3.0;
    private int burstIntervalTicks = 20;
    private int initialInstanceCount = 2;
    private int minInstanceCount = 1;
    private int maxInstanceCount = 8;
    private int queueCapacity = 100;
    private int workerCount = 4;
    private long serviceTimeMs = 20L;
    private String loadBalancerStrategy = "ROUND_ROBIN";
    private boolean autoscalerEnabled = false;
    private int scaleUpQueueThreshold = 8;
    private int scaleDownQueueThreshold = 2;
    private int cooldownTicks = 10;
    private int autoscalerEvaluationIntervalTicks = 5;
    private int tickDurationMs = 100;

    public int getTrafficRate() {
        return trafficRate;
    }

    public void setTrafficRate(int trafficRate) {
        this.trafficRate = trafficRate;
    }

    public String getTrafficProfile() {
        return trafficProfile;
    }

    public void setTrafficProfile(String trafficProfile) {
        this.trafficProfile = trafficProfile;
    }

    public double getBurstMultiplier() {
        return burstMultiplier;
    }

    public void setBurstMultiplier(double burstMultiplier) {
        this.burstMultiplier = burstMultiplier;
    }

    public int getBurstIntervalTicks() {
        return burstIntervalTicks;
    }

    public void setBurstIntervalTicks(int burstIntervalTicks) {
        this.burstIntervalTicks = burstIntervalTicks;
    }

    public int getInitialInstanceCount() {
        return initialInstanceCount;
    }

    public void setInitialInstanceCount(int initialInstanceCount) {
        this.initialInstanceCount = initialInstanceCount;
    }

    public int getMinInstanceCount() {
        return minInstanceCount;
    }

    public void setMinInstanceCount(int minInstanceCount) {
        this.minInstanceCount = minInstanceCount;
    }

    public int getMaxInstanceCount() {
        return maxInstanceCount;
    }

    public void setMaxInstanceCount(int maxInstanceCount) {
        this.maxInstanceCount = maxInstanceCount;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public long getServiceTimeMs() {
        return serviceTimeMs;
    }

    public void setServiceTimeMs(long serviceTimeMs) {
        this.serviceTimeMs = serviceTimeMs;
    }

    public String getLoadBalancerStrategy() {
        return loadBalancerStrategy;
    }

    public void setLoadBalancerStrategy(String loadBalancerStrategy) {
        this.loadBalancerStrategy = loadBalancerStrategy;
    }

    public boolean isAutoscalerEnabled() {
        return autoscalerEnabled;
    }

    public void setAutoscalerEnabled(boolean autoscalerEnabled) {
        this.autoscalerEnabled = autoscalerEnabled;
    }

    public int getScaleUpQueueThreshold() {
        return scaleUpQueueThreshold;
    }

    public void setScaleUpQueueThreshold(int scaleUpQueueThreshold) {
        this.scaleUpQueueThreshold = scaleUpQueueThreshold;
    }

    public int getScaleDownQueueThreshold() {
        return scaleDownQueueThreshold;
    }

    public void setScaleDownQueueThreshold(int scaleDownQueueThreshold) {
        this.scaleDownQueueThreshold = scaleDownQueueThreshold;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public int getAutoscalerEvaluationIntervalTicks() {
        return autoscalerEvaluationIntervalTicks;
    }

    public void setAutoscalerEvaluationIntervalTicks(int autoscalerEvaluationIntervalTicks) {
        this.autoscalerEvaluationIntervalTicks = autoscalerEvaluationIntervalTicks;
    }

    public int getTickDurationMs() {
        return tickDurationMs;
    }

    public void setTickDurationMs(int tickDurationMs) {
        this.tickDurationMs = tickDurationMs;
    }
}
