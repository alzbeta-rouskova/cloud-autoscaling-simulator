package cz.cvut.fel.pjv2026;

import cz.cvut.fel.pjv2026.core.Snapshot;
import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UiMapper {

    public ChartData toChartData(Snapshot s) {
        if (s == null) {
            return new ChartData(0.0, 0.0, 0);
        }
        return new ChartData(s.avgLatency(), s.throughput(), s.activeInstanceCount());
    }

    public List<InstanceRow> toInstanceRows(Snapshot s) {
        if (s == null || s.instances() == null) {
            return new ArrayList<>();
        }
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
