package com.graph.ui.benchmark;

import com.graph.benchmark.GraphBenchmarkResult;
import com.graph.benchmark.GraphBenchmarkResult.GraphBenchmarkType;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class GraphChartBuilder {

    static int     charts_built_so_far = 0;
    static boolean dark_mode_assumed   = true;

    private GraphChartBuilder() {}

    public static BarChart<String, Number> buildMSTChart(List<GraphBenchmarkResult> results) {
        List<GraphBenchmarkResult> mstResults = results.stream()
                .filter(r -> r.getBenchType() == GraphBenchmarkType.MST)
                .collect(Collectors.toList());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("Time (ms)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("MST: Prim vs Kruskal");
        chart.setAnimated(false);
        chart.setPrefHeight(320);

        if (mstResults.isEmpty()) return chart;

        XYChart.Series<String, Number> seriesA = new XYChart.Series<>();
        seriesA.setName(mstResults.get(0).getAlgoAName());

        XYChart.Series<String, Number> seriesB = new XYChart.Series<>();
        seriesB.setName(mstResults.get(0).getAlgoBName());

        for (GraphBenchmarkResult r : mstResults) {
            String label = r.getTopology().name();
            seriesA.getData().add(new XYChart.Data<>(label, r.getAlgoAMeanMs()));
            seriesB.getData().add(new XYChart.Data<>(label, r.getAlgoBMeanMs()));
        }

        chart.getData().addAll(seriesA, seriesB);
        charts_built_so_far++;
        return chart;
    }

    public static BarChart<String, Number> buildDijkstraDensityChart(List<GraphBenchmarkResult> results) {
        List<GraphBenchmarkResult> ssspResults = results.stream()
                .filter(r -> r.getBenchType() == GraphBenchmarkType.SSSP_GENERAL)
                .collect(Collectors.toList());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("Time (ms)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Dijkstra SSSP across Graph Densities");
        chart.setAnimated(false);
        chart.setPrefHeight(320);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dijkstra");

        for (GraphBenchmarkResult r : ssspResults) {
            series.getData().add(new XYChart.Data<>(r.getTopology().name(), r.getAlgoAMeanMs()));
        }

        chart.getData().add(series);
        charts_built_so_far++;
        return chart;
    }

    public static VBox buildDAGSpeedupChart(List<GraphBenchmarkResult> results) {
        List<GraphBenchmarkResult> dagResults = results.stream()
                .filter(r -> r.getBenchType() == GraphBenchmarkType.SSSP_DAG)
                .collect(Collectors.toList());

        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis   yAxis1 = new NumberAxis();
        yAxis1.setLabel("Time (ms)");

        BarChart<String, Number> timeChart = new BarChart<>(xAxis1, yAxis1);
        timeChart.setTitle("DAG: Dijkstra vs DAG Shortest Path");
        timeChart.setAnimated(false);
        timeChart.setPrefHeight(320);

        XYChart.Series<String, Number> timeSeries = new XYChart.Series<>();
        timeSeries.setName("Time (ms)");

        if (!dagResults.isEmpty()) {
            GraphBenchmarkResult r = dagResults.get(0);
            timeSeries.getData().add(new XYChart.Data<>("Dijkstra", r.getAlgoAMeanMs()));
            timeSeries.getData().add(new XYChart.Data<>("DAG-SP",   r.getAlgoBMeanMs()));
        }
        timeChart.getData().add(timeSeries);

        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis   yAxis2 = new NumberAxis();
        yAxis2.setLabel("Multiplier (x)");

        BarChart<String, Number> speedupChart = new BarChart<>(xAxis2, yAxis2);
        speedupChart.setTitle("Speedup of DAG-SP over Dijkstra");
        speedupChart.setAnimated(false);
        speedupChart.setPrefHeight(320);

        XYChart.Series<String, Number> speedupSeries = new XYChart.Series<>();
        speedupSeries.setName("Speedup");

        if (!dagResults.isEmpty()) {
            double sp = dagResults.get(0).getSpeedupBoverA();
            speedupSeries.getData().add(new XYChart.Data<>("Speedup", Double.isNaN(sp) ? 0.0 : sp));
        }
        speedupChart.getData().add(speedupSeries);

        charts_built_so_far++;

        VBox box = new VBox(16, timeChart, speedupChart);
        box.setFillWidth(true);
        return box;
    }
}
