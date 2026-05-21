package com.graph.core.graphsekelton;

import java.util.List;

public interface GraphInterface {

    // use this for undirected stuff (MST graphs)
    void addEdge(int u, int v, int weight);

    // only for DAG building, one direction only
    void addDirectedEdge(int u, int v, int weight);

    // greedy grow from vertex 0
    List<Edge> primMST();

    // sort all edges then union-find
    List<Edge> kruskalMST();

    // classic dijkstra with priority queue
    int[] dijkstra(int source);

    /**
     * Linear-time shortest path — only works on real DAGs.
     *
     * @throws IllegalStateException if a cycle is detected (i.e. you lied about having no cycles)
     */
    // linear time only works on real DAGs, throws if you lied about having no cycles
    int[] dagShortestPath(int source);
}