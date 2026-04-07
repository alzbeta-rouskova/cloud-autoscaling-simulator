package cz.cvut.fel.pjv2026.core;

import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;

import java.util.List;

public class Snapshot {

    public long tick;
    public double throughput;
    public double avgLatency;
    public double avgQueueLength;
    public int droppedCount;
    public double dropRate;
    public double utilization;
    public int instanceCount;
    public List<Double> latencyHistory;
    public List<Double> throughputHistory;
    public List<Double> instanceCountHistory;
    public List<InstanceSnapshot> instances;

    public Snapshot(long tick, double throughput, double avgLatency, double avgQueueLength, int droppedCount, double dropRate, double utilization, int instanceCount, List<Double> latencyHistory, List<Double> throughputHistory, List<Double> instanceCountHistory, List<InstanceSnapshot> instances) {
        this.tick = tick;
        this.throughput = throughput;
        this.avgLatency = avgLatency;
        this.avgQueueLength = avgQueueLength;
        this.droppedCount = droppedCount;
        this.dropRate = dropRate;
        this.utilization = utilization;
        this.instanceCount = instanceCount;
        this.latencyHistory = latencyHistory;
        this.throughputHistory = throughputHistory;
        this.instanceCountHistory = instanceCountHistory;
        this.instances = instances;
    }
}
