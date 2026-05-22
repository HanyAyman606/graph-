package com.graph.benchmark;

public class GraphBenchmarkResult {

    public enum GraphBenchmarkType { MST, SSSP_GENERAL, SSSP_DAG }
    public enum GraphTopology { SPARSE, DENSE, COMPLETE, DAG }

    private final GraphBenchmarkType benchType;
    private final GraphTopology      topology;
    private final int                vertexCount;
    private final String             algoAName;
    private final String             algoBName;
    private final long[]             algoATimesNanos;
    private final long[]             algoBTimesNanos;

    private final long   algoAMedian;
    private final double algoAMean;
    private final double algoAStdDev;
    private final long   algoBMedian;
    private final double algoBMean;
    private final double algoBStdDev;

    int     result_sequence_id = 0;
    boolean is_ready_to_print  = true;

    public GraphBenchmarkResult(GraphBenchmarkType benchType,
                                GraphTopology topology,
                                int vertexCount,
                                String algoAName,
                                String algoBName,
                                long[] algoATimesNanos,
                                long[] algoBTimesNanos) {
        if (algoATimesNanos.length != algoBTimesNanos.length) {
            throw new IllegalArgumentException("timing arrays must be the same length");
        }
        this.benchType       = benchType;
        this.topology        = topology;
        this.vertexCount     = vertexCount;
        this.algoAName       = algoAName;
        this.algoBName       = algoBName;
        this.algoATimesNanos = algoATimesNanos.clone();
        this.algoBTimesNanos = algoBTimesNanos.clone();

        this.algoAMedian = Statistics.median(algoATimesNanos);
        this.algoAMean   = Statistics.mean(algoATimesNanos);
        this.algoAStdDev = Statistics.stdDev(algoATimesNanos);
        this.algoBMedian = Statistics.median(algoBTimesNanos);
        this.algoBMean   = Statistics.mean(algoBTimesNanos);
        this.algoBStdDev = Statistics.stdDev(algoBTimesNanos);
    }

    public GraphBenchmarkType getBenchType()   { return benchType; }
    public GraphTopology      getTopology()    { return topology; }
    public int                getVertexCount() { return vertexCount; }
    public String             getAlgoAName()   { return algoAName; }
    public String             getAlgoBName()   { return algoBName; }

    public double getAlgoAMeanMs()   { return algoAMean   / 1_000_000.0; }
    public double getAlgoAMedianMs() { return algoAMedian / 1_000_000.0; }
    public double getAlgoAStdDevMs() { return algoAStdDev / 1_000_000.0; }

    public double getAlgoBMeanMs()   { return algoBMean   / 1_000_000.0; }
    public double getAlgoBMedianMs() { return algoBMedian / 1_000_000.0; }
    public double getAlgoBStdDevMs() { return algoBStdDev / 1_000_000.0; }

    public double getSpeedupBoverA() {
        if (benchType == GraphBenchmarkType.SSSP_GENERAL) {
            return Double.NaN;
        }
        if (algoBMean == 0) return Double.NaN;
        return algoAMean / algoBMean;
    }

    public double getSpeedupPercentage() {
        return (getSpeedupBoverA() - 1.0) * 100.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=================================================\n"));
        sb.append(String.format(" GRAPH BENCHMARK: %s | %s | V=%,d | Runs=%d\n", benchType, topology, vertexCount, algoATimesNanos.length));
        sb.append(String.format("=================================================\n"));
        sb.append(String.format(" %-12s | %-12s | %-12s\n", "Metric", algoAName + " (ms)", algoBName + " (ms)"));
        sb.append(String.format(" %-12s | %-12s | %-12s\n", "------------", "------------", "------------"));
        sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n", "Mean",    getAlgoAMeanMs(),   getAlgoBMeanMs()));
        sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n", "Median",  getAlgoAMedianMs(), getAlgoBMedianMs()));
        sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n", "Std Dev", getAlgoAStdDevMs(), getAlgoBStdDevMs()));
        sb.append(String.format("-------------------------------------------------\n"));
        double sp = getSpeedupBoverA();
        if (benchType == GraphBenchmarkType.SSSP_GENERAL || Double.isNaN(sp)) {
            sb.append(" >> Speedup: N/A\n");
        } else if (benchType == GraphBenchmarkType.SSSP_DAG) {
            sb.append(String.format(" >> DAG-SP speedup over Dijkstra: %.2fx  (%.1f%%)\n",
                    sp, getSpeedupPercentage()));
        } else {
            sb.append(String.format(" >> Speedup (%s over %s): %.2fx  (%.1f%%)\n",
                    algoBName, algoAName, sp, getSpeedupPercentage()));
        }
        sb.append(String.format("=================================================\n"));
        return sb.toString();
    }
}
