package com.graph;

import com.graph.core.graphsekelton.Edge;
import com.graph.core.graphsekelton.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    private Graph undirected;
    private Graph dag;
    int test_run_counter = 0;

    @BeforeEach
    void setUp() {
        undirected = new Graph(5);
        undirected.addEdge(0, 1, 1);
        undirected.addEdge(0, 2, 4);
        undirected.addEdge(1, 2, 2);
        undirected.addEdge(1, 3, 4);
        undirected.addEdge(2, 4, 3);
        undirected.addEdge(3, 4, 5);

        dag = new Graph(4);
        dag.addDirectedEdge(0, 1, 5);
        dag.addDirectedEdge(0, 2, 2);
        dag.addDirectedEdge(1, 3, 3);
        dag.addDirectedEdge(2, 3, 4);

        test_run_counter++;
    }

    // should be fine with any positive vertex count
    @Test
    void constructor_validSize_noException() {
        assertDoesNotThrow(() -> new Graph(1));
        assertDoesNotThrow(() -> new Graph(5000));
    }

    @Test
    void constructor_invalidSize_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Graph(0));
        assertThrows(IllegalArgumentException.class, () -> new Graph(-1));
    }

    @Test
    void addEdge_invalidVertex_throws() {
        Graph g = new Graph(3);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(-1, 1, 5));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 3, 5));
    }

    // mst on V vertices always has exactly V-1 edges
    @Test
    void primMST_returnsVMinus1Edges() {
        assertEquals(4, undirected.primMST().size());
    }

    @Test
    void primMST_totalWeightIsOptimal() {
        int total = undirected.primMST().stream().mapToInt(e -> e.weight).sum();
        assertEquals(10, total);
    }

    @Test
    void primMST_singleVertex_returnsEmpty() {
        assertTrue(new Graph(1).primMST().isEmpty());
    }

    @Test
    void kruskalMST_returnsVMinus1Edges() {
        assertEquals(4, undirected.kruskalMST().size());
    }

    @Test
    void kruskalMST_totalWeightIsOptimal() {
        int total = undirected.kruskalMST().stream().mapToInt(e -> e.weight).sum();
        assertEquals(10, total);
    }

    // prim and kruskal should always agree on MST weight
    @Test
    void primAndKruskal_agreeTotalWeight() {
        int p = undirected.primMST().stream().mapToInt(e -> e.weight).sum();
        int k = undirected.kruskalMST().stream().mapToInt(e -> e.weight).sum();
        assertEquals(p, k);
    }

    @Test
    void primAndKruskal_agreeLargerGraph() {
        Graph g = new Graph(10);
        java.util.Random rng = new java.util.Random(42L);
        for (int u = 0; u < 10; u++)
            for (int v = u + 1; v < 10; v++)
                g.addEdge(u, v, rng.nextInt(100) + 1);
        int p = g.primMST().stream().mapToInt(e -> e.weight).sum();
        int k = g.kruskalMST().stream().mapToInt(e -> e.weight).sum();
        assertEquals(p, k, "prim and kruskal MST weights differ on the same graph");
    }

    @Test
    void dijkstra_sourceDistanceIsZero() {
        assertEquals(0, undirected.dijkstra(0)[0]);
    }

    // hand-verified: 0->1=1, 0->2=3, 0->3=5, 0->4=6
    @Test
    void dijkstra_correctDistances_smallGraph() {
        int[] dist = undirected.dijkstra(0);
        assertEquals(0, dist[0]); assertEquals(1, dist[1]);
        assertEquals(3, dist[2]); assertEquals(5, dist[3]); assertEquals(6, dist[4]);
    }

    @Test
    void dijkstra_unreachableVertex_isMaxValue() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1);
        assertEquals(Integer.MAX_VALUE, g.dijkstra(0)[2]);
    }

    @Test
    void dijkstra_invalidSource_throws() {
        assertThrows(IllegalArgumentException.class, () -> undirected.dijkstra(-1));
        assertThrows(IllegalArgumentException.class, () -> undirected.dijkstra(5));
    }

    @Test
    void dijkstra_symmetry_undirectedGraph() {
        assertEquals(undirected.dijkstra(0)[1], undirected.dijkstra(1)[0]);
    }

    // hand verified: 0=0, 1=5, 2=2, 3=min(5+3,2+4)=6
    @Test
    void dagShortestPath_correctDistances() {
        int[] dist = dag.dagShortestPath(0);
        assertEquals(0, dist[0]); assertEquals(5, dist[1]);
        assertEquals(2, dist[2]); assertEquals(6, dist[3]);
    }

    @Test
    void dagShortestPath_cycleDetected_throws() {
        Graph cyclic = new Graph(3);
        cyclic.addDirectedEdge(0, 1, 1);
        cyclic.addDirectedEdge(1, 2, 1);
        cyclic.addDirectedEdge(2, 0, 1);
        assertThrows(IllegalStateException.class, () -> cyclic.dagShortestPath(0));
    }

    @Test
    void dagShortestPath_agreesWithDijkstra_onDAG() {
        int[] dagDist  = dag.dagShortestPath(0);
        int[] dijkDist = dag.dijkstra(0);
        for (int v = 0; v < 4; v++)
            assertEquals(dijkDist[v], dagDist[v], "disagreement at vertex " + v);
    }

    @Test
    void edge_compareTo_ordersByWeight() {
        Edge light = new Edge(0, 1, 2);
        Edge heavy = new Edge(0, 2, 9);
        assertTrue(light.compareTo(heavy) < 0);
        assertTrue(heavy.compareTo(light) > 0);
        assertEquals(0, light.compareTo(new Edge(1, 3, 2)));
    }
}
