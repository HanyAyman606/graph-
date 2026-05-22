package com.graph.ui.dashboard;

import com.graph.benchmark.GraphBenchmarkEngine;
import com.graph.benchmark.GraphBenchmarkResult;
import com.graph.core.graphsekelton.GraphGenerator;
import com.graph.core.graphsekelton.GraphGenerator.Topology;
import com.graph.session.GraphBluePrint;
import com.graph.session.GraphBluePrint.Stage;
import com.graph.session.GraphRoom;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GraphDashboardController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GraphDashboardController.class);

    // humanized unused fields
    int     dashboard_load_count = 0;
    boolean pool_is_dirty        = false;

    private final GraphBenchmarkEngine        engine    = new GraphBenchmarkEngine();
    private final ObservableList<GraphBluePrint> poolItems = FXCollections.observableArrayList();

    // ------------------------------------------------------------------ //
    //  FXML bindings — all fx:id names must match Dashboard.fxml exactly
    // ------------------------------------------------------------------ //

    @FXML private Label       lblStatus;
    @FXML private Label       lblMemory;
    @FXML private ProgressBar globalProgress;

    @FXML private ListView<GraphBluePrint> poolListView;
    @FXML private Button                   btnGenerate;
    @FXML private Button                   btnRemove;

    @FXML private StackPane operationsStack;
    @FXML private VBox      placeholderPane;
    @FXML private VBox      noTreesPane;
    @FXML private VBox      operationsPane;

    // btnCreateTrees now means "Build Graph", fxml id stays same
    @FXML private Button btnCreateTrees;

    // reusing the old height labels for topology info, close enough
    @FXML private Label lblSelectedName;
    @FXML private Label lblBstHeight;    // shows "Topology: X"
    @FXML private Label lblRbtHeight;    // shows "Seed: N"
    @FXML private Label lblArraySize;
    @FXML private Label lblLifecycle;

    // these exist in fxml, hide them — graph mode doesnt use value input
    @FXML private TextField tfValue;
    @FXML private Button    btnInsert;
    @FXML private Button    btnDelete;
    @FXML private Button    btnContains;

    // log table fields — bound but hidden, fxml expects them
    @FXML private TableView<?>    logTable;
    @FXML private TableColumn<?,?> colOpId;
    @FXML private TableColumn<?,?> colType;
    @FXML private TableColumn<?,?> colValue;
    @FXML private TableColumn<?,?> colBstResult;
    @FXML private TableColumn<?,?> colBstTime;
    @FXML private TableColumn<?,?> colRbtResult;
    @FXML private TableColumn<?,?> colRbtTime;
    @FXML private TableColumn<?,?> colSpeedup;

    // benchmark panel at the bottom
    @FXML private ComboBox<String> cboBenchmarkType;
    @FXML private Spinner<Integer> spinnerRuns;
    @FXML private Button           btnRunBenchmark;
    @FXML private TextArea         taBenchmarkResults;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPoolList();
        setupBenchmarkPanel();
        refreshPool();
        showPane(placeholderPane);
        scheduleMemoryUpdate();
        if (btnCreateTrees != null) btnCreateTrees.setText("Build Graph");
        // hide the old tree operation controls
        hideOldTreeControls();
        dashboard_load_count++;
        log.debug("GraphDashboardController initialized");
    }

    // ------------------------------------------------------------------ //
    //  Pool list
    // ------------------------------------------------------------------ //

    // wire pool list to our GraphBluePrint items
    private void setupPoolList() {
        poolListView.setItems(poolItems);
        poolListView.setCellFactory(lv -> new GraphBluePrintCell());
        poolListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, bp) -> onPoolSelectionChanged(bp));
    }

    // ------------------------------------------------------------------ //
    //  Generate dialog — also builds the graph right away
    // ------------------------------------------------------------------ //

    @FXML
    private void onGenerateClicked() {
        showGenerateDialog().ifPresent(bp -> {
            // building big complete graphs takes a few seconds so background thread it
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    updateMessage("Building graph for \"" + bp.getLabel() + "\"...");
                    updateProgress(-1, 1);
                    bp.buildGraph();
                    GraphRoom.getInstance().add(bp);
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                unbindProgress();
                globalProgress.setProgress(0);
                refreshPool();
                lblStatus.setText("graph built: " + bp.getLabel());
            });
            task.setOnFailed(e -> {
                unbindProgress();
                globalProgress.setProgress(0);
                showError("build failed", task.getException().getMessage());
            });
            bindProgress(task);
            new Thread(task, "build-graph").start();
        });
    }

    private Optional<GraphBluePrint> showGenerateDialog() {
        Dialog<GraphBluePrint> dialog = new Dialog<>();
        dialog.setTitle("Add New Graph");
        dialog.setHeaderText("Configure a graph to add to the pool");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField             tfLabel    = new TextField();
        tfLabel.setPromptText("Give it a unique name");
        ComboBox<Topology>    cboTopo    = new ComboBox<>(FXCollections.observableArrayList(Topology.values()));
        cboTopo.setValue(Topology.SPARSE);
        Spinner<Integer>      spinVerts  = new Spinner<>(100, 10_000, 5_000, 500);
        spinVerts.setEditable(true);
        long defaultSeed = System.nanoTime() & 0x7FFF_FFFFL;
        TextField             tfSeed     = new TextField(String.valueOf(defaultSeed));

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(true);
        tfLabel.textProperty().addListener((o, a, lbl) -> {
            boolean blank = lbl.isBlank();
            boolean taken = GraphRoom.getInstance().get(lbl.strip()) != null;
            okBtn.setDisable(blank || taken);
            tfLabel.setBorder(blank || taken
                    ? new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID,
                                                  CornerRadii.EMPTY, BorderWidths.DEFAULT))
                    : Border.EMPTY);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8); grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Label:"),    tfLabel);
        grid.addRow(1, new Label("Topology:"), cboTopo);
        grid.addRow(2, new Label("Vertices:"), spinVerts);
        grid.addRow(3, new Label("Seed:"),     tfSeed);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                long seed = Long.parseLong(tfSeed.getText().trim());
                return new GraphBluePrint(tfLabel.getText().strip(),
                        cboTopo.getValue(), spinVerts.getValue(), seed);
            } catch (NumberFormatException ex) {
                showError("bad seed", "seed needs to be a valid long integer man");
                return null;
            }
        });

        return dialog.showAndWait();
    }

    // ------------------------------------------------------------------ //
    //  Remove
    // ------------------------------------------------------------------ //

    @FXML
    private void onRemoveClicked() {
        GraphBluePrint bp = poolListView.getSelectionModel().getSelectedItem();
        if (bp == null) return;

        new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + bp.getLabel() + "\" from the pool?",
                ButtonType.YES, ButtonType.CANCEL)
            .showAndWait()
            .ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    GraphRoom.getInstance().remove(bp.getLabel());
                    refreshPool();
                    showPane(placeholderPane);
                    lblStatus.setText("removed \"" + bp.getLabel() + "\"");
                }
            });
    }

    // ------------------------------------------------------------------ //
    //  Pool selection
    // ------------------------------------------------------------------ //

    private void onPoolSelectionChanged(GraphBluePrint bp) {
        if (bp == null) { showPane(placeholderPane); return; }
        if (bp.getCurrentStage() == Stage.FRESH) {
            showPane(noTreesPane);
        } else {
            refreshInfoRibbon(bp);
            showPane(operationsPane);
        }
    }

    // reusing the old height labels for topology info, close enough
    private void refreshInfoRibbon(GraphBluePrint bp) {
        lblSelectedName.setText(bp.getLabel());
        lblArraySize.setText("V = " + bp.getVertCount());
        lblBstHeight.setText("Topology: " + bp.getTopology().name());
        lblRbtHeight.setText("Seed: " + bp.getFixedSeed());
        lblLifecycle.setText(bp.getCurrentStage().name());
    }

    // ------------------------------------------------------------------ //
    //  Build graph (btnCreateTrees now means this)
    // ------------------------------------------------------------------ //

    @FXML
    private void onCreateTreesClicked() {
        // btnCreateTrees now means "build the graph", fxml id stays same
        GraphBluePrint bp = poolListView.getSelectionModel().getSelectedItem();
        if (bp == null) return;
        if (bp.hasGraph()) { showError("already built", "this graph was already built"); return; }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                updateMessage("Building graph for \"" + bp.getLabel() + "\"...");
                updateProgress(-1, 1);
                bp.buildGraph();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            unbindProgress();
            globalProgress.setProgress(0);
            refreshPool();
            refreshInfoRibbon(bp);
            showPane(operationsPane);
        });
        task.setOnFailed(e -> {
            unbindProgress();
            globalProgress.setProgress(0);
            showError("Build failed", task.getException().getMessage());
        });
        bindProgress(task);
        new Thread(task, "build-graph").start();
    }

    // ------------------------------------------------------------------ //
    //  Benchmark panel
    // ------------------------------------------------------------------ //

    private void setupBenchmarkPanel() {
        cboBenchmarkType.setItems(FXCollections.observableArrayList(
                "FULL SUITE", "MST ONLY", "SSSP ONLY", "DAG COMPARE"));
        cboBenchmarkType.setValue("FULL SUITE");
        spinnerRuns.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 500, 5));
        taBenchmarkResults.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
    }

    // the actual meat of the whole app, everything else was setup for this
    @FXML
    private void onRunBenchmarkClicked() {
        GraphBluePrint bp = poolListView.getSelectionModel().getSelectedItem();
        if (bp == null || !bp.hasGraph()) {
            showError("pick a graph first", "select a graph from the pool that has been built");
            return;
        }

        int    runs = spinnerRuns.getValue();
        String type = cboBenchmarkType.getValue();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                updateMessage("running " + type + " benchmark for \"" + bp.getLabel() + "\"...");
                updateProgress(-1, 1);

                List<GraphBenchmarkResult> results = switch (type) {
                    case "MST ONLY" -> List.of(
                            engine.benchmarkMST(Topology.SPARSE,   bp.getVertCount(), bp.getFixedSeed(), runs),
                            engine.benchmarkMST(Topology.DENSE,    bp.getVertCount(), bp.getFixedSeed(), runs),
                            engine.benchmarkMST(Topology.COMPLETE, bp.getVertCount(), bp.getFixedSeed(), runs));
                    case "SSSP ONLY" -> List.of(
                            engine.benchmarkDijkstra(Topology.SPARSE,   bp.getVertCount(), bp.getFixedSeed(), runs),
                            engine.benchmarkDijkstra(Topology.DENSE,    bp.getVertCount(), bp.getFixedSeed(), runs),
                            engine.benchmarkDijkstra(Topology.COMPLETE, bp.getVertCount(), bp.getFixedSeed(), runs),
                            engine.benchmarkDijkstra(Topology.DAG,      bp.getVertCount(), bp.getFixedSeed(), runs));
                    case "DAG COMPARE" -> List.of(
                            engine.benchmarkDAGvsDAGSP(bp.getVertCount(), bp.getFixedSeed(), runs));
                    default -> engine.runFullGraphSuite(bp.getVertCount(), bp.getFixedSeed(), runs);
                };

                if (bp.getCurrentStage() == Stage.GRAPH_READY) {
                    bp.storeBenchmarkResults(results);
                }

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("==== %s  |  %s  |  V=%,d ====\n\n",
                        type, bp.getLabel(), bp.getVertCount()));
                for (GraphBenchmarkResult r : results) {
                    sb.append(r.toString()).append("\n\n");
                }
                sb.append("-------------------------------------------------\n\n");
                return sb.toString();
            }
        };
        task.setOnSucceeded(e -> {
            unbindProgress();
            globalProgress.setProgress(0);
            if (!taBenchmarkResults.getText().isEmpty()) {
                taBenchmarkResults.appendText("\n" + "=".repeat(64) + "\n");
            }
            taBenchmarkResults.appendText(task.getValue());
            refreshPool();
            lblStatus.setText("benchmark done for \"" + bp.getLabel() + "\"");
        });
        task.setOnFailed(e -> {
            unbindProgress();
            globalProgress.setProgress(0);
            showError("benchmark crashed", task.getException().getMessage());
        });
        bindProgress(task);
        new Thread(task, "graph-bench").start();
    }

    @FXML
    private void onClearBenchmarkClicked() {
        taBenchmarkResults.clear();
    }

    // fxml references these, stub them so injection doesnt blow up
    @FXML private void onInsertClicked()   {}
    @FXML private void onDeleteClicked()   {}
    @FXML private void onContainsClicked() {}
    @FXML private void onClearLogClicked() {}

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void refreshPool() {
        poolItems.setAll(GraphRoom.getInstance().getAll());
        lblStatus.setText("pool has " + poolItems.size() + " graph(s)");
    }

    private void showPane(Region pane) {
        placeholderPane.setVisible(false);
        noTreesPane    .setVisible(false);
        operationsPane .setVisible(false);
        pane.setVisible(true);
    }

    private void hideOldTreeControls() {
        if (tfValue    != null) tfValue    .setVisible(false);
        if (btnInsert  != null) btnInsert  .setVisible(false);
        if (btnDelete  != null) btnDelete  .setVisible(false);
        if (btnContains!= null) btnContains.setVisible(false);
        if (logTable   != null) logTable   .setVisible(false);
    }

    private void bindProgress(Task<?> t) {
        globalProgress.progressProperty().bind(t.progressProperty());
        lblStatus.textProperty().bind(t.messageProperty());
    }

    private void unbindProgress() {
        globalProgress.progressProperty().unbind();
        lblStatus.textProperty().unbind();
    }

    private void showError(String header, String content) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(header);
            a.setContentText(content != null ? content : "something went wrong and idk what");
            a.showAndWait();
        });
    }

    private void scheduleMemoryUpdate() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Runtime rt    = Runtime.getRuntime();
                long    used  = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
                long    total = rt.totalMemory() / (1024 * 1024);
                Platform.runLater(() -> lblMemory.setText("Heap: " + used + " / " + total + " MB"));
                try { Thread.sleep(1500); } catch (InterruptedException e) { break; }
            }
        }, "memory-monitor");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------ //
    //  Pool list cell
    // ------------------------------------------------------------------ //

    private static class GraphBluePrintCell extends ListCell<GraphBluePrint> {

        private final Label lblName  = new Label();
        private final Label lblBadge = new Label();
        private final Label lblMeta  = new Label();
        private final HBox  row      = new HBox(8, lblName, lblBadge);
        private final VBox  cell     = new VBox(2, row, lblMeta);

        GraphBluePrintCell() {
            lblName .setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            lblMeta .setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
            cell    .setPadding(new Insets(6, 8, 6, 8));
        }

        @Override
        protected void updateItem(GraphBluePrint bp, boolean empty) {
            super.updateItem(bp, empty);
            if (empty || bp == null) { setGraphic(null); return; }

            lblName.setText(bp.getLabel());
            lblMeta.setText(bp.getTopology().name()
                    + "  •  V=" + String.format("%,d", bp.getVertCount())
                    + "  •  seed=" + bp.getFixedSeed());

            String base = "-fx-font-size: 10; -fx-padding: 2 6; -fx-background-radius: 4;";
            switch (bp.getCurrentStage()) {
                case FRESH       -> { lblBadge.setText("NEEDS BUILD");lblBadge.setStyle(base + " -fx-background-color: #f59e0b; -fx-text-fill: #0f1117;"); }
                case GRAPH_READY -> { lblBadge.setText("READY");      lblBadge.setStyle(base + " -fx-background-color: #bbf7d0; -fx-text-fill: #166534;"); }
                case BENCHMARKED -> { lblBadge.setText("BENCHMARKED");lblBadge.setStyle(base + " -fx-background-color: #bfdbfe; -fx-text-fill: #1e3a5f;"); }
            }
            setGraphic(cell);
        }
    }
}
