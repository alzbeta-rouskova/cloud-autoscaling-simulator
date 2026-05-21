package cz.cvut.fel.pjv2026;

import cz.cvut.fel.pjv2026.core.Snapshot;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChartsController {

    private final LineChart<Number, Number> throughputChart;
    private final LineChart<Number, Number> latencyChart;
    private final LineChart<Number, Number> instanceChart;

    private final XYChart.Series<Number, Number> throughputSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> latencySeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> instanceSeries = new XYChart.Series<>();

    private final VBox root;

    public ChartsController() {
        throughputChart = buildChart("Throughput", "tick", "req/s");
        latencyChart = buildChart("Average latency", "tick", "ms");
        instanceChart = buildChart("Active instances", "tick", "count");

        throughputChart.getData().add(throughputSeries);
        latencyChart.getData().add(latencySeries);
        instanceChart.getData().add(instanceSeries);

        root = new VBox(8, throughputChart, latencyChart, instanceChart);
        root.setStyle("CHART_COLOR_1: #1f4e79;");
        VBox.setVgrow(throughputChart, Priority.ALWAYS);
        VBox.setVgrow(latencyChart, Priority.ALWAYS);
        VBox.setVgrow(instanceChart, Priority.ALWAYS);
    }

    public Node getView() {
        return root;
    }

    public void update(Snapshot snapshot) {
        long lastTick = snapshot.tick();
        replaceSeries(throughputSeries, snapshot.throughputHistory(), lastTick);
        replaceSeries(latencySeries, snapshot.latencyHistory(), lastTick);
        replaceSeries(instanceSeries, snapshot.instanceCountHistory(), lastTick);
    }

    public void clear() {
        throughputSeries.getData().clear();
        latencySeries.getData().clear();
        instanceSeries.getData().clear();
    }

    private void replaceSeries(XYChart.Series<Number, Number> series, List<Double> history, long lastTick) {
        series.getData().clear();
        long firstTick = lastTick - history.size() + 1;
        long t = firstTick;
        for (Double value : history) {
            series.getData().add(new XYChart.Data<>(t, value));
            t++;
        }
    }

    private static LineChart<Number, Number> buildChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
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
