package cz.arouskova.autoscaler.instance;

public class InstanceSnapshot {

    public String id;
    public int queueLength;
    public int activeWorkers;
    public int workerCount;
    public int processedCount;
    public int droppedCount;
    public InstanceStatus status;

    public InstanceSnapshot(String id, int queueLength, int activeWorkers, int workerCount, int processedCount, int droppedCount, InstanceStatus status) {
        this.id = id;
        this.queueLength = queueLength;
        this.activeWorkers = activeWorkers;
        this.workerCount = workerCount;
        this.processedCount = processedCount;
        this.droppedCount = droppedCount;
        this.status = status;
    }
}
