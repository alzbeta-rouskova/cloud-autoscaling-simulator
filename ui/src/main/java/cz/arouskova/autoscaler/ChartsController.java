package cz.arouskova.autoscaler;

import cz.arouskova.autoscaler.core.Snapshot;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Owns the three live charts shown in the dashboard centre: throughput,
 * average latency and active instance count. Each chart is backed by a single
 * series whose data points are replaced on every snapshot from the engine.
 */
public class ChartsController {

    private final XYChart.Series<Number, Number> throughputSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> latencySeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> instanceSeries = new XYChart.Series<>();

    private final VBox root;

    public ChartsController() {
        LineChart<Number, Number> throughputChart = buildChart("Throughput", "req/s");
        LineChart<Number, Number> latencyChart = buildChart("Average latency", "ms");
        LineChart<Number, Number> instanceChart = buildChart("Active instances", "count");

        throughputChart.getData().add(throughputSeries);
        latencyChart.getData().add(latencySeries);
        instanceChart.getData().add(instanceSeries);

        root = new VBox(8, throughputChart, latencyChart, instanceChart);
        VBox.setVgrow(throughputChart, Priority.ALWAYS);
        VBox.setVgrow(latencyChart, Priority.ALWAYS);
        VBox.setVgrow(instanceChart, Priority.ALWAYS);
    }

    /**
     * @return root node to embed in the parent layout
     */
    public Node getView() {
        return root;
    }

    /**
     * Replaces the data points of all three series with the histories carried
     * by the snapshot. The x-axis is anchored on the snapshot's current tick.
     *
     * @param snapshot current engine snapshot
     */
    public void update(Snapshot snapshot) {
        long lastTick = snapshot.tick();
        replaceSeries(throughputSeries, snapshot.throughputHistory(), lastTick);
        replaceSeries(latencySeries, snapshot.latencyHistory(), lastTick);
        replaceSeries(instanceSeries, snapshot.instanceCountHistory(), lastTick);
    }

    /**
     * Clears all chart data; called on simulation reset.
     */
    public void clear() {
        throughputSeries.getData().clear();
        latencySeries.getData().clear();
        instanceSeries.getData().clear();
    }

    private void replaceSeries(XYChart.Series<Number, Number> series, List<Double> history, long lastTick) {
        series.getData().clear();
        long t = lastTick - history.size() + 1;
        for (Double value : history) {
            series.getData().add(new XYChart.Data<>(t, value));
            t++;
        }
    }

    private static LineChart<Number, Number> buildChart(String title, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("tick");
        yAxis.setLabel(yLabel);
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(true);
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        return chart;
    }
}
