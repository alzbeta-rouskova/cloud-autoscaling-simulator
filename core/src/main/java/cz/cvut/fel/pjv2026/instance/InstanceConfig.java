package cz.cvut.fel.pjv2026.instance;

public class InstanceConfig {

    public int queueCapacity;
    public int workerCount;

    public InstanceConfig(int queueCapacity, int workerCount) {
        this.queueCapacity = queueCapacity;
        this.workerCount = workerCount;
    }
}
