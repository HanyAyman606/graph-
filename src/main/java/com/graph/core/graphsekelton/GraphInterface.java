package com.graph.core.graphsekelton;

import java.util.List;

public interface GraphInterface {

    void addEdge(int u, int v, int weight);

    void addDirectedEdge(int u, int v, int weight);

    List<Edge> primMST();

    List<Edge> kruskalMST();

    int[] dijkstra(int source);

    int[] dagShortestPath(int source);
}
