package com.graph;

import com.graph.core.graphsekelton.GraphGenerator.Topology;
import com.graph.session.GraphBluePrint;
import com.graph.session.GraphBluePrint.Stage;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class GraphBluePrintTest {

    private static final Topology TOPO  = Topology.SPARSE;
    private static final int      VERTS = 50;
    private static final long     SEED  = 42L;

    int test_counter = 0;

    @Test
    void constructor_validArgs_stageIsFresh() {
        assertEquals(Stage.FRESH,
                new GraphBluePrint("test-bp", TOPO, VERTS, SEED).getCurrentStage());
        test_counter++;
    }

    @Test
    void constructor_blankLabel_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphBluePrint("  ", TOPO, VERTS, SEED));
        assertThrows(IllegalArgumentException.class,
                () -> new GraphBluePrint("", TOPO, VERTS, SEED));
        test_counter++;
    }

    @Test
    void constructor_zeroVerts_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphBluePrint("bp", TOPO, 0, SEED));
        assertThrows(IllegalArgumentException.class,
                () -> new GraphBluePrint("bp", TOPO, -5, SEED));
        test_counter++;
    }

    @Test
    void buildGraph_changesStagToGraphReady() {
        GraphBluePrint bp = new GraphBluePrint("bp-ready", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertEquals(Stage.GRAPH_READY, bp.getCurrentStage());
        test_counter++;
    }

    @Test
    void buildGraph_calledTwice_throws() {
        GraphBluePrint bp = new GraphBluePrint("bp-twice", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertThrows(IllegalStateException.class, bp::buildGraph);
        test_counter++;
    }

    @Test
    void buildGraph_graphNotNullAfter() {
        GraphBluePrint bp = new GraphBluePrint("bp-notnull", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertNotNull(bp.getTheGraph());
        test_counter++;
    }

    @Test
    void hasGraph_falseBeforeBuild() {
        assertFalse(new GraphBluePrint("bp-before", TOPO, VERTS, SEED).hasGraph());
        test_counter++;
    }

    @Test
    void hasGraph_trueAfterBuild() {
        GraphBluePrint bp = new GraphBluePrint("bp-after", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertTrue(bp.hasGraph());
        test_counter++;
    }

    @Test
    void isBenchmarked_falseBeforeResults() {
        GraphBluePrint bp = new GraphBluePrint("bp-notbench", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertFalse(bp.isBenchmarked());
        test_counter++;
    }

    @Test
    void storeBenchmarkResults_changesStageToToBenchmarked() {
        GraphBluePrint bp = new GraphBluePrint("bp-bench", TOPO, VERTS, SEED);
        bp.buildGraph();
        bp.storeBenchmarkResults(Collections.emptyList());
        assertEquals(Stage.BENCHMARKED, bp.getCurrentStage());
        test_counter++;
    }

    @Test
    void storeBenchmarkResults_beforeGraphReady_throws() {
        GraphBluePrint bp = new GraphBluePrint("bp-early", TOPO, VERTS, SEED);
        assertThrows(IllegalStateException.class,
                () -> bp.storeBenchmarkResults(Collections.emptyList()));
        test_counter++;
    }

    @Test
    void getTheGraph_returnsCorrectVertCount() {
        GraphBluePrint bp = new GraphBluePrint("bp-verts", TOPO, VERTS, SEED);
        bp.buildGraph();
        assertEquals(VERTS, bp.getTheGraph().getTotalVerts());
        test_counter++;
    }

    @Test
    void toString_containsLabel() {
        assertTrue(new GraphBluePrint("my-graph", TOPO, VERTS, SEED)
                .toString().contains("my-graph"));
        test_counter++;
    }
}
