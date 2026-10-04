package cz.arouskova.autoscaler;

import cz.arouskova.autoscaler.core.Snapshot;
import cz.arouskova.autoscaler.instance.InstanceSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure mapping functions that translate immutable {@link Snapshot} instances
 * produced by the simulation engine into UI-friendly DTOs consumed by the
 * dashboard ({@link ChartData} for the status bar, {@link InstanceRow} for
 * the instances table).
 */
public class UiMapper {

    /**
     * Extracts scalar metrics from a snapshot into a chart DTO.
     *
     * @param s engine snapshot; must not be null
     * @return chart DTO carrying latency, throughput and active instance count
     */
    public ChartData toChartData(Snapshot s) {
        return new ChartData(s.avgLatency(), s.throughput(), s.activeInstanceCount());
    }

    /**
     * Maps the snapshot's per-instance views into table rows for the instances table.
     *
     * @param s engine snapshot; must not be null
     * @return one row per instance (ACTIVE and DRAINING), in snapshot order
     */
    public List<InstanceRow> toInstanceRows(Snapshot s) {
        List<InstanceRow> rows = new ArrayList<>(s.instances().size());
        for (InstanceSnapshot is : s.instances()) {
            rows.add(new InstanceRow(
                    is.id,
                    is.queueLength,
                    is.activeWorkers,
                    is.processedCount,
                    is.droppedCount,
                    is.status));
        }
        return rows;
    }
}
