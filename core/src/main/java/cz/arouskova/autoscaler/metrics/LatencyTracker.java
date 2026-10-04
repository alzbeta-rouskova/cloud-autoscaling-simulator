package cz.arouskova.autoscaler.metrics;

public class LatencyTracker {

    private long sum;
    private long count;

    public synchronized void record(long latencyMs) {
        sum += latencyMs;
        count++;
    }

    public synchronized double average() {
        if (count == 0) {
            return 0.0;
        }

        return (double) sum / count;
    }

    public synchronized void reset() {
        sum = 0;
        count = 0;
    }
}
