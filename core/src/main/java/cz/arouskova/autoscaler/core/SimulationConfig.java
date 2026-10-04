package cz.arouskova.autoscaler.core;

import cz.arouskova.autoscaler.lb.LoadBalancerType;

/**
 * Immutable configuration carrying all parameters of a single simulation run.
 * Built via {@link #builder()} or derived from an existing config via
 * {@link #toBuilder()}. From M5 on, populated by {@code ConfigLoader} from
 * JSON through {@code SimulationConfigDto}; range validation lives in
 * {@code ConfigValidator}, not in this record.
 *
 * @param trafficRate                       requests generated per tick (interpretation depends on {@code trafficProfile})
 * @param trafficProfile                    traffic shape: constant rate or bursty (baseline + periodic spikes)
 * @param burstMultiplier                   multiplier applied to {@code trafficRate} during a burst (BURSTY only)
 * @param burstIntervalTicks                period between bursts in ticks (BURSTY only)
 * @param initialInstanceCount              instances provisioned by {@link SimulationEngine#start()}
 * @param minInstanceCount                  hard floor for autoscaler (ACTIVE never falls below this)
 * @param maxInstanceCount                  hard ceiling for autoscaler (ACTIVE never grows above this)
 * @param queueCapacity                     max pending requests per instance before drop
 * @param workerCount                       worker threads inside each {@code ServiceInstance}
 * @param serviceTimeMs                     fixed processing time per request in ms
 * @param lbStrategy                        load balancing strategy
 * @param autoscalerEnabled                 when {@code false}, engine wires autoscaler but never invokes it
 * @param scaleUpQueueThreshold             avg queue length above which SCALE_UP is issued
 * @param scaleDownQueueThreshold           avg queue length below which SCALE_DOWN is issued
 * @param cooldownTicks                     minimum ticks between two scaling decisions (anti-thrashing)
 * @param autoscalerEvaluationIntervalTicks how often the autoscaler policy is evaluated, in ticks
 * @param tickDurationMs                    wall-clock duration of one simulated tick, in ms
 */
public record SimulationConfig(
        int trafficRate,
        TrafficProfileType trafficProfile,
        double burstMultiplier,
        int burstIntervalTicks,
        int initialInstanceCount,
        int minInstanceCount,
        int maxInstanceCount,
        int queueCapacity,
        int workerCount,
        long serviceTimeMs,
        LoadBalancerType lbStrategy,
        boolean autoscalerEnabled,
        int scaleUpQueueThreshold,
        int scaleDownQueueThreshold,
        int cooldownTicks,
        int autoscalerEvaluationIntervalTicks,
        int tickDurationMs
) {

    /**
     * Starts a fresh builder pre-populated with sensible defaults
     * (constant 50 req/tick, 2 instances, 100ms tick, autoscaler off).
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Starts a builder pre-populated with values from this config. Useful
     * for tests that need a small change to an otherwise default config.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Mutable builder for {@link SimulationConfig}. All setters return
     * {@code this} for chaining.
     */
    public static final class Builder {
        private int trafficRate = 50;
        private TrafficProfileType trafficProfile = TrafficProfileType.CONSTANT;
        private double burstMultiplier = 3.0;
        private int burstIntervalTicks = 20;
        private int initialInstanceCount = 2;
        private int minInstanceCount = 1;
        private int maxInstanceCount = 8;
        private int queueCapacity = 100;
        private int workerCount = 4;
        private long serviceTimeMs = 20L;
        private LoadBalancerType lbStrategy = LoadBalancerType.ROUND_ROBIN;
        private boolean autoscalerEnabled = false;
        private int scaleUpQueueThreshold = 8;
        private int scaleDownQueueThreshold = 2;
        private int cooldownTicks = 10;
        private int autoscalerEvaluationIntervalTicks = 5;
        private int tickDurationMs = 100;

        private Builder() {}

        private Builder(SimulationConfig source) {
            this.trafficRate = source.trafficRate;
            this.trafficProfile = source.trafficProfile;
            this.burstMultiplier = source.burstMultiplier;
            this.burstIntervalTicks = source.burstIntervalTicks;
            this.initialInstanceCount = source.initialInstanceCount;
            this.minInstanceCount = source.minInstanceCount;
            this.maxInstanceCount = source.maxInstanceCount;
            this.queueCapacity = source.queueCapacity;
            this.workerCount = source.workerCount;
            this.serviceTimeMs = source.serviceTimeMs;
            this.lbStrategy = source.lbStrategy;
            this.autoscalerEnabled = source.autoscalerEnabled;
            this.scaleUpQueueThreshold = source.scaleUpQueueThreshold;
            this.scaleDownQueueThreshold = source.scaleDownQueueThreshold;
            this.cooldownTicks = source.cooldownTicks;
            this.autoscalerEvaluationIntervalTicks = source.autoscalerEvaluationIntervalTicks;
            this.tickDurationMs = source.tickDurationMs;
        }

        public Builder trafficRate(int v) {
            this.trafficRate = v;
            return this;
        }
        public Builder trafficProfile(TrafficProfileType v) {
            this.trafficProfile = v;
            return this;
        }
        public Builder burstMultiplier(double v) {
            this.burstMultiplier = v;
            return this;
        }
        public Builder burstIntervalTicks(int v) {
            this.burstIntervalTicks = v;
            return this;
        }
        public Builder initialInstanceCount(int v) {
            this.initialInstanceCount = v;
            return this;
        }
        public Builder minInstanceCount(int v) {
            this.minInstanceCount = v;
            return this;
        }
        public Builder maxInstanceCount(int v) {
            this.maxInstanceCount = v;
            return this;
        }
        public Builder queueCapacity(int v) {
            this.queueCapacity = v;
            return this;
        }
        public Builder workerCount(int v) {
            this.workerCount = v;
            return this;
        }
        public Builder serviceTimeMs(long v) {
            this.serviceTimeMs = v;
            return this;
        }
        public Builder lbStrategy(LoadBalancerType v) {
            this.lbStrategy = v;
            return this;
        }
        public Builder autoscalerEnabled(boolean v) {
            this.autoscalerEnabled = v;
            return this;
        }
        public Builder scaleUpQueueThreshold(int v) {
            this.scaleUpQueueThreshold = v;
            return this;
        }
        public Builder scaleDownQueueThreshold(int v) {
            this.scaleDownQueueThreshold = v;
            return this;
        }
        public Builder cooldownTicks(int v) {
            this.cooldownTicks = v;
            return this;
        }
        public Builder autoscalerEvaluationIntervalTicks(int v) {
            this.autoscalerEvaluationIntervalTicks = v;
            return this;
        }
        public Builder tickDurationMs(int v) {
            this.tickDurationMs = v;
            return this;
        }

        public SimulationConfig build() {
            return new SimulationConfig(
                    trafficRate, trafficProfile, burstMultiplier, burstIntervalTicks,
                    initialInstanceCount, minInstanceCount, maxInstanceCount,
                    queueCapacity, workerCount, serviceTimeMs, lbStrategy,
                    autoscalerEnabled, scaleUpQueueThreshold, scaleDownQueueThreshold,
                    cooldownTicks, autoscalerEvaluationIntervalTicks, tickDurationMs
            );
        }
    }
}
