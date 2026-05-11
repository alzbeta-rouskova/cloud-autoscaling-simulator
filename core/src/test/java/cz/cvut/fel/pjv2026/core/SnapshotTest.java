package cz.cvut.fel.pjv2026.core;

import cz.cvut.fel.pjv2026.instance.InstanceSnapshot;
import cz.cvut.fel.pjv2026.instance.InstanceStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotTest {

    @Test
    void snapshot_is_immutable() {
        List<InstanceSnapshot> mutableSource = new ArrayList<>();
        mutableSource.add(sampleInstance("inst-1"));
        Snapshot snapshot = sampleSnapshot(mutableSource);

        mutableSource.add(sampleInstance("inst-2"));

        assertEquals(1, snapshot.instances().size());
    }

    @Test
    void instance_list_in_snapshot_is_unmodifiable() {
        Snapshot snapshot = sampleSnapshot(List.of(sampleInstance("inst-1")));

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.instances().add(sampleInstance("inst-2")));
    }

    private static Snapshot sampleSnapshot(List<InstanceSnapshot> instances) {
        return new Snapshot(
                0L, 0.0, 0.0, 0.0,
                0, 0.0, 0.0, 0,
                List.of(), List.of(), List.of(),
                instances
        );
    }

    private static InstanceSnapshot sampleInstance(String id) {
        return new InstanceSnapshot(id, 0, 0, 1, 0, 0, InstanceStatus.ACTIVE);
    }
}
