package cz.arouskova.autoscaler.instance;

public class InstanceConfig {

    public int queueCapacity;
    public int workerCount;

    public InstanceConfig(int queueCapacity, int workerCount) {
        this.queueCapacity = queueCapacity;
        this.workerCount = workerCount;
    }
}
