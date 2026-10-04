package cz.arouskova.autoscaler.metrics;

import cz.arouskova.autoscaler.core.SimulationConfig;
import cz.arouskova.autoscaler.core.Snapshot;
import cz.arouskova.autoscaler.instance.InstanceSnapshot;
import cz.arouskova.autoscaler.instance.InstanceStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsCollectorTest {

    @Test
    void empty_instance_list_yields_zero_active_count() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of());

        assertEquals(0, snapshot.activeInstanceCount());
    }

    @Test
    void counts_only_active_instances() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of(
                instance("a", 0, 0, 1, 0, 0, InstanceStatus.ACTIVE),
                instance("b", 0, 0, 1, 0, 0, InstanceStatus.ACTIVE),
                instance("c", 0, 0, 1, 0, 0, InstanceStatus.DRAINING)
        ));

        assertEquals(2, snapshot.activeInstanceCount());
    }

    @Test
    void snapshot_lists_all_instances_including_draining() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of(
                instance("a", 0, 0, 1, 0, 0, InstanceStatus.ACTIVE),
                instance("b", 0, 0, 1, 0, 0, InstanceStatus.DRAINING)
        ));

        assertEquals(2, snapshot.instances().size());
    }

    @Test
    void averages_queue_length_across_instances() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of(
                instance("a", 2, 0, 1, 0, 0, InstanceStatus.ACTIVE),
                instance("b", 4, 0, 1, 0, 0, InstanceStatus.ACTIVE),
                instance("c", 6, 0, 1, 0, 0, InstanceStatus.ACTIVE)
        ));

        assertEquals(4.0, snapshot.avgQueueLength(), 0.0001);
    }

    @Test
    void sums_dropped_count_across_instances() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of(
                instance("a", 0, 0, 1, 0, 1, InstanceStatus.ACTIVE),
                instance("b", 0, 0, 1, 0, 2, InstanceStatus.ACTIVE),
                instance("c", 0, 0, 1, 0, 3, InstanceStatus.ACTIVE)
        ));

        assertEquals(6, snapshot.droppedCount());
    }

    @Test
    void utilization_is_ratio_of_active_to_total_workers() {
        Snapshot snapshot = newCollector().buildSnapshot(0, List.of(
                instance("a", 0, 2, 4, 0, 0, InstanceStatus.ACTIVE),
                instance("b", 0, 1, 2, 0, 0, InstanceStatus.ACTIVE)
        ));

        assertEquals(0.5, snapshot.utilization(), 0.0001);
    }

    @Test
    void throughput_reflects_processed_delta_between_ticks() {
        MetricsCollector mc = newCollector();
        mc.buildSnapshot(0, List.of(instance("a", 0, 0, 1, 10, 0, InstanceStatus.ACTIVE)));

        Snapshot snapshot = mc.buildSnapshot(1, List.of(instance("a", 0, 0, 1, 30, 0, InstanceStatus.ACTIVE)));

        assertEquals(30.0, snapshot.throughput(), 0.0001);
    }

    @Test
    void resets_latency_tracker_per_tick() {
        MetricsCollector mc = newCollector();
        mc.latencyTracker().record(100);
        mc.buildSnapshot(0, List.of());

        Snapshot second = mc.buildSnapshot(1, List.of());

        assertEquals(0.0, second.avgLatency(), 0.0001);
    }

    private static MetricsCollector newCollector() {
        SimulationConfig config = SimulationConfig.builder()
                .tickDurationMs(100)
                .build();
        return new MetricsCollector(config);
    }

    private static InstanceSnapshot instance(String id, int queueLength, int activeWorkers,
                                             int workerCount, int processed, int dropped,
                                             InstanceStatus status) {
        return new InstanceSnapshot(id, queueLength, activeWorkers, workerCount, processed, dropped, status);
    }
}
