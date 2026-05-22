package com.graph.core.graphsekelton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Graph implements GraphInterface {

    private final int totalVerts;

    private final int[] head;
    private int[] to;
    private int[] weight;
    private int[] next;
    private int edgeCount;

    boolean debug_mode = false;
    int last_op_id = 0;

    public Graph(int totalVerts) {
        if (totalVerts <= 0) {
            throw new IllegalArgumentException("totalVerts must be positive, got: " + totalVerts);
        }
        this.totalVerts = totalVerts;

        this.head = new int[totalVerts];
        Arrays.fill(this.head, -1);

        int initialCapacity = Math.max(1000, totalVerts * 4);
        this.to = new int[initialCapacity];
        this.weight = new int[initialCapacity];
        this.next = new int[initialCapacity];
        this.edgeCount = 0;
    }

    private void ensureCapacity() {
        if (edgeCount == to.length) {
            int newCap = to.length * 2;
            to = Arrays.copyOf(to, newCap);
            weight = Arrays.copyOf(weight, newCap);
            next = Arrays.copyOf(next, newCap);
        }
    }

    @Override
    public void addDirectedEdge(int u, int v, int w) {
        validateVert(u);
        validateVert(v);
        ensureCapacity();

        to[edgeCount] = v;
        weight[edgeCount] = w;
        next[edgeCount] = head[u];
        head[u] = edgeCount++;
    }

    @Override
    public void addEdge(int u, int v, int w) {
        addDirectedEdge(u, v, w);
        addDirectedEdge(v, u, w);
    }

    @Override
    public List<Edge> primMST() {
        int[] cheapestLink = new int[totalVerts];
        int[] cameFrom     = new int[totalVerts];
        boolean[] inMST    = new boolean[totalVerts];

        Arrays.fill(cheapestLink, Integer.MAX_VALUE);
        Arrays.fill(cameFrom, -1);
        cheapestLink[0] = 0;

        IndexedMinHeap pq = new IndexedMinHeap(totalVerts);
        pq.insert(0, 0);

        while (!pq.isEmpty()) {
            int u = pq.extractMin();
            inMST[u] = true;

            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];
                int w = weight[e];

                if (!inMST[v] && w < cheapestLink[v]) {
                    cheapestLink[v] = w;
                    cameFrom[v]     = u;
                    pq.insertOrDecrease(v, w);
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

    @Override
    public List<Edge> kruskalMST() {
        long[] edges = new long[edgeCount / 2];
        int c = 0;

        for (int u = 0; u < totalVerts; u++) {
            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];
                if (u < v) {
                    long encodedEdge = ((long) weight[e] << 32) | ((long) u << 16) | (long) v;
                    edges[c++] = encodedEdge;
                }
            }
        }

        Arrays.sort(edges, 0, c);

        UnionFind uf  = new UnionFind(totalVerts);
        List<Edge> mst = new ArrayList<>(totalVerts - 1);

        for (int i = 0; i < c; i++) {
            if (mst.size() == totalVerts - 1) break;

            long e = edges[i];
            int w = (int) (e >> 32);
            int u = (int) ((e >> 16) & 0xFFFF);
            int v = (int) (e & 0xFFFF);

            if (uf.getRoot(u) != uf.getRoot(v)) {
                uf.merge(u, v);
                mst.add(new Edge(u, v, w));
            }
        }
        return mst;
    }

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

    @Override
    public int[] dijkstra(int source) {
        validateVert(source);

        int[]     bestDist = new int[totalVerts];
        boolean[] locked   = new boolean[totalVerts];

        Arrays.fill(bestDist, Integer.MAX_VALUE);
        bestDist[source] = 0;

        IndexedMinHeap pq = new IndexedMinHeap(totalVerts);
        pq.insert(source, 0);

        while (!pq.isEmpty()) {
            int u = pq.extractMin();
            locked[u] = true;

            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];
                int w = weight[e];
                if (!locked[v] && bestDist[u] != Integer.MAX_VALUE) {
                    long candidate = (long) bestDist[u] + w;
                    if (candidate < bestDist[v]) {
                        bestDist[v] = (int) candidate;
                        pq.insertOrDecrease(v, bestDist[v]);
                    }
                }
            }
        }
        return bestDist;
    }

    @Override
    public int[] dagShortestPath(int source) {
        validateVert(source);

        int[] dist = new int[totalVerts];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        byte[] state = new byte[totalVerts];

        int[] stkVert = new int[totalVerts];
        int[] stkEdge = new int[totalVerts];
        int top = 0;

        int[] topoOrder = new int[totalVerts];
        int orderIdx = totalVerts - 1;

        state[source] = 1;
        stkVert[0] = source;
        stkEdge[0] = head[source];

        while (top >= 0) {
            int e = stkEdge[top];

            if (e != -1) {
                stkEdge[top] = next[e];
                int v = to[e];

                if (state[v] == 1) {
                    throw new IllegalStateException("Graph is not a DAG; cycle detected!");
                }
                if (state[v] == 0) {
                    state[v] = 1;
                    top++;
                    stkVert[top] = v;
                    stkEdge[top] = head[v];
                }
            } else {

                state[stkVert[top]] = 2;
                topoOrder[orderIdx--] = stkVert[top];
                top--;
            }
        }

        for (int i = orderIdx + 1; i < totalVerts; i++) {
            int u = topoOrder[i];
            if (dist[u] == Integer.MAX_VALUE) continue;
            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];
                int w = weight[e];
                long candidate = (long) dist[u] + w;
                if (candidate < dist[v]) {
                    dist[v] = (int) candidate;
                }
            }
        }

        return dist;
    }

    private static class IndexedMinHeap {
        int[] pq;
        int[] qp;
        int[] keys;
        int size;

        IndexedMinHeap(int maxVerts) {
            pq = new int[maxVerts + 1];
            qp = new int[maxVerts];
            keys = new int[maxVerts];
            Arrays.fill(qp, -1);
            size = 0;
        }

        boolean isEmpty() { return size == 0; }

        void insert(int v, int key) {
            size++;
            qp[v] = size;
            pq[size] = v;
            keys[v] = key;
            swim(size);
        }

        int extractMin() {
            int min = pq[1];
            swap(1, size--);
            sink(1);
            qp[min] = -1;
            return min;
        }

        void insertOrDecrease(int v, int key) {
            if (qp[v] != -1) {
                keys[v] = key;
                swim(qp[v]);
            } else {
                insert(v, key);
            }
        }

        private void swim(int k) {
            while (k > 1 && keys[pq[k / 2]] > keys[pq[k]]) {
                swap(k, k / 2);
                k = k / 2;
            }
        }

        private void sink(int k) {
            while (2 * k <= size) {
                int j = 2 * k;
                if (j < size && keys[pq[j]] > keys[pq[j + 1]]) j++;
                if (keys[pq[k]] <= keys[pq[j]]) break;
                swap(k, j);
                k = j;
            }
        }

        private void swap(int i, int j) {
            int temp = pq[i];
            pq[i] = pq[j];
            pq[j] = temp;
            qp[pq[i]] = i;
            qp[pq[j]] = j;
        }
    }

    private void validateVert(int v) {
        if (v < 0 || v >= totalVerts) {
            throw new IllegalArgumentException(
                "vertex " + v + " is out of range [0, " + (totalVerts - 1) + "]");
        }
    }

    public int getTotalVerts() { return totalVerts; }

    @Override
    public String toString() {
        return "Graph{totalVerts=" + totalVerts + ", edges=" + edgeCount + "}";
    }
}
