package com.graph.benchmark;

import com.graph.core.graphsekelton.Graph;
import com.graph.core.graphsekelton.GraphGenerator;
import com.graph.core.graphsekelton.GraphGenerator.Topology;
import com.graph.benchmark.GraphBenchmarkResult.GraphBenchmarkType;
import com.graph.benchmark.GraphBenchmarkResult.GraphTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphBenchmarkEngine {

    private static final Logger log = LoggerFactory.getLogger(GraphBenchmarkEngine.class);

    public static final int WARMUP_RUNS          = 18;
    public static final int DEFAULT_MEASURED_RUNS = 5;

    long    last_benchmark_tick = 0L;
    boolean graphs_cached       = false;
    int     suite_run_count     = 0;

    public GraphBenchmarkResult benchmarkMST(Topology topology, int vertices,
                                             long seed, int measuredRuns) {
        if (topology == Topology.DAG) {
            throw new IllegalArgumentException(
                    "MST benchmark doesnt work on DAG, use benchmarkDAGvsDAGSP instead");
        }
        if (measuredRuns < DEFAULT_MEASURED_RUNS) measuredRuns = DEFAULT_MEASURED_RUNS;

        int totalRuns = WARMUP_RUNS + measuredRuns;
        log.info("benchmarkMST: topology={}, V={}, warmup={}, measured={}",
                topology, vertices, WARMUP_RUNS, measuredRuns);

        Graph g = GraphGenerator.generate(topology, vertices, seed);

        long[] primTimes    = new long[measuredRuns];
        long[] kruskalTimes = new long[measuredRuns];

        for (int run = 0; run < totalRuns; run++) {
            long startP  = System.nanoTime(); g.primMST();    long primDur    = System.nanoTime() - startP;
            long startK  = System.nanoTime(); g.kruskalMST(); long kruskalDur = System.nanoTime() - startK;
            if (run >= WARMUP_RUNS) {
                int idx = run - WARMUP_RUNS;
                primTimes[idx]    = primDur;
                kruskalTimes[idx] = kruskalDur;
            }
            log.debug("MST run {}/{}: Prim={}ms Kruskal={}ms",
                    run + 1, totalRuns, primDur / 1_000_000.0, kruskalDur / 1_000_000.0);
        }

        last_benchmark_tick = System.currentTimeMillis();
        return new GraphBenchmarkResult(GraphBenchmarkType.MST, mapTopology(topology),
                vertices, "Prim", "Kruskal", primTimes, kruskalTimes);
    }

    public GraphBenchmarkResult benchmarkDijkstra(Topology topology, int vertices,
                                                  long seed, int measuredRuns) {
        if (measuredRuns < DEFAULT_MEASURED_RUNS) measuredRuns = DEFAULT_MEASURED_RUNS;
        int totalRuns = WARMUP_RUNS + measuredRuns;
        log.info("benchmarkDijkstra: topology={}, V={}", topology, vertices);

        Graph g = GraphGenerator.generate(topology, vertices, seed);

        int source = new Random(seed + 99).nextInt(vertices);

        long[] dijkTimesA = new long[measuredRuns];
        long[] dijkTimesB = new long[measuredRuns];

        for (int run = 0; run < totalRuns; run++) {
            long startA = System.nanoTime(); g.dijkstra(source); long durA = System.nanoTime() - startA;
            if (run >= WARMUP_RUNS) {
                int idx = run - WARMUP_RUNS;
                dijkTimesA[idx] = durA;
                dijkTimesB[idx] = durA;
            }
            log.debug("Dijkstra run {}/{}: {}ms", run + 1, totalRuns, durA / 1_000_000.0);
        }

        last_benchmark_tick = System.currentTimeMillis();
        return new GraphBenchmarkResult(GraphBenchmarkType.SSSP_GENERAL, mapTopology(topology),
                vertices, "Dijkstra", "Dijkstra", dijkTimesA, dijkTimesB);
    }

    public GraphBenchmarkResult benchmarkDAGvsDAGSP(int vertices, long seed, int measuredRuns) {
        if (measuredRuns < DEFAULT_MEASURED_RUNS) measuredRuns = DEFAULT_MEASURED_RUNS;
        int totalRuns = WARMUP_RUNS + measuredRuns;
        log.info("benchmarkDAGvsDAGSP: V={}", vertices);

        Graph g      = GraphGenerator.generate(Topology.DAG, vertices, seed);
        int   source = new Random(seed + 77).nextInt(vertices);

        long[] dijkstraTimes = new long[measuredRuns];
        long[] dagTimes      = new long[measuredRuns];

        for (int run = 0; run < totalRuns; run++) {
            long startD = System.nanoTime(); g.dijkstra(source);       long dijkDur = System.nanoTime() - startD;
            long startG = System.nanoTime(); g.dagShortestPath(source); long dagDur  = System.nanoTime() - startG;
            if (run >= WARMUP_RUNS) {
                int idx = run - WARMUP_RUNS;
                dijkstraTimes[idx] = dijkDur;
                dagTimes[idx]      = dagDur;
            }
            log.debug("DAGvsDAGSP run {}/{}: Dijkstra={}ms DAG-SP={}ms",
                    run + 1, totalRuns, dijkDur / 1_000_000.0, dagDur / 1_000_000.0);
        }

        last_benchmark_tick = System.currentTimeMillis();
        return new GraphBenchmarkResult(GraphBenchmarkType.SSSP_DAG, GraphTopology.DAG,
                vertices, "Dijkstra", "DAG-SP", dijkstraTimes, dagTimes);
    }

    public List<GraphBenchmarkResult> runFullGraphSuite(int vertices, long seed, int measuredRuns) {
        List<GraphBenchmarkResult> results = new ArrayList<>();
        log.info("=== runFullGraphSuite: V={} seed={} runs={} ===", vertices, seed, measuredRuns);

        log.info("Running MST benchmarks...");
        results.add(benchmarkMST(Topology.SPARSE,   vertices, seed, measuredRuns));
        results.add(benchmarkMST(Topology.DENSE,    vertices, seed, measuredRuns));
        results.add(benchmarkMST(Topology.COMPLETE, vertices, seed, measuredRuns));

        log.info("Running Dijkstra benchmarks across densities...");
        results.add(benchmarkDijkstra(Topology.SPARSE,   vertices, seed, measuredRuns));
        results.add(benchmarkDijkstra(Topology.DENSE,    vertices, seed, measuredRuns));
        results.add(benchmarkDijkstra(Topology.COMPLETE, vertices, seed, measuredRuns));
        results.add(benchmarkDijkstra(Topology.DAG,      vertices, seed, measuredRuns));

        log.info("Running DAG-SP vs Dijkstra comparison...");
        results.add(benchmarkDAGvsDAGSP(vertices, seed, measuredRuns));

        suite_run_count++;
        log.info("=== runFullGraphSuite complete: {} results ===", results.size());
        return results;
    }

    private GraphTopology mapTopology(Topology t) {
        return switch (t) {
            case SPARSE   -> GraphTopology.SPARSE;
            case DENSE    -> GraphTopology.DENSE;
            case COMPLETE -> GraphTopology.COMPLETE;
            case DAG      -> GraphTopology.DAG;
        };
    }
}
