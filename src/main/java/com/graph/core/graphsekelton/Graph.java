package com.graph.core.graphsekelton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayDeque;

public class Graph implements GraphInterface {

    // ------------------------------------------------------------------ //
    //  Internal adjacency node
    // ------------------------------------------------------------------ //

    private static class Neighbor {
        int dest;
        int weight;

        Neighbor(int d, int w) {
            this.dest   = d;
            this.weight = w;
        }
    }

    // ------------------------------------------------------------------ //
    //  Fields
    // ------------------------------------------------------------------ //

    private final int totalVerts;
    private final List<List<Neighbor>> adj;

    boolean debug_mode = false;
    int last_op_id = 0;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public Graph(int totalVerts) {
        if (totalVerts <= 0) {
            throw new IllegalArgumentException("totalVerts must be positive, got: " + totalVerts);
        }
        this.totalVerts = totalVerts;
        this.adj = new ArrayList<>(totalVerts);
        for (int i = 0; i < totalVerts; i++) {
            adj.add(new ArrayList<>());
        }
    }

    // ------------------------------------------------------------------ //
    //  Edge insertion
    // ------------------------------------------------------------------ //

    // use this for undirected stuff (MST graphs)
    @Override
    public void addEdge(int u, int v, int weight) {
        validateVert(u);
        validateVert(v);
        adj.get(u).add(new Neighbor(v, weight));
        adj.get(v).add(new Neighbor(u, weight));
    }

    // only for DAG building, one direction only
    @Override
    public void addDirectedEdge(int u, int v, int weight) {
        validateVert(u);
        validateVert(v);
        adj.get(u).add(new Neighbor(v, weight));
    }

    // ------------------------------------------------------------------ //
    //  Prim's MST
    // ------------------------------------------------------------------ //

    // prim grows the tree one cheap edge at a time, starting from vertex 0
    @Override
    public List<Edge> primMST() {
        int[] cheapestLink = new int[totalVerts];
        int[] cameFrom     = new int[totalVerts];
        boolean[] alreadyPicked = new boolean[totalVerts];

        Arrays.fill(cheapestLink, Integer.MAX_VALUE);
        Arrays.fill(cameFrom, -1);
        cheapestLink[0] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(totalVerts, (a, b) -> Integer.compare(a[0], b[0]));
        pq.offer(new int[]{0, 0});

        while (!pq.isEmpty()) {
            int[] top  = pq.poll();
            int   cost = top[0];
            int   u    = top[1];

            if (alreadyPicked[u]) continue;
            alreadyPicked[u] = true;

            for (Neighbor nb : adj.get(u)) {
                int v = nb.dest;
                int w = nb.weight;
                if (!alreadyPicked[v] && w < cheapestLink[v]) {
                    cheapestLink[v] = w;
                    cameFrom[v]     = u;
                    pq.offer(new int[]{w, v});
                }
            }
        }

        List<Edge> mst = new ArrayList<>(totalVerts - 1);
        for (int v = 1; v < totalVerts; v++) {
            if (cameFrom[v] != -1) {
                mst.add(new Edge(cameFrom[v], v, cheapestLink[v]));
            }
        }
        return mst;
    }

    // ------------------------------------------------------------------ //
    //  Kruskal's MST
    // ------------------------------------------------------------------ //

    // kruskal picks the globally cheapest edge that doesnt cause a cycle
    @Override
    public List<Edge> kruskalMST() {
        List<Edge> allEdges = new ArrayList<>();
        for (int u = 0; u < totalVerts; u++) {
            for (Neighbor nb : adj.get(u)) {
                if (u < nb.dest) {
                    allEdges.add(new Edge(u, nb.dest, nb.weight));
                }
            }
        }
        Collections.sort(allEdges);

        UnionFind uf  = new UnionFind(totalVerts);
        List<Edge> mst = new ArrayList<>(totalVerts - 1);

        for (Edge e : allEdges) {
            if (mst.size() == totalVerts - 1) break;
            if (uf.getRoot(e.source) != uf.getRoot(e.destination)) {
                uf.merge(e.source, e.destination);
                mst.add(e);
            }
        }
        return mst;
    }

    // ------------------------------------------------------------------ //
    //  Union-Find for Kruskal
    // ------------------------------------------------------------------ //

    private static class UnionFind {
        int[] boss;
        int[] treeRank;

        UnionFind(int n) {
            boss     = new int[n];
            treeRank = new int[n];
            for (int i = 0; i < n; i++) boss[i] = i;
        }

        int getRoot(int x) {
            if (boss[x] != x) boss[x] = getRoot(boss[x]);
            return boss[x];
        }

        void merge(int a, int b) {
            int ra = getRoot(a);
            int rb = getRoot(b);
            if (ra == rb) return;
            if (treeRank[ra] < treeRank[rb])       boss[ra] = rb;
            else if (treeRank[ra] > treeRank[rb])  boss[rb] = ra;
            else { boss[rb] = ra; treeRank[ra]++; }
        }
    }

    // ------------------------------------------------------------------ //
    //  Dijkstra's SSSP
    // ------------------------------------------------------------------ //

    // classic dijkstra, nothing fancy, just a priority queue and relaxation
    @Override
    public int[] dijkstra(int source) {
        validateVert(source);

        int[]     bestDist = new int[totalVerts];
        boolean[] locked   = new boolean[totalVerts];

        Arrays.fill(bestDist, Integer.MAX_VALUE);
        bestDist[source] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(totalVerts, (a, b) -> Integer.compare(a[0], b[0]));
        pq.offer(new int[]{0, source});

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int   u   = top[1];

            if (locked[u]) continue;
            locked[u] = true;

            for (Neighbor nb : adj.get(u)) {
                int v = nb.dest;
                if (!locked[v] && bestDist[u] != Integer.MAX_VALUE) {
                    long candidate = (long) bestDist[u] + nb.weight;
                    if (candidate < bestDist[v]) {
                        bestDist[v] = (int) candidate;
                        pq.offer(new int[]{bestDist[v], v});
                    }
                }
            }
        }
        return bestDist;
    }

    // ------------------------------------------------------------------ //
    //  DAG Shortest Path
    // ------------------------------------------------------------------ //

    // linear time because we process in topo order, no pq needed
    @Override
    public int[] dagShortestPath(int source) {
        validateVert(source);

        int[] topoOrder = kahnsTopoSort();

        int[] dist = new int[totalVerts];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        for (int u : topoOrder) {
            if (dist[u] == Integer.MAX_VALUE) continue;
            for (Neighbor nb : adj.get(u)) {
                long candidate = (long) dist[u] + nb.weight;
                if (candidate < dist[nb.dest]) {
                    dist[nb.dest] = (int) candidate;
                }
            }
        }
        return dist;
    }

    // ------------------------------------------------------------------ //
    //  Kahn's topological sort
    // ------------------------------------------------------------------ //

    // kahns bfs topo sort, also doubles as cycle detector which is neat
    private int[] kahnsTopoSort() {
        int[] incomingCount = new int[totalVerts];
        for (int u = 0; u < totalVerts; u++) {
            for (Neighbor nb : adj.get(u)) {
                incomingCount[nb.dest]++;
            }
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < totalVerts; v++) {
            if (incomingCount[v] == 0) queue.offer(v);
        }

        int[] processingOrder = new int[totalVerts];
        int   filled          = 0;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            processingOrder[filled++] = u;
            for (Neighbor nb : adj.get(u)) {
                if (--incomingCount[nb.dest] == 0) {
                    queue.offer(nb.dest);
                }
            }
        }

        if (filled != totalVerts) {
            throw new IllegalStateException("cycle found, this isnt a DAG");
        }
        return processingOrder;
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    // sanity check so we dont get weird array index errors
    private void validateVert(int v) {
        if (v < 0 || v >= totalVerts) {
            throw new IllegalArgumentException(
                "vertex " + v + " is out of range [0, " + (totalVerts - 1) + "]");
        }
    }

    public int getTotalVerts() { return totalVerts; }

    @Override
    public String toString() {
        return "Graph{totalVerts=" + totalVerts + "}";
    }
}
