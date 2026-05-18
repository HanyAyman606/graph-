package com.trees.ui.benchmark;

import com.trees.benchmark.BenchmarkResult;
import com.trees.benchmark.SortingAlgorithmResult;
import com.trees.benchmark.TreeSortResult;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

public class ChartBuilder{    public static BarChart<String,Number>buildTimeChart(List<BenchmarkResult>results,String operation)
    
    {
        CategoryAxis xAxis=new CategoryAxis();
       
        NumberAxis yAxis=new NumberAxis();
        
        yAxis.setLabel("Time (ms)");
        
        BarChart<String,Number> chart = new BarChart<>(xAxis, yAxis);
       
        chart.setTitle(operation +"Performance");

        XYChart.Series<String,Number> bstSeries=new XYChart.Series<>();
       
        bstSeries.setName("BST");
        
        XYChart.Series<String,Number> rbtSeries=new XYChart.Series<>();
       
        rbtSeries.setName("RBT");

        for (BenchmarkResult r:results) {
            if (r.getBenchmarkType().name().equals(operation)) {
                String method = r.getGenerationMethod().name().replace("NEARLY_SORTED", "NS");
                bstSeries.getData().add(new XYChart.Data<>(method,r.getBstMeanMs()));
                
                
                rbtSeries.getData().add(new XYChart.Data<>(method,r.getRbtMeanMs()));
            }
        }

        chart.getData().addAll(bstSeries, rbtSeries);
        
        return chart;
    }

    public static BarChart<String,Number>buildHeightChart(List<BenchmarkResult> results)
    
    {
        CategoryAxis xAxis=new CategoryAxis();
        NumberAxis yAxis =new NumberAxis();
       
        yAxis.setLabel("Final Tree Height");
        
       
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
       
        chart.setTitle("Tree Height Degradation");

        XYChart.Series<String, Number> bstSeries = new XYChart.Series<>();
       
        bstSeries.setName("BST");
        
        XYChart.Series<String, Number> rbtSeries = new XYChart.Series<>();
       
        rbtSeries.setName("RBT");

        for (BenchmarkResult r : results) 
            {
       
       
     if (r.getBenchmarkType().name().equals("INSERT")) 
        {
                String method = r.getGenerationMethod().name().replace("NEARLY_SORTED", "NS");
                bstSeries.getData().add(new XYChart.Data<>(method, r.getBstFinalHeight()));
                rbtSeries.getData().add(new XYChart.Data<>(method, r.getRbtFinalHeight()));
            }
        }

        chart.getData().addAll(bstSeries, rbtSeries);
        return chart;
    }

    public static BarChart<String, Number> buildSortChart(TreeSortResult sortResult) {
     
        CategoryAxis xAxis = new CategoryAxis();
     
        NumberAxis yAxis = new NumberAxis();
     
        yAxis.setLabel("Time (ms)");
     
        
        BarChart<String,Number> chart=new BarChart<>(xAxis, yAxis);
        
        chart.setTitle("Tree Sort vs Sorting Algorithms");
        
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (sortResult.getTreeSortBenchmark() != null) {
       
            series.getData().add(new XYChart.Data<>("BST",sortResult.getTreeSortBenchmark().getBstMeanMs()));
       
            series.getData().add(new XYChart.Data<>("RBT",sortResult.getTreeSortBenchmark().getRbtMeanMs()));
        }

        if (sortResult.getSortingResults() != null)
             {
            
                for (SortingAlgorithmResult r : sortResult.getSortingResults()) {
            
                    String name = r.getAlgorithmName().replace("Sort", "");
            
                    series.getData().add(new XYChart.Data<>(name, r.getMeanMs()));
            }
        }

        chart.getData().add(series);
        return chart;
    }
}