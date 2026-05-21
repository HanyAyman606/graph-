package com.graph;

import com.graph.core.graphsekelton.Edge;
import com.graph.core.graphsekelton.Graph;
import com.graph.core.graphsekelton.GraphGenerator;
import com.graph.core.graphsekelton.GraphGenerator.Topology;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphGeneratorTest {

    private static final int  V    = 100;
    private static final long SEED = 42L;
    int test_run_counter = 0;

    @Test
    void generate_sameSeed_sameGraph_sparse() {
        int w1 = GraphGenerator.generate(Topology.SPARSE, V, SEED).primMST().stream().mapToInt(e -> e.weight).sum();
        int w2 = GraphGenerator.generate(Topology.SPARSE, V, SEED).primMST().stream().mapToInt(e -> e.weight).sum();
        assertEquals(w1, w2, "same seed produced different MST weight");
        test_run_counter++;
    }

    @Test
    void generate_differentSeed_likelyDifferentGraph() {
        int w1 = GraphGenerator.generate(Topology.SPARSE, V, SEED).primMST().stream().mapToInt(e -> e.weight).sum();
        int w2 = GraphGenerator.generate(Topology.SPARSE, V, SEED + 1).primMST().stream().mapToInt(e -> e.weight).sum();
        assertNotEquals(w1, w2, "different seeds gave the same MST weight");
        test_run_counter++;
    }

    @Test
    void sparse_mstHasVMinus1Edges() {
        assertEquals(V - 1, GraphGenerator.generate(Topology.SPARSE, V, SEED).primMST().size());
        test_run_counter++;
    }

    @Test
    void sparse_dijkstraReachesAllVertices() {
        int[] dist = GraphGenerator.generate(Topology.SPARSE, V, SEED).dijkstra(0);
        for (int v = 0; v < V; v++)
            assertNotEquals(Integer.MAX_VALUE, dist[v], "vertex " + v + " unreachable");
        test_run_counter++;
    }

    @Test
    void sparse_primAndKruskalAgree() {
        Graph g = GraphGenerator.generate(Topology.SPARSE, V, SEED);
        assertEquals(g.primMST().stream().mapToInt(e -> e.weight).sum(),
                     g.kruskalMST().stream().mapToInt(e -> e.weight).sum());
        test_run_counter++;
    }

    @Test
    void dense_mstHasVMinus1Edges() {
        assertEquals(V - 1, GraphGenerator.generate(Topology.DENSE, V, SEED).primMST().size());
        test_run_counter++;
    }

    @Test
    void dense_dijkstraReachesAllVertices() {
        int[] dist = GraphGenerator.generate(Topology.DENSE, V, SEED).dijkstra(0);
        for (int v = 0; v < V; v++)
            assertNotEquals(Integer.MAX_VALUE, dist[v], "vertex " + v + " unreachable in dense");
        test_run_counter++;
    }

    @Test
    void dense_primAndKruskalAgree() {
        Graph g = GraphGenerator.generate(Topology.DENSE, V, SEED);
        assertEquals(g.primMST().stream().mapToInt(e -> e.weight).sum(),
                     g.kruskalMST().stream().mapToInt(e -> e.weight).sum());
        test_run_counter++;
    }

    @Test
    void complete_mstHasVMinus1Edges() {
        assertEquals(V - 1, GraphGenerator.generate(Topology.COMPLETE, V, SEED).primMST().size());
        test_run_counter++;
    }

    @Test
    void complete_primAndKruskalAgree() {
        Graph g = GraphGenerator.generate(Topology.COMPLETE, V, SEED);
        assertEquals(g.primMST().stream().mapToInt(e -> e.weight).sum(),
                     g.kruskalMST().stream().mapToInt(e -> e.weight).sum());
        test_run_counter++;
    }

    @Test
    void dag_noExceptionOnDagShortestPath() {
        assertDoesNotThrow(() -> GraphGenerator.generate(Topology.DAG, V, SEED).dagShortestPath(0));
        test_run_counter++;
    }

    @Test
    void dag_noCycleDetected() {
        assertDoesNotThrow(() -> GraphGenerator.generate(Topology.DAG, V, SEED).dagShortestPath(0),
                "DAG generator produced a cyclic graph");
        test_run_counter++;
    }

    @Test
    void dag_sourceDistanceIsZero() {
        assertEquals(0, GraphGenerator.generate(Topology.DAG, V, SEED).dagShortestPath(0)[0]);
        test_run_counter++;
    }

    @Test
    void dag_dagSpAndDijkstraAgreeOnReachableVertices() {
        Graph g       = GraphGenerator.generate(Topology.DAG, V, SEED);
        int[] dagDist = g.dagShortestPath(0);
        int[] dijDist = g.dijkstra(0);
        for (int v = 0; v < V; v++)
            assertEquals(dijDist[v], dagDist[v], "disagree at vertex " + v);
        test_run_counter++;
    }

    @Test
    void generate_allTopologies_noException() {
        for (Topology t : Topology.values()) {
            assertDoesNotThrow(() -> GraphGenerator.generate(t, V, SEED),
                    "exception for topology: " + t);
            test_run_counter++;
        }
    }

    @Test
    void generate_allTopologies_mstSizeCorrect() {
        for (Topology t : Topology.values()) {
            if (t == Topology.DAG) continue;
            List<Edge> mst = GraphGenerator.generate(t, V, SEED).primMST();
            assertEquals(V - 1, mst.size(), "wrong MST size for topology: " + t);
            test_run_counter++;
        }
    }
}
