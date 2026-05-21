package cz.cvut.fel.pjv2026;

import cz.cvut.fel.pjv2026.core.Snapshot;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Owns the right-side instances table. Each row corresponds to one service
 * instance (ACTIVE or DRAINING) and is rebuilt from the latest snapshot on
 * every UI update. The Status column is colour-coded: green for ACTIVE,
 * orange for DRAINING.
 */
public class InstancesTableController {

    private final UiMapper uiMapper = new UiMapper();
    private final ObservableList<InstanceRow> rows = FXCollections.observableArrayList();
    private final VBox root;

    public InstancesTableController() {
        TableView<InstanceRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new javafx.scene.control.Label("No instances yet"));

        TableColumn<InstanceRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().id));

        TableColumn<InstanceRow, Number> queueCol = new TableColumn<>("Queue");
        queueCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().queueLength));

        TableColumn<InstanceRow, Number> workersCol = new TableColumn<>("Workers");
        workersCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().activeWorkers));

        TableColumn<InstanceRow, Number> processedCol = new TableColumn<>("Processed");
        processedCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().processedCount));

        TableColumn<InstanceRow, Number> droppedCol = new TableColumn<>("Dropped");
        droppedCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().droppedCount));

        TableColumn<InstanceRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "ACTIVE" -> setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                        case "DRAINING" -> setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        table.getColumns().add(idCol);
        table.getColumns().add(queueCol);
        table.getColumns().add(workersCol);
        table.getColumns().add(processedCol);
        table.getColumns().add(droppedCol);
        table.getColumns().add(statusCol);

        root = new VBox(table);
        root.setPrefWidth(420);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    /**
     * @return root node to embed in the parent layout
     */
    public Node getView() {
        return root;
    }

    /**
     * Replaces all rows with views built from the snapshot's per-instance data.
     *
     * @param snapshot current engine snapshot
     */
    public void update(Snapshot snapshot) {
        List<InstanceRow> newRows = uiMapper.toInstanceRows(snapshot);
        rows.setAll(newRows);
    }

    /**
     * Removes all rows; called on simulation reset.
     */
    public void clear() {
        rows.clear();
    }
}
