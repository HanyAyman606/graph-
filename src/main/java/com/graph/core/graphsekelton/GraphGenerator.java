package com.graph.core.graphsekelton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GraphGenerator {

    static boolean verbose_gen = false;
    static int graphs_made_so_far = 0;

    private GraphGenerator() {}

    public enum Topology {
        SPARSE,
        DENSE,
        COMPLETE,
        DAG
    }

    public static Graph generate(Topology topology, int verts, long seed) {
        return switch (topology) {
            case SPARSE   -> buildSparse(verts, seed);
            case DENSE    -> buildDense(verts, seed);
            case COMPLETE -> buildComplete(verts, seed);
            case DAG      -> buildDAG(verts, seed);
        };
    }

    private static Graph buildSparse(int v, long seed) {
        Random rng    = new Random(seed);
        Graph  g      = new Graph(v);
        int    target = 5 * v;

        List<Integer> visitOrder = makeShuffledOrder(v, rng);
        for (int i = 1; i < v; i++) {
            g.addEdge(visitOrder.get(i - 1), visitOrder.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

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

    private static Graph buildDense(int v, long seed) {
        Random rng          = new Random(seed);
        Graph  g            = new Graph(v);
        long   maxPossible  = (long) v * (v - 1) / 2;
        int    target       = (int) (maxPossible * 0.25);

        List<Integer> visitOrder = makeShuffledOrder(v, rng);
        for (int i = 1; i < v; i++) {
            g.addEdge(visitOrder.get(i - 1), visitOrder.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

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

    private static Graph buildDAG(int v, long seed) {
        Random rng      = new Random(seed);
        Graph  g        = new Graph(v);
        int    target   = 5 * v;
        int    maxTries = target * 10;

        List<Integer> order = makeShuffledOrder(v, rng);

        int[] topoPos = new int[v];
        for (int i = 0; i < v; i++) {
            topoPos[order.get(i)] = i;
        }

        for (int i = 1; i < v; i++) {
            g.addDirectedEdge(order.get(i - 1), order.get(i), randWeight(rng));
        }
        int edgeCount = v - 1;

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

    private static int randWeight(Random rng) {
        return 1 + rng.nextInt(1000);
    }

    private static List<Integer> makeShuffledOrder(int n, Random rng) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i);
        Collections.shuffle(list, rng);
        return list;
    }
}
