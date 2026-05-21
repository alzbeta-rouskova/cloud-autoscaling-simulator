package cz.cvut.fel.pjv2026;

import cz.cvut.fel.pjv2026.instance.InstanceStatus;

/**
 * UI DTO representing one row in the instances table, built from a single
 * {@link cz.cvut.fel.pjv2026.instance.InstanceSnapshot} by {@link UiMapper}.
 * Both ACTIVE and DRAINING instances are shown; the {@link #status} column
 * distinguishes them.
 */
public class InstanceRow {

    public String id;
    public int queueLength;
    public int activeWorkers;
    public int processedCount;
    public int droppedCount;
    public InstanceStatus status;

    public InstanceRow(String id, int queueLength, int activeWorkers, int processedCount,
                       int droppedCount, InstanceStatus status) {
        this.id = id;
        this.queueLength = queueLength;
        this.activeWorkers = activeWorkers;
        this.processedCount = processedCount;
        this.droppedCount = droppedCount;
        this.status = status;
    }
}
