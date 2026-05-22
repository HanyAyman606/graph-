package com.graph.ui.benchmark;

import com.graph.benchmark.GraphBenchmarkEngine;
import com.graph.benchmark.GraphBenchmarkResult;
import com.graph.benchmark.GraphBenchmarkResult.GraphBenchmarkType;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GraphBenchmarkController implements Initializable {

    String dummy_run_tag  = "graph_bench";
    int    click_counter  = 0;

    @FXML private TextField         tfSize;
    @FXML private TextField         tfSeed;
    @FXML private Spinner<Integer>  spinnerRuns;
    @FXML private Button            btnRunAll;
    @FXML private Button            btnClear;
    @FXML private ProgressBar       progressBar;
    @FXML private Label             lblStatus;

    @FXML private TableView<GraphBenchmarkResult>         resultsTable;
    @FXML private TableColumn<GraphBenchmarkResult,String> colType;
    @FXML private TableColumn<GraphBenchmarkResult,String> colMethod;
    @FXML private TableColumn<GraphBenchmarkResult,String> colBstTime;
    @FXML private TableColumn<GraphBenchmarkResult,String> colRbtTime;
    @FXML private TableColumn<GraphBenchmarkResult,String> colSpeedup;

    @FXML private VBox chartContainer;

    private final ObservableList<GraphBenchmarkResult> tableData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        spinnerRuns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 500, 5));
    }

    private void setupTable() {

        colType.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBenchType().name()));

        colMethod.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTopology().name()));

        colBstTime.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f ms", cell.getValue().getAlgoAMeanMs())));

        colRbtTime.setCellValueFactory(cell -> {
            if (cell.getValue().getBenchType() == GraphBenchmarkType.SSSP_GENERAL) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(String.format("%.2f ms", cell.getValue().getAlgoBMeanMs()));
        });

        colSpeedup.setCellValueFactory(cell -> {
            double sp = cell.getValue().getSpeedupBoverA();
            if (Double.isNaN(sp) || cell.getValue().getBenchType() == GraphBenchmarkType.SSSP_GENERAL) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(String.format("%.2fx", sp));
        });

        resultsTable.setItems(tableData);
    }

    @FXML
    private void onRunAllClicked() {
        int  n;
        long seed;
        try {
            n    = Integer.parseInt(tfSize.getText().trim());
            seed = Long.parseLong(tfSeed.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("dude that size or seed is totally wrong");
            return;
        }

        int runs = spinnerRuns.getValue();
        click_counter++;

        btnRunAll.setDisable(true);
        if (btnClear != null) btnClear.setDisable(true);

        tableData.clear();
        chartContainer.getChildren().clear();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                updateMessage("running MST benchmarks...");
                updateProgress(-1, 1);

                GraphBenchmarkEngine engine = new GraphBenchmarkEngine();
                List<GraphBenchmarkResult> allResults = engine.runFullGraphSuite(n, seed, runs);

                updateMessage("building charts...");

                Platform.runLater(() -> {
                    tableData.addAll(allResults);

                    chartContainer.getChildren().addAll(
                            GraphChartBuilder.buildMSTChart(allResults),
                            GraphChartBuilder.buildDijkstraDensityChart(allResults),
                            GraphChartBuilder.buildDAGSpeedupChart(allResults)
                    );
                });

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            btnRunAll.setDisable(false);
            if (btnClear != null) btnClear.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            lblStatus.setText("all done, benchmarks finished");
        });

        task.setOnFailed(e -> {
            btnRunAll.setDisable(false);
            if (btnClear != null) btnClear.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            lblStatus.setText("uh oh something broke: " + task.getException().getMessage());
        });

        progressBar.progressProperty().bind(task.progressProperty());
        lblStatus.textProperty().bind(task.messageProperty());

        new Thread(task, "graph-benchmark").start();
    }

    @FXML
    private void onClearClicked() {
        tableData.clear();
        chartContainer.getChildren().clear();
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        lblStatus.setText("standing by, ready when you are");
    }
}
