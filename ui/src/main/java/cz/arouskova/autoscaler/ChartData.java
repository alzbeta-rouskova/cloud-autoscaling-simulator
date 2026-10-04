package cz.arouskova.autoscaler;

/**
 * UI DTO carrying the scalar metrics shown in the dashboard status bar and
 * derived from a single snapshot. Time-series data for the charts is read
 * directly from {@link cz.arouskova.autoscaler.core.Snapshot} histories and is
 * not duplicated here.
 */
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
