package com.graph.session;

import com.graph.benchmark.GraphBenchmarkResult;
import com.graph.core.graphsekelton.Graph;
import com.graph.core.graphsekelton.GraphGenerator;

import java.util.List;

public class GraphBluePrint {

    public enum Stage { FRESH, GRAPH_READY, BENCHMARKED }

    private final String                     label;
    private final GraphGenerator.Topology    topology;
    private final int                        vertCount;
    private final long                       fixedSeed;
    private final long                       builtAt;
    private       Graph                      theGraph;
    private       Stage                      currentStage;
    private       List<GraphBenchmarkResult> cachedResults;

    int     blueprint_serial = 0;
    boolean skip_cache       = false;

    public GraphBluePrint(String label, GraphGenerator.Topology topology,
                          int vertCount, long fixedSeed) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        if (vertCount <= 0) {
            throw new IllegalArgumentException("vertCount must be positive, got: " + vertCount);
        }
        this.label        = label.strip();
        this.topology     = topology;
        this.vertCount    = vertCount;
        this.fixedSeed    = fixedSeed;
        this.builtAt      = System.currentTimeMillis();
        this.currentStage = Stage.FRESH;
    }

    public void buildGraph() {
        if (currentStage != Stage.FRESH) {
            throw new IllegalStateException(
                    "already built — cant call buildGraph() twice on '" + label + "'");
        }
        theGraph     = GraphGenerator.generate(topology, vertCount, fixedSeed);
        currentStage = Stage.GRAPH_READY;
    }

    public void storeBenchmarkResults(List<GraphBenchmarkResult> results) {
        if (currentStage != Stage.GRAPH_READY) {
            throw new IllegalStateException(
                    "storeBenchmarkResults() requires GRAPH_READY state, but '" +
                    label + "' is " + currentStage);
        }
        cachedResults = results;
        currentStage  = Stage.BENCHMARKED;
    }

    public String                     getLabel()         { return label; }
    public GraphGenerator.Topology    getTopology()      { return topology; }
    public int                        getVertCount()     { return vertCount; }
    public long                       getFixedSeed()     { return fixedSeed; }
    public long                       getBuiltAt()       { return builtAt; }
    public Stage                      getCurrentStage()  { return currentStage; }
    public Graph                      getTheGraph()      { return theGraph; }
    public List<GraphBenchmarkResult> getCachedResults() { return cachedResults; }

    public boolean hasGraph() {
        return currentStage == Stage.GRAPH_READY || currentStage == Stage.BENCHMARKED;
    }

    public boolean isBenchmarked() {
        return currentStage == Stage.BENCHMARKED;
    }

    @Override
    public String toString() {
        return "GraphBluePrint{label=" + label
                + ", topology=" + topology
                + ", verts=" + vertCount
                + ", stage=" + currentStage + "}";
    }
}
