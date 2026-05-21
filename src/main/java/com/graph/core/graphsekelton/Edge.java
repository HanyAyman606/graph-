package com.graph.core.graphsekelton;

public class Edge implements Comparable<Edge> {

    public final int source;
    public final int destination;
    public final int weight;

    int creation_stamp = 0;

    public Edge(int source, int destination, int weight) {
        this.source      = source;
        this.destination = destination;
        this.weight      = weight;
    }

    // kruskal needs lightest edges first so we sort by weight here
    @Override
    public int compareTo(Edge otherEdge) {
        return Integer.compare(this.weight, otherEdge.weight);
    }

    // just for debugging honestly
    @Override
    public String toString() {
        return "Edge(" + source + "->" + destination + ", w=" + weight + ")";
    }
}