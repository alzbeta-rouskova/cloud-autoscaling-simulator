package cz.cvut.fel.pjv2026;

public class ChartData {

    public double latency;
    public double throughput;
    public int instanceCount;

    public ChartData(double latency, double throughput, int instanceCount) {
        this.latency = latency;
        this.throughput = throughput;
        this.instanceCount = instanceCount;
    }
}
