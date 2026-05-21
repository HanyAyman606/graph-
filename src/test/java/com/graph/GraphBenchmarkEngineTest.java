package com.graph;

import com.graph.benchmark.GraphBenchmarkEngine;
import com.graph.benchmark.GraphBenchmarkResult;
import com.graph.core.graphsekelton.GraphGenerator.Topology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphBenchmarkEngineTest {

    private static final int  N    = 200;
    private static final long SEED = 42L;
    private static final int  RUNS = 5;

    int test_counter = 0;

    private GraphBenchmarkEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GraphBenchmarkEngine();
        test_counter++;
    }

    // basic smoke test, sparse MST should just work
    @Test
    void benchmarkMST_sparse_resultNotNull() {
        assertNotNull(engine.benchmarkMST(Topology.SPARSE, N, SEED, RUNS));
        test_counter++;
    }

    @Test
    void benchmarkMST_dense_resultNotNull() {
        assertNotNull(engine.benchmarkMST(Topology.DENSE, N, SEED, RUNS));
        test_counter++;
    }

    @Test
    void benchmarkMST_complete_resultNotNull() {
        assertNotNull(engine.benchmarkMST(Topology.COMPLETE, N, SEED, RUNS));
        test_counter++;
    }

    // DAG is directed, MST doesnt apply, should throw
    @Test
    void benchmarkMST_dag_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.benchmarkMST(Topology.DAG, N, SEED, RUNS));
        test_counter++;
    }

    @Test
    void benchmarkMST_timingsPositive() {
        GraphBenchmarkResult r = engine.benchmarkMST(Topology.SPARSE, N, SEED, RUNS);
        assertTrue(r.getAlgoAMeanMs() > 0, "Prim mean time should be > 0");
        assertTrue(r.getAlgoBMeanMs() > 0, "Kruskal mean time should be > 0");
        test_counter++;
    }

    @Test
    void benchmarkMST_primAndKruskalNamesCorrect() {
        GraphBenchmarkResult r = engine.benchmarkMST(Topology.SPARSE, N, SEED, RUNS);
        assertEquals("Prim",    r.getAlgoAName());
        assertEquals("Kruskal", r.getAlgoBName());
        test_counter++;
    }

    @Test
    void benchmarkDijkstra_sparse_resultNotNull() {
        assertNotNull(engine.benchmarkDijkstra(Topology.SPARSE, N, SEED, RUNS));
        test_counter++;
    }

    @Test
    void benchmarkDijkstra_allTopologies_noException() {
        for (Topology t : Topology.values())
            assertDoesNotThrow(() -> engine.benchmarkDijkstra(t, N, SEED, RUNS),
                    "benchmarkDijkstra threw for: " + t);
        test_counter++;
    }

    @Test
    void benchmarkDAGvsDAGSP_resultNotNull() {
        assertNotNull(engine.benchmarkDAGvsDAGSP(N, SEED, RUNS));
        test_counter++;
    }

    @Test
    void benchmarkDAGvsDAGSP_algoNamesCorrect() {
        GraphBenchmarkResult r = engine.benchmarkDAGvsDAGSP(N, SEED, RUNS);
        assertEquals("Dijkstra", r.getAlgoAName());
        assertEquals("DAG-SP",   r.getAlgoBName());
        test_counter++;
    }

    @Test
    void benchmarkDAGvsDAGSP_speedupNotNaN() {
        assertFalse(Double.isNaN(engine.benchmarkDAGvsDAGSP(N, SEED, RUNS).getSpeedupBoverA()),
                "speedup came back NaN, algoBMean must have been zero");
        test_counter++;
    }

    @Test
    void runFullGraphSuite_returns8Results() {
        assertEquals(8, engine.runFullGraphSuite(N, SEED, RUNS).size(),
                "expected 8 results from full suite");
        test_counter++;
    }

    @Test
    void runFullGraphSuite_allResultsNonNull() {
        List<GraphBenchmarkResult> results = engine.runFullGraphSuite(N, SEED, RUNS);
        for (int i = 0; i < results.size(); i++)
            assertNotNull(results.get(i), "result at index " + i + " was null");
        test_counter++;
    }
}
