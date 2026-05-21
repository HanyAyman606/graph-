package com.graph.core.graphsekelton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GraphGenerator {

    // unused but keeping track mentally
    static boolean verbose_gen = false;
    static int graphs_made_so_far = 0;

    // no instances of this, its just a factory
    private GraphGenerator() {}

    // ------------------------------------------------------------------ //
    //  Topology enum
    // ------------------------------------------------------------------ //

    public enum Topology {
        SPARSE,
        DENSE,
        COMPLETE,
        DAG
    }

    // ------------------------------------------------------------------ //
    //  Public entry point
    // ------------------------------------------------------------------ //

    public static Graph generate(Topology topology, int verts, long seed) {
        return switch (topology) {
            case SPARSE   -> buildSparse(verts, seed);
            case DENSE    -> buildDense(verts, seed);
            case COMPLETE -> buildComplete(verts, seed);
            case DAG      -> buildDAG(verts, seed);
        };
    }

    // ------------------------------------------------------------------ //
    //  Sparse  — ~5V edges
    // ------------------------------------------------------------------ //

    // sparse means roughly 5V edges, spanning tree first so its connected
    private static Graph buildSparse(int v, long seed) {
        Random rng    = new Random(seed);
        Graph  g      = new Graph(v);
        int    target = 5 * v;

        // step 1 — spanning chain via shuffled order (guarantees connectivity)
        List<Integer> visitOrder = makeShuffledOrder(v, rng);
        for (int i = 1; i < v; i++) {
            g.addEdge(visitOrder.get(i - 1), visitOrder.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

        // step 2 — throw in random extra edges until we hit the target
        while (edgeCount < target) {
            int u = rng.nextInt(v);
            int w = rng.nextInt(v);
            if (u != w) {
                g.addEdge(u, w, randWeight(rng));
                edgeCount++;
            }
        }

        return g;
    }

    // ------------------------------------------------------------------ //
    //  Dense  — ~25% of all possible edges
    // ------------------------------------------------------------------ //

    // dense is 25% of all possible edges
    private static Graph buildDense(int v, long seed) {
        Random rng          = new Random(seed);
        Graph  g            = new Graph(v);
        long   maxPossible  = (long) v * (v - 1) / 2;
        int    target       = (int) (maxPossible * 0.25);

        // step 1 — spanning chain so the graph is at least connected
        List<Integer> visitOrder = makeShuffledOrder(v, rng);
        for (int i = 1; i < v; i++) {
            g.addEdge(visitOrder.get(i - 1), visitOrder.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

        // step 2 — reservoir style: iterate all pairs and accept probabilistically
        int  remaining  = target - edgeCount;
        long candidates = maxPossible - (v - 1);

        for (int u = 0; u < v && remaining > 0; u++) {
            for (int w = u + 1; w < v && remaining > 0; w++) {
                if (candidates > 0 && rng.nextDouble() <= (double) remaining / candidates) {
                    g.addEdge(u, w, randWeight(rng));
                    edgeCount++;
                    remaining--;
                }
                candidates--;
            }
        }

        return g;
    }

    // ------------------------------------------------------------------ //
    //  Complete  — every pair connected
    // ------------------------------------------------------------------ //

    // complete graph, every vertex talks to every other vertex
    private static Graph buildComplete(int v, long seed) {
        Random rng = new Random(seed);
        Graph  g   = new Graph(v);

        for (int u = 0; u < v; u++) {
            for (int w = u + 1; w < v; w++) {
                g.addEdge(u, w, randWeight(rng));
            }
        }

        return g;
    }

    // ------------------------------------------------------------------ //
    //  DAG  — directed acyclic, ~5V edges
    // ------------------------------------------------------------------ //

    // DAG trick: only add edge u->w if u comes before w in topo order, impossible to get a cycle
    private static Graph buildDAG(int v, long seed) {
        Random rng      = new Random(seed);
        Graph  g        = new Graph(v);
        int    target   = 5 * v;
        int    maxTries = target * 10;

        // assign each vertex a random position in topo order
        List<Integer> order = makeShuffledOrder(v, rng);

        // topoPos[vertex] = its position in the topo ordering
        int[] topoPos = new int[v];
        for (int i = 0; i < v; i++) {
            topoPos[order.get(i)] = i;
        }

        // step 1 — chain along topo order, guarantees reachability
        for (int i = 1; i < v; i++) {
            g.addDirectedEdge(order.get(i - 1), order.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

        // step 2 — add more forward edges until we hit target or run out of tries
        int tries = 0;
        while (edgeCount < target && tries < maxTries) {
            int u = rng.nextInt(v);
            int w = rng.nextInt(v);
            if (topoPos[u] < topoPos[w]) {
                g.addDirectedEdge(u, w, randWeight(rng));
                edgeCount++;
            }
            tries++;
        }

        return g;
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    // spec says weights in [1,1000]
    private static int randWeight(Random rng) {
        return 1 + rng.nextInt(1000);
    }

    // builds a shuffled list of 0..n-1, useful for random spanning chains
    private static List<Integer> makeShuffledOrder(int n, Random rng) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i);
        Collections.shuffle(list, rng);
        return list;
    }
}